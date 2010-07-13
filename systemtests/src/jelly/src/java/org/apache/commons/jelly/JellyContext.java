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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.jelly.parser.XMLParser;
import org.apache.commons.jelly.util.ClassLoaderUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
  * <p><code>JellyContext</code> represents the Jelly context.</p>
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.10 $
  */
public class JellyContext {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(JellyContext.class);

    /** Default for inheritance of variables **/
    private static final boolean DEFAULT_INHERIT = true;

    /** Default for export of variables **/
    private static final boolean DEFAULT_EXPORT = false;

    /** String used to denote a script can't be parsed */
    private static final String BAD_PARSE = "Could not parse Jelly script";

    /**
     * The class loader to use for instantiating application objects.
     * If not specified, the context class loader, or the class loader
     * used to load this class itself, is used, based on the value of the
     * <code>useContextClassLoader</code> variable.
     */
    protected ClassLoader classLoader;

    /**
     * Do we want to use the Context ClassLoader when loading classes
     * for instantiating new objects?  Default is <code>false</code>.
     */
    protected boolean useContextClassLoader = false;

    /** The root URL context (where scripts are located from) */
    private URL rootURL;

    /** The current URL context (where relative scripts are located from) */
    private URL currentURL;

    /** Tag libraries found so far */
    private Map taglibs = new Hashtable();

    /** synchronized access to the variables in scope */
    private Map variables = new Hashtable();

    /** The parent context */
    private JellyContext parent;

    /** Do we inherit variables from parent context? */
    private boolean inherit = JellyContext.DEFAULT_INHERIT;

    /** Do we export our variables to parent context? */
    private boolean export  = JellyContext.DEFAULT_EXPORT;

    /** Should we export tag libraries to our parents context */
    private boolean exportLibraries = true;

    /** Maps a Thread to its local Script data cache. It's 
     * like a ThreadLocal, but it reclaims memory better
     * when the JellyCointext goes out of scope.
     * This isn't a ThreadLocal because of the typical usage scenario of
     * JellyContext. ThreadLocal is meant to be sued as a static variable,
     * we were using it as a local variable.
     * {@link #setThreadLocalScriptData(Script,Object)}
      */
    private Map threadLocalScriptData = Collections.synchronizedMap(new WeakHashMap());
    // THINKME: Script objects are like Object (for equals and hashCode) I think.
    //          It should be asy to optimize hash-map distribution, e.g. by
    //          shifting the hashcode return value (presuming Object.hashcode()
    //          is something like an address)

    /**
     * Create a new context with the currentURL set to the rootURL
     */
    public JellyContext() {
        this.currentURL = rootURL;
        init();
    }

    /**
     * Create a new context with the given rootURL
     * @param rootURL the root URL used in resolving absolute resources i.e. those starting with '/'
     */
    public JellyContext(URL rootURL) {
        this( rootURL, rootURL );
    }

    /**
     * Create a new context with the given rootURL and currentURL
     * @param rootURL the root URL used in resolving absolute resources i.e. those starting with '/'
     * @param currentURL the root URL used in resolving relative resources
     */
    public JellyContext(URL rootURL, URL currentURL) {
        this.rootURL = rootURL;
        this.currentURL = currentURL;
        init();
    }


    /**
     * Create a new context with the given parent context.
     * The parent's rootURL and currentURL are set on the child, and the parent's variables are
     * available in the child context under the name <code>parentScope</code>.
     *
     * @param parent the parent context for the newly created context.
     */
    public JellyContext(JellyContext parent) {
        this.parent = parent;
        this.rootURL = parent.rootURL;
        this.currentURL = parent.currentURL;
        this.variables.put("parentScope", parent.variables);
        init();
    }

    /**
     * Create a new context with the given parent context.
     * The parent's rootURL are set on the child, and the parent's variables are
     * available in the child context under the name <code>parentScope</code>.
     *
     * @param parentJellyContext the parent context for the newly created context.
     * @param currentURL the root URL used in resolving relative resources
     */
    public JellyContext(JellyContext parentJellyContext, URL currentURL) {
        this(parentJellyContext);
        this.currentURL = currentURL;
    }

