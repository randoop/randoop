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

import org.apache.commons.collections.primitives.decorators.UnmodifiableLongIterator;
import org.apache.commons.collections.primitives.decorators.UnmodifiableLongList;
import org.apache.commons.collections.primitives.decorators.UnmodifiableLongListIterator;

/**
 * This class consists exclusively of static methods that operate on or
 * return LongCollections.
 * <p>
 * The methods of this class all throw a NullPoLongerException if the 
 * provided collection is null.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:25 $
 * 
 * @author Rodney Waldhoff 
 */
public final class LongCollections {

    /**
     * Returns an unmodifiable LongList containing only the specified element.
     * @param value the single value
     * @return an unmodifiable LongList containing only the specified element.
     */    
    public static LongList singletonLongList(long value) {
        // TODO: a specialized implementation of LongList may be more performant
        LongList list = new ArrayLongList(1);
        list.add(value);
        return UnmodifiableLongList.wrap(list);
    }

    /**
     * Returns an unmodifiable LongIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable LongIterator containing only the specified element.
     */    
    public static LongIterator singletonLongIterator(long value) {
        return singletonLongList(value).iterator();
    }

    /**
     * Returns an unmodifiable LongListIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable LongListIterator containing only the specified element.
     */    
    public static LongListIterator singletonLongListIterator(long value) {
        return singletonLongList(value).listIterator();
    }

    /**
     * Returns an unmodifiable version of the given non-null LongList.
     * @param list the non-null LongList to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null LongList
     * @throws NullPoLongerException if the given LongList is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableLongList#wrap
     */    
    public static LongList unmodifiableLongList(LongList list) throws NullPointerException {
        if(null == list) {
            throw new NullPointerException();
        }
        return UnmodifiableLongList.wrap(list);
    }
    
    /**
     * Returns an unmodifiable version of the given non-null LongIterator.
     * @param iter the non-null LongIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null LongIterator
     * @throws NullPoLongerException if the given LongIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableLongIterator#wrap
     */    
    public static LongIterator unmodifiableLongIterator(LongIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableLongIterator.wrap(iter);
    }
        
    /**
     * Returns an unmodifiable version of the given non-null LongListIterator.
     * @param iter the non-null LongListIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null LongListIterator
     * @throws NullPoLongerException if the given LongListIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableLongListIterator#wrap
     */    
    public static LongListIterator unmodifiableLongListIterator(LongListIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableLongListIterator.wrap(iter);
    }
    
    /**
     * Returns an unmodifiable, empty LongList.
     * @return an unmodifiable, empty LongList.
     * @see #EMPTY_LONG_LIST
     */    
    public static LongList getEmptyLongList() {
        return EMPTY_LONG_LIST;
    }
    
    /**
     * Returns an unmodifiable, empty LongIterator
     * @return an unmodifiable, empty LongIterator.
     * @see #EMPTY_LONG_ITERATOR
     */    
    public static LongIterator getEmptyLongIterator() {
        return EMPTY_LONG_ITERATOR;
    }
    
    /**
     * Returns an unmodifiable, empty LongListIterator
     * @return an unmodifiable, empty LongListIterator.
     * @see #EMPTY_LONG_LIST_ITERATOR
     */    
    public static LongListIterator getEmptyLongListIterator() {
        return EMPTY_LONG_LIST_ITERATOR;
    }    

    /**
     * An unmodifiable, empty LongList
     * @see #getEmptyLongList
     */    
    public static final LongList EMPTY_LONG_LIST = unmodifiableLongList(new ArrayLongList(0));

    /**
     * An unmodifiable, empty LongIterator
     * @see #getEmptyLongIterator
     */    
    public static final LongIterator EMPTY_LONG_ITERATOR = unmodifiableLongIterator(EMPTY_LONG_LIST.iterator());

    /**
     * An unmodifiable, empty LongListIterator
     * @see #getEmptyLongListIterator
     */    
    public static final LongListIterator EMPTY_LONG_LIST_ITERATOR = unmodifiableLongListIterator(EMPTY_LONG_LIST.listIterator());
}
