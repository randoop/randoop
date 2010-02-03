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
package org.apache.commons.chain.web;


import java.net.URL;
import javax.servlet.ServletContext;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>Utility methods for loading class loader and web application resources
 * to configure a {@link Catalog}.  These methods are shared between
 * <code>ChainListener</code> and <code>ChainServlet</code>.</p>
 *
 * @author Craig R. McClanahan
 * @author Ted Husted
 */

final class ChainResources {


    // -------------------------------------------------------- Static Variables


    /**
     * <p>The <code>Log</code> instance for this class.</p>
     */
    private static final Log log = LogFactory.getLog(ChainResources.class);


    // ---------------------------------------------------------- Static Methods


    /**
     * <p>Parse the specified class loader resources.</p>
     *
     * @param resources Comma-delimited list of resources (or <code>null</code>)
     * @param parser {@link ConfigParser} to use for parsing
     */
    static void parseClassResources(String resources,
                                    ConfigParser parser) {

        if (resources == null) {
            return;
        }
        ClassLoader loader =
            Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ChainResources.class.getClassLoader();
        }
        String path = null;
        try {
            while (true) {
                int comma = resources.indexOf(",");
                if (comma < 0) {
                    path = resources.trim();
                    resources = "";
                } else {
                    path = resources.substring(0, comma);
                    resources = resources.substring(comma + 1);
                }
                if (path.length() < 1) {
                    break;
                }
                URL url = loader.getResource(path);
                if (url == null) {
                    throw new IllegalStateException
                        ("Missing chain config resource '" + path + "'");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Loading chain config resource '" + path + "'");
                }
                parser.parse(url);
            }
        } catch (Exception e) {
            throw new RuntimeException
                ("Exception parsing chain config resource '" + path + "': "
                 + e.getMessage());
        }

    }


    /**
     * <p>Parse the specified class loader resources.</p>
     *
     * @param catalog {@link Catalog} we are populating
     * @param resources Comma-delimited list of resources (or <code>null</code>)
     * @param parser {@link ConfigParser} to use for parsing
     *
     * @deprecated Use the variant that does not take a catalog, on a
     *  configuration resource containing "catalog" element(s)
     */
    static void parseClassResources(Catalog catalog, String resources,
                                    ConfigParser parser) {

        if (resources == null) {
            return;
        }
        ClassLoader loader =
            Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ChainResources.class.getClassLoader();
        }
        String path = null;
        try {
            while (true) {
                int comma = resources.indexOf(",");
                if (comma < 0) {
                    path = resources.trim();
                    resources = "";
                } else {
                    path = resources.substring(0, comma);
                    resources = resources.substring(comma + 1);
                }
                if (path.length() < 1) {
                    break;
                }
                URL url = loader.getResource(path);
                if (url == null) {
                    throw new IllegalStateException
                        ("Missing chain config resource '" + path + "'");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Loading chain config resource '" + path + "'");
                }
                parser.parse(catalog, url);
            }
        } catch (Exception e) {
            throw new RuntimeException
                ("Exception parsing chain config resource '" + path + "': "
                 + e.getMessage());
        }

    }


    /**
     * <p>Parse the specified web application resources.</p>
     *
     * @param context <code>ServletContext</code> for this web application
     * @param resources Comma-delimited list of resources (or <code>null</code>)
     * @param parser {@link ConfigParser} to use for parsing
     */
    static void parseWebResources(ServletContext context,
                                  String resources,
                                  ConfigParser parser) {

        if (resources == null) {
            return;
        }
        String path = null;
        try {
            while (true) {
                int comma = resources.indexOf(",");
                if (comma < 0) {
                    path = resources.trim();
                    resources = "";
                } else {
                    path = resources.substring(0, comma);
                    resources = resources.substring(comma + 1);
                }
                if (path.length() < 1) {
                    break;
                }
                URL url = context.getResource(path);
                if (url == null) {
                    throw new IllegalStateException
                        ("Missing chain config resource '" + path + "'");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Loading chain config resource '" + path + "'");
                }
                parser.parse(url);
            }
        } catch (Exception e) {
            throw new RuntimeException
                ("Exception parsing chain config resource '" + path + "': "
                 + e.getMessage());
        }

    }


    /**
     * <p>Parse the specified web application resources.</p>
     *
     * @param catalog {@link Catalog} we are populating
     * @param context <code>ServletContext</code> for this web application
     * @param resources Comma-delimited list of resources (or <code>null</code>)
     * @param parser {@link ConfigParser} to use for parsing
     *
     * @deprecated Use the variant that does not take a catalog, on a
     *  configuration resource containing "catalog" element(s)
     */
    static void parseWebResources(Catalog catalog, ServletContext context,
                                  String resources,
                                  ConfigParser parser) {

        if (resources == null) {
            return;
        }
        String path = null;
        try {
            while (true) {
                int comma = resources.indexOf(",");
                if (comma < 0) {
                    path = resources.trim();
                    resources = "";
                } else {
                    path = resources.substring(0, comma);
                    resources = resources.substring(comma + 1);
                }
                if (path.length() < 1) {
                    break;
                }
                URL url = context.getResource(path);
                if (url == null) {
                    throw new IllegalStateException
                        ("Missing chain config resource '" + path + "'");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Loading chain config resource '" + path + "'");
                }
                parser.parse(catalog, url);
            }
        } catch (Exception e) {
            throw new RuntimeException
                ("Exception parsing chain config resource '" + path + "': "
                 + e.getMessage());
        }

    }


}
