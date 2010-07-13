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

package org.apache.commons.jelly.tags.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * The base tag for all http requests
 *
 * @author  dion
 * @version $Id: HttpTagSupport.java,v 1.3 2002/07/14 16:51:33 dion Exp $
 */
public abstract class HttpTagSupport extends TagSupport {

    /** unique identifier of the tag/ variable to store result in */
    private String _var;

    /**
     * the path to be tested relative to the host/port specifed on the parent
     * {@link SuiteTag suite tag}.
     * Either this property and the suite's host
     * must be provided, or the {@link #getUrl() url} property must be specifed
     */
    private String _path;

    /**
     * The complete uri to be processed
     * Either this property or the {@link #getPath() path} and the suite's host
     * must be provided.
     */
    private String _uri;

    /** whether or not to follow redirects */
    private boolean _followRedirects = false;
    /** list of parameters as name value pairs */
    private List _parameters;
    /** list of headers as name value pairs */
    private List _requestHeaders;
    /** the header name for the user agent */
    private static final String HEADER_NAME_USER_AGENT = "User-Agent";

    /**
     * Creates a new instance of HttpTag
     */
    public HttpTagSupport() {
        setParameters(new ArrayList());
        setRequestHeaders(new ArrayList());
    }

    /**
     * @return the url specified by the tag, either the url if not null, or
     * a combination of the host, port and path
     */
    public String getResolvedUrl() {
        if (getUri() != null) {
            return getUri();
        } else {
            // build it from path, host and optionally port
            SessionTag session = (SessionTag) findAncestorWithClass(
                SessionTag.class);
            String host = session.getHost();
            String port = session.getPort();
            // short term hack, need to add port and security in
            return "http://" + host + getPath();
        }
    }

    /**
     * A method that must be implemented by subclasses to provide the
     * {@link HttpMethod url method} implementation
     *
     * @return a HttpUrlMethod implementation
     * @throws MalformedURLException when the {@link getUrl() url} or
     * {@link #getPath() path} is invalid
     */
    protected abstract HttpMethod getHttpMethod()
        throws MalformedURLException;

    /**
     * Perform the tag functionality. In this case, get the http url method
     * execute it and make it available for validation
     *
     * @param xmlOutput where to send output
     * @throws JellyTagException when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        // allow nested tags first, e.g body
        invokeBody(xmlOutput);

        // track request execution
        long start = System.currentTimeMillis();
        HttpMethod urlMethod = null;
        try {
            urlMethod = getConfiguredHttpMethod();
            getHttpClient().executeMethod(urlMethod);
        }
        catch (MalformedURLException e) {
            throw new JellyTagException(e);
        }
        catch (IOException e) {
            throw new JellyTagException(e);
        }
        long end = System.currentTimeMillis();

        // set variable to value
        if (getVar() != null) {
            getContext().setVariable(getVar(), urlMethod);
            getContext().setVariable(getVar() + ".responseTime",
                String.valueOf(end - start));
        }
    }

    /**
     * retrieve the {@link HttpUrlMethod method} from the subclass and
     * configure it ready for execution
     *
     * @return a configured {@link HttpUrlMethod method}
     * @throws MalformedURLException when retrieving the URL fails
     */
    private HttpMethod getConfiguredHttpMethod() throws
    MalformedURLException {
        // retrieve and configure url method
        HttpMethod urlMethod = getHttpMethod();
        urlMethod.setFollowRedirects(isFollowRedirects());
        // add request headers
        NameValuePair header = null;
        for (int index = 0; index < getRequestHeaders().size(); index++) {
            header = (NameValuePair) getRequestHeaders().get(index);
            urlMethod.addRequestHeader(header.getName(), header.getValue());
        }
        // add parameters
        setParameters(urlMethod);
        // add the default user agent to the list if one doesn't exist
        // and the session tag does exist and have a user agent
        if (urlMethod.getRequestHeader(HttpTagSupport.HEADER_NAME_USER_AGENT)
            == null && getSessionTag() != null
            && getSessionTag().getUserAgent() != null) {

            urlMethod.addRequestHeader(HttpTagSupport.HEADER_NAME_USER_AGENT,
                    getSessionTag().getUserAgent());
        }
        return urlMethod;
    }

