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
 * @version $Revision: 1.4 $
 */
public class JellyServiceImpl implements JellyService, Configurable {

    private boolean m_configured = false;
    private Map m_scripts = new HashMap();

    /**
     * Constructor for JellyService.
     */
    public JellyServiceImpl() {
        super();
    }

    /**
     * @see org.apache.commons.jelly.avalon.JellyService.runNamedScript(String, Map)
     */
    public Map runNamedScript( String name, Map params ) throws Exception {
        return runNamedScript(name, params, createXMLOutput());
    }

    /**
     * @see org.apache.commons.jelly.avalon.JellyService.runNamedScript(String, Map, XMLOutput)
     */
    public Map runNamedScript( String name, Map params, XMLOutput output ) throws Exception {
        if( !m_scripts.containsKey( name ) )
            throw new JellyException( "No script exists for script name [" + name + "]" );

        Script script = (Script)m_scripts.get( name );
        JellyContext context = createJellyContext();

        context.setVariables( params );

        script.run( context, output );
        return context.getVariables();
    }

     /**
     * @see org.apache.commons.jelly.avalon.JellyService.runNamedScript(String, Map, OutputStream)
     */
    public Map runNamedScript( String name, Map params, OutputStream out ) throws Exception {
        XMLOutput xmlOutput = XMLOutput.createXMLOutput( out );
        Map answer = runNamedScript(name, params, xmlOutput);
        xmlOutput.flush();
        return answer;
    }

     /**
     * @see org.apache.commons.jelly.avalon.JellyService.runScript(String, Map, XMLOutput)
     */
    public Map runScript( String url, Map params, XMLOutput output ) throws Exception {
        URL actualUrl = null;
        try {
           actualUrl = new URL( url );
        }
        catch( MalformedURLException x ) {
            throw new JellyException( "Could not find script at URL [" + url + "]: " +
                                        x.getMessage(), x );
        }

        // Set up the context
        JellyContext context = createJellyContext();
        context.setVariables( params );

        // Run the script
        context.runScript(url, output);
        return context.getVariables();
    }

     /**
     * @see org.apache.commons.jelly.avalon.JellyService.runScript(String, Map, OutputStream)
     */
    public Map runScript( String url, Map params, OutputStream out ) throws Exception {
        XMLOutput xmlOutput = XMLOutput.createXMLOutput( out );
        Map answer = runScript(url, params, xmlOutput);
        xmlOutput.flush();
        return answer;
    }

     /**
     * @see org.apache.commons.jelly.avalon.JellyService.runScript(String, Map)
     */
    public Map runScript( String url, Map params ) throws Exception {
        return runScript(url, params, createXMLOutput());
    }


    // Configurable interface
    //-------------------------------------------------------------------------


    /**
     * <p>Configures the Jelly Service with named scripts.</p>
     *
     * <p>
     * The configuration looks like:
     * </p>
     * <p>
     * &lt;jelly&gt;<br />
     * &nbsp;&nbsp;&lt;script&gt;<br />
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;name&gt;script name&lt;/name&gt;<br />
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;url validate="false"&gt;url to script file&lt;/url&gt;<br />
     * &nbsp;&nbsp;&lt;/script&gt;<br />
     * &lt;/jelly&gt;<br />
     * </p>
     * <p>
     *   Where each &lt;script&gt; element defines a seperate script. The validate attribute
     *   on the url tag is optional and defaults to false.
     * </p>
     *
     * @param config The configuration
     * @exception ConfigurationException
     */
    public void configure( Configuration config ) throws ConfigurationException {
        if( m_configured )
            throw new ConfigurationException( "configure may only be executed once" );

        if( !"jelly".equals( config.getName() ) )
            throw new ConfigurationException( "Expected <jelly> but got " + config.getName() );

        // Configure named scripts
        Configuration[] scripts = config.getChildren( "scripts" );
        for (int i = 0; i < scripts.length; i++) {
            String name = config.getChild( "name" ).getValue();

            // Try to load and compile the script
            try {
                String scriptName = config.getChild( "url" ).getValue();
                // Try to load the script via file, then by URL, then by classloader
                URL url = null;
                File file = new File( scriptName );
                if( file.exists() ) {
                    url = file.toURL();
                }
                else {
                    try {
                        url = new URL( scriptName );
                    }
                    catch( MalformedURLException mfue ) {
                      // Last try, via classloader
                      url = getClass().getResource( scriptName );
                    }
                }

                // All atempts failed...
                if( url == null )
                    throw new ConfigurationException( "Could not find script [" + scriptName + "]" );

                // Get the script and store it
                Jelly jelly = new Jelly();
                jelly.setUrl( url );
                boolean validate = config.getChild( "url" ).getAttributeAsBoolean( "validate",  false );
                jelly.setValidateXML( validate );
                Script script = jelly.compileScript();

                m_scripts.put( name, script );
            }
            catch( Throwable t ) {
                throw new ConfigurationException( "Could not load script [" + name + "]: " + t.getMessage() );
            }
        }
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new JellyContext instance. Derived classes
     * could overload this method to provide a custom JellyContext instance.
     */
    protected JellyContext createJellyContext() {
        return new JellyContext();
    }

    /**
     * Factory method to create a new XMLOutput to give to scripts as they run.
     * Derived classes could overload this method, such as to pipe output to
     * some log file etc.
     */
    protected XMLOutput createXMLOutput() {
        // output will just be ignored
        return new XMLOutput();
    }

}

