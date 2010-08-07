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


import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;


/**
 * <p>Copy a specified literal value, or a context attribute stored under
 * the <code>fromKey</code> (if any), to the <code>toKey</code>.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.8 $ $Date: 2005/01/07 19:18:03 $
 */

public class CopyCommand implements Command {


    // -------------------------------------------------------------- Properties


    private String fromKey = null;


    /**
     * <p>Return the context attribute key for the source attribute.</p>
     */
    public String getFromKey() {

    return (this.fromKey);

    }


    /**
     * <p>Set the context attribute key for the source attribute.</p>
     *
     * @param fromKey The new key
     */
    public void setFromKey(String fromKey) {

    this.fromKey = fromKey;

    }


    private String toKey = null;


    /**
     * <p>Return the context attribute key for the destination attribute.</p>
     */
    public String getToKey() {

    return (this.toKey);

    }


    /**
     * <p>Set the context attribute key for the destination attribute.</p>
     *
     * @param toKey The new key
     */
    public void setToKey(String toKey) {

    this.toKey = toKey;

    }


    private String value = null;


    /**
     * <p>Return the literal value to be copied.</p>
     */
    public String getValue() {

        return (this.value);

    }


    /**
     * <p>Set the literal value to be copied.</p>
     *
     * @param value The new value
     */
    public void setValue(String value) {

        this.value = value;

    }


    // ---------------------------------------------------------- Filter Methods


    /**
     * <p>Copy a specified literal value, or a context attribute stored under
     * the <code>fromKey</code> (if any), to the <code>toKey</code>.</p>
     *
     * @param context {@link Context} in which we are operating
     *
     * @return <code>false</code> so that processing will continue
     */
    public boolean execute(Context context) throws Exception {

        Object value = this.value;
        
        if (value == null) {
            value = context.get(getFromKey());
        }
        
        if (value != null) {
            context.put(getToKey(), value);
        } else {
            context.remove(getToKey());
        }
        
        return (false);

    }


}
