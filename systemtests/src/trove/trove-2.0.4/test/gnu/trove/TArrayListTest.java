package gnu.trove;

import junit.framework.TestCase;

import java.util.Arrays;


public class TArrayListTest extends TestCase {
    private TIntArrayList list;

    public void setUp() throws Exception {
        super.setUp();
        
        list = new TIntArrayList();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }


    public void testToNativeArray() {
        assertTrue(Arrays.equals(new int[]{ 1, 2, 3, 4, 5 }, list.toNativeArray()));
        assertTrue(Arrays.equals(new int[]{ 1, 2, 3, 4 }, list.toNativeArray(0, 4)));
        assertTrue(Arrays.equals(new int[]{ 2, 3, 4, 5 }, list.toNativeArray(1, 4)));
        assertTrue(Arrays.equals(new int[]{ 2, 3, 4 }, list.toNativeArray(1, 3)));
    }

    public void testSubList() throws Exception {
        TIntArrayList subList = list.subList(1, 4);
        assertEquals(3, subList.size());
        assertEquals(2, subList.get(0));
        assertEquals(4, subList.get(2));
    }

    public void testSublist_Exceptions() {
        try {
            list.subList(1, 0);
            fail("expected IllegalArgumentException when end < begin");
        }
        catch (IllegalArgumentException expected) {
        }

        try {
            list.subList(-1, 3);
            fail("expected IndexOutOfBoundsException when begin < 0");
        }
        catch (IndexOutOfBoundsException expected) {
        }

        try {
            list.subList(1, 42);
            fail("expected IndexOutOfBoundsException when end > length");
        }
        catch (IndexOutOfBoundsException expected) {
        }
    }

    public void testMax() {
        assertEquals( 5, list.max() );
        assertEquals( 1, list.min() );

        TIntArrayList list2 = new TIntArrayList();
        list2.add( 3 );
        list2.add( 1 );
        list2.add( 2 );
        list2.add( 5 );
        list2.add( 4 );
        assertEquals( 5, list2.max() );
        assertEquals( 1, list2.min() );
    }
}
