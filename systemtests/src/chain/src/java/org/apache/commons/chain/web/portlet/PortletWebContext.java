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
package org.apache.commons.chain.web.portlet;


import java.util.Collections;
import java.util.Map;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import org.apache.commons.chain.web.WebContext;


/**
 * <p>Concrete implementation of {@link WebContext} suitable for use in
 * portlets.  The abstract methods are mapped to the appropriate
 * collections of the underlying portlet context, request, and response
 * instances that are passed to the constructor (or the initialize method).</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2004/02/25 00:01:06 $
 */

public class PortletWebContext extends WebContext {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct an uninitialized {@link PortletWebContext} instance.</p>
     */
    public PortletWebContext() {

        ;

    }


    /**
     * <p>Construct a {@link PortletWebContext} instance that is initialized
     * with the specified Portlet API objects.</p>
     *
     * @param context The <code>PortletContext</code> for this web application
     * @param request The <code>PortletRequest</code> for this request
     * @param response The <code>PortletResponse</code> for this request
     */
    public PortletWebContext(PortletContext context,
                             PortletRequest request,
                             PortletResponse response) {

        initialize(context, request, response);

    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The lazily instantiated <code>Map</code> of application scope
     * attributes.</p>
     */
    private Map applicationScope = null;


    /**
     * <p>The <code>PortletContext</code> for this web application.</p>
     */
    protected PortletContext context = null;


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
     * <p>The <code>PortletRequest</code> for this request.</p>
     */
    protected PortletRequest request = null;


    /**
     * <p>The lazily instantiated <code>Map</code> of request scope
     * attributes.</p>
     */
    private Map requestScope = null;


    /**
     * <p>The <code>PortletResponse</code> for this request.</p>
     */
    protected PortletResponse response = null;


    /**
     * <p>The lazily instantiated <code>Map</code> of session scope
     * attributes.</p>
     */
    private Map sessionScope = null;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Return the {@link PortletContext} for this context.</p>
     */
    public PortletContext getContext() {

    return (this.context);

    }


    /**
     * <p>Return the {@link PortletRequest} for this context.</p>
     */
    public PortletRequest getRequest() {

    return (this.request);

    }


    /**
     * <p>Return the {@link PortletResponse} for this context.</p>
     */
    public PortletResponse getResponse() {

    return (this.response);

    }


    /**
     * <p>Initialize (or reinitialize) this {@link PortletWebContext} instance
     * for the specified Portlet API objects.</p>
     *
     * @param context The <code>PortletContext</code> for this web application
     * @param request The <code>PortletRequest</code> for this request
     * @param response The <code>PortletResponse</code> for this request
     */
    public void initialize(PortletContext context,
                           PortletRequest request,
                           PortletResponse response) {

        // Save the specified Portlet API object references
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

        // Release references to Portlet API objects
        context = null;
        request = null;
        response = null;

    }



    // ------------------------------------------------------ WebContext Methods


    public Map getApplicationScope() {

        if ((applicationScope == null) && (context != null)) {
            applicationScope = new PortletApplicationScopeMap(context);
        }
        return (applicationScope);

    }


    public Map getHeader() {

        if ((header == null) && (request != null)) {
        //            header = new PortletHeaderMap(request);
        header = Collections.EMPTY_MAP;
        }
        return (header);

    }


    public Map getHeaderValues() {

        if ((headerValues == null) && (request != null)) {
        //            headerValues = new PortletHeaderValuesMap(request);
        headerValues = Collections.EMPTY_MAP;
        }
        return (headerValues);

    }


    public Map getInitParam() {

        if ((initParam == null) && (context != null)) {
            initParam = new PortletInitParamMap(context);
        }
        return (initParam);

    }


    public Map getParam() {

        if ((param == null) && (request != null)) {
            param = new PortletParamMap(request);
        }
        return (param);

    }


    public Map getParamValues() {

        if ((paramValues == null) && (request != null)) {
            paramValues = new PortletParamValuesMap(request);
        }
        return (paramValues);

    }


    public Map getRequestScope() {

        if ((requestScope == null) && (request != null)) {
            requestScope = new PortletRequestScopeMap(request);
        }
        return (requestScope);

    }


    public Map getSessionScope() {

        if ((sessionScope == null) && (request != null)) {
            sessionScope =
        new PortletSessionScopeMap(request.getPortletSession());
        }
        return (sessionScope);

    }



}
