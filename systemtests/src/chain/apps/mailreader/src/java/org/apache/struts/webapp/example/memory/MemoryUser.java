/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/java/org/apache/struts/webapp/example/memory/MemoryUser.java,v 1.1 2004/03/25 12:41:51 husted Exp $
 * $Revision: 1.1 $
 * $Date: 2004/03/25 12:41:51 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.struts.webapp.example.memory;

import org.apache.struts.webapp.example.Subscription;
import org.apache.struts.webapp.example.User;
import org.apache.struts.webapp.example.UserDatabase;

import java.util.HashMap;


/**
 * <p>Concrete implementation of {@link User} for an in-memory
 * database backed by an XML data file.</p>
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/25 12:41:51 $
 * @since Struts 1.1
 */

public final class MemoryUser implements User {


    // ----------------------------------------------------------- Constructors


    /**
     * <p>Construct a new User associated with the specified
     * {@link UserDatabase}.
     *
     * @param database The user database with which we are associated
     * @param username The username of this user
     */
    public MemoryUser(MemoryUserDatabase database, String username) {

        super();
        this.database = database;
        this.username = username;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The {@link UserDatabase} with which we are associated.
     */
    private MemoryUserDatabase database = null;


    /**
     * The {@link Subscription}s for this User, keyed by hostname.
     */
    private HashMap subscriptions = new HashMap();


    /**
     * The username for this user.
     */
    private String username = null;


    // ------------------------------------------------------------- Properties


    /**
     * The {@link UserDatabase} with which we are associated.
     */
    public UserDatabase getDatabase() {
        return (this.database);
    }


    /**
     * The email address from which messages are sent.
     */
    private String fromAddress = null;

    public String getFromAddress() {
        return (this.fromAddress);
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }


    /**
     * The full name of this user, included in from addresses.
     */
    private String fullName = null;

    public String getFullName() {
        return (this.fullName);
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    /**
     * The password (in clear text).
     */
    private String password = null;

    public String getPassword() {
        return (this.password);
    }

    public void setPassword(String password) {
        this.password = password;
    }


    /**
     * The EMAIL address to which replies should be sent.
     */
    private String replyToAddress = null;

    public String getReplyToAddress() {
        return (this.replyToAddress);
    }

    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }


    /**
     * Find and return all {@link Subscription}s associated with this user.
     * If there are none, a zero-length array is returned.
     */
    public Subscription[] getSubscriptions() {

        synchronized (subscriptions) {
            Subscription results[] = new Subscription[subscriptions.size()];
            return ((Subscription[]) subscriptions.values().toArray(results));
        }

    }


    /**
     * The username (must be unique).
     */
    public String getUsername() {
        return (this.username);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a new {@link Subscription} associated with this
     * User, for the specified host name.
     *
     * @param host Host name for which to create a subscription
     *
     * @exception IllegalArgumentException if the host name is not unique
     *  for this user
     */
    public Subscription createSubscription(String host) {

        synchronized (subscriptions) {
            if (subscriptions.get(host) != null) {
                throw new IllegalArgumentException("Duplicate host '" + host
                        + "' for user '" +
                        username + "'");
            }
            MemorySubscription subscription =
                    new MemorySubscription(this, host);
            synchronized (subscriptions) {
                subscriptions.put(host, subscription);
            }
            return (subscription);
        }

    }


    /**
     * Find and return the {@link Subscription} associated with the specified
     * host.  If none is found, return <code>null</code>.
     *
     * @param host Host name to look up
     */
    public Subscription findSubscription(String host) {

        synchronized (subscriptions) {
            return ((Subscription) subscriptions.get(host));
        }

    }


    /**
     * Remove the specified {@link Subscription} from being associated
     * with this User.
     *
     * @param subscription Subscription to be removed
     *
     * @exception IllegalArgumentException if the specified subscription is not
     *  associated with this User
     */
    public void removeSubscription(Subscription subscription) {

        if (!(this == subscription.getUser())) {
            throw new IllegalArgumentException
                    ("Subscription not associated with this user");
        }
        synchronized (subscriptions) {
            subscriptions.remove(subscription.getHost());
        }

    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("<user username=\"");
        sb.append(username);
        sb.append("\"");
        if (fromAddress != null) {
            sb.append(" fromAddress=\"");
            sb.append(fromAddress);
            sb.append("\"");
        }
        if (fullName != null) {
            sb.append(" fullName=\"");
            sb.append(fullName);
            sb.append("\"");
        }
        if (password != null) {
            sb.append(" password=\"");
            sb.append(password);
            sb.append("\"");
        }
        if (replyToAddress != null) {
            sb.append(" replyToAddress=\"");
            sb.append(replyToAddress);
            sb.append("\"");
        }
        sb.append(">");
        return (sb.toString());

    }


}
