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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.CollectionTag;

/**
 * A tag which creates a List implementation and optionally
 * adds all of the elements identified by the items attribute.
 * The exact implementation of List can be specified via the
 * class attribute
 * </pre>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.3 $
 */
public class UseListTag extends UseBeanTag implements CollectionTag {

    private Expression items;

    public UseListTag(){
    }

    public List getList() {
        return (List) getBean();
    }


    // CollectionTag interface
    //-------------------------------------------------------------------------
    public void addItem(Object value) {
        getList().add(value);
    }

    // DynaTag interface
    //-------------------------------------------------------------------------
    public Class getAttributeType(String name) throws JellyTagException {
        if (name.equals("items")) {
            return Expression.class;
        }
        return super.getAttributeType(name);
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    protected void setBeanProperties(Object bean, Map attributes) throws JellyTagException {
        items = (Expression) attributes.remove("items");
        super.setBeanProperties(bean, attributes);
    }

    protected void processBean(String var, Object bean) throws JellyTagException {
        super.processBean(var, bean);

        List list = getList();

        // if the items variable is specified lets append all the items
        if (items != null) {
            Iterator iter = items.evaluateAsIterator(context);
            while (iter.hasNext()) {
                list.add( iter.next() );
            }
        }
    }

    protected Class getDefaultClass() {
        return ArrayList.class;
    }
}
