/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/java/org/apache/struts/webapp/example/memory/MemoryUserDatabase.java,v 1.1 2004/03/25 12:41:51 husted Exp $
 * $Revision: 1.1 $
 * $Date: 2004/03/25 12:41:51 $
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
package org.apache.struts.webapp.example.memory;


import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.webapp.example.ExpiredPasswordException;
import org.apache.struts.webapp.example.Subscription;
import org.apache.struts.webapp.example.User;
import org.apache.struts.webapp.example.UserDatabase;
import org.xml.sax.Attributes;

import java.io.*;
import java.util.HashMap;


/**
 * <p>Concrete implementation of {@link UserDatabase} for an in-memory
 * database backed by an XML data file.</p>
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/25 12:41:51 $
 * @since Struts 1.1
 */

public class MemoryUserDatabase implements UserDatabase {


    // ----------------------------------------------------------- Constructors


    // ----------------------------------------------------- Instance Variables


    /**
     * Logging output for this user database instance.
     */
    private Log log = LogFactory.getLog(this.getClass());


    /**
     * The {@link User}s associated with this UserDatabase, keyed by username.
     */
    private HashMap users = new HashMap();


    // ------------------------------------------------------------- Properties


    /**
     * Absolute pathname to the persistent file we use for loading and storing
     * persistent data.
     */
    private String pathname = null;

    private String pathnameOld = null;

    private String pathnameNew = null;

    public String getPathname() {
        return (this.pathname);
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
        pathnameOld = pathname + ".old";
        pathnameNew = pathname + ".new";
    }


    // --------------------------------------------------------- Public Methods


    // See interface for Javadoc
    public void close() throws Exception {

        save();

    }


    // See interface for Javadoc
    public User createUser(String username) {

        synchronized (users) {
            if (users.get(username) != null) {
                throw new IllegalArgumentException("Duplicate user '" +
                        username + "'");
            }
            if (log.isTraceEnabled()) {
                log.trace("Creating user '" + username + "'");
            }
            MemoryUser user = new MemoryUser(this, username);
            synchronized (users) {
                users.put(username, user);
            }
            return (user);
        }

    }


    // See interface for Javadoc
    public User findUser(String username) throws ExpiredPasswordException {

        synchronized (users) {
            return ((User) users.get(username));
        }

    }


    // See interface for Javadoc
    public User[] findUsers() {

        synchronized (users) {
            User results[] = new User[users.size()];
            return ((User[]) users.values().toArray(results));
        }

    }


    // See interface for Javadoc
    public void open() throws Exception {

        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try {

            // Acquire an input stream to our database file
            if (log.isDebugEnabled()) {
                log.debug("Loading database from '" + pathname + "'");
            }
            fis = new FileInputStream(pathname);
            bis = new BufferedInputStream(fis);

            // Construct a digester to use for parsing
            Digester digester = new Digester();
            digester.push(this);
            digester.setValidating(false);
            digester.addFactoryCreate
                    ("database/user",
                            new MemoryUserCreationFactory(this));
            digester.addFactoryCreate
                    ("database/user/subscription",
                            new MemorySubscriptionCreationFactory());

            // Parse the input stream to initialize our database
            digester.parse(bis);
            bis.close();
            bis = null;
            fis = null;

        } catch (Exception e) {

            log.error("Loading database from '" + pathname + "':", e);
            throw e;

        } finally {

            if (bis != null) {
                try {
                    bis.close();
                } catch (Throwable t) {
                    ;
                }
                bis = null;
                fis = null;
            }

        }

    }


    // See interface for Javadoc
    public void removeUser(User user) {

        if (!(this == user.getDatabase())) {
            throw new IllegalArgumentException
                    ("User not associated with this database");
        }
        if (log.isTraceEnabled()) {
            log.trace("Removing user '" + user.getUsername() + "'");
        }
        synchronized (users) {
            users.remove(user.getUsername());
        }

    }


