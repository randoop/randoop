/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/java/org/apache/struts/webapp/example/User.java,v 1.1 2004/03/25 12:42:04 husted Exp $
 * $Revision: 1.1 $
 * $Date: 2004/03/25 12:42:04 $
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


package org.apache.struts.webapp.example;


/**
 * <p>A <strong>User</strong> which is stored, along with his or her
 * associated {@link Subscription}s, in a {@link UserDatabase}.</p>
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/25 12:42:04 $
 * @since Struts 1.1
 */

public interface User {


    // ------------------------------------------------------------- Properties


    /**
     * Return the {@link UserDatabase} with which we are associated.
     */
    public UserDatabase getDatabase();


    /**
     * Return the from address.
     */
    public String getFromAddress();


    /**
     * Set the from address.
     *
     * @param fromAddress The new from address
     */
    public void setFromAddress(String fromAddress);


    /**
     * Return the full name.
     */
    public String getFullName();


    /**
     * Set the full name.
     *
     * @param fullName The new full name
     */
    public void setFullName(String fullName);


    /**
     * Return the password.
     */
    public String getPassword();


    /**
     * Set the password.
     *
     * @param password The new password
     */
    public void setPassword(String password);


    /**
     * Return the reply-to address.
     */
    public String getReplyToAddress();


    /**
     * Set the reply-to address.
     *
     * @param replyToAddress The new reply-to address
     */
    public void setReplyToAddress(String replyToAddress);


    /**
     * Find and return all {@link Subscription}s associated with this user.
     * If there are none, a zero-length array is returned.
     */
    public Subscription[] getSubscriptions();


    /**
     * Return the username.
     */
    public String getUsername();


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
    public Subscription createSubscription(String host);


    /**
     * Find and return the {@link Subscription} associated with the specified
     * host.  If none is found, return <code>null</code>.
     *
     * @param host Host name to look up
     */
    public Subscription findSubscription(String host);


    /**
     * Remove the specified {@link Subscription} from being associated
     * with this User.
     *
     * @param subscription Subscription to be removed
     *
     * @exception IllegalArgumentException if the specified subscription is not
     *  associated with this User
     */
    public void removeSubscription(Subscription subscription);


}
