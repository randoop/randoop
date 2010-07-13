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
package org.apache.commons.jelly.tags.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.CollectionTag;

/**
 * A simple tag which demonstrates how to process beans generically.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version  $Revision: 1.4 $
 */
public class MyContainerTag extends TagSupport implements CollectionTag {

    private List list = new ArrayList();
    private String var;

    public MyContainerTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        invokeBody(output);
        context.setVariable(var, list);
        list = new ArrayList();
    }

    // CollectionTag interface
    //-------------------------------------------------------------------------
    public void addItem(Object value) {
        list.add(value);
    }

    // Properties
    //-------------------------------------------------------------------------
    /**
     * @return String
     */
    public String getVar() {
        return var;
    }

    /**
     * Sets the var.
     * @param var The var to set
     */
    public void setVar(String var) {
        this.var = var;
    }

}
