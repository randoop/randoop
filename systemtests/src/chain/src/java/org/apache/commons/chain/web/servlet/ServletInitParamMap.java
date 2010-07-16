/*
 * Copyright 1999-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.chain.web.servlet;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.apache.commons.chain.web.MapEntry;


/**
 * <p>Private implementation of <code>Map</code> for servlet context
 * init parameters.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2004/09/10 22:43:20 $
 */

final class ServletInitParamMap implements Map {


    public ServletInitParamMap(ServletContext context) {
        this.context = context;
    }


    private ServletContext context = null;


    public void clear() {
        throw new UnsupportedOperationException();
    }


    public boolean containsKey(Object key) {
        return (context.getInitParameter(key(key)) != null);
    }


    public boolean containsValue(Object value) {
        Iterator values = values().iterator();
        while (values.hasNext()) {
            if (value.equals(values.next())) {
                return (true);
            }
        }
        return (false);
    }


    public Set entrySet() {
        Set set = new HashSet();
        Enumeration keys = context.getInitParameterNames();
        String key;
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            set.add(new MapEntry(key, context.getInitParameter(key), false));
        }
        return (set);
    }


    public boolean equals(Object o) {
        return (context.equals(o));
    }


    public Object get(Object key) {
        return (context.getInitParameter(key(key)));
    }


    public int hashCode() {
        return (context.hashCode());
    }


    public boolean isEmpty() {
        return (size() < 1);
    }


    public Set keySet() {
        Set set = new HashSet();
        Enumeration keys = context.getInitParameterNames();
        while (keys.hasMoreElements()) {
            set.add(keys.nextElement());
        }
        return (set);
    }


    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }


    public void putAll(Map map) {
        throw new UnsupportedOperationException();
    }


    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }


    public int size() {
        int n = 0;
        Enumeration keys = context.getInitParameterNames();
        while (keys.hasMoreElements()) {
            keys.nextElement();
            n++;
        }
        return (n);
    }


    public Collection values() {
        List list = new ArrayList();
        Enumeration keys = context.getInitParameterNames();
        while (keys.hasMoreElements()) {
            list.add(context.getInitParameter((String) keys.nextElement()));
        }
        return (list);
    }


    private String key(Object key) {
        if (key == null) {
            throw new IllegalArgumentException();
        } else if (key instanceof String) {
            return ((String) key);
        } else {
            return (key.toString());
        }
    }


}
