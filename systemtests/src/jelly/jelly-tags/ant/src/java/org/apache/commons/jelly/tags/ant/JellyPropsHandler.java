package org.apache.commons.jelly.tags.ant;

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

import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.grant.DefaultPropsHandler;
import org.apache.commons.jelly.JellyContext;

/** Implementation of a Commons Grant <code>propsHandler</code>
 *  to resolve through Jelly's context.
 *
 *  @author <a href="mailto:bob@eng.werken.com">Bob McWhirter</a>
 *  @author <a href="mailto:stephenh@chase3000.com">Stephen Haberman</a>
 */
public class JellyPropsHandler extends DefaultPropsHandler {

    /** The JellyContext. */
    private JellyContext context;

    /** Simple constructor with the context to be used.
     *
     *  @param context The context to be used.
     */
    public JellyPropsHandler(JellyContext context) {
        this.context = context;
    }

    /** Set an ant property.
     *
     *  @param name The property name.
     *  @param value The property value.
     */
    public void setProperty(String name, String value) {
        this.context.setVariable(name, value);
    }

    /** Retrieve an ant property.
     *
     *  @param name The property name.
     *
     *  @return The property value.
     */
    public String getProperty(String name) {
        if (name == null) {
            return null;
        }
        Object value = this.context.getVariable(name);
        if (value == null) {
            return null;
        }
        else {
            return value.toString();
        }
    }

    /** Retrieve all ant properties.
     *
     *  @return A <code>Hashtable</code> of all properties.
     */
    public Hashtable getProperties() {
        Hashtable h = new Hashtable();
        for (Iterator i = this.context.getVariableNames(); i.hasNext(); ) {
            String name = (String) i.next();
            Object value = this.context.getVariable(name);
            if (name != null && value != null && value.toString() != null) {
                h.put(name, value.toString());
            }
        }
        return h;
    }

}
