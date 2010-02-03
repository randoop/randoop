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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * <p>Implementation of <code>Hashtable</code> that uses <code>WeakReference</code>'s
 * to hold it's keys thus allowing them to be reclaimed by the garbage collector.
 * This class follows the symantics of <code>Hashtable</code> as closely as possible.
 * It therefore does not accept null values or keys.
 * <p>
 * This implementation is also tuned towards a particular purpose: for use as a replacement
 * for <code>Hashtable</code> in <code>LogFactory</code>. This application requires
 * good liveliness for <code>get</code> and <code>put</code>. Various tradeoffs
 * have been made with this in mind.
 * </p>
 * <p>
 * <strong>Usage:</strong> typical use case is as a drop-in replacement 
 * for the <code>Hashtable</code> use in <code>LogFactory</code> for J2EE enviroments
 * running 1.3+ JVMs. Use of this class <i>in most cases</i> (see below) will
 * allow classloaders to be collected by the garbage collector without the need 
 * to call {@link org.apache.commons.logging.LogFactory#release(ClassLoader) LogFactory.release(ClassLoader)}.
 * </p>
 * <p>
 * In a particular usage scenario, use of <code>WeakHashtable</code> alone will
 * be insufficent to allow garbage collection of a classloader without a call to
 * <code>release</code>.  If the abstract class <code>LogFactory</code> is 
 * loaded by a parent classloader and a concrete subclass implementation of 
 * <code>LogFactory</code> is loaded by a child classloader, the concrete
 * implementation will have a strong reference to the child classloader via the
 * chain <code>getClass().getClassLoader()</code>. The <code>WeakHashtable</code>
 * will have a strong reference to the <code>LogFactory</code> implementation as
 * one of the values in its map. This chain of references will prevent 
 * collection of the child classloader.
 * </p>
 * <p>
 * Such a situation would typically only occur if commons-logging.jar were
 * loaded by a parent classloader (e.g. a server level classloader in a
 * servlet container) and a custom <code>LogFactory</code> implementation were
 * loaded by a child classloader (e.g. a web app classloader).  If use of
 * a custom <code>LogFactory</code> subclass is desired, ensuring that the
 * custom subclass is loaded by the same classloader as <code>LogFactory</code>
 * will prevent problems.  In normal deployments, the standard implementations 
 * of <code>LogFactory</code> found in package <code>org.apache.commons.logging.impl</code> 
 * will be loaded by the same classloader that loads <code>LogFactory</code> 
 * itself, so use of the standard <code>LogFactory</code> implementations
 * should not pose problems.
 * 
 * @author Brian Stansberry
 */
public final class WeakHashtable extends Hashtable {

    /** 
     * The maximum number of times put() or remove() can be called before
     * the map will be purged of all cleared entries.
     */
    private static final int MAX_CHANGES_BEFORE_PURGE = 100;
    
    /** 
     * The maximum number of times put() or remove() can be called before
     * the map will be purged of one cleared entry.
     */
    private static final int PARTIAL_PURGE_COUNT     = 10;
    
    /* ReferenceQueue we check for gc'd keys */
    private ReferenceQueue queue = new ReferenceQueue();
    /* Counter used to control how often we purge gc'd entries */
    private int changeCount = 0;
    
    /**
     * Constructs a WeakHashtable with the Hashtable default
     * capacity and load factor.
     */
    public WeakHashtable() {}
    
    
    /**
     *@see Hashtable
     */
    public boolean containsKey(Object key) {
        // purge should not be required
        Referenced referenced = new Referenced(key);
        return super.containsKey(referenced);
    }
    
    /**
     *@see Hashtable
     */
    public Enumeration elements() {
        purge();
        return super.elements();
    }
    
    /**
     *@see Hashtable
     */
    public Set entrySet() {
        purge();
        Set referencedEntries = super.entrySet();
        Set unreferencedEntries = new HashSet();
        for (Iterator it=referencedEntries.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Referenced referencedKey = (Referenced) entry.getKey();
            Object key = referencedKey.getValue();
            Object value = entry.getValue();
            if (key != null) {
                Entry dereferencedEntry = new Entry(key, value);
                unreferencedEntries.add(dereferencedEntry);
            }
        }
        return unreferencedEntries;
    }
    
    /**
     *@see Hashtable
     */
    public Object get(Object key) {
        // for performance reasons, no purge
        Referenced referenceKey = new Referenced(key);
        return super.get(referenceKey);
    }
    
    /**
     *@see Hashtable
     */
    public Enumeration keys() {
        purge();
        final Enumeration enumer = super.keys();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return enumer.hasMoreElements();
            }
            public Object nextElement() {
                 Referenced nextReference = (Referenced) enumer.nextElement();
                 return nextReference.getValue();
            }
        };
    }
    
        
    /**
     *@see Hashtable
     */
    public Set keySet() {
        purge();
        Set referencedKeys = super.keySet();
        Set unreferencedKeys = new HashSet();
        for (Iterator it=referencedKeys.iterator(); it.hasNext();) {
            Referenced referenceKey = (Referenced) it.next();
            Object keyValue = referenceKey.getValue();
            if (keyValue != null) {
                unreferencedKeys.add(keyValue);
            }
        }
        return unreferencedKeys;
    }
    
    /**
     *@see Hashtable
     */    
    public Object put(Object key, Object value) {
        // check for nulls, ensuring symantics match superclass
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed");
        }
        if (value == null) {
            throw new NullPointerException("Null values are not allowed");
        }

        // for performance reasons, only purge every 
        // MAX_CHANGES_BEFORE_PURGE times
        if (changeCount++ > MAX_CHANGES_BEFORE_PURGE) {
            purge();
            changeCount = 0;
        }
        // do a partial purge more often
        else if ((changeCount % PARTIAL_PURGE_COUNT) == 0) {
            purgeOne();
        }
        
        Object result = null;
        Referenced keyRef = new Referenced(key, queue);
        return super.put(keyRef, value);
    }
    
    /**
     *@see Hashtable
     */    
    public void putAll(Map t) {
        if (t != null) {
            Set entrySet = t.entrySet();
            for (Iterator it=entrySet.iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     *@see Hashtable
     */      
    public Collection values() {
        purge();
        return super.values();
    }
    
    /**
     *@see Hashtable
     */     
    public Object remove(Object key) {
        // for performance reasons, only purge every 
        // MAX_CHANGES_BEFORE_PURGE times
        if (changeCount++ > MAX_CHANGES_BEFORE_PURGE) {
            purge();
            changeCount = 0;
        }
        // do a partial purge more often
        else if ((changeCount % PARTIAL_PURGE_COUNT) == 0) {
            purgeOne();
        }
        return super.remove(new Referenced(key));
    }
    
    /**
     *@see Hashtable
     */    
    public boolean isEmpty() {
        purge();
        return super.isEmpty();
    }
    
    /**
     *@see Hashtable
     */    
    public int size() {
        purge();
        return super.size();
    }
    
    /**
     *@see Hashtable
     */        
    public String toString() {
        purge();
        return super.toString();
    }
    
    /**
     * @see Hashtable
     */
    protected void rehash() {
        // purge here to save the effort of rehashing dead entries
        purge();
        super.rehash();
    }
    
    /**
     * Purges all entries whose wrapped keys
     * have been garbage collected.
     */
    private void purge() {
        synchronized (queue) {
            WeakKey key;
            while ((key = (WeakKey) queue.poll()) != null) {
                super.remove(key.getReferenced());
            }
        }
    }
    
    /**
     * Purges one entry whose wrapped key 
     * has been garbage collected.
     */
    private void purgeOne() {
        
        synchronized (queue) {
            WeakKey key = (WeakKey) queue.poll();
            if (key != null) {
                super.remove(key.getReferenced());
            }
        }
    }
    
    /** Entry implementation */
    private final static class Entry implements Map.Entry {
    
        private final Object key;
        private final Object value;
        
        private Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }
    
        public boolean equals(Object o) {
            boolean result = false;
            if (o != null && o instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) o;
                result =    (getKey()==null ?
                                            entry.getKey() == null : 
                                            getKey().equals(entry.getKey()))
                            &&
                            (getValue()==null ?
                                            entry.getValue() == null : 
                                            getValue().equals(entry.getValue()));
            }
            return result;
        } 
        
        public int hashCode() {

            return (getKey()==null ? 0 : getKey().hashCode()) ^
                (getValue()==null ? 0 : getValue().hashCode());
        }

        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Entry.setValue is not supported.");
        }
        
        public Object getValue() {
            return value;
        }
        
        public Object getKey() {
            return key;
        }
    }
    
    
    /** Wrapper giving correct symantics for equals and hashcode */
    private final static class Referenced {
        
        private final WeakReference reference;
        private final int           hashCode;

        /**
         * 
         * @throws NullPointerException if referant is <code>null</code>
         */        
        private Referenced(Object referant) {
            reference = new WeakReference(referant);
            // Calc a permanent hashCode so calls to Hashtable.remove()
            // work if the WeakReference has been cleared
            hashCode  = referant.hashCode();
        }
        
        /**
         * 
         * @throws NullPointerException if key is <code>null</code>
         */
        private Referenced(Object key, ReferenceQueue queue) {
            reference = new WeakKey(key, queue, this);
            // Calc a permanent hashCode so calls to Hashtable.remove()
            // work if the WeakReference has been cleared
            hashCode  = key.hashCode();

        }
        
        public int hashCode() {
            return hashCode;
        }
        
        private Object getValue() {
            return reference.get();
        }
        
        public boolean equals(Object o) {
            boolean result = false;
            if (o instanceof Referenced) {
                Referenced otherKey = (Referenced) o;
                Object thisKeyValue = getValue();
                Object otherKeyValue = otherKey.getValue();
                if (thisKeyValue == null) {                     
                    result = (otherKeyValue == null);
                    
                    // Since our hashcode was calculated from the original
                    // non-null referant, the above check breaks the 
                    // hashcode/equals contract, as two cleared Referenced
                    // objects could test equal but have different hashcodes.
                    // We can reduce (not eliminate) the chance of this
                    // happening by comparing hashcodes.
                    if (result == true) {
                        result = (this.hashCode() == otherKey.hashCode());
                    }
                    // In any case, as our c'tor does not allow null referants
                    // and Hashtable does not do equality checks between 
                    // existing keys, normal hashtable operations should never 
                    // result in an equals comparison between null referants
                }
                else
                {
                    result = thisKeyValue.equals(otherKeyValue);
                }
            }
            return result;
        }
    }
    
    /**
     * WeakReference subclass that holds a hard reference to an
     * associated <code>value</code> and also makes accessible
     * the Referenced object holding it.
     */
    private final static class WeakKey extends WeakReference {

        private final Referenced referenced;
        
        private WeakKey(Object key, 
                        ReferenceQueue queue,
                        Referenced referenced) {
            super(key, queue);
            this.referenced = referenced;
        }
        
        private Referenced getReferenced() {
            return referenced;
        }
     }
}