/**
 * Copyright (c) 2009-2017, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.client.retry;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.netbout.spi.Attachment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

/**
 * Cached attachment.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.3
 */
@Immutable
@ToString(includeFieldNames = false)
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "origin")
public final class ReAttachment implements Attachment {

    /**
     * Original object.
     */
    private final transient Attachment origin;

    /**
     * Public ctor.
     * @param orgn Original object
     */
    public ReAttachment(final Attachment orgn) {
        this.origin = orgn;
    }

    @Override
    @RetryOnFailure(
        verbose = false, attempts = Tv.TWENTY,
        delay = Tv.FIVE, unit = TimeUnit.SECONDS
    )
    public String name() throws IOException {
        return this.origin.name();
    }

    @Override
    @RetryOnFailure(
        verbose = false, attempts = Tv.TWENTY,
        delay = Tv.FIVE, unit = TimeUnit.SECONDS
    )
    public String ctype() throws IOException {
        return this.origin.ctype();
    }

    @Override
    @RetryOnFailure(
        verbose = false, attempts = Tv.TWENTY,
        delay = Tv.FIVE, unit = TimeUnit.SECONDS
    )
    public String etag() throws IOException {
        return this.origin.etag();
    }

    @Override
    @RetryOnFailure(
        verbose = false, attempts = Tv.TWENTY,
        delay = Tv.FIVE, unit = TimeUnit.SECONDS
    )
    public boolean unseen() throws IOException {
        return this.origin.unseen();
    }

    @Override
    @RetryOnFailure(
        verbose = false, attempts = Tv.TWENTY,
        delay = Tv.FIVE, unit = TimeUnit.SECONDS
    )
    public Date date() throws IOException {
        return this.origin.date();
    }

    @Override
    @RetryOnFailure(
        verbose = false, attempts = Tv.TWENTY,
        delay = Tv.FIVE, unit = TimeUnit.SECONDS
    )
    public String author() throws IOException {
        return this.origin.author();
    }

    @Override
    @RetryOnFailure(
        verbose = false, attempts = Tv.TWENTY,
        delay = Tv.FIVE, unit = TimeUnit.SECONDS
    )
    public InputStream read() throws IOException {
        return this.origin.read();
    }

    @Override
    public void write(final InputStream stream, final String ctype,
        final String etag) throws IOException {
        this.write(IOUtils.toByteArray(stream), ctype, etag);
    }

    /**
     * Write a byte array.
     * @param bytes Bytes to write
     * @param ctype Content type
     * @param etag Etag
     * @throws IOException If fails
     */
    @RetryOnFailure(
        verbose = false, attempts = Tv.TWENTY,
        delay = Tv.FIVE, unit = TimeUnit.SECONDS
    )
    private void write(final byte[] bytes, final String ctype,
        final String etag) throws IOException {
        this.origin.write(new ByteArrayInputStream(bytes), ctype, etag);
    }

}
