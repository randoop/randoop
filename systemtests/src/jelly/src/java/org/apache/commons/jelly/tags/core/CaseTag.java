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
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;

/**
 * A tag which conditionally evaluates its body if
 * my {@link #setValue value} attribute equals my ancestor
 * {@link SwitchTag &lt;switch&gt;} tag's
 * {@link SwitchTag#setOn "on"} attribute.
 *
 * This tag must be contained within the body of some
 * {@link SwitchTag &lt;switch&gt;} tag.
 *
 * @see SwitchTag
 *
 * @author Rodney Waldhoff
 * @version $Revision: 1.8 $ $Date: 2004/09/09 12:27:53 $
 */
public class CaseTag extends TagSupport {

    public CaseTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void setValue(Expression value) {
        this.valueExpression = value;
    }

    public void setFallThru(boolean fallThru) {
        this.fallThru = fallThru;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if(null == this.valueExpression) {
            throw new MissingAttributeException("value");
        }
        SwitchTag tag = (SwitchTag)findAncestorWithClass(SwitchTag.class);
        if(null == tag) {
            throw new JellyTagException("This tag must be enclosed inside a <switch> tag" );
        }
        if(tag.hasDefaultBeenEncountered()) {
            throw new JellyTagException("<default> should be the last tag within a <switch>" );
        }
        Object value = valueExpression.evaluate(context);
        if(tag.isFallingThru() ||
           (null == tag.getValue() && null == value) ||
           (null != tag.getValue() && tag.getValue().equals(value))) {
            tag.caseMatched();
            tag.setFallingThru(fallThru);
            invokeBody(output);
        }
    }

    // Attributes
    //-------------------------------------------------------------------------
    private Expression valueExpression = null;
    private boolean fallThru = false;

}
