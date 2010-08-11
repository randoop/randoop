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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;


/**
 * <p>Simple in-memory implementation of {@link Catalog}.  This class can
 * also be used as the basis for more advanced implementations.</p>
 *
 * <p>This implementation is thread-safe.</p>
 *
 * @author Craig R. McClanahan
 * @author Matthew J. Sgarlata
 * @version $Revision: 1.12 $ $Date: 2004/11/30 05:52:23 $
 */

public class CatalogBase implements Catalog {


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The map of named {@link Command}s, keyed by name.
     */
    protected Map commands = Collections.synchronizedMap(new HashMap());


    // --------------------------------------------------------- Public Methods


    // Documented in Catalog interface
    public void addCommand(String name, Command command) {

        commands.put(name, command);

    }

    // Documented in Catalog interface
    public Command getCommand(String name) {

        return ((Command) commands.get(name));

    }


    // Documented in Catalog interface
    public Iterator getNames() {

        return (commands.keySet().iterator());

    }

    /**
     * Converts this Catalog to a String.  Useful for debugging purposes.
     * @return a representation of this catalog as a String
     */
    public String toString() {

        Iterator names = getNames();
        StringBuffer str =
            new StringBuffer("[" + this.getClass().getName() + ": ");

        while (names.hasNext()) {
            str.append(names.next());
            if (names.hasNext()) {
            str.append(", ");
            }
        }
        str.append("]");

        return str.toString();

    }
}
