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
 * Executes the child &lt;case&gt; tag whose value equals my on attribute.
 * Executes a child &lt;default&gt; tag when present and no &lt;case&gt; tag has
 * yet matched.
 *
 * @see CaseTag
 * @see DefaultTag
 *
 * @author Rodney Waldhoff
 * @version $Revision: 1.7 $ $Date: 2004/09/09 12:27:53 $
 */
public class SwitchTag extends TagSupport {

    public SwitchTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------

    /**
     * Sets the value to switch on.
     * Note that the {@link Expression} is evaluated only once, when the
     * &lt;switch&gt; tag is evaluated.
     * @param on the value to switch on
     */
    public void setOn(Expression on) {
        this.on = on;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if(null == on) {
            throw new MissingAttributeException("on");
        } else {
            value = on.evaluate(context);
            invokeBody(output);
        }
    }

    // Protected properties
    //-------------------------------------------------------------------------
    protected boolean hasSomeCaseMatched() {
        return this.someCaseMatched;
    }

    protected void caseMatched() {
        this.someCaseMatched = true;
    }

    protected boolean isFallingThru() {
        return this.fallingThru;
    }

    protected void setFallingThru(boolean fallingThru) {
        this.fallingThru = fallingThru;
    }

    protected Object getValue() {
        return value;
    }

    protected boolean hasDefaultBeenEncountered() {
        return defaultEncountered;
    }

    protected void defaultEncountered() {
        this.defaultEncountered = true;
    }

    // Attributes
    //-------------------------------------------------------------------------
    private boolean someCaseMatched = false;
    private boolean fallingThru = false;
    private boolean defaultEncountered = false;
    private Expression on = null;
    private Object value = null;

}
