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
package com.netbout.hub;

import com.jcabi.urn.URN;
import com.jcabi.urn.URNMocker;
import com.netbout.spi.Identity;
import java.util.Random;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link BoutMgr}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutMgrMocker {

    /**
     * The object/mock.
     */
    private final transient BoutMgr mgr = Mockito.mock(BoutMgr.class);

    /**
     * Author of all bouts.
     */
    private transient URN author = new URNMocker().mock();

    /**
     * With this author in all created bouts.
     * @param name The name of author
     * @return This object
     */
    public BoutMgrMocker withAuthor(final URN name) {
        this.author = name;
        return this;
    }

    /**
     * Build it.
     * @return The mock
     */
    public BoutMgr mock() {
        Mockito.doReturn(Math.abs(new Random().nextLong()))
            .when(this.mgr).create(Mockito.any(URN.class));
        try {
            Mockito.doAnswer(
                new Answer<BoutDt>() {
                    public BoutDt answer(final InvocationOnMock invocation) {
                        final Long num = (Long) invocation.getArguments()[0];
                        return new BoutDtMocker()
                            .withNumber(num)
                            .withParticipant(
                                new ParticipantDtMocker()
                                    .withIdentity(BoutMgrMocker.this.author)
                                    .mock()
                            )
                            .mock();
                    }
                }
            ).when(this.mgr).find(Mockito.anyLong());
        } catch (Identity.BoutNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this.mgr;
    }

}