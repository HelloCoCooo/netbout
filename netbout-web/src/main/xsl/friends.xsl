<?xml version="1.0"?>
<!--
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
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" version="1.0">
    <xsl:template match="friends">
        <div class="friends">
            <xsl:apply-templates select="friend"/>
        </div>
    </xsl:template>
    <xsl:template match="friend">
        <div class="friend" style="left:{(position()-1) * 57}px">
            <img alt="{alias}" src="{links/link[@rel='photo']/@href}"/>
            <span class="bar">
                <xsl:call-template name="crop">
                    <xsl:with-param name="text" select="alias"/>
                    <xsl:with-param name="length" select="25"/>
                </xsl:call-template>
                <xsl:if test="links/link[@rel='kick']">
                    <xsl:text> </xsl:text>
                    <a class="kick">
                        <xsl:attribute name="href">
                            <xsl:value-of select="links/link[@rel='kick']/@href"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:call-template name="format">
                                <xsl:with-param name="text" select="'kick.X.off.this.bout'"/>
                                <xsl:with-param name="value" select="alias"/>
                            </xsl:call-template>
                        </xsl:attribute>
                        <xsl:text>x</xsl:text>
                    </a>
                </xsl:if>
            </span>
        </div>
        <xsl:text> </xsl:text>
    </xsl:template>
</xsl:stylesheet>
