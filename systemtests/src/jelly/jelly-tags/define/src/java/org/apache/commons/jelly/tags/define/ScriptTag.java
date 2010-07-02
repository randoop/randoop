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
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * &lt;script&gt; tag is used to assign a Script object
 * to a variable. The script can then be called whenever the user wishes
 * maybe from inside an expression or more typically via the &lt;invoke&gt; tag.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class ScriptTag extends TagSupport {

    private String var;

    public ScriptTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        if ( var == null ) {
            throw new JellyTagException( "<define:script> must have a var attribute" );
        }
        context.setVariable( var, getBody() );
    }

    // Properties
    //-------------------------------------------------------------------------

    /** @return the variable name of the script to create */
    public String getVar() {
        return var;
    }

    /** Sets the variable name of the tag to create */
    public void setVar(String var) {
        this.var = var;
    }
}
