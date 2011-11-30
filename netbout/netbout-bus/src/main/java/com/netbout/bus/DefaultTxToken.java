/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.bus;

import com.netbout.spi.Plain;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang.StringUtils;

/**
 * Default token.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class DefaultTxToken implements TxToken {

    /**
     * Transaction mnemo.
     */
    private final transient String imnemo;

    /**
     * List of arguments.
     */
    private final transient List<Plain<?>> args =
        new CopyOnWriteArrayList<Plain<?>>();

    /**
     * Result.
     */
    private transient Plain<?> done;

    /**
     * Public ctor.
     * @param mnemo Mnemo-code of the request
     * @param arguments The arguments
     * @param config List of attributes
     */
    public DefaultTxToken(final String mnemo, final List<Plain<?>> arguments) {
        this.imnemo = mnemo;
        this.args.addAll(arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s[(%s)]",
            this.mnemo(),
            StringUtils.join(this.args, ")(")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String mnemo() {
        return this.imnemo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plain<?> arg(final int pos) {
        return this.args.get(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void result(final Plain<?> res) {
        this.done = res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompleted() {
        return this.done != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plain<?> getResult() {
        return this.done;
    }

}
