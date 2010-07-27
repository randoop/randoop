/*
 * Copyright(c) 2008, MQSoftware, Inc.
 * All rights reserved.
 */
package gnu.trove;

public class TLongIntHashMapTester {

    public static void main(String[] args) {
        System.out.println("Int max:    " + Integer.MAX_VALUE);
        System.out.println("Long value: " + 4000000000L);


        int array_size = Integer.MAX_VALUE - Integer.parseInt( args[ 0 ] );

        System.out.print("Allocating map: " + array_size + "...");
        long time = System.currentTimeMillis();

        TLongIntHashMap map = new TLongIntHashMap( array_size );
        System.out.println("done: " + ( System.currentTimeMillis() - time ) );

        int i = 0;
        time = System.currentTimeMillis();
        for (long l = 0; l < 4000000000L; l++) {
            map.put(l, i++);
            if ((l % 10000000) == 0) {
                long newTime = System.currentTimeMillis();
                System.out.println("l=" + l / 1000000 + "Mio in " + (newTime - time) +
                        "ms"
                );
                time = newTime;
            }
        }
    }

}