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

import org.apache.commons.collections.primitives.BooleanList;

/**
 * @since Commons Primitives 1.1
 * @version $Revision: 1.1 $ $Date: 2004/07/12 18:29:43 $
 * @author Rodney Waldhoff 
 */
final class NonSerializableBooleanListList extends AbstractBooleanListList {
    
    /**
     * Creates a {@link List List} wrapping
     * the specified {@link BooleanList BooleanList}.
     */
    public NonSerializableBooleanListList(BooleanList list) {
        _list = list;
    }

    protected BooleanList getBooleanList() {
        return _list;
    }    
    
    private BooleanList _list = null;

}
