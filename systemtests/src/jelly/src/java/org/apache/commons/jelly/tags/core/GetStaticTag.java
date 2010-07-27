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

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.util.ClassLoaderUtils;


/**
 * A tag which can retrieve the value of a static field of a given class.
 * The following attributes are required:<br />
 * <ul>
 *   <li>var - The variable to which to assign the resulting value.</li>
 *   <li>field - The name of the static field to retrieve.</li>
 *   <li>className - The name of the class containing the static field.</li>
 * </ul>
 *
 * Example usage:
 * <pre>
 * &lt;j:getStatic var="closeOperation" className="javax.swing.JFrame"
 *              field="EXIT_ON_CLOSE"/&gt;
 * </pre>
 *
 * @version $Revision: 1.3 $
 */

public class GetStaticTag extends TagSupport {

    /** The variable to which to assign the resulting value. */
    private String var;

    /** The name of the static field to retrieve. */
    private String field;

    /** The name of the class containing the static field. */
    private String className;


    /**
     * Sets the name of the variable exported by this tag.
     *
     * @param var The variable name.
     */

    public void setVar(String var) {
        this.var = var;
    }


    /**
     * Sets the name of the field to retrieve.
     *
     * @param method The method name
     */

    public void setField(String field) {
        this.field = field;
    }


    /**
     * Sets the fully qualified name of the class containing the static field.
     *
     * @param className The name of the class.
     */

    public void setClassName(String className) {
        this.className = className;
    }


    // Tag interface
    //------------------------------------------------------------------------

    public void doTag(XMLOutput output) throws JellyTagException {
        String message = null;

        if(var == null)
            message = "var";
        else if(field == null)
            message = "field";
        else if(className == null)
            message = "className";

        if(message != null)
            throw new MissingAttributeException(message);

        try {
            Class type     = ClassLoaderUtils.getClassLoader(getClass()).loadClass(className);
            Object result  = type.getField(field).get(null);
            JellyContext context = getContext();

            context.setVariable(var, result);

        } catch(Throwable t) {
            throw
                new JellyTagException("Could not access " + className + "." +
                                      var + ".  Original exception message: " +
                                      t.getMessage(), t);
        }
    }

}


/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
