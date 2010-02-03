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

import org.apache.commons.collections.primitives.ShortList;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
public final class UnmodifiableShortList extends BaseUnmodifiableShortList implements Serializable {
    UnmodifiableShortList(ShortList list) {
        this.proxied = list;
    }
    
    public static final ShortList wrap(ShortList list) {
        if(null == list) {
            return null; 
        } else if(list instanceof UnmodifiableShortList) {
            return list;
        } else if(list instanceof Serializable) {
            return new UnmodifiableShortList(list);
        } else {
            return new NonSerializableUnmodifiableShortList(list);
        }
    }

    protected ShortList getProxiedList() {
        return proxied;
    }

    private ShortList proxied = null;
}
