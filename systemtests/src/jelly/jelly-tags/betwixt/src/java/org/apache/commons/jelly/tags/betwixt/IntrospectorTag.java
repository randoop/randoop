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
package org.apache.commons.jelly.tags.betwixt;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.strategy.CapitalizeNameMapper;
import org.apache.commons.betwixt.strategy.DecapitalizeNameMapper;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;
import org.apache.commons.betwixt.strategy.NameMapper;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a Betwixt XMLIntrospector instance that can be used by the other Betwixt tags.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class IntrospectorTag extends TagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(IntrospectorTag.class);

    private XMLIntrospector introspector;
    private String var;

    static {

        // register converters to standard Strategies
        ConvertUtils.register(
            new Converter() {
                public Object convert(Class type, Object value) {
                    if ( value instanceof String ) {
                        return createNameMapper((String) value);
                    }
                    else if ( value == null ) {
                        return null;
                    }
                    else {
                        throw new ConversionException(
                            "Don't know how to convert: " + value
                            + " of type: " + value.getClass().getName()
                            + " into a NameMapper"
                        );
                    }
                }
            },
            NameMapper.class
        );
    }



    public IntrospectorTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws MissingAttributeException, JellyTagException {

        if ( var == null ) {
            throw new MissingAttributeException( "var" );
        }
        invokeBody(output);

        XMLIntrospector introspector = getIntrospector();

        context.setVariable( var, introspector );

        // now lets clear this introspector so that its recreated again next time
        this.introspector = null;
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the current XMLIntrospector, lazily creating one if required
     */
    public XMLIntrospector getIntrospector() {
        if ( introspector == null ) {
            introspector = createIntrospector();
        }
        return introspector;
    }

    /**
     * Sets whether attributes or elements should be used for primitive types.
     * The default is false.
     */
    public void setAttributesForPrimitives(boolean attributesForPrimitives) {
        getIntrospector().setAttributesForPrimitives(attributesForPrimitives);
    }

    /**
     * Sets the name mapper used for element names.
     * You can also use the Strings 'lowercase', 'uppercase' or 'hyphenated'
     * as aliases to the common name mapping strategies or specify a class name String.
     */
    public void setElementNameMapper(NameMapper nameMapper) {
        getIntrospector().setElementNameMapper(nameMapper);
    }

    /**
     * Sets the name mapper used for attribute names.
     * You can also use the Strings 'lowercase', 'uppercase' or 'hyphenated'
     * as aliases to the common name mapping strategies or specify a class name String.
     */
    public void setAttributeNameMapper(NameMapper nameMapper) {
        getIntrospector().setAttributeNameMapper(nameMapper);
    }


    /**
     * Sets the variable name to output the new XMLIntrospector to.
     * If this attribute is not specified then this tag must be nested
     * inside an &lt;parse&gt; or &lt;output&gt; tag
     */
    public void setVar(String var) {
        this.var = var;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Static helper method which will convert the given string into
     * standard named strategies such as 'lowercase', 'uppercase' or 'hyphenated'
     * or use the name as a class name and create a new instance.
     */
    protected static NameMapper createNameMapper(String name) {
        if ( name.equalsIgnoreCase( "lowercase" ) ) {
            return new DecapitalizeNameMapper();
        }
        else if ( name.equalsIgnoreCase( "uppercase" ) ) {
            return new CapitalizeNameMapper();
        }
        else if ( name.equalsIgnoreCase( "hyphenated" ) ) {
            return new HyphenatedNameMapper();
        }
        else {
            // lets try load the class of this name
            Class theClass = null;
            try {
                theClass = Thread.currentThread().getContextClassLoader().loadClass( name );
            }
            catch (Exception e) {
                throw new ConversionException( "Could not load class called: " + name, e );
            }

            Object object = null;
            try {
                object = theClass.newInstance();
            }
            catch (Exception e) {
                throw new ConversionException( "Could not instantiate an instance of: " + name, e );
            }
            if ( object instanceof NameMapper ) {
                return (NameMapper) object;
            }
            if ( object == null ) {
                throw new ConversionException( "No NameMapper created for type: " + name );
            }
            else {
                throw new ConversionException(
                    "Created object: " + object
                    + " is not a NameMapper! Its type is: " + object.getClass().getName()
                );
            }
        }
    }

    /**
     * Factory method to create a new XMLIntrospector
     */
    protected XMLIntrospector createIntrospector() {
        return new XMLIntrospector();
    }
}
