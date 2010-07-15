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

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
abstract class BaseProxyCharCollection implements CharCollection {
    protected abstract CharCollection getProxiedCollection();

    protected BaseProxyCharCollection() {
    }
    
    public boolean add(char element) {
        return getProxiedCollection().add(element);
    }

    public boolean addAll(CharCollection c) {
        return getProxiedCollection().addAll(c);
    }

    public void clear() {
        getProxiedCollection().clear();
    }

    public boolean contains(char element) {
        return getProxiedCollection().contains(element);
    }

    public boolean containsAll(CharCollection c) {
        return getProxiedCollection().containsAll(c);
    }

    public boolean isEmpty() {
        return getProxiedCollection().isEmpty();
    }

    public CharIterator iterator() {
        return getProxiedCollection().iterator();
    }

    public boolean removeAll(CharCollection c) {
        return getProxiedCollection().removeAll(c);
    }

    public boolean removeElement(char element) {
        return getProxiedCollection().removeElement(element);
    }

    public boolean retainAll(CharCollection c) {
        return getProxiedCollection().retainAll(c);
    }

    public int size() {
        return getProxiedCollection().size();
    }

    public char[] toArray() {
        return getProxiedCollection().toArray();
    }

    public char[] toArray(char[] a) {
        return getProxiedCollection().toArray(a);
    }

    // TODO: Add note about possible contract violations here.
    
    public boolean equals(Object obj) {
        return getProxiedCollection().equals(obj);
    }

    public int hashCode() {
        return getProxiedCollection().hashCode();
    }

    public String toString() {
        return getProxiedCollection().toString();
    }

}
