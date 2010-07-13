/*

 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/test/org/apache/commons/mailreader/LogonUserTest.java,v 1.3 2004/04/08 23:26:07 husted Exp $
 * $Revision: 1.3 $
 * $Date: 2004/04/08 23:26:07 $
 *
 * Copyright 2000-2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mailreader;

import junit.framework.TestCase;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.mailreader.MailReader;
import org.apache.commons.chain.mailreader.commands.LogonUser;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.struts.webapp.example.User;
import org.apache.struts.webapp.example.UserDatabase;
import org.apache.struts.webapp.example.memory.MemoryUserDatabase;

import java.util.Locale;

/**
 */
public class LogonUserTest extends TestCase {

    private Locale locale = Locale.getDefault();
    private UserDatabase database;
    private Command command;

    private static String USER = "user";
    private static String PASS = "pass";
    private static String FULL_NAME = "John Q. User";

    public void setUp() {

        command = new LogonUser();
        database = new MemoryUserDatabase();
        User user = database.createUser(USER);
        user.setPassword(PASS);
        user.setFullName(FULL_NAME);

    }

    public void testJohnQ() {

        Context input = new ContextBase();
        input.put(MailReader.PN_USERNAME, USER);
        input.put(MailReader.PN_PASSWORD, PASS);

        MailReader context = new MailReader(locale, input, database);

        try {
            command.execute(context);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        User user = context.getUser();
        assertNotNull("Where's waldo?", user);
        assertEquals("Who am I?", "John Q. User", user.getFullName());

    }

    public void testInvalidPassword() {

        Context input = new ContextBase();
        input.put(MailReader.PN_USERNAME, USER);
        input.put(MailReader.PN_PASSWORD, "SezMe");

        MailReader context = new MailReader(locale, input, database);

        try {
            command.execute(context);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        User user = context.getUser();
        assertNull("Who's watching the store", user);

    }

    public void testInvalidUsername() {

        Context input = new ContextBase();
        input.put(MailReader.PN_USERNAME, "zaphod");
        input.put(MailReader.PN_PASSWORD, PASS);

        MailReader context = new MailReader(locale, input, database);

        try {
            command.execute(context);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        User user = context.getUser();
        assertNull("Who's watching the store", user);

    }

}
