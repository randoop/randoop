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

import org.apache.commons.collections.primitives.CharCollection;
import org.apache.commons.collections.primitives.CharIterator;
import org.apache.commons.collections.primitives.CharList;
import org.apache.commons.collections.primitives.CharListIterator;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
abstract class BaseUnmodifiableCharList extends BaseProxyCharList {

    public final void add(int index, char element) {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }

    public final boolean addAll(int index, CharCollection collection) {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }

    public final char removeElementAt(int index) {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }

    public final char set(int index, char element) {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }

    public final boolean add(char element) {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }

    public final boolean addAll(CharCollection c) {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }

    public final void clear() {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }

    public final boolean removeAll(CharCollection c) {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }

    public final boolean removeElement(char element) {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }

    public final boolean retainAll(CharCollection c) {
        throw new UnsupportedOperationException("This CharList is not modifiable.");
    }    
    
    public final CharList subList(int fromIndex, int toIndex) {
        return UnmodifiableCharList.wrap(getProxiedList().subList(fromIndex,toIndex));
    }

    public final CharIterator iterator() {
        return UnmodifiableCharIterator.wrap(getProxiedList().iterator());
    }
    
    public CharListIterator listIterator() {
        return UnmodifiableCharListIterator.wrap(getProxiedList().listIterator());
    }

    public CharListIterator listIterator(int index) {
        return UnmodifiableCharListIterator.wrap(getProxiedList().listIterator(index));
    }

}
