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
package com.netbout.client;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.log.Logger;
import com.netbout.spi.Attachment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

/**
 * REST attachment.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.0
 * @todo #1090:30min There is code duplication in public methods in this class:
 *  ctype(), etag(), unseen(), date(), author(), read() and write(). Please
 *  refactor to avoid it.
 */
@Immutable
@ToString(includeFieldNames = false)
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "request", "attachment" })
final class RtAttachment implements Attachment {

    /**
     * Request to use.
     */
    private final transient Request request;

    /**
     * Its name.
     */
    private final transient String attachment;

    /**
     * Public ctor.
     * @param req Request to use
     * @param name Its name
     */
    RtAttachment(final Request req, final String name) {
        this.request = req;
        this.attachment = name;
    }

    @Override
    public String name() {
        return this.attachment;
    }

    @Override
    public String ctype() throws IOException {
        return this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .xml()
            .xpath(this.xpath("ctype/text()"))
            .get(0);
    }

    @Override
    public String etag() throws IOException {
        return this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .xml()
            .xpath(this.xpath("etag/text()"))
            .get(0);
    }

    @Override
    public boolean unseen() throws IOException {
        return Boolean.parseBoolean(
            this.request.fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .xml()
                .xpath(this.xpath("unseen/text()"))
                .get(0)
        );
    }

    @Override
    public Date date() throws IOException {
        return new Date(
            Long.parseLong(
                this.request.fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .as(XmlResponse.class)
                    .xml()
                    .xpath(this.xpath("date/text()"))
                    .get(0)
            )
        );
    }

    @Override
    public String author() throws IOException {
        return this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .xml()
            .xpath(this.xpath("author/text()"))
            .get(0);
    }

    @Override
    public InputStream read() throws IOException {
        return new ByteArrayInputStream(
            this.request.fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .rel(this.xpath("links/link[@rel='download']/@href"))
                .fetch().binary()
        );
    }

    @Override
    public void write(final InputStream stream, final String ctype,
        final String etag) throws IOException {
        this.request.fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .rel("/page/links/link[@rel='upload']/@href")
            .uri()
            .queryParam("name", this.name())
            .queryParam("ctype", ctype)
            .queryParam("etag", etag)
            .back()
            .body().set(IOUtils.toByteArray(stream)).back()
            .method(Request.POST)
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        Logger.info(this, "attachment '%s' written", this.attachment);
    }

    /**
     * Xpath of the attachment in the page.
     * @param path Path to append
     * @return XPath
     */
    private String xpath(final String path) {
        return String.format(
            "/page/bout/attachments/attachment[name='%s']/%s",
            this.attachment, path
        );
    }
}
