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

package org.apache.commons.jelly.avalon;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

// Avalon
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

// Jelly
import org.apache.commons.jelly.Jelly;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

/**
 * An Avalon based service for executing Jelly scripts. The
 * service allows executing a script based on a name as well
 * as by a URL.
 *
 * @author <a href="mailto:robert@bull-enterprises.com">Robert McIntosh</a>
 * @version 1.1
 */
public interface JellyService {

    /**
     * Executes a named script with the supplied
     * Map of parameters.
     *
     * @param params Parameters to be supplied to the script
     * @return All of the variables from the JellyContext
     * @exception Exception if the script raises some kind of exception while processing
     */
    public Map runNamedScript( String name, Map params ) throws Exception;

    /**
     * Executes a named script with the supplied
     * Map of parameters.
     *
     * @param name is the name of the script to run
     * @param params Parameters to be supplied to the script
     * @param output is the XMLOutput for any output to be sent
     * @return All of the variables from the JellyContext
     * @exception Exception if the script raises some kind of exception while processing
     */
    public Map runNamedScript( String name, Map params, XMLOutput output ) throws Exception;

    /**
     * Executes a named script with the supplied
     * Map of parameters and send the output of the script
     * to the supplied output stream.
     *
     * @param name is the name of the script to run
     * @param params Parameters to be supplied to the script
     * @param out is the outputStream for output to be sent
     * @return All of the variables from the JellyContext
     * @exception Exception if the script raises some kind of exception while processing
     */
    public Map runNamedScript( String name, Map params, OutputStream out ) throws Exception;

    /**
     * Runs a script from the supplied url
     *
     * @param url The URL of the script
     * @param params Parameters to be supplied to the script
     * @param output is the XMLOutput where output of the script will go
     * @return All of the variables from the JellyContext
     */
    public Map runScript( String url, Map params, XMLOutput output ) throws Exception;

    /**
     * Runs a script from the supplied url and sends the output of the script to
     * the supplied OutputStream.
     *
     * @param url The URL of the script
     * @param params Parameters to be supplied to the script
     * @param out The OutputStream to send the output of the script to
     * @return All of the variables from the JellyContext
     * @exception Exception if the script raises some kind of exception while processing
     */
    public Map runScript( String url, Map params, OutputStream out ) throws Exception;

    /**
     * Runs a script from the supplied url
     *
     * @param url The URL of the script
     * @param params Parameters to be supplied to the script
     * @return All of the variables from the JellyContext
     * @exception Exception if the script raises some kind of exception while processing
     */
    public Map runScript( String url, Map params ) throws Exception;

}

