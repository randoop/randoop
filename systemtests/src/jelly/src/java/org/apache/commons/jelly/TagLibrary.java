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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import org.apache.commons.jelly.expression.CompositeExpression;
import org.apache.commons.jelly.expression.ConstantExpression;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.jelly.impl.TagScript;

import org.xml.sax.Attributes;

/** <p><code>Taglib</code> represents the metadata for a Jelly custom tag library.</p>
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.25 $
  */

public abstract class TagLibrary {

    private Map tags = new HashMap();

    static {

        // register standard converters

        ConvertUtils.register(
            new Converter() {
                public Object convert(Class type, Object value) {
                    if ( value instanceof File ) {
                        return (File) value;
                    }
                    else if ( value != null ) {
                        String text = value.toString();
                        return new File( text );
                    }
                    return null;
                }
            },
            File.class
        );
    }

    public TagLibrary() {
    }

    /** Creates a new script to execute the given tag name and attributes */
    public TagScript createTagScript(String name, Attributes attributes)
        throws JellyException {

        Object value = tags.get(name);
        if (value instanceof Class) {
            Class type = (Class) value;
            return TagScript.newInstance(type);
        }
        else if (value instanceof TagFactory) {
            return new TagScript( (TagFactory) value );
        }
        return null;

    }

    /** Creates a new Tag for the given tag name and attributes */
    public Tag createTag(String name, Attributes attributes)
        throws JellyException {

        Object value = tags.get(name);
        if (value instanceof Class) {
            Class type = (Class) value;
            try {
                return (Tag) type.newInstance();
            } catch (InstantiationException e) {
                throw new JellyException(e.toString());
            } catch (IllegalAccessException e) {
                throw new JellyException(e.toString());
            }
        }
        else if (value instanceof TagFactory) {
            TagFactory factory = (TagFactory) value;
            return factory.createTag(name, attributes);
        }
        return null;
    }

    /** Allows taglibs to use their own expression evaluation mechanism */
    public Expression createExpression(
        ExpressionFactory factory,
        TagScript tagScript,
        String attributeName,
        String attributeValue)
        throws JellyException {

        ExpressionFactory myFactory = getExpressionFactory();
        if (myFactory == null) {
            myFactory = factory;
        }
        if (myFactory != null) {
            return CompositeExpression.parse(attributeValue, myFactory);
        }

        // will use a constant expression instead
        return new ConstantExpression(attributeValue);
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Registers a tag implementation Class for a given tag name
     */
    protected void registerTag(String name, Class type) {
        tags.put(name, type);
    }

    /**
     * Registers a tag factory for a given tag name
     */
    protected void registerTagFactory(String name, TagFactory tagFactory) {
        tags.put(name, tagFactory);
    }

    /** Allows derived tag libraries to use their own factory */
    protected ExpressionFactory getExpressionFactory() {
        return null;
    }

    protected Map getTagClasses() {
        return tags;
    }

}
