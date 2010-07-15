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

import org.apache.commons.collections.primitives.FloatCollection;
import org.apache.commons.collections.primitives.FloatList;

/**
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractFloatListList extends AbstractFloatCollectionCollection implements List {
    
    public void add(int index, Object element) {
        getFloatList().add(index,((Number)element).floatValue());
    }

    public boolean addAll(int index, Collection c) {
        return getFloatList().addAll(index,CollectionFloatCollection.wrap(c));
    }

    public Object get(int index) {
        return new Float(getFloatList().get(index));
    }

    public int indexOf(Object element) {
        return getFloatList().indexOf(((Number)element).floatValue());
    }

    public int lastIndexOf(Object element) {
        return getFloatList().lastIndexOf(((Number)element).floatValue());
    }

    /**
     * {@link FloatListIteratorListIterator#wrap wraps} the 
     * {@link org.apache.commons.collections.primitives.FloatListIterator FloatListIterator}
     * returned by my underlying 
     * {@link FloatList FloatList}, 
     * if any.
     */
    public ListIterator listIterator() {
        return FloatListIteratorListIterator.wrap(getFloatList().listIterator());
    }

    /**
     * {@link FloatListIteratorListIterator#wrap wraps} the 
     * {@link org.apache.commons.collections.primitives.FloatListIterator FloatListIterator}
     * returned by my underlying 
     * {@link FloatList FloatList}, 
     * if any.
     */
    public ListIterator listIterator(int index) {
        return FloatListIteratorListIterator.wrap(getFloatList().listIterator(index));
    }

    public Object remove(int index) {
        return new Float(getFloatList().removeElementAt(index));
    }

    public Object set(int index, Object element) {
        return new Float(getFloatList().set(index, ((Number)element).floatValue() ));
    }

    public List subList(int fromIndex, int toIndex) {
        return FloatListList.wrap(getFloatList().subList(fromIndex,toIndex));
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
        return getFloatList().hashCode();
    }
    
    protected final FloatCollection getFloatCollection() {
        return getFloatList();
    }
    
    protected abstract FloatList getFloatList();
        

}
