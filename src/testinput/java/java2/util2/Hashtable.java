/*
 * @(#)Hashtable.java	1.95 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

import java.io.*;

/**
 * This class implements a hashtable, which maps keys to values. Any
 * non-<code>null</code> object can be used as a key or as a value. <p>
 *
 * To successfully store and retrieve objects from a hashtable, the
 * objects used as keys must implement the <code>hashCode</code>
 * method and the <code>equals</code> method. <p>
 *
 * An instance of <code>Hashtable</code> has two parameters that affect its
 * performance: <i>initial capacity</i> and <i>load factor</i>.  The
 * <i>capacity</i> is the number of <i>buckets</i> in the hash table, and the
 * <i>initial capacity</i> is simply the capacity at the time the hash table
 * is created.  Note that the hash table is <i>open</i>: in the case of a "hash
 * collision", a single bucket stores multiple entries, which must be searched
 * sequentially.  The <i>load factor</i> is a measure of how full the hash
 * table is allowed to get before its capacity is automatically increased.
 * When the number of entries in the hashtable exceeds the product of the load
 * factor and the current capacity, the capacity is increased by calling the
 * <code>rehash</code> method.<p>
 *
 * Generally, the default load factor (.75) offers a good tradeoff between
 * time and space costs.  Higher values decrease the space overhead but
 * increase the time cost to look up an entry (which is reflected in most
 * <tt>Hashtable</tt> operations, including <tt>get</tt> and <tt>put</tt>).<p>
 *
 * The initial capacity controls a tradeoff between wasted space and the
 * need for <code>rehash</code> operations, which are time-consuming.
 * No <code>rehash</code> operations will <i>ever</i> occur if the initial
 * capacity is greater than the maximum number of entries the
 * <tt>Hashtable</tt> will contain divided by its load factor.  However,
 * setting the initial capacity too high can waste space.<p>
 *
 * If many entries are to be made into a <code>Hashtable</code>,
 * creating it with a sufficiently large capacity may allow the
 * entries to be inserted more efficiently than letting it perform
 * automatic rehashing as needed to grow the table. <p>
 *
 * This example creates a hashtable of numbers. It uses the names of
 * the numbers as keys:
 * <p><blockquote><pre>
 *     Hashtable numbers = new Hashtable();
 *     numbers.put("one", new Integer(1));
 *     numbers.put("two", new Integer(2));
 *     numbers.put("three", new Integer(3));
 * </pre></blockquote>
 * <p>
 * To retrieve a number, use the following code:
 * <p><blockquote><pre>
 *     Integer n = (Integer)numbers.get("two");
 *     if (n != null) {
 *         System.out.println("two = " + n);
 *     }
 * </pre></blockquote>
 * <p>
 * As of the Java 2 platform v1.2, this class has been retrofitted to
 * implement Map, so that it becomes a part of Java's collection framework.
 * Unlike the new collection implementations, Hashtable is synchronized.<p>
 *
 * The Iterators returned by the iterator and listIterator methods
 * of the Collections returned by all of Hashtable's "collection view methods"
 * are <em>fail-fast</em>: if the Hashtable is structurally modified
 * at any time after the Iterator is created, in any way except through the
 * Iterator's own remove or add methods, the Iterator will throw a
 * ConcurrentModificationException.  Thus, in the face of concurrent
 * modification, the Iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the future.
 * The Enumerations returned by Hashtable's keys and values methods are
 * <em>not</em> fail-fast.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i><p>
 *
 * This class is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Arthur van Hoff
 * @author  Josh Bloch
 * @version 1.95, 01/23/03
 * @see     Object#equals(java.lang.Object)
 * @see     Object#hashCode()
 * @see     Hashtable#rehash()
 * @see     Collection
 * @see	    Map
 * @see	    HashMap
 * @see	    TreeMap
 * @since JDK1.0
 */
public class Hashtable extends Dictionary implements Map, Cloneable, java.io.Serializable {
  /**
   * The hash table data.
   */
  private transient Entry[] table;

