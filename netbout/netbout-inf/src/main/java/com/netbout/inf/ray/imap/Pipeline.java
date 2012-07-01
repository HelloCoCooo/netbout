/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.inf.ray.imap;

import com.jcabi.log.Logger;
import com.netbout.inf.Attribute;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Pipeline used by {@link Draft} during baselining.
 *
 * <p>Class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
final class Pipeline implements Closeable, Iterator<Catalog.Item> {

    /**
     * Token.
     */
    interface Token {
        /**
         * Convert it to item.
         * @return The item
         * @throws IOException If some IO problem inside
         */
        Catalog.Item item() throws IOException;
    }

    /**
     * Comparable token.
     */
    interface ComparableToken
        extends Pipeline.Token, Comparable<Pipeline.Token> {
    }

    /**
     * Source draft.
     */
    private final transient Draft draft;

    /**
     * Source catalog.
     */
    private final transient Catalog catalog;

    /**
     * The attribute.
     */
    private final transient Attribute attribute;

    /**
     * Backlog with data.
     */
    private final transient Iterator<Backlog.Item> biterator;

    /**
     * Pre-existing catalog with data.
     */
    private final transient Iterator<Catalog.Item> citerator;

    /**
     * Pre-existing data file.
     */
    private final transient RandomAccessFile data;

    /**
     * Output stream.
     */
    private final transient OutputStream output;

    /**
     * Current position in output stream.
     */
    private final transient AtomicLong opos = new AtomicLong();

    /**
     * Information about the item already retrieved from iterators.
     */
    private final transient AtomicReference<Pipeline.Token> token =
        new AtomicReference<Pipeline.Token>();

    /**
     * Value retrieved in previous call to {@link #next()}.
     */
    private final transient AtomicReference<Catalog.Item> ahead =
        new AtomicReference<Catalog.Item>();

