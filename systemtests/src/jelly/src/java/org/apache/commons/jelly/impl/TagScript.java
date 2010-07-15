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
package org.apache.commons.jelly.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.ConvertingWrapDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;

import org.apache.commons.jelly.CompilableTag;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.DynaTag;
import org.apache.commons.jelly.LocationAware;
import org.apache.commons.jelly.NamespaceAwareTag;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * <p><code>TagScript</code> is a Script that evaluates a custom tag.</p>
 *
 * <b>Note</b> that this class should be re-entrant and used
 * concurrently by multiple threads.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.50 $
 */
public class TagScript implements Script {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(TagScript.class);


    /** The attribute expressions that are created */
    protected Map attributes = new Hashtable();

    /** the optional namespaces Map of prefix -> URI of this single Tag */
    private Map tagNamespacesMap;

    /**
     * The optional namespace context mapping all prefixes -> URIs in scope
     * at the point this tag is used.
     * This Map is only created lazily if it is required by the NamespaceAwareTag.
     */
    private Map namespaceContext;

    /** the Jelly file which caused the problem */
    private String fileName;

    /** the qualified element name which caused the problem */
    private String elementName;

    /** the local (non-namespaced) tag name */
    private String localName;

    /** the line number of the tag */
    private int lineNumber = -1;

    /** the column number of the tag */
    private int columnNumber = -1;

    /** the factory of Tag instances */
    private TagFactory tagFactory;

    /** the body script used for this tag */
    private Script tagBody;

    /** the parent TagScript */
    private TagScript parent;

    /** the SAX attributes */
    private Attributes saxAttributes;
    
    /** the url of the script when parsed */
    private URL scriptURL = null;

    /**
     * @return a new TagScript based on whether
     * the given Tag class is a bean tag or DynaTag
     */
    public static TagScript newInstance(Class tagClass) {
        TagFactory factory = new DefaultTagFactory(tagClass);
        return new TagScript(factory);
    }

    public TagScript() {
    }

    public TagScript(TagFactory tagFactory) {
        this.tagFactory = tagFactory;
    }

    public String toString() {
        return super.toString() + "[tag=" + elementName + ";at=" + lineNumber + ":" + columnNumber + "]";
    }

    /**
     * Compiles the tags body
     */
    public Script compile() throws JellyException {
        if (tagBody != null) {
            tagBody = tagBody.compile();
        }
        return this;
    }

    /**
     * Sets the optional namespaces prefix -> URI map of
     * the namespaces attached to this Tag
     */
    public void setTagNamespacesMap(Map tagNamespacesMap) {
        // lets check that this is a thread-safe map
        if ( ! (tagNamespacesMap instanceof Hashtable) ) {
            tagNamespacesMap = new Hashtable( tagNamespacesMap );
        }
        this.tagNamespacesMap = tagNamespacesMap;
    }

    /**
     * Configures this TagScript from the SAX Locator, setting the column
     * and line numbers
     */
    public void setLocator(Locator locator) {
        setLineNumber( locator.getLineNumber() );
        setColumnNumber( locator.getColumnNumber() );
    }


    /** Add an initialization attribute for the tag.
     * This method must be called after the setTag() method
     */
    public void addAttribute(String name, Expression expression) {
        if (log.isDebugEnabled()) {
            log.debug("adding attribute name: " + name + " expression: " + expression);
        }
        attributes.put(name, expression);
    }

    /**
     * Strips off the name of a script to create a new context URL
     * FIXME: Copied from JellyContext
     */
    private URL getJellyContextURL(URL url) throws MalformedURLException {
        String text = url.toString();
        int idx = text.lastIndexOf('/');
        text = text.substring(0, idx + 1);
        return new URL(text);
    }

    // Script interface
    //-------------------------------------------------------------------------

