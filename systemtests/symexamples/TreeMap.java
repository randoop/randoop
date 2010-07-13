//// This is java.util.TreeMap from the Standard Java Library, //KORAT
//// with small changes that allow checking with Korat. //KORAT
/*
 * @(#)TreeMap.java     1.43 00/02/02
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

//KORAT package java.util; //KORAT
package symexamples; //KORAT
import java.util.*; //KORAT

/**
 * Red-Black tree based implementation of the <tt>SortedMap</tt> interface.
 * This class guarantees that the map will be in ascending key order, sorted
 * according to the <i>natural order</i> for the key's class (see
 * <tt>Comparable</tt>), or by the comparator provided at creation time,
 * depending on which constructor is used.<p>
 *
 * This implementation provides guaranteed log(n) time cost for the
 * <tt>containsKey</tt>, <tt>get</tt>, <tt>put</tt> and <tt>remove</tt>
 * operations.  Algorithms are adaptations of those in Cormen, Leiserson, and
 * Rivest's <I>Introduction to Algorithms</I>.<p>
 *
 * Note that the ordering maintained by a sorted map (whether or not an
 * explicit comparator is provided) must be <i>consistent with equals</i> if
 * this sorted map is to correctly implement the <tt>Map</tt> interface.  (See
 * <tt>Comparable</tt> or <tt>Comparator</tt> for a precise definition of
 * <i>consistent with equals</i>.)  This is so because the <tt>Map</tt>
 * interface is defined in terms of the equals operation, but a map performs
 * all key comparisons using its <tt>compareTo</tt> (or <tt>compare</tt>)
 * method, so two keys that are deemed equal by this method are, from the
 * standpoint of the sorted map, equal.  The behavior of a sorted map
 * <i>is</i> well-defined even if its ordering is inconsistent with equals; it
 * just fails to obey the general contract of the <tt>Map</tt> interface.<p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a map concurrently, and at least one of the threads modifies
 * the map structurally, it <i>must</i> be synchronized externally.  (A
 * structural modification is any operation that adds or deletes one or more
 * mappings; merely changing the value associated with an existing key is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the map.  If no
 * such object exists, the map should be "wrapped" using the
 * <tt>Collections.synchronizedMap</tt> method.  This is best done at creation
 * time, to prevent accidental unsynchronized access to the map: 
 * <pre>
 *     Map m = Collections.synchronizedMap(new TreeMap(...));
 * </pre><p>
 *
 * The iterators returned by all of this class's "collection view methods" are
 * <i>fail-fast</i>: if the map is structurally modified at any time after the
 * iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> or <tt>add</tt> methods, the iterator throws a
 * <tt>ConcurrentModificationException</tt>.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 *
 * @author  Josh Bloch and Doug Lea
 * @version 1.43, 02/02/00
 * @see Map
 * @see HashMap
 * @see Hashtable
 * @see Comparable
 * @see Comparator
 * @see Collection
 * @see Collections#synchronizedMap(Map)
 * @since 1.2
 */

