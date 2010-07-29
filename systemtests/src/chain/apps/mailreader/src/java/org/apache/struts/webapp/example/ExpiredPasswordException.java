/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/java/org/apache/struts/webapp/example/ExpiredPasswordException.java,v 1.1 2004/03/25 12:42:03 husted Exp $
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

import org.apache.struts.util.ModuleException;

/**
 * Example of an application-specific exception for which a handler
 * can be configured.
 */


public class ExpiredPasswordException extends ModuleException {


    /**
     * Construct a new instance of this exception for the specified username.
     *
     * @param username Username whose password has expired
     */
    public ExpiredPasswordException(String username) {
        super("error.password.expired", username);
    }


}

