//// This is java.util.LinkedList from the Standard Java Library, //KORAT
//// with small changes that allow checking with Korat. //KORAT
/*
 * @(#)LinkedList.java    1.32 00/02/02
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
//import korat.*; //KORAT

/**
 * Linked list implementation of the <tt>List</tt> interface.  Implements all
 * optional list operations, and permits all elements (including
 * <tt>null</tt>).  In addition to implementing the <tt>List</tt> interface,
 * the <tt>LinkedList</tt> class provides uniformly named methods to
 * <tt>get</tt>, <tt>remove</tt> and <tt>insert</tt> an element at the
 * beginning and end of the list.  These operations allow linked lists to be
 * used as a stack, queue, or double-ended queue (deque).<p>
 *
 * All of the stack/queue/deque operations could be easily recast in terms of
 * the standard list operations.  They're included here primarily for
 * convenience, though they may run slightly faster than the equivalent List
 * operations.<p>
 *
 * All of the operations perform as could be expected for a doubly-linked
 * list.  Operations that index into the list will traverse the list from
 * the begining or the end, whichever is closer to the specified index.<p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a list concurrently, and at least one of the threads
 * modifies the list structurally, it <i>must</i> be synchronized
 * externally.  (A structural modification is any operation that adds or
 * deletes one or more elements; merely setting the value of an element is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the list.  If no
 * such object exists, the list should be "wrapped" using the
 * Collections.synchronizedList method.  This is best done at creation time,
 * to prevent accidental unsynchronized access to the list: <pre>
 *     List list = Collections.synchronizedList(new LinkedList(...));
 * </pre><p>
 *
 * The iterators returned by the this class's <tt>iterator</tt> and
 * <tt>listIterator</tt> methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in any way
 * except through the Iterator's own <tt>remove</tt> or <tt>add</tt> methods,
 * the iterator will throw a <tt>ConcurrentModificationException</tt>.  Thus,
 * in the face of concurrent modification, the iterator fails quickly and
 * cleanly, rather than risking arbitrary, non-deterministic behavior at an
 * undetermined time in the future.
 *
 * @author  Josh Bloch
 * @version 1.32, 02/02/00
 * @see        List
 * @see        ArrayList
 * @see        Vector
 * @see        Collections#synchronizedList(List)
 * @since 1.2
 */

