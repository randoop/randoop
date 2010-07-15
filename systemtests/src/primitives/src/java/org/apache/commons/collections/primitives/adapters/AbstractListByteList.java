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

import org.apache.commons.collections.primitives.ByteCollection;
import org.apache.commons.collections.primitives.ByteIterator;
import org.apache.commons.collections.primitives.ByteList;
import org.apache.commons.collections.primitives.ByteListIterator;

/**
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
abstract class AbstractListByteList extends AbstractCollectionByteCollection implements ByteList {

    public void add(int index, byte element) {
        getList().add(index,new Byte(element));
    }

    public boolean addAll(int index, ByteCollection collection) {
        return getList().addAll(index,ByteCollectionCollection.wrap(collection));
    }

    public byte get(int index) {
        return ((Number)getList().get(index)).byteValue();
    }

    public int indexOf(byte element) {
        return getList().indexOf(new Byte(element));
    }

    public int lastIndexOf(byte element) {
        return getList().lastIndexOf(new Byte(element));
    }

    /**
     * {@link ListIteratorByteListIterator#wrap wraps} the 
     * {@link ByteList ByteList} 
     * returned by my underlying 
     * {@link ByteListIterator ByteListIterator},
     * if any.
     */
    public ByteListIterator listIterator() {
        return ListIteratorByteListIterator.wrap(getList().listIterator());
    }

    /**
     * {@link ListIteratorByteListIterator#wrap wraps} the 
     * {@link ByteList ByteList} 
     * returned by my underlying 
     * {@link ByteListIterator ByteListIterator},
     * if any.
     */
    public ByteListIterator listIterator(int index) {
        return ListIteratorByteListIterator.wrap(getList().listIterator(index));
    }

    public byte removeElementAt(int index) {
        return ((Number)getList().remove(index)).byteValue();
    }

    public byte set(int index, byte element) {
        return ((Number)getList().set(index,new Byte(element))).byteValue();
    }

    public ByteList subList(int fromIndex, int toIndex) {
        return ListByteList.wrap(getList().subList(fromIndex,toIndex));
    }

    public boolean equals(Object obj) {
        if(obj instanceof ByteList) {
            ByteList that = (ByteList)obj;
            if(this == that) {
                return true;
            } else if(this.size() != that.size()) {
                return false;            
            } else {
                ByteIterator thisiter = iterator();
                ByteIterator thatiter = that.iterator();
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