//JML public class TreeMap extends AbstractMap //JML
public class TreeMap extends AbstractMap//JML
                     implements SortedMap/*, Cloneable, java.io.Serializable*/
{
    //JML Copied this from AbstractMap.java to make JML work.
    /*
    public boolean isEmpty() { //JML
    return size() == 0; //JML
    } //JML
    public void super_putAll(Map t) { //JML
    //JML Iterator i = t.entrySet().iterator(); //JML
        java.util.Iterator i = (java.util.Iterator)t.entrySet().iterator(); //JML
    while (i.hasNext()) { //JML
        Entry e = (Entry) i.next(); //JML
        put(e.getKey(), e.getValue()); //JML
    } //JML
    } //JML
    */

    /**
     * The Comparator used to maintain order in this TreeMap, or
     * null if this TreeMap uses its elements natural ordering.
     *
     * @serial
     */
    private Comparator comparator = null;

    private transient Entry root = null;

    /**
     * The number of entries in the tree
     */
    private transient int size = 0;

    /**
     * The number of structural modifications to the tree.
     */
    //private transient int modCount = 0;

    private void incrementSize()   { /*modCount++;*/ size++; }
    private void decrementSize()   { /*modCount++;*/ size--; }

    /**
     * Constructs a new, empty map, sorted according to the keys' natural
     * order.  All keys inserted into the map must implement the
     * <tt>Comparable</tt> interface.  Furthermore, all such keys must be
     * <i>mutually comparable</i>: <tt>k1.compareTo(k2)</tt> must not throw a
     * ClassCastException for any elements <tt>k1</tt> and <tt>k2</tt> in the
     * map.  If the user attempts to put a key into the map that violates this
     * constraint (for example, the user attempts to put a string key into a
     * map whose keys are integers), the <tt>put(Object key, Object
     * value)</tt> call will throw a <tt>ClassCastException</tt>.
     *
     * @see Comparable
     */
    public TreeMap() {
    }

    /**
     * Constructs a new, empty map, sorted according to the given comparator.
     * All keys inserted into the map must be <i>mutually comparable</i> by
     * the given comparator: <tt>comparator.compare(k1, k2)</tt> must not
     * throw a <tt>ClassCastException</tt> for any keys <tt>k1</tt> and
     * <tt>k2</tt> in the map.  If the user attempts to put a key into the
     * map that violates this constraint, the <tt>put(Object key, Object
     * value)</tt> call will throw a <tt>ClassCastException</tt>.
     *
     * @param c the comparator that will be used to sort this map.  A
     *        <tt>null</tt> value indicates that the keys' <i>natural
     *        ordering</i> should be used.
     */
    /*
    public TreeMap(Comparator c) {
        this.comparator = c;
    }
    */
    
    /**
     * Constructs a new map containing the same mappings as the given map,
     * sorted according to the keys' <i>natural order</i>.  All keys inserted
     * into the new map must implement the <tt>Comparable</tt> interface.
     * Furthermore, all such keys must be <i>mutually comparable</i>:
     * <tt>k1.compareTo(k2)</tt> must not throw a <tt>ClassCastException</tt>
     * for any elements <tt>k1</tt> and <tt>k2</tt> in the map.  This method
     * runs in n*log(n) time.
     *
     * @param  m the map whose mappings are to be placed in this map.
     * @throws ClassCastException the keys in t are not Comparable, or
     *         are not mutually comparable.
     */
    /*
    public TreeMap(Map m) {
        putAll(m);
    }
    */

    /**
     * Constructs a new map containing the same mappings as the given
     * <tt>SortedMap</tt>, sorted according to the same ordering.  This method
     * runs in linear time.
     *
     * @param  m the sorted map whose mappings are to be placed in this map,
     *         and whose comparator is to be used to sort this map.
     */
    /*
    public TreeMap(SortedMap m) {
        comparator = m.comparator();
        try {
            //JML buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
            buildFromSorted(m.size(), (java.util.Iterator)m.entrySet().iterator(), null, null); //JML
        } catch (java.io.IOException cannotHappen) {
        } catch (ClassNotFoundException cannotHappen) {
        }
    }
    */


    // Query Operations

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    /*
    public int size() {
        return size;
    }
    */

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map contains a mapping for the
     *            specified key.
     * @throws ClassCastException if the key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *            natural ordering, or its comparator does not tolerate
     *            <tt>null</tt> keys.
     */
    /*
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }
    */

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.  More formally, returns <tt>true</tt> if and only if
     * this map contains at least one mapping to a value <tt>v</tt> such
     * that <tt>(value==null ? v==null : value.equals(v))</tt>.  This
     * operation will probably require time linear in the Map size for most
     * implementations of Map.
     *
     * @param value value whose presence in this Map is to be tested.
     * @since 1.2
     */
    /*
    public boolean containsValue(Object value) {
        return (root==null ? false :
                (value==null ? valueSearchNull(root)
                             : valueSearchNonNull(root, value)));
    }

    private boolean valueSearchNull(Entry n) {
        if (n.value == null)
            return true;

        // Check left and right subtrees for value
        return (n.left  != null && valueSearchNull(n.left)) ||
               (n.right != null && valueSearchNull(n.right));
    }

    private boolean valueSearchNonNull(Entry n, Object value) {
        // Check this node for the value
        if (value.equals(n.value))
            return true;

        // Check left and right subtrees for value
        return (n.left  != null && valueSearchNonNull(n.left, value)) ||
               (n.right != null && valueSearchNonNull(n.right, value));
    }
    */

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <tt>null</tt> if the map contains no mapping for the key.
     * @throws    ClassCastException key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *            natural ordering, or its comparator does not tolerate
     *            <tt>null</tt> keys.
     * 
     * @see #containsKey(Object)
     */
    /*
    public Object get(Object key) {
        Entry p = getEntry(key);
        return (p==null ? null : p.value);
    }
    */

    /**
     * Returns the comparator used to order this map, or <tt>null</tt> if this
     * map uses its keys' natural order.
     *
     * @return the comparator associated with this sorted map, or
     *         <tt>null</tt> if it uses its keys' natural sort method.
     */
    /*    
    public Comparator comparator() {
        return comparator;
    }
    */

    /**
     * Returns the first (lowest) key currently in this sorted map.
     *
     * @return the first (lowest) key currently in this sorted map.
     * @throws    NoSuchElementException Map is empty.
     */
    /*
    public Object firstKey() {
        return key(firstEntry());
    }
    */

    /**
     * Returns the last (highest) key currently in this sorted map.
     *
     * @return the last (highest) key currently in this sorted map.
     * @throws    NoSuchElementException Map is empty.
     */
    /*
    public Object lastKey() {
        return key(lastEntry());
    }
    */

    /**
     * Copies all of the mappings from the specified map to this map.  These
     * mappings replace any mappings that this map had for any of the keys
     * currently in the specified map.
     *
     * @param     map mappings to be stored in this map.
     * @throws    ClassCastException class of a key or value in the specified
     *            map prevents it from being stored in this map.
     * 
     * @throws NullPointerException this map does not permit <tt>null</tt>
     *            keys and a specified key is <tt>null</tt>.
     */
    /*
    public void putAll(Map map) {
        int mapSize = map.size();
        if (size==0 && mapSize!=0 && map instanceof SortedMap) {
            Comparator c = ((SortedMap)map).comparator();
            if (c == comparator || (c != null && c.equals(comparator))) {
              ++modCount;
              try {
                  //JML buildFromSorted(mapSize, map.entrySet().iterator(),
                  buildFromSorted(mapSize, (java.util.Iterator)map.entrySet().iterator(), //JML
                                  null, null);
              } catch (java.io.IOException cannotHappen) {
              } catch (ClassNotFoundException cannotHappen) {
              }
              return;
            }
        }
        //JML super.putAll(map);
        super_putAll(map); //JML
    }
    */
    /**
     * Returns this map's entry for the given key, or <tt>null</tt> if the map
     * does not contain an entry for the key.
     *
     * @return this map's entry for the given key, or <tt>null</tt> if the map
     *         does not contain an entry for the key.
     * @throws ClassCastException if the key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *            natural order, or its comparator does not tolerate *
     *            <tt>null</tt> keys.
     */
    private Entry getEntry(Integer key) {
    //private Entry getEntry(SymbolicInteger key) {
        Entry p = root;
        while (p != null) {
            //int cmp = compare(key,p.key);
            if (key.compareTo(p.key) == 0)
            //if (key._EQ(_pc, p.key))
                return p;
            else if (key.compareTo(p.key) < 0)
            //else if (key._LT(_pc, p.key))
                p = p.left;
            else
                p = p.right;
        }
        return null;
    }

    /**
     * Gets the entry corresponding to the specified key; if no such entry
     * exists, returns the entry for the least key greater than the specified
     * key; if no such entry exists (i.e., the greatest key in the Tree is less
     * than the specified key), returns <tt>null</tt>.
     */
    /*
    private Entry getCeilEntry(Object key) {
        Entry p = root;
        if (p==null)
            return null;

        while (true) {
            int cmp = compare(key, p.key);
            if (cmp == 0) {
                return p;
            } else if (cmp < 0) {
                if (p.left != null)
                    p = p.left;
                else
                    return p;
            } else {
                if (p.right != null) {
                    p = p.right;
                } else {
                    Entry parent = p.parent;
                    Entry ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
    }
    */
    /**
     * Returns the entry for the greatest key less than the specified key; if
     * no such entry exists (i.e., the least key in the Tree is greater than
     * the specified key), returns <tt>null</tt>.
     */
    /*
    private Entry getPrecedingEntry(Object key) {
        Entry p = root;
        if (p==null)
            return null;

        while (true) {
            int cmp = compare(key, p.key);
            if (cmp > 0) {
                if (p.right != null)
                    p = p.right;
                else
                    return p;
            } else {
                if (p.left != null) {
                    p = p.left;
                } else {
                    Entry parent = p.parent;
                    Entry ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
    }
    */
    /**
     * Returns the key corresonding to the specified Entry.  Throw 
     * NoSuchElementException if the Entry is <tt>null</tt>.
     */
    /*
    private static Object key(Entry e) {
        if (e==null)
            throw new NoSuchElementException();
        return e.key;
    }
    */

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     * @throws    ClassCastException key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *            natural order, or its comparator does not tolerate
     *            <tt>null</tt> keys.
     */
    /*@ public normal_behavior
      @     requires repOk();
      @     ensures repOk() && containsKey(key) && value == get(key);
      @*/
    public Object put(Integer key, Object value) {
    //public Object put(SymbolicInteger key, Object value, PathCondition pc) {
        //_pc = pc;
        Entry t = root;

        if (t == null) {
            incrementSize();
            root = new Entry(key, value, null);
            return null;
        }

        while (true) {
            //int cmp = compare(key, t.key);
            if (key.compareTo(t.key) == 0) {
            //if (key._EQ(_pc, t.key)){
                return t.setValue(value);
            } else if (key.compareTo(t.key) < 0) {
            //} else if (key._LT(_pc, t.key)){
                if (t.left != null) {
                    t = t.left;
                } else {
                    incrementSize();
                    t.left = new Entry(key, value, t);
                    fixAfterInsertion(t.left);
                    return null;
                }
            } else { // cmp > 0
            //} else if (key._GE(_pc, t.key)){ // cmp > 0s
                if (t.right != null) {
                    t = t.right;
                } else {
                    incrementSize();
                    t.right = new Entry(key, value, t);
                    fixAfterInsertion(t.right);
                    return null;
                }
            }
        }
    }

    /**
     * Removes the mapping for this key from this TreeMap if present.
     *
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated
     *         <tt>null</tt> with the specified key.
     * 
     * @throws    ClassCastException key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *            natural order, or its comparator does not tolerate
     *            <tt>null</tt> keys.
     */
    // Added to test this method with JML.
    /*@ public normal_behavior
      @     requires repOk();
      @     ensures repOk() && !containsKey(key);
      @*/
    public Object remove(Integer key) {
    //public Object remove(SymbolicInteger key, PathCondition pc) {
        //gov.nasa.jpf.jvm.Verify.beginAtomic();
        //_pc = pc;
                
        Entry p = getEntry(key);
        if (p == null) {
            //gov.nasa.jpf.jvm.Verify.endAtomic();
            return null;
        }

        Object oldValue = p.value;
        //SymbolicInteger oldValue = p.value;
        deleteEntry(p);
        //gov.nasa.jpf.jvm.Verify.endAtomic();
        return oldValue;
    }

    /**
     * Removes all mappings from this TreeMap.
     */
    /*
    public void clear() {
        //modCount++;
        size = 0;
        root = null;
    }
    */

    /**
     * Returns a shallow copy of this <tt>TreeMap</tt> instance. (The keys and
     * values themselves are not cloned.)
     *
     * @return a shallow copy of this Map.
     */
    /*
    public Object clone() {
        TreeMap clone = null;
        try { 
            clone = (TreeMap)super.clone();
        } catch (CloneNotSupportedException e) { 
            throw new InternalError();
        }

        // Put clone into "virgin" state (except for comparator)
        clone.root = null;
        clone.size = 0;
        //clone.modCount = 0;
        clone.keySet = clone.entrySet = null;
        clone.values = null;

    if (root != null) clone.root = (Entry)root.clone(); //KORAT
    clone.size = size; //KORAT

        // Initialize clone with our mappings
    //KORAT        try {
    //KORAT            clone.buildFromSorted(size, entrySet().iterator(), null, null);
    //KORAT        } catch (java.io.IOException cannotHappen) {
    //KORAT        } catch (ClassNotFoundException cannotHappen) {
    //KORAT        }

        return clone;
    }
    */

    // Views

    /**
     * These fields are initialized to contain an instance of the appropriate
     * view the first time this view is requested.  The views are stateless,
     * so there's no reason to create more than one of each.
     */
    private transient Set               keySet = null;
    private transient Set               entrySet = null;
    private transient Collection        values = null;

    /**
     * Returns a Set view of the keys contained in this map.  The set's
     * iterator will return the keys in ascending order.  The map is backed by
     * this <tt>TreeMap</tt> instance, so changes to this map are reflected in
     * the Set, and vice-versa.  The Set supports element removal, which
     * removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt>, and <tt>clear</tt> operations.  It does not support
     * the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the keys contained in this TreeMap.
     */
    /*
    public Set keySet() {
        if (keySet == null) {
            keySet = new AbstractSet() {
                public java.util.Iterator iterator() {
                    return new Iterator(KEYS);
                }

                public int size() {
                    return TreeMap.this.size();
                }

                public boolean contains(Object o) {
                    return containsKey(o);
                }

                public boolean remove(Object o) {
                    int oldSize = size;
                    TreeMap.this.remove(o);
                    return size != oldSize;
                }

                public void clear() {
                    TreeMap.this.clear();
                }
            };
        }
        return keySet;
    }
    */
    /**
     * Returns a collection view of the values contained in this map.  The
     * collection's iterator will return the values in the order that their
     * corresponding keys appear in the tree.  The collection is backed by
     * this <tt>TreeMap</tt> instance, so changes to this map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from the map through
     * the <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map.
     */
    /*
    public Collection values() {
        if (values == null) {
            values = new AbstractCollection() {
                public java.util.Iterator iterator() {
                    return new Iterator(VALUES);
                }

                public int size() {
                    return TreeMap.this.size();
                }

                public boolean contains(Object o) {
                    for (Entry e = firstEntry(); e != null; e = successor(e))
                        if (valEquals(e.getValue(), o))
                            return true;
                    return false;
                }

                public boolean remove(Object o) {
                    for (Entry e = firstEntry(); e != null; e = successor(e)) {
                        if (valEquals(e.getValue(), o)) {
                            deleteEntry(e);
                            return true;
                        }
                    }
                    return false;
                }

                public void clear() {
                    TreeMap.this.clear();
                }
            };
        }
        return values;
    }
    */

    /**
     * Returns a set view of the mappings contained in this map.  The set's
     * iterator returns the mappings in ascending key order.  Each element in
     * the returned set is a <tt>Map.Entry</tt>.  The set is backed by this
     * map, so changes to this map are reflected in the set, and vice-versa.
     * The set supports element removal, which removes the corresponding
     * mapping from the TreeMap, through the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map.
     * @see Map.Entry
     */
    /*
    public Set entrySet() {
        return null;
    }
    */
    /*
    public Set entrySet() {
        if (entrySet == null) {
            entrySet = new AbstractSet() {
                public java.util.Iterator iterator() {
                    return new Iterator(ENTRIES);
                }

                public boolean contains(Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry)o;
                    Object value = entry.getValue();
                    Entry p = getEntry(entry.getKey());
                    return p != null && valEquals(p.getValue(), value);
                }

                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry)o;
                    Object value = entry.getValue();
                    Entry p = getEntry(entry.getKey());
                    if (p != null && valEquals(p.getValue(), value)) {
                        deleteEntry(p);
                        return true;
                    }
                    return false;
                }

                public int size() {
                    return TreeMap.this.size();
                }

                public void clear() {
                    TreeMap.this.clear();
                }
            };
        }
        return entrySet;
    }
    */
    /**
     * Returns a view of the portion of this map whose keys range from
     * <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive.  (If
     * <tt>fromKey</tt> and <tt>toKey</tt> are equal, the returned sorted map
     * is empty.)  The returned sorted map is backed by this map, so changes
     * in the returned sorted map are reflected in this map, and vice-versa.
     * The returned sorted map supports all optional map operations.<p>
     *
     * The sorted map returned by this method will throw an
     * <tt>IllegalArgumentException</tt> if the user attempts to insert a key
     * less than <tt>fromKey</tt> or greater than or equal to
     * <tt>toKey</tt>.<p>
     *
     * Note: this method always returns a <i>half-open range</i> (which
     * includes its low endpoint but not its high endpoint).  If you need a
     * <i>closed range</i> (which includes both endpoints), and the key type
     * allows for calculation of the successor a given key, merely request the
     * subrange from <tt>lowEndpoint</tt> to <tt>successor(highEndpoint)</tt>.
     * For example, suppose that <tt>m</tt> is a sorted map whose keys are
     * strings.  The following idiom obtains a view containing all of the
     * key-value mappings in <tt>m</tt> whose keys are between <tt>low</tt>
     * and <tt>high</tt>, inclusive:
     *      <pre>    SortedMap sub = m.submap(low, high+"\0");</pre>
     * A similar technique can be used to generate an <i>open range</i> (which
     * contains neither endpoint).  The following idiom obtains a view
     * containing all of the key-value mappings in <tt>m</tt> whose keys are
     * between <tt>low</tt> and <tt>high</tt>, exclusive:
     *      <pre>    SortedMap sub = m.subMap(low+"\0", high);</pre>
     *
     * @param fromKey low endpoint (inclusive) of the subMap.
     * @param toKey high endpoint (exclusive) of the subMap.
     * 
     * @return a view of the portion of this map whose keys range from
     *         <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive.
     * 
     * @throws ClassCastException if <tt>fromKey</tt> and <tt>toKey</tt>
     *         cannot be compared to one another using this map's comparator
     *         (or, if the map has no comparator, using natural ordering).
     * @throws IllegalArgumentException if <tt>fromKey</tt> is greater than
     *         <tt>toKey</tt>.
     * @throws NullPointerException if <tt>fromKey</tt> or <tt>toKey</tt> is
     *         <tt>null</tt> and this map uses natural order, or its
     *         comparator does not tolerate <tt>null</tt> keys.
     */
    /*
    public SortedMap subMap(Object fromKey, Object toKey) {
        //JML return new SubMap(fromKey, toKey);
        return null; //JML
    }
    */
    /**
     * Returns a view of the portion of this map whose keys are strictly less
     * than <tt>toKey</tt>.  The returned sorted map is backed by this map, so
     * changes in the returned sorted map are reflected in this map, and
     * vice-versa.  The returned sorted map supports all optional map
     * operations.<p>
     *
     * The sorted map returned by this method will throw an
     * <tt>IllegalArgumentException</tt> if the user attempts to insert a key
     * greater than or equal to <tt>toKey</tt>.<p>
     *
     * Note: this method always returns a view that does not contain its
     * (high) endpoint.  If you need a view that does contain this endpoint,
     * and the key type allows for calculation of the successor a given key,
     * merely request a headMap bounded by <tt>successor(highEndpoint)</tt>.
     * For example, suppose that suppose that <tt>m</tt> is a sorted map whose
     * keys are strings.  The following idiom obtains a view containing all of
     * the key-value mappings in <tt>m</tt> whose keys are less than or equal
     * to <tt>high</tt>:
     * <pre>
     *     SortedMap head = m.headMap(high+"\0");
     * </pre>
     *
     * @param toKey high endpoint (exclusive) of the headMap.
     * @return a view of the portion of this map whose keys are strictly
     *         less than <tt>toKey</tt>.
     *
     * @throws ClassCastException if <tt>toKey</tt> is not compatible
     *         with this map's comparator (or, if the map has no comparator,
     *         if <tt>toKey</tt> does not implement <tt>Comparable</tt>).
     * @throws IllegalArgumentException if this map is itself a subMap,
     *         headMap, or tailMap, and <tt>toKey</tt> is not within the
     *         specified range of the subMap, headMap, or tailMap.
     * @throws NullPointerException if <tt>toKey</tt> is <tt>null</tt> and
     *         this map uses natural order, or its comparator does not
     *         tolerate <tt>null</tt> keys.
     */
    /*
    public SortedMap headMap(Object toKey) {
        //JML return new SubMap(toKey, true);
        return null; //JML
    }
    */

    /**
     * Returns a view of the portion of this map whose keys are greater than
     * or equal to <tt>fromKey</tt>.  The returned sorted map is backed by
     * this map, so changes in the returned sorted map are reflected in this
     * map, and vice-versa.  The returned sorted map supports all optional map
     * operations.<p>
     *
     * The sorted map returned by this method will throw an
     * <tt>IllegalArgumentException</tt> if the user attempts to insert a key
     * less than <tt>fromKey</tt>.<p>
     *
     * Note: this method always returns a view that contains its (low)
     * endpoint.  If you need a view that does not contain this endpoint, and
     * the element type allows for calculation of the successor a given value,
     * merely request a tailMap bounded by <tt>successor(lowEndpoint)</tt>.
     * For For example, suppose that suppose that <tt>m</tt> is a sorted map
     * whose keys are strings.  The following idiom obtains a view containing
     * all of the key-value mappings in <tt>m</tt> whose keys are strictly
     * greater than <tt>low</tt>: <pre>
     *     SortedMap tail = m.tailMap(low+"\0");
     * </pre>
     *
     * @param fromKey low endpoint (inclusive) of the tailMap.
     * @return a view of the portion of this map whose keys are greater
     *         than or equal to <tt>fromKey</tt>.
     * @throws ClassCastException if <tt>fromKey</tt> is not compatible
     *         with this map's comparator (or, if the map has no comparator,
     *         if <tt>fromKey</tt> does not implement <tt>Comparable</tt>).
     * @throws IllegalArgumentException if this map is itself a subMap,
     *         headMap, or tailMap, and <tt>fromKey</tt> is not within the
     *         specified range of the subMap, headMap, or tailMap.
     * @throws NullPointerException if <tt>fromKey</tt> is <tt>null</tt> and
     *         this map uses natural order, or its comparator does not
     *         tolerate <tt>null</tt> keys.
     */
    /*
    public SortedMap tailMap(Object fromKey) {
        //JML return new SubMap(fromKey, false);
        return null; //JML
    }
    */

    //JML Commented out this whole inner class, because it requires AbstractMap,
    //JML which currently doesn't work as it imports an inner interface.
//      private class SubMap extends AbstractMap
//                           implements SortedMap, java.io.Serializable {
//          private static final long serialVersionUID = -6520786458950516097L;

//          /**
//           * fromKey is significant only if fromStart is false.  Similarly,
//           * toKey is significant only if toStart is false.
//           */
//          private boolean fromStart = false, toEnd = false;
//          private Object  fromKey,           toKey;

//          SubMap(Object fromKey, Object toKey) {
//              if (compare(fromKey, toKey) > 0)
//                  throw new IllegalArgumentException("fromKey > toKey");
//              this.fromKey = fromKey;
//              this.toKey = toKey;
//          }

//          SubMap(Object key, boolean headMap) {
//              compare(key, key); // Type-check key

//              if (headMap) {
//                  fromStart = true;
//                  toKey = key;
//              } else {
//                  toEnd = true;
//                  fromKey = key;
//              }
//          }

//          SubMap(boolean fromStart, Object fromKey, boolean toEnd, Object toKey){
//              this.fromStart = fromStart;
//              this.fromKey= fromKey;
//              this.toEnd = toEnd;
//              this.toKey = toKey;
//          }

//          public boolean isEmpty() {
//              return entrySet.isEmpty();
//          }

//          public boolean containsKey(Object key) {
//              return inRange(key) && TreeMap.this.containsKey(key);
//          }

//          public Object get(Object key) {
//              if (!inRange(key))
//                  return null;
//              return TreeMap.this.get(key);
//          }

//          public Object put(Object key, Object value) {
//              if (!inRange(key))
//                  throw new IllegalArgumentException("key out of range");
//              return TreeMap.this.put(key, value);
//          }

//          public Comparator comparator() {
//              return comparator;
//          }

//          public Object firstKey() {
//              Object first = key(fromStart ? firstEntry():getCeilEntry(fromKey));
//              if (!toEnd && compare(first, toKey) >= 0)
//                  throw(new NoSuchElementException());
//              return first;
//          }

//          public Object lastKey() {
//              Object last = key(toEnd ? lastEntry() : getPrecedingEntry(toKey));
//              if (!fromStart && compare(last, fromKey) < 0)
//                  throw(new NoSuchElementException());
//              return last;
//          }

//          private transient Set entrySet = new EntrySetView();

//          public Set entrySet() {
//              return entrySet;
//          }

//          private class EntrySetView extends AbstractSet {
//              private transient int size = -1, sizeModCount;

//              public int size() {
//                  if (size == -1 || sizeModCount != TreeMap.this.modCount) {
//                      size = 0;  sizeModCount = TreeMap.this.modCount;
//                      java.util.Iterator i = iterator();
//                      while (i.hasNext()) {
//                          size++;
//                          i.next();
//                      }
//                  }
//                  return size;
//              }

//              public boolean isEmpty() {
//                  return !iterator().hasNext();
//              }

//              public boolean contains(Object o) {
//                  if (!(o instanceof Map.Entry))
//                      return false;
//                  Map.Entry entry = (Map.Entry)o;
//                  Object key = entry.getKey();
//                  if (!inRange(key))
//                      return false;
//                  TreeMap.Entry node = getEntry(key);
//                  return node != null &&
//                         valEquals(node.getValue(), entry.getValue());
//              }

//              public boolean remove(Object o) {
//                  if (!(o instanceof Map.Entry))
//                      return false;
//                  Map.Entry entry = (Map.Entry)o;
//                  Object key = entry.getKey();
//                  if (!inRange(key))
//                      return false;
//                  TreeMap.Entry node = getEntry(key);
//                  if (node!=null && valEquals(node.getValue(),entry.getValue())){
//                      deleteEntry(node);
//                      return true;
//                  }
//                  return false;
//              }

//              public java.util.Iterator iterator() {
//                  return new Iterator(
//                      (fromStart ? firstEntry() : getCeilEntry(fromKey)),
//                      (toEnd     ? null         : getCeilEntry(toKey)));
//              }
//          }

//          public SortedMap subMap(Object fromKey, Object toKey) {
//              if (!inRange2(fromKey))
//                  throw new IllegalArgumentException("fromKey out of range");
//              if (!inRange2(toKey))
//                  throw new IllegalArgumentException("toKey out of range");
//              return new SubMap(fromKey, toKey);
//          }

//          public SortedMap headMap(Object toKey) {
//              if (!inRange2(toKey))
//                  throw new IllegalArgumentException("toKey out of range");
//              return new SubMap(fromStart, fromKey, false, toKey);
//          }

//          public SortedMap tailMap(Object fromKey) {
//              if (!inRange2(fromKey))
//                  throw new IllegalArgumentException("fromKey out of range");
//              return new SubMap(false, fromKey, toEnd, toKey);
//          }

//          private boolean inRange(Object key) {
//              return (fromStart || compare(key, fromKey) >= 0) &&
//                     (toEnd     || compare(key, toKey)   <  0);
//          }

//          // This form allows the high endpoint (as well as all legit keys)
//          private boolean inRange2(Object key) {
//              return (fromStart || compare(key, fromKey) >= 0) &&
//                     (toEnd     || compare(key, toKey)   <= 0);
//          }
//      }

    // Types of Iterators
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;

    /**
     * TreeMap Iterator.
     */
    /*
    private class Iterator implements java.util.Iterator {
        private int type;
        private int expectedModCount = TreeMap.this.modCount;
        private Entry lastReturned = null;
        private Entry next;
        private Entry firstExcluded = null;

        Iterator(int type) {
            this.type = type;
            next = firstEntry();
        }

        Iterator(Entry first, Entry firstExcluded) {
            type = ENTRIES;
            next = first;
            this.firstExcluded = firstExcluded;
        }

        public boolean hasNext() {
            return next != firstExcluded;
        }

        public Object next() {
            if (next == firstExcluded)
                throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            lastReturned = next;
            next = successor(next);
            return (type == KEYS ? lastReturned.key :
                    (type == VALUES ? lastReturned.value : lastReturned));
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            deleteEntry(lastReturned);
            expectedModCount++;
            lastReturned = null;
        }
    }
    */

    /**
     * Compares two keys using the correct comparison method for this TreeMap.
     */
    /*
    private int compare(Object k1, Object k2) {
        return (comparator==null ? ((Comparable)k1).compareTo(k2)
                                 : comparator.compare(k1, k2));
    } 
    */   

    /**
     * Test two values  for equality.  Differs from o1.equals(o2) only in
     * that it copes with with <tt>null</tt> o1 properly.
     */
    /*
    private static boolean valEquals(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }
    */

    private static final boolean RED   = false;
    private static final boolean BLACK = true;

    /**
     * Node in the Tree.  Doubles as a means to pass key-value pairs back to
     * user (see Map.Entry).
     */

    //KORAT static class Entry implements Map.Entry {
    public static class Entry implements Map.Entry/*, java.io.Serializable, Cloneable*/ { //KORAT
        //Object key;
        Integer key;
        //SymbolicInteger key;
        //boolean _key_is_initialized = false;
        
        Object value;
        //SymbolicInteger value;
        //boolean _value_is_initialized = false;
        
        /*
        SymbolicInteger _get_key() {
            if (!_key_is_initialized) {
                _key_is_initialized = true;
                key = SymbolicInteger._SymbolicInteger();
            }
            return key;
        }

        SymbolicInteger _get_value() {
            if (!_value_is_initialized) {
                _value_is_initialized = true;
                value = SymbolicInteger._SymbolicInteger();
            }
            return key;
        }

        void _set_key(SymbolicInteger e) {
            _key_is_initialized = true;
            key = e;
        }

        void _set_value(SymbolicInteger e) {
            _value_is_initialized = true;
            value = e;
        }
        */
        Entry left = null;
        Entry right = null;
        Entry parent;
        boolean color = BLACK;

    /*
    //KORAT START    
    private static final long serialVersionUID = 919286545866124009L;

    public Object clone(){
        Entry e = null;
        try{
        e = (Entry)super.clone();
        e.color = color;
        e.parent = null;
        e.key = key;
        e.value = value;
        e.left = null;
        e.right = null;
        if (left != null) {
            e.left = (Entry)left.clone();
            e.left.parent = e;
        }
        if (right != null) {
            e.right = (Entry)right.clone();
            e.right.parent = e;
        }
        }catch(CloneNotSupportedException exc) {
        System.out.println("Problems: " + exc);
        }
        return e;
    }
    //KORAT END
    */
        /**
         * Make a new cell with given key, value, and parent, and with <tt>null</tt>
         * child links, and BLACK color. 
         */
        //Entry(Object key, Object value, Entry parent) { 
        Entry(Integer key, Object value, Entry parent) {
        //Entry(SymbolicInteger key, Object value, Entry parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        /**
         * Returns the key.
         *
         * @return the key.
         */
        public Object getKey() { 
            return key; 
        }

        /**
         * Returns the value associated with the key.
         *
         * @return the value associated with the key.
         */
        public Object getValue() {
            return value;
        }

        /**
         * Replaces the value currently associated with the key with the given
         * value.
         *
         * @return the value associated with the key before this method was
         *         called.
         */
        public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            //this.value = (SymbolicInteger)value;
            return oldValue;
        }

          /*
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;

            return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
        }

        public int hashCode() {
            int keyHash = (key==null ? 0 : key.hashCode());
            int valueHash = (value==null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        public String toString() {
            //KORAT return key + "=" + value;
            return (cont(key) + "=" + cont(value) + //KORAT
                    " l: " + cont(left) + " r: " + cont(right) + //KORAT
                    " p: " + cont(parent) + " c: " + (color?"B":"R")); //KORAT
        }
        //KORAT-START
        private String cont(Object o) {
            if (o == null) return "N";
            else return o.toString();
        }
    private String cont(Entry e) {
        if (e == null) return "n";
        else return e.key.toString();
    }
        Object maximumKey() {
            Entry t = left;
            while (t.right != null)
                t = t.right;
            return t.key;
        }

        Object minimumKey() {
            Entry t = right;
            while (t.left != null)
                t = t.left;
            return t.key;
        }
        //KORAT-END
         */
    }

    /**
     * Returns the first Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    /*
    private Entry firstEntry() {
        Entry p = root;
        if (p != null)
            while (p.left != null)
                p = p.left;
        return p;
    }
    */

    /**
     * Returns the last Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    /*
    private Entry lastEntry() {
        Entry p = root;
        if (p != null)
            while (p.right != null)
                p = p.right;
        return p;
    }
    */

    /**
     * Returns the successor of the specified Entry, or null if no such.
     */
    private Entry successor(Entry t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            Entry p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } else {
            Entry p = t.parent;
            Entry ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Balancing operations.
     *
     * Implementations of rebalancings during insertion and deletion are
     * slightly different than the CLR version.  Rather than using dummy
     * nilnodes, we use a set of accessors that deal properly with null.  They
     * are used to avoid messiness surrounding nullness checks in the main
     * algorithms.
     */

    private static boolean colorOf(Entry p) {
        return (p == null ? BLACK : p.color);
    }

    private static Entry  parentOf(Entry p) { 
        return (p == null ? null: p.parent);
    }

    private static void setColor(Entry p, boolean c) { 
        if (p != null)  p.color = c; 
    }

    private static Entry  leftOf(Entry p) { 
        return (p == null)? null: p.left; 
    }

    private static Entry  rightOf(Entry p) { 
        return (p == null)? null: p.right; 
    }

    /** From CLR **/
    private void rotateLeft(Entry p) {
        Entry r = p.right;
        p.right = r.left;
        if (r.left != null)
            r.left.parent = p;
        r.parent = p.parent;
        if (p.parent == null)
            root = r;
        else if (p.parent.left == p)
            p.parent.left = r;
        else
            p.parent.right = r;
        r.left = p;
        p.parent = r;
    }

    /** From CLR **/
    private void rotateRight(Entry p) {
        Entry l = p.left;
        p.left = l.right;
        if (l.right != null) l.right.parent = p;
        l.parent = p.parent;
        if (p.parent == null)
            root = l;
        else if (p.parent.right == p)
            p.parent.right = l;
        else p.parent.left = l;
        l.right = p;
        p.parent = l;
    }


    /** From CLR **/
    private void fixAfterInsertion(Entry x) {
        x.color = RED;

        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Entry y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    if (parentOf(parentOf(x)) != null) 
                        rotateRight(parentOf(parentOf(x)));
                }
            } else {
                Entry y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x),  BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    if (parentOf(parentOf(x)) != null) 
                        rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    /**
     * Delete node p, and then rebalance the tree.
     */
    private void deleteEntry(Entry p) {
        decrementSize();

        // If strictly internal, first swap position with successor.
        if (p.left != null && p.right != null) {
            Entry s = successor(p);
            swapPosition(s, p);
        } 

        // Start fixup at replacement node, if it exists.
        Entry replacement = (p.left != null ? p.left : p.right);

        if (replacement != null) {
            // Link replacement to parent 
            replacement.parent = p.parent;
            if (p.parent == null)       
                root = replacement; 
            else if (p == p.parent.left)  
                p.parent.left  = replacement;
            else
                p.parent.right = replacement;

            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = p.right = p.parent = null;
      
            // Fix replacement
            //MUTANT if (p.color == RED)
            if (p.color == BLACK)
                fixAfterDeletion(replacement);
        } else if (p.parent == null) { // return if we are the only node.
            root = null;
        } else { //  No children. Use self as phantom replacement and unlink.
            if (p.color == BLACK) 
                fixAfterDeletion(p);

            if (p.parent != null) {
                if (p == p.parent.left) 
                    p.parent.left = null;
                else if (p == p.parent.right) 
                    p.parent.right = null;
                p.parent = null;
            }
        }
    }

    /** From CLR **/
    private void fixAfterDeletion(Entry x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Entry sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib))  == BLACK && 
                    colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib,  RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else { // symmetric
                Entry sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK && 
                    colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib,  RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK); 
    }

    /**
     * Swap the linkages of two nodes in a tree.
     */
    private void swapPosition(Entry x, Entry y) {
        // Save initial values.
        Entry px = x.parent, lx = x.left, rx = x.right;
        Entry py = y.parent, ly = y.left, ry = y.right;
        boolean xWasLeftChild = px != null && x == px.left;
        boolean yWasLeftChild = py != null && y == py.left;

        // Swap, handling special cases of one being the other's parent.
        if (x == py) {  // x was y's parent
            x.parent = y;
            if (yWasLeftChild) { 
                y.left = x; 
                y.right = rx; 
            } else {
                y.right = x;
                y.left = lx;  
            }
        } else {
            x.parent = py; 
            if (py != null) {
                if (yWasLeftChild)
                    py.left = x;
                else
                    py.right = x;
            }
            y.left = lx;   
            y.right = rx;
        }

        if (y == px) { // y was x's parent
            y.parent = x;
            if (xWasLeftChild) { 
                x.left = y; 
                x.right = ry; 
            } else {
                x.right = y;
                x.left = ly;  
            }
        } else {
            y.parent = px; 
            if (px != null) {
                if (xWasLeftChild)
                    px.left = y;
                else
                    px.right = y;
            }
            x.left = ly;   
            x.right = ry;  
        }

        // Fix children's parent pointers
        if (x.left != null)
            x.left.parent = x;
        if (x.right != null)
            x.right.parent = x;
        if (y.left != null)
            y.left.parent = y;
        if (y.right != null)
            y.right.parent = y;

        // Swap colors
        boolean c = x.color;
        x.color = y.color;
        y.color = c;

        // Check if root changed
        if (root == x)
            root = y;
        else if (root == y)
            root = x;
    }


    //private static final long serialVersionUID = 919286545866124006L;

    /**
     * Save the state of the <tt>TreeMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>size</i> of the TreeMap (the number of key-value
     *             mappings) is emitted (int), followed by the key (Object)
     *             and value (Object) for each key-value mapping represented
     *             by the TreeMap. The key-value mappings are emitted in
     *             key-order (as determined by the TreeMap's Comparator,
     *             or by the keys' natural ordering if the TreeMap has no
     *             Comparator).
     */
    /*
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out the Comparator and any hidden stuff
        s.defaultWriteObject();

        // Write out size (number of Mappings)
        s.writeInt(size);

        // Write out keys and values (alternating)
        //JML for (java.util.Iterator i = entrySet().iterator(); i.hasNext(); ) {
//KORAT          for (java.util.Iterator i = (java.util.Iterator)entrySet().iterator(); i.hasNext(); ) {
//KORAT              Entry e = (Entry)i.next();
//KORAT              s.writeObject(e.key);
//KORAT              s.writeObject(e.value);
//KORAT          }
    s.writeObject(root); //KORAT
    }
    */


    /**
     * Reconstitute the <tt>TreeMap</tt> instance from a stream (i.e.,
     * deserialize it).
     */
    /*
    private void readObject(final java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in the Comparator and any hidden stuff
        s.defaultReadObject();

        // Read in size
        //KORAT int size = s.readInt();
        size = s.readInt(); //KORAT

        root = (Entry)s.readObject(); //KORAT
        //KORAT buildFromSorted(size, null, s, null);
    }
    */

    
    /** Intended to be called only from TreeSet.readObject **/
    /*
    void readTreeSet(int size, java.io.ObjectInputStream s, Object defaultVal)
        throws java.io.IOException, ClassNotFoundException {
        buildFromSorted(size, null, s, defaultVal);
    }
    */

    /** Intended to be called only from TreeSet.addAll **/
    /*
    void addAllForTreeSet(SortedSet set, Object defaultVal) {
      try {
          //JML buildFromSorted(set.size(), set.iterator(), null, defaultVal);
          buildFromSorted(set.size(), (java.util.Iterator)set.iterator(), null, defaultVal); //JML 
      } catch (java.io.IOException cannotHappen) {
      } catch (ClassNotFoundException cannotHappen) {
      }
    }
    */


    /**
     * Linear time tree building algorithm from sorted data.  Can accept keys
     * and/or values from iterator or stream. This leads to too many
     * parameters, but seems better than alternatives.  The four formats
     * that this method accepts are:
     *
     *    1) An iterator of Map.Entries.  (it != null, defaultVal == null).
     *    2) An iterator of keys.         (it != null, defaultVal != null).
     *    3) A stream of alternating serialized keys and values.
     *                                    (it == null, defaultVal == null).
     *    4) A stream of serialized keys. (it == null, defaultVal != null).
     *
     * It is assumed that the comparator of the TreeMap is already set prior
     * to calling this method.
     *
     * @param size the number of keys (or key-value pairs) to be read from
     *        the iterator or stream.
     * @param it If non-null, new entries are created from entries
     *        or keys read from this iterator.
     * @param it If non-null, new entries are created from keys and
     *        possibly values read from this stream in serialized form.
     *        Exactly one of it and str should be non-null.
     * @param defaultVal if non-null, this default value is used for
     *        each value in the map.  If null, each value is read from
     *        iterator or stream, as described above.
     * @throws IOException propagated from stream reads. This cannot
     *         occur if str is null.
     * @throws ClassNotFoundException propagated from readObject. 
     *         This cannot occur if str is null.
     */
    /*
    private void buildFromSorted(int size, java.util.Iterator it,
                                  java.io.ObjectInputStream str,
                                  Object defaultVal)
        throws  java.io.IOException, ClassNotFoundException {
        this.size = size;
        root = buildFromSorted(0, 0, size-1, computeRedLevel(size),
                               it, str, defaultVal);
    }
    */
    /**
     * Recursive "helper method" that does the real work of the
     * of the previous method.  Identically named parameters have
     * identical definitions.  Additional parameters are documented below.
     * It is assumed that the comparator and size fields of the TreeMap are
     * already set prior to calling this method.  (It ignores both fields.)
     *
     * @param level the current level of tree. Initial call should be 0.
     * @param lo the first element index of this subtree. Initial should be 0.
     * @param hi the last element index of this subtree.  Initial should be
     *        size-1.
     * @param redLevel the level at which nodes should be red. 
     *        Must be equal to computeRedLevel for tree of this size.
     */
   /*
    private static Entry buildFromSorted(int level, int lo, int hi,
                                         int redLevel,
                                         java.util.Iterator it, 
                                         java.io.ObjectInputStream str,
                                         Object defaultVal) 
        throws  java.io.IOException, ClassNotFoundException {*/
        /*
         * Strategy: The root is the middlemost element. To get to it, we
         * have to first recursively construct the entire left subtree,
         * so as to grab all of its elements. We can then proceed with right
         * subtree. 
         *
         * The lo and hi arguments are the minimum and maximum
         * indices to pull out of the iterator or stream for current subtree.
         * They are not actually indexed, we just proceed sequentially,
         * ensuring that items are extracted in corresponding order.
         */
