/*
 * Copyright 2003-2004 The Apache Software Foundation
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


import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.chain.impl.CatalogBase;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p><code>Servlet</code> that automatically scans chain configuration files
 * in the current web application at startup time, and exposes the result in a
 * {@link Catalog} under a specified servlet context attribute.  The following
 * <em>servlet</em> init parameters are utilized:</p>
 * <ul>
 * <li><strong>org.apache.commons.chain.CONFIG_CLASS_RESOURCE</strong> -
 *     comma-delimited list of chain configuration resources to be loaded
 *     via <code>ClassLoader.getResource()</code> calls.  If not specified,
 *     no class loader resources will be loaded.</li>
 * <li><strong>org.apache.commons.chain.CONFIG_WEB_RESOURCE</strong> -
 *     comma-delimited list of chain configuration webapp resources
 *     to be loaded.  If not specified, no web application resources
 *     will be loaded.</li>
 * <li><strong>org.apache.commons.chain.CONFIG_ATTR</strong> -
 *     Name of the servlet context attribute under which the
 *     resulting {@link Catalog} will be created or updated.
 *     If not specified, it is expected that parsed resources will
 *     contain <code>&lt;catalog&gt;</code> elements (which will
 *     cause registration of the created {@link Catalog}s into
 *     the {@link CatalogFactory} for this application, and no
 *     servet context attribute will be created.
 *     <strong>NOTE</strong> - This parameter is deprecated.</p>
 * <li><strong>org.apache.commons.chain.RULE_SET</strong> -
 *     Fully qualified class name of a Digester <code>RuleSet</code>
 *     implementation to use for parsing configuration resources (this
 *     class must have a public zero-args constructor).  If not defined,
 *     the standard <code>RuleSet</code> implementation will be used.</li>
 * </ul>
 *
 * <p>When a web application that has configured this servlet is
 * started, it will acquire the {@link Catalog} under the specified servlet
 * context attribute key, creating a new one if there is none already there.
 * This {@link Catalog} will then be populated by scanning configuration
 * resources from the following sources (loaded in this order):</p>
 * <ul>
 * <li>Resources loaded from specified resource paths from the
 *     webapp's class loader (via <code>ClassLoader.getResource()</code>).</li>
 * <li>Resources loaded from specified resource paths in the web application
 *     archive (via <code>ServetContext.getResource()</code>).</li>
 * </ul>
 *
 * <p>If no attribute key is specified, on the other hand, parsed configuration
 * resources are expected to contain <code>&lt;catalog&gt;</code> elements,
 * and the catalogs will be registered with the {@link CatalogFactory}
 * for this web application.</p>
 *
 * <p>This class runs on Servlet 2.2 or later.  If you are running on a
 * Servlet 2.3 or later system, you should also consider using
 * {@link ChainListener} to initialize your {@link Catalog}.  Note that
 * {@link ChainListener} uses parameters of the same names, but they are
 * <em>context</em> init parameters instead of <em>servlet</em> init
 * parameters. Because of this, you can use both facilities in the
 * same application, if desired.</p>
 *
 * @author Matthew J. Sgarlata
 * @author Craig R. McClanahan
 * @author Ted Husted
 */

public class ChainServlet extends HttpServlet {


    // ------------------------------------------------------ Manifest Constants


    /**
     * <p>The name of the context init parameter containing the name of the
     * servlet context attribute under which our resulting {@link Catalog}
     * will be stored.</p>
     */
    public static final String CONFIG_ATTR =
        "org.apache.commons.chain.CONFIG_ATTR";


    /**
     * <p>The name of the context init parameter containing a comma-delimited
     * list of class loader resources to be scanned.</p>
     */
    public static final String CONFIG_CLASS_RESOURCE =
        "org.apache.commons.chain.CONFIG_CLASS_RESOURCE";


    /**
     * <p>The name of the context init parameter containing a comma-delimited
     * list of web applicaton resources to be scanned.</p>
     */
    public static final String CONFIG_WEB_RESOURCE =
        "org.apache.commons.chain.CONFIG_WEB_RESOURCE";


    /**
     * <p>The name of the context init parameter containing the fully
     * qualified class name of the <code>RuleSet</code> implementation
     * for configuring our {@link ConfigParser}.</p>
     */
    public static final String RULE_SET =
        "org.apache.commons.chain.RULE_SET";


    // -------------------------------------------------------- Static Variables


    /**
     * <p>The <code>Log</code> instance to use with this class.</p>
     */
    protected static final Log log = LogFactory.getLog(ChainServlet.class);


    // --------------------------------------------------------- Servlet Methods


    /**
     * <p>Clean up after ourselves as this application shuts down.</p>
     */
    public void destroy() {

        ServletConfig config = getServletConfig();
        ServletContext context = getServletContext();
        String attr = config.getInitParameter(CONFIG_ATTR);
        if (attr != null) {
            context.removeAttribute(attr);
        }
        CatalogFactory.clear();

    }


    /**
     * <p>Create (if necessary) and configure a {@link Catalog} from the
     * servlet init parameters that have been specified.</p>
     *
     * @throws ServletException if the servlet could not be initialized
     */
    public void init() throws ServletException {

        ServletConfig config = getServletConfig();
        ServletContext context = getServletContext();
        if (log.isInfoEnabled()) {
            log.info("Initializing chain servlet '"
                     + config.getServletName() + "'");
        }

        // Retrieve servlet init parameters that we need
        String attr = config.getInitParameter(CONFIG_ATTR);
        String classResources =
            context.getInitParameter(CONFIG_CLASS_RESOURCE);
        String ruleSet = context.getInitParameter(RULE_SET);
        String webResources = context.getInitParameter(CONFIG_WEB_RESOURCE);

        // Retrieve or create the Catalog instance we may be updating
        Catalog catalog = null;
        if (attr != null) {
            catalog = (Catalog) context.getAttribute(attr);
            if (catalog == null) {
                catalog = new CatalogBase();
            }
        }

        // Construct the configuration resource parser we will use
        ConfigParser parser = new ConfigParser();
        if (ruleSet != null) {
            try {
                ClassLoader loader =
                    Thread.currentThread().getContextClassLoader();
                if (loader == null) {
                    loader = this.getClass().getClassLoader();
                }
                Class clazz = loader.loadClass(ruleSet);
                parser.setRuleSet((RuleSet) clazz.newInstance());
            } catch (Exception e) {
                throw new ServletException("Exception initalizing RuleSet '"
                                           + ruleSet + "' instance", e);
            }
        }

        // Parse the resources specified in our init parameters (if any)
        if (attr == null) {
            ChainResources.parseClassResources
                (classResources, parser);
            ChainResources.parseWebResources
                (context, webResources, parser);
        } else {
            ChainResources.parseClassResources
                (catalog, classResources, parser);
            ChainResources.parseWebResources
                (catalog, context, webResources, parser);
        }

        // Expose the completed catalog (if requested)
        if (attr != null) {
            context.setAttribute(attr, catalog);
        }

    }


    /**
     * <p>Does nothing; this servlet's only purpose is to initialize a Chain
     * and store it in the servlet context.</p>
     *
     * @param request the request issued by the client
     * @param response the response to be returned to the cliengt
     *
     * @throws javax.servlet.ServletException (this exception is never thrown)
     * @throws java.io.IOException (this exception is never thrown)
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
        throws ServletException, IOException {

        ; // do nothing

    }


}
