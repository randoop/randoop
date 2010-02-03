/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/agility/src/java/org/apache/commons/agility/Controller.java,v 1.1 2004/06/01 00:55:28 husted Exp $
 * $Revision: 1.1 $
 * $Date: 2004/06/01 00:55:28 $
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
package org.apache.commons.agility;

/**
 * A controller "interacts with a client, controlling and managing the handling
 * of each request" [J2EE Design Patterns].
 */
public interface Controller {

    /**
     * Add a handler for a request.
     * @param handler RequestHandler to add
     */
    void addHandler(RequestHandler handler);

    /**
     * Return the RequestHandler matching name.
     * @param name RequestHandler name to match
     * @return the RequestHandler matching name
     * @throws ProcessException if name cannot be matched
     */
    RequestHandler getHandler(String name) throws ProcessException;

    /**
     * Main entry method that is called for each request
     * @param request The Request to process
     * @throws ProcessException On any error
     */
    void process(Request request) throws ProcessException;
}
