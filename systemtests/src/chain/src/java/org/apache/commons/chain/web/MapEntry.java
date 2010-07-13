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


import java.util.Map;


/**
 * <p>Map.Entry implementation that can be constructed to either be read-only
 * or not.</p>
 *
 * @version $Revision: 1.3 $ $Date: 2004/11/30 05:52:23 $
 */

public class MapEntry implements Map.Entry {


    /**
     * <p>The entry key.</p>
     */
    private Object key;

    /**
     * <p>The entry value.</p>
     */
    private Object value;

    /**
     * <p>Whether the entry can be modified.</p>
     */
    private boolean modifiable = false;


    /**
     * <p>Creates a map entry that can either allow modifications or not.</p>
     *
     * @param key The entry key
     * @param value The entry value
     * @param modifiable Whether the entry should allow modification or not
     */
    public MapEntry(Object key, Object value, boolean modifiable) {
        this.key = key;
        this.value = value;
        this.modifiable = modifiable;
    }


    /**
     * <p>Gets the entry key.</p>
     *
     * @return The entry key
     */
    public Object getKey() {
        return key;
    }


    /**
     * <p>Gets the entry value.</p>
     *
     * @return The entry key
     */
    public Object getValue() {
        return value;
    }


    /**
     * <p>Sets the entry value if the entry can be modified.</p>
     *
     * @param val The new value
     * @return The old entry value
     * @throws UnsupportedOperationException If the entry cannot be modified
     */
    public Object setValue(Object val) {
        if (modifiable) {
            Object oldVal = this.value;
            this.value = val;
            return oldVal;
        } else {
            throw new UnsupportedOperationException();
        }
    }


    /**
     * <p>Determines if this entry is equal to the passed object.</p>
     *
     * @param o The object to test
     * @return True if equal, else false
     */
    public boolean equals(Object o) {
        if (o != null && o instanceof Map.Entry) {
            Map.Entry entry = (Map.Entry)o;
            return (this.getKey() == null ?
                    entry.getKey() == null : this.getKey().equals(entry.getKey()))  &&
                   (this.getValue() == null ?
                    entry.getValue() == null : this.getValue().equals(entry.getValue()));
        }
        return false;
    }


    /**
     * <p>Returns the hashcode for this entry.</p>
     *
     * @return The and'ed hashcode of the key and value
     */
    public int hashCode() {
        return (this.getKey() == null   ? 0 : this.getKey().hashCode()) ^
               (this.getValue() == null ? 0 : this.getValue().hashCode());
    }
}
