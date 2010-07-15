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

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.primitives.ShortCollection;
import org.apache.commons.collections.primitives.ShortIterator;
import org.apache.commons.collections.primitives.ShortList;
import org.apache.commons.collections.primitives.ShortListIterator;

/**
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractListShortList extends AbstractCollectionShortCollection implements ShortList {

    public void add(int index, short element) {
        getList().add(index,new Short(element));
    }

    public boolean addAll(int index, ShortCollection collection) {
        return getList().addAll(index,ShortCollectionCollection.wrap(collection));
    }

    public short get(int index) {
        return ((Number)getList().get(index)).shortValue();
    }

    public int indexOf(short element) {
        return getList().indexOf(new Short(element));
    }

    public int lastIndexOf(short element) {
        return getList().lastIndexOf(new Short(element));
    }

    /**
     * {@link ListIteratorShortListIterator#wrap wraps} the 
     * {@link ShortList ShortList} 
     * returned by my underlying 
     * {@link ShortListIterator ShortListIterator},
     * if any.
     */
    public ShortListIterator listIterator() {
        return ListIteratorShortListIterator.wrap(getList().listIterator());
    }

    /**
     * {@link ListIteratorShortListIterator#wrap wraps} the 
     * {@link ShortList ShortList} 
     * returned by my underlying 
     * {@link ShortListIterator ShortListIterator},
     * if any.
     */
    public ShortListIterator listIterator(int index) {
        return ListIteratorShortListIterator.wrap(getList().listIterator(index));
    }

    public short removeElementAt(int index) {
        return ((Number)getList().remove(index)).shortValue();
    }

    public short set(int index, short element) {
        return ((Number)getList().set(index,new Short(element))).shortValue();
    }

    public ShortList subList(int fromIndex, int toIndex) {
        return ListShortList.wrap(getList().subList(fromIndex,toIndex));
    }

    public boolean equals(Object obj) {
        if(obj instanceof ShortList) {
            ShortList that = (ShortList)obj;
            if(this == that) {
                return true;
            } else if(this.size() != that.size()) {
                return false;            
            } else {
                ShortIterator thisiter = iterator();
                ShortIterator thatiter = that.iterator();
                while(thisiter.hasNext()) {
                    if(thisiter.next() != thatiter.next()) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }
        
    public int hashCode() {
        return getList().hashCode();
    }
    
    final protected Collection getCollection() {
        return getList();
    }
    
    abstract protected List getList();
}
