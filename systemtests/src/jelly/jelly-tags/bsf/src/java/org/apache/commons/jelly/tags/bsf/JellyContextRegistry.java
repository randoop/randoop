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
package org.apache.commons.jelly.tags.bsf;

import org.apache.bsf.util.ObjectRegistry;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** A BSF ObjectRegistry which uses the Context to find and
  * register objects
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.5 $
  */
public class JellyContextRegistry extends ObjectRegistry {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(JellyContextRegistry.class);

    /** The context */
    private JellyContext context;

    public JellyContextRegistry() {
    }

    public JellyContext getJellyContext() {
        return context;
    }

    public void setJellyContext(JellyContext context) {
        this.context = context;
    }

    // ObjectRegistry interface
    //-------------------------------------------------------------------------
    public Object lookup(String name) {
        return context.getVariable(name);
    }

    public void register(String name, Object value) {
        context.setVariable(name, value);
    }

    public void unregister(String name) {
        context.removeVariable(name);
    }
}
