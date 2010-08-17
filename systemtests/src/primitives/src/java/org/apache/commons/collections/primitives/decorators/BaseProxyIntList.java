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

import org.apache.commons.collections.primitives.IntCollection;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.IntListIterator;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
abstract class BaseProxyIntList extends BaseProxyIntCollection implements IntList {
    protected abstract IntList getProxiedList();

    protected final IntCollection getProxiedCollection() {
        return getProxiedList();
    }

    protected BaseProxyIntList() {
    }

    public void add(int index, int element) {
        getProxiedList().add(index,element);
    }

    public boolean addAll(int index, IntCollection collection) {        
        return getProxiedList().addAll(index,collection);
    }

    public int get(int index) {
        return getProxiedList().get(index);
    }

    public int indexOf(int element) {
        return getProxiedList().indexOf(element);
    }

    public int lastIndexOf(int element) {
        return getProxiedList().lastIndexOf(element);
    }

    public IntListIterator listIterator() {
        return getProxiedList().listIterator();
    }

    public IntListIterator listIterator(int index) {
        return getProxiedList().listIterator(index);
    }

    public int removeElementAt(int index) {
        return getProxiedList().removeElementAt(index);
    }

    public int set(int index, int element) {
        return getProxiedList().set(index,element);
    }

    public IntList subList(int fromIndex, int toIndex) {
        return getProxiedList().subList(fromIndex,toIndex);
    }

}
