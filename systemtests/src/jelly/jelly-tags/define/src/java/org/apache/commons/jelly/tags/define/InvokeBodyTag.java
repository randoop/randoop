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
package org.apache.commons.jelly.tags.define;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.DynamicTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * &lt;invokeBody&gt; tag is used inside a &lt;tag&gt; tag
 * (i.e. the definition of a dynamic tag) to invoke the tags body when
 * the tag is invoked.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class InvokeBodyTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(InvokeBodyTag.class);

    public InvokeBodyTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        // Try find find the body from the reserved 'org.apache.commons.jelly.body' variable
        Script script = (Script) context.getVariable("org.apache.commons.jelly.body");
        if (script != null) {
            script.run(context, output);
        }
        else {
            // note this mechanism does not work properly for arbitrarily
            // nested dynamic tags. A better way is required.
            Tag tag = findAncestorWithClass(this, DynamicTag.class);
            if (tag == null) {
                throw new JellyTagException("Cannot invoke body, no dynamic tag is defined in this block");
            }
            else {
                tag.invokeBody(output);
            }
        }
    }
}
