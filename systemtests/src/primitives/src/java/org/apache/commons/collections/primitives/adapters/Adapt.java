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

import org.apache.commons.collections.primitives.ByteCollection;
import org.apache.commons.collections.primitives.ByteIterator;
import org.apache.commons.collections.primitives.ByteList;
import org.apache.commons.collections.primitives.ByteListIterator;
import org.apache.commons.collections.primitives.CharCollection;
import org.apache.commons.collections.primitives.CharIterator;
import org.apache.commons.collections.primitives.CharList;
import org.apache.commons.collections.primitives.CharListIterator;
import org.apache.commons.collections.primitives.DoubleCollection;
import org.apache.commons.collections.primitives.DoubleIterator;
import org.apache.commons.collections.primitives.DoubleList;
import org.apache.commons.collections.primitives.DoubleListIterator;
import org.apache.commons.collections.primitives.FloatCollection;
import org.apache.commons.collections.primitives.FloatIterator;
import org.apache.commons.collections.primitives.FloatList;
import org.apache.commons.collections.primitives.FloatListIterator;
import org.apache.commons.collections.primitives.IntCollection;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.IntListIterator;
import org.apache.commons.collections.primitives.LongCollection;
import org.apache.commons.collections.primitives.LongIterator;
import org.apache.commons.collections.primitives.LongList;
import org.apache.commons.collections.primitives.LongListIterator;
import org.apache.commons.collections.primitives.ShortCollection;
import org.apache.commons.collections.primitives.ShortIterator;
import org.apache.commons.collections.primitives.ShortList;
import org.apache.commons.collections.primitives.ShortListIterator;

