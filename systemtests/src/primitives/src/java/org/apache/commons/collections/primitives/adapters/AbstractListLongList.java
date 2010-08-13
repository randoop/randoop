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

import org.apache.commons.collections.primitives.LongCollection;
import org.apache.commons.collections.primitives.LongIterator;
import org.apache.commons.collections.primitives.LongList;
import org.apache.commons.collections.primitives.LongListIterator;

/**
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractListLongList extends AbstractCollectionLongCollection implements LongList {

    public void add(int index, long element) {
        getList().add(index,new Long(element));
    }

    public boolean addAll(int index, LongCollection collection) {
        return getList().addAll(index,LongCollectionCollection.wrap(collection));
    }

    public long get(int index) {
        return ((Number)getList().get(index)).longValue();
    }

    public int indexOf(long element) {
        return getList().indexOf(new Long(element));
    }

    public int lastIndexOf(long element) {
        return getList().lastIndexOf(new Long(element));
    }

    /**
     * {@link ListIteratorLongListIterator#wrap wraps} the 
     * {@link LongList LongList} 
     * returned by my underlying 
     * {@link LongListIterator LongListIterator},
     * if any.
     */
    public LongListIterator listIterator() {
        return ListIteratorLongListIterator.wrap(getList().listIterator());
    }

    /**
     * {@link ListIteratorLongListIterator#wrap wraps} the 
     * {@link LongList LongList} 
     * returned by my underlying 
     * {@link LongListIterator LongListIterator},
     * if any.
     */
    public LongListIterator listIterator(int index) {
        return ListIteratorLongListIterator.wrap(getList().listIterator(index));
    }

    public long removeElementAt(int index) {
        return ((Number)getList().remove(index)).longValue();
    }

    public long set(int index, long element) {
        return ((Number)getList().set(index,new Long(element))).longValue();
    }

    public LongList subList(int fromIndex, int toIndex) {
        return ListLongList.wrap(getList().subList(fromIndex,toIndex));
    }

    public boolean equals(Object obj) {
        if(obj instanceof LongList) {
            LongList that = (LongList)obj;
            if(this == that) {
                return true;
            } else if(this.size() != that.size()) {
                return false;            
            } else {
                LongIterator thisiter = iterator();
                LongIterator thatiter = that.iterator();
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
