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

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * Created: Sat Nov  3 10:33:15 2001
 *
 * @author Eric D. Friedman
 * @version $Id: THashSetTest.java,v 1.4 2008/04/04 16:31:12 robeden Exp $
 */

public class THashSetTest extends TestCase  {

    protected Set s;
    
    public THashSetTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        s = new THashSet();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        s = null;
    }

    public void testIsEmpty() throws Exception {
        assertTrue("new set wasn't empty", s.isEmpty());

        s.add("One");
        assertTrue("set with element reports empty", ! s.isEmpty());
        s.clear();
        assertTrue("cleared set reports not-empty", s.isEmpty());
    }

    public void testContains() throws Exception {
        Object o = "testContains";
        s.add(o);
        assertTrue("contains failed", s.contains(o));
    }

    public void testContainsAll() throws Exception {
        Object[] o = { "Hello World", "Goodbye World", "Hello Goodbye" };
        s.addAll(Arrays.asList(o));
        for (int i = 0; i < o.length; i++) {
            assertTrue(o[i].toString(),s.contains(o[i]));
        }
        assertTrue("containsAll failed: " + s,
                   s.containsAll(Arrays.asList(o)));
    }

    public void testAdd() throws Exception {
        assertTrue("add failed", s.add("One"));
        assertTrue("duplicated add succeded", ! s.add("One"));
    }

    public void testRemove() throws Exception {
        s.add("One");
        s.add("Two");
        assertTrue("One was not added", s.contains("One"));
        assertTrue("One was not removed", s.remove("One"));
        assertTrue("One was not removed", ! s.contains("One"));
    }

    public void testSize() throws Exception {
        s = new THashSet();
        assertEquals("initial size was not 0",0, s.size());

        for (int i = 0; i < 99; i++) {
            s.add(new Object());
            assertEquals("size did not increase after add", i+1, s.size());
        }
    }

    public void testClear() throws Exception {
        s = new THashSet();
        s.addAll(Arrays.asList(new String[] {"one","two","three"}));
        assertEquals("size was not 3",3, s.size());
        s.clear();
        assertEquals("initial size was not 0",0, s.size());
    }

    public void testSerialize() throws Exception {
        s = new THashSet();
        s.addAll(Arrays.asList(new String[] {"one","two","three"}));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(s);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        THashSet s2 = (THashSet)ois.readObject();

        assertEquals(s, s2);
    }

    public void testNormalLoad() throws Exception {
        THashSet set = new THashSet(11, 0.5f);
        assertEquals(set._maxSize, 11);
        for (int i = 0; i < 12; i++) {
            set.add(new Integer(i));
        }
        assertTrue(set._maxSize > 12);
    }

    public void testMaxLoad() throws Exception {
        THashSet set = new THashSet(11, 1.0f);
        assertEquals(10, set._maxSize);
        for (int i = 0; i < 12; i++) {
            set.add(new Integer(i));
        }
        assertTrue(set._maxSize > 12);
    }

    public void testToArray() {
        Object[] str = { "hi", "bye", "hello", "goodbye" };
        s.addAll(Arrays.asList(str));
        Object[] res = s.toArray();
        Arrays.sort(str);
        Arrays.sort(res);
        assertTrue(Arrays.equals(str, res));
    }

    public void testToArrayWithParams() {
        Object[] str = { "hi", "bye", "hello", "goodbye" };
        s.addAll(Arrays.asList(str));
        String[] sink = new String[str.length + 2];
        sink[sink.length - 1] = "residue";
        sink[sink.length - 2] = "will be cleared";
        Object[] res = s.toArray(sink);

        Set copy = new HashSet();
        copy.addAll(Arrays.asList(res));
        
        Set bogey = new HashSet();
        bogey.addAll(Arrays.asList(str));
        bogey.add("residue");
        bogey.add(null);
        assertEquals(bogey, copy);
    }

    public void testReusesRemovedSlotsOnCollision() {
        THashSet set = new THashSet(11, 0.5f);

        class Foo {
            public int hashCode() {
                return 4;
            }
        }

        Foo f1 = new Foo();
        Foo f2 = new Foo();
        Foo f3 = new Foo();
        set.add(f1);
        
        int idx = set.insertionIndex(f2);
        set.add(f2);
        assertEquals(f2, set._set[idx]);
        set.remove(f2);
        assertEquals(set.REMOVED, set._set[idx]);
        assertEquals(idx, set.insertionIndex(f3));
        set.add(f3);
        assertEquals(f3, set._set[idx]);
    }

    public void testRehashing() throws Exception {
        for (int i = 0; i < 10000; i++) {
            s.add(new Integer(i));
        }
    }

    /**
     * this tests that we throw when people violate the
     * general contract for hashcode on java.lang.Object
     */
    public void testSomeBadlyWrittenObject() {
      boolean didThrow = false;
      int i = 0;
      try {
        for (; i < 101; i++) {
            s.add(new Crap());
        }
      } catch (IllegalArgumentException e) {
        didThrow = true;
      }
      assertTrue("expected THashSet to throw an IllegalArgumentException", didThrow);
    }


    public void testIterable() {

        Set<String> set = new THashSet<String>();
        set.add( "One" );
        set.add( "Two" );

        for( String s : set ) {
            assertTrue(s.equals("One") || s.equals("Two" ));
        }
    }


    public void testToString() {
        Set<String> set = new THashSet<String>();
        set.add( "One" );
        set.add( "Two" );

        String to_string = set.toString();
        assertTrue( to_string,
            to_string.equals( "{One,Two}" ) ||
            to_string.equals( "{Two,One}" ) );
    }


      // in this junk class, all instances hash to the same
      // address, but some objects claim to be equal where
      // others do not.
    public static class Crap {
        public boolean equals(Object other) {
            return other instanceof Crap;
        }

        public int hashCode() {
          return System.identityHashCode( this );
        }
    }

    public static void main(String[] args) throws Exception {
      junit.textui.TestRunner.run(new THashSetTest("testBadlyWrittenObject"));
    }
} // THashSetTests
