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

import org.apache.commons.collections.primitives.BooleanCollection;
import org.apache.commons.collections.primitives.BooleanList;

/**
 * @since Commons Primitives 1.1
 * @version $Revision: 1.1 $ $Date: 2004/07/12 18:29:43 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractBooleanListList extends AbstractBooleanCollectionCollection implements List {
    
    public void add(int index, Object element) {
        getBooleanList().add(index,((Boolean)element).booleanValue());
    }

    public boolean addAll(int index, Collection c) {
        return getBooleanList().addAll(index,CollectionBooleanCollection.wrap(c));
    }

    public Object get(int index) {
        return new Boolean(getBooleanList().get(index));
    }

    public int indexOf(Object element) {
        return getBooleanList().indexOf(((Boolean)element).booleanValue());
    }

    public int lastIndexOf(Object element) {
        return getBooleanList().lastIndexOf(((Boolean)element).booleanValue());
    }

    /**
     * {@link BooleanListIteratorListIterator#wrap wraps} the 
     * {@link org.apache.commons.collections.primitives.BooleanListIterator BooleanListIterator}
     * returned by my underlying 
     * {@link BooleanList BooleanList}, 
     * if any.
     */
    public ListIterator listIterator() {
        return BooleanListIteratorListIterator.wrap(getBooleanList().listIterator());
    }

    /**
     * {@link BooleanListIteratorListIterator#wrap wraps} the 
     * {@link org.apache.commons.collections.primitives.BooleanListIterator BooleanListIterator}
     * returned by my underlying 
     * {@link BooleanList BooleanList}, 
     * if any.
     */
    public ListIterator listIterator(int index) {
        return BooleanListIteratorListIterator.wrap(getBooleanList().listIterator(index));
    }

    public Object remove(int index) {
        return new Boolean(getBooleanList().removeElementAt(index));
    }

    public Object set(int index, Object element) {
        return new Boolean(getBooleanList().set(index, ((Boolean)element).booleanValue() ));
    }

    public List subList(int fromIndex, int toIndex) {
        return BooleanListList.wrap(getBooleanList().subList(fromIndex,toIndex));
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
        return getBooleanList().hashCode();
    }
    
    protected final BooleanCollection getBooleanCollection() {
        return getBooleanList();
    }
    
    protected abstract BooleanList getBooleanList();
        

}
