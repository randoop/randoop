/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/agility/src/java/org/apache/commons/agility/Response.java,v 1.1 2004/06/01 00:55:28 husted Exp $
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
 * Outcome of a Request.
 */
public interface Response {

    /**
     * Logical name of corresponding Request.
     * @return Logical name of corresponding Request
     */
    String getName();
}
