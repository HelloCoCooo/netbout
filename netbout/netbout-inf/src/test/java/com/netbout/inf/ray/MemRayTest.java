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
package com.netbout.inf.ray;

import com.netbout.inf.Cursor;
import com.netbout.inf.FolderMocker;
import com.netbout.inf.Ray;
import com.netbout.inf.TermBuilder;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case of {@link MemRay}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MemRayTest {

    /**
     * MemRay can store and find.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void storesAndFinds() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final Long number = Math.abs(new Random().nextLong());
        final String attribute = "some-attribute";
        final String value = "some value to set, \u0433!";
        ray.msg(1L);
        ray.cursor().add(
            ray.builder().matcher(TermBuilder.NUMBER, number.toString()),
            attribute,
            value
        );
        final Cursor cursor = ray.cursor().shift(
            ray.builder().matcher(attribute, value)
        );
        MatcherAssert.assertThat(cursor.valid(), Matchers.is(true));
        MatcherAssert.assertThat(cursor.end(), Matchers.is(false));
        MatcherAssert.assertThat(
            cursor.msg().number(),
            Matchers.equalTo(number)
        );
        MatcherAssert.assertThat(
            cursor.msg().first(attribute),
            Matchers.equalTo(value)
        );
        ray.close();
    }

}