    // See interface for Javadoc
    public void save() throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Saving database to '" + pathname + "'");
        }
        File fileNew = new File(pathnameNew);
        PrintWriter writer = null;

        try {

            // Configure our PrintWriter
            FileOutputStream fos = new FileOutputStream(fileNew);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            writer = new PrintWriter(osw);

            // Print the file prolog
            writer.println("<?xml version='1.0'?>");
            writer.println("<database>");

            // Print entries for each defined user and associated subscriptions
            User users[] = findUsers();
            for (int i = 0; i < users.length; i++) {
                writer.print("  ");
                writer.println(users[i]);
                Subscription subscriptions[] =
                        users[i].getSubscriptions();
                for (int j = 0; j < subscriptions.length; j++) {
                    writer.print("    ");
                    writer.println(subscriptions[j]);
                    writer.print("    ");
                    writer.println("</subscription>");
                }
                writer.print("  ");
                writer.println("</user>");
            }

            // Print the file epilog
            writer.println("</database>");

            // Check for errors that occurred while printing
            if (writer.checkError()) {
                writer.close();
                fileNew.delete();
                throw new IOException
                        ("Saving database to '" + pathname + "'");
            }
            writer.close();
            writer = null;

        } catch (IOException e) {

            if (writer != null) {
                writer.close();
            }
            fileNew.delete();
            throw e;

        }


        // Perform the required renames to permanently save this file
        File fileOrig = new File(pathname);
        File fileOld = new File(pathnameOld);
        if (fileOrig.exists()) {
            fileOld.delete();
            if (!fileOrig.renameTo(fileOld)) {
                throw new IOException
                        ("Renaming '" + pathname + "' to '" + pathnameOld + "'");
            }
        }
        if (!fileNew.renameTo(fileOrig)) {
            if (fileOld.exists()) {
                fileOld.renameTo(fileOrig);
            }
            throw new IOException
                    ("Renaming '" + pathnameNew + "' to '" + pathname + "'");
        }
        fileOld.delete();

    }


}


/**
 * Digester object creation factory for subscription instances.
 */
class MemorySubscriptionCreationFactory implements ObjectCreationFactory {

    private Digester digester = null;

    public Digester getDigester() {
        return (this.digester);
    }

    public void setDigester(Digester digester) {
        this.digester = digester;
    }

    public Object createObject(Attributes attributes) {
        String host = attributes.getValue("host");
        User user = (User) digester.peek();
        Subscription subscription = user.createSubscription(host);
        String autoConnect = attributes.getValue("autoConnect");
        if (autoConnect == null) {
            autoConnect = "false";
        }
        if ("true".equalsIgnoreCase(autoConnect) ||
                "yes".equalsIgnoreCase(autoConnect)) {
            subscription.setAutoConnect(true);
        } else {
            subscription.setAutoConnect(false);
        }
        subscription.setPassword(attributes.getValue("password"));
        subscription.setType(attributes.getValue("type"));
        subscription.setUsername(attributes.getValue("username"));
        return (subscription);
    }

}


/**
 * Digester object creation factory for user instances.
 */
class MemoryUserCreationFactory implements ObjectCreationFactory {

    public MemoryUserCreationFactory(MemoryUserDatabase database) {
        this.database = database;
    }

    private MemoryUserDatabase database = null;

    private Digester digester = null;

    public Digester getDigester() {
        return (this.digester);
    }

    public void setDigester(Digester digester) {
        this.digester = digester;
    }

    public Object createObject(Attributes attributes) {
        String username = attributes.getValue("username");
        User user = database.createUser(username);
        user.setFromAddress(attributes.getValue("fromAddress"));
        user.setFullName(attributes.getValue("fullName"));
        user.setPassword(attributes.getValue("password"));
        user.setReplyToAddress(attributes.getValue("replyToAddress"));
        return (user);
    }

}
