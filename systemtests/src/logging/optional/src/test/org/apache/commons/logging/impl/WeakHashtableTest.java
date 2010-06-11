/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 


package org.apache.commons.logging.impl;

import java.lang.ref.*;
import junit.framework.*;
import java.util.*;

public class WeakHashtableTest extends TestCase {

    
    /** Maximum number of iterations before our test fails */
    private static final int MAX_GC_ITERATIONS = 50;

    private WeakHashtable weakHashtable;
    private Long keyOne;
    private Long keyTwo;
    private Long keyThree;
    private Long valueOne;
    private Long valueTwo;
    private Long valueThree;

    public WeakHashtableTest(String testName) {
        super(testName);
    }
    

    protected void setUp() throws Exception {
        super.setUp();
        weakHashtable = new WeakHashtable();
        
        keyOne = new Long(1);
        keyTwo = new Long(2);
        keyThree = new Long(3);
        valueOne = new Long(100);
        valueTwo = new Long(200);
        valueThree = new Long(300);
        
        weakHashtable.put(keyOne, valueOne);
        weakHashtable.put(keyTwo, valueTwo);
        weakHashtable.put(keyThree, valueThree);
    }

    /** Tests public boolean contains(ObjectÊvalue) */
    public void testContains() throws Exception {
        assertFalse(weakHashtable.contains(new Long(1)));
        assertFalse(weakHashtable.contains(new Long(2)));
        assertFalse(weakHashtable.contains(new Long(3)));
        assertTrue(weakHashtable.contains(new Long(100)));
        assertTrue(weakHashtable.contains(new Long(200)));
        assertTrue(weakHashtable.contains(new Long(300)));
        assertFalse(weakHashtable.contains(new Long(400)));
    }
    
    /** Tests public boolean containsKey(ObjectÊkey) */
    public void testContainsKey() throws Exception {
        assertTrue(weakHashtable.containsKey(new Long(1)));
        assertTrue(weakHashtable.containsKey(new Long(2)));
        assertTrue(weakHashtable.containsKey(new Long(3)));
        assertFalse(weakHashtable.containsKey(new Long(100)));
        assertFalse(weakHashtable.containsKey(new Long(200)));
        assertFalse(weakHashtable.containsKey(new Long(300)));
        assertFalse(weakHashtable.containsKey(new Long(400)));    
    }
    
    /** Tests public boolean containsValue(ObjectÊvalue) */
    public void testContainsValue() throws Exception {
        assertFalse(weakHashtable.containsValue(new Long(1)));
        assertFalse(weakHashtable.containsValue(new Long(2)));
        assertFalse(weakHashtable.containsValue(new Long(3)));
        assertTrue(weakHashtable.containsValue(new Long(100)));
        assertTrue(weakHashtable.containsValue(new Long(200)));
        assertTrue(weakHashtable.containsValue(new Long(300)));
        assertFalse(weakHashtable.containsValue(new Long(400)));    
    }
    
    /** Tests public Enumeration elements() */
    public void testElements() throws Exception {
        ArrayList elements = new ArrayList();
        for (Enumeration e = weakHashtable.elements(); e.hasMoreElements();) {
            elements.add(e.nextElement());
        }
        assertEquals(3, elements.size());
        assertTrue(elements.contains(valueOne));
        assertTrue(elements.contains(valueTwo));
        assertTrue(elements.contains(valueThree));
    }
    
    /** Tests public Set entrySet() */
    public void testEntrySet() throws Exception {
        Set entrySet = weakHashtable.entrySet();
        for (Iterator it = entrySet.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            if (keyOne.equals(key)) {
                assertEquals(valueOne, entry.getValue());
            } else if (keyTwo.equals(key)) {
                assertEquals(valueTwo, entry.getValue());
            } else if (keyThree.equals(key)) {
                assertEquals(valueThree, entry.getValue());
            } else {
                fail("Unexpected key");
            }
        }
    }
    
