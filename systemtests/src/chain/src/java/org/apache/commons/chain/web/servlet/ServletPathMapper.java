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


import javax.servlet.http.HttpServletRequest;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;


/**
 * <p>{@link Command} that uses the "servlet path" component of the request URI
 * to select a {@link Command} from the appropriate {@link Catalog}, and
 * execute it.  To use this command, you would typically map an instance
 * of {@link ChainProcessor} to an extension pattern like "*.execute" and
 * then arrange that this is the default command to be executed.  In such
 * an environment, a request for a context relative URI of "/foo.execute"
 * would cause the "/foo.execute" command to be loaded and executed.</p>
 *
 * @author Craig R. McClanahan
 */

public class ServletPathMapper implements Command {


    // ------------------------------------------------------ Instance Variables


    private String catalogKey = ChainProcessor.CATALOG_DEFAULT;


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the context key under which our {@link Catalog} has been
     * stored.</p>
     */
    public String getCatalogKey() {

        return (this.catalogKey);

    }


    /**
     * <p>Set the context key under which our {@link Catalog} has been
     * stored.</p>
     *
     * @param catalogKey The new catalog key
     */
    public void setCatalogKey(String catalogKey) {

        this.catalogKey = catalogKey;

    }


    // --------------------------------------------------------- Command Methods


    /**
     * <p>Look up the servlet path information for this request, and use it to
     * select an appropriate {@link Command} to be executed.
     *
     * @param context Context for the current request
     */
    public boolean execute(Context context) throws Exception {

        // Look up the servlet path for this request
        ServletWebContext swcontext = (ServletWebContext) context;
        HttpServletRequest request = swcontext.getRequest();
        String servletPath = (String)
            request.getAttribute("javax.servlet.include.servlet_path");
        if (servletPath == null) {
            servletPath = request.getServletPath();
        }

        // Map to the Command specified by the extra path info
        Catalog catalog = (Catalog) context.get(getCatalogKey());
        Command command = catalog.getCommand(servletPath);
        return (command.execute(context));

    }


}
