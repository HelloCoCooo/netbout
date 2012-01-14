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
package com.netbout.hub;

import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import java.util.Date;

/**
 * Message used in searches.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class StubMessage implements Message {

    /**
     * The bout where this message is located.
     */
    private final transient Bout ibout;

    /**
     * Public ctor.
     * @param bout The bout where this message is located
     */
    public StubMessage(final Bout bout) {
        this.ibout = bout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Message msg) {
        return this.ibout.date().compareTo(msg.date());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout bout() {
        return this.ibout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity author() {
        return this.ibout.participants().iterator().next().identity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String text() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return this.ibout.date();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean seen() {
        return true;
    }

}