    /**
     * Create a new context with the given parent context.
     * The parent's variables are available in the child context under the name <code>parentScope</code>.
     *
     * @param parentJellyContext the parent context for the newly created context.
     * @param rootURL the root URL used in resolving absolute resources i.e. those starting with '/'
     * @param currentURL the root URL used in resolving relative resources
     */
    public JellyContext(JellyContext parentJellyContext, URL rootURL, URL currentURL) {
        this(parentJellyContext, currentURL);
        this.rootURL = rootURL;
    }

    /**
     * Initialize the context.
     * This includes adding the context to itself under the name <code>context</code> and
     * making the System Properties available as <code>systemScope</code>
     */
    private void init() {
        variables.put("context",this);
        try {
            variables.put("systemScope", System.getProperties() );
        } catch (SecurityException e) {
            log.debug("security exception accessing system properties", e);
        }
    }

    /**
     * @return the parent context for this context
     */
    public JellyContext getParent() {
        return parent;
    }

    /**
     * @return the scope of the given name, such as the 'parent' scope.
     * If Jelly is used in a Servlet situation then 'request', 'session' and 'application' are other names
     * for scopes
     */
    public JellyContext getScope(String name) {
        if ( "parent".equals( name ) ) {
            return getParent();
        }
        return null;
    }

    /**
     * Finds the variable value of the given name in this context or in any other parent context.
     * If this context does not contain the variable, then its parent is used and then its parent
     * and so forth until the context with no parent is found.
     *
     * @return the value of the variable in this or one of its descendant contexts or null
     *  if the variable could not be found.
     */
    public Object findVariable(String name) {
        Object answer = variables.get(name);
        boolean definedHere = answer != null || variables.containsKey(name);

        if (definedHere) return answer;

        if ( answer == null && parent != null ) {
            answer = parent.findVariable(name);
        }
        // ### this is a hack - remove this when we have support for pluggable Scopes
        if ( answer == null ) {
            answer = getSystemProperty(name);
        }

        if (log.isDebugEnabled()) {
            log.debug("findVariable: " + name + " value: " + answer );
        }
        return answer;
    }


    /** @return the value of the given variable name */
    public Object getVariable(String name) {
        Object value = variables.get(name);
        boolean definedHere = value != null || variables.containsKey(name);

        if (definedHere) return value;

        if ( value == null && isInherit() ) {
            JellyContext parentContext = getParent();
            if (parentContext != null) {
                value = parentContext.getVariable( name );
            }
        }

        // ### this is a hack - remove this when we have support for pluggable Scopes
        if ( value == null ) {
            value = getSystemProperty(name);
        }

        return value;
    }

    /**
     * Get a system property and handle security exceptions
     * @param name the name of the property to retrieve
     * @return the value of the property, or null if a SecurityException occurs
     */
    private Object getSystemProperty(String name) {
        try {
            return System.getProperty(name);
        }
        catch (SecurityException e) {
            log.debug("security exception accessing system properties", e);
        }
        return null;
    }

    /**
     * @return the value of the given variable name in the given variable scope
     * @param name is the name of the variable
     * @param scopeName is the optional scope name such as 'parent'. For servlet environments
     * this could be 'application', 'session' or 'request'.
     */
    public Object getVariable(String name, String scopeName) {
        JellyContext scope = getScope(scopeName);
        if ( scope != null ) {
            return scope.getVariable(name);
        }
        return null;
    }



    /** Sets the value of the named variable */
    public void setVariable(String name, Object value) {
        if ( isExport() ) {
            getParent().setVariable( name, value );
            return;
        }
        if (value == null) {
            variables.remove(name);
        }
        else {
            variables.put(name, value);
        }
    }

    /**
     * Sets the value of the given variable name in the given variable scope
     * @param name is the name of the variable
     * @param scopeName is the optional scope name such as 'parent'. For servlet environments
     *  this could be 'application', 'session' or 'request'.
     * @param value is the value of the attribute
     */
    public void setVariable(String name, String scopeName, Object value) {
        JellyContext scope = getScope(scopeName);
        if ( scope != null ) {
            scope.setVariable(name, value);
        }
    }

