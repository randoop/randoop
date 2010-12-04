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
package org.apache.commons.jelly.tags.beanshell;

import bsh.EvalError;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * A tag which invokes a BeanShell script..
 *
 * @author Jason Horman
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.6 $
 */
public class ScriptTag extends TagSupport {

    public ScriptTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            JellyInterpreter interpreter = BeanShellExpressionFactory.getInterpreter(context);

            // @todo it'd be really nice to create a JellyNameSpace to pass into
            // this method so that any variables declared by beanshell could be exported
            // into the JellyContext
            String text = getBodyText(false);
            interpreter.eval(text);
        } catch (EvalError e) {
            throw new JellyTagException(e);
        }
    }
}
