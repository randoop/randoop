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

package org.apache.commons.jelly.tags.jetty;

import java.io.IOException;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import org.mortbay.http.SocketListener;
import org.mortbay.util.InetAddrPort;

/**
 * Declare a socket listener for a Jetty http server
 *
 * @author  rtl
 * @version $Id: SocketListenerTag.java,v 1.3 2002/07/14 12:38:22 dion Exp $
 */
public class SocketListenerTag extends TagSupport {

    /** parameter port with default*/
    private int _port = JettyHttpServerTag.DEFAULT_PORT;

    /** parameter host, with default */
    private String _host = JettyHttpServerTag.DEFAULT_HOST;

    /** Creates a new instance of SocketListenerTag */
    public SocketListenerTag() {
    }

    /**
     * Perform the tag functionality. In this case, add a socket listener
     * for the specified host and port to the parent server,
     *
     * @param xmlOutput where to send output
     * @throws Exception when an error occurs
     */
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        JettyHttpServerTag httpserver = (JettyHttpServerTag) findAncestorWithClass(
            JettyHttpServerTag.class);
        if ( httpserver == null ) {
            throw new JellyTagException( "<socketListener> tag must be enclosed inside a <server> tag" );
        }

        try {
            httpserver.addListener(
                new SocketListener(new InetAddrPort(getHost(), getPort())));
        }
        catch (IOException e) {
            throw new JellyTagException(e);
        }

        invokeBody(xmlOutput);
    }

    //--------------------------------------------------------------------------
    // Property accessors/mutators
    //--------------------------------------------------------------------------
    /**
     * Getter for property port.
     *
     * @return value of property port.
     */
    public int getPort() {
        return _port;
    }

    /**
     * Setter for property port.
     *
     * @param port New value of property port.
     */
    public void setPort(int port) {
        _port = port;
    }

    /**
     * Getter for property host.
     *
     * @return value of property host.
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

}
