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
package org.apache.commons.chain.web.servlet;


import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.web.ChainServlet;


/**
 * <p>Custom subclass of {@link ChainServlet} that also dispatches incoming
 * requests to a configurable {@link Command} loaded from the specified
 * {@link Catalog}.</p>
 *
 * <p>In addition to the <em>servlet</em> init parameters supported by
 * {@link ChainServlet}, this class supports the following additional
 * parameters:</p>
 * <ul>
 * <li><strong>org.apache.commons.chain.CATALOG</strong> - Name of the
 *     catalog from which to acquire commands to be executed.  If not
 *     specified, the default catalog for this application will be used.</li>
 * <li><strong>org.apache.commons.chain.COMMAND</strong> - Name of the
 *     {@link Command} (looked up in our configured {@link Catalog} used
 *     to process all incoming servlet requests.  If not specified,
 *     defaults to <code>command</code>.</li>
 * </ul>
 *
 * <p>Also, the <code>org.apache.commons.chain.CONFIG_ATTR</code>
 * init parameter is also used to identify the {@link Context} attribute under
 * which our configured {@link Catalog} will be made available to
 * {@link Command}s processing our requests, in addition to its definition
 * of the <code>ServletContext</code> attribute key under which the
 * {@link Catalog} is available.</p>
 */

public class ChainProcessor extends ChainServlet {


    // ------------------------------------------------------ Manifest Constants


    /**
     * <p>The name of the servlet init parameter containing the name of the
     * {@link Catalog} to use for processing incoming requests.</p>
     */
    public static final String CATALOG =
        "org.apache.commons.chain.CATALOG";


    /**
     * <p>The default request attribute under which we expose the
     * {@link Catalog} being used to subordinate {@link Command}s.</p>
     */
    public static final String CATALOG_DEFAULT =
        "org.apache.commons.chain.CATALOG";


    /**
     * <p>The name of the servlet init parameter containing the name of the
     * {@link Command} (loaded from our configured {@link Catalog} to use
     * for processing each incoming request.</p>
     */
    public static final String COMMAND =
        "org.apache.commons.chain.COMMAND";


    /**
     * <p>The default command name.</p>
     */
    private static final String COMMAND_DEFAULT = "command";


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The name of the context attribute under which our {@link Catalog}
     * is stored.  This value is also used as the name of the
     * context attribute under which the catalog is exposed to commands.
     * If not specified, we will look up commands in the appropriate
     * {@link Catalog} retrieved from our {@link CatalogFactory}.</p>
     */
    private String attribute = null;


    /**
     * <p>The name of the {@link Catalog} to retrieve from the
     * {@link CatalogFactory} for this application, or <code>null</code>
     * to select the default {@link Catalog}.</p>
     */
    private String catalog = null;


    /**
     * <p>The name of the {@link Command} to be executed for each incoming
     * request.</p>
     */
    private String command = null;


    /**
     * <p>The {@link CatalogFactory} for this application.</p>
     */
    private CatalogFactory factory = null;


    // --------------------------------------------------------- Servlet Methods


    /**
     * <p>Clean up as this application is shut down.</p>
     */
    public void destroy() {

        super.destroy();
        attribute = null;
        catalog = null;
        command = null;
        factory = null;

    }


    /**
     * <p>Cache the name of the command we should execute for each request.</p>
     *
     * @exception ServletException if an initialization error occurs
     */
    public void init() throws ServletException {

        super.init();
        attribute = getServletConfig().getInitParameter(CONFIG_ATTR);
        catalog = getServletConfig().getInitParameter(CATALOG);
        command = getServletConfig().getInitParameter(COMMAND);
        if (command == null) {
            command = COMMAND_DEFAULT;
        }
        factory = CatalogFactory.getInstance();

    }


    /**
     * <p>Configure a {@link ServletWebContext} for the current request, and
     * pass it to the <code>execute()</code> method of the specified
     * {@link Command}, loaded from our configured {@link Catalog}.</p>
     *
     * @param request The request we are processing
     * @param response The response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
        throws IOException, ServletException {

        ServletWebContext context =
            new ServletWebContext(getServletContext(), request, response);
        Catalog theCatalog = null;
        if (attribute != null) {
            theCatalog = (Catalog) getServletContext().getAttribute
                (this.attribute);
        } else if (catalog != null) {
            theCatalog = factory.getCatalog(catalog);
        } else {
            theCatalog = factory.getCatalog();
        }
        if (attribute == null) {
            request.setAttribute(CATALOG_DEFAULT, theCatalog);
        }
        Command command = theCatalog.getCommand(this.command);
        try {
            command.execute(context);
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }


}
