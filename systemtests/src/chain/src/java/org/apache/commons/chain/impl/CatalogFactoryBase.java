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

package org.apache.commons.chain.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;

/**
 * <p>A simple implementation of {@link CatalogFactory}.</p>
 *
 * @author Sean Schofield
 * @version $Revision: 1.4 $ $Date: 2004/11/30 05:52:23 $
 */

public class CatalogFactoryBase extends CatalogFactory {


    // ----------------------------------------------------------- Constructors


    /**
     * <p>Construct an empty instance of {@link CatalogFactoryBase}.  This
     * constructor is intended solely for use by {@link CatalogFactory}.</p>
     */
    public CatalogFactoryBase() { }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The default {@link Catalog} for this {@link CatalogFactory).</p>
     */
    private Catalog catalog = null;


    /**
     * <p>Map of named {@link Catalog}s, keyed by catalog name.</p>
     */
    private Map catalogs = new HashMap();


    // --------------------------------------------------------- Public Methods


    // Documented in CatalogFactory interface
    public Catalog getCatalog() {

        return catalog;

    }


    // Documented in CatalogFactory interface
    public void setCatalog(Catalog catalog) {

        this.catalog = catalog;

    }


    // Documented in CatalogFactory interface
    public Catalog getCatalog(String name) {

        synchronized (catalogs) {
            return (Catalog) catalogs.get(name);
        }

    }


    // Documented in CatalogFactory interface
    public void addCatalog(String name, Catalog catalog) {

        synchronized (catalogs) {
            catalogs.put(name, catalog);
        }

    }


    // Documented in CatalogFactory interface
    public Iterator getNames() {

        synchronized (catalogs) {
            return catalogs.keySet().iterator();
        }

    }


}