    /** Evaluates the body of a tag */
    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        URL rootURL = context.getRootURL();
        URL currentURL = context.getCurrentURL();
        try {
            Tag tag = getTag(context);
            if ( tag == null ) {
                return;
            }
            tag.setContext(context);
            setContextURLs(context);

            if ( tag instanceof DynaTag ) {
                DynaTag dynaTag = (DynaTag) tag;

                // ### probably compiling this to 2 arrays might be quicker and smaller
                for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String name = (String) entry.getKey();
                    Expression expression = (Expression) entry.getValue();

                    Class type = dynaTag.getAttributeType(name);
                    Object value = null;
                    if (type != null && type.isAssignableFrom(Expression.class) && !type.isAssignableFrom(Object.class)) {
                        value = expression;
                    }
                    else {
                        value = expression.evaluateRecurse(context);
                    }
                    dynaTag.setAttribute(name, value);
                }
            }
            else {
                // treat the tag as a bean
                DynaBean dynaBean = new ConvertingWrapDynaBean( tag );
                for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String name = (String) entry.getKey();
                    Expression expression = (Expression) entry.getValue();

                    DynaProperty property = dynaBean.getDynaClass().getDynaProperty(name);
                    if (property == null) {
                        throw new JellyException("This tag does not understand the '" + name + "' attribute" );
                    }
                    Class type = property.getType();

                    Object value = null;
                    if (type.isAssignableFrom(Expression.class) && !type.isAssignableFrom(Object.class)) {
                        value = expression;
                    }
                    else {
                        value = expression.evaluateRecurse(context);
                    }
                    dynaBean.set(name, value);
                }
            }

            tag.doTag(output);
            if (output != null) {
                output.flush();
            }
        }
        catch (JellyTagException e) {
            handleException(e);
        } catch (JellyException e) {
            handleException(e);
        } catch (IOException e) {
            handleException(e);
        } catch (RuntimeException e) {
            handleException(e);
        }
        catch (Error e) {
           /*
            * Not sure if we should be converting errors to exceptions,
            * but not trivial to remove because JUnit tags throw
            * Errors in the normal course of operation.  Hmm...
            */
            handleException(e);
        } finally {
            context.setRootURL(rootURL);
            context.setCurrentURL(currentURL);
        }

    }

    /**
     * Set the context's root and current URL if not present
     * @param context
     * @throws JellyTagException
     */
    protected void setContextURLs(JellyContext context) throws JellyTagException {
        if ((context.getCurrentURL() == null || context.getRootURL() == null) && scriptURL != null)
        {
            if (context.getRootURL() == null) context.setRootURL(scriptURL);
            if (context.getCurrentURL() == null) context.setCurrentURL(scriptURL);
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the tag to be evaluated, creating it lazily if required.
     */
    public Tag getTag(JellyContext context) throws JellyException {
        Tag tag = (Tag) context.getThreadScriptData(this);
        if ( tag == null ) {
            tag = createTag();
            if ( tag != null ) {
                context.setThreadScriptData(this,tag);
                configureTag(tag,context);
            }
        }
        return tag;
    }

    /**
     * Returns the Factory of Tag instances.
     * @return the factory
     */
    public TagFactory getTagFactory() {
        return tagFactory;
    }

    /**
     * Sets the Factory of Tag instances.
     * @param tagFactory The factory to set
     */
    public void setTagFactory(TagFactory tagFactory) {
        this.tagFactory = tagFactory;
    }

    /**
     * Returns the parent.
     * @return TagScript
     */
    public TagScript getParent() {
        return parent;
    }

    /**
     * Returns the tagBody.
     * @return Script
     */
    public Script getTagBody() {
        return tagBody;
    }

    /**
     * Sets the parent.
     * @param parent The parent to set
     */
    public void setParent(TagScript parent) {
        this.parent = parent;
    }

    /**
     * Sets the tagBody.
     * @param tagBody The tagBody to set
     */
    public void setTagBody(Script tagBody) {
        this.tagBody = tagBody;
    }

    /**
     * @return the Jelly file which caused the problem
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the Jelly file which caused the problem
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
        try
        {
            this.scriptURL = getJellyContextURL(new URL(fileName));
        } catch (MalformedURLException e) {
            log.debug("error setting script url", e);
        }
    }


    /**
     * @return the element name which caused the problem
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * Sets the element name which caused the problem
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
    /**
     * @return the line number of the tag
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Sets the line number of the tag
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * @return the column number of the tag
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Sets the column number of the tag
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * Returns the SAX attributes of this tag
     * @return Attributes
     */
    public Attributes getSaxAttributes() {
        return saxAttributes;
    }

    /**
     * Sets the SAX attributes of this tag
     * @param saxAttributes The saxAttributes to set
     */
    public void setSaxAttributes(Attributes saxAttributes) {
        this.saxAttributes = saxAttributes;
    }

    /**
     * Returns the local, non namespaced XML name of this tag
     * @return String
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Sets the local, non namespaced name of this tag.
     * @param localName The localName to set
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }


    /**
     * Returns the namespace context of this tag. This is all the prefixes
     * in scope in the document where this tag is used which are mapped to
     * their namespace URIs.
     *
     * @return a Map with the keys are namespace prefixes and the values are
     * namespace URIs.
     */
    public synchronized Map getNamespaceContext() {
        if (namespaceContext == null) {
            if (parent != null) {
                namespaceContext = getParent().getNamespaceContext();
                if (tagNamespacesMap != null && !tagNamespacesMap.isEmpty()) {
                    // create a new child context
                    Hashtable newContext = new Hashtable(namespaceContext.size()+1);
                    newContext.putAll(namespaceContext);
                    newContext.putAll(tagNamespacesMap);
                    namespaceContext = newContext;
                }
            }
            else {
                namespaceContext = tagNamespacesMap;
                if (namespaceContext == null) {
                    namespaceContext = new Hashtable();
                }
            }
        }
        return namespaceContext;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new Tag instance.
     * The default implementation is to delegate to the TagFactory
     */
    protected Tag createTag() throws JellyException {
        if ( tagFactory != null) {
            return tagFactory.createTag(localName, getSaxAttributes());
        }
        return null;
    }

	
    /**
     * Compiles a newly created tag if required, sets its parent and body.
     */
    protected void configureTag(Tag tag, JellyContext context) throws JellyException {
        if (tag instanceof CompilableTag) {
            ((CompilableTag) tag).compile();
        }
        Tag parentTag = null;
        if ( parent != null ) {
            parentTag = parent.getTag(context);
        }
        tag.setParent( parentTag );
        tag.setBody( tagBody );

        if (tag instanceof NamespaceAwareTag) {
            NamespaceAwareTag naTag = (NamespaceAwareTag) tag;
            naTag.setNamespaceContext(getNamespaceContext());
        }
        if (tag instanceof LocationAware) {
            applyLocation((LocationAware) tag);
        }
    }


    /**
     * Allows the script to set the tag instance to be used, such as in a StaticTagScript
     * when a StaticTag is switched with a DynamicTag
     */
    protected void setTag(Tag tag, JellyContext context) {
        context.setThreadScriptData(this,tag);
    }

    /**
     * Output the new namespace prefixes used for this element
     */
    protected void startNamespacePrefixes(XMLOutput output) throws SAXException {
        if ( tagNamespacesMap != null ) {
            for ( Iterator iter = tagNamespacesMap.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iter.next();
                String prefix = (String) entry.getKey();
                String uri = (String) entry.getValue();
                output.startPrefixMapping(prefix, uri);
            }
        }
    }

    /**
     * End the new namespace prefixes mapped for the current element
     */
    protected void endNamespacePrefixes(XMLOutput output) throws SAXException {
        if ( tagNamespacesMap != null ) {
            for ( Iterator iter = tagNamespacesMap.keySet().iterator(); iter.hasNext(); ) {
                String prefix = (String) iter.next();
                output.endPrefixMapping(prefix);
            }
        }
    }

    /**
     * Converts the given value to the required type.
     *
     * @param value is the value to be converted. This will not be null
     * @param requiredType the type that the value should be converted to
     */
    protected Object convertType(Object value, Class requiredType)
        throws JellyException {
        if (requiredType.isInstance(value)) {
            return value;
        }
        if (value instanceof String) {
            return ConvertUtils.convert((String) value, requiredType);
        }
        return value;
    }

    /**
     * Creates a new Jelly exception, adorning it with location information
     */
    protected JellyException createJellyException(String reason) {
        return new JellyException(
            reason, fileName, elementName, columnNumber, lineNumber
        );
    }

    /**
     * Creates a new Jelly exception, adorning it with location information
     */
    protected JellyException createJellyException(String reason, Exception cause) {
        if (cause instanceof JellyException) {
            return (JellyException) cause;
        }

        if (cause instanceof InvocationTargetException) {
            return new JellyException(
                reason,
                ((InvocationTargetException) cause).getTargetException(),
                fileName,
                elementName,
                columnNumber,
                lineNumber);
        }
        return new JellyException(
            reason, cause, fileName, elementName, columnNumber, lineNumber
        );
    }

    /**
     * A helper method to handle this Jelly exception.
     * This method adorns the JellyException with location information
     * such as adding line number information etc.
     */
    protected void handleException(JellyTagException e) throws JellyTagException {
        if (log.isTraceEnabled()) {
            log.trace( "Caught exception: " + e, e );
        }

        applyLocation(e);

        throw e;
    }

    /**
     * A helper method to handle this Jelly exception.
     * This method adorns the JellyException with location information
     * such as adding line number information etc.
     */
    protected void handleException(JellyException e) throws JellyTagException {
        if (log.isTraceEnabled()) {
            log.trace( "Caught exception: " + e, e );
        }

        applyLocation(e);

        throw new JellyTagException(e);
    }

    protected void applyLocation(LocationAware locationAware) {
        if (locationAware.getLineNumber() == -1) {
            locationAware.setColumnNumber(columnNumber);
            locationAware.setLineNumber(lineNumber);
        }
        if ( locationAware.getFileName() == null ) {
            locationAware.setFileName( fileName );
        }
        if ( locationAware.getElementName() == null ) {
            locationAware.setElementName( elementName );
        }
    }

    /**
     * A helper method to handle this non-Jelly exception.
     * This method will rethrow the exception, wrapped in a JellyException
     * while adding line number information etc.
     */
    protected void handleException(Exception e) throws JellyTagException {
        if (log.isTraceEnabled()) {
            log.trace( "Caught exception: " + e, e );
        }

        if (e instanceof LocationAware) {
            applyLocation((LocationAware) e);
        }

        if ( e instanceof JellyException ) {
            e.fillInStackTrace();
        }

        if ( e instanceof InvocationTargetException) {
            throw new JellyTagException( ((InvocationTargetException)e).getTargetException(),
                                      fileName,
                                      elementName,
                                      columnNumber,
                                      lineNumber );
        }

        throw new JellyTagException(e, fileName, elementName, columnNumber, lineNumber);
    }

    /**
     * A helper method to handle this non-Jelly exception.
     * This method will rethrow the exception, wrapped in a JellyException
     * while adding line number information etc.
     *
     * Is this method wise?
     */
    protected void handleException(Error e) throws Error, JellyTagException {
        if (log.isTraceEnabled()) {
            log.trace( "Caught exception: " + e, e );
        }

        if (e instanceof LocationAware) {
            applyLocation((LocationAware) e);
        }

        throw new JellyTagException(e, fileName, elementName, columnNumber, lineNumber);
    }
}
