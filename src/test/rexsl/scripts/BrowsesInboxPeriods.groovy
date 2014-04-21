/**
 * Copyright (c) 2009-2014, Netbout.com
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
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
package com.netbout.rest.rexsl.scripts

import com.jcabi.urn.URN
import com.netbout.client.EtaAssertion
import com.netbout.client.RestSession
import com.netbout.client.RestUriBuilder
import com.rexsl.test.RestTester
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

def leon = new RestSession(rexsl.home).authenticate(new URN('urn:test:leon'), '')

RestTester.start(RestUriBuilder.from(leon))
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read first inbox page, with many bouts')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertThat(new EtaAssertion())
    .assertXPath('/page/identity[name="urn:test:leon"]')
    .assertXPath('/page/identity/locale')
    .assertXPath('/page/bouts[count(bout) = 5]')
    .assertXPath('/page/bouts/bout[position() = 1 and number = 908]')
    .assertXPath('/page/bouts/bout[position() = 2 and number = 907]')
    .assertXPath('/page/bouts/bout[position() = 3 and number = 906]')
    .assertXPath('/page/bouts/bout[position() = 4 and number = 905]')
    .assertXPath('/page/bouts/bout[position() = 5 and number = 904]')
    .assertXPath('/page/periods/link[@rel="more"]')
    //.assertXPath('/page/periods/link[@rel="earliest"]')
    .rel('/page/periods/link[@rel="more"]/@href')
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .get('read second page of inbox')
    .assertStatus(HttpURLConnection.HTTP_OK)
    .assertXPath('//bouts[count(bout) > 1]')
    .assertXPath('/page/bouts/bout[position() = 1 and number = 903]')
    .assertXPath('/page/bouts/bout[position() = 2 and number = 902]')
    .assertXPath('/page/bouts/bout[position() = 3 and number = 901]')
    .assertXPath('/page/view[. != ""]')