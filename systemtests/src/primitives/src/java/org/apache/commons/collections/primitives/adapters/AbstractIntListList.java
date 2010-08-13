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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.primitives.IntCollection;
import org.apache.commons.collections.primitives.IntList;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractIntListList extends AbstractIntCollectionCollection implements List {
    
    public void add(int index, Object element) {
        getIntList().add(index,((Number)element).intValue());
    }

    public boolean addAll(int index, Collection c) {
        return getIntList().addAll(index,CollectionIntCollection.wrap(c));
    }

    public Object get(int index) {
        return new Integer(getIntList().get(index));
    }

    public int indexOf(Object element) {
        return getIntList().indexOf(((Number)element).intValue());
    }

    public int lastIndexOf(Object element) {
        return getIntList().lastIndexOf(((Number)element).intValue());
    }

    /**
     * {@link IntListIteratorListIterator#wrap wraps} the 
     * {@link org.apache.commons.collections.primitives.IntListIterator IntListIterator}
     * returned by my underlying 
     * {@link IntList IntList}, 
     * if any.
     */
    public ListIterator listIterator() {
        return IntListIteratorListIterator.wrap(getIntList().listIterator());
    }

    /**
     * {@link IntListIteratorListIterator#wrap wraps} the 
     * {@link org.apache.commons.collections.primitives.IntListIterator IntListIterator}
     * returned by my underlying 
     * {@link IntList IntList}, 
     * if any.
     */
    public ListIterator listIterator(int index) {
        return IntListIteratorListIterator.wrap(getIntList().listIterator(index));
    }

    public Object remove(int index) {
        return new Integer(getIntList().removeElementAt(index));
    }

    public Object set(int index, Object element) {
        return new Integer(getIntList().set(index, ((Number)element).intValue() ));
    }

    public List subList(int fromIndex, int toIndex) {
        return IntListList.wrap(getIntList().subList(fromIndex,toIndex));
    }

    public boolean equals(Object obj) {
        if(obj instanceof List) {
            List that = (List)obj;
            if(this == that) {
                return true;
            } else if(this.size() != that.size()) {
                return false;            
            } else {
                Iterator thisiter = iterator();
                Iterator thatiter = that.iterator();
                while(thisiter.hasNext()) {
                    Object thiselt = thisiter.next();
                    Object thatelt = thatiter.next();
                    if(null == thiselt ? null != thatelt : !(thiselt.equals(thatelt))) {
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
        return getIntList().hashCode();
    }
    
    protected final IntCollection getIntCollection() {
        return getIntList();
    }
    
    protected abstract IntList getIntList();
        

}
