package randoop.test;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import junit.framework.TestCase;
import randoop.ContractCheckingVisitor;
import randoop.ExecutableSequence;
import randoop.Globals;
import randoop.LineRemover;
import randoop.ObjectContract;
import randoop.Sequence;

public class LineRemoverTests2 extends TestCase{

  String CODE1= 
    "var0 = prim int 3; " + Globals.lineSep + ""+                                  
    "var1 = cons java.lang.Integer.<init>(int) var0; " + Globals.lineSep + ""+     //to remove
    "var2 = cons java.util.ArrayList.<init>(int) var1; " + Globals.lineSep + "" +  //to remove
    "var3 = prim int 4; " + Globals.lineSep + ""+                                  //to remove
    "var4 = cons " + A4Faulty.class.getName()+ ".<init>(int) var0;";

  String CODE2= 
    "var0 = prim int 3; " + Globals.lineSep + ""+                                  
    "var1 = cons java.lang.Integer.<init>(int) var0; " + Globals.lineSep + ""+     //to remove
    "var2 = cons java.util.ArrayList.<init>(int) var1; " + Globals.lineSep + "" +  //to remove
    "var3 = prim int 4; " + Globals.lineSep + ""+                                  //to remove
    "var4 = cons java.util.ArrayList.<init>(int) var1; " + Globals.lineSep + "" +  //to remove
    "var5 = cons " + A4Faulty.class.getName()+ ".<init>(int) var0;";

  public void test1() throws Exception {

    if (true) return;

    final Sequence seq = Sequence.parse(CODE1);
//  System.out.println(seq.toCodeString());

    ExecutableSequence result =new ExecutableSequence(seq);
    result.execute(new ContractCheckingVisitor(Collections.<ObjectContract>emptyList(), true));
//  System.out.println(result.toCodeString());

    ExecutableSequence minimize = LineRemover.minimize(result);
//  System.out.println(minimize.toCodeString());
    assertEquals(5, minimize.sequence.size());
    assertTrue(minimize.toCodeString(), minimize.toCodeString().contains("EqualsNotReflexive"));

    ExecutableSequence minNoDummies = LineRemover.minimize(result);
//  System.out.println(minNoDummies.toCodeString());

//  assertEquals(2, minNoDummies.sequence.size());
//  assertTrue(minNoDummies.toCodeString(), minNoDummies.toCodeString().contains("EqualsNotReflexive"));
  }

  public void testEqualAfterMinimization() throws Exception {

    // Currently off. Turn on after rework on minimizer.
    if (true) return;

    Sequence seq1 = Sequence.parse(CODE1);
    ExecutableSequence result1 =new ExecutableSequence(seq1);
    result1.execute(new ContractCheckingVisitor(Collections.<ObjectContract>emptyList(), true));
    ExecutableSequence minNoDummies1 = LineRemover.minimize(result1);
//  System.out.println(minNoDummies1.toCodeString());

    Sequence seq2 = Sequence.parse(CODE2);
    ExecutableSequence result2 =new ExecutableSequence(seq2);
    result2.execute(new ContractCheckingVisitor(Collections.<ObjectContract>emptyList(), true));
    ExecutableSequence minNoDummies2 = LineRemover.minimize(result2);
//  System.out.println(minNoDummies2.toCodeString());

    //the codes should be same
    assertEquals(minNoDummies1.toCodeString(), minNoDummies2.toCodeString());

    //the sequences should be same too
    assertTrue(minNoDummies1.equals(minNoDummies2));
  }

  public void testMinimization() throws Exception {
    if (true) return;
    String CODE= 
      "var0 = cons " + A5Faulty.class.getName()+ ".<init>(); " + Globals.lineSep + "" +
      "var1 = method " + A5Faulty.class.getName()+ ".foo() var0;"; 

    Sequence seq1 = Sequence.parse(CODE);
    ExecutableSequence result1 =new ExecutableSequence(seq1);
    result1.execute(new ContractCheckingVisitor(Collections.<ObjectContract>emptyList(), true));
    ExecutableSequence minNoDummies1 = LineRemover.minimize(result1);
    assertEquals(2, minNoDummies1.sequence.size());
  }