    /**
     * Set the current parameters on the url method ready for processing
     *
     * @param method the {@link HttpUrlMethod method} to configure
     * @throws MalformedURLException when {@link #getHttpUrlMethod()} does
     */
    protected void setParameters(HttpMethod method) throws
    MalformedURLException {
        if (getParameters().size() > 0) {
            NameValuePair[] parameters = (NameValuePair[]) getParameters().
                toArray(new NameValuePair[0]);
            method.setQueryString(parameters);
        }
    }

    /**
     * retrieve the optional parent session tag
     *
     * @return the ancestor tag with class {@link SessionTag} or null if
     *      not found
     */
    private SessionTag getSessionTag() {
        SessionTag sessionTag = (SessionTag) findAncestorWithClass(
            SessionTag.class);
        return sessionTag;
    }

    /**
     * return a HttpClient shared on the session tag, or a new one if no
     * session tag exists
     *
     * @return the shared http client from the session tag, or create a new one.
     */
    private HttpClient getHttpClient() {
        SessionTag session = getSessionTag();
        HttpClient client = null;
        if (session != null) {
            client = session.getHttpClient();
            client.setStrictMode(session.isStrictMode());
        } else {
            client = new HttpClient();
        }
        return client;
    }

    /**
     * Add a parameter to the list
     *
     * @param name the parameter name
     * @param value the parameter value
     */
    public void addParameter(String name, String value) {
        getParameters().add(new NameValuePair(name, value));
    }

    /**
     * Add a request header to the list
     *
     * @param name the header name
     * @param value the header value
     */
    public void addRequestHeader(String name, String value) {
        getRequestHeaders().add(new NameValuePair(name, value));
    }

    //--------------------------------------------------------------------------
    // Property accessors/mutators
    //--------------------------------------------------------------------------

    /**
     * Getter for property var.
     *
     * @return Value of property var.
     */
    public String getVar() {
        return _var;
    }

    /**
     * Setter for property var.
     *
     * @param var New value of property var.
     */
    public void setVar(String var) {
        _var = var;
    }

    /**
     * Getter for property path.
     *
     * @return Value of property path.
     */
    public String getPath() {
        return _path;
    }

    /**
     * Setter for property path.
     *
     * @param path New value of property path.
     */
    public void setPath(String path) {
        _path = path;
    }

    /**
     * Getter for property uri.
     *
     * @return Value of property uri.
     */
    public String getUri() {
        return _uri;
    }

    /**
     * Setter for property uri.
     *
     * @param uri New value of property uri.
     */
    public void setUri(String uri) {
        _uri = uri;
    }

    /**
     * Getter for property followRedirects.
     *
     * @return Value of property followRedirects.
     */
    public boolean isFollowRedirects() {
        return _followRedirects;
    }

    /**
     * Setter for property followRedirects.
     *
     * @param followRedirects New value of property followRedirects.
     */
    public void setFollowRedirects(boolean followRedirects) {
        _followRedirects = followRedirects;
    }

    /**
     * Getter for property parameters.
     *
     * @return Value of property parameters.
     */
    public List getParameters() {
        return _parameters;
    }

    /**
     * Setter for property parameters.
     *
     * @param parameters New value of property parameters.
     */
    public void setParameters(List parameters) {
        _parameters = parameters;
    }

    /**
     * Getter for property requestHeaders.
     *
     * @return Value of property requestHeaders.
     */
    public List getRequestHeaders() {
        return _requestHeaders;
    }

    /**
     * Setter for property requestHeaders.
     *
     * @param requestHeaders New value of property requestHeaders.
     */
    public void setRequestHeaders(List requestHeaders) {
        _requestHeaders = requestHeaders;
    }

}
