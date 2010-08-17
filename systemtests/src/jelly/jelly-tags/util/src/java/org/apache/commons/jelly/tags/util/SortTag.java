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

package org.apache.commons.jelly.tags.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

public class SortTag extends TagSupport {

    /** things to sort */
    private List items;
    
    /** the variable to store the result in */
    private String var;
    
    /** property of the beans to sort on, if any */
    private String property;
    
    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws JellyTagException {
        if (var == null)
        {
            throw new MissingAttributeException("var");
        }
        
        if (items == null) {
            throw new MissingAttributeException("items");
        }
        
        List sorted = new ArrayList(items);
        if (property == null) {
            Collections.sort(sorted);
        } else {
            BeanComparator comparator = new BeanComparator(property);
            Collections.sort(sorted, comparator);
        }
        context.setVariable(var, sorted);
    }
    
    /**
     * Set the items to be sorted
     * @param newItems some collection
     */
    public void setItems(List newItems) {
        items = newItems;
    }
    
    /**
     * The variable to hold the sorted collection.
     * @param newVar the name of the variable.
     */
    public void setVar(String newVar) {
        var = newVar;
    }
    
    public void setProperty(String newProperty)
    {
        property = newProperty;
    }
}