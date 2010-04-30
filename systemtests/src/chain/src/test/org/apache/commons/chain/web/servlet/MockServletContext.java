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


import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;


// Mock Object for ServletContext (Version 2.3)
public class MockServletContext implements ServletContext {


    private Hashtable attributes = new Hashtable();
    private Hashtable parameters = new Hashtable();


    // --------------------------------------------------------- Public Methods


    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }


    // ------------------------------------------------- ServletContext Methods


    public Object getAttribute(String name) {
        return (attributes.get(name));
    }

    public Enumeration getAttributeNames() {
        return (attributes.keys());
    }

    public ServletContext getContext(String uripath) {
        throw new UnsupportedOperationException();
    }

    public String getInitParameter(String name) {
        return ((String) parameters.get(name));
    }

    public Enumeration getInitParameterNames() {
        return (parameters.keys());
    }

    public int getMajorVersion() {
        return (2);
    }

    public String getMimeType(String path) {
        throw new UnsupportedOperationException();
    }

    public int getMinorVersion() {
        return (3);
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        throw new UnsupportedOperationException();
    }

    public String getRealPath(String path) {
        throw new UnsupportedOperationException();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException();
    }

    public URL getResource(String path) throws MalformedURLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getResourceAsStream(String path) {
        throw new UnsupportedOperationException();
    }

    public Set getResourcePaths(String path) {
        throw new UnsupportedOperationException();
    }

    public Servlet getServlet(String name) throws ServletException {
        throw new UnsupportedOperationException();
    }

    public String getServletContextName() {
        return ("MockServletContext");
    }

    public String getServerInfo() {
        return ("MockServletContext");
    }

    public Enumeration getServlets() {
        throw new UnsupportedOperationException();
    }

    public Enumeration getServletNames() {
        throw new UnsupportedOperationException();
    }

    public void log(String message) {
        throw new UnsupportedOperationException();
    }

    public void log(Exception exception, String message) {
        throw new UnsupportedOperationException();
    }

    public void log(String message, Throwable exception) {
        throw new UnsupportedOperationException();
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }


}
