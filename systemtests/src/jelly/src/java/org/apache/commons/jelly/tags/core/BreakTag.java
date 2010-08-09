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

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.BreakException;
import org.apache.commons.jelly.expression.Expression;

/**
 * A tag which terminates the execution of the current &lt;forEach&gt; or &lg;while&gt;
 * loop. This tag can take an optional boolean test attribute which if its true
 * then the break occurs otherwise the loop continues processing.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.9 $
 */
public class BreakTag extends TagSupport {

    /** The expression to evaluate. */
    private Expression test;

    /**
     * If specified, the given variable will hold a true/false value
     * indicating if the loop was broken.
     */
    private String var;

    public BreakTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws BreakException {
        boolean broken = false;
        if (test == null || test.evaluateAsBoolean(context)) {
            broken = true;
        }
        if ( var != null ) {
            context.setVariable( this.var, String.valueOf(broken));
        }
        if ( broken ) {
            throw new BreakException();
        }
    }

    /**
     * Sets the Jelly expression to evaluate (optional).
     * If this is <code>null</code> or evaluates to
     * <code>true</code> then the loop is terminated
     *
     * @param test the Jelly expression to evaluate
     */
    public void setTest(Expression test) {
        this.test = test;
    }

    /**
     * Sets the variable name to export indicating if the item was broken
     * @param var name of the variable to be exported
     */
    public void setVar(String var) {
        this.var = var;
    }

}
