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
import org.apache.commons.collections.primitives.CharList;
import org.apache.commons.collections.primitives.CharListIterator;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
abstract class BaseProxyCharList extends BaseProxyCharCollection implements CharList {
    protected abstract CharList getProxiedList();

    protected final CharCollection getProxiedCollection() {
        return getProxiedList();
    }

    protected BaseProxyCharList() {
    }

    public void add(int index, char element) {
        getProxiedList().add(index,element);
    }

    public boolean addAll(int index, CharCollection collection) {        
        return getProxiedList().addAll(index,collection);
    }

    public char get(int index) {
        return getProxiedList().get(index);
    }

    public int indexOf(char element) {
        return getProxiedList().indexOf(element);
    }

    public int lastIndexOf(char element) {
        return getProxiedList().lastIndexOf(element);
    }

    public CharListIterator listIterator() {
        return getProxiedList().listIterator();
    }

    public CharListIterator listIterator(int index) {
        return getProxiedList().listIterator(index);
    }

    public char removeElementAt(int index) {
        return getProxiedList().removeElementAt(index);
    }

    public char set(int index, char element) {
        return getProxiedList().set(index,element);
    }

    public CharList subList(int fromIndex, int toIndex) {
        return getProxiedList().subList(fromIndex,toIndex);
    }

}