/**
 * Convenience methods for constructing adapters.
 *  
 * @since Commons Primitives 1.1
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
public final class Adapt {
    public Adapt() {
    }
    
    // to object based
    //---------------------------------------------------------------

    public static final Collection toCollection(ByteCollection c) {
        return ByteCollectionCollection.wrap(c);
    }
    
    public static final Collection toCollection(CharCollection c) {
        return CharCollectionCollection.wrap(c);
    }
    
    public static final Collection toCollection(DoubleCollection c) {
        return DoubleCollectionCollection.wrap(c);
    }
    
    public static final Collection toCollection(FloatCollection c) {
        return FloatCollectionCollection.wrap(c);
    }
    
    public static final Collection toCollection(IntCollection c) {
        return IntCollectionCollection.wrap(c);
    }
    
    public static final Collection toCollection(LongCollection c) {
        return LongCollectionCollection.wrap(c);
    }
    
    public static final Collection toCollection(ShortCollection c) {
        return ShortCollectionCollection.wrap(c);
    }

    public static final List toList(ByteList c) {
        return ByteListList.wrap(c);
    }
    
    public static final List toList(CharList c) {
        return CharListList.wrap(c);
    }
    
    public static final List toList(DoubleList c) {
        return DoubleListList.wrap(c);
    }
    
    public static final List toList(FloatList c) {
        return FloatListList.wrap(c);
    }
    
    public static final List toList(IntList c) {
        return IntListList.wrap(c);
    }
    
    public static final List toList(LongList c) {
        return LongListList.wrap(c);
    }
    
    public static final List toList(ShortList c) {
        return ShortListList.wrap(c);
    }

    public static final Iterator toIterator(ByteIterator c) {
        return ByteIteratorIterator.wrap(c);
    }
    
    public static final Iterator toIterator(CharIterator c) {
        return CharIteratorIterator.wrap(c);
    }
    
    public static final Iterator toIterator(DoubleIterator c) {
        return DoubleIteratorIterator.wrap(c);
    }
    
    public static final Iterator toIterator(FloatIterator c) {
        return FloatIteratorIterator.wrap(c);
    }
    
    public static final Iterator toIterator(IntIterator c) {
        return IntIteratorIterator.wrap(c);
    }
    
    public static final Iterator toIterator(LongIterator c) {
        return LongIteratorIterator.wrap(c);
    }
    
    public static final Iterator toIterator(ShortIterator c) {
        return ShortIteratorIterator.wrap(c);
    }

    public static final ListIterator toListIterator(ByteListIterator c) {
        return ByteListIteratorListIterator.wrap(c);
    }
    
    public static final ListIterator toListIterator(CharListIterator c) {
        return CharListIteratorListIterator.wrap(c);
    }
    
    public static final ListIterator toListIterator(DoubleListIterator c) {
        return DoubleListIteratorListIterator.wrap(c);
    }
    
    public static final ListIterator toListIterator(FloatListIterator c) {
        return FloatListIteratorListIterator.wrap(c);
    }
    
    public static final ListIterator toListIterator(IntListIterator c) {
        return IntListIteratorListIterator.wrap(c);
    }
    
    public static final ListIterator toListIterator(LongListIterator c) {
        return LongListIteratorListIterator.wrap(c);
    }
    
    public static final ListIterator toListIterator(ShortListIterator c) {
        return ShortListIteratorListIterator.wrap(c);
    }

    // to byte based
    //---------------------------------------------------------------
    
    public static final ByteCollection toByteCollection(Collection c) {
        return CollectionByteCollection.wrap(c);
    }

    public static final ByteList toByteList(List c) {
        return ListByteList.wrap(c);
    }

    public static final ByteIterator toByteIterator(Iterator c) {
        return IteratorByteIterator.wrap(c);
    }

    public static final ByteListIterator toByteListIterator(ListIterator c) {
        return ListIteratorByteListIterator.wrap(c);
    }

    // to char based
    //---------------------------------------------------------------
    
    public static final CharCollection toCharCollection(Collection c) {
        return CollectionCharCollection.wrap(c);
    }

    public static final CharList toCharList(List c) {
        return ListCharList.wrap(c);
    }

    public static final CharIterator toCharIterator(Iterator c) {
        return IteratorCharIterator.wrap(c);
    }

    public static final CharListIterator toCharListIterator(ListIterator c) {
        return ListIteratorCharListIterator.wrap(c);
    }

    // to double based
    //---------------------------------------------------------------
    
    public static final DoubleCollection toDoubleCollection(Collection c) {
        return CollectionDoubleCollection.wrap(c);
    }

    public static final DoubleList toDoubleList(List c) {
        return ListDoubleList.wrap(c);
    }

    public static final DoubleIterator toDoubleIterator(Iterator c) {
        return IteratorDoubleIterator.wrap(c);
    }

    public static final DoubleListIterator toDoubleListIterator(ListIterator c) {
        return ListIteratorDoubleListIterator.wrap(c);
    }
    
    // to float based
    //---------------------------------------------------------------
    
    public static final FloatCollection toFloatCollection(Collection c) {
        return CollectionFloatCollection.wrap(c);
    }

    public static final FloatList toFloatList(List c) {
        return ListFloatList.wrap(c);
    }

    public static final FloatIterator toFloatIterator(Iterator c) {
        return IteratorFloatIterator.wrap(c);
    }

    public static final FloatListIterator toFloatListIterator(ListIterator c) {
        return ListIteratorFloatListIterator.wrap(c);
    }
    
    // to int based
    //---------------------------------------------------------------
    
    public static final IntCollection toIntCollection(Collection c) {
        return CollectionIntCollection.wrap(c);
    }

    public static final IntList toIntList(List c) {
        return ListIntList.wrap(c);
    }

    public static final IntIterator toIntIterator(Iterator c) {
        return IteratorIntIterator.wrap(c);
    }

    public static final IntListIterator toIntListIterator(ListIterator c) {
        return ListIteratorIntListIterator.wrap(c);
    }
    
    // to long based
    //---------------------------------------------------------------
    
    public static final LongCollection toLongCollection(Collection c) {
        return CollectionLongCollection.wrap(c);
    }

    public static final LongList toLongList(List c) {
        return ListLongList.wrap(c);
    }

    public static final LongIterator toLongIterator(Iterator c) {
        return IteratorLongIterator.wrap(c);
    }

    public static final LongListIterator toLongListIterator(ListIterator c) {
        return ListIteratorLongListIterator.wrap(c);
    }
    
    // to short based
    //---------------------------------------------------------------
    
    public static final ShortCollection toShortCollection(Collection c) {
        return CollectionShortCollection.wrap(c);
    }

    public static final ShortList toShortList(List c) {
        return ListShortList.wrap(c);
    }

    public static final ShortIterator toShortIterator(Iterator c) {
        return IteratorShortIterator.wrap(c);
    }

    public static final ShortListIterator toShortListIterator(ListIterator c) {
        return ListIteratorShortListIterator.wrap(c);
    }
}