/*
 * Copyright(c) 2006, NEXVU Technologies
 * All rights reserved.
 */
package gnu.trove;

import gnu.trove.decorator.TByteIntHashMapDecorator;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 *
 */
public class P2PHashMapTest extends TestCase {
    final byte KEY_ONE = ( byte ) 100;
    final byte KEY_TWO = ( byte ) 101;

    public P2PHashMapTest( String name ) {
        super( name );
    }


    public void testKeys() {
        TByteIntHashMap map = new TByteIntHashMap();



        map.put( KEY_ONE, 1 );
        map.put( KEY_TWO, 2 );

        assertEquals( 2, map.size() );

        byte[] keys = map.keys( new byte[ map.size() ] );
        assertEquals( 2, keys.length );
        TByteArrayList keys_list = new TByteArrayList( keys );

        assertTrue( keys_list.contains( KEY_ONE ) );
        assertTrue( keys_list.contains( KEY_TWO ) );

        byte[] keys2 = map.keys();
        assertEquals( 2, keys2.length );
        TByteArrayList keys_list2 = new TByteArrayList( keys2 );

        assertTrue( keys_list2.contains( KEY_ONE ) );
        assertTrue( keys_list2.contains( KEY_TWO ) );
    }


    public void testDecorator() {
        TByteIntHashMap map = new TByteIntHashMap();

        map.put( KEY_ONE, 1 );
        map.put( KEY_TWO, 2 );

        Map<Byte,Integer> decorator = new TByteIntHashMapDecorator( map );

        assertEquals( 2, decorator.size() );
        assertEquals( Integer.valueOf( 1 ), decorator.get( Byte.valueOf( KEY_ONE ) ) );
        assertEquals( Integer.valueOf( 2 ), decorator.get( Byte.valueOf( KEY_TWO ) ) );

        Set<Byte> decorator_keys = decorator.keySet();
        assertEquals( 2, decorator_keys.size() );
        Iterator<Byte> it = decorator_keys.iterator();
        int count = 0;
        while( it.hasNext() ) {
            count++;
            System.out.println(it.next());
        }
        assertEquals( 2, count );

        assertSame(map, ( ( TByteIntHashMapDecorator ) decorator ).getMap() );
    }


    public void testIterator() {
        TByteIntHashMap map = new TByteIntHashMap();

        TByteIntIterator iterator = map.iterator();
        assertFalse( iterator.hasNext() );

        map.put( KEY_ONE, 1 );
        map.put( KEY_TWO, 2 );

        iterator = map.iterator();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertFalse( iterator.hasNext() );
    }


    public void testAdjustValue() {
        TByteIntHashMap map = new TByteIntHashMap();

        map.put( KEY_ONE, 1 );

        boolean changed = map.adjustValue( KEY_ONE, 1 );
        assertTrue(changed);
        assertEquals( 2, map.get( KEY_ONE ) );

        changed = map.adjustValue( KEY_ONE, 5 );
        assertTrue(changed);
        assertEquals( 7, map.get( KEY_ONE ) );

        changed = map.adjustValue( KEY_ONE, -3 );
        assertTrue(changed);
        assertEquals( 4, map.get( KEY_ONE ) );

        changed = map.adjustValue( KEY_TWO, 1 );
        assertFalse(changed);
        assertFalse(map.containsKey( KEY_TWO ));
    }


    public void testAdjustOrPutValue() {
        TByteIntHashMap map = new TByteIntHashMap();

        map.put( KEY_ONE, 1 );

        long new_value = map.adjustOrPutValue( KEY_ONE, 1, 100 );
        assertEquals(2, new_value);
        assertEquals( 2, map.get( KEY_ONE ) );

        new_value = map.adjustOrPutValue( KEY_ONE, 5, 100 );
        assertEquals(7, new_value);
        assertEquals( 7, map.get( KEY_ONE ) );

        new_value = map.adjustOrPutValue( KEY_ONE, -3, 100 );
        assertEquals(4, new_value);
        assertEquals( 4, map.get( KEY_ONE ) );

        new_value = map.adjustOrPutValue( KEY_TWO, 1, 100 );
        assertEquals(100, new_value);
        assertTrue(map.containsKey( KEY_TWO ));
        assertEquals( 100, map.get( KEY_TWO ) );

        new_value = map.adjustOrPutValue( KEY_TWO, 1, 100 );
        assertEquals(101, new_value);
        assertEquals( 101, map.get( KEY_TWO ) );
    }


