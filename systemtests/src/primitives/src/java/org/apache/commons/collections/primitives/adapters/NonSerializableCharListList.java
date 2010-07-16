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
package org.apache.commons.collections.primitives.adapters;

import org.apache.commons.collections.primitives.CharList;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
final class NonSerializableCharListList extends AbstractCharListList {
    
    /**
     * Creates a {@link List List} wrapping
     * the specified {@link CharList CharList}.
     */
    public NonSerializableCharListList(CharList list) {
        _list = list;
    }

    protected CharList getCharList() {
        return _list;
    }    
    
    private CharList _list = null;

}
