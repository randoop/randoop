/*
 * Copyright 2002,2004 The Apache Software Foundation.
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

package org.apache.commons.jelly.tags.xmlunit;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

public class ActualTag extends XMLUnitTagSupport {

    public void doTag(XMLOutput output) throws JellyTagException {
        Document actualDocument = parseBody();

        AssertDocumentsEqualTag assertTag =
            (AssertDocumentsEqualTag) findAncestorWithClass(AssertDocumentsEqualTag
                .class);
        assertTag.setActual(actualDocument);
    }

    protected SAXReader createSAXReader() {
        return new SAXReader();
    }
}