    /**
     * Test for tracking issue #1204014. +0.0 and -0.0 have different bit patterns, but
     * should be counted the same as keys in a map. Checks for doubles and floats.
     */
    public void testFloatZeroHashing() {
        TDoubleObjectHashMap<String> po_double_map = new TDoubleObjectHashMap<String>();
        TDoubleIntHashMap pp_double_map = new TDoubleIntHashMap();
        TFloatObjectHashMap<String> po_float_map = new TFloatObjectHashMap<String>();
        TFloatIntHashMap pp_float_map = new TFloatIntHashMap();

        final double zero_double = 0.0;
        final double negative_zero_double = -zero_double;
        final float zero_float = 0.0f;
        final float negative_zero_float = -zero_float;

        // Sanity check... make sure I'm really creating two different values.
        final String zero_bits_double =
            Long.toBinaryString( Double.doubleToLongBits(zero_double) );
        final String negative_zero_bits_double =
            Long.toBinaryString( Double.doubleToLongBits(negative_zero_double) );
        assertFalse( zero_bits_double + " == " + negative_zero_bits_double,
            zero_bits_double.equals( negative_zero_bits_double ) );

        final String zero_bits_float =
            Integer.toBinaryString( Float.floatToIntBits( zero_float ) );
        final String negative_zero_bits_float =
            Integer.toBinaryString( Float.floatToIntBits( negative_zero_float ) );
        assertFalse( zero_bits_float + " == " + negative_zero_bits_float,
            zero_bits_float.equals( negative_zero_bits_float ) );


        po_double_map.put(zero_double, "Zero" );
        po_double_map.put(negative_zero_double, "Negative Zero" );

        pp_double_map.put(zero_double, 0 );
        pp_double_map.put(negative_zero_double, -1 );

        po_float_map.put(zero_float, "Zero" );
        po_float_map.put(negative_zero_float, "Negative Zero" );

        pp_float_map.put(zero_float, 0 );
        pp_float_map.put(negative_zero_float, -1 );


        assertEquals( 1, po_double_map.size() );
        assertEquals( po_double_map.get(zero_double), "Negative Zero" );
        assertEquals( po_double_map.get(negative_zero_double), "Negative Zero" );

        assertEquals( 1, pp_double_map.size() );
        assertEquals( pp_double_map.get(zero_double), -1 );
        assertEquals( pp_double_map.get(negative_zero_double), -1 );

        assertEquals( 1, po_float_map.size() );
        assertEquals( po_float_map.get(zero_float), "Negative Zero" );
        assertEquals( po_float_map.get(negative_zero_float), "Negative Zero" );

        assertEquals( 1, pp_float_map.size() );
        assertEquals( pp_float_map.get(zero_float), -1 );
        assertEquals( pp_float_map.get(negative_zero_float), -1 );


        po_double_map.put(zero_double, "Zero" );
        pp_double_map.put(zero_double, 0 );
        po_float_map.put(zero_float, "Zero" );
        pp_float_map.put(zero_float, 0 );


        assertEquals( 1, po_double_map.size() );
        assertEquals( po_double_map.get(zero_double), "Zero" );
        assertEquals( po_double_map.get(negative_zero_double), "Zero" );

        assertEquals( 1, pp_double_map.size() );
        assertEquals( pp_double_map.get(zero_double), 0 );
        assertEquals( pp_double_map.get(negative_zero_double), 0 );

        assertEquals( 1, po_float_map.size() );
        assertEquals( po_float_map.get(zero_float), "Zero" );
        assertEquals( po_float_map.get(negative_zero_float), "Zero" );

        assertEquals( 1, pp_float_map.size() );
        assertEquals( pp_float_map.get(zero_float), 0 );
        assertEquals( pp_float_map.get(negative_zero_float), 0 );
    }


    public void testPutIfAbsent() {
        TIntIntHashMap map = new TIntIntHashMap();

        map.put( 1, 10 );
        map.put( 2, 20 );
        map.put( 3, 30 );

        assertEquals( 10, map.putIfAbsent( 1, 111 ) );
        assertEquals( 10, map.get( 1 ) );
        assertEquals( 0, map.putIfAbsent( 9, 90 ) );
        assertEquals( 90, map.get( 9 ) );
    }
}