public class LinkedList extends AbstractSequentialList
                /*implements List, Cloneable, java.io.Serializable*/
{
    public transient Entry header = new Entry(null, null, null);// KORAT
    //KORAT private transient Entry header = new Entry(null, null, null);
    private transient int size = 0;

    /**
     * Constructs an empty list.
     */
    public LinkedList() {
        header.next = header.previous = header;
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list.
     */
    /*
     public LinkedList(Collection c) {
     this();
     addAll(c);
     }
     */

    /**
     * Returns the first element in this list.
     *
     * @return the first element in this list.
     */
    /*
    public Object getFirst() {
    if (size==0)
        throw new NoSuchElementException();

    return header.next.element;
    }
    */

    /**
     * Returns the last element in this list.
     *
     * @return the last element in this list.
     * @throws    NoSuchElementException if this list is empty.
     */
    /*
    public Object getLast()  {
    if (size==0)
        throw new NoSuchElementException();

    return header.previous.element;
    }
    */

    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list.
     * @throws    NoSuchElementException if this list is empty.
     */
    /*@ public normal_behavior
      @     requires repOk();
      @     ensures repOk() && \result.equals(\old(header.next.element));
      @*/
     /*
    public Object removeFirst() {
    //MUTANT Object first = header.previous.element;
        Object first = header.next.element;
    remove(header.next);
    return first;
    }
    */

    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list.
     * @throws    NoSuchElementException if this list is empty.
     */
    
    //public Object removeLast() {
    
    public Integer removeLast() {
    Integer last = header.previous.element;
    remove(header.previous);
    return last;
    }
    

    /**
     * Inserts the given element at the beginning of this list.
     * 
     * @param o the element to be inserted at the beginning of this list.
     */
    /*
    public void addFirst(Object o) {
    addBefore(o, header.next);
    }
    */

    /**
     * Appends the given element to the end of this list.  (Identical in
     * function to the <tt>add</tt> method; included only for consistency.)
     * 
     * @param o the element to be inserted at the end of this list.
     */
    /*
    public void addLast(Object o) {
    addBefore(o, header);
    }
    */

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that <tt>(o==null ? e==null
     * : o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified element.
     */
    /*
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }
    */

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list.
     */
    /*
    public int size() {
    return size;
    }
    */

    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of
     * <tt>Collection.add</tt>).
     */
    //public boolean add(Object o) {
    public boolean add(Integer o) {
    addBefore(o, header);
        return true;
    }

    /**
     * Removes the first occurrence of the specified element in this list.  If
     * the list does not contain the element, it is unchanged.  More formally,
     * removes the element with the lowest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if such an
     * element exists).
     *
     * @param o element to be removed from this list, if present.
     * @return <tt>true</tt> if the list contained the specified element.
     */
    /*@ public normal_behavior
      @     requires repOk();
      @     ensures repOk();
      @*/
    //public boolean remove(Object o) {
    public boolean remove(Integer o) {
        /*
        if (o==null) {
            for (Entry e = header.next; e != header; e = e.next) {
                if (e.element==null) {
                    remove(e);
                    return true;
                }
            }
        } else {*/
            for (Entry e = header.next; e != header; e = e.next) {
                if (o.equals(e.element)) {
                    remove(e);
                    return true;
                }
            }
        //}
        return false;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator.  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in
     * progress.  (This implies that the behavior of this call is undefined if
     * the specified Collection is this list, and this list is nonempty.)
     *
     * @param c the elements to be inserted into this list.
     * 
     * @throws IndexOutOfBoundsException if the specified index is out of
     *         range (<tt>index &lt; 0 || index &gt; size()</tt>).
     */
    /*
    public boolean addAll(Collection c) {
        return addAll(size, c);
    }
    */

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert first element
     *            from the specified collection.
     * @param c elements to be inserted into this list.
     * @throws IndexOutOfBoundsException if the specified index is out of
     *            range (<tt>index &lt; 0 || index &gt; size()</tt>).
     */
    /*
    public boolean addAll(int index, Collection c) {
    int numNew = c.size();
        if (numNew==0)
            return false;
    modCount++;

        Entry successor = (index==size ? header : entry(index));
        Entry predecessor = successor.previous;
    Iterator it = c.iterator();
    for (int i=0; i<numNew; i++) {
            Entry e = new Entry(it.next(), successor, predecessor);
            predecessor.next = e;
            predecessor = e;
        }
        successor.previous = predecessor;

        size += numNew;
        return true;
    }
    */

    /**
     * Removes all of the elements from this list.
     */
    /*
    public void clear() {
    modCount++;
        header.next = header.previous = header;
    size = 0;
    }
    */


    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * 
     * @throws IndexOutOfBoundsException if the specified index is is out of
     * range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    /*
    public Object get(int index) {
        return entry(index).element;
    }
    */

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if the specified index is out of
     *          range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    /*
    public Object set(int index, Object element) {
        Entry e = entry(index);
        Object oldVal = e.element;
        e.element = element;
        return oldVal;
    }
    */
    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * 
     * @throws IndexOutOfBoundsException if the specified index is out of
     *          range (<tt>index &lt; 0 || index &gt; size()</tt>).
     */
    /*
    public void add(int index, Object element) {
        addBefore(element, (index==size ? header : entry(index)));
    }
    */

    /**
     * Removes the element at the specified position in this list.  Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     * 
     * @throws IndexOutOfBoundsException if the specified index is out of
     *           range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    
    //public Object remove(int index) {
    /*
    public Object removeIndex(Integer index) {
        Entry e = entry(index);
        remove(e);
        return e.element;
    }
    */

    /**
     * Return the indexed entry.
     */    
    
    /*
    //private Entry entry(int index) {
    private Entry entry(Integer index) {
        //if (index < 0 || index >= size)
        if (index.compareTo(new Integer(0)) < 0 || index.compareTo(new Integer(size)) >= 0)
            throw new IndexOutOfBoundsException("Index: "+index+
                                                ", Size: "+size);
        Entry e = header;
        //if (index < size/2) {
        if (index.compareTo(new Integer(size/2)) < 0) {
            //for (int i = 0; i <= index; i++)
            for (int i = 0; index.compareTo(new Integer(i)) >= 0; i++)
                e = e.next;
        } else {
            //for (int i = size; i > index; i--)
            for (int i = size; index.compareTo(new Integer(i)) < 0; i--)
                e = e.previous;
        }
        return e;
    }
    */

    // Search Operations

    /**
     * Returns the index in this list of the first occurrence of the
     * specified element, or -1 if the List does not contain this
     * element.  More formally, returns the lowest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if
     * there is no such index.
     *
     * @param o element to search for.
     * @return the index in this list of the first occurrence of the
     *            specified element, or -1 if the list does not contain this
     *            element.
     */
    /*
    public int indexOf(Object o) {
        int index = 0;
        if (o==null) {
            for (Entry e = header.next; e != header; e = e.next) {
                if (e.element==null)
                    return index;
                index++;
            }
        } else {
            for (Entry e = header.next; e != header; e = e.next) {
                if (o.equals(e.element))
                    return index;
                index++;
            }
        }
        return -1;
    }
    */

    /**
     * Returns the index in this list of the last occurrence of the
     * specified element, or -1 if the list does not contain this
     * element.  More formally, returns the highest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if
     * there is no such index.
     *
     * @param o element to search for.
     * @return the index in this list of the last occurrence of the
     *            specified element, or -1 if the list does not contain this
     *            element.
     */
    /*
    public int lastIndexOf(Object o) {
        int index = size;
        if (o==null) {
            for (Entry e = header.previous; e != header; e = e.previous) {
                index--;
                if (e.element==null)
                    return index;
            }
        } else {
            for (Entry e = header.previous; e != header; e = e.previous) {
                index--;
                if (o.equals(e.element))
                    return index;
            }
        }
        return -1;
    }
    */

    /**
     * Returns a list-iterator of the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * Obeys the general contract of <tt>List.listIterator(int)</tt>.<p>
     *
     * The list-iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list-iterator's own <tt>remove</tt> or <tt>add</tt>
     * methods, the list-iterator will throw a
     * <tt>ConcurrentModificationException</tt>.  Thus, in the face of
     * concurrent modification, the iterator fails quickly and cleanly, rather
     * than risking arbitrary, non-deterministic behavior at an undetermined
     * time in the future.
     *
     * @param index index of first element to be returned from the
     *            list-iterator (by a call to <tt>next</tt>).
     * @return a ListIterator of the elements in this list (in proper
     *            sequence), starting at the specified position in the list.
     * @throws    IndexOutOfBoundsException if index is out of range
     *          (<tt>index &lt; 0 || index &gt; size()</tt>).
     * @see List#listIterator(int)
     */
    /*
    public ListIterator listIterator(int index) {
    return new ListItr(index);
    }
    */

    //KORAT private class ListItr implements ListIterator {
    public class ListItr implements ListIterator { // KORAT
    private Entry lastReturned = header;
    private Entry next;
    private int nextIndex;
    //private int expectedModCount = modCount;

    ListItr(int index) {
        if (index < 0 || index > size)
        throw new IndexOutOfBoundsException("Index: "+index+
                            ", Size: "+size);
        if (index < size/2) {
        next = header.next;
        for (nextIndex=0; nextIndex<index; nextIndex++)
            next = next.next;
        } else {
        next = header;
        for (nextIndex=size; nextIndex>index; nextIndex--)
            next = next.previous;
        }
    }

    public boolean hasNext() {
        return nextIndex != size;
    }

    public Object next() {
        checkForComodification();
        if (nextIndex == size)
        throw new NoSuchElementException();

        lastReturned = next;
        next = next.next;
        nextIndex++;
        return lastReturned.element;
    }

    public boolean hasPrevious() {
        return nextIndex != 0;
    }

    public Object previous() {
        if (nextIndex == 0)
        throw new NoSuchElementException();

        lastReturned = next = next.previous;
        nextIndex--;
        checkForComodification();
        return lastReturned.element;
    }

    public int nextIndex() {
        return nextIndex;
    }

    public int previousIndex() {
        return nextIndex-1;
    }

    public void remove() {
        LinkedList.this.remove(lastReturned);
        if (next==lastReturned)
                next = lastReturned.next;
            else
        nextIndex--;
        lastReturned = header;
        //expectedModCount++;
    }

    public void set(Object o) {
    //public void set(Integer o) {
        if (lastReturned == header)
        throw new IllegalStateException();
        checkForComodification();
        lastReturned.element = (Integer)o;
    }

    public void add(Object o) {
    //public void add(Integer o) {
        checkForComodification();
        lastReturned = header;
        addBefore((Integer)o, next);
        nextIndex++;
        //expectedModCount++;
    }

        final void checkForComodification() {
            /*if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
                */
        }
    }
    //JML dies on java.util.Collections//JML
    /*@ public normal_behavior//JML
      @     requires repOk();//JML
      @     ensures repOk();//JML
      @*///JML
    /*
    public void reverse() {//JML
        Collections.reverse(this);//JML
    }
    */
    //JML
    //JML
     

    //KORAT private static class Entry {
    public static class Entry { //KORAT
    //Object element;
    Integer element;
    Entry next;
    Entry previous;

    //Entry(Object element, Entry next, Entry previous) {
    Entry(Integer element, Entry next, Entry previous) {
        this.element = element;
        this.next = next;
        this.previous = previous;
    }
    }

    //private Entry addBefore(Object o, Entry e) {
    private Entry addBefore(Integer o, Entry e) {
    Entry newEntry = new Entry(o, e, e.previous);
    newEntry.previous.next = newEntry;
    newEntry.next.previous = newEntry;
    size++;
    //modCount++;
    return newEntry;
    }

    private void remove(Entry e) {
    if (e == header)
        throw new NoSuchElementException();

    e.previous.next = e.next;
    e.next.previous = e.previous;
    size--;
    //modCount++;
    }

    /**
     * Returns a shallow copy of this <tt>LinkedList</tt>. (The elements
     * themselves are not cloned.)
     *
     * @return a shallow copy of this <tt>LinkedList</tt> instance.
     */
    /*
    public Object clone() {
        LinkedList clone = null;
    try { 
        clone = (LinkedList)super.clone();
    } catch (CloneNotSupportedException e) { 
        throw new InternalError();
    }

        // Put clone into "virgin" state
        clone.header = new Entry(null, null, null);
        clone.header.next = clone.header.previous = clone.header;
        clone.size = 0;
        clone.modCount = 0;

        // Initialize clone with our elements
        for (Entry e = header.next; e != header; e = e.next)
            clone.add(e.element);

        return clone;
    }
    */

    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @return an array containing all of the elements in this list
     *            in the correct order.
     */
    /*
    public Object[] toArray() {
    Object[] result = new Object[size];
        int i = 0;
        for (Entry e = header.next; e != header; e = e.next)
            result[i++] = e.element;
    return result;
    }
    */

    /**
     * Returns an array containing all of the elements in this list in
     * the correct order.  The runtime type of the returned array is that of
     * the specified array.  If the list fits in the specified array, it
     * is returned therein.  Otherwise, a new array is allocated with the
     * runtime type of the specified array and the size of this list.<p>
     *
     * If the list fits in the specified array with room to spare
     * (i.e., the array has more elements than the list),
     * the element in the array immediately following the end of the
     * collection is set to null.  This is useful in determining the length
     * of the list <i>only</i> if the caller knows that the list
     * does not contain any null elements.
     *
     * @param a the array into which the elements of the list are to
     *        be stored, if it is big enough; otherwise, a new array of the
     *         same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException if the runtime type of a is not a
     *         supertype of the runtime type of every element in this list.
     */
    /*
    public Object[] toArray(Object a[]) {
        if (a.length < size)
            a = (Object[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);
        int i = 0;
        for (Entry e = header.next; e != header; e = e.next)
            a[i++] = e.element;

        if (a.length > size)
            a[size] = null;

        return a;
    }
    */

    //private static final long serialVersionUID = 876323262645176354L;

    /**
     * Save the state of this <tt>LinkedList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The size of the list (the number of elements it
     *           contains) is emitted (int), followed by all of its
     * elements (each an Object) in the proper order.  */
    /*
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
    // Write out any hidden serialization magic
    s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

    // Write out all elements in the proper order.
        for (Entry e = header.next; e != header; e = e.next)
            s.writeObject(e.element);
    }
    */

    /**
     * Reconstitute this <tt>LinkedList</tt> instance from a stream (that is
     * deserialize it).
     */
    /*
    private synchronized void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
    // Read in any hidden serialization magic
    s.defaultReadObject();

        // Read in size
        int size = s.readInt();

        // Initialize header
        header = new Entry(null, null, null);
        header.next = header.previous = header;

    // Read in all elements in the proper order.
    for (int i=0; i<size; i++)
            add(s.readObject());
    }
    */
    /*
    //KORAT-START
    public boolean repOk(){
        return repOkNS();
    }

    public boolean repOkCommon() {
    if (header == null) return false;
        if (header.element != null) return false;
        Set visited = new java.util.HashSet();
    visited.add(header);
        Entry current = header;
        while (true) {
            Entry next = current.next;
            if (next == null) return false;
            if (next.previous != current) return false;
            current = next;
            if (!visited.add(next)) break;
        }
        if (current != header) return false; //// maybe not needed (also in SortedList.java)
        if (visited.size()-1 != size) return false;
        return true;
    }

    public boolean repOkNS() {
        if (!repOkCommon()) return false;
        for (Entry current = header.next; current != header; 
             current = current.next) {
            touch(current.element) ;
        }
        return true;
    }
    void touch(Object o) {}

    public boolean repOkSorted() {
        if (!repOkCommon()) return false;
        // check for sorted
        if ((header.next != header) && (!(header.next.element instanceof Comparable)))
            return false;
        for (Entry current = header.next; current.next != header; 
             current = current.next) {
            if ((!(current.next.element instanceof Comparable)) || (((Comparable)current.element).compareTo((Comparable)current.next.element) > 0))
                return false;
        }
        return true;
    }

    final boolean _KORAT_POSTPROCESS_isListWithPrevious_Entry_next_previous(Entry header) { return true; }

    public boolean repOkSpecial() {
        return repOkNSSpecial();
    }

    public boolean repOkCommonSpecial() {
    if (header == null) return false;
        if (header.element != null) return false;
        if (!_KORAT_POSTPROCESS_isListWithPrevious_Entry_next_previous(header)) return false;
        int num = 0;
        for (Entry current = header.next; current != header; 
             current = current.next) {
            num++;
        }
        if (num != size) return false;
        return true;
    }

    public boolean repOkNSSpecial() {
        if (!repOkCommonSpecial()) return false;
        for (Entry current = header.next; current != header; 
             current = current.next) {
            if (current.element==current.element) ;
        }
        return true;
    }

    public boolean repOkSortedSpecial() {
        if (!repOkCommonSpecial()) return false;
        if ((header.next != header) && (!(header.next.element instanceof Comparable)))
            return false;
        for (Entry current = header.next; current.next != header; 
             current = current.next) {
            if ((!(current.next.element instanceof Comparable)) || (((Comparable)current.element).compareTo((Comparable)current.next.element) > 0))
                return false;
        }
        return true;
    }
    

    // requires this - sorted
    public void insert(Object o){
        Entry current = header.next;
        while ((current != header) && (((Comparable)current.element).compareTo((Comparable)o) < 0)){
            current = current.next;
        }
        addBefore(o, current);
    }

    // requires : this and l - sorted
    public void merge(LinkedList l){
        Iterator i = l.iterator();
        Entry current = header.next;
        Comparable that = null;
        if (i.hasNext())
            that = (Comparable)i.next();
        while (that != null) {
            if ((current == header) || (((Comparable)current.element).compareTo(that) >= 0)){
                current.previous = (current.previous.next = new Entry(that, current, current.previous));
                size++;
                if (i.hasNext())
                    that = (Comparable)i.next();
                else
                    that = null;
            } else {
                current = current.next;
            }
        }
    }

    // requires repOk();
    // returns true if all elements are Comparable.
    public boolean comparableElements(){
        for(Iterator i = this.iterator(); i.hasNext();){
            if (!(i.next() instanceof Comparable))
                return false;
        }
        return true;
    }
    //KORAT-END
     */
}