  /**
   * The total number of entries in the hash table.
   */
  private transient int count;

  /**
   * The table is rehashed when its size exceeds this threshold.  (The
   * value of this field is (int)(capacity * loadFactor).)
   *
   * @serial
   */
  private int threshold;

  /**
   * The load factor for the hashtable.
   *
   * @serial
   */
  private float loadFactor;

  /**
   * The number of times this Hashtable has been structurally modified
   * Structural modifications are those that change the number of entries in
   * the Hashtable or otherwise modify its internal structure (e.g.,
   * rehash).  This field is used to make iterators on Collection-views of
   * the Hashtable fail-fast.  (See ConcurrentModificationException).
   */
  private transient int modCount = 0;

  /** use serialVersionUID from JDK 1.0.2 for interoperability */
  private static final long serialVersionUID = 1421746759512286392L;

  /**
   * Constructs a new, empty hashtable with the specified initial
   * capacity and the specified load factor.
   *
   * @param      initialCapacity   the initial capacity of the hashtable.
   * @param      loadFactor        the load factor of the hashtable.
   * @exception  IllegalArgumentException  if the initial capacity is less
   *             than zero, or if the load factor is nonpositive.
   */
  public Hashtable(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0) {
      throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
    }
    if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
      throw new IllegalArgumentException("Illegal Load: " + loadFactor);
    }

    if (initialCapacity == 0) initialCapacity = 1;
    this.loadFactor = loadFactor;
    table = new Entry[initialCapacity];
    threshold = (int) (initialCapacity * loadFactor);
  }

  /**
   * Constructs a new, empty hashtable with the specified initial capacity
   * and default load factor, which is <tt>0.75</tt>.
   *
   * @param     initialCapacity   the initial capacity of the hashtable.
   * @exception IllegalArgumentException if the initial capacity is less
   *              than zero.
   */
  public Hashtable(int initialCapacity) {
    this(initialCapacity, 0.75f);
  }

  /**
   * Constructs a new, empty hashtable with a default initial capacity (11)
   * and load factor, which is <tt>0.75</tt>.
   */
  public Hashtable() {
    this(11, 0.75f);
  }

  /**
   * Constructs a new hashtable with the same mappings as the given
   * Map.  The hashtable is created with an initial capacity sufficient to
   * hold the mappings in the given Map and a default load factor, which is
   * <tt>0.75</tt>.
   *
   * @param t the map whose mappings are to be placed in this map.
   * @throws NullPointerException if the specified map is null.
   * @since   1.2
   */
  public Hashtable(Map t) {
    this(Math.max(2 * t.size(), 11), 0.75f);
    putAll(t);
  }

  /**
   * Returns the number of keys in this hashtable.
   *
   * @return  the number of keys in this hashtable.
   */
  public synchronized int size() {
    return count;
  }

  /**
   * Tests if this hashtable maps no keys to values.
   *
   * @return  <code>true</code> if this hashtable maps no keys to values;
   *          <code>false</code> otherwise.
   */
  public synchronized boolean isEmpty() {
    return count == 0;
  }

  /**
   * Returns an enumeration of the keys in this hashtable.
   *
   * @return  an enumeration of the keys in this hashtable.
   * @see     Enumeration
   * @see     #elements()
   * @see	#keySet()
   * @see	Map
   */
  public synchronized Enumeration keys() {
    return getEnumeration(KEYS);
  }

  /**
   * Returns an enumeration of the values in this hashtable.
   * Use the Enumeration methods on the returned object to fetch the elements
   * sequentially.
   *
   * @return  an enumeration of the values in this hashtable.
   * @see     java2.util2.Enumeration
   * @see     #keys()
   * @see	#values()
   * @see	Map
   */
  public synchronized Enumeration elements() {
    return getEnumeration(VALUES);
  }

  /**
   * Tests if some key maps into the specified value in this hashtable.
   * This operation is more expensive than the <code>containsKey</code>
   * method.<p>
   *
   * Note that this method is identical in functionality to containsValue,
   * (which is part of the Map interface in the collections framework).
   *
   * @param      value   a value to search for.
   * @return     <code>true</code> if and only if some key maps to the
   *             <code>value</code> argument in this hashtable as
   *             determined by the <tt>equals</tt> method;
   *             <code>false</code> otherwise.
   * @exception  NullPointerException  if the value is <code>null</code>.
   * @see        #containsKey(Object)
   * @see        #containsValue(Object)
   * @see	   Map
   */
  public synchronized boolean contains(Object value) {
    if (value == null) {
      throw new NullPointerException();
    }

    Entry[] tab = table;
    for (int i = tab.length; i-- > 0; ) {
      for (Entry e = tab[i]; e != null; e = e.next) {
        if (e.value.equals(value)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if this Hashtable maps one or more keys to this value.<p>
   *
   * Note that this method is identical in functionality to contains
   * (which predates the Map interface).
   *
   * @param value value whose presence in this Hashtable is to be tested.
   * @return <tt>true</tt> if this map maps one or more keys to the
   *         specified value.
   * @throws NullPointerException  if the value is <code>null</code>.
   * @see	   Map
   * @since 1.2
   */
  public boolean containsValue(Object value) {
    return contains(value);
  }

  /**
   * Tests if the specified object is a key in this hashtable.
   *
   * @param   key   possible key.
   * @return  <code>true</code> if and only if the specified object
   *          is a key in this hashtable, as determined by the
   *          <tt>equals</tt> method; <code>false</code> otherwise.
   * @throws  NullPointerException  if the key is <code>null</code>.
   * @see     #contains(Object)
   */
  public synchronized boolean containsKey(Object key) {
    Entry[] tab = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index]; e != null; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable.
   *
   * @param   key   a key in the hashtable.
   * @return  the value to which the key is mapped in this hashtable;
   *          <code>null</code> if the key is not mapped to any value in
   *          this hashtable.
   * @throws  NullPointerException  if the key is <code>null</code>.
   * @see     #put(Object, Object)
   */
  public synchronized Object get(Object key) {
    Entry[] tab = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index]; e != null; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        return e.value;
      }
    }
    return null;
  }

  /**
   * Increases the capacity of and internally reorganizes this
   * hashtable, in order to accommodate and access its entries more
   * efficiently.  This method is called automatically when the
   * number of keys in the hashtable exceeds this hashtable's capacity
   * and load factor.
   */
  protected void rehash() {
    int oldCapacity = table.length;
    Entry[] oldMap = table;

    int newCapacity = oldCapacity * 2 + 1;
    Entry[] newMap = new Entry[newCapacity];

    modCount++;
    threshold = (int) (newCapacity * loadFactor);
    table = newMap;

    for (int i = oldCapacity; i-- > 0; ) {
      for (Entry old = oldMap[i]; old != null; ) {
        Entry e = old;
        old = old.next;

        int index = (e.hash & 0x7FFFFFFF) % newCapacity;
        e.next = newMap[index];
        newMap[index] = e;
      }
    }
  }

  /**
   * Maps the specified <code>key</code> to the specified
   * <code>value</code> in this hashtable. Neither the key nor the
   * value can be <code>null</code>. <p>
   *
   * The value can be retrieved by calling the <code>get</code> method
   * with a key that is equal to the original key.
   *
   * @param      key     the hashtable key.
   * @param      value   the value.
   * @return     the previous value of the specified key in this hashtable,
   *             or <code>null</code> if it did not have one.
   * @exception  NullPointerException  if the key or value is
   *               <code>null</code>.
   * @see     Object#equals(Object)
   * @see     #get(Object)
   */
  public synchronized Object put(Object key, Object value) {
    // Make sure the value is not null
    if (value == null) {
      throw new NullPointerException();
    }

    // Makes sure the key is not already in the hashtable.
    Entry[] tab = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index]; e != null; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        Object old = e.value;
        e.value = value;
        return old;
      }
    }

    modCount++;
    if (count >= threshold) {
      // Rehash the table if the threshold is exceeded
      rehash();

      tab = table;
      index = (hash & 0x7FFFFFFF) % tab.length;
    }

    // Creates the new entry.
    Entry e = new Entry(hash, key, value, tab[index]);
    tab[index] = e;
    count++;
    return null;
  }

  /**
   * Removes the key (and its corresponding value) from this
   * hashtable. This method does nothing if the key is not in the hashtable.
   *
   * @param   key   the key that needs to be removed.
   * @return  the value to which the key had been mapped in this hashtable,
   *          or <code>null</code> if the key did not have a mapping.
   * @throws  NullPointerException  if the key is <code>null</code>.
   */
  public synchronized Object remove(Object key) {
    Entry[] tab = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        modCount++;
        if (prev != null) {
          prev.next = e.next;
        } else {
          tab[index] = e.next;
        }
        count--;
        Object oldValue = e.value;
        e.value = null;
        return oldValue;
      }
    }
    return null;
  }

  /**
   * Copies all of the mappings from the specified Map to this Hashtable
   * These mappings will replace any mappings that this Hashtable had for any
   * of the keys currently in the specified Map.
   *
   * @param t Mappings to be stored in this map.
   * @throws NullPointerException if the specified map is null.
   * @since 1.2
   */
  public synchronized void putAll(Map t) {
    Iterator i = t.entrySet().iterator();
    while (i.hasNext()) {
      Map.Entry e = (Map.Entry) i.next();
      put(e.getKey(), e.getValue());
    }
  }

  /**
   * Clears this hashtable so that it contains no keys.
   */
  public synchronized void clear() {
    Entry[] tab = table;
    modCount++;
    for (int index = tab.length; --index >= 0; ) tab[index] = null;
    count = 0;
  }

  /**
   * Creates a shallow copy of this hashtable. All the structure of the
   * hashtable itself is copied, but the keys and values are not cloned.
   * This is a relatively expensive operation.
   *
   * @return  a clone of the hashtable.
   */
  public synchronized Object clone() {
    try {
      Hashtable t = (Hashtable) super.clone();
      t.table = new Entry[table.length];
      for (int i = table.length; i-- > 0; ) {
        t.table[i] = (table[i] != null) ? (Entry) table[i].clone() : null;
      }
      t.keySet = null;
      t.entrySet = null;
      t.values = null;
      t.modCount = 0;
      return t;
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }

  /**
   * Returns a string representation of this <tt>Hashtable</tt> object
   * in the form of a set of entries, enclosed in braces and separated
   * by the ASCII characters "<tt>,&nbsp;</tt>" (comma and space). Each
   * entry is rendered as the key, an equals sign <tt>=</tt>, and the
   * associated element, where the <tt>toString</tt> method is used to
   * convert the key and element to strings. <p>Overrides to
   * <tt>toString</tt> method of <tt>Object</tt>.
   *
   * @return  a string representation of this hashtable.
   */
  public synchronized String toString() {
    int max = size() - 1;
    StringBuilder buf = new StringBuilder();
    Iterator it = entrySet().iterator();

    buf.append("{");
    for (int i = 0; i <= max; i++) {
      Map.Entry e = (Map.Entry) (it.next());
      Object key = e.getKey();
      Object value = e.getValue();
      buf.append((key == this ? "(this Map)" : key) + "=" + (value == this ? "(this Map)" : value));

      if (i < max) buf.append(", ");
    }
    buf.append("}");
    return buf.toString();
  }

  private Enumeration getEnumeration(int type) {
    if (count == 0) {
      return emptyEnumerator;
    } else {
      return new Enumerator(type, false);
    }
  }

  private Iterator getIterator(int type) {
    if (count == 0) {
      return emptyIterator;
    } else {
      return new Enumerator(type, true);
    }
  }

  // Views

  /**
   * Each of these fields are initialized to contain an instance of the
   * appropriate view the first time this view is requested.  The views are
   * stateless, so there's no reason to create more than one of each.
   */
  private transient volatile Set keySet = null;
  private transient volatile Set entrySet = null;
  private transient volatile Collection values = null;

  /**
   * Returns a Set view of the keys contained in this Hashtable.  The Set
   * is backed by the Hashtable, so changes to the Hashtable are reflected
   * in the Set, and vice-versa.  The Set supports element removal
   * (which removes the corresponding entry from the Hashtable), but not
   * element addition.
   *
   * @return a set view of the keys contained in this map.
   * @since 1.2
   */
  public Set keySet() {
    if (keySet == null) keySet = Collections.synchronizedSet(new KeySet(), this);
    return keySet;
  }

  private class KeySet extends AbstractSet {
    public Iterator iterator() {
      return getIterator(KEYS);
    }

    public int size() {
      return count;
    }

    public boolean contains(Object o) {
      return containsKey(o);
    }

    public boolean remove(Object o) {
      return Hashtable.this.remove(o) != null;
    }

    public void clear() {
      Hashtable.this.clear();
    }
  }

  /**
   * Returns a Set view of the entries contained in this Hashtable.
   * Each element in this collection is a Map.Entry.  The Set is
   * backed by the Hashtable, so changes to the Hashtable are reflected in
   * the Set, and vice-versa.  The Set supports element removal
   * (which removes the corresponding entry from the Hashtable),
   * but not element addition.
   *
   * @return a set view of the mappings contained in this map.
   * @see   Map.Entry
   * @since 1.2
   */
  public Set entrySet() {
    if (entrySet == null) entrySet = Collections.synchronizedSet(new EntrySet(), this);
    return entrySet;
  }

  private class EntrySet extends AbstractSet {
    public Iterator iterator() {
      return getIterator(ENTRIES);
    }

    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) return false;
      Map.Entry entry = (Map.Entry) o;
      Object key = entry.getKey();
      Entry[] tab = table;
      int hash = key.hashCode();
      int index = (hash & 0x7FFFFFFF) % tab.length;

      for (Entry e = tab[index]; e != null; e = e.next)
        if (e.hash == hash && e.equals(entry)) return true;
      return false;
    }

    public boolean remove(Object o) {
      if (!(o instanceof Map.Entry)) return false;
      Map.Entry entry = (Map.Entry) o;
      Object key = entry.getKey();
      Entry[] tab = table;
      int hash = key.hashCode();
      int index = (hash & 0x7FFFFFFF) % tab.length;

      for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
        if (e.hash == hash && e.equals(entry)) {
          modCount++;
          if (prev != null) prev.next = e.next;
          else tab[index] = e.next;

          count--;
          e.value = null;
          return true;
        }
      }
      return false;
    }

    public int size() {
      return count;
    }

    public void clear() {
      Hashtable.this.clear();
    }
  }

  /**
   * Returns a Collection view of the values contained in this Hashtable.
   * The Collection is backed by the Hashtable, so changes to the Hashtable
   * are reflected in the Collection, and vice-versa.  The Collection
   * supports element removal (which removes the corresponding entry from
   * the Hashtable), but not element addition.
   *
   * @return a collection view of the values contained in this map.
   * @since 1.2
   */
  public Collection values() {
    if (values == null) values = Collections.synchronizedCollection(new ValueCollection(), this);
    return values;
  }

  private class ValueCollection extends AbstractCollection {
    public Iterator iterator() {
      return getIterator(VALUES);
    }

    public int size() {
      return count;
    }

    public boolean contains(Object o) {
      return containsValue(o);
    }

    public void clear() {
      Hashtable.this.clear();
    }
  }

  // Comparison and hashing

  /**
   * Compares the specified Object with this Map for equality,
   * as per the definition in the Map interface.
   *
   * @param  o object to be compared for equality with this Hashtable
   * @return true if the specified Object is equal to this Map.
   * @see Map#equals(Object)
   * @since 1.2
   */
  public synchronized boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof Map)) return false;
    Map t = (Map) o;
    if (t.size() != size()) {
      return false;
    }

    try {
      Iterator i = entrySet().iterator();
      while (i.hasNext()) {
        Map.Entry e = (Map.Entry) i.next();
        Object key = e.getKey();
        Object value = e.getValue();
        if (value == null) {
          if (!(t.get(key) == null && t.containsKey(key))) return false;
        } else {
          if (!value.equals(t.get(key))) return false;
        }
      }
    } catch (ClassCastException unused) {
      return false;
    } catch (NullPointerException unused) {
      return false;
    }

    return true;
  }

  /**
   * Returns the hash code value for this Map as per the definition in the
   * Map interface.
   *
   * @see Map#hashCode()
   * @since 1.2
   */
  public synchronized int hashCode() {
    /*
     * This code detects the recursion caused by computing the hash code
     * of a self-referential hash table and prevents the stack overflow
     * that would otherwise result.  This allows certain 1.1-era
     * applets with self-referential hash tables to work.  This code
     * abuses the loadFactor field to do double-duty as a hashCode
     * in progress flag, so as not to worsen the space performance.
     * A negative load factor indicates that hash code computation is
     * in progress.
     */
    int h = 0;
    if (count == 0 || loadFactor < 0) return h; // Returns zero

    loadFactor = -loadFactor; // Mark hashCode computation in progress
    Entry[] tab = table;
    for (int i = 0; i < tab.length; i++)
      for (Entry e = tab[i]; e != null; e = e.next) h += e.key.hashCode() ^ e.value.hashCode();
    loadFactor = -loadFactor; // Mark hashCode computation complete

    return h;
  }

  /**
   * Save the state of the Hashtable to a stream (i.e., serialize it).
   *
   * @serialData The <i>capacity</i> of the Hashtable (the length of the
   *		   bucket array) is emitted (int), followed  by the
   *		   <i>size</i> of the Hashtable (the number of key-value
   *		   mappings), followed by the key (Object) and value (Object)
   *		   for each key-value mapping represented by the Hashtable
   *		   The key-value mappings are emitted in no particular order.
   */
  private synchronized void writeObject(java.io.ObjectOutputStream s) throws IOException {
    // Write out the length, threshold, loadfactor
    s.defaultWriteObject();

    // Write out length, count of elements and then the key/value objects
    s.writeInt(table.length);
    s.writeInt(count);
    for (int index = table.length - 1; index >= 0; index--) {
      Entry entry = table[index];

      while (entry != null) {
        s.writeObject(entry.key);
        s.writeObject(entry.value);
        entry = entry.next;
      }
    }
  }

  /**
   * Reconstitute the Hashtable from a stream (i.e., deserialize it).
   */
  private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
    // Read in the length, threshold, and loadfactor
    s.defaultReadObject();

    // Read the original length of the array and number of elements
    int origlength = s.readInt();
    int elements = s.readInt();

    // Compute new size with a bit of room 5% to grow but
    // No larger than the original size.  Make the length
    // odd if it's large enough, this helps distribute the entries.
    // Guard against the length ending up zero, that's not valid.
    int length = (int) (elements * loadFactor) + (elements / 20) + 3;
    if (length > elements && (length & 1) == 0) length--;
    if (origlength > 0 && length > origlength) length = origlength;

    table = new Entry[length];
    count = 0;

    // Read the number of elements and then all the key/value objects
    for (; elements > 0; elements--) {
      Object key = s.readObject();
      Object value = s.readObject();
      put(key, value); // synch could be eliminated for performance
    }
  }

  /**
   * Hashtable collision list.
   */
  private static class Entry implements Map.Entry {
    int hash;
    Object key;
    Object value;
    Entry next;

    protected Entry(int hash, Object key, Object value, Entry next) {
      this.hash = hash;
      this.key = key;
      this.value = value;
      this.next = next;
    }

    protected Object clone() {
      return new Entry(hash, key, value, (next == null ? null : (Entry) next.clone()));
    }

    // Map.Entry Ops

    public Object getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    public Object setValue(Object value) {
      if (value == null) {
        throw new NullPointerException();
      }

      Object oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry)) return false;
      Map.Entry e = (Map.Entry) o;

      return (key == null ? e.getKey() == null : key.equals(e.getKey()))
          && (value == null ? e.getValue() == null : value.equals(e.getValue()));
    }

    public int hashCode() {
      return hash ^ (value == null ? 0 : value.hashCode());
    }

    public String toString() {
      return key.toString() + "=" + value.toString();
    }
  }

  // Types of Enumerations/Iterations
  private static final int KEYS = 0;
  private static final int VALUES = 1;
  private static final int ENTRIES = 2;

  /**
   * A hashtable enumerator class.  This class implements both the
   * Enumeration and Iterator interfaces, but individual instances
   * can be created with the Iterator methods disabled.  This is necessary
   * to avoid unintentionally increasing the capabilities granted a user
   * by passing an Enumeration.
   */
  private class Enumerator implements Enumeration, Iterator {
    Entry[] table = Hashtable.this.table;
    int index = table.length;
    Entry entry = null;
    Entry lastReturned = null;
    int type;

    /**
     * Indicates whether this Enumerator is serving as an Iterator
     * or an Enumeration.  (true -> Iterator).
     */
    boolean iterator;

    /**
     * The modCount value that the iterator believes that the backing
     * List should have.  If this expectation is violated, the iterator
     * has detected concurrent modification.
     */
    protected int expectedModCount = modCount;

    Enumerator(int type, boolean iterator) {
      this.type = type;
      this.iterator = iterator;
    }

    public boolean hasMoreElements() {
      Entry e = entry;
      int i = index;
      Entry[] t = table;
      /* Use locals for faster loop iteration */
      while (e == null && i > 0) {
        e = t[--i];
      }
      entry = e;
      index = i;
      return e != null;
    }

    public Object nextElement() {
      Entry et = entry;
      int i = index;
      Entry[] t = table;
      /* Use locals for faster loop iteration */
      while (et == null && i > 0) {
        et = t[--i];
      }
      entry = et;
      index = i;
      if (et != null) {
        Entry e = lastReturned = entry;
        entry = e.next;
        return type == KEYS ? e.key : (type == VALUES ? e.value : e);
      }
      throw new NoSuchElementException("Hashtable Enumerator");
    }

    // Iterator methods
    public boolean hasNext() {
      return hasMoreElements();
    }

    public Object next() {
      if (modCount != expectedModCount) throw new ConcurrentModificationException();
      return nextElement();
    }

    public void remove() {
      if (!iterator) throw new UnsupportedOperationException();
      if (lastReturned == null) throw new IllegalStateException("Hashtable Enumerator");
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }

      synchronized (Hashtable.this) {
        Entry[] tab = Hashtable.this.table;
        int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;

        for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
          if (e == lastReturned) {
            modCount++;
            expectedModCount++;
            if (prev == null) tab[index] = e.next;
            else prev.next = e.next;
            count--;
            lastReturned = null;
            return;
          }
        }
        throw new ConcurrentModificationException();
      }
    }
  }

  private static EmptyEnumerator emptyEnumerator = new EmptyEnumerator();
  private static EmptyIterator emptyIterator = new EmptyIterator();

  /**
   * A hashtable enumerator class for empty hash tables, specializes
   * the general Enumerator
   */
  private static class EmptyEnumerator implements Enumeration {

    EmptyEnumerator() {}

    public boolean hasMoreElements() {
      return false;
    }

    public Object nextElement() {
      throw new NoSuchElementException("Hashtable Enumerator");
    }
  }

  /**
   * A hashtable iterator class for empty hash tables
   */
  private static class EmptyIterator implements Iterator {

    EmptyIterator() {}

    public boolean hasNext() {
      return false;
    }

    public Object next() {
      throw new NoSuchElementException("Hashtable Iterator");
    }

    public void remove() {
      throw new IllegalStateException("Hashtable Iterator");
    }
  }
}
