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
import org.apache.commons.collections.primitives.ShortList;
import org.apache.commons.collections.primitives.ShortListIterator;

/**
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:27 $
 * 
 * @author Rodney Waldhoff 
 */
abstract class BaseProxyShortList extends BaseProxyShortCollection implements ShortList {
    protected abstract ShortList getProxiedList();

    protected final ShortCollection getProxiedCollection() {
        return getProxiedList();
    }

    protected BaseProxyShortList() {
    }

    public void add(int index, short element) {
        getProxiedList().add(index,element);
    }

    public boolean addAll(int index, ShortCollection collection) {        
        return getProxiedList().addAll(index,collection);
    }

    public short get(int index) {
        return getProxiedList().get(index);
    }

    public int indexOf(short element) {
        return getProxiedList().indexOf(element);
    }

    public int lastIndexOf(short element) {
        return getProxiedList().lastIndexOf(element);
    }

    public ShortListIterator listIterator() {
        return getProxiedList().listIterator();
    }

    public ShortListIterator listIterator(int index) {
        return getProxiedList().listIterator(index);
    }

    public short removeElementAt(int index) {
        return getProxiedList().removeElementAt(index);
    }

    public short set(int index, short element) {
        return getProxiedList().set(index,element);
    }

    public ShortList subList(int fromIndex, int toIndex) {
        return getProxiedList().subList(fromIndex,toIndex);
    }

}
