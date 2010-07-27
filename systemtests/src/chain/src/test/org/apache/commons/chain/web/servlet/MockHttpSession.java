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


import org.apache.commons.chain.web.MockEnumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;



// Mock Object for HttpSession (Version 2.3)
public class MockHttpSession implements HttpSession {



    public MockHttpSession() {
        super();
    }


    public MockHttpSession(ServletContext servletContext) {
        super();
        setServletContext(servletContext);
    }



    protected HashMap attributes = new HashMap();
    protected ServletContext servletContext = null;


    // --------------------------------------------------------- Public Methods


    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


    // ---------------------------------------------------- HttpSession Methods


    public Object getAttribute(String name) {
        return (attributes.get(name));
    }


    public Enumeration getAttributeNames() {
        return (new MockEnumeration(attributes.keySet().iterator()));
    }


    public long getCreationTime() {
        throw new UnsupportedOperationException();
    }


    public String getId() {
        throw new UnsupportedOperationException();
    }


    public long getLastAccessedTime() {
        throw new UnsupportedOperationException();
    }


    public int getMaxInactiveInterval() {
        throw new UnsupportedOperationException();
    }


    public ServletContext getServletContext() {
        return (this.servletContext);
    }


    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException();
    }


    public Object getValue(String name) {
        throw new UnsupportedOperationException();
    }


    public String[] getValueNames() {
        throw new UnsupportedOperationException();
    }


    public void invalidate() {
        throw new UnsupportedOperationException();
    }


    public boolean isNew() {
        throw new UnsupportedOperationException();
    }


    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException();
    }


    public void removeAttribute(String name) {
        attributes.remove(name);
    }


    public void removeValue(String name) {
        throw new UnsupportedOperationException();
    }


    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }


    public void setMaxInactiveInterval(int interval) {
        throw new UnsupportedOperationException();
    }


}