    /** Removes the given variable */
    public void removeVariable(String name) {
        variables.remove(name);
    }

    /**
     * Removes the given variable in the specified scope.
     *
     * @param name is the name of the variable
     * @param scopeName is the optional scope name such as 'parent'. For servlet environments
     *  this could be 'application', 'session' or 'request'.
     */
    public void removeVariable(String name, String scopeName) {
        JellyContext scope = getScope(scopeName);
        if ( scope != null ) {
            scope.removeVariable(name);
        }
    }

    /**
     * @return an Iterator over the current variable names in this
     * context
     */
    public Iterator getVariableNames() {
        return variables.keySet().iterator();
    }

    /**
     * @return the Map of variables in this scope
     */
    public Map getVariables() {
        return variables;
    }

    /**
     * Sets the Map of variables to use
     */
    public void setVariables(Map variables) {
        // I have seen this fail when the passed Map contains a key, value
        // pair where the value is null
        for (Iterator iter = variables.entrySet().iterator(); iter.hasNext();) {
            Map.Entry element = (Map.Entry) iter.next();
            if (element.getValue() != null) {
                this.variables.put(element.getKey(), element.getValue());
            }
        }
        //this.variables.putAll( variables );
    }

    /**
     * A factory method to create a new child context of the
     * current context.
     */
    public JellyContext newJellyContext(Map newVariables) {
        // XXXX: should allow this new context to
        // XXXX: inherit parent contexts?
        // XXXX: Or at least publish the parent scope
        // XXXX: as a Map in this new variable scope?
        newVariables.put("parentScope", variables);
        JellyContext answer = createChildContext();
        answer.setVariables(newVariables);
        return answer;
    }

    /**
     * A factory method to create a new child context of the
     * current context.
     */
    public JellyContext newJellyContext() {
        return createChildContext();
    }
    

    /** Gets the Script data item that may have previously been stored
     * by the script, in this context, for the current thread.
     *  
     * @return the tag associated with the current context and thread
      */
    public Object getThreadScriptData(Script script) {
        if( script == null )
            return null;
        Tag tag = (Tag) getThreadScriptDataMap().get(script);
		if( tag == null && getParent() != null) {
			return getParent().getThreadScriptData(script);
		} else {
			return tag;
		}
    }
	
	/** Gets a per-thread (thread local) Map of data for use by
     * Scripts.
     * @return the thread local Map of Script data */
	public Map getThreadScriptDataMap() {
        Map rv;
        Thread t = Thread.currentThread();
        Map data = (Map) threadLocalScriptData.get(t);
        if (data == null) {
            rv = new HashMap();
            threadLocalScriptData.put(t, rv);
        } else {
            rv = data;
        }
		return rv;
	}
    
    /** Stores an object that lasts for the life of this context
     * and is local to the current thread. This method is
     * mainly intended to store Tag instances. However, any
     * Script that wants to cache data can use this
     * method.
      */
    public void setThreadScriptData(Script script, Object data) {
        getThreadScriptDataMap().put(script,data);
    }
    
    /** Clears variables set by Tags (basically, variables set in a Jelly script)
     * and data stored by {@link Script} instances.
     * @see #clearVariables()
     * @see #clearThreadScriptData()
     * @see #clearScriptData()
      */
    public void clear() {
        clearScriptData();
        clearVariables();
    }
    
    /** Clears variables set by Tags (variables set while running a Jelly script)
     * @see #clear()
     * @see #clearThreadScriptData()
     * @see #clearScriptData()
     */
    public void clearVariables() {
        variables.clear();
    }
    
    /** Clears data cached by {@link Script} instances, 
     * for this context, <strong>for the current thread</strong>.
     * The data cleared could be cached Tag instances or other data
     * saved by Script classes.
     * @see #clear()
     * @see #clearVariables()
     * @see #clearScriptData()
     */
    public void clearThreadScriptData() {
        getThreadScriptDataMap().clear();
    }
    