    /**
     * Public ctor.
     * @param drft Draft to use for data
     * @param dest Destination
     * @param src Source
     * @param attr Attribute
     * @throws IOException If some IO problem inside
     * @checkstyle ParameterNumber (4 lines)
     */
    public Pipeline(final Draft drft, final Baseline dest, final Baseline src,
        final Attribute attr) throws IOException {
        this.attribute = attr;
        this.catalog = src.catalog(this.attribute);
        this.draft = drft;
        this.biterator = this.reordered(
            this.draft.backlog(this.attribute).iterator()
        );
        this.citerator = this.catalog.iterator();
        this.data = new RandomAccessFile(src.data(this.attribute), "r");
        this.output = new FileOutputStream(dest.data(this.attribute));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.data.close();
        this.output.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return this.biterator.hasNext() || this.citerator.hasNext()
            || this.token.get() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog.Item next() {
        synchronized (this.token) {
            Catalog.Item item = null;
            while (this.hasNext()) {
                try {
                    item = this.fetch();
                } catch (java.io.IOException ex) {
                    throw new IllegalStateException(ex);
                }
                if (this.ahead.get() == null) {
                    this.ahead.set(item);
                }
                if (!item.value().equals(this.ahead.get().value())) {
                    item = this.ahead.getAndSet(item);
                    break;
                }
            }
            if (item == null) {
                throw new NoSuchElementException();
            }
            return item;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("#remove");
    }

    /**
     * Fetch next item from one of two iterators.
     *
     * <p>This method is merging two iterators, sorting elements according
     * to their hash codes. Catalog iterator {@code this.citerator} has
     * higher priority. We keep items retrieved from iterators in
     * {@code this.token} variable. When variable is set to null it means
     * that we should to one of two iterators for the next value. If
     * the variable holds some value - it is a candidate for the next
     * result of this {@code next()} method.
     *
     * @return The item fetched
     * @throws IOException If some IO problem inside
     */
    private Catalog.Item fetch() throws IOException {
        if (this.token.get() == null) {
            if (this.citerator.hasNext()) {
                this.token.set(this.cattoken(this.citerator));
            } else if (this.biterator.hasNext()) {
                this.token.set(this.backtoken(this.biterator));
            } else {
                throw new NoSuchElementException();
            }
        }
        Catalog.Item next;
        final Pipeline.Token saved = this.token.get();
        if (saved instanceof Pipeline.ComparableToken
            && this.biterator.hasNext()) {
            final Pipeline.ComparableToken comparable =
                Pipeline.ComparableToken.class.cast(saved);
            final Token btoken = this.backtoken(this.biterator);
            if (comparable.compareTo(btoken) > 0) {
                next = btoken.item();
            } else {
                next = comparable.item();
                this.token.set(btoken);
            }
        } else {
            next = saved.item();
            this.token.set(null);
        }
        return next;
    }

    /**
     * Get token from catalog.
     *
     * <p>We don't close the input stream here, because such a closing
     * operation will lead the closing of the entire
     * {@link RandomAccessFile} ({@code Pipeline.this.data}).
     *
     * @param iterator The iterator to read from
     * @return Token retrieved
     */
    private Token cattoken(final Iterator<Catalog.Item> iterator) {
        final Catalog.Item item = iterator.next();
        // @checkstyle AnonInnerLength (50 lines)
        return new ComparableToken() {
            @Override
            public Catalog.Item item() throws IOException {
                final long pos =
                    Pipeline.this.catalog.seek(item.value());
                Pipeline.this.data.seek(pos);
                final InputStream input = Channels.newInputStream(
                    Pipeline.this.data.getChannel()
                );
                final int len = IOUtils.copy(
                    input,
                    Pipeline.this.output
                );
                Logger.debug(
                    this,
                    "#item(): copied %d bytes from pos #%d ('%[text]s')",
                    len,
                    pos,
                    item.value()
                );
                return new Catalog.Item(
                    item.value(),
                    Pipeline.this.opos.getAndAdd(len)
                );
            }
            @Override
            public int compareTo(final Token tkn) {
                return new Integer(this.hashCode()).compareTo(
                    new Integer(tkn.hashCode())
                );
            }
            @Override
            public int hashCode() {
                return item.hashCode();
            }
            @Override
            public boolean equals(final Object tkn) {
                return this == tkn || tkn.hashCode() == this.hashCode();
            }
        };
    }

    /**
     * Get token from backlog.
     * @param iterator The iterator to read from
     * @return Token retrieved
     */
    private Token backtoken(final Iterator<Backlog.Item> iterator) {
        final Backlog.Item item = iterator.next();
        // @checkstyle AnonInnerLength (50 lines)
        return new Token() {
            @Override
            public Catalog.Item item() throws IOException {
                final File file = Pipeline.this.draft.numbers(
                    Pipeline.this.attribute, item.path()
                );
                final InputStream input = new FileInputStream(file);
                final int len = IOUtils.copy(
                    input,
                    Pipeline.this.output
                );
                input.close();
                Logger.debug(
                    this,
                    "#item('%s'): copied %d bytes from '/%s' ('%[text]s')",
                    Pipeline.this.attribute,
                    len,
                    FilenameUtils.getName(file.getPath()),
                    item.value()
                );
                return new Catalog.Item(
                    item.value(),
                    Pipeline.this.opos.getAndAdd(len)
                );
            }
            @Override
            public int hashCode() {
                return item.hashCode();
            }
            @Override
            public boolean equals(final Object tkn) {
                return this == tkn || tkn.hashCode() == this.hashCode();
            }
        };
    }

    /**
     * Create reordered iterator.
     * @param origin The iterator to read from
     * @return Reordered iterator
     */
    private Iterator<Backlog.Item> reordered(
        final Iterator<Backlog.Item> origin) {
        final List<Backlog.Item> items = new LinkedList<Backlog.Item>();
        final Comparator<Backlog.Item> comp = new Comparator<Backlog.Item>() {
            public int compare(final Backlog.Item left,
                final Backlog.Item right) {
                return left.value().compareTo(right.value());
            }
        };
        while (origin.hasNext()) {
            final Backlog.Item item = origin.next();
            final int idx = Collections.binarySearch(items, item, comp);
            if (idx > 0) {
                items.remove(idx);
            }
            items.add(item);
        }
        Collections.sort(items);
        return items.iterator();
    }

}