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


import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.chain.web.WebContext;


/**
 * <p>Concrete implementation of {@link WebContext} suitable for use in
 * Servlets and JSP pages.  The abstract methods are mapped to the appropriate
 * collections of the underlying servlet context, request, and response
 * instances that are passed to the constructor (or the initialize method).</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2004/02/25 00:01:04 $
 */

public class ServletWebContext extends WebContext {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct an uninitialized {@link ServletWebContext} instance.</p>
     */
    public ServletWebContext() {

        ;

    }


    /**
     * <p>Construct a {@link ServletWebContext} instance that is initialized
     * with the specified Servlet API objects.</p>
     *
     * @param context The <code>ServletContext</code> for this web application
     * @param request The <code>HttpServletRequest</code> for this request
     * @param response The <code>HttpServletResponse</code> for this request
     */
    public ServletWebContext(ServletContext context,
                             HttpServletRequest request,
                             HttpServletResponse response) {

        initialize(context, request, response);

    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The lazily instantiated <code>Map</code> of application scope
     * attributes.</p>
     */
    private Map applicationScope = null;


    /**
     * <p>The <code>ServletContext</code> for this web application.</p>
     */
    protected ServletContext context = null;


    /**
     * <p>The lazily instantiated <code>Map</code> of header name-value
     * combinations (immutable).</p>
     */
    private Map header = null;


    /**
     * <p>The lazily instantitated <code>Map</code> of header name-values
     * combinations (immutable).</p>
     */
    private Map headerValues = null;


    /**
     * <p>The lazily instantiated <code>Map</code> of context initialization
     * parameters.</p>
     */
    private Map initParam = null;


    /**
     * <p>The lazily instantiated <code>Map</code> of request
     * parameter name-value.</p>
     */
    private Map param = null;


    /**
     * <p>The lazily instantiated <code>Map</code> of request
     * parameter name-values.</p>
     */
    private Map paramValues = null;


    /**
     * <p>The <code>HttpServletRequest</code> for this request.</p>
     */
    protected HttpServletRequest request = null;


    /**
     * <p>The lazily instantiated <code>Map</code> of request scope
     * attributes.</p>
     */
    private Map requestScope = null;


    /**
     * <p>The <code>HttpServletResponse</code> for this request.</p>
     */
    protected HttpServletResponse response = null;


    /**
     * <p>The lazily instantiated <code>Map</code> of session scope
     * attributes.</p>
     */
    private Map sessionScope = null;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Return the {@link ServletContext} for this context.</p>
     */
    public ServletContext getContext() {

    return (this.context);

    }


    /**
     * <p>Return the {@link HttpServletRequest} for this context.</p>
     */
    public HttpServletRequest getRequest() {

    return (this.request);

    }


    /**
     * <p>Return the {@link HttpServletResponse} for this context.</p>
     */
    public HttpServletResponse getResponse() {

    return (this.response);

    }


    /**
     * <p>Initialize (or reinitialize) this {@link ServletWebContext} instance
     * for the specified Servlet API objects.</p>
     *
     * @param context The <code>ServletContext</code> for this web application
     * @param request The <code>HttpServletRequest</code> for this request
     * @param response The <code>HttpServletResponse</code> for this request
     */
    public void initialize(ServletContext context,
                           HttpServletRequest request,
                           HttpServletResponse response) {

        // Save the specified Servlet API object references
        this.context = context;
        this.request = request;
        this.response = response;

        // Perform other setup as needed

    }


    /**
     * <p>Release references to allocated resources acquired in
     * <code>initialize()</code> of via subsequent processing.  After this
     * method is called, subsequent calls to any other method than
     * <code>initialize()</code> will return undefined results.</p>
     */
    public void release() {

        // Release references to allocated collections
        applicationScope = null;
        header = null;
        headerValues = null;
        initParam = null;
        param = null;
        paramValues = null;
        requestScope = null;
        sessionScope = null;

        // Release references to Servlet API objects
        context = null;
        request = null;
        response = null;

    }



    // ------------------------------------------------------ WebContext Methods


    public Map getApplicationScope() {

        if ((applicationScope == null) && (context != null)) {
            applicationScope = new ServletApplicationScopeMap(context);
        }
        return (applicationScope);

    }


    public Map getHeader() {

        if ((header == null) && (request != null)) {
            header = new ServletHeaderMap(request);
        }
        return (header);

    }


    public Map getHeaderValues() {

        if ((headerValues == null) && (request != null)) {
            headerValues = new ServletHeaderValuesMap(request);
        }
        return (headerValues);

    }


    public Map getInitParam() {

        if ((initParam == null) && (context != null)) {
            initParam = new ServletInitParamMap(context);
        }
        return (initParam);

    }


    public Map getParam() {

        if ((param == null) && (request != null)) {
            param = new ServletParamMap(request);
        }
        return (param);

    }


    public Map getParamValues() {

        if ((paramValues == null) && (request != null)) {
            paramValues = new ServletParamValuesMap(request);
        }
        return (paramValues);

    }


    public Map getRequestScope() {

        if ((requestScope == null) && (request != null)) {
            requestScope = new ServletRequestScopeMap(request);
        }
        return (requestScope);

    }


    public Map getSessionScope() {

        if ((sessionScope == null) && (request != null)) {
            sessionScope = new ServletSessionScopeMap(request.getSession());
        }
        return (sessionScope);

    }



}