    /** Clears data cached by {@link Script} instances, 
     * for this context, <strong>for all threads</strong>. 
     * The data cleared could be cached Tag instances or other data
     * saved by Script classes.
     * @see #clear()
     * @see #clearThreadScriptData()
     * @see #clearVariables()
     */
    public void clearScriptData() {
        threadLocalScriptData.clear();
    }
    
    /** Registers the given tag library against the given namespace URI.
     * This should be called before the parser is used.
     */
    public void registerTagLibrary(String namespaceURI, TagLibrary taglib) {
        if (log.isDebugEnabled()) {
            log.debug("Registering tag library to: " + namespaceURI + " taglib: " + taglib);
        }
        taglibs.put(namespaceURI, taglib);

        if (isExportLibraries() && parent != null) {
            parent.registerTagLibrary( namespaceURI, taglib );
        }
    }

    /** Registers the given tag library class name against the given namespace URI.
     * The class will be loaded via the given ClassLoader
     * This should be called before the parser is used.
     */
    public void registerTagLibrary(
        String namespaceURI,
        String className) {

        if (log.isDebugEnabled()) {
            log.debug("Registering tag library to: " + namespaceURI + " taglib: " + className);
        }
        taglibs.put(namespaceURI, className);

        if (isExportLibraries() && parent != null) {
            parent.registerTagLibrary( namespaceURI, className );
        }
    }

    public boolean isTagLibraryRegistered(String namespaceURI) {
        boolean answer = taglibs.containsKey( namespaceURI );
        if (answer) {
            return true;
        }
        else if ( parent != null ) {
            return parent.isTagLibraryRegistered(namespaceURI);
        }
        else {
            return false;
        }
    }

    /**
     * @return the TagLibrary for the given namespace URI or null if one could not be found
     */
    public TagLibrary getTagLibrary(String namespaceURI) {

        // use my own mapping first, so that namespaceURIs can
        // be redefined inside child contexts...

        Object answer = taglibs.get(namespaceURI);

        if ( answer == null && parent != null ) {
            answer = parent.getTagLibrary( namespaceURI );
        }

        if ( answer instanceof TagLibrary ) {
            return (TagLibrary) answer;
        }
        else if ( answer instanceof String ) {
            String className = (String) answer;
            Class theClass = null;
            try {
                theClass = getClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e) {
                log.error("Could not find the class: " + className, e);
            }
            if ( theClass != null ) {
                try {
                    Object object = theClass.newInstance();
                    if (object instanceof TagLibrary) {
                        taglibs.put(namespaceURI, object);
                        return (TagLibrary) object;
                    }
                    else {
                        log.error(
                            "The tag library object mapped to: "
                                + namespaceURI
                                + " is not a TagLibrary. Object = "
                                + object);
                    }
                }
                catch (Exception e) {
                    log.error(
                        "Could not instantiate instance of class: " + className + ". Reason: " + e,
                        e);
                }
            }
        }

        return null;
    }

    /**
     * Attempts to parse the script from the given uri using the
     * {@link #getResource} method then returns the compiled script.
     */
    public Script compileScript(String uri) throws JellyException {
        XMLParser parser = getXMLParser();
        parser.setContext(this);
        InputStream in = getResourceAsStream(uri);
        if (in == null) {
            throw new JellyException("Could not find Jelly script: " + uri);
        }
        Script script = null;
        try {
            script = parser.parse(in);
        } catch (IOException e) {
            throw new JellyException(JellyContext.BAD_PARSE, e);
        } catch (SAXException e) {
            throw new JellyException(JellyContext.BAD_PARSE, e);
        }

        return script.compile();
    }

    /**
     * Attempts to parse the script from the given URL using the
     * {@link #getResource} method then returns the compiled script.
     */
    public Script compileScript(URL url) throws JellyException {
        XMLParser parser = getXMLParser();
        parser.setContext(this);

        Script script = null;
        try {
            script = parser.parse(url.toString());
        } catch (IOException e) {
            throw new JellyException(JellyContext.BAD_PARSE, e);
        } catch (SAXException e) {
            throw new JellyException(JellyContext.BAD_PARSE, e);
        }

        return script.compile();
    }

