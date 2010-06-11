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

package org.apache.commons.jelly.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Project;

/** <p><code>AntJellyContext</code> represents the Jelly context from inside Ant.</p>
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.10 $
  */

public class AntJellyContext extends JellyContext {

    /** The Ant project which contains the variables */
    private Project project;

    /** The Log to which logging calls will be made. */
    private Log log = LogFactory.getLog(AntJellyContext.class);

    public AntJellyContext(Project project, JellyContext parentJellyContext) {
        super( parentJellyContext );
        this.project = project;
    }

    /** @return the value of the given variable name */
    public Object getVariable(String name) {
        // look in parent first
        Object answer = super.getVariable(name);
        if (answer == null) {
            answer = project.getProperty(name);
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "Looking up variable: " + name + " answer: " + answer );
        }

        return answer;
    }

    /** Sets the value of the given variable name */
    public void setVariable(String name, Object value) {
        if ( log.isDebugEnabled() ) {
            log.debug( "Setting variable: " + name + " to: " + value );
        }

        super.setVariable( name, value );

        // only export string values back to Ant?
        if ( value instanceof String ) {
            project.setProperty(name, (String) value);
        }
    }

    /** Removes the given variable */
    public void removeVariable(String name) {
        super.removeVariable( name );
        project.setProperty(name, null);
    }

    /**
     * @return an Iterator over the current variable names in this
     * context
     */
    public Iterator getVariableNames() {
        return getVariables().keySet().iterator();
    }

    /**
     * @return the Map of variables in this scope
     */
    public Map getVariables() {
        // we should add all the Project's properties
        Map map = new HashMap( project.getProperties() );

        // override any local properties
        map.putAll( super.getVariables() );
        return map;
    }

    /**
     * Sets the Map of variables to use
     */

    public void setVariables(Map variables) {
        super.setVariables(variables);

        // export any Ant properties
        for ( Iterator iter = variables.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if ( value instanceof String ) {
                project.setProperty(key, (String)value);
            }
        }
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new child of this context
     */
    protected JellyContext createChildContext() {
        return new AntJellyContext(project, this);
    }

}
