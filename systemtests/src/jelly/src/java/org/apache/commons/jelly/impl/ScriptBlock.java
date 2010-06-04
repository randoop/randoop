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
package org.apache.commons.jelly.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

/** <p><code>ScriptBlock</code> a block of scripts.</p>
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.16 $
  */
public class ScriptBlock implements Script {

    /** The list of scripts */
    private List list = new ArrayList();

    /**
     * Create a new instance.
     */
    public ScriptBlock() {
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return super.toString() + "[scripts=" + list + "]";
    }

    /** Add a new script to the end of this block */
    public void addScript(Script script) {
        list.add(script);
    }

    /** Removes a script from this block */
    public void removeScript(Script script) {
        list.remove(script);
    }

    /**
     * Gets the child scripts that make up this block. This list is live
     * so that it can be modified if requried
     */
    public List getScriptList() {
        return list;
    }

    // Script interface
    //-------------------------------------------------------------------------
    public Script compile() throws JellyException {
        int size = list.size();
        if (size == 1) {
            Script script = (Script) list.get(0);
            return script.compile();
        }
        // now compile children
        for (int i = 0; i < size; i++) {
            Script script = (Script) list.get(i);
            list.set(i, script.compile());
        }
        return this;
    }

    /** Evaluates the body of a tag */
    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
/*
        for (int i = 0, size = scripts.length; i < size; i++) {
            Script script = scripts[i];
            script.run(context, output);
        }
*/
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Script script = (Script) iter.next();
            script.run(context, output);
        }
    }
    
    /**
     * Trim the body of the script.
     * In this case, trim all elements, removing any that are empty text.
     */
    public void trimWhitespace() {
        List list = getScriptList();
        for ( int i = list.size() - 1; i >= 0; i-- ) {
            Script script = (Script) list.get(i);
            if ( script instanceof TextScript ) {
                TextScript textScript = (TextScript) script;
                String text = textScript.getText();
                text = text.trim();
                if ( text.length() == 0 ) {
                    list.remove(i);
                }
                else {
                    textScript.setText(text);
                }
            }
        }
    }
}
