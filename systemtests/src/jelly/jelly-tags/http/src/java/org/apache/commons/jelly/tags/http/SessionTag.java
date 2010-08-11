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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * A http session. This is the container for data shared across requests
 *
 * @author dion
 */
public class SessionTag extends TagSupport {

    /** default host for requests */
    private String _host;
    /** default port for requests */
    private String _port;
    /** Proxy details for requests */
    private Proxy _proxy = new Proxy();
    /** whether the default is for secure comms */
    private boolean _secure;
    /** the browser identifier */
    private String _userAgent;
    /** strict compliance */
    private boolean _strictMode = false;

    /** http client used to store state and execute requests */
    private HttpClient _httpClient;

    /**
     * Creates a new instance of SessionTag
     */
    public SessionTag() {
    }

    /**
     * Process the tag
     *
     * @param xmlOutput to write output
     * @throws Exception when any error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        if (_httpClient == null)
        {
            _httpClient = new HttpClient();
        }
        
        if (isProxyAvailable()) {
            _httpClient.getHostConfiguration().setProxy(getProxyHost(), getProxyPort());
        }

        invokeBody(xmlOutput);
    }

    /**
     * Getter for property httpClient.
     *
     * @return Value of property httpClient.
     */
    public HttpClient getHttpClient() {
        return _httpClient;
    }

    /**
     * Setter for property httpClient.
     *
     * @param httpClient New value of property httpClient.
     */
    public void setHttpClient(HttpClient httpClient) {
        _httpClient = httpClient;
    }
    /**
     * Tests whether the {@link #getProxy() proxy} is ready for use
     *
     * @return true if the {@link #getProxy() proxy} is configured for use
     */
    public boolean isProxyAvailable() {
        return getProxy() != null && getProxy().getHost() != null
            && getProxy().getPort() != Proxy.PORT_UNSPECIFIED;
    }

    /**
     * Helper method for proxy host property
     *
     * @return the {@link #getProxy() proxy's} host property
     */
    public String getProxyHost() {
        return getProxy().getHost();
    }

    /**
     * Helper method for proxy <code>host</code> property
     *
     * @param host the {@link #getProxy() proxy's} host property
     */
    public void setProxyHost(String host) {
        getProxy().setHost(host);
    }

    /**
     * Helper method for proxy <code>port</code> property
     *
     * @return the {@link #getProxy() proxy's} port property
     */
    public int getProxyPort() {
        return getProxy().getPort();
    }

    /**
     * Helper method for proxy <code>port</code> property
     *
     * @param port the {@link #getProxy() proxy's} port property
     */
    public void setProxyPort(int port) {
        getProxy().setPort(port);
    }

    /**
     * Getter for property host.
     *
     * @return Value of property host.
     */
    public String getHost() {
        return _host;
    }

    /**
     * Setter for property host.
     *
     * @param host New value of property host.
     */
    public void setHost(String host) {
        _host = host;
    }

    /** Getter for property port.
     * @return Value of property port.
     */
    public String getPort() {
        return _port;
    }

    /** Setter for property port.
     * @param port New value of property port.
     */
    public void setPort(String port) {
        _port = port;
    }

    /**
     * Getter for property proxy.
     *
     * @return Value of property proxy.
     */
    public Proxy getProxy() {
        return _proxy;
    }

    /**
     * Setter for property proxy.
     *
     * @param proxy New value of property proxy.
     */
    public void setProxy(Proxy proxy) {
        _proxy = proxy;
    }

    /**
     * Getter for property secure.
     *
     * @return Value of property secure.
     */
    public boolean isSecure() {
        return _secure;
    }

    /**
     * Setter for property secure.
     *
     * @param secure New value of property secure.
     */
    public void setSecure(boolean secure) {
        _secure = secure;
    }

    /** Getter for property userAgent.
     * @return Value of property userAgent.
     */
    public String getUserAgent() {
        return _userAgent;
    }

    /** Setter for property userAgent.
     * @param userAgent New value of property userAgent.
     */
    public void setUserAgent(String userAgent) {
        _userAgent = userAgent;
    }

    /** Getter for property strictMode.
     * @return Value of property strictMode.
     */
    public boolean isStrictMode() {
        return _strictMode;
    }

    /** Setter for property strictMode.
     * @param strictMode New value of property strictMode.
     */
    public void setStrictMode(boolean strictMode) {
        _strictMode = strictMode;
    }

}
