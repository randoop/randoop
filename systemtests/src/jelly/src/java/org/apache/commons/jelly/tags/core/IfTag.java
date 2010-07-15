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

/** A tag which conditionally evaluates its body based on some condition
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.14 $
  */
public class IfTag extends TagSupport {

    /** The expression to evaluate. */
    private Expression test;

    public IfTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if (test != null) {
            if (test.evaluateAsBoolean(context)) {
                invokeBody(output);
            }
        }
        else {
            throw new MissingAttributeException( "test" );
        }

    }

    // Properties
    //-------------------------------------------------------------------------
    /** Sets the Jelly expression to evaluate. If this returns true, the body of
     * the tag is evaluated
     *
     * @param test the Jelly expression to evaluate
     */
    public void setTest(Expression test) {
        this.test = test;
    }
}