  public void testMinimization2() throws Exception {
    if (true) return;
    String CODE= 
      "var0 = cons " + Vec.class.getName()+ ".<init>(); " + Globals.lineSep + "" +
      "var1 = method " + Vec.class.getName()+ ".push(" + Object.class.getName()+ ") var0 var0;"; 

    Sequence seq1 = Sequence.parse(CODE);
    ExecutableSequence result1 =new ExecutableSequence(seq1);
    result1.execute(new ContractCheckingVisitor(Collections.<ObjectContract>emptyList(), true));
    ExecutableSequence minNoDummies1 = LineRemover.minimize(result1);
    assertEquals(2, minNoDummies1.sequence.size());
  }

//Problem when running this from ANT - never finishes    
//public void testMinimization3() throws Exception {
//String CODE= 
//"var0 = cons " + A5Faulty.class.getName()+ ".<init>();" + Globals.lineSep + "" +
//"var1 = cons " + NonterminatingInputTest.Looper.class.getName()+ ".<init>();" + Globals.lineSep + "" +
//"var2 = cons " + A5Faulty.class.getName()+ ".<init>();";

//Sequence seq1 = SequenceParser.parse(CODE);
//ExecutableSequence result1 = new ExecutableSequence(seq1);
//result1.execute(new ContractCheckingVisitor(new ContractCheckingVisitor(ContractCheckingVisitor.allCheckers())));
//ExecutableSequence minNoDummies1 = LineRemover.minimize(result1, true);
//}

  public void testMinimization4() throws Exception {
    if (true)   //disabled because it's not implemented
      return;
    String CODE= 
      "var0 = prim int 3; " + Globals.lineSep + ""+                                  
      "var1 = cons java.lang.Integer.<init>(int) var0; " + Globals.lineSep + ""+     //to remove
      "var2 = prim int 4; " + Globals.lineSep + ""+                                  //to remove
      "var3 = cons " + A6FaultyToStringBlowsUp.class.getName()+ ".<init>(int) var0;" + Globals.lineSep + "" +
      "var4 = method " + A6FaultyToStringBlowsUp.class.getName()+ ".foo() var3;";//to remove

    Sequence seq1 = Sequence.parse(CODE);
    System.out.println(seq1.toCodeString());
    System.out.println("----------------------------------");
    ExecutableSequence result1 = new ExecutableSequence(seq1);
    result1.execute(new ContractCheckingVisitor(Collections.<ObjectContract>emptyList(), true));
    System.out.println(result1.toCodeString());
    System.out.println("----------------------------------");
    ExecutableSequence minNoDummies1 = LineRemover.minimize(result1);
    System.out.println(minNoDummies1.toCodeString());
    System.out.println("----------------------------------");
    assertEquals(2, minNoDummies1.sequence.size());
  }

  // Subject classes
  //----------------------------------------------------------------------------------

  //class with a fault : this.equals(this) returns true
  public static class A4Faulty{
    public A4Faulty(int i) { /*nothing*/}
    @Override
    public boolean equals(Object obj) { return false; }
  }

  //class with a fault : after calling foo calling toString blows up  
  public static class A5Faulty{
    public A5Faulty() { /*nothing*/}
    private Object x; 
    public int foo(){
      this.x= this;
      return 3;
    }
    @Override
    public String toString() {
      return "x" + String.valueOf(x);
    }
    @Override
    public boolean equals(Object o) {
      if (x != null) return x.equals(o);
      return o == this;
    }
  }

  public static class A6FaultyToStringBlowsUp{
    public A6FaultyToStringBlowsUp(int i) { /*nothing*/}
    public int foo(){ return 3;}
    @Override
    public String toString() {
      throw new NullPointerException();
    }
  }    

  public static class A7FaultyStackOverflow{
    private Object x;
    public A7FaultyStackOverflow(int i) { /*nothing*/}
    public int foo(Object o){ this.x= o; return 2;}
    @Override
    public String toString() {
      return "x" + String.valueOf(x);
    }
  }    

  public static class Vec<T> implements Serializable, IVec<T> {

    private static final long serialVersionUID = 1L;

    private static final int RANDOM_SEED = 91648253;

    public Vec() {
      this(5);
    }

    @SuppressWarnings("unchecked")
    public Vec(int size) {
      myarray = (T[]) new Object[size];
    }

    @SuppressWarnings("unchecked")
    public Vec(int size, T pad) {
      myarray = (T[]) new Object[size];
      for (int i = 0; i < size; i++) {
        myarray[i] = pad;
      }
      nbelem = size;
    }

