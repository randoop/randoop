/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/java/org/apache/struts/webapp/example/Constants.java,v 1.1 2004/03/25 12:42:03 husted Exp $
 * $Revision: 1.1 $
 * $Date: 2004/03/25 12:42:03 $
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
 * Manifest constants for the example application.
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/25 12:42:03 $
 */

public final class Constants {


    /**
     * The package name for this application.
     */
    public static final String PACKAGE = "org.apache.struts.webapp.example";


    /**
     * The application scope attribute under which our user database
     * is stored.
     */
    public static final String DATABASE_KEY = "database";


    /**
     * The session scope attribute under which the Subscription object
     * currently selected by our logged-in User is stored.
     */
    public static final String SUBSCRIPTION_KEY = "subscription";


    /**
     * The session scope attribute under which the User object
     * for the currently logged in user is stored.
     */
    public static final String USER_KEY = "user";


    /**
     * A static message in case database resource is not loaded.
     */
    public static final String ERROR_DATABASE_NOT_LOADED =
            "ERROR:  User database not loaded -- check servlet container logs for error messages.";


    /**
     * A static message in case message resource is not loaded.
     */
    public static final String ERROR_MESSAGES_NOT_LOADED =
            "ERROR:  Message resources not loaded -- check servlet container logs for error messages.";

}