    /** Tests public Object get(ObjectÊkey) */
    public void testGet() throws Exception {
        assertEquals(valueOne, weakHashtable.get(keyOne));
        assertEquals(valueTwo, weakHashtable.get(keyTwo));
        assertEquals(valueThree, weakHashtable.get(keyThree));
        assertNull(weakHashtable.get(new Long(50)));
    }
    
    /** Tests public Enumeration keys() */
    public void testKeys() throws Exception {
        ArrayList keys = new ArrayList();
        for (Enumeration e = weakHashtable.keys(); e.hasMoreElements();) {
            keys.add(e.nextElement());
        }
        assertEquals(3, keys.size());
        assertTrue(keys.contains(keyOne));
        assertTrue(keys.contains(keyTwo));
        assertTrue(keys.contains(keyThree));    
    }
    
    /** Tests public Set keySet() */
    public void testKeySet() throws Exception {
        Set keySet = weakHashtable.keySet();
        assertEquals(3, keySet.size());
        assertTrue(keySet.contains(keyOne));
        assertTrue(keySet.contains(keyTwo));
        assertTrue(keySet.contains(keyThree));
    }
    
    /** Tests public Object put(ObjectÊkey, ObjectÊvalue) */
    public void testPut() throws Exception {
        Long anotherKey = new Long(2004);
        weakHashtable.put(anotherKey, new Long(1066));
        
        assertEquals(new Long(1066), weakHashtable.get(anotherKey));
               
        // Test compliance with the hashtable API re nulls
        Exception caught = null;
        try {
            weakHashtable.put(null, new Object());
        }
        catch (Exception e) {
            caught = e;
        }
        assertNotNull("did not throw an exception adding a null key", caught);
        caught = null;
        try {
            weakHashtable.put(new Object(), null);
        }
        catch (Exception e) {
            caught = e;
        }
        assertNotNull("did not throw an exception adding a null value", caught);
    }
    
    /** Tests public void putAll(MapÊt) */
    public void testPutAll() throws Exception {
        Map newValues = new HashMap();
        Long newKey = new Long(1066);
        Long newValue = new Long(1415);
        newValues.put(newKey, newValue);
        Long anotherNewKey = new Long(1645);
        Long anotherNewValue = new Long(1815);
        newValues.put(anotherNewKey, anotherNewValue);
        weakHashtable.putAll(newValues);
        
        assertEquals(5, weakHashtable.size());
        assertEquals(newValue, weakHashtable.get(newKey));
        assertEquals(anotherNewValue, weakHashtable.get(anotherNewKey));
    }
    
    /** Tests public Object remove(ObjectÊkey) */
    public void testRemove() throws Exception {
        weakHashtable.remove(keyOne);
        assertEquals(2, weakHashtable.size());
        assertNull(weakHashtable.get(keyOne));
    }
    
    /** Tests public Collection values() */
    public void testValues() throws Exception {
        Collection values = weakHashtable.values();
        assertEquals(3, values.size());
        assertTrue(values.contains(valueOne));
        assertTrue(values.contains(valueTwo));
        assertTrue(values.contains(valueThree));
    }
    
    public void testRelease() throws Exception {
        assertNotNull(weakHashtable.get(new Long(1)));
        ReferenceQueue testQueue = new ReferenceQueue();
        WeakReference weakKeyOne = new WeakReference(keyOne, testQueue);

        // lose our references
        keyOne = null;
        keyTwo = null;
        keyThree = null;
        valueOne = null;
        valueTwo = null;
        valueThree = null;
        
        int iterations = 0;
        int bytz = 2;
        while(true) {
            System.gc();
            if(iterations++ > MAX_GC_ITERATIONS){
                fail("Max iterations reached before resource released.");
            }
            
            if(weakHashtable.get(new Long(1)) == null) {
                break;
                
            } else {
                // create garbage:
                byte[] b =  new byte[bytz];
                bytz = bytz * 2;
            }
        }
        
        // some JVMs seem to take a little time to put references on 
        // the reference queue once the reference has been collected
        // need to think about whether this is enough to justify
        // stepping through the collection each time...
        while(testQueue.poll() == null) {}
        
        // Test that the released objects are not taking space in the table
        assertEquals("underlying table not emptied", 0, weakHashtable.size());
    }
}
