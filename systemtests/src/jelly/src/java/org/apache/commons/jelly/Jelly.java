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

package org.apache.commons.jelly;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.jelly.util.ClassLoaderUtils;
import org.apache.commons.jelly.util.CommandLineParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

/**
 * <p><code>Jelly</code> is a helper class which is capable of
 * running a Jelly script. This class can be used from the command line
 * or can be used as the basis of an Ant task.</p> Command line usage is as follows:
 *
 * <pre>
 * jelly [scriptFile] [-script scriptFile -o outputFile -Dsysprop=syspropval]
 * </pre>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.35 $
 */
public class Jelly {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(Jelly.class);

    /** The JellyContext to use */
    private JellyContext context;

    /** The URL of the script to execute */
    private URL url;

    /** The URL of the root context for other scripts */
    private URL rootContext;

    /** Whether we have loaded the properties yet */
    private boolean loadedProperties = false;

    /**
     * whether to override the default namespace
     */
    private String defaultNamespaceURI = null;

    /**
     * whether or not to validate the Jelly script
     */
    private boolean validateXML = false;

    public Jelly() {
    }

    /**
     * Usage: jelly [scriptFile] [-script scriptFile -o outputFile -Dsysprop=syspropval]
     */
    public static void main(String[] args) throws Exception {

        try {
            if (args.length <= 0) {
                System.out.println("Usage: jelly [scriptFile] [-script scriptFile -o outputFile -Dsysprop=syspropval]");
                return;
            }

            // parse the command line options using CLI
            // using a separate class to avoid unnecessary
            // dependencies
            CommandLineParser.getInstance().invokeCommandLineJelly(args);
        }
        catch (JellyException e) {
            Throwable cause = e.getCause();

            if (cause == null) {
                e.printStackTrace();
            } else {
                cause.printStackTrace();
            }
        }
    }


    public static String getJellyVersion() {
        return readBuildTimestampResource("jelly-version.txt");
    }

    public static String getJellyBuildDate() {
        return readBuildTimestampResource("jelly-build-date.txt");
    }

    private static String readBuildTimestampResource(String name) {
        java.io.Reader in = null;
        try {
            java.io.StringWriter w = new java.io.StringWriter();
            in = new java.io.InputStreamReader(Jelly.class.getResourceAsStream(name),"utf-8");
            int r;
            while ( (r=in.read()) >= 0 ) {
                w.write((char) r);
            }
            return w.toString();
        } catch(Exception ex) {
            ex.printStackTrace();
            try { in.close(); } catch(Exception e) {}
            throw new IllegalStateException("Resource \"" + name + "\" not found.");
        }
    }



    /**
     * Compiles the script
     */
    public Script compileScript() throws JellyException {
        if (! loadedProperties) {
            loadedProperties = true;
            loadJellyProperties();
        }

        XMLParser parser = new XMLParser();
        try {
            parser.setContext(getJellyContext());
        } catch (MalformedURLException e) {
            throw new JellyException(e.toString());
        }

        Script script = null;
        try {
            parser.setDefaultNamespaceURI(this.defaultNamespaceURI);
            parser.setValidating(this.validateXML);
            script = parser.parse(getUrl());
            script = script.compile();
            if (log.isDebugEnabled()) {
               log.debug("Compiled script: " + getUrl());
            }
        } catch (IOException e) {
            throw new JellyException("could not parse Jelly script",e);
        } catch (SAXException e) {
            throw new JellyException("could not parse Jelly script",e);
        }

        return script;
    }


    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the script URL to use as an absolute URL or a relative filename
     */
    public void setScript(String script) throws MalformedURLException {
        setUrl(resolveURL(script));
    }

    public URL getUrl() {
        return url;
    }

    /**
     * Sets the script URL to use
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Gets the root context
     */
    public URL getRootContext() throws MalformedURLException {
        if (rootContext == null) {
            rootContext = new File(System.getProperty("user.dir")).toURL();
        }
        return rootContext;
    }

    /**
     * Sets the root context
     */
    public void setRootContext(URL rootContext) {
        this.rootContext = rootContext;
    }

    /**
     * The context to use
     */
    public JellyContext getJellyContext() throws MalformedURLException {
        if (context == null) {
            // take off the name off the URL
            String text = getUrl().toString();
            int idx = text.lastIndexOf('/');
            text = text.substring(0, idx + 1);
            context = new JellyContext(getRootContext(), new URL(text));
        }
        return context;
    }


    /**
     * Set the jelly namespace to use for unprefixed elements.
     * Will be overridden by an explicit namespace in the
     * XML document.
     *
     * @param namespace jelly namespace to use (e.g. 'jelly:core')
     */
    public void setDefaultNamespaceURI(String namespace) {
        this.defaultNamespaceURI = namespace;
    }

    /**
     * When set to true, the XML parser will attempt to validate
     * the Jelly XML before converting it into a Script.
     *
     * @param validate whether or not to validate
     */
    public void setValidateXML(boolean validate) {
        this.validateXML = validate;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    /**
     * @return the URL for the relative file name or absolute URL
     */
    protected URL resolveURL(String name) throws MalformedURLException {

        URL resourceUrl = ClassLoaderUtils.getClassLoader(getClass()).getResource(name);
        if (resourceUrl == null)
        {
            File file = new File(name);
            if (file.exists()) {
                return file.toURL();
            }
            return new URL(name);
        } else {
            return resourceUrl;
        }
    }

    /**
     * Attempts to load jelly.properties from the current directory,
     * the users home directory or from the classpath
     */
    protected void loadJellyProperties() {
        InputStream is = null;

        String userDir = System.getProperty("user.home");
        File f = new File(userDir + File.separator + "jelly.properties");
        loadProperties(f);

        f = new File("jelly.properties");
        loadProperties(f);


        is = ClassLoaderUtils.getClassLoader(getClass()).getResourceAsStream("jelly.properties");
        if (is != null) {
            try {
                loadProperties(is);
            }
            catch (Exception e) {
                log.error( "Caught exception while loading jelly.properties from the classpath. Reason: " + e, e );
            }
        }
    }

    /**
     * Load properties from a file into the context
     * @param f
     */
    private void loadProperties(File f) {
        InputStream is = null;
        try {
            if (f.exists()) {
                is = new FileInputStream(f);
                loadProperties(is);
            }
        } catch (Exception e) {
            log.error( "Caught exception while loading: " + f.getName() + ". Reason: " + e, e );
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    if (log.isDebugEnabled()) log.debug("error closing property input stream", e);
                }
            }
        }
    }

    /**
     * Loads the properties from the given input stream
     */
    protected void loadProperties(InputStream is) throws IOException {
        JellyContext theContext = getJellyContext();
        Properties props = new Properties();
        props.load(is);
        Enumeration propsEnum = props.propertyNames();
        while (propsEnum.hasMoreElements()) {
            String key = (String) propsEnum.nextElement();
            String value = props.getProperty(key);

            // @todo we should parse the value in case its an Expression
            theContext.setVariable(key, value);
        }
    }
}