    /**
     * Attempts to parse the script from the given InputSource using the
     * {@link #getResource} method then returns the compiled script.
     */
    public Script compileScript(InputSource source) throws JellyException {
        XMLParser parser = getXMLParser();
        parser.setContext(this);

        Script script = null;
        try {
            script = parser.parse(source);
        } catch (IOException e) {
            throw new JellyException(JellyContext.BAD_PARSE, e);
        } catch (SAXException e) {
            throw new JellyException(JellyContext.BAD_PARSE, e);
        }

        return script.compile();
    }

    /**
     * @return a thread pooled XMLParser to avoid the startup overhead
     * of the XMLParser
     */
    protected XMLParser getXMLParser() {
        XMLParser parser = createXMLParser();
        return parser;
    }

    /**
     * Factory method to allow JellyContext implementations to overload how an XMLParser
     * is created - such as to overload what the default ExpressionFactory should be.
     */
    protected XMLParser createXMLParser() {
        return new XMLParser();
    }

    /**
     * Parses the script from the given File then compiles it and runs it.
     *
     * @return the new child context that was used to run the script
     */
    public JellyContext runScript(File file, XMLOutput output) throws JellyException {
        try {
            return runScript(file.toURL(), output, JellyContext.DEFAULT_EXPORT,
                JellyContext.DEFAULT_INHERIT);
        } catch (MalformedURLException e) {
            throw new JellyException(e.toString());
        }
    }

    /**
     * Parses the script from the given URL then compiles it and runs it.
     *
     * @return the new child context that was used to run the script
     */
    public JellyContext runScript(URL url, XMLOutput output) throws JellyException {
        return runScript(url, output, JellyContext.DEFAULT_EXPORT,
            JellyContext.DEFAULT_INHERIT);
    }

    /**
     * Parses the script from the given InputSource then compiles it and runs it.
     *
     * @return the new child context that was used to run the script
     */
    public JellyContext runScript(InputSource source, XMLOutput output) throws JellyException {
        return runScript(source, output, JellyContext.DEFAULT_EXPORT,
            JellyContext.DEFAULT_INHERIT);
    }

    /**
     * Parses the script from the given uri using the
     * JellyContext.getResource() API then compiles it and runs it.
     *
     * @return the new child context that was used to run the script
     */
    public JellyContext runScript(String uri, XMLOutput output) throws JellyException {
        URL url = null;
        try {
            url = getResource(uri);
        } catch (MalformedURLException e) {
            throw new JellyException(e.toString());
        }

        if (url == null) {
            throw new JellyException("Could not find Jelly script: " + url);
        }
        return runScript(url, output, JellyContext.DEFAULT_EXPORT,
            JellyContext.DEFAULT_INHERIT);
    }

    /**
     * Parses the script from the given uri using the
     * JellyContext.getResource() API then compiles it and runs it.
     *
     * @return the new child context that was used to run the script
     */
    public JellyContext runScript(String uri, XMLOutput output,
                          boolean export, boolean inherit) throws JellyException {
        URL url = null;
        try {
            url = getResource(uri);
        } catch (MalformedURLException e) {
            throw new JellyException(e.toString());
        }

        if (url == null) {
            throw new JellyException("Could not find Jelly script: " + url);
        }

        return runScript(url, output, export, inherit);
    }

    /**
     * Parses the script from the given file then compiles it and runs it.
     *
     * @return the new child context that was used to run the script
     */
    public JellyContext runScript(File file, XMLOutput output,
                          boolean export, boolean inherit) throws JellyException {
        try {
            return runScript(file.toURL(), output, export, inherit);
        } catch (MalformedURLException e) {
            throw new JellyException(e.toString());
        }
    }

    /**
     * Parses the script from the given URL then compiles it and runs it.
     *
     * @return the new child context that was used to run the script
     */
    public JellyContext runScript(URL url, XMLOutput output,
                          boolean export, boolean inherit) throws JellyException {
        return runScript(new InputSource(url.toString()), output, export, inherit);
    }

