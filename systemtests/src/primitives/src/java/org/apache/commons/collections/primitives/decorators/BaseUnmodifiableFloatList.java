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

import org.apache.commons.collections.primitives.FloatCollection;
import org.apache.commons.collections.primitives.FloatIterator;
import org.apache.commons.collections.primitives.FloatList;
import org.apache.commons.collections.primitives.FloatListIterator;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
abstract class BaseUnmodifiableFloatList extends BaseProxyFloatList {

    public final void add(int index, float element) {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }

    public final boolean addAll(int index, FloatCollection collection) {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }

    public final float removeElementAt(int index) {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }

    public final float set(int index, float element) {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }

    public final boolean add(float element) {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }

    public final boolean addAll(FloatCollection c) {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }

    public final void clear() {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }

    public final boolean removeAll(FloatCollection c) {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }

    public final boolean removeElement(float element) {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }

    public final boolean retainAll(FloatCollection c) {
        throw new UnsupportedOperationException("This FloatList is not modifiable.");
    }    
    
    public final FloatList subList(int fromIndex, int toIndex) {
        return UnmodifiableFloatList.wrap(getProxiedList().subList(fromIndex,toIndex));
    }

    public final FloatIterator iterator() {
        return UnmodifiableFloatIterator.wrap(getProxiedList().iterator());
    }
    
    public FloatListIterator listIterator() {
        return UnmodifiableFloatListIterator.wrap(getProxiedList().listIterator());
    }

    public FloatListIterator listIterator(int index) {
        return UnmodifiableFloatListIterator.wrap(getProxiedList().listIterator(index));
    }

}
