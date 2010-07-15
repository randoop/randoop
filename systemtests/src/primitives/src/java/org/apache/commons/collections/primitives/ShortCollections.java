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
package org.apache.commons.collections.primitives;

import org.apache.commons.collections.primitives.decorators.UnmodifiableShortIterator;
import org.apache.commons.collections.primitives.decorators.UnmodifiableShortList;
import org.apache.commons.collections.primitives.decorators.UnmodifiableShortListIterator;

/**
 * This class consists exclusively of static methods that operate on or
 * return ShortCollections.
 * <p>
 * The methods of this class all throw a NullPoShorterException if the 
 * provided collection is null.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:25 $
 * 
 * @author Rodney Waldhoff 
 */
public final class ShortCollections {

    /**
     * Returns an unmodifiable ShortList containing only the specified element.
     * @param value the single value
     * @return an unmodifiable ShortList containing only the specified element.
     */    
    public static ShortList singletonShortList(short value) {
        // TODO: a specialized implementation of ShortList may be more performant
        ShortList list = new ArrayShortList(1);
        list.add(value);
        return UnmodifiableShortList.wrap(list);
    }

    /**
     * Returns an unmodifiable ShortIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable ShortIterator containing only the specified element.
     */    
    public static ShortIterator singletonShortIterator(short value) {
        return singletonShortList(value).iterator();
    }

    /**
     * Returns an unmodifiable ShortListIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable ShortListIterator containing only the specified element.
     */    
    public static ShortListIterator singletonShortListIterator(short value) {
        return singletonShortList(value).listIterator();
    }

    /**
     * Returns an unmodifiable version of the given non-null ShortList.
     * @param list the non-null ShortList to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null ShortList
     * @throws NullPoShorterException if the given ShortList is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableShortList#wrap
     */    
    public static ShortList unmodifiableShortList(ShortList list) throws NullPointerException {
        if(null == list) {
            throw new NullPointerException();
        }
        return UnmodifiableShortList.wrap(list);
    }
    
    /**
     * Returns an unmodifiable version of the given non-null ShortIterator.
     * @param iter the non-null ShortIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null ShortIterator
     * @throws NullPoShorterException if the given ShortIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableShortIterator#wrap
     */    
    public static ShortIterator unmodifiableShortIterator(ShortIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableShortIterator.wrap(iter);
    }
        
    /**
     * Returns an unmodifiable version of the given non-null ShortListIterator.
     * @param iter the non-null ShortListIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null ShortListIterator
     * @throws NullPoShorterException if the given ShortListIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableShortListIterator#wrap
     */    
    public static ShortListIterator unmodifiableShortListIterator(ShortListIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableShortListIterator.wrap(iter);
    }
    
    /**
     * Returns an unmodifiable, empty ShortList.
     * @return an unmodifiable, empty ShortList.
     * @see #EMPTY_SHORT_LIST
     */    
    public static ShortList getEmptyShortList() {
        return EMPTY_SHORT_LIST;
    }
    
    /**
     * Returns an unmodifiable, empty ShortIterator
     * @return an unmodifiable, empty ShortIterator.
     * @see #EMPTY_SHORT_ITERATOR
     */    
    public static ShortIterator getEmptyShortIterator() {
        return EMPTY_SHORT_ITERATOR;
    }
    
    /**
     * Returns an unmodifiable, empty ShortListIterator
     * @return an unmodifiable, empty ShortListIterator.
     * @see #EMPTY_SHORT_LIST_ITERATOR
     */    
    public static ShortListIterator getEmptyShortListIterator() {
        return EMPTY_SHORT_LIST_ITERATOR;
    }    

    /**
     * An unmodifiable, empty ShortList
     * @see #getEmptyShortList
     */    
    public static final ShortList EMPTY_SHORT_LIST = unmodifiableShortList(new ArrayShortList(0));

    /**
     * An unmodifiable, empty ShortIterator
     * @see #getEmptyShortIterator
     */    
    public static final ShortIterator EMPTY_SHORT_ITERATOR = unmodifiableShortIterator(EMPTY_SHORT_LIST.iterator());

    /**
     * An unmodifiable, empty ShortListIterator
     * @see #getEmptyShortListIterator
     */    
    public static final ShortListIterator EMPTY_SHORT_LIST_ITERATOR = unmodifiableShortListIterator(EMPTY_SHORT_LIST.listIterator());
}
