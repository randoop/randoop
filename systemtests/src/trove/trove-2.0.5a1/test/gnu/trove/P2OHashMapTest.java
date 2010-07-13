/*
 * Copyright(c) 2007, NEXVU Technologies
 * All rights reserved.
 */
package gnu.trove;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;


/**
 *
 */
public class P2OHashMapTest extends TestCase {
    public P2OHashMapTest( String name ) {
        super( name );
    }


    public void testGetValues() {
        TIntObjectHashMap<String> map = new TIntObjectHashMap<String>();

        map.put( 1, "one" );
        map.put( 2, "two" );
        map.put( 3, "three" );
        map.put( 4, "four" );

        // Exact size
        String[] template = new String[ map.size() ];
        String[] values = map.getValues( template );
        assertSame( template, values );

        List<String> list = Arrays.asList( values );
        assertTrue( list.contains( "one" ) );
        assertTrue( list.contains( "two" ) );
        assertTrue( list.contains( "three" ) );
        assertTrue( list.contains( "four" ) );

        // Zero length
        template = new String[ 0 ];
        values = map.getValues( template );
        assertNotSame( template, values );

        list = Arrays.asList( values );
        assertTrue( list.contains( "one" ) );
        assertTrue( list.contains( "two" ) );
        assertTrue( list.contains( "three" ) );
        assertTrue( list.contains( "four" ) );

        // Longer than needed
        template = new String[ 10 ];
        values = map.getValues( template );
        assertSame( template, values );

        list = Arrays.asList( values );
        assertTrue( list.contains( "one" ) );
        assertTrue( list.contains( "two" ) );
        assertTrue( list.contains( "three" ) );
        assertTrue( list.contains( "four" ) );
    }


    public void testPutIfAbsent() {
        TIntObjectHashMap<String> map = new TIntObjectHashMap<String>();

        map.put( 1, "One" );
        map.put( 2, "Two" );
        map.put( 3, "Three" );

        assertEquals( "One", map.putIfAbsent( 1, "Two" ) );
        assertEquals( "One", map.get( 1 ) );
        assertNull( map.putIfAbsent( 9, "Nine" ) );
        assertEquals( "Nine", map.get( 9 ) );
    }


    public void testBug2037709() {
        TIntObjectHashMap<String> m = new TIntObjectHashMap<String>();
        for (int i = 0; i < 10; i++) {
            m.put(i, String.valueOf(i));
        }

        int sz = m.size();
        assertEquals(10, sz);

        int[] keys = new int[sz];
        m.keys(keys);

        boolean[] seen = new boolean[sz];
        Arrays.fill( seen, false );
        for (int i = 0; i < 10; i++) {
            seen[ keys[ i ] ] = true;
        }

        for (int i = 0; i < 10; i++) {
            if (!seen[ i ]) {
                TestCase.fail("Missing key for: " + i);
            }
        }
    }
}