    public int size() {
      return nbelem;
    }

    /**
     * Remove nofelems from the Vector. It is assumed that the number of
     * elements to remove is smaller or equals to the current number of elements
     * in the vector
     * 
     * @param nofelems
     *            the number of elements to remove.
     */
    public void shrink(int nofelems) {
      assert nofelems <= size();
      while (nofelems-- > 0) {
        myarray[--nbelem] = null;
      }
    }

    /**
     * reduce the Vector to exactly newsize elements
     * 
     * @param newsize
     *            the new size of the vector.
     */
    public void shrinkTo(final int newsize) {
      assert newsize <= size();
      for (int i = nbelem; i > newsize; i--) {
        myarray[i - 1] = null;
      }
      nbelem = newsize;
      assert size() == newsize;
    }

    /**
     * Pop the last element on the stack. It is assumed that the stack is not
     * empty!
     */
    public void pop() {
      assert size() > 0;
      myarray[--nbelem] = null;
    }

    public void growTo(final int newsize, final T pad) {
      assert newsize >= size();
      ensure(newsize);
      for (int i = nbelem; i < newsize; i++) {
        myarray[i] = pad;
      }
      nbelem = newsize;
    }

    @SuppressWarnings("unchecked")
    public final void ensure(final int nsize) {
      if (nsize >= myarray.length) {
        T[] narray = (T[]) new Object[Math.max(nsize, nbelem * 2)];
        System.arraycopy(myarray, 0, narray, 0, nbelem);
        myarray = narray;
      }
    }

    public IVec<T> push(final T elem) {
      ensure(nbelem + 1);
      myarray[nbelem++] = elem;
      return this;
    }

    public void unsafePush(final T elem) {
      myarray[nbelem++] = elem;
    }

    /**
     * Insert an element at the very begining of the vector. The former first
     * element is appended to the end of the vector in order to have a constant
     * time operation.
     * 
     * @param elem
     *            the element to put first in the vector.
     */
    public void insertFirst(final T elem) {
      if (nbelem > 0) {
        push(myarray[0]);
        myarray[0] = elem;
        return;
      }
      push(elem);
    }

    public void insertFirstWithShifting(final T elem) {
      if (nbelem > 0) {
        ensure(nbelem + 1);
        for (int i = nbelem; i > 0; i--) {
          myarray[i] = myarray[i - 1];
        }
        myarray[0] = elem;
        nbelem++;
        return;
      }
      push(elem);
    }

    public void clear() {
      while (nbelem > 0) {
        myarray[--nbelem] = null;
      }
    }

    /**
     * return the latest element on the stack. It is assumed that the stack is
     * not empty!
     * 
     * @return the last element on the stack (the one on the top)
     */
    public T last() {
      assert size() != 0;
      return myarray[nbelem - 1];
    }

    public T get(int i) {
      return myarray[i];
    }

    public void set(int i, T o) {
      myarray[i] = o;
    }

    /**
     * Enleve un element qui se trouve dans le vecteur!!!
     * 
     * @param elem
     *            un element du vecteur
     */
    public void remove(T elem) {
      assert size() > 0;
      int j = 0;
      for (; myarray[j] != elem; j++) {
        assert j < size();
      }
      for (; j < size() - 1; j++) {
        myarray[j] = myarray[j + 1];
      }
      pop();
    }

    /**
     * Delete the ith element of the vector. The latest element of the vector
     * replaces the removed element at the ith indexer.
     * 
     * @param i
     *            the indexer of the element in the vector
     * @return the former ith element of the vector that is now removed from the
     *         vector
     */
    public T delete(int i) {
      assert i >= 0;
      assert i < nbelem;
      T ith = myarray[i];
      myarray[i] = myarray[--nbelem];
      myarray[nbelem] = null;
      return ith;
    }

    public void copyTo(IVec<T> copy) {
      Vec<T> ncopy = (Vec<T>) copy;
      int nsize = nbelem + ncopy.nbelem;
      copy.ensure(nsize);
      for (int i = 0; i < nbelem; i++) {
        ncopy.myarray[i + ncopy.nbelem] = myarray[i];
      }
      ncopy.nbelem = nsize;
    }

    /**
     * @param dest
     */
     public <E> void copyTo(E[] dest) {
       assert dest.length >= nbelem;
       System.arraycopy(myarray, 0, dest, 0, nbelem);
     }

