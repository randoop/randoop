/*
 * Copyright 1999-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.chain.config;


import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;


/**
 * <p>Digester rule that will cause the top-most element on the Digester
 * stack (if it is a {@link Catalog} to be registered with the
 * {@link CatalogFactory} instance for our application.  If the attribute
 * specified to our constructor has a value, that will be used as the name
 * under which to register this {@link Catalog}.  Otherwise, this will
 * become the default {@link Catalog} for this application.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2004/11/30 05:52:23 $
 */
class ConfigCatalogRule extends Rule {


    // ----------------------------------------------------------- Constructors


    /**
     * <p>Construct a new instance of this rule that looks for an attribute
     * with the specified name.</p>
     *
     * @param nameAttribute Name of the attribute containing the name under
     *  which this command should be registered
     * @param catalogClass Name of the implementation class for newly
     *  created {@link Catalog} instances
     */
    public ConfigCatalogRule(String nameAttribute, String catalogClass) {
        super();
        this.nameAttribute = nameAttribute;
        this.catalogClass = catalogClass;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The fully qualified class name of a {@link Catalog} class to use for
     * instantiating new instances.</p>
     */
    private String catalogClass = null;


    /**
     * <p>The name of the attribute under which we can retrieve the name
     * this catalog should be registered with (if any).</p>
     */
    private String nameAttribute = null;


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Retrieve or create a {@link Catalog} with the name specified by
     * the <code>nameAttribute</code> attribute, or the default {@link Catalog}
     * if there is no such attribute defined.  Push it onto the top of the
     * stack.</p>
     *
     * @param namespace the namespace URI of the matching element, or an
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just
     *   the element name otherwise
     * @param attributes The attribute list of this element
     */
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {

        // Retrieve any current Catalog with the specified name
        Catalog catalog = null;
        CatalogFactory factory = CatalogFactory.getInstance();
        String nameValue = attributes.getValue(nameAttribute);
        if (nameValue == null) {
            catalog = factory.getCatalog();
        } else {
            catalog = factory.getCatalog(nameValue);
        }

        // Create and register a new Catalog instance if necessary
        if (catalog == null) {
            Class clazz = digester.getClassLoader().loadClass(catalogClass);
            catalog = (Catalog) clazz.newInstance();
            if (nameValue == null) {
                factory.setCatalog(catalog);
            } else {
                factory.addCatalog(nameValue, catalog);
            }
        }

        // Push this Catalog onto the top of the stack
        digester.push(catalog);

    }


}
