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
package org.apache.commons.jelly.tags.core;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.xml.sax.helpers.DefaultHandler;


/** A tag which executes its body but passing no output.
 *  <p>
 * Using this tag will still take the time to perform toString on each object
 * returned to the output (but this toString value is discarded.
 * A future version should go more internally so that this is avoided.
 *
 * @author <a href="mailto:paul@activemath.org">Paul Libbrecht</a>
 * @version $Revision: 1.5 $
  */
public class MuteTag extends TagSupport {

    /**
     * Create an instance
     */
    public MuteTag() {
    }

    /**
     * Invoke the body and produce no output.
     * @param output tag output, which is ignored.
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        super.invokeBody(new MuteXMLOutput());
    }

    /**
     * An XMLOutput which really outputs nothing, in particular, avoids calling
     * toString() in objects returned...
     */
    static class MuteXMLOutput extends XMLOutput {
        public MuteXMLOutput() {
            super(new DefaultHandler());
        }
       
        /**
         * Do nothing, not even invoke the toString!
         */
        public void objectData(Object o) {
        }
    } // class MuteXMLOutput
} // class TagSupport