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
package com.netbout.db;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.NotEmptyHandler;
import com.jcabi.jdbc.SingleHandler;
import com.jcabi.jdbc.Utc;
import com.jcabi.jdbc.VoidHandler;
import com.jcabi.urn.URN;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Manipulations on the level of identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Farm
public final class IdentityFarm {

    /**
     * Find identities by keyword.
     * @param who Who is searching for them
     * @param keyword The keyword
     * @return List of identities
     * @throws SQLException If fails
     */
    @Operation("find-identities-by-keyword")
    public List<URN> findIdentitiesByKeyword(final URN who,
        final String keyword) throws SQLException {
        final String matcher = String.format(
            "%%%s%%",
            keyword.toUpperCase(Locale.ENGLISH)
        );
        return new JdbcSession(Database.source()).sql(
            // @checkstyle StringLiteralsConcatenation (13 lines)
            // @checkstyle LineLength (11 lines)
            "SELECT identity.name FROM identity"
            + " LEFT JOIN participant p1 ON p1.identity = identity.name"
            + " LEFT JOIN participant p2 ON p2.identity = ? AND p2.bout = p1.bout"
            + " LEFT JOIN alias ON alias.identity = identity.name"
            + " WHERE UCASE(identity.name) = ? OR"
            + " (p2.identity IS NOT NULL"
            + "  AND UCASE(alias.name) LIKE ?"
            + "  AND (identity.name LIKE 'urn:facebook:%' OR identity.name LIKE 'urn:test:%'))"
            + " GROUP BY identity.name"
            + " ORDER BY COUNT(p1.bout) DESC"
            + " LIMIT 10"
        )
            .set(who)
            .set(keyword.toUpperCase(Locale.ENGLISH))
            .set(matcher)
            .select(new NamesHandler());
    }

