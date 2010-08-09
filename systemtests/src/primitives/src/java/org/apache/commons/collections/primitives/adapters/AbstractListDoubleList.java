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

import org.apache.commons.collections.primitives.DoubleCollection;
import org.apache.commons.collections.primitives.DoubleIterator;
import org.apache.commons.collections.primitives.DoubleList;
import org.apache.commons.collections.primitives.DoubleListIterator;

/**
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractListDoubleList extends AbstractCollectionDoubleCollection implements DoubleList {

    public void add(int index, double element) {
        getList().add(index,new Double(element));
    }

    public boolean addAll(int index, DoubleCollection collection) {
        return getList().addAll(index,DoubleCollectionCollection.wrap(collection));
    }

    public double get(int index) {
        return ((Number)getList().get(index)).doubleValue();
    }

    public int indexOf(double element) {
        return getList().indexOf(new Double(element));
    }

    public int lastIndexOf(double element) {
        return getList().lastIndexOf(new Double(element));
    }

    /**
     * {@link ListIteratorDoubleListIterator#wrap wraps} the 
     * {@link DoubleList DoubleList} 
     * returned by my underlying 
     * {@link DoubleListIterator DoubleListIterator},
     * if any.
     */
    public DoubleListIterator listIterator() {
        return ListIteratorDoubleListIterator.wrap(getList().listIterator());
    }

    /**
     * {@link ListIteratorDoubleListIterator#wrap wraps} the 
     * {@link DoubleList DoubleList} 
     * returned by my underlying 
     * {@link DoubleListIterator DoubleListIterator},
     * if any.
     */
    public DoubleListIterator listIterator(int index) {
        return ListIteratorDoubleListIterator.wrap(getList().listIterator(index));
    }

    public double removeElementAt(int index) {
        return ((Number)getList().remove(index)).doubleValue();
    }

    public double set(int index, double element) {
        return ((Number)getList().set(index,new Double(element))).doubleValue();
    }

    public DoubleList subList(int fromIndex, int toIndex) {
        return ListDoubleList.wrap(getList().subList(fromIndex,toIndex));
    }

    public boolean equals(Object obj) {
        if(obj instanceof DoubleList) {
            DoubleList that = (DoubleList)obj;
            if(this == that) {
                return true;
            } else if(this.size() != that.size()) {
                return false;            
            } else {
                DoubleIterator thisiter = iterator();
                DoubleIterator thatiter = that.iterator();
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