     /*
      * Copie un vecteur dans un autre (en vidant le premier), en temps constant.
      */
     public void moveTo(IVec<T> dest) {
       copyTo(dest);
       clear();
     }

     public void moveTo(int dest, int source) {
       myarray[dest]=myarray[source];
       myarray[source]=null;
     }

     private int nbelem;

     private T[] myarray;

     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
       StringBuffer stb = new StringBuffer();
       for (int i = 0; i < nbelem - 1; i++) {
         stb.append(myarray[i]);
         stb.append(",");
       }
       if (nbelem > 0) {
         stb.append(myarray[nbelem - 1]);
       }
       return stb.toString();
     }

     private static Random rand = new Random(RANDOM_SEED);

     void selectionSort(int from, int to, Comparator<T> cmp) {
       int i, j, best_i;
       T tmp;

       for (i = from; i < to - 1; i++) {
         best_i = i;
         for (j = i + 1; j < to; j++) {
           if (cmp.compare(myarray[j], myarray[best_i]) < 0)
             best_i = j;
         }
         tmp = myarray[i];
         myarray[i] = myarray[best_i];
         myarray[best_i] = tmp;
       }
     }

     void sort(int from, int to, Comparator<T> cmp) {
       int width = to - from;
       if (to - from <= 15)
         selectionSort(from, to, cmp);

       else {
         T pivot = myarray[rand.nextInt(width) + from];
         T tmp;
         int i = from - 1;
         int j = to;

         for (;;) {
           do
             i++;
           while (cmp.compare(myarray[i], pivot) < 0);
           do
             j--;
           while (cmp.compare(pivot, myarray[j]) < 0);

           if (i >= j)
             break;

           tmp = myarray[i];
           myarray[i] = myarray[j];
           myarray[j] = tmp;
         }

         sort(from, i, cmp);
         sort(i, to, cmp);
       }
     }

     /**
      * @param comparator
      */
      public void sort(Comparator<T> comparator) {
       sort(0, nbelem, comparator);
     }

     public void sortUnique(Comparator<T> cmp) {
       int i, j;
       T last;

       if (nbelem == 0)
         return;

       sort(0, nbelem, cmp);

       i = 1;
       last = myarray[0];
       for (j = 1; j < nbelem; j++) {
         if (cmp.compare(last, myarray[j]) < 0) {
           last = myarray[i] = myarray[j];
           i++;
         }
       }

       nbelem = i;
     }

