/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.mockito.Mockito;

/**
 * Mocker of {@link RestClient}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RestClientMocker {

    /**
     * Paths.
     */
    private final transient ConcurrentMap<String, List<String>> paths =
        new ConcurrentHashMap<String, List<String>>();

    /**
     * Return this value on XPath.
     * @param xpath The xpath
     * @param text What to return
     * @return This object
     */
    public RestClientMocker onXPath(final String xpath, final String text) {
        final List<String> list = new ArrayList<String>();
        list.add(text);
        this.paths.put(xpath, list);
        return this;
    }

    /**
     * Mock it.
     * @return Mocked client
     */
    public RestClient mock() {
        final RestResponse response = Mockito.mock(RestResponse.class);
        Mockito.doReturn(response).when(response)
            .assertXPath(Mockito.anyString());
        Mockito.doReturn(response).when(response)
            .assertStatus(Mockito.anyInt());
        for (ConcurrentMap.Entry<String, List<String>> entry
            : this.paths.entrySet()) {
            Mockito.doReturn(entry.getValue()).when(response)
                .xpath(entry.getKey());
        }
        final RestClient client = Mockito.mock(RestClient.class);
        Mockito.doReturn(response).when(client).get(Mockito.anyString());
        Mockito.doReturn(response).when(client)
            .post(Mockito.anyString(), (String) Mockito.anyVararg());
        return client;
    }

}
