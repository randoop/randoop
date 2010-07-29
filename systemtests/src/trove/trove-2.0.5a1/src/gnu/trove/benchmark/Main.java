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

package gnu.trove.benchmark;

import gnu.trove.*;
import java.util.*;

/**
 *
 * Created: Sat Nov  3 18:17:56 2001
 *
 * @author Eric D. Friedman
 * @version $Id: Main.java,v 1.5 2006/12/11 21:03:48 robeden Exp $
 */

public class Main {
    static final int ITERATIONS = 25;

    static final int SET_SIZE = 100000;
    static final List<Integer> dataset = new ArrayList<Integer>(SET_SIZE);
    static {
        for (int i = 0; i < SET_SIZE; i++) {
            dataset.add(new Integer(i));
        }
    }
    
    public static Operation getSetOperation() {
        
        return new Operation() {
                public void theirs() {
                    Set s = new HashSet(SET_SIZE);
                    for (Iterator i = dataset.iterator(); i.hasNext();) {
                        s.add(i.next());
                    }
                }

                public void ours() {
                    Set s = new THashSet(SET_SIZE);
                    for (Iterator i = dataset.iterator(); i.hasNext();) {
                        s.add(i.next());
                    }
                }

                public String toString() {
                    return "compares " + dataset.size() + " Set.add() operations";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }

    public static Operation getLinkedListAddOp() {
        try {
            Class.forName( "gnu.trove.TLinkableAdapter" );
        }
        catch( Throwable t ) {
            return new Operation() {

                public int getIterationCount() {
                    return 1;
                }

                public void ours() {}
                public void theirs() {}

                public String toString() {
                    return "(**UNAVAILABLE**) compares " + dataset.size() +
                        " LinkedList.add() operations";
                }
            };
        }

        final List data = new ArrayList(100000);
        for (int i = 0; i < 100000; i++) {
            data.add(new TLinkableAdapter());
        }
        

        return new Operation() {

                public void theirs() {
                    List l = new LinkedList();
                    for (Iterator i = data.iterator(); i.hasNext();) {
                        l.add(i.next());
                    }
                }

                public void ours() {
                    List l = new TLinkedList();
                    for (Iterator i = data.iterator(); i.hasNext();) {
                        l.add(i.next());
                    }
                }

                public String toString() {
                    return "compares " + dataset.size() + " LinkedList.add() operations";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }

    static Operation getContainsOp() {
        final Set theirs = new HashSet(dataset.size());
        theirs.addAll(dataset);
        final Set ours = new THashSet(dataset.size());
        ours.addAll(dataset);

        return new Operation() {
                public void theirs() {
                    for (int i = 0; i < dataset.size(); i += 5) {
                        theirs.contains(dataset.get(i));
                    }
                }

                public void ours() {
                    for (int i = 0; i < dataset.size(); i += 5) {
                        ours.contains(dataset.get(i));
                    }
                }

                public String toString() {
                    return "compares " + dataset.size() / 5 + " Set.contains() operations";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }


    static Operation getRandomSetContainsOp() {
        final Set theirs = new HashSet(SET_SIZE);
        final Set ours = new THashSet(SET_SIZE);
        Random rand = new Random(9999L);

        for (int i = 0; i < SET_SIZE; i++) {
            Integer x = new Integer(rand.nextInt());
            theirs.add(x);
            ours.add(x);
        }

        Random rand2 = new Random(9998L);
        final List query = new ArrayList(SET_SIZE);
        int match = 0;
        for (int i = 0; i < SET_SIZE; i++) {
            Integer x = new Integer(rand.nextInt());
            query.add(x);
            if (theirs.contains(x)) {
                match++;
            }
        }

        final int success = match;

        return new Operation() {
                public void theirs() {
                    for (Iterator i = query.iterator(); i.hasNext();) {
                        theirs.contains(i.next());
                    }
                }

                public void ours() {
                    for (Iterator i = query.iterator(); i.hasNext();) {
                        ours.contains(i.next());
                    }
                }

                public String toString() {
                    return "compares " + SET_SIZE + " Set.contains() operations. "
                        + success + " are actually present in set";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }

    static Operation getMapPutOp() {
        return new Operation() {
                public void theirs() {
                    Map theirs = new HashMap(dataset.size());
                    for (Iterator i = dataset.iterator();i.hasNext();) {
                        Object o = i.next();
                        theirs.put(o,o);
                    }
                }

                public void ours() {
                    Map ours = new THashMap(dataset.size());
                    for (Iterator i = dataset.iterator();i.hasNext();) {
                        Object o = i.next();
                        ours.put(o,o);
                    }
                }

                public String toString() {
                    return "compares " + dataset.size() + " Map.put() operations";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }

    static Operation getIterationOp() {
        final Map theirMap = new HashMap(dataset.size());
        final Map ourMap   = new THashMap(dataset.size());
        for (Iterator i = dataset.iterator();i.hasNext();) {
            Object o = i.next();
            theirMap.put(o,o);
            ourMap.put(o,o);
        }
        
        return new Operation() {
                public void theirs() {
                    Map m = theirMap;
                    Iterator i = m.keySet().iterator();
                    for (int size = m.size(); size-- > 0;) {
                        Object o = i.next();
                    }
                }

                public void ours() {
                    Map m = ourMap;
                    Iterator i = m.keySet().iterator();
                    for (int size = m.size(); size-- > 0;) {
                        Object o = i.next();
                    }
                }

                public String toString() {
                    return "compares Iterator.next() over " + dataset.size() + " map keys";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }


    static Operation getIterationWithHasNextOp() {
        final Map theirMap = new HashMap(dataset.size());
        final Map ourMap   = new THashMap(dataset.size());
        for (Iterator i = dataset.iterator();i.hasNext();) {
            Object o = i.next();
            theirMap.put(o,o);
            ourMap.put(o,o);
        }
        
        return new Operation() {
                public void theirs() {
                    Map m = theirMap;
                    Iterator i = m.keySet().iterator();
                    while (i.hasNext()) {
                        Object o = i.next();
                    }
                }

                public void ours() {
                    Map m = ourMap;
                    Iterator i = m.keySet().iterator();
                    while (i.hasNext()) {
                        Object o = i.next();
                    }
                }

                public String toString() {
                    return "compares Iterator.hasNext()/ Iterator.next() over " + theirMap.size() + " keys";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }

    static Operation getIntMapPut() {
        return new Operation() {
                public void theirs() {
                }

                public void ours() {
                    TIntIntHashMap ours = new TIntIntHashMap(SET_SIZE);
                    for (int i = dataset.size(); i-- > 0;) {
                        ours.put(i,i);
                    }
                }

                public String toString() {
                    return dataset.size() + " entry primitive int map.put timing run; no basis for comparison";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }

    static Operation getSumSetOperation() {
        final Set theirSet = new HashSet(dataset.size());
        final THashSet ourSet = new THashSet(dataset.size());
        theirSet.addAll(dataset);
        ourSet.addAll(dataset);
        final TObjectProcedure proc = new TObjectProcedure() {
                int sum = 0;
                public boolean execute(Object o) {
                    sum += ((Integer)o).intValue();
                    return true;
                }
            };
        return new Operation() {
                public void theirs() {
                    int sum = 0;
                    for (Iterator i = theirSet.iterator(); i.hasNext();) {
                        sum += ((Integer)i.next()).intValue();
                    }
                }

                public void ours() {
                    ourSet.forEach(proc);
                }

                public String toString() {
                    return "sums a " + theirSet.size() + " element Set of Integer objects.  Their approach uses Iterator.hasNext()/next(); ours uses THashSet.forEach(TObjectProcedure)";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }

    // This is an internal test comparing the old method using adjustValue() & put() to the
    // new adjustOrPutValue() method.
    static Operation getAdjustCompareOp() {
        final TObjectIntHashMap<Integer> theirs = new TObjectIntHashMap<Integer>();
        final TObjectIntHashMap<Integer> ours = new TObjectIntHashMap<Integer>();

        for( int i = 0; i < SET_SIZE; i++ ) {
            if ( ( i % 1000 ) == 0 ) {
                Integer int_obj = dataset.get( i );

                theirs.put( int_obj, int_obj.intValue() );
                ours.put( int_obj, int_obj.intValue() );
            }
        }

        return new Operation() {
                public void theirs() {
                    int size = dataset.size();
                    for( int i = 0; i < size; i++ ) {
                        Integer key = dataset.get(i);
                        if ( !theirs.adjustValue(key, 1) ) {
                            theirs.put(key,1);
                        }
                    }
                }

                public void ours() {
                    int size = dataset.size();
                    for( int i = 0; i < size; i++ ) {
                        theirs.adjustOrPutValue(dataset.get(i), 1, 1);
                    }
                }

                public String toString() {
                    return "compares adjustValue & put vs. adjustOrPutValue";
                }

                public int getIterationCount() {
                    return ITERATIONS;
                }
            };
    }


    public static void main (String[] args) {
        Operation op;
        Timer t;
        Reporter reporter;

        reporter = new TextReporter();
        reporter.start();

        // NOTE: this is internal only
//        op = getAdjustCompareOp();     // 10 times
//        t = new Repeater(op);
//        reporter.report(t.run());

        op = getRandomSetContainsOp();     // 10 times
        t = new Repeater(op);
        reporter.report(t.run());

        op = getSumSetOperation();     // 10 times
        t = new Repeater(op);
        reporter.report(t.run());

        op = getIterationWithHasNextOp();     // 10 times
        t = new Repeater(op);
        reporter.report(t.run());

        op = getIterationOp();     // 10 times
        t = new Repeater(op);
        reporter.report(t.run());

        op = getLinkedListAddOp();     // 10 times
        t = new Repeater(op);
        reporter.report(t.run());

        op = getIntMapPut();     // 10 times
        t = new Repeater(op);
        reporter.report(t.run());

        op = getMapPutOp();     // 10 times
        t = new Repeater(op);
        reporter.report(t.run());

        op = getContainsOp(); // 10 times
        t = new Repeater(op);
        reporter.report(t.run());
        
        op = getSetOperation(); // 10 times
        t = new Repeater(op);
        reporter.report(t.run());

        reporter.finish();
    }
} // Main
