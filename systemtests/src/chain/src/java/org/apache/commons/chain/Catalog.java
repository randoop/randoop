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
package org.apache.commons.chain;


import java.util.Iterator;


/**
 * <p>A {@link Catalog} is a collection of named {@link Command}s (or
 * {@link Chain}s) that can be used retrieve the set of commands that
 * should be performed based on a symbolic identifier.  Use of catalogs
 * is optional, but convenient when there are multiple possible chains
 * that can be selected and executed based on environmental conditions.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.7 $ $Date: 2004/11/30 05:52:22 $
 */

public interface Catalog {


    /**
     * <p>A default context attribute for storing a default {@link Catalog},
     * provided as a convenience only.</p>
     */
    String CATALOG_KEY = "org.apache.commons.chain.CATALOG";


    /**
     * <p>Add a new name and associated {@link Command} or {@link Chain}
     * to the set of named commands known to this {@link Catalog},
     * replacing any previous command for that name.
     *
     * @param name Name of the new command
     * @param command {@link Command} or {@link Chain} to be returned
     *  for later lookups on this name
     */
    void addCommand(String name, Command command);


    /**
     * <p>Return the {@link Command} or {@link Chain} associated with the
     * specified name, if any; otherwise, return <code>null</code>.</p>
     *
     * @param name Name for which a {@link Command} or {@link Chain}
     *  should be retrieved
     */
    Command getCommand(String name);



    /**
     * <p>Return an <code>Iterator</code> over the set of named commands
     * known to this {@link Catalog}.  If there are no known commands,
     * an empty Iterator is returned.</p>
     */
    Iterator getNames();


}