    /**
     * Get identity photo.
     * @param name The name of the identity
     * @return Photo of the identity
     * @throws SQLException If fails
     */
    @Operation("get-identity-photo")
    public URL getIdentityPhoto(final URN name) throws SQLException {
        return new JdbcSession(Database.source())
            .sql("SELECT photo FROM identity WHERE name = ?")
            .set(name)
            .select(
                new JdbcSession.Handler<URL>() {
                    @Override
                    public URL handle(final ResultSet rset)
                        throws SQLException {
                        if (!rset.next()) {
                            throw new IllegalArgumentException(
                                String.format(
                                    "Identity '%s' not found, can't read photo",
                                    name
                                )
                            );
                        }
                        try {
                            return new URL(rset.getString(1));
                        } catch (java.net.MalformedURLException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            );
    }

    /**
     * Identity was mentioned in the app and should be registed here.
     * @param name The name of identity
     * @throws SQLException If fails
     */
    @Operation("identity-mentioned")
    public void identityMentioned(final URN name) throws SQLException {
        final Boolean exists = new JdbcSession(Database.source())
            .sql("SELECT name FROM identity WHERE name = ?")
            .set(name)
            .select(new NotEmptyHandler());
        if (!exists) {
            new JdbcSession(Database.source())
                // @checkstyle LineLength (1 line)
                .sql("INSERT INTO identity (name, photo, date) VALUES (?, ?, ?)")
                .set(name)
                .set("http://img.netbout.com/unknown.png")
                .set(new Utc(DateUtils.round(new Date(), Calendar.SECOND)))
                .insert(new VoidHandler());
        }
    }

    /**
     * Identities were joined.
     * @param main The name of main identity
     * @param child The name of child identity
     * @throws SQLException If fails
     */
    @Operation("identities-joined")
    public void identitiesJoined(final URN main, final URN child)
        throws SQLException {
        new JdbcSession(Database.source())
            .autocommit(false)
            // @checkstyle LineLength (1 line)
            .sql("INSERT INTO alias (name, identity, date) SELECT l.name, ?, ? FROM alias l LEFT JOIN alias r ON l.name = r.name AND r.identity = ? WHERE l.identity = ? AND r.identity IS NULL GROUP BY l.name")
            .set(main)
            .set(new Utc(DateUtils.round(new Date(), Calendar.SECOND)))
            .set(main)
            .set(child)
            .execute()
            .sql("DELETE FROM alias WHERE identity = ?")
            .set(child)
            .execute()
            .sql("UPDATE message SET author = ? WHERE author = ?")
            .set(main)
            .set(child)
            .execute()
            // @checkstyle LineLength (1 line)
            .sql("INSERT INTO participant (bout, identity, confirmed, date) SELECT l.bout, ?, l.confirmed, ? FROM participant l LEFT JOIN participant r ON l.bout = r.bout AND r.identity = ? WHERE l.identity = ? AND r.identity IS NULL GROUP BY l.bout")
            .set(main)
            .set(new Utc(DateUtils.round(new Date(), Calendar.SECOND)))
            .set(main)
            .set(child)
            .execute()
            .sql("DELETE FROM participant WHERE identity = ?")
            .set(child)
            .execute()
            // @checkstyle LineLength (1 line)
            .sql("INSERT INTO seen (message, identity, date) SELECT l.message, ?, ? FROM seen l LEFT JOIN seen r ON l.message = r.message AND r.identity = ? WHERE l.identity = ? AND r.identity IS NULL GROUP BY l.message")
            .set(main)
            .set(new Utc(DateUtils.round(new Date(), Calendar.SECOND)))
            .set(main)
            .set(child)
            .execute()
            .sql("DELETE FROM seen WHERE identity = ?")
            .set(child)
            .execute()
            .sql("DELETE FROM identity WHERE name = ?")
            .set(child)
            .execute()
            .commit();
    }

    /**
     * Changed identity photo.
     * @param name The name of identity
     * @param photo The photo to set
     * @throws SQLException If fails
     */
    @Operation("changed-identity-photo")
    public void changedIdentityPhoto(final URN name, final URL photo)
        throws SQLException {
        new JdbcSession(Database.source())
            .sql("UPDATE identity SET photo = ? WHERE name = ?")
            .set(photo)
            .set(name)
            .execute();
    }

    /**
     * Find silent identities.
     * @return List of their names
     * @throws SQLException If fails
     */
    @Operation("find-silent-identities")
    public List<URN> findSilentIdentities() throws SQLException {
        final Calendar cal = new GregorianCalendar();
        cal.add(Calendar.HOUR, -1);
        return new JdbcSession(Database.source()).sql(
            // @checkstyle StringLiteralsConcatenation (4 lines)
            "SELECT author, MAX(message.date) AS recent FROM message"
            + " LEFT JOIN seen  ON seen.message = message.number"
            + " WHERE seen.message IS NULL"
            + " GROUP BY author HAVING recent < ?"
        )
            .set(new Utc(cal.getTime()))
            .select(new NamesHandler());
    }

    /**
     * Get marker of silence of this identity.
     * @param name The name of identity
     * @return The marker
     * @throws SQLException If fails
     */
    @Operation("get-silence-marker")
    public String getSilenceMarker(final URN name) throws SQLException {
        final Date recent = new JdbcSession(Database.source()).sql(
            // @checkstyle StringLiteralsConcatenation (4 lines)
            "SELECT message.date FROM message"
            + " LEFT JOIN seen ON seen.message = message.number"
            + " WHERE author = ? AND seen.message IS NULL"
            + " ORDER BY message.date DESC"
        )
            .set(name)
            .select(new SingleHandler<Utc>(Utc.class))
            .getDate();
        final Long total = new JdbcSession(Database.source()).sql(
            // @checkstyle StringLiteralsConcatenation (5 lines)
            "SELECT COUNT(*) FROM message"
            + " LEFT JOIN seen ON seen.message = message.number "
            + " JOIN participant p ON p.bout = message.bout"
            + " WHERE p.identity = ? AND message.date > ?"
            + " AND seen.message IS NULL"
        )
            .set(name)
            .set(new Utc(recent))
            .select(new SingleHandler<Long>(Long.class));
        String marker;
        if (total == 1) {
            marker = "1 message";
        } else if (total > 0) {
            marker = String.format("%d messages", total);
        } else {
            marker = "";
        }
        return marker;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    static final class NamesHandler implements JdbcSession.Handler<List<URN>> {
        @Override
        public List<URN> handle(final ResultSet rset)
            throws SQLException {
            List<URN> names = null;
            while (rset.next()) {
                if (names == null) {
                    names = new LinkedList<URN>();
                }
                names.add(URN.create(rset.getString(1)));
            }
            return names;
        }
    }

}