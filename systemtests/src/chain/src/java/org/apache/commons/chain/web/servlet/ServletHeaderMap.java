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
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.chain.web.MapEntry;


/**
 * <p>Private implementation of <code>Map</code> for servlet request
 * name-value.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2004/09/10 22:43:20 $
 */

final class ServletHeaderMap implements Map {


    public ServletHeaderMap(HttpServletRequest request) {
        this.request = request;
    }


    private HttpServletRequest request = null;


    public void clear() {
        throw new UnsupportedOperationException();
    }


    public boolean containsKey(Object key) {
        return (request.getHeader(key(key)) != null);
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
        Enumeration keys = request.getHeaderNames();
        String key;
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            set.add(new MapEntry(key, request.getHeader(key), false));
        }
        return (set);
    }


    public boolean equals(Object o) {
        return (request.equals(o));
    }


    public Object get(Object key) {
        return (request.getHeader(key(key)));
    }


    public int hashCode() {
        return (request.hashCode());
    }


    public boolean isEmpty() {
        return (size() < 1);
    }


    public Set keySet() {
        Set set = new HashSet();
        Enumeration keys = request.getHeaderNames();
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
        Enumeration keys = request.getHeaderNames();
        while (keys.hasMoreElements()) {
            keys.nextElement();
            n++;
        }
        return (n);
    }


    public Collection values() {
        List list = new ArrayList();
        Enumeration keys = request.getHeaderNames();
        while (keys.hasMoreElements()) {
            list.add(request.getHeader((String) keys.nextElement()));
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
