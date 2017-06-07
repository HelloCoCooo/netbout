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
package com.netbout.dynamo;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import com.netbout.spi.Alias;
import com.netbout.spi.Friend;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Dynamo friend.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = "name")
@EqualsAndHashCode(of = { "table", "name" })
final class DyFriend implements Friend {

    /**
     * Table with aliases.
     */
    private final transient Table table;

    /**
     * This alias.
     */
    private final transient String name;

    /**
     * Ctor.
     * @param region Region we're in
     * @param alias Alias
     */
    DyFriend(final Region region, final String alias) {
        this.table = region.table(DyAliases.TBL);
        this.name = alias;
    }

    @Override
    public String alias() {
        return this.name;
    }

    @Override
    public URI photo() throws IOException {
        final Iterator<Item> items = this.table.frame()
            .where(DyAliases.HASH, this.name)
            .through(
                new QueryValve()
                    .withLimit(1)
                    .withAttributesToGet(DyAliases.ATTR_PHOTO)
            )
            .iterator();
        final URI uri;
        if (items.hasNext()) {
            uri = URI.create(items.next().get(DyAliases.ATTR_PHOTO).getS());
        } else {
            uri = Alias.BLANK;
        }
        return uri;
    }

    @Override
    public String email() throws IOException {
        final Iterator<Item> items = this.table.frame()
            .where(DyAliases.HASH, this.name)
            .through(
                new QueryValve()
                    .withLimit(1)
                    .withAttributesToGet(DyAliases.ATTR_EMAIL)
            )
            .iterator();
        String email = "";
        if (items.hasNext()) {
            final Item item = items.next();
            if (item.has(DyAliases.ATTR_EMAIL)) {
                email = item.get(DyAliases.ATTR_EMAIL).getS();
            }
        }
        if (email.contains("!")) {
            email = email.substring(0, email.indexOf('!'));
        }
        return email;
    }
}
