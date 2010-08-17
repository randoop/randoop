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

import org.apache.commons.collections.primitives.decorators.UnmodifiableCharIterator;
import org.apache.commons.collections.primitives.decorators.UnmodifiableCharList;
import org.apache.commons.collections.primitives.decorators.UnmodifiableCharListIterator;

/**
 * This class consists exclusively of static methods that operate on or
 * return CharCollections.
 * <p>
 * The methods of this class all throw a NullPoCharerException if the 
 * provided collection is null.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:25 $
 * 
 * @author Rodney Waldhoff 
 */
public final class CharCollections {

    /**
     * Returns an unmodifiable CharList containing only the specified element.
     * @param value the single value
     * @return an unmodifiable CharList containing only the specified element.
     */    
    public static CharList singletonCharList(char value) {
        // TODO: a specialized implementation of CharList may be more performant
        CharList list = new ArrayCharList(1);
        list.add(value);
        return UnmodifiableCharList.wrap(list);
    }

    /**
     * Returns an unmodifiable CharIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable CharIterator containing only the specified element.
     */    
    public static CharIterator singletonCharIterator(char value) {
        return singletonCharList(value).iterator();
    }

    /**
     * Returns an unmodifiable CharListIterator containing only the specified element.
     * @param value the single value
     * @return an unmodifiable CharListIterator containing only the specified element.
     */    
    public static CharListIterator singletonCharListIterator(char value) {
        return singletonCharList(value).listIterator();
    }

    /**
     * Returns an unmodifiable version of the given non-null CharList.
     * @param list the non-null CharList to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null CharList
     * @throws NullPoCharerException if the given CharList is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableCharList#wrap
     */    
    public static CharList unmodifiableCharList(CharList list) throws NullPointerException {
        if(null == list) {
            throw new NullPointerException();
        }
        return UnmodifiableCharList.wrap(list);
    }
    
    /**
     * Returns an unmodifiable version of the given non-null CharIterator.
     * @param iter the non-null CharIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null CharIterator
     * @throws NullPoCharerException if the given CharIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableCharIterator#wrap
     */    
    public static CharIterator unmodifiableCharIterator(CharIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableCharIterator.wrap(iter);
    }
        
    /**
     * Returns an unmodifiable version of the given non-null CharListIterator.
     * @param iter the non-null CharListIterator to wrap in an unmodifiable decorator
     * @return an unmodifiable version of the given non-null CharListIterator
     * @throws NullPoCharerException if the given CharListIterator is null
     * @see org.apache.commons.collections.primitives.decorators.UnmodifiableCharListIterator#wrap
     */    
    public static CharListIterator unmodifiableCharListIterator(CharListIterator iter) {
        if(null == iter) {
            throw new NullPointerException();
        }
        return UnmodifiableCharListIterator.wrap(iter);
    }
    
    /**
     * Returns an unmodifiable, empty CharList.
     * @return an unmodifiable, empty CharList.
     * @see #EMPTY_CHAR_LIST
     */    
    public static CharList getEmptyCharList() {
        return EMPTY_CHAR_LIST;
    }
    
    /**
     * Returns an unmodifiable, empty CharIterator
     * @return an unmodifiable, empty CharIterator.
     * @see #EMPTY_CHAR_ITERATOR
     */    
    public static CharIterator getEmptyCharIterator() {
        return EMPTY_CHAR_ITERATOR;
    }
    
    /**
     * Returns an unmodifiable, empty CharListIterator
     * @return an unmodifiable, empty CharListIterator.
     * @see #EMPTY_CHAR_LIST_ITERATOR
     */    
    public static CharListIterator getEmptyCharListIterator() {
        return EMPTY_CHAR_LIST_ITERATOR;
    }    

    /**
     * An unmodifiable, empty CharList
     * @see #getEmptyCharList
     */    
    public static final CharList EMPTY_CHAR_LIST = unmodifiableCharList(new ArrayCharList(0));

    /**
     * An unmodifiable, empty CharIterator
     * @see #getEmptyCharIterator
     */    
    public static final CharIterator EMPTY_CHAR_ITERATOR = unmodifiableCharIterator(EMPTY_CHAR_LIST.iterator());

    /**
     * An unmodifiable, empty CharListIterator
     * @see #getEmptyCharListIterator
     */    
    public static final CharListIterator EMPTY_CHAR_LIST_ITERATOR = unmodifiableCharListIterator(EMPTY_CHAR_LIST.listIterator());
}
