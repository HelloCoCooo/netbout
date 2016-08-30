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
import com.netbout.spi.User;
import java.util.LinkedList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

/**
 * Integration case for {@link RtInbox}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 */
public final class RtInboxITCase {

    /**
     * Netbout rule.
     * @checkstyle VisibilityModifierCheck (3 lines)
     */
    @Rule
    public final transient NbRule rule = new NbRule();

    /**
     * RtInbox can list bouts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void listsBouts() throws Exception {
        final User user = this.rule.get();
        final Alias alias = user.aliases().iterate().iterator().next();
        final Inbox inbox = alias.inbox();
        MatcherAssert.assertThat(
            inbox.iterate(),
            Matchers.<Bout>iterableWithSize(Matchers.greaterThan(1))
        );
    }

    /**
     * RtInbox can search for bouts.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void searchesBouts() throws Exception {
        final Inbox inbox =
            this.rule.get().aliases().iterate().iterator().next().inbox();
        final Bout first = inbox.bout(inbox.start());
        final String firsttitle = "bout1 title";
        first.rename(firsttitle);
        first.messages().post("hello");
        final Bout second = inbox.bout(inbox.start());
        final String secondtitle = "bout2 title";
        second.rename(secondtitle);
        second.messages().post("message with term");
        final String thirdtitle = "bout title with term";
        inbox.bout(inbox.start()).rename(thirdtitle);
        final List<String> titles = new LinkedList<>();
        for (final Bout bout : inbox.search("term")) {
            titles.add(bout.title());
        }
        MatcherAssert.assertThat(
            titles,
            Matchers.hasItems(secondtitle, thirdtitle)
        );
        MatcherAssert.assertThat(
            titles,
            Matchers.not(Matchers.hasItem(firsttitle))
        );
    }
}