    /**
     * Parses the script from the given InputSource then compiles it and runs it.
     *
     * @return the new child context that was used to run the script
     */
    public JellyContext runScript(InputSource source, XMLOutput output,
                          boolean export, boolean inherit) throws JellyException {
        Script script = compileScript(source);

        URL newJellyContextURL = null;
        try {
            newJellyContextURL = getJellyContextURL(source);
        } catch (MalformedURLException e) {
            throw new JellyException(e.toString());
        }

        JellyContext newJellyContext = newJellyContext();
        newJellyContext.setRootURL( newJellyContextURL );
        newJellyContext.setCurrentURL( newJellyContextURL );
        newJellyContext.setExport( export );
        newJellyContext.setInherit( inherit );

        if ( inherit ) {
            // use the same variable scopes
            newJellyContext.variables = this.variables;
        }

        if (log.isDebugEnabled() ) {
            log.debug( "About to run script: " + source.getSystemId() );
            log.debug( "root context URL: " + newJellyContext.rootURL );
            log.debug( "current context URL: " + newJellyContext.currentURL );
        }

        script.run(newJellyContext, output);

        return newJellyContext;
    }

    /**
     * Returns a URL for the given resource from the specified path.
     * If the uri starts with "/" then the path is taken as relative to
     * the current context root.
     * If the uri is a well formed URL then it is used.
     * If the uri is a file that exists and can be read then it is used.
     * Otherwise the uri is interpreted as relative to the current context (the
     * location of the current script).
     */
    public URL getResource(String uri) throws MalformedURLException {
        if (uri.startsWith("/")) {
            // append this uri to the context root
            return createRelativeURL(rootURL, uri.substring(1));
        }
        else {
            try {
                return new URL(uri);
            }
            catch (MalformedURLException e) {
                // lets try find a relative resource
                try {
                    return createRelativeURL(currentURL, uri);
                } catch (MalformedURLException e2) {
                    throw e;
                }
            }
        }
    }

