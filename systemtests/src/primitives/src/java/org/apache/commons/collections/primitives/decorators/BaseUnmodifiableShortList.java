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

import org.apache.commons.collections.primitives.ShortCollection;
import org.apache.commons.collections.primitives.ShortIterator;
import org.apache.commons.collections.primitives.ShortList;
import org.apache.commons.collections.primitives.ShortListIterator;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
abstract class BaseUnmodifiableShortList extends BaseProxyShortList {

    public final void add(int index, short element) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean addAll(int index, ShortCollection collection) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final short removeElementAt(int index) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final short set(int index, short element) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean add(short element) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean addAll(ShortCollection c) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final void clear() {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean removeAll(ShortCollection c) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean removeElement(short element) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }

    public final boolean retainAll(ShortCollection c) {
        throw new UnsupportedOperationException("This ShortList is not modifiable.");
    }    
    
    public final ShortList subList(int fromIndex, int toIndex) {
        return UnmodifiableShortList.wrap(getProxiedList().subList(fromIndex,toIndex));
    }

    public final ShortIterator iterator() {
        return UnmodifiableShortIterator.wrap(getProxiedList().iterator());
    }
    
    public ShortListIterator listIterator() {
        return UnmodifiableShortListIterator.wrap(getProxiedList().listIterator());
    }

    public ShortListIterator listIterator(int index) {
        return UnmodifiableShortListIterator.wrap(getProxiedList().listIterator(index));
    }

}
