///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2006, Rob Eden All Rights Reserved.
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

package gnu.trove.benchmark;

import gnu.trove.TIntObjectHashMap;

import java.util.Random;


/**
 *
 */
public class CompactionBenchmark {
    public static void main( String[] args ) {
        float compact_factor;
        if ( args.length > 0 ) compact_factor = Float.parseFloat( args[ 0 ] );
        else compact_factor = 0.5f;

        System.out.println( "Compact factor: " + compact_factor );

        int[] primitives = new int[ 1000000 ];
        for( int i = 0; i < primitives.length; i++ ) {
            primitives[ i ] = i;
        }

        Integer[] objects = new Integer[ primitives.length ];
        for( int i = 0; i < primitives.length; i++ ) {
            objects[ i ] = new Integer( primitives[ i ] );
        }


        TIntObjectHashMap<Integer> map = new TIntObjectHashMap<Integer>() {

            @Override
            public void compact() {
                super.compact();
                System.out.print( "c" );
            }
        };
        map.setAutoCompactionFactor( compact_factor );

        Random rand = new Random();

        final boolean manual_compaction = compact_factor == 0;

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long total = 0;
        for( int i = 0; i < 50; i++ ) {
            long time = System.currentTimeMillis();

            runTest( primitives, objects, map, rand );

            if ( manual_compaction ) map.compact();

            time = System.currentTimeMillis() - time;

            System.out.println( time );

            total += time;
            min = Math.min( time, min );
            max = Math.max( time, max );
        }

        System.out.println( "----------------------" );
        System.out.println( "Avg: " + ( total / 100.0 ) );
        System.out.println( "Min: " + min );
        System.out.println( "Max: " + max );
    }


    private static void runTest( int[] primitives, Integer[] objects,
        TIntObjectHashMap<Integer> map, Random rand ) {

        for( int i = 0; i < 250000; i++ ) {
            int index = rand.nextInt( primitives.length );
            map.put( primitives[ index ], objects[ index ] );
        }

        for( int i = 0; i < 250000; i++ ) {
            int index = rand.nextInt( primitives.length );
            map.remove( primitives[ index ] );
        }
    }
}