     public static final <A extends Comparable<A>> Comparator<A> defaultComparator() {
       return new Comparator<A>() {
         public int compare(A a, A b) {
           return a.compareTo(b);
         }
       };
     }

     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
       if (obj instanceof IVec) {
         IVec<?> v = (IVec<?>) obj;
         if (v.size() != size())
           return false;
         for (int i = 0; i < size(); i++) {
           if (!v.get(i).equals(get(i))) {
             return false;
           }
         }
         return true;
       }
       return false;
     }

     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
       int sum = 0;
       for (int i = 0; i < nbelem; i++) {
         sum += myarray.hashCode() / nbelem;
       }
       return sum;
     }

     public Iterator<T> iterator() {
       return new Iterator<T>() {
         private int i = 0;

         public boolean hasNext() {
           return i < nbelem;
         }

         public T next() {
           if (i == nbelem)
             throw new NoSuchElementException();
           return myarray[i++];
         }

         public void remove() {
           throw new UnsupportedOperationException();
         }
       };
     }
  }
  interface IVec<T> extends Iterable<T> {
    int size();
    void shrink(int nofelems);
    void shrinkTo(final int newsize);
    void pop();

    void growTo(final int newsize, final T pad);

    void ensure(final int nsize);

    IVec<T> push(final T elem);

    void unsafePush(T elem);
    void insertFirst(final T elem);

    void insertFirstWithShifting(final T elem);

    void clear();
    T last();

    T get(int i);

    void set(int i, T o);

    void remove(T elem);

    T delete(int i);

    void copyTo(IVec<T> copy);

    <E> void copyTo(E[] dest);

    void moveTo(IVec<T> dest);

    void moveTo(int dest, int source);

    void sort(Comparator<T> comparator);

    void sortUnique(Comparator<T> comparator);
  }

  //------------
  /*******************************************************************************
   * Copyright (c) 2000, 2004 IBM Corporation and others.
   * All rights reserved. This program and the accompanying materials
   * are made available under the terms of the Eclipse Public License v1.0
   * which accompanies this distribution, and is available at
   * http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributors:
   *     IBM Corporation - initial API and implementation
   *******************************************************************************/
  class ObjectCache {
    public Object keyTable[];
    public int valueTable[];
    int elementSize;
    int threshold;
    /**
     * Constructs a new, empty hashtable. A default capacity is used.
     * Note that the hashtable will automatically grow when it gets full.
     */
    public ObjectCache() {
      this(13);
    }
    /**
     * Constructs a new, empty hashtable with the specified initial
     * capacity.
     * @param initialCapacity int
     *  the initial number of buckets
     */
    public ObjectCache(int initialCapacity) {
      this.elementSize = 0;
      this.threshold = (int) (initialCapacity * 0.66f);
      this.keyTable = new Object[initialCapacity];
      this.valueTable = new int[initialCapacity];
    }
    /**
     * Clears the hash table so that it has no more elements in it.
     */
    public void clear() {
      for (int i = keyTable.length; --i >= 0;) {
        keyTable[i] = null;
        valueTable[i] = 0;
      }
      elementSize = 0;
    }
    /** Returns true if the collection contains an element for the key.
     *
     * @param key char[] the key that we are looking for
     * @return boolean
     */
    public boolean containsKey(Object key) {
      int index = hashCode(key);
      while (keyTable[index] != null) {
        if (keyTable[index] == key)
          return true;
        index = (index + 1) % keyTable.length;
      }
      return false;
    }
    /** Gets the object associated with the specified key in the
     * hashtable.
     * @param key <CODE>char[]</CODE> the specified key
     * @return int the element for the key or -1 if the key is not
     *  defined in the hash table.
     */
    public int get(Object key) {
      int index = hashCode(key);
      while (keyTable[index] != null) {
        if (keyTable[index] == key)
          return valueTable[index];
        index = (index + 1) % keyTable.length;
      }
      return -1;
    }
    /**
     * Return the hashcode for the key parameter
     *
     * @param key org.eclipse.jdt.internal.compiler.lookup.MethodBinding
     * @return int
     */
    public int hashCode(Object key) {
      return (key.hashCode() & 0x7FFFFFFF) % keyTable.length;
    }
    /**
     * Puts the specified element into the hashtable, using the specified
     * key.  The element may be retrieved by doing a get() with the same key.
     * The key and the element cannot be null. 
     * 
     * @param key <CODE>Object</CODE> the specified key in the hashtable
     * @param value <CODE>int</CODE> the specified element
     * @return int the old value of the key, or -1 if it did not have one.
     */
    public int put(Object key, int value) { 
      int index = hashCode(key);
      while (keyTable[index] != null) {
        if (keyTable[index] == key)
          return valueTable[index] = value;
        index = (index + 1) % keyTable.length;
      }
      keyTable[index] = key;
      valueTable[index] = value;

      // assumes the threshold is never equal to the size of the table
      if (++elementSize > threshold)
        rehash();
      return value;
    }
    /**
     * Rehashes the content of the table into a bigger table.
     * This method is called automatically when the hashtable's
     * size exceeds the threshold.
     */
    private void rehash() {
      ObjectCache newHashtable = new ObjectCache(keyTable.length * 2);
      for (int i = keyTable.length; --i >= 0;)
        if (keyTable[i] != null)
          newHashtable.put(keyTable[i], valueTable[i]);

      this.keyTable = newHashtable.keyTable;
      this.valueTable = newHashtable.valueTable;
      this.threshold = newHashtable.threshold;
    }
    /**
     * Returns the number of elements contained in the hashtable.
     *
     * @return <CODE>int</CODE> The size of the table
     */
    public int size() {
      return elementSize;
    }
    /**
     * Converts to a rather lengthy String.
     *
     * @return String the ascii representation of the receiver
     */
    @Override
    public String toString() {
      int max = size();
      StringBuffer buf = new StringBuffer();
      buf.append("{"); //$NON-NLS-1$
      for (int i = 0; i < max; ++i) {
        if (keyTable[i] != null) {
          buf.append(keyTable[i]).append("->").append(valueTable[i]); //$NON-NLS-1$
        }
        if (i < max) {
          buf.append(", "); //$NON-NLS-1$
        }
      }
      buf.append("}"); //$NON-NLS-1$
      return buf.toString();
    }
  }

}