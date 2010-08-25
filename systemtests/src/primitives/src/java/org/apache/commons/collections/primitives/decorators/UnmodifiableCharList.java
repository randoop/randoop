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
package org.apache.commons.collections.primitives.decorators;

import java.io.Serializable;

import org.apache.commons.collections.primitives.CharList;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
public final class UnmodifiableCharList extends BaseUnmodifiableCharList implements Serializable {
    UnmodifiableCharList(CharList list) {
        this.proxied = list;
    }
    
    public static final CharList wrap(CharList list) {
        if(null == list) {
            return null; 
        } else if(list instanceof UnmodifiableCharList) {
            return list;
        } else if(list instanceof Serializable) {
            return new UnmodifiableCharList(list);
        } else {
            return new NonSerializableUnmodifiableCharList(list);
        }
    }

    protected CharList getProxiedList() {
        return proxied;
    }

    private CharList proxied = null;
}
