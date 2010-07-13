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

import org.apache.commons.collections.primitives.LongCollection;
import org.apache.commons.collections.primitives.LongIterator;
import org.apache.commons.collections.primitives.LongList;
import org.apache.commons.collections.primitives.LongListIterator;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
abstract class BaseUnmodifiableLongList extends BaseProxyLongList {

    public final void add(int index, long element) {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }

    public final boolean addAll(int index, LongCollection collection) {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }

    public final long removeElementAt(int index) {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }

    public final long set(int index, long element) {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }

    public final boolean add(long element) {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }

    public final boolean addAll(LongCollection c) {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }

    public final void clear() {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }

    public final boolean removeAll(LongCollection c) {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }

    public final boolean removeElement(long element) {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }

    public final boolean retainAll(LongCollection c) {
        throw new UnsupportedOperationException("This LongList is not modifiable.");
    }    
    
    public final LongList subList(int fromIndex, int toIndex) {
        return UnmodifiableLongList.wrap(getProxiedList().subList(fromIndex,toIndex));
    }

    public final LongIterator iterator() {
        return UnmodifiableLongIterator.wrap(getProxiedList().iterator());
    }
    
    public LongListIterator listIterator() {
        return UnmodifiableLongListIterator.wrap(getProxiedList().listIterator());
    }

    public LongListIterator listIterator(int index) {
        return UnmodifiableLongListIterator.wrap(getProxiedList().listIterator(index));
    }

}
