/**
 * Copyright (c) 2009-2016, netbout.com
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
package com.netbout.client;

import com.netbout.spi.Alias;
import com.netbout.spi.Bout;
import com.netbout.spi.Inbox;
import com.netbout.spi.Message;
import com.netbout.spi.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration case for {@link RtMessages}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 */
public final class RtMessagesITCase {

    /**
     * Netbout rule.
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @Rule
    public final transient NbRule rule = new NbRule();

    /**
     * RtMessages can list messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void listsMessagesInBout() throws Exception {
        final User user = this.rule.get();
        final Alias alias = user.aliases().iterate().iterator().next();
        final Inbox inbox = alias.inbox();
        inbox.start();
        final Bout bout = inbox.iterate().iterator().next();
        bout.messages().post("hey, how are you?");
        bout.messages().post("I'm fine");
        MatcherAssert.assertThat(
            bout.messages().iterate(),
            Matchers.<Message>iterableWithSize(Matchers.greaterThan(1))
        );
    }

}
