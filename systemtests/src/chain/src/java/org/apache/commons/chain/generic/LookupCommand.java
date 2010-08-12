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
package org.apache.commons.chain.generic;


import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;


/**
 * <p>Look up a specified {@link Command} (which could also be a {@link Chain})
 * in a {@link Catalog}, and delegate execution to it.  If the delegated-to
 * {@link Command} is also a {@link Filter}, its <code>postprocess()</code>
 * method will also be invoked at the appropriate time.</p>
 *
 * <p>The name of the {@link Command} can be specified either directly (via
 * the <code>name</code> property) or indirectly (via the <code>nameKey</code>
 * property).  Exactly one of these must be set.</p>
 *
 * <p>If the <code>optional</code> property is set to <code>true</code>,
 * failure to find the specified command in the specified catalog will be
 * silently ignored.  Otherwise, a lookup failure will trigger an
 * <code>IllegalArgumentException</code>.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.11 $ $Date: 2005/01/08 18:02:28 $
 */

public class LookupCommand implements Filter {


    // -------------------------------------------------------------- Properties


    private String catalogName = null;


    /**
     * <p>Return the name of the {@link Catalog} to be searched, or
     * <code>null</code> to search the default {@link Catalog}.</p>
     */
    public String getCatalogName() {

        return (this.catalogName);

    }


    /**
     * <p>Set the name of the {@link Catalog} to be searched, or
     * <code>null</code> to search the default {@link Catalog}.</p>
     *
     * @param catalogName The new {@link Catalog} name or <code>null</code>
     */
    public void setCatalogName(String catalogName) {

        this.catalogName = catalogName;

    }


    private String name = null;


    /**
     * <p>Return the name of the {@link Command} that we will look up and
     * delegate execution to.</p>
     */
    public String getName() {

        return (this.name);

    }


    /**
     * <p>Set the name of the {@link Command} that we will look up and
     * delegate execution to.</p>
     *
     * @param name The new command name
     */
    public void setName(String name) {

        this.name = name;

    }


    private String nameKey = null;


    /**
     * <p>Return the context attribute key under which the {@link Command}
     * name is stored.</p>
     */
    public String getNameKey() {

        return (this.nameKey);

    }


    /**
     * <p>Set the context attribute key under which the {@link Command}
     * name is stored.</p>
     *
     * @param nameKey The new context attribute key
     */
    public void setNameKey(String nameKey) {

        this.nameKey = nameKey;

    }


    private boolean optional = false;


    /**
     * <p>Return <code>true</code> if locating the specified command
     * is optional.</p>
     */
    public boolean isOptional() {

        return (this.optional);

    }


    /**
     * <p>Set the optional flag for finding the specified command.</p>
     *
     * @param optional The new optional flag
     */
    public void setOptional(boolean optional) {

        this.optional = optional;

    }



    // ---------------------------------------------------------- Filter Methods


    /**
     * <p>Look up the specified command, and (if found) execute it.</p>
     *
     * @param context The context for this request
     *
     * @exception IllegalArgumentException if no such {@link Command}
     *  can be found and the <code>optional</code> property is set
     *  to <code>false</code>
     */
    public boolean execute(Context context) throws Exception {

        Command command = getCommand(context);
        if (command != null) {
            return (command.execute(context));
        } else {
            return (false);
        }

    }


    /**
     * <p>If the executed command was itself a {@link Filter}, call the
     * <code>postprocess()</code> method of that {@link Filter} as well.</p>
     *
     * @param context The context for this request
     * @param exception Any <code>Exception</code> thrown by command execution
     *
     * @exception Exception if thrown by the <code>postprocess()</code> method
     */
    public boolean postprocess(Context context, Exception exception) {

        Command command = getCommand(context);
        if (command != null) {
            if (command instanceof Filter) {
                return (((Filter) command).postprocess(context, exception));
            }
        }
        return (false);

    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Return the {@link Command} instance to be delegated to.</p>
     *
     * @param context {@link Context} for this request
     *
     * @exception IllegalArgumentException if no such {@link Command}
     *  can be found and the <code>optional</code> property is set
     *  to <code>false</code>
     */
    protected Command getCommand(Context context) {

        CatalogFactory catalogFactory = CatalogFactory.getInstance();
        String catalogName = getCatalogName();
        Catalog catalog = null;
        if (catalogName == null) {
            // use default catalog
            catalog = catalogFactory.getCatalog();
        } else {
            catalog = catalogFactory.getCatalog(catalogName);
        }
        if (catalog == null) {
            if (catalogName == null) {
                throw new IllegalArgumentException
                    ("Cannot find default catalog");
            } else {
                throw new IllegalArgumentException
                    ("Cannot find catalog '" + catalogName + "'");
            }
        }

        Command command = null;
        String name = getName();
        if (name == null) {
            name = (String) context.get(getNameKey());
        }
        if (name != null) {
            command = catalog.getCommand(name);
            if ((command == null) && !isOptional()) {
                if (catalogName == null) {
                    throw new IllegalArgumentException
                        ("Cannot find command '" + name
                         + "' in default catalog");
                } else {
                    throw new IllegalArgumentException
                        ("Cannot find command '" + name
                         + "' in catalog '" + catalogName + "'");
                }
            }
            return (command);
        } else {
            throw new IllegalArgumentException("No command name");
        }

    }


}
