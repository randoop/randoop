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
package org.apache.commons.jelly.tags.fmt;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.expression.Expression;

/**
 * Support for tag handlers for &lt;param&gt;, the parameter setting
 * tag in JSTL.
 * @author <a href="mailto:willievu@yahoo.com">Willie Vu</a>
 * @version 1.1
 *
 * @task handle body content trimming
 */
public class ParamTag extends TagSupport {

    /** Holds value of property value. */
    private Expression value;

    /** Creates a new instance of ParamTag */
    public ParamTag() {
    }

    /**
     * Evaluates this tag after all the tags properties have been initialized.
     *
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        MessageTag parent = null;
        Tag t = findAncestorWithClass(this, MessageTag.class);
        if (t != null) {
            parent = (MessageTag) t;
        } else {
            // must be nested inside a <fmt:message> action.
            throw new JellyTagException("must be nested inside a <fmt:message> action.");
        }

        Object valueInput = null;
        if (this.value != null) {
            valueInput = this.value.evaluate(context);
        }
        else {
            // get value from body
            valueInput = getBodyText();
        }

        parent.addParam(valueInput);
    }

    /** Setter for property value.
     * @param value New value of property value.
     *
     */
    public void setValue(Expression value) {
        this.value = value;
    }

}
