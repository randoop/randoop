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
import org.apache.commons.jelly.impl.BreakException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A tag which performs an iteration while the result of an expression is true.
 *
 * @author <a href="mailto:eric@ericalexander.net">Eric Alexander</a>
 * @author dIon Gillard
 * @version $Revision: 1.15 $
 */
public class WhileTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(WhileTag.class);

    /** The expression to use to determine if the while should continue */
    private Expression test;

    /**
     * Create a new while tag
     */
    public WhileTag() {
    }

    /**
     * Tag interface
     * @param output destination for xml output
     * @throws MissingAttributeException when the test attribute is missing
     * @throws Exception for anything else
     */
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if (test != null) {
            try {
                while (test.evaluateAsBoolean(getContext())) {
                    if (log.isDebugEnabled()) {
                        log.debug("evaluated to true! gonna keep on chuggin!");
                    }
                    invokeBody(output);
                }
            }
            catch (BreakException e) {
                if (log.isDebugEnabled()) {
                    log.debug("loop terminated by break: " + e, e);
                }
            }
        }
        else {
            throw new MissingAttributeException("test");
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Setter for the expression
     * @param e the expression to test
     */
    public void setTest(Expression e) {
        this.test = e;
    }
}

