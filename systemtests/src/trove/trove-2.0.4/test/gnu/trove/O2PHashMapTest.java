///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001-2006, Eric D. Friedman All Rights Reserved.
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

import gnu.trove.decorator.TObjectIntHashMapDecorator;
import junit.framework.TestCase;

import java.util.*;


/**
 *
 */
public class O2PHashMapTest extends TestCase {

    public O2PHashMapTest( String name ) {
        super( name );
    }


    public void testKeys() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );
        map.put( "two", 2 );

        assertEquals( 2, map.size() );

        String[] keys = map.keys( new String[ map.size() ] );
        assertEquals( 2, keys.length );
        List<String> keys_list = Arrays.asList( keys );
        
        assertTrue( keys_list.contains( "one" ) );
        assertTrue( keys_list.contains( "two" ) );

        Object[] keys2 = map.keys();
        assertEquals( 2, keys2.length );
        List keys_list2 = Arrays.asList( keys2 );

        assertTrue( keys_list2.contains( "one" ) );
        assertTrue( keys_list2.contains( "two" ) );
    }


    public void testDecorator() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );
        map.put( "two", 2 );

        Map<String,Integer> decorator = new TObjectIntHashMapDecorator<String>( map );

        assertEquals( 2, decorator.size() );
        assertEquals( Integer.valueOf( 1 ), decorator.get( "one" ) );
        assertEquals( Integer.valueOf( 2 ), decorator.get( "two" ) );

        Set<String> decorator_keys = decorator.keySet();
        assertEquals( 2, decorator_keys.size() );
        Iterator<String> it = decorator_keys.iterator();
        int count = 0;
        while( it.hasNext() ) {
            count++;
            System.out.println(it.next());
        }
        assertEquals( 2, count );

        assertSame(map, ( ( TObjectIntHashMapDecorator ) decorator ).getMap() );
    }


    public void testIterator() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        TObjectIntIterator<String> iterator = map.iterator();
        assertFalse( iterator.hasNext() );

        map.put( "one", 1 );
        map.put( "two", 2 );

        iterator = map.iterator();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertFalse( iterator.hasNext() );
    }


    public void testIteratorRemoval() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );
        map.put( "two", 2 );
        map.put( "three", 3 );
        map.put( "four", 4 );
        map.put( "five", 5 );
        map.put( "six", 6 );
        map.put( "seven", 7 );
        map.put( "eight", 8 );
        map.put( "nine", 9 );
        map.put( "ten", 10 );

        TObjectIntIterator<String> iterator = map.iterator();
        while( map.size() > 5 && iterator.hasNext() ) {
            iterator.advance();
            iterator.remove();
        }

        assertEquals( 5, map.size() );
        iterator = map.iterator();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertFalse( iterator.hasNext() );

        iterator = map.iterator();
        while( iterator.hasNext() ) {
            iterator.advance();
            iterator.remove();
        }

        assertEquals( 0, map.size() );
    }


    public void testIteratorRemoval2() {
        TIntObjectHashMap<String> map = new TIntObjectHashMap<String>(10000, 0.5f);

        for( int pass = 0; pass < 10; pass++ ) {
            System.out.println("Test");
            Random r = new Random();
            System.out.println("ADD");
            for (int i = 0; i <= 10000; i++) {
                map.put(r.nextInt(), "Test" + i);
            }

            System.out.println("REMOVE");
            TIntObjectIterator iterator = map.iterator();
            while (map.size() > 5000 && iterator.hasNext()) {
                iterator.advance();
                iterator.remove();
            }
        }
    }


    public void testAdjustValue() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );

        boolean changed = map.adjustValue( "one", 1 );
        assertTrue(changed);
        assertEquals( 2, map.get( "one" ) );

        changed = map.adjustValue( "one", 5 );
        assertTrue(changed);
        assertEquals( 7, map.get( "one" ) );

        changed = map.adjustValue( "one", -3 );
        assertTrue(changed);
        assertEquals( 4, map.get( "one" ) );

        changed = map.adjustValue( "two", 1 );
        assertFalse(changed);
        assertFalse(map.containsKey( "two" ));
    }


    public void testAdjustOrPutValue() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );

        long new_value = map.adjustOrPutValue( "one", 1, 100 );
        assertEquals(2, new_value);
        assertEquals( 2, map.get( "one" ) );

        new_value = map.adjustOrPutValue( "one", 5, 100 );
        assertEquals(7, new_value);
        assertEquals( 7, map.get( "one" ) );

        new_value = map.adjustOrPutValue( "one", -3, 100 );
        assertEquals(4, new_value);
        assertEquals( 4, map.get( "one" ) );

        new_value = map.adjustOrPutValue( "two", 1, 100 );
        assertEquals(100, new_value);
        assertTrue(map.containsKey( "two" ));
        assertEquals( 100, map.get( "two" ) );

        new_value = map.adjustOrPutValue( "two", 1, 100 );
        assertEquals(101, new_value);
        assertEquals( 101, map.get( "two" ) );
    }


    public void testRetain() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );
        map.put( "two", 2 );
        map.put( "three", 3 );
        map.put( "four", 4 );
        map.put( "five", 5 );
        map.put( "six", 6 );
        map.put( "seven", 7 );
        map.put( "eight", 8 );
        map.put( "nine", 9 );
        map.put( "ten", 10 );

        System.out.println("Start retain...");
        map.retainEntries( new TObjectIntProcedure<String>() {
            public boolean execute(String a, int b) {
                if ( b <= 5 ) return false;
                if ( b > 8 ) return false;

                return true;
            }
        } );
        System.out.println("... end retain");

        assertEquals( 3, map.size() );
        assertFalse( map.containsKey( "one" ) );
        assertFalse( map.containsKey( "two" ) );
        assertFalse( map.containsKey( "three" ) );
        assertFalse( map.containsKey( "four" ) );
        assertFalse( map.containsKey( "five" ) );
        assertTrue( map.containsKey( "six" ) );
        assertTrue( map.containsKey( "seven" ) );
        assertTrue( map.containsKey( "eight" ) );
        assertFalse( map.containsKey( "nine" ) );
        assertFalse( map.containsKey( "ten" ) );

        map.put( "eleven", 11 );
        map.put( "twelve", 12 );
        map.put( "thirteen", 13 );
        map.put( "fourteen", 14 );
        map.put( "fifteen", 15 );
        map.put( "sixteen", 16 );
        map.put( "seventeen", 17 );
        map.put( "eighteen", 18 );
        map.put( "nineteen", 19 );
        map.put( "twenty", 20 );


        System.out.println("Start retain...");
        map.retainEntries( new TObjectIntProcedure<String>() {
            public boolean execute(String a, int b) {
                if ( b <= 15 ) return false;

                return true;
            }
        } );
        System.out.println("... end retain");

        assertEquals( 5, map.size() );
        assertFalse( map.containsKey( "one" ) );
        assertFalse( map.containsKey( "two" ) );
        assertFalse( map.containsKey( "three" ) );
        assertFalse( map.containsKey( "four" ) );
        assertFalse( map.containsKey( "five" ) );
        assertFalse( map.containsKey( "six" ) );
        assertFalse( map.containsKey( "seven" ) );
        assertFalse( map.containsKey( "eight" ) );
        assertFalse( map.containsKey( "nine" ) );
        assertFalse( map.containsKey( "ten" ) );
        assertFalse( map.containsKey( "eleven" ) );
        assertFalse( map.containsKey( "twelve" ) );
        assertFalse( map.containsKey( "thirteen" ) );
        assertFalse( map.containsKey( "fourteen" ) );
        assertFalse( map.containsKey( "fifteen" ) );
        assertTrue( map.containsKey( "sixteen" ) );
        assertTrue( map.containsKey( "seventeen" ) );
        assertTrue( map.containsKey( "eighteen" ) );
        assertTrue( map.containsKey( "nineteen" ) );
        assertTrue( map.containsKey( "twenty" ) );


        System.out.println("Start retain...");
        map.retainEntries( new TObjectIntProcedure<String>() {
            public boolean execute(String a, int b) {
                return false;
            }
        } );
        System.out.println("... end retain");

        System.out.println("Retain test done");

        assertEquals( 0, map.size() );
    }


    public void testPutIfAbsent() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "One", 1 );
        map.put( "Two", 2 );
        map.put( "Three", 3 );

        assertEquals( 1, map.putIfAbsent( "One", 2 ) );
        assertEquals( 1, map.get( "One" ) );
        assertEquals( 0, map.putIfAbsent( "Nine", 9 ) );
        assertEquals( 9, map.get( "Nine" ) );
    }
}