/*
        if (hi < lo) return null;

        int mid = (lo + hi) / 2;
        
        Entry left  = null;
        if (lo < mid) 
            left = buildFromSorted(level+1, lo, mid - 1, redLevel,
                                   it, str, defaultVal);
        
        // extract key and/or value from iterator or stream
        Object key;
        Object value;
        if (it != null) { // use iterator
            if (defaultVal==null) {
                Map.Entry entry = (Map.Entry) it.next();
                key = entry.getKey();
                value = entry.getValue();
            } else {
                key = it.next();
                value = defaultVal;
            }
        } else { // use stream
            key = str.readObject();
            value = (defaultVal != null ? defaultVal : str.readObject());
        }

        Entry middle =  new Entry(key, value, null);
        
        // color nodes in non-full bottommost level red
        if (level == redLevel)
            middle.color = RED;
        
        if (left != null) { 
            middle.left = left; 
            left.parent = middle; 
        }
        
        if (mid < hi) {
            Entry right = buildFromSorted(level+1, mid+1, hi, redLevel,
                                          it, str, defaultVal);
            middle.right = right;
            right.parent = middle;
        }
        
        return middle;
    }
*/
    /**
     * Find the level down to which to assign all nodes BLACK.  This is the
     * last `full' level of the complete binary tree produced by
     * buildTree. The remaining nodes are colored RED. (This makes a `nice'
     * set of color assignments wrt future insertions.) This level number is
     * computed by finding the number of splits needed to reach the zeroeth
     * node.  (The answer is ~lg(N), but in any case must be computed by same
     * quick O(lg(N)) loop.)
     */
    /*
    private static int computeRedLevel(int sz) {
        int level = 0;
        for (int m = sz - 1; m >= 0; m = m / 2 - 1) 
            level++;
        return level;
    }
    */
    /*
    //KORAT-START
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        //JML for (java.util.Iterator i = entrySet().iterator(); i.hasNext(); ) {
        for (java.util.Iterator i = (java.util.Iterator)entrySet().iterator(); i.hasNext(); ) {
            Entry e = (Entry)i.next();
            buf.append(e.toString());
            if (i.hasNext()) buf.append(", ");
        }
        buf.append("}");
        return buf.toString();
    }

    
    public boolean repOk() {
    if (root == null) 
        return size == 0;
        // RootHasNoParent
        if (root.parent != null)
            return debug("RootHasNoParent");
        Set visited = new java.util.HashSet();
    visited.add(new Wrapper(root));
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Entry current = (Entry)workList.removeFirst();
            // Acyclic
            //// if (!visited.add(new Wrapper(current)))
            ////     return debug("Acyclic");
            // Parent Definition
            Entry cl = current.left;
            if (cl != null) {
        if (!visited.add(new Wrapper(cl)))
            return debug("Acyclic");
                if (cl.parent != current)
                    return debug("parent_Input1");
                workList.add(cl);
            }
            Entry cr = current.right;
            if (cr != null) {
        if (!visited.add(new Wrapper(cr)))
            return debug("Acyclic");
                if (cr.parent != current)
                    return debug("parent_Input2");
                workList.add(cr);
            }
        }
        // SizeOk
        if (visited.size() != size)
            return debug("SizeOk");
        if (!repOkColors()) return false;
        return repOkKeysAndValues();
    }

    private boolean repOkColors() {
        // RedHasOnlyBlackChildren
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Entry current = (Entry)workList.removeFirst();
            Entry cl = current.left;
            Entry cr = current.right;
            if (current.color == RED) {
                if (cl != null && cl.color == RED)
                    return debug("RedHasOnlyBlackChildren1");
                if (cr != null && cr.color == RED)
                    return debug("RedHasOnlyBlackChildren2");
            }
            if (cl != null) workList.add(cl);
            if (cr != null) workList.add(cr);
        }
        // SimplePathsFromRootToNILHaveSameNumberOfBlackNodes
        int numberOfBlack = -1;
        workList = new java.util.LinkedList();
        workList.add(new Pair(root, 0));
        while (!workList.isEmpty()) {
            Pair p = (Pair)workList.removeFirst();
            Entry e = p.e;
            int n = p.n;
            if (e != null && e.color == BLACK) n++;
            if (e == null) {
                if (numberOfBlack == -1) 
                    numberOfBlack = n;
                else if (numberOfBlack != n)
                    return debug("SimplePathsFromRootToNILHaveSameNumberOfBlackNodes");
            } else {
                workList.add(new Pair(e.left, n));
                workList.add(new Pair(e.right, n));
            }
        }
        return true;
    }

    private boolean repOkKeysAndValues() {
        // BST1 and BST2
        // this was the old way of determining if the keys are ordered
//          workList = new java.util.LinkedList();
//          workList.add(root);
//          while (!workList.isEmpty()) {
//              Entry current = (Entry)workList.removeFirst();
//              Entry cl = current.left;
//              Entry cr = current.right;
//              if (current.key==current.key) ;
//              if (cl != null) {
//                  if (compare(current.key, current.maximumKey()) <= 0)
//                      return debug("BST1");
//                  workList.add(cl);
//              }
//              if (cr != null) {
//                  if (compare(current.key, current.minimumKey()) >= 0)
//                      return debug("BST2");
//                  workList.add(cr);
//              }
//          }
        // this is the new (Alex's) way to determine if the keys are ordered
        if (!orderedKeys(root, null, null)) 
            return debug("BST");
        // touch values
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Entry current = (Entry)workList.removeFirst();
            touch(current.value);
            if (current.left != null) workList.add(current.left);
            if (current.right != null) workList.add(current.right);
        }
        return true;
    }
    private void touch(Object o) {}

    private boolean orderedKeys(Entry e, Object min, Object max) {
    if (e.key == null)
        return false;
    if (((min != null) && (compare(e.key, min) <= 0)) ||
        ((max != null) && (compare(e.key, max) >= 0)))
        return false;
    if (e.left != null)
        if (!orderedKeys(e.left, min, e.key))
        return false;
    if (e.right != null)
        if (!orderedKeys(e.right, e.key, max))
        return false;
    return true;
    }

    private final boolean debug(String s) {
        // System.out.println(s);
        return false;
    }

    private final class Pair {
        Entry e; int n;
        Pair(Entry e, int n) { this.e = e; this.n = n; }
    }
    
    private static final class Wrapper {
        Entry e;
        Wrapper(Entry e) { this.e = e; }
        public boolean equals(Object obj) {
            if (!(obj instanceof Wrapper)) return false;
            return e == ((Wrapper)obj).e;
        }
    public int hashCode() {
        return System.identityHashCode(e);
    }
    }
    //KORAT-END
    */
    /*
    public static int N = 6;
    public static TreeMap t = new TreeMap();
        
    public static void main (String[] a) {
        int call_seq_length = 0;
        //while (true) {
        while (call_seq_length <= N) {
            if (Verify.randomBool()) {
                Verify.beginAtomic(); 
                t.put();
                Verify.endAtomic(); 
            } else {
                Verify.beginAtomic(); 
                t.remove();
                Verify.endAtomic(); 
            }
            Verify.beginAtomic(); 
            Verify.incrementCounter(0);        
            System.out.println("Call length: " + call_seq_length + " Solution " + Verify.getCounter(0));
            Verify.endAtomic();     
            call_seq_length++;
        } 
        Verify.ignoreIf(true);        
    }
    */
    //private static PathCondition _pc = null;

}
