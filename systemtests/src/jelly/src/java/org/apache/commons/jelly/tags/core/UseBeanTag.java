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
package org.apache.commons.jelly.tags.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MapTagSupport;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.BeanSource;
import org.apache.commons.jelly.util.ClassLoaderUtils;

/**
 * A tag which instantiates an instance of the given class
 * and then sets the properties on the bean.
 * The class can be specified via a {@link java.lang.Class} instance or
 * a String which will be used to load the class using either the current
 * thread's context class loader or the class loader used to load this
 * Jelly library.
 *
 * This tag can be used it as follows,
 * <pre>
 * &lt;j:useBean var="person" class="com.acme.Person" name="James" location="${loc}"/&gt;
 * &lt;j:useBean var="order" class="${orderClass}" amount="12" price="123.456"/&gt;
 * </pre>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.3 $
 */
public class UseBeanTag extends MapTagSupport implements BeanSource {

    /** the current bean instance */
    private Object bean;

    /** the default class to use if no Class is specified */
    private Class defaultClass;

    /**
     * a Set of Strings of property names to ignore (remove from the
     * Map of attributes before passing to ConvertUtils)
     */
    private Set ignoreProperties;

    /**
     * If this tag finds an attribute in the XML that's not
     * ignored by {@link #ignoreProperties} and isn't a
     * bean property, should it throw an exception?
     * @see #setIgnoreUnknownProperties(boolean)
     */
    private boolean ignoreUnknownProperties = false;


    public UseBeanTag() {
    }

    public UseBeanTag(Class defaultClass) {
        this.defaultClass = defaultClass;
    }

    // BeanSource interface
    //-------------------------------------------------------------------------

    /**
     * @return the bean that has just been created
     */
    public Object getBean() {
        return bean;
    }


    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {
        Map attributes = getAttributes();
        String var = (String) attributes.get( "var" );
        Object classObject = attributes.get( "class" );
        addIgnoreProperty("class");
        addIgnoreProperty("var");

        try {
            // this method could return null in derived classes
            Class theClass = convertToClass(classObject);

            bean = newInstance(theClass, attributes, output);
            setBeanProperties(bean, attributes);

            // invoke body which could result in other properties being set
            invokeBody(output);

            processBean(var, bean);
        }
        catch (ClassNotFoundException e) {
            throw new JellyTagException(e);
        }
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Allow derived classes to programatically set the bean
     */
    protected void setBean(Object bean) {
        this.bean = bean;
    }

    /**
     * Attempts to convert the given object to a Class instance.
     * If the classObject is already a Class it will be returned
     * otherwise it will be converted to a String and loaded
     * using the default class loading mechanism.
     */
    protected Class convertToClass(Object classObject)
    throws MissingAttributeException, ClassNotFoundException {
        if (classObject instanceof Class) {
            return (Class) classObject;
        }
        else if ( classObject == null ) {
            Class theClass = getDefaultClass();
            if (theClass == null) {
                throw new MissingAttributeException("class");
            }
            return theClass;
        }
        else {
            String className = classObject.toString();
            return loadClass(className);
        }
    }

    /**
     * Loads the given class using the default class loading mechanism
     * which is to try use the current Thread's context class loader first
     * otherise use the class loader which loaded this class.
     */
    protected Class loadClass(String className) throws ClassNotFoundException {
        return ClassLoaderUtils.loadClass(className, getClass());
    }

    /**
     * Creates a new instance of the given class, which by default will invoke the
     * default constructor.
     * Derived tags could do something different here.
     */
    protected Object newInstance(Class theClass, Map attributes, XMLOutput output)
    throws JellyTagException {
        try {
            return theClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new JellyTagException(e.toString());
        } catch (InstantiationException e) {
            throw new JellyTagException(e.toString());
        }
    }

    /**
     * Sets the properties on the bean. Derived tags could implement some custom
     * type conversion etc.
     * <p/>
     * This method ignores all property names in the Set returned by {@link #getIgnorePropertySet()}.
     */
    protected void setBeanProperties(Object bean, Map attributes) throws JellyTagException {
        Map attrsToUse = new HashMap(attributes);
        attrsToUse.keySet().removeAll(getIgnorePropertySet());

        validateBeanProperties(bean, attrsToUse);

        try {
            BeanUtils.populate(bean, attrsToUse);
        } catch (IllegalAccessException e) {
            throw new JellyTagException("could not set the properties of the bean",e);
        } catch (InvocationTargetException e) {
            throw new JellyTagException("could not set the properties of the bean",e);
        }
    }

    /**
     * If {@link #isIgnoreUnknownProperties()} returns true, make sure that
     * every non-ignored ({@see #addIgnoreProperty(String)}) property
     * matches a writable property on the target bean.
     * @param bean the bean to validate
     * @param attributes the list of properties to validate
     * @throws JellyTagException when a property is not writeable
     */
    protected void validateBeanProperties(Object bean, Map attributes) throws JellyTagException {
        if (!isIgnoreUnknownProperties()) {
            for (Iterator i=attributes.keySet().iterator();i.hasNext();) {
                String attrName = (String)i.next();
                if (! PropertyUtils.isWriteable(bean, attrName)) {
                    throw new JellyTagException("No bean property found: " + attrName);
                }
            }
        }
    }

    /**
     * By default this will export the bean using the given variable if it is defined.
     * This Strategy method allows derived tags to process the beans in different ways
     * such as to register this bean with its parent tag etc.
     */
    protected void processBean(String var, Object bean) throws JellyTagException {
        if (var != null) {
            context.setVariable(var, bean);
        }
        else {
            ArgTag parentArg = (ArgTag)(findAncestorWithClass(ArgTag.class));
            if(null != parentArg) {
                parentArg.setValue(bean);
            }
        }
    }

    /**
     * Allows derived classes to provide a default bean implementation class
     */
    protected Class getDefaultClass() {
        return defaultClass;
    }

    /**
     * Adds a name to the Set of property names that will be skipped when setting
     * bean properties. In other words, names added here won't be set into the bean
     * if they're present in the attribute Map.
     * @param name
     */
    protected void addIgnoreProperty(String name) {
        getIgnorePropertySet().add(name);
    }

    /**
     * @return the Set of property names that should be ignored when setting the
     * properties of the bean.
     */
    protected Set getIgnorePropertySet() {
        if (ignoreProperties == null) {
            ignoreProperties = new HashSet();
        }

        return ignoreProperties;
    }

    /**
     * @see {@link #setIgnoreUnknownProperties(boolean)}
     * @return
     */
    public boolean isIgnoreUnknownProperties() {
        return ignoreUnknownProperties;
    }

    /**
     * If this tag finds an attribute in the XML that's not
     * ignored by {@link #ignoreProperties} and isn't a
     * bean property, should it throw an exception?
     * @param ignoreUnknownProperties Sets {@link #ignoreUnknownProperties}.
     */
    public void setIgnoreUnknownProperties(boolean ignoreUnknownProps) {
        this.ignoreUnknownProperties = ignoreUnknownProps;
    }
}
