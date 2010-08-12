/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/agility/src/test/org/apache/commons/agility/ProcessingTest.java,v 1.1 2004/06/01 00:56:48 husted Exp $
 * $Revision: 1.1 $
 * $Date: 2004/06/01 00:56:48 $
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

import junit.framework.TestCase;
import org.apache.commons.agility.impl.ControllerCatalog;
import org.apache.commons.agility.impl.HandlerCommand;
import org.apache.commons.agility.impl.RequestContext;

public class ProcessingTest extends TestCase {

    /**
     * Create a ControllerCatalog, add a HandlerCommand,
     * process a corresponding RequestContext, and confirm
     * that the ResponseContext name matches.
     * @throws Exception on unexpected failure
     */
    public void testRequestResponseName() throws Exception {

        String NAME = "TestProcessing";

        Controller controller = new ControllerCatalog();

        RequestHandler handler = new HandlerCommand(NAME);
        controller.addHandler(handler);
        Request request = new RequestContext(NAME);
        controller.process(request);
        Response response = request.getResponse();

        assertNotNull(response);
        assertEquals(NAME, response.getName());
    }
}
