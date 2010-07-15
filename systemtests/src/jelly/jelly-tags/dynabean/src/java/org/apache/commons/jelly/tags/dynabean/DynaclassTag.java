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
package org.apache.commons.jelly.tags.dynabean;

import java.util.ArrayList;

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * A tag which creates and defines and creates a DynaClass
 * The DynaClass object is placed by name in the context,
 * so that a DynaBean tag can use it by name to instantiate
 * a DynaBean object
 *
 * @author Theo Niemeijer
 * @version 1.0
 */
public class DynaclassTag extends TagSupport {

    private ArrayList propList = new ArrayList();
    private DynaProperty[] props = null;
    private DynaClass dynaClass = null;

    private String name;
    private String var;

    public DynaclassTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {

        if (name == null) {
            throw new MissingAttributeException("name");
        }

        if (var == null) {
            var = name;
        }

        // Evaluate the body of the dynaclass definition
        invokeBody(output);

        // Convert the list of properties into array
        props =
            (DynaProperty[]) propList.toArray(
                new DynaProperty[propList.size()]);

        if (props == null) {
            throw new JellyTagException("No properties list");
        }

        if (props.length == 0) {
            throw new JellyTagException("No properties");
        }

        // Create the dynaclass with name and properties
        dynaClass = (DynaClass) new BasicDynaClass(name, null, props);

        // Place new dynaclass in context
        context.setVariable(getVar(), dynaClass);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the name of the new DynaClass
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getVar() {
        return var;
    }

    /**
     * Sets the name of the variable to export the DynaClass instance
     */
    public void setVar(String var) {
        this.var = var;
    }

    protected void addDynaProperty(DynaProperty prop) {
        propList.add(prop);
    }
}
