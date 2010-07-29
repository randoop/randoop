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

import org.apache.commons.collections.primitives.IntCollection;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.IntListIterator;

/**
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractListIntList extends AbstractCollectionIntCollection implements IntList {

    public void add(int index, int element) {
        getList().add(index,new Integer(element));
    }

    public boolean addAll(int index, IntCollection collection) {
        return getList().addAll(index,IntCollectionCollection.wrap(collection));
    }

    public int get(int index) {
        return ((Number)getList().get(index)).intValue();
    }

    public int indexOf(int element) {
        return getList().indexOf(new Integer(element));
    }

    public int lastIndexOf(int element) {
        return getList().lastIndexOf(new Integer(element));
    }

    /**
     * {@link ListIteratorIntListIterator#wrap wraps} the 
     * {@link IntList IntList} 
     * returned by my underlying 
     * {@link IntListIterator IntListIterator},
     * if any.
     */
    public IntListIterator listIterator() {
        return ListIteratorIntListIterator.wrap(getList().listIterator());
    }

    /**
     * {@link ListIteratorIntListIterator#wrap wraps} the 
     * {@link IntList IntList} 
     * returned by my underlying 
     * {@link IntListIterator IntListIterator},
     * if any.
     */
    public IntListIterator listIterator(int index) {
        return ListIteratorIntListIterator.wrap(getList().listIterator(index));
    }

    public int removeElementAt(int index) {
        return ((Number)getList().remove(index)).intValue();
    }

    public int set(int index, int element) {
        return ((Number)getList().set(index,new Integer(element))).intValue();
    }

    public IntList subList(int fromIndex, int toIndex) {
        return ListIntList.wrap(getList().subList(fromIndex,toIndex));
    }

    public boolean equals(Object obj) {
        if(obj instanceof IntList) {
            IntList that = (IntList)obj;
            if(this == that) {
                return true;
            } else if(this.size() != that.size()) {
                return false;            
            } else {
                IntIterator thisiter = iterator();
                IntIterator thatiter = that.iterator();
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
