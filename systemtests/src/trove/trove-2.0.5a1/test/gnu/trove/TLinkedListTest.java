///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package gnu.trove;

import junit.framework.*;
import java.io.*;
import java.util.*;

/**
 *
 * Created: Sat Nov 10 15:57:07 2001
 *
 * @author Eric D. Friedman
 * @version $Id: TLinkedListTest.java,v 1.6 2008/05/05 22:49:19 robeden Exp $
 */

public class TLinkedListTest extends TestCase {
    protected TLinkedList<Data> list;
    
    public TLinkedListTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        list = new TLinkedList<Data>();
    }

    public void tearDown() throws Exception {
        list = null;
    }

    public void testAdd() throws Exception {
        Data[] data = { new Data(1), new Data(2), new Data(3) };
        for (int i = 0; i < data.length; i++) {
            list.add(data[i]);
        }
        assertEquals(3,list.size());
    }

    public void testInsert() throws Exception {
        Data[] data = { new Data(2), new Data(4), new Data(6) };
        for (int i = 0; i < data.length; i++) {
            list.add(data[i]);
        }

        list.insert( 0, new Data( 1 ) );
        list.insert( 2, new Data( 3 ) );
        list.insert( 4, new Data( 5 ) );
        list.insert( list.size(), new Data( 7 ) );

        assertEquals( 7, list.size() );
        for( int i = 0; i < list.size(); i++ ) {
            assertEquals(i + 1, list.get( i )._val );
        }
    }

    public void testNextIterator() throws Exception {
        Data[] data = new Data[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Data(i);
            list.add(data[i]);
        }

        int count = 0;
        for (Iterator i = list.iterator(); i.hasNext();) {
            assertEquals(data[count++], i.next());
        }
        assertEquals(data.length,count);

        count = 4;
        for (Iterator i = list.listIterator(4); i.hasNext();) {
            assertEquals(data[count++], i.next());
        }
        assertEquals(data.length,count);
    }

    public void testPreviousIterator() throws Exception {
        Data[] data = new Data[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Data(i);
            list.add(data[i]);
        }

        int count = 100;
        for (ListIterator i = list.listIterator(list.size());
             i.hasPrevious();) {
            assertEquals(data[--count], i.previous());
        }
        assertEquals(0,count);

        count = 5;
        for (ListIterator i = list.listIterator(count); i.hasPrevious();) {
            assertEquals(data[--count], i.previous());
        }
        assertEquals(0,count);
    }

    public void testIteratorSet() throws Exception {
        Data[] data = new Data[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Data(i);
            list.add(data[i]);
        }

        ListIterator i;

        i = list.listIterator(5);
        i.next();
        Data d = new Data(999);
        i.set(d);
        assertEquals(d, list.get(5));
    }

    public void testRemoveOnlyElementInList() throws Exception {
        Data d = new Data(0);
        list.add(d);
        ListIterator i = list.listIterator();
        assertTrue(i.hasNext());
        assertEquals(d, i.next());
        i.remove();
        assertTrue(! i.hasNext());
        assertTrue(! i.hasPrevious());
        assertEquals(0, list.size());
    }

    public void testRemovePrevious() throws Exception {
        Data[] d = { new Data(0), new Data(1), new Data(2) };
        list.addAll(Arrays.asList(d));

        ListIterator i = list.listIterator(list.size());
        i.previous();
        i.previous();
        i.remove();
        assertEquals(2, list.size());
        assertTrue(i.hasPrevious());
        assertEquals(d[0], i.previous());
        assertTrue(! i.hasPrevious());
        assertTrue(i.hasNext());
        assertEquals(d[0], i.next());
        assertTrue(i.hasNext());
        assertEquals(d[2], i.next());
        assertTrue(! i.hasNext());
        assertTrue(i.hasPrevious());
        assertEquals(2, list.size());
    }

    public void testRemoveLast() throws Exception {
        Data[] d = { new Data(0), new Data(1), new Data(2) };
        list.addAll(Arrays.asList(d));

        ListIterator i = list.listIterator(list.size());
        i.previous();
        i.remove();
        assertEquals(2, list.size());
        assertTrue(i.hasPrevious());
        assertTrue(! i.hasNext());
    }

    public void testRemoveFirst() throws Exception {
        Data[] d = { new Data(0), new Data(1), new Data(2) };
        list.addAll(Arrays.asList(d));

        ListIterator i = list.listIterator(0);
        i.next();
        i.remove();
        assertEquals(2, list.size());
        assertTrue(! i.hasPrevious());
        assertTrue(i.hasNext());
    }
    
    public void testRemoveNext() throws Exception {
        Data[] d = { new Data(0), new Data(1), new Data(2) };
        list.addAll(Arrays.asList(d));

        ListIterator i = list.listIterator();
        assertTrue(i.hasNext());
        i.next();
        assertTrue(i.hasNext());
        assertTrue(i.hasPrevious());
        i.remove();
        assertEquals(2, list.size());
        assertTrue(! i.hasPrevious());
        assertTrue(i.hasNext());
        assertEquals(d[1], i.next());
        assertTrue(i.hasNext());
        assertEquals(d[2], i.next());
        assertTrue(i.hasPrevious());
        assertTrue(! i.hasNext());
    }

    public void testRemoveThrowsAfterAdd() throws Exception {
        Data d = new Data(0);
        list.add(d);
        ListIterator i = list.listIterator();
        boolean didThrow = false;
        
        try {
            i.remove();
        } catch (IllegalStateException e) {
            didThrow = true;
        } // end of try-catch
        assertTrue(didThrow);
    }

    public void testRemoveThrowsWithoutPrevious() throws Exception {
        Data d = new Data(0);
        list.add(d);
        ListIterator i = list.listIterator(list.size());
        boolean didThrow = false;

        assertTrue(i.hasPrevious());
        try {
            i.remove();
        } catch (IllegalStateException e) {
            didThrow = true;
        } // end of try-catch
        assertTrue(didThrow);
    }

    public void testRemoveThrowsWithoutNext() throws Exception {
        Data d = new Data(0);
        list.add(d);
        ListIterator i = list.listIterator();
        boolean didThrow = false;

        assertTrue(i.hasNext());
        try {
            i.remove();
        } catch (IllegalStateException e) {
            didThrow = true;
        } // end of try-catch
        assertTrue(didThrow);
    }

    public void testIteratorAddFront() throws Exception {
        Data[] d = { new Data(0), new Data(1), new Data(2) };
        list.addAll(Arrays.asList(d));

        ListIterator i = list.listIterator();
        Data d1 = new Data(5);
        assertTrue(! i.hasPrevious());
        i.add(d1);
        assertTrue(i.hasPrevious());
        assertEquals(d1, i.previous());
        assertEquals(d1, i.next());
        assertEquals(d[0], i.next());
        assertEquals(d1, list.get(0));
    }

    public void testIteratorAddBack() throws Exception {
        Data[] d = { new Data(0), new Data(1), new Data(2) };
        list.addAll(Arrays.asList(d));

        ListIterator i = list.listIterator(list.size());
        Data d1 = new Data(5);
        assertEquals(3, list.size());
        assertTrue(i.hasPrevious());
        assertTrue(! i.hasNext());
        i.add(d1);
        assertTrue(i.hasPrevious());
        assertTrue(! i.hasNext());
        assertEquals(4, list.size());
        
        assertEquals(d1, i.previous());
        assertEquals(d1, i.next());
        assertEquals(d1, list.get(3));
    }

    public void testIteratorAddMiddle() throws Exception {
        Data[] d = { new Data(0), new Data(1), new Data(2) };
        list.addAll(Arrays.asList(d));

        ListIterator i = list.listIterator(1);
        Data d1 = new Data(5);
        assertEquals(3, list.size());
        assertTrue(i.hasPrevious());
        assertTrue(i.hasNext());
        i.add(d1);
        assertTrue(i.hasPrevious());
        assertTrue(i.hasNext());
        assertEquals(4, list.size());
        
        assertEquals(d1, i.previous());
        assertEquals(d1, i.next());
        assertEquals(d1, list.get(1));
    }

    public void testIteratorSetSingleElementList() throws Exception {
        Data d1 = new Data(5);
        Data d2 = new Data(4);
        list.add(d1);

        ListIterator i = list.listIterator(0);
        i.next();
        i.set(d2);
        assertEquals(1, list.size());
        assertTrue(! i.hasNext());
        assertTrue(i.hasPrevious());
        assertEquals(d2, i.previous());
        assertTrue(i.hasNext());
        assertTrue(! i.hasPrevious());
        assertEquals(d2, i.next());
    }

    public void testIteratorAddEmptyList() throws Exception {
        ListIterator i = list.listIterator();
        Data d1 = new Data(5);
        assertTrue(! i.hasPrevious());
        assertTrue(! i.hasNext());
        i.add(d1);
        assertTrue(i.hasPrevious());
        assertTrue(! i.hasNext());
        assertEquals(d1, i.previous());
        assertEquals(d1, i.next());
        assertEquals(d1, list.get(0));
    }

    public void testIteratorRemoveOnNext() throws Exception {
        Data[] data = new Data[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Data(i);
            list.add(data[i]);
        }

        ListIterator i;

        i = list.listIterator(5);
        i.next();
        i.remove();
        Data d = new Data(6);
        assertEquals(d, list.get(5));
    }

    public void testSerialize() throws Exception {
        TLinkedList list1 = new TLinkedList();
        Data[] data = new Data[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Data(i);
            list1.add(data[i]);
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(list1);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        TLinkedList list2 = (TLinkedList)ois.readObject();
        assertEquals(list1, list2);
    }

    public void testForEach() throws Exception {
        list.add( new Data( 0 ) );
        list.add( new Data( 1 ) );
        list.add( new Data( 2 ) );
        list.add( new Data( 3 ) );
        list.add( new Data( 4 ) );

        // test exiting early
        boolean processed_full_list = list.forEachValue(new TObjectProcedure<Data>() {
            public boolean execute(Data object) {
                if ( object._val == 2 ) return false;

                object._val++;

                return true;
            }
        } );
        assertFalse( processed_full_list );

        assertEquals( 1, list.get( 0 )._val );
        assertEquals( 2, list.get( 1 )._val );
        assertEquals( 2, list.get( 2 )._val );
        assertEquals( 3, list.get( 3 )._val );
        assertEquals( 4, list.get( 4 )._val );

        // test full list processing
        processed_full_list = list.forEachValue(new TObjectProcedure<Data>() {
            public boolean execute(Data object) {
                object._val++;
                return true;
            }
        } );
        assertTrue( processed_full_list );

        assertEquals( 2, list.get( 0 )._val );
        assertEquals( 3, list.get( 1 )._val );
        assertEquals( 3, list.get( 2 )._val );
        assertEquals( 4, list.get( 3 )._val );
        assertEquals( 5, list.get( 4 )._val );
    }

    public void testAddBefore() {
        Data one = new Data( 1 );
        Data three = new Data( 3 );
        Data four = new Data( 4 );
        Data five = new Data( 5 );

        list.add( one );
        list.add( three );
        list.add( four );
        list.add( five );

        list.addBefore( one, new Data( 0 ) );
        list.addBefore( three, new Data( 2 ) );
        list.addBefore( null, new Data( 6 ) );

        System.out.println("List: " + list);

        // Iterate forward
        int value = -1;
        Data cur = list.getFirst();
        while( cur != null ) {
            assertEquals( value + 1, cur._val );
            value = cur._val;

            cur = cur.getNext();
        }

        assertEquals( 6, value );

        // Iterate backward
        value = 7;
        cur = list.getLast();
        while( cur != null ) {
            assertEquals( value - 1, cur._val );
            value = cur._val;

            cur = cur.getPrevious();
        }

        assertEquals( 0, value );
    }

    public void testAddAfter() {
        Data one = new Data( 1 );
        Data three = new Data( 3 );
        Data five = new Data( 5 );

        list.add( one );
        list.add( three );
        list.add( five );

        list.addAfter( one, new Data( 2 ) );
        list.addAfter( three, new Data( 4 ) );
        list.addAfter( five, new Data( 6 ) );
        list.addAfter( null, new Data( 0 ) );

        System.out.println("List: " + list);

        // Iterate forward
        int value = -1;
        Data cur = list.getFirst();
        while( cur != null ) {
            assertEquals( value + 1, cur._val );
            value = cur._val;

            cur = cur.getNext();
        }

        assertEquals( 6, value );

        // Iterate backward
        value = 7;
        cur = list.getLast();
        while( cur != null ) {
            System.out.println("Itr back: " + cur._val);
            assertEquals( value - 1, cur._val );
            value = cur._val;

            cur = cur.getPrevious();
        }

        assertEquals( 0, value );
    }

    public void testPastIndexGet() {
        try {
            list.get( 0 );
            fail( "Shouldn't have allowed get of index 0" );
        }
        catch( IndexOutOfBoundsException ex ) {
            // this is good
        }

        try {
            list.get( 1 );
            fail( "Shouldn't have allowed get of index 1" );
        }
        catch( IndexOutOfBoundsException ex ) {
            // this is good
        }

        list.add( new Data( 1 ) );
        list.get( 0 );

        try {
            list.get( 1 );
            fail( "Shouldn't have allowed get of index 1" );
        }
        catch( IndexOutOfBoundsException ex ) {
            // this is good
        }
    }


    static class Data implements TLinkable {
        protected int _val;
        
        public Data(int val) {
            _val = val;
        }
        
        protected TLinkable _next;

        // NOTE: use covariant overriding
        /**
         * Get the value of next.
         * @return value of next.
         */
        public Data getNext() {
            return ( Data ) _next;
        }
        
        /**
         * Set the value of next.
         * @param next value to assign to next.
         */
        public void setNext(TLinkable next) {
            this._next = next;
        }
        protected TLinkable _previous;

        // NOTE: use covariant overriding
        /**
         * Get the value of previous.
         * @return value of previous.
         */
        public Data getPrevious() {
            return ( Data ) _previous;
        }
        
        /**
         * Set the value of previous.
         * @param previous value to assign to previous.
         */
        public void setPrevious(TLinkable previous) {
            this._previous = previous;
        }

        public String toString() {
            return "" + _val;
        }

        public boolean equals(Object o) {
            Data that = (Data)o;
            return this._val == that._val;
        }
    }
    
} // TLinkedListTests
