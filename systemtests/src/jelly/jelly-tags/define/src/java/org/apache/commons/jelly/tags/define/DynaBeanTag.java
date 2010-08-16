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

package org.apache.commons.jelly.tags.define;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.Attribute;
import org.apache.commons.jelly.impl.DynamicDynaBeanTag;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;

/**
 * Binds a Java bean to the given named Jelly tag so that the attributes of
 * the tag set the bean properties..
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class DynaBeanTag extends DefineTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(DynaBeanTag.class);

    /** An empty Map as I think Collections.EMPTY_MAP is only JDK 1.3 onwards */
    private static final Map EMPTY_MAP = new HashMap();

    /** the name of the tag to create */
    private String name;

    /** the DyanClass to bind to the tag */
    private DynaClass dynaClass;

    /** the name of the attribute used for the variable name */
    private String varAttribute = "var";

    /** the attribute definitions for this dynamic tag */
    private Map attributes;

    /**
     * Adds a new attribute definition to this dynamic tag
     */
    public void addAttribute(Attribute attribute) {
        if ( attributes == null ) {
            attributes = new HashMap();
        }
        attributes.put( attribute.getName(), attribute );
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        invokeBody(output);

        if (name == null) {
            throw new MissingAttributeException("name");
        }
        if (dynaClass == null) {
            throw new MissingAttributeException("dynaClass");
        }

        final DynaClass theDynaClass = dynaClass;
        final Map beanAttributes = (attributes != null) ? attributes : EMPTY_MAP;

        TagFactory factory = new TagFactory() {
            public Tag createTag(String name, Attributes attributes) {
                return  new DynamicDynaBeanTag(theDynaClass, beanAttributes, varAttribute);
            }
        };
        getTagLibrary().registerBeanTag(name, factory);

        // now lets clear the attributes for next invocation and help the GC
        attributes = null;
    }


    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the name of the tag to create
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the name of the attribute used to define the bean variable that this dynamic
     * tag will output its results as. This defaults to 'var' though this property
     * can be used to change this if it conflicts with a bean property called 'var'.
     */
    public void setVarAttribute(String varAttribute) {
        this.varAttribute = varAttribute;
    }

    /**
     * Returns the dynaClass.
     * @return DynaClass
     */
    public DynaClass getDynaClass() {
        return dynaClass;
    }

    /**
     * Sets the {@link DynaClass} which will be bound to this dynamic tag.
     */
    public void setDynaClass(DynaClass dynaClass) {
        this.dynaClass = dynaClass;
    }

}