    /**
     * Attempts to open an InputStream to the given resource at the specified path.
     * If the uri starts with "/" then the path is taken as relative to
     * the current context root. If the uri is a well formed URL then it
     * is used. Otherwise the uri is interpreted as relative to the current
     * context (the location of the current script).
     *
     * @return null if this resource could not be loaded, otherwise the resources
     *  input stream is returned.
     */
    public InputStream getResourceAsStream(String uri) {
        try {
            URL url = getResource(uri);
            return url.openStream();
        }
        catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.trace(
                    "Caught exception attempting to open: " + uri + ". Exception: " + e,
                    e);
            }
            return null;
        }
    }


    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the current root context URL from which all absolute resource URIs
     *  will be relative to. For example in a web application the root URL will
     *  map to the web directory which contains the WEB-INF directory.
     */
    public URL getRootURL() {
        return rootURL;
    }

    /**
     * Sets the current root context URL from which all absolute resource URIs
     *  will be relative to. For example in a web application the root URL will
     *  map to the web directory which contains the WEB-INF directory.
     */
    public void setRootURL(URL rootURL) {
        this.rootURL = rootURL;
    }


    /**
     * @return the current URL context of the current script that is executing.
     *  This URL context is used to deduce relative scripts when relative URIs are
     *  used in calls to {@link #getResource} to process relative scripts.
     */
    public URL getCurrentURL() {
        return currentURL;
    }

    /**
     * Sets the current URL context of the current script that is executing.
     *  This URL context is used to deduce relative scripts when relative URIs are
     *  used in calls to {@link #getResource} to process relative scripts.
     */
    public void setCurrentURL(URL currentURL) {
        this.currentURL = currentURL;
    }

    /**
     * Returns whether we export tag libraries to our parents context
     * @return boolean
     */
    public boolean isExportLibraries() {
        return exportLibraries;
    }

    /**
     * Sets whether we export tag libraries to our parents context
     * @param exportLibraries The exportLibraries to set
     */
    public void setExportLibraries(boolean exportLibraries) {
        this.exportLibraries = exportLibraries;
    }


    /**
     * Sets whether we should export variable definitions to our parent context
     */
    public void setExport(boolean export) {
        this.export = export;
    }

    /**
     * @return whether we should export variable definitions to our parent context
     */
    public boolean isExport() {
        return this.export;
    }

    /**
     * Sets whether we should inherit variables from our parent context
     */
    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    /**
     * @return whether we should inherit variables from our parent context
     */
    public boolean isInherit() {
        return this.inherit;
    }


    /**
     * Return the class loader to be used for instantiating application objects
     * when required.  This is determined based upon the following rules:
     * <ul>
     * <li>The class loader set by <code>setClassLoader()</code>, if any</li>
     * <li>The thread context class loader, if it exists and the
     *     <code>useContextClassLoader</code> property is set to true</li>
     * <li>The class loader used to load the XMLParser class itself.
     * </ul>
     */
    public ClassLoader getClassLoader() {
        return ClassLoaderUtils.getClassLoader(classLoader, useContextClassLoader, getClass());
    }

    /**
     * Set the class loader to be used for instantiating application objects
     * when required.
     *
     * @param classLoader The new class loader to use, or <code>null</code>
     *  to revert to the standard rules
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Return the boolean as to whether the context classloader should be used.
     */
    public boolean getUseContextClassLoader() {
        return useContextClassLoader;
    }

    /**
     * Determine whether to use the Context ClassLoader (the one found by
     * calling <code>Thread.currentThread().getContextClassLoader()</code>)
     * to resolve/load classes.  If not
     * using Context ClassLoader, then the class-loading defaults to
     * using the calling-class' ClassLoader.
     *
     * @param use determines whether to use JellyContext ClassLoader.
     */
    public void setUseContextClassLoader(boolean use) {
        useContextClassLoader = use;
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    /**
     * @return a new relative URL from the given root and with the addition of the
     * extra relative URI
     *
     * @param rootURL is the root context from which the relative URI will be applied
     * @param relativeURI is the relative URI (without a leading "/")
     * @throws MalformedURLException if the URL is invalid.
     */
    protected URL createRelativeURL(URL rootURL, String relativeURI)
        throws MalformedURLException {
        URL url = rootURL;
        if (url == null) {
            File file = new File(System.getProperty("user.dir"));
            url = file.toURL();
        }
        String urlText = url.toString() + relativeURI;
        if ( log.isDebugEnabled() ) {
            log.debug("Attempting to open url: " + urlText);
        }
        return new URL(urlText);
    }

    /**
     * Strips off the name of a script to create a new context URL
     */
    protected URL getJellyContextURL(URL url) throws MalformedURLException {
        String text = url.toString();
        int idx = text.lastIndexOf('/');
        text = text.substring(0, idx + 1);
        return new URL(text);
    }

    /**
     * Strips off the name of a script to create a new context URL
     */
    protected URL getJellyContextURL(InputSource source) throws MalformedURLException {
        String text = source.getSystemId();
        if (text != null) {
            int idx = text.lastIndexOf('/');
            text = text.substring(0, idx + 1);
            return new URL(text);
        } else {
            return null;
        }
        
    }

    /**
     * Factory method to create a new child of this context
     */
    protected JellyContext createChildContext() {
        return new JellyContext(this);
    }

    /**
     * Change the parent context to the one provided
     * @param context the new parent context
     */
    protected void setParent(JellyContext context)
    {
        parent = context;
        this.variables.put("parentScope", parent.variables);
        // need to re-export tag libraries to the new parent
        if (isExportLibraries() && parent != null) {
            for (Iterator keys = taglibs.keySet().iterator(); keys.hasNext();)
            {
                String namespaceURI = (String) keys.next();
                Object tagLibOrClassName = taglibs.get(namespaceURI);
                if (tagLibOrClassName instanceof TagLibrary)
                {
                    parent.registerTagLibrary( namespaceURI, (TagLibrary) tagLibOrClassName );
                }
                else
                {
                    parent.registerTagLibrary( namespaceURI, (String) tagLibOrClassName );
                }
            }
        }

    }

}
