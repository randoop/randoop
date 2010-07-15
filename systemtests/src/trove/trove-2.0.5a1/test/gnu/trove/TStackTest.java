package gnu.trove;

import junit.framework.TestCase;


/**
 *
 */
public class TStackTest extends TestCase {


    public TStackTest() {
        super();
    }

    public TStackTest(String string) {
        super(string);
    }


    public void testBasic() {
        TIntStack stack = new TIntStack();

        assertEquals(0, stack.size());

        stack.push(10);

        assertEquals(1, stack.size());

        assertEquals(10, stack.peek());
        assertEquals(1, stack.size());
        assertEquals(10, stack.peek());
        assertEquals(1, stack.size());

        assertEquals(10, stack.pop());
        assertEquals(0, stack.size());

        stack.push( 10 );
        stack.push( 20 );
        stack.push( 30 );

        assertEquals(3, stack.size());
        assertEquals(30, stack.pop());
        assertEquals(20, stack.pop());
        assertEquals(10, stack.pop());
    }

    public void testArrays() {
        TIntStack stack = new TIntStack();

        int[] array;

        array = stack.toNativeArray();
        assertNotNull(array);
        assertEquals(0, array.length);

        stack.push(10);
        stack.push(20);

        array = stack.toNativeArray();
        assertNotNull(array);
        assertEquals(2, array.length);
        assertEquals(10, array[0]);
        assertEquals(20, array[1]);
        assertEquals(2, stack.size());

        int[] array_correct_size = new int[ 2 ];
        stack.toNativeArray(array_correct_size);
        assertEquals(10, array_correct_size[0]);
        assertEquals(20, array_correct_size[1]);

        int[] array_too_long = new int[ 2 ];
        stack.toNativeArray(array_too_long);
        assertEquals(10, array_too_long[0]);
        assertEquals(20, array_too_long[1]);

        int[] array_too_short = new int[ 1 ];
        try {
            stack.toNativeArray(array_too_short);
        }
        catch( IndexOutOfBoundsException ex ) {
            // this is good
        }
    }
}
