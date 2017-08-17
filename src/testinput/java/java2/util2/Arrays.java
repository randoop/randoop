/*
 * @(#)Arrays.java	1.48 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

import java.lang.reflect.*;

/**
 * This class contains various methods for manipulating arrays (such as
 * sorting and searching).  This class also contains a static factory
 * that allows arrays to be viewed as lists.
 *
 * <p>The methods in this class all throw a <tt>NullPointerException</tt> if
 * the specified array reference is null.
 *
 * <p>The documentation for the methods contained in this class includes
 * briefs description of the <i>implementations</i>.  Such descriptions should
 * be regarded as <i>implementation notes</i>, rather than parts of the
 * <i>specification</i>.  Implementors should feel free to substitute other
 * algorithms, so long as the specification itself is adhered to.  (For
 * example, the algorithm used by <tt>sort(Object[])</tt> does not have to be
 * a mergesort, but it does have to be <i>stable</i>.)
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @version 1.48, 01/23/03
 * @see     Comparable
 * @see     Comparator
 * @since   1.2
 */
public class Arrays {
  // Suppresses default constructor, ensuring non-instantiability.
  private Arrays() {}

  // Sorting

  /**
   * Sorts the specified array of longs into ascending numerical order.
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   */
  public static void sort(long[] a) {
    sort1(a, 0, a.length);
  }

  /**
   * Sorts the specified range of the specified array of longs into
   * ascending numerical order.  The range to be sorted extends from index
   * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
   * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)
   *
   * <p>The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   * @param fromIndex the index of the first element (inclusive) to be
   *        sorted.
   * @param toIndex the index of the last element (exclusive) to be sorted.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   * <tt>toIndex &gt; a.length</tt>
   */
  public static void sort(long[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    sort1(a, fromIndex, toIndex - fromIndex);
  }

  /**
   * Sorts the specified array of ints into ascending numerical order.
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   */
  public static void sort(int[] a) {
    sort1(a, 0, a.length);
  }

  /**
   * Sorts the specified range of the specified array of ints into
   * ascending numerical order.  The range to be sorted extends from index
   * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
   * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
   *
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   * @param fromIndex the index of the first element (inclusive) to be
   *        sorted.
   * @param toIndex the index of the last element (exclusive) to be sorted.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void sort(int[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    sort1(a, fromIndex, toIndex - fromIndex);
  }

  /**
   * Sorts the specified array of shorts into ascending numerical order.
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   */
  public static void sort(short[] a) {
    sort1(a, 0, a.length);
  }

  /**
   * Sorts the specified range of the specified array of shorts into
   * ascending numerical order.  The range to be sorted extends from index
   * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
   * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
   *
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   * @param fromIndex the index of the first element (inclusive) to be
   *        sorted.
   * @param toIndex the index of the last element (exclusive) to be sorted.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void sort(short[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    sort1(a, fromIndex, toIndex - fromIndex);
  }

  /**
   * Sorts the specified array of chars into ascending numerical order.
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   */
  public static void sort(char[] a) {
    sort1(a, 0, a.length);
  }

  /**
   * Sorts the specified range of the specified array of chars into
   * ascending numerical order.  The range to be sorted extends from index
   * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
   * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
   *
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   * @param fromIndex the index of the first element (inclusive) to be
   *        sorted.
   * @param toIndex the index of the last element (exclusive) to be sorted.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void sort(char[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    sort1(a, fromIndex, toIndex - fromIndex);
  }

  /**
   * Sorts the specified array of bytes into ascending numerical order.
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   */
  public static void sort(byte[] a) {
    sort1(a, 0, a.length);
  }

  /**
   * Sorts the specified range of the specified array of bytes into
   * ascending numerical order.  The range to be sorted extends from index
   * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
   * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
   *
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   * @param fromIndex the index of the first element (inclusive) to be
   *        sorted.
   * @param toIndex the index of the last element (exclusive) to be sorted.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void sort(byte[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    sort1(a, fromIndex, toIndex - fromIndex);
  }

  /**
   * Sorts the specified array of doubles into ascending numerical order.
   * <p>
   * The <code>&lt;</code> relation does not provide a total order on
   * all floating-point values; although they are distinct numbers
   * <code>-0.0 == 0.0</code> is <code>true</code> and a NaN value
   * compares neither less than, greater than, nor equal to any
   * floating-point value, even itself.  To allow the sort to
   * proceed, instead of using the <code>&lt;</code> relation to
   * determine ascending numerical order, this method uses the total
   * order imposed by {@link Double#compareTo}.  This ordering
   * differs from the <code>&lt;</code> relation in that
   * <code>-0.0</code> is treated as less than <code>0.0</code> and
   * NaN is considered greater than any other floating-point value.
   * For the purposes of sorting, all NaN values are considered
   * equivalent and equal.
   * <p>
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   */
  public static void sort(double[] a) {
    sort2(a, 0, a.length);
  }

  /**
   * Sorts the specified range of the specified array of doubles into
   * ascending numerical order.  The range to be sorted extends from index
   * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
   * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)
   * <p>
   * The <code>&lt;</code> relation does not provide a total order on
   * all floating-point values; although they are distinct numbers
   * <code>-0.0 == 0.0</code> is <code>true</code> and a NaN value
   * compares neither less than, greater than, nor equal to any
   * floating-point value, even itself.  To allow the sort to
   * proceed, instead of using the <code>&lt;</code> relation to
   * determine ascending numerical order, this method uses the total
   * order imposed by {@link Double#compareTo}.  This ordering
   * differs from the <code>&lt;</code> relation in that
   * <code>-0.0</code> is treated as less than <code>0.0</code> and
   * NaN is considered greater than any other floating-point value.
   * For the purposes of sorting, all NaN values are considered
   * equivalent and equal.
   * <p>
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   * @param fromIndex the index of the first element (inclusive) to be
   *        sorted.
   * @param toIndex the index of the last element (exclusive) to be sorted.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void sort(double[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    sort2(a, fromIndex, toIndex);
  }

  /**
   * Sorts the specified array of floats into ascending numerical order.
   * <p>
   * The <code>&lt;</code> relation does not provide a total order on
   * all floating-point values; although they are distinct numbers
   * <code>-0.0f == 0.0f</code> is <code>true</code> and a NaN value
   * compares neither less than, greater than, nor equal to any
   * floating-point value, even itself.  To allow the sort to
   * proceed, instead of using the <code>&lt;</code> relation to
   * determine ascending numerical order, this method uses the total
   * order imposed by {@link Float#compareTo}.  This ordering
   * differs from the <code>&lt;</code> relation in that
   * <code>-0.0f</code> is treated as less than <code>0.0f</code> and
   * NaN is considered greater than any other floating-point value.
   * For the purposes of sorting, all NaN values are considered
   * equivalent and equal.
   * <p>
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   */
  public static void sort(float[] a) {
    sort2(a, 0, a.length);
  }

  /**
   * Sorts the specified range of the specified array of floats into
   * ascending numerical order.  The range to be sorted extends from index
   * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
   * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)
   * <p>
   * The <code>&lt;</code> relation does not provide a total order on
   * all floating-point values; although they are distinct numbers
   * <code>-0.0f == 0.0f</code> is <code>true</code> and a NaN value
   * compares neither less than, greater than, nor equal to any
   * floating-point value, even itself.  To allow the sort to
   * proceed, instead of using the <code>&lt;</code> relation to
   * determine ascending numerical order, this method uses the total
   * order imposed by {@link Float#compareTo}.  This ordering
   * differs from the <code>&lt;</code> relation in that
   * <code>-0.0f</code> is treated as less than <code>0.0f</code> and
   * NaN is considered greater than any other floating-point value.
   * For the purposes of sorting, all NaN values are considered
   * equivalent and equal.
   * <p>
   * The sorting algorithm is a tuned quicksort, adapted from Jon
   * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
   * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
   * 1993).  This algorithm offers n*log(n) performance on many data sets
   * that cause other quicksorts to degrade to quadratic performance.
   *
   * @param a the array to be sorted.
   * @param fromIndex the index of the first element (inclusive) to be
   *        sorted.
   * @param toIndex the index of the last element (exclusive) to be sorted.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void sort(float[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    sort2(a, fromIndex, toIndex);
  }

  private static void sort2(double[] a, int fromIndex, int toIndex) {
    final long NEG_ZERO_BITS = Double.doubleToLongBits(-0.0d);
    /*
     * The sort is done in three phases to avoid the expense of using
     * NaN and -0.0 aware comparisons during the main sort.
     */

    /*
     * Preprocessing phase:  Move any NaN's to end of array, count the
     * number of -0.0's, and turn them into 0.0's.
     */
    int numNegZeros = 0;
    int i = fromIndex, n = toIndex;
    while (i < n) {
      if (a[i] != a[i]) {
        double swap = a[i];
        a[i] = a[--n];
        a[n] = swap;
      } else {
        if (a[i] == 0 && Double.doubleToLongBits(a[i]) == NEG_ZERO_BITS) {
          a[i] = 0.0d;
          numNegZeros++;
        }
        i++;
      }
    }

    // Main sort phase: quicksort everything but the NaN's
    sort1(a, fromIndex, n - fromIndex);

    // Postprocessing phase: change 0.0's to -0.0's as required
    if (numNegZeros != 0) {
      int j = binarySearch(a, 0.0d, fromIndex, n - 1); // posn of ANY zero
      do {
        j--;
      } while (j >= 0 && a[j] == 0.0d);

      // j is now one less than the index of the FIRST zero
      for (int k = 0; k < numNegZeros; k++) a[++j] = -0.0d;
    }
  }

  private static void sort2(float[] a, int fromIndex, int toIndex) {
    final int NEG_ZERO_BITS = Float.floatToIntBits(-0.0f);
    /*
     * The sort is done in three phases to avoid the expense of using
     * NaN and -0.0 aware comparisons during the main sort.
     */

    /*
     * Preprocessing phase:  Move any NaN's to end of array, count the
     * number of -0.0's, and turn them into 0.0's.
     */
    int numNegZeros = 0;
    int i = fromIndex, n = toIndex;
    while (i < n) {
      if (a[i] != a[i]) {
        float swap = a[i];
        a[i] = a[--n];
        a[n] = swap;
      } else {
        if (a[i] == 0 && Float.floatToIntBits(a[i]) == NEG_ZERO_BITS) {
          a[i] = 0.0f;
          numNegZeros++;
        }
        i++;
      }
    }

    // Main sort phase: quicksort everything but the NaN's
    sort1(a, fromIndex, n - fromIndex);

    // Postprocessing phase: change 0.0's to -0.0's as required
    if (numNegZeros != 0) {
      int j = binarySearch(a, 0.0f, fromIndex, n - 1); // posn of ANY zero
      do {
        j--;
      } while (j >= 0 && a[j] == 0.0f);

      // j is now one less than the index of the FIRST zero
      for (int k = 0; k < numNegZeros; k++) a[++j] = -0.0f;
    }
  }

  /*
   * The code for each of the seven primitive types is largely identical.
   * C'est la vie.
   */

  /**
   * Sorts the specified sub-array of longs into ascending order.
   */
  private static void sort1(long[] x, int off, int len) {
    // Insertion sort on smallest arrays
    if (len < 7) {
      for (int i = off; i < len + off; i++)
        for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, j, j - 1);
      return;
    }

    // Choose a partition element, v
    int m = off + (len >> 1); // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = off + len - 1;
      if (len > 40) { // Big arrays, pseudomedian of 9
        int s = len / 8;
        l = med3(x, l, l + s, l + 2 * s);
        m = med3(x, m - s, m, m + s);
        n = med3(x, n - 2 * s, n - s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    long v = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off, b = a, c = off + len - 1, d = c;
    while (true) {
      while (b <= c && x[b] <= v) {
        if (x[b] == v) swap(x, a++, b);
        b++;
      }
      while (c >= b && x[c] >= v) {
        if (x[c] == v) swap(x, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, b++, c--);
    }

    // Swap partition elements back to middle
    int s, n = off + len;
    s = Math.min(a - off, b - a);
    vecswap(x, off, b - s, s);
    s = Math.min(d - c, n - d - 1);
    vecswap(x, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) sort1(x, off, s);
    if ((s = d - c) > 1) sort1(x, n - s, s);
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(long[] x, int a, int b) {
    long t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   */
  private static void vecswap(long[] x, int a, int b, int n) {
    for (int i = 0; i < n; i++, a++, b++) swap(x, a, b);
  }

  /**
   * Returns the index of the median of the three indexed longs.
   */
  private static int med3(long[] x, int a, int b, int c) {
    return (x[a] < x[b]
        ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a)
        : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Sorts the specified sub-array of integers into ascending order.
   */
  private static void sort1(int[] x, int off, int len) {
    // Insertion sort on smallest arrays
    if (len < 7) {
      for (int i = off; i < len + off; i++)
        for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, j, j - 1);
      return;
    }

    // Choose a partition element, v
    int m = off + (len >> 1); // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = off + len - 1;
      if (len > 40) { // Big arrays, pseudomedian of 9
        int s = len / 8;
        l = med3(x, l, l + s, l + 2 * s);
        m = med3(x, m - s, m, m + s);
        n = med3(x, n - 2 * s, n - s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    int v = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off, b = a, c = off + len - 1, d = c;
    while (true) {
      while (b <= c && x[b] <= v) {
        if (x[b] == v) swap(x, a++, b);
        b++;
      }
      while (c >= b && x[c] >= v) {
        if (x[c] == v) swap(x, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, b++, c--);
    }

    // Swap partition elements back to middle
    int s, n = off + len;
    s = Math.min(a - off, b - a);
    vecswap(x, off, b - s, s);
    s = Math.min(d - c, n - d - 1);
    vecswap(x, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) sort1(x, off, s);
    if ((s = d - c) > 1) sort1(x, n - s, s);
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(int[] x, int a, int b) {
    int t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   */
  private static void vecswap(int[] x, int a, int b, int n) {
    for (int i = 0; i < n; i++, a++, b++) swap(x, a, b);
  }

  /**
   * Returns the index of the median of the three indexed integers.
   */
  private static int med3(int[] x, int a, int b, int c) {
    return (x[a] < x[b]
        ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a)
        : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Sorts the specified sub-array of shorts into ascending order.
   */
  private static void sort1(short[] x, int off, int len) {
    // Insertion sort on smallest arrays
    if (len < 7) {
      for (int i = off; i < len + off; i++)
        for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, j, j - 1);
      return;
    }

    // Choose a partition element, v
    int m = off + (len >> 1); // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = off + len - 1;
      if (len > 40) { // Big arrays, pseudomedian of 9
        int s = len / 8;
        l = med3(x, l, l + s, l + 2 * s);
        m = med3(x, m - s, m, m + s);
        n = med3(x, n - 2 * s, n - s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    short v = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off, b = a, c = off + len - 1, d = c;
    while (true) {
      while (b <= c && x[b] <= v) {
        if (x[b] == v) swap(x, a++, b);
        b++;
      }
      while (c >= b && x[c] >= v) {
        if (x[c] == v) swap(x, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, b++, c--);
    }

    // Swap partition elements back to middle
    int s, n = off + len;
    s = Math.min(a - off, b - a);
    vecswap(x, off, b - s, s);
    s = Math.min(d - c, n - d - 1);
    vecswap(x, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) sort1(x, off, s);
    if ((s = d - c) > 1) sort1(x, n - s, s);
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(short[] x, int a, int b) {
    short t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   */
  private static void vecswap(short[] x, int a, int b, int n) {
    for (int i = 0; i < n; i++, a++, b++) swap(x, a, b);
  }

  /**
   * Returns the index of the median of the three indexed shorts.
   */
  private static int med3(short[] x, int a, int b, int c) {
    return (x[a] < x[b]
        ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a)
        : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Sorts the specified sub-array of chars into ascending order.
   */
  private static void sort1(char[] x, int off, int len) {
    // Insertion sort on smallest arrays
    if (len < 7) {
      for (int i = off; i < len + off; i++)
        for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, j, j - 1);
      return;
    }

    // Choose a partition element, v
    int m = off + (len >> 1); // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = off + len - 1;
      if (len > 40) { // Big arrays, pseudomedian of 9
        int s = len / 8;
        l = med3(x, l, l + s, l + 2 * s);
        m = med3(x, m - s, m, m + s);
        n = med3(x, n - 2 * s, n - s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    char v = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off, b = a, c = off + len - 1, d = c;
    while (true) {
      while (b <= c && x[b] <= v) {
        if (x[b] == v) swap(x, a++, b);
        b++;
      }
      while (c >= b && x[c] >= v) {
        if (x[c] == v) swap(x, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, b++, c--);
    }

    // Swap partition elements back to middle
    int s, n = off + len;
    s = Math.min(a - off, b - a);
    vecswap(x, off, b - s, s);
    s = Math.min(d - c, n - d - 1);
    vecswap(x, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) sort1(x, off, s);
    if ((s = d - c) > 1) sort1(x, n - s, s);
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(char[] x, int a, int b) {
    char t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   */
  private static void vecswap(char[] x, int a, int b, int n) {
    for (int i = 0; i < n; i++, a++, b++) swap(x, a, b);
  }

  /**
   * Returns the index of the median of the three indexed chars.
   */
  private static int med3(char[] x, int a, int b, int c) {
    return (x[a] < x[b]
        ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a)
        : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Sorts the specified sub-array of bytes into ascending order.
   */
  private static void sort1(byte[] x, int off, int len) {
    // Insertion sort on smallest arrays
    if (len < 7) {
      for (int i = off; i < len + off; i++)
        for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, j, j - 1);
      return;
    }

    // Choose a partition element, v
    int m = off + (len >> 1); // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = off + len - 1;
      if (len > 40) { // Big arrays, pseudomedian of 9
        int s = len / 8;
        l = med3(x, l, l + s, l + 2 * s);
        m = med3(x, m - s, m, m + s);
        n = med3(x, n - 2 * s, n - s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    byte v = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off, b = a, c = off + len - 1, d = c;
    while (true) {
      while (b <= c && x[b] <= v) {
        if (x[b] == v) swap(x, a++, b);
        b++;
      }
      while (c >= b && x[c] >= v) {
        if (x[c] == v) swap(x, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, b++, c--);
    }

    // Swap partition elements back to middle
    int s, n = off + len;
    s = Math.min(a - off, b - a);
    vecswap(x, off, b - s, s);
    s = Math.min(d - c, n - d - 1);
    vecswap(x, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) sort1(x, off, s);
    if ((s = d - c) > 1) sort1(x, n - s, s);
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(byte[] x, int a, int b) {
    byte t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   */
  private static void vecswap(byte[] x, int a, int b, int n) {
    for (int i = 0; i < n; i++, a++, b++) swap(x, a, b);
  }

  /**
   * Returns the index of the median of the three indexed bytes.
   */
  private static int med3(byte[] x, int a, int b, int c) {
    return (x[a] < x[b]
        ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a)
        : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Sorts the specified sub-array of doubles into ascending order.
   */
  private static void sort1(double[] x, int off, int len) {
    // Insertion sort on smallest arrays
    if (len < 7) {
      for (int i = off; i < len + off; i++)
        for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, j, j - 1);
      return;
    }

    // Choose a partition element, v
    int m = off + (len >> 1); // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = off + len - 1;
      if (len > 40) { // Big arrays, pseudomedian of 9
        int s = len / 8;
        l = med3(x, l, l + s, l + 2 * s);
        m = med3(x, m - s, m, m + s);
        n = med3(x, n - 2 * s, n - s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    double v = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off, b = a, c = off + len - 1, d = c;
    while (true) {
      while (b <= c && x[b] <= v) {
        if (x[b] == v) swap(x, a++, b);
        b++;
      }
      while (c >= b && x[c] >= v) {
        if (x[c] == v) swap(x, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, b++, c--);
    }

    // Swap partition elements back to middle
    int s, n = off + len;
    s = Math.min(a - off, b - a);
    vecswap(x, off, b - s, s);
    s = Math.min(d - c, n - d - 1);
    vecswap(x, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) sort1(x, off, s);
    if ((s = d - c) > 1) sort1(x, n - s, s);
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(double[] x, int a, int b) {
    double t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   */
  private static void vecswap(double[] x, int a, int b, int n) {
    for (int i = 0; i < n; i++, a++, b++) swap(x, a, b);
  }

  /**
   * Returns the index of the median of the three indexed doubles.
   */
  private static int med3(double[] x, int a, int b, int c) {
    return (x[a] < x[b]
        ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a)
        : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Sorts the specified sub-array of floats into ascending order.
   */
  private static void sort1(float[] x, int off, int len) {
    // Insertion sort on smallest arrays
    if (len < 7) {
      for (int i = off; i < len + off; i++)
        for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, j, j - 1);
      return;
    }

    // Choose a partition element, v
    int m = off + (len >> 1); // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = off + len - 1;
      if (len > 40) { // Big arrays, pseudomedian of 9
        int s = len / 8;
        l = med3(x, l, l + s, l + 2 * s);
        m = med3(x, m - s, m, m + s);
        n = med3(x, n - 2 * s, n - s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    float v = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off, b = a, c = off + len - 1, d = c;
    while (true) {
      while (b <= c && x[b] <= v) {
        if (x[b] == v) swap(x, a++, b);
        b++;
      }
      while (c >= b && x[c] >= v) {
        if (x[c] == v) swap(x, c, d--);
        c--;
      }
      if (b > c) break;
      swap(x, b++, c--);
    }

    // Swap partition elements back to middle
    int s, n = off + len;
    s = Math.min(a - off, b - a);
    vecswap(x, off, b - s, s);
    s = Math.min(d - c, n - d - 1);
    vecswap(x, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) sort1(x, off, s);
    if ((s = d - c) > 1) sort1(x, n - s, s);
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(float[] x, int a, int b) {
    float t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   */
  private static void vecswap(float[] x, int a, int b, int n) {
    for (int i = 0; i < n; i++, a++, b++) swap(x, a, b);
  }

  /**
   * Returns the index of the median of the three indexed floats.
   */
  private static int med3(float[] x, int a, int b, int c) {
    return (x[a] < x[b]
        ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a)
        : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Sorts the specified array of objects into ascending order, according to
   * the <i>natural ordering</i> of its elements.  All elements in the array
   * must implement the <tt>Comparable</tt> interface.  Furthermore, all
   * elements in the array must be <i>mutually comparable</i> (that is,
   * <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt>
   * for any elements <tt>e1</tt> and <tt>e2</tt> in the array).<p>
   *
   * This sort is guaranteed to be <i>stable</i>:  equal elements will
   * not be reordered as a result of the sort.<p>
   *
   * The sorting algorithm is a modified mergesort (in which the merge is
   * omitted if the highest element in the low sublist is less than the
   * lowest element in the high sublist).  This algorithm offers guaranteed
   * n*log(n) performance.
   *
   * @param a the array to be sorted.
   * @throws  ClassCastException if the array contains elements that are not
   *		<i>mutually comparable</i> (for example, strings and integers).
   * @see Comparable
   */
  public static void sort(Object[] a) {
    Object[] aux = (Object[]) a.clone();
    mergeSort(aux, a, 0, a.length, 0);
  }

  /**
   * Sorts the specified range of the specified array of objects into
   * ascending order, according to the <i>natural ordering</i> of its
   * elements.  The range to be sorted extends from index
   * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
   * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)  All
   * elements in this range must implement the <tt>Comparable</tt>
   * interface.  Furthermore, all elements in this range must be <i>mutually
   * comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not throw a
   * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
   * <tt>e2</tt> in the array).<p>
   *
   * This sort is guaranteed to be <i>stable</i>:  equal elements will
   * not be reordered as a result of the sort.<p>
   *
   * The sorting algorithm is a modified mergesort (in which the merge is
   * omitted if the highest element in the low sublist is less than the
   * lowest element in the high sublist).  This algorithm offers guaranteed
   * n*log(n) performance.
   *
   * @param a the array to be sorted.
   * @param fromIndex the index of the first element (inclusive) to be
   *        sorted.
   * @param toIndex the index of the last element (exclusive) to be sorted.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   * @throws    ClassCastException if the array contains elements that are
   *		  not <i>mutually comparable</i> (for example, strings and
   *		  integers).
   * @see Comparable
   */
  public static void sort(Object[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    Object[] aux = (Object[]) cloneSubarray(a, fromIndex, toIndex);
    mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
  }

  /**
   * Tuning parameter: list size at or below which insertion sort will be
   * used in preference to mergesort or quicksort.
   */
  private static final int INSERTIONSORT_THRESHOLD = 7;

  /**
   * Clones an array within the specified bounds.
   * This method assumes that a is an array.
   */
  private static Object cloneSubarray(Object[] a, int from, int to) {
    int n = to - from;
    Object result = Array.newInstance(a.getClass().getComponentType(), n);
    System.arraycopy(a, from, result, 0, n);
    return result;
  }

  /**
   * Src is the source array that starts at index 0
   * Dest is the (possibly larger) array destination with a possible offset
   * low is the index in dest to start sorting
   * high is the end index in dest to end sorting
   * off is the offset to generate corresponding low, high in src
   */
  private static void mergeSort(Object[] src, Object[] dest, int low, int high, int off) {
    int length = high - low;

    // Insertion sort on smallest arrays
    if (length < INSERTIONSORT_THRESHOLD) {
      for (int i = low; i < high; i++)
        for (int j = i;
            j > low && ((Comparable) dest[j - 1]).compareTo((Comparable) dest[j]) > 0;
            j--) swap(dest, j, j - 1);
      return;
    }

    // Recursively sort halves of dest into src
    int destLow = low;
    int destHigh = high;
    low += off;
    high += off;
    int mid = (low + high) >> 1;
    mergeSort(dest, src, low, mid, -off);
    mergeSort(dest, src, mid, high, -off);

    // If list is already sorted, just copy from src to dest.  This is an
    // optimization that results in faster sorts for nearly ordered lists.
    if (((Comparable) src[mid - 1]).compareTo((Comparable) src[mid]) <= 0) {
      System.arraycopy(src, low, dest, destLow, length);
      return;
    }

    // Merge sorted halves (now in src) into dest
    for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
      if (q >= high || p < mid && ((Comparable) src[p]).compareTo(src[q]) <= 0) dest[i] = src[p++];
      else dest[i] = src[q++];
    }
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(Object[] x, int a, int b) {
    Object t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  /**
   * Sorts the specified array of objects according to the order induced by
   * the specified comparator.  All elements in the array must be
   * <i>mutually comparable</i> by the specified comparator (that is,
   * <tt>c.compare(e1, e2)</tt> must not throw a <tt>ClassCastException</tt>
   * for any elements <tt>e1</tt> and <tt>e2</tt> in the array).<p>
   *
   * This sort is guaranteed to be <i>stable</i>:  equal elements will
   * not be reordered as a result of the sort.<p>
   *
   * The sorting algorithm is a modified mergesort (in which the merge is
   * omitted if the highest element in the low sublist is less than the
   * lowest element in the high sublist).  This algorithm offers guaranteed
   * n*log(n) performance.
   *
   * @param a the array to be sorted.
   * @param c the comparator to determine the order of the array.  A
   *        <tt>null</tt> value indicates that the elements' <i>natural
   *        ordering</i> should be used.
   * @throws  ClassCastException if the array contains elements that are
   *		not <i>mutually comparable</i> using the specified comparator.
   * @see Comparator
   */
  public static void sort(Object[] a, Comparator c) {
    Object[] aux = (Object[]) a.clone();
    if (c == null) mergeSort(aux, a, 0, a.length, 0);
    else mergeSort(aux, a, 0, a.length, 0, c);
  }

  /**
   * Sorts the specified range of the specified array of objects according
   * to the order induced by the specified comparator.  The range to be
   * sorted extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be sorted is empty.)  All elements in the range must be
   * <i>mutually comparable</i> by the specified comparator (that is,
   * <tt>c.compare(e1, e2)</tt> must not throw a <tt>ClassCastException</tt>
   * for any elements <tt>e1</tt> and <tt>e2</tt> in the range).<p>
   *
   * This sort is guaranteed to be <i>stable</i>:  equal elements will
   * not be reordered as a result of the sort.<p>
   *
   * The sorting algorithm is a modified mergesort (in which the merge is
   * omitted if the highest element in the low sublist is less than the
   * lowest element in the high sublist).  This algorithm offers guaranteed
   * n*log(n) performance.
   *
   * @param a the array to be sorted.
   * @param fromIndex the index of the first element (inclusive) to be
   *        sorted.
   * @param toIndex the index of the last element (exclusive) to be sorted.
   * @param c the comparator to determine the order of the array.  A
   *        <tt>null</tt> value indicates that the elements' <i>natural
   *        ordering</i> should be used.
   * @throws ClassCastException if the array contains elements that are not
   *	       <i>mutually comparable</i> using the specified comparator.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   * @see Comparator
   */
  public static void sort(Object[] a, int fromIndex, int toIndex, Comparator c) {
    rangeCheck(a.length, fromIndex, toIndex);
    Object[] aux = (Object[]) cloneSubarray(a, fromIndex, toIndex);
    if (c == null) mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
    else mergeSort(aux, a, fromIndex, toIndex, -fromIndex, c);
  }

  /**
   * Src is the source array that starts at index 0
   * Dest is the (possibly larger) array destination with a possible offset
   * low is the index in dest to start sorting
   * high is the end index in dest to end sorting
   * off is the offset into src corresponding to low in dest
   */
  private static void mergeSort(
      Object[] src, Object[] dest, int low, int high, int off, Comparator c) {
    int length = high - low;

    // Insertion sort on smallest arrays
    if (length < INSERTIONSORT_THRESHOLD) {
      for (int i = low; i < high; i++)
        for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--) swap(dest, j, j - 1);
      return;
    }

    // Recursively sort halves of dest into src
    int destLow = low;
    int destHigh = high;
    low += off;
    high += off;
    int mid = (low + high) >> 1;
    mergeSort(dest, src, low, mid, -off, c);
    mergeSort(dest, src, mid, high, -off, c);

    // If list is already sorted, just copy from src to dest.  This is an
    // optimization that results in faster sorts for nearly ordered lists.
    if (c.compare(src[mid - 1], src[mid]) <= 0) {
      System.arraycopy(src, low, dest, destLow, length);
      return;
    }

    // Merge sorted halves (now in src) into dest
    for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
      if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0) dest[i] = src[p++];
      else dest[i] = src[q++];
    }
  }

  /**
   * Check that fromIndex and toIndex are in range, and throw an
   * appropriate exception if they aren't.
   */
  private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
    }
    if (fromIndex < 0) throw new ArrayIndexOutOfBoundsException(fromIndex);
    if (toIndex > arrayLen) throw new ArrayIndexOutOfBoundsException(toIndex);
  }

  // Searching

  /**
   * Searches the specified array of longs for the specified value using the
   * binary search algorithm.  The array <strong>must</strong> be sorted (as
   * by the <tt>sort</tt> method, above) prior to making this call.  If it
   * is not sorted, the results are undefined.  If the array contains
   * multiple elements with the specified value, there is no guarantee which
   * one will be found.
   *
   * @param a the array to be searched.
   * @param key the value to be searched for.
   * @return index of the search key, if it is contained in the list;
   *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
   *	       <i>insertion point</i> is defined as the point at which the
   *	       key would be inserted into the list: the index of the first
   *	       element greater than the key, or <tt>list.size()</tt>, if all
   *	       elements in the list are less than the specified key.  Note
   *	       that this guarantees that the return value will be &gt;= 0 if
   *	       and only if the key is found.
   * @see #sort(long[])
   */
  public static int binarySearch(long[] a, long key) {
    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >> 1;
      long midVal = a[mid];

      if (midVal < key) low = mid + 1;
      else if (midVal > key) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Searches the specified array of ints for the specified value using the
   * binary search algorithm.  The array <strong>must</strong> be sorted (as
   * by the <tt>sort</tt> method, above) prior to making this call.  If it
   * is not sorted, the results are undefined.  If the array contains
   * multiple elements with the specified value, there is no guarantee which
   * one will be found.
   *
   * @param a the array to be searched.
   * @param key the value to be searched for.
   * @return index of the search key, if it is contained in the list;
   *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
   *	       <i>insertion point</i> is defined as the point at which the
   *	       key would be inserted into the list: the index of the first
   *	       element greater than the key, or <tt>list.size()</tt>, if all
   *	       elements in the list are less than the specified key.  Note
   *	       that this guarantees that the return value will be &gt;= 0 if
   *	       and only if the key is found.
   * @see #sort(int[])
   */
  public static int binarySearch(int[] a, int key) {
    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >> 1;
      int midVal = a[mid];

      if (midVal < key) low = mid + 1;
      else if (midVal > key) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Searches the specified array of shorts for the specified value using
   * the binary search algorithm.  The array <strong>must</strong> be sorted
   * (as by the <tt>sort</tt> method, above) prior to making this call.  If
   * it is not sorted, the results are undefined.  If the array contains
   * multiple elements with the specified value, there is no guarantee which
   * one will be found.
   *
   * @param a the array to be searched.
   * @param key the value to be searched for.
   * @return index of the search key, if it is contained in the list;
   *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
   *	       <i>insertion point</i> is defined as the point at which the
   *	       key would be inserted into the list: the index of the first
   *	       element greater than the key, or <tt>list.size()</tt>, if all
   *	       elements in the list are less than the specified key.  Note
   *	       that this guarantees that the return value will be &gt;= 0 if
   *	       and only if the key is found.
   * @see #sort(short[])
   */
  public static int binarySearch(short[] a, short key) {
    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >> 1;
      short midVal = a[mid];

      if (midVal < key) low = mid + 1;
      else if (midVal > key) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Searches the specified array of chars for the specified value using the
   * binary search algorithm.  The array <strong>must</strong> be sorted (as
   * by the <tt>sort</tt> method, above) prior to making this call.  If it
   * is not sorted, the results are undefined.  If the array contains
   * multiple elements with the specified value, there is no guarantee which
   * one will be found.
   *
   * @param a the array to be searched.
   * @param key the value to be searched for.
   * @return index of the search key, if it is contained in the list;
   *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
   *	       <i>insertion point</i> is defined as the point at which the
   *	       key would be inserted into the list: the index of the first
   *	       element greater than the key, or <tt>list.size()</tt>, if all
   *	       elements in the list are less than the specified key.  Note
   *	       that this guarantees that the return value will be &gt;= 0 if
   *	       and only if the key is found.
   * @see #sort(char[])
   */
  public static int binarySearch(char[] a, char key) {
    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >> 1;
      char midVal = a[mid];

      if (midVal < key) low = mid + 1;
      else if (midVal > key) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Searches the specified array of bytes for the specified value using the
   * binary search algorithm.  The array <strong>must</strong> be sorted (as
   * by the <tt>sort</tt> method, above) prior to making this call.  If it
   * is not sorted, the results are undefined.  If the array contains
   * multiple elements with the specified value, there is no guarantee which
   * one will be found.
   *
   * @param a the array to be searched.
   * @param key the value to be searched for.
   * @return index of the search key, if it is contained in the list;
   *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
   *	       <i>insertion point</i> is defined as the point at which the
   *	       key would be inserted into the list: the index of the first
   *	       element greater than the key, or <tt>list.size()</tt>, if all
   *	       elements in the list are less than the specified key.  Note
   *	       that this guarantees that the return value will be &gt;= 0 if
   *	       and only if the key is found.
   * @see #sort(byte[])
   */
  public static int binarySearch(byte[] a, byte key) {
    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >> 1;
      byte midVal = a[mid];

      if (midVal < key) low = mid + 1;
      else if (midVal > key) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Searches the specified array of doubles for the specified value using
   * the binary search algorithm.  The array <strong>must</strong> be sorted
   * (as by the <tt>sort</tt> method, above) prior to making this call.  If
   * it is not sorted, the results are undefined.  If the array contains
   * multiple elements with the specified value, there is no guarantee which
   * one will be found.  This method considers all NaN values to be
   * equivalent and equal.
   *
   * @param a the array to be searched.
   * @param key the value to be searched for.
   * @return index of the search key, if it is contained in the list;
   *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
   *	       <i>insertion point</i> is defined as the point at which the
   *	       key would be inserted into the list: the index of the first
   *	       element greater than the key, or <tt>list.size()</tt>, if all
   *	       elements in the list are less than the specified key.  Note
   *	       that this guarantees that the return value will be &gt;= 0 if
   *	       and only if the key is found.
   * @see #sort(double[])
   */
  public static int binarySearch(double[] a, double key) {
    return binarySearch(a, key, 0, a.length - 1);
  }

  private static int binarySearch(double[] a, double key, int low, int high) {
    while (low <= high) {
      int mid = (low + high) >> 1;
      double midVal = a[mid];

      int cmp;
      if (midVal < key) {
        cmp = -1; // Neither val is NaN, thisVal is smaller
      } else if (midVal > key) {
        cmp = 1; // Neither val is NaN, thisVal is larger
      } else {
        long midBits = Double.doubleToLongBits(midVal);
        long keyBits = Double.doubleToLongBits(key);
        cmp =
            (midBits == keyBits
                ? 0
                : // Values are equal
                (midBits < keyBits
                    ? -1
                    : // (-0.0, 0.0) or (!NaN, NaN)
                    1)); // (0.0, -0.0) or (NaN, !NaN)
      }

      if (cmp < 0) low = mid + 1;
      else if (cmp > 0) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Searches the specified array of floats for the specified value using
   * the binary search algorithm.  The array <strong>must</strong> be sorted
   * (as by the <tt>sort</tt> method, above) prior to making this call.  If
   * it is not sorted, the results are undefined.  If the array contains
   * multiple elements with the specified value, there is no guarantee which
   * one will be found.  This method considers all NaN values to be
   * equivalent and equal.
   *
   * @param a the array to be searched.
   * @param key the value to be searched for.
   * @return index of the search key, if it is contained in the list;
   *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
   *	       <i>insertion point</i> is defined as the point at which the
   *	       key would be inserted into the list: the index of the first
   *	       element greater than the key, or <tt>list.size()</tt>, if all
   *	       elements in the list are less than the specified key.  Note
   *	       that this guarantees that the return value will be &gt;= 0 if
   *	       and only if the key is found.
   * @see #sort(float[])
   */
  public static int binarySearch(float[] a, float key) {
    return binarySearch(a, key, 0, a.length - 1);
  }

  private static int binarySearch(float[] a, float key, int low, int high) {
    while (low <= high) {
      int mid = (low + high) >> 1;
      float midVal = a[mid];

      int cmp;
      if (midVal < key) {
        cmp = -1; // Neither val is NaN, thisVal is smaller
      } else if (midVal > key) {
        cmp = 1; // Neither val is NaN, thisVal is larger
      } else {
        int midBits = Float.floatToIntBits(midVal);
        int keyBits = Float.floatToIntBits(key);
        cmp =
            (midBits == keyBits
                ? 0
                : // Values are equal
                (midBits < keyBits
                    ? -1
                    : // (-0.0, 0.0) or (!NaN, NaN)
                    1)); // (0.0, -0.0) or (NaN, !NaN)
      }

      if (cmp < 0) low = mid + 1;
      else if (cmp > 0) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Searches the specified array for the specified object using the binary
   * search algorithm.  The array must be sorted into ascending order
   * according to the <i>natural ordering</i> of its elements (as by
   * <tt>Sort(Object[]</tt>), above) prior to making this call.  If it is
   * not sorted, the results are undefined.
   * (If the array contains elements that are not  mutually comparable (for
   * example,strings and integers), it <i>cannot</i> be sorted according
   * to the natural order of its elements, hence results are undefined.)
   *  If the array contains multiple
   * elements equal to the specified object, there is no guarantee which
   * one will be found.
   *
   * @param a the array to be searched.
   * @param key the value to be searched for.
   * @return index of the search key, if it is contained in the list;
   *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
   *	       <i>insertion point</i> is defined as the point at which the
   *	       key would be inserted into the list: the index of the first
   *	       element greater than the key, or <tt>list.size()</tt>, if all
   *	       elements in the list are less than the specified key.  Note
   *	       that this guarantees that the return value will be &gt;= 0 if
   *	       and only if the key is found.
   * @throws ClassCastException if the search key in not comparable to the
   *         elements of the array.
   * @see Comparable
   * @see #sort(Object[])
   */
  public static int binarySearch(Object[] a, Object key) {
    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >> 1;
      Object midVal = a[mid];
      int cmp = ((Comparable) midVal).compareTo(key);

      if (cmp < 0) low = mid + 1;
      else if (cmp > 0) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  /**
   * Searches the specified array for the specified object using the binary
   * search algorithm.  The array must be sorted into ascending order
   * according to the specified comparator (as by the <tt>Sort(Object[],
   * Comparator)</tt> method, above), prior to making this call.  If it is
   * not sorted, the results are undefined.
   * If the array contains multiple
   * elements equal to the specified object, there is no guarantee which one
   * will be found.
   *
   * @param a the array to be searched.
   * @param key the value to be searched for.
   * @param c the comparator by which the array is ordered.  A
   *        <tt>null</tt> value indicates that the elements' <i>natural
   *        ordering</i> should be used.
   * @return index of the search key, if it is contained in the list;
   *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
   *	       <i>insertion point</i> is defined as the point at which the
   *	       key would be inserted into the list: the index of the first
   *	       element greater than the key, or <tt>list.size()</tt>, if all
   *	       elements in the list are less than the specified key.  Note
   *	       that this guarantees that the return value will be &gt;= 0 if
   *	       and only if the key is found.
   * @throws ClassCastException if the array contains elements that are not
   *	       <i>mutually comparable</i> using the specified comparator,
   *	       or the search key in not mutually comparable with the
   *	       elements of the array using this comparator.
   * @see Comparable
   * @see #sort(Object[], Comparator)
   */
  public static int binarySearch(Object[] a, Object key, Comparator c) {
    if (c == null) {
      return binarySearch(a, key);
    }

    int low = 0;
    int high = a.length - 1;

    while (low <= high) {
      int mid = (low + high) >> 1;
      Object midVal = a[mid];
      int cmp = c.compare(midVal, key);

      if (cmp < 0) low = mid + 1;
      else if (cmp > 0) high = mid - 1;
      else return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  // Equality Testing

  /**
   * Returns <tt>true</tt> if the two specified arrays of longs are
   * <i>equal</i> to one another.  Two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs
   * of elements in the two arrays are equal.  In other words, two arrays
   * are equal if they contain the same elements in the same order.  Also,
   * two array references are considered equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   */
  public static boolean equals(long[] a, long[] a2) {
    if (a == a2) return true;
    if (a == null || a2 == null) return false;

    int length = a.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) return false;
    }

    return true;
  }

  /**
   * Returns <tt>true</tt> if the two specified arrays of ints are
   * <i>equal</i> to one another.  Two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs
   * of elements in the two arrays are equal.  In other words, two arrays
   * are equal if they contain the same elements in the same order.  Also,
   * two array references are considered equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   */
  public static boolean equals(int[] a, int[] a2) {
    if (a == a2) return true;
    if (a == null || a2 == null) return false;

    int length = a.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) return false;
    }

    return true;
  }

  /**
   * Returns <tt>true</tt> if the two specified arrays of shorts are
   * <i>equal</i> to one another.  Two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs
   * of elements in the two arrays are equal.  In other words, two arrays
   * are equal if they contain the same elements in the same order.  Also,
   * two array references are considered equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   */
  public static boolean equals(short[] a, short[] a2) {
    if (a == a2) return true;
    if (a == null || a2 == null) return false;

    int length = a.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) return false;
    }

    return true;
  }

  /**
   * Returns <tt>true</tt> if the two specified arrays of chars are
   * <i>equal</i> to one another.  Two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs
   * of elements in the two arrays are equal.  In other words, two arrays
   * are equal if they contain the same elements in the same order.  Also,
   * two array references are considered equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   */
  public static boolean equals(char[] a, char[] a2) {
    if (a == a2) return true;
    if (a == null || a2 == null) return false;

    int length = a.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) return false;
    }

    return true;
  }

  /**
   * Returns <tt>true</tt> if the two specified arrays of bytes are
   * <i>equal</i> to one another.  Two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs
   * of elements in the two arrays are equal.  In other words, two arrays
   * are equal if they contain the same elements in the same order.  Also,
   * two array references are considered equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   */
  public static boolean equals(byte[] a, byte[] a2) {
    if (a == a2) return true;
    if (a == null || a2 == null) return false;

    int length = a.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) return false;
    }

    return true;
  }

  /**
   * Returns <tt>true</tt> if the two specified arrays of booleans are
   * <i>equal</i> to one another.  Two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs
   * of elements in the two arrays are equal.  In other words, two arrays
   * are equal if they contain the same elements in the same order.  Also,
   * two array references are considered equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   */
  public static boolean equals(boolean[] a, boolean[] a2) {
    if (a == a2) return true;
    if (a == null || a2 == null) return false;

    int length = a.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      if (a[i] != a2[i]) return false;
    }

    return true;
  }

  /**
   * Returns <tt>true</tt> if the two specified arrays of doubles are
   * <i>equal</i> to one another.  Two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs
   * of elements in the two arrays are equal.  In other words, two arrays
   * are equal if they contain the same elements in the same order.  Also,
   * two array references are considered equal if both are <tt>null</tt>.<p>
   *
   * Two doubles <tt>d1</tt> and <tt>d2</tt> are considered equal if:
   * <pre>    <tt>new Double(d1).equals(new Double(d2))</tt></pre>
   * (Unlike the <tt>==</tt> operator, this method considers
   * <tt>NaN</tt> equals to itself, and 0.0d unequal to -0.0d.)
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   * @see Double#equals(Object)
   */
  public static boolean equals(double[] a, double[] a2) {
    if (a == a2) return true;
    if (a == null || a2 == null) return false;

    int length = a.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++)
      if (Double.doubleToLongBits(a[i]) != Double.doubleToLongBits(a2[i])) return false;

    return true;
  }

  /**
   * Returns <tt>true</tt> if the two specified arrays of floats are
   * <i>equal</i> to one another.  Two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs
   * of elements in the two arrays are equal.  In other words, two arrays
   * are equal if they contain the same elements in the same order.  Also,
   * two array references are considered equal if both are <tt>null</tt>.<p>
   *
   * Two floats <tt>f1</tt> and <tt>f2</tt> are considered equal if:
   * <pre>    <tt>new Float(f1).equals(new Float(f2))</tt></pre>
   * (Unlike the <tt>==</tt> operator, this method considers
   * <tt>NaN</tt> equals to itself, and 0.0f unequal to -0.0f.)
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   * @see Float#equals(Object)
   */
  public static boolean equals(float[] a, float[] a2) {
    if (a == a2) return true;
    if (a == null || a2 == null) return false;

    int length = a.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++)
      if (Float.floatToIntBits(a[i]) != Float.floatToIntBits(a2[i])) return false;

    return true;
  }

  /**
   * Returns <tt>true</tt> if the two specified arrays of Objects are
   * <i>equal</i> to one another.  The two arrays are considered equal if
   * both arrays contain the same number of elements, and all corresponding
   * pairs of elements in the two arrays are equal.  Two objects <tt>e1</tt>
   * and <tt>e2</tt> are considered <i>equal</i> if <tt>(e1==null ? e2==null
   * : e1.equals(e2))</tt>.  In other words, the two arrays are equal if
   * they contain the same elements in the same order.  Also, two array
   * references are considered equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   */
  public static boolean equals(Object[] a, Object[] a2) {
    if (a == a2) return true;
    if (a == null || a2 == null) return false;

    int length = a.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      Object o1 = a[i];
      Object o2 = a2[i];
      if (!(o1 == null ? o2 == null : o1.equals(o2))) return false;
    }

    return true;
  }

  // Filling

  /**
   * Assigns the specified long value to each element of the specified array
   * of longs.
   *
   * @param a the array to be filled.
   * @param val the value to be stored in all elements of the array.
   */
  public static void fill(long[] a, long val) {
    fill(a, 0, a.length, val);
  }

  /**
   * Assigns the specified long value to each element of the specified
   * range of the specified array of longs.  The range to be filled
   * extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be filled is empty.)
   *
   * @param a the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be
   *        filled with the specified value.
   * @param toIndex the index of the last element (exclusive) to be
   *        filled with the specified value.
   * @param val the value to be stored in all elements of the array.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void fill(long[] a, int fromIndex, int toIndex, long val) {
    rangeCheck(a.length, fromIndex, toIndex);
    for (int i = fromIndex; i < toIndex; i++) a[i] = val;
  }

  /**
   * Assigns the specified int value to each element of the specified array
   * of ints.
   *
   * @param a the array to be filled.
   * @param val the value to be stored in all elements of the array.
   */
  public static void fill(int[] a, int val) {
    fill(a, 0, a.length, val);
  }

  /**
   * Assigns the specified int value to each element of the specified
   * range of the specified array of ints.  The range to be filled
   * extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be filled is empty.)
   *
   * @param a the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be
   *        filled with the specified value.
   * @param toIndex the index of the last element (exclusive) to be
   *        filled with the specified value.
   * @param val the value to be stored in all elements of the array.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void fill(int[] a, int fromIndex, int toIndex, int val) {
    rangeCheck(a.length, fromIndex, toIndex);
    for (int i = fromIndex; i < toIndex; i++) a[i] = val;
  }

  /**
   * Assigns the specified short value to each element of the specified array
   * of shorts.
   *
   * @param a the array to be filled.
   * @param val the value to be stored in all elements of the array.
   */
  public static void fill(short[] a, short val) {
    fill(a, 0, a.length, val);
  }

  /**
   * Assigns the specified short value to each element of the specified
   * range of the specified array of shorts.  The range to be filled
   * extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be filled is empty.)
   *
   * @param a the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be
   *        filled with the specified value.
   * @param toIndex the index of the last element (exclusive) to be
   *        filled with the specified value.
   * @param val the value to be stored in all elements of the array.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void fill(short[] a, int fromIndex, int toIndex, short val) {
    rangeCheck(a.length, fromIndex, toIndex);
    for (int i = fromIndex; i < toIndex; i++) a[i] = val;
  }

  /**
   * Assigns the specified char value to each element of the specified array
   * of chars.
   *
   * @param a the array to be filled.
   * @param val the value to be stored in all elements of the array.
   */
  public static void fill(char[] a, char val) {
    fill(a, 0, a.length, val);
  }

  /**
   * Assigns the specified char value to each element of the specified
   * range of the specified array of chars.  The range to be filled
   * extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be filled is empty.)
   *
   * @param a the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be
   *        filled with the specified value.
   * @param toIndex the index of the last element (exclusive) to be
   *        filled with the specified value.
   * @param val the value to be stored in all elements of the array.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void fill(char[] a, int fromIndex, int toIndex, char val) {
    rangeCheck(a.length, fromIndex, toIndex);
    for (int i = fromIndex; i < toIndex; i++) a[i] = val;
  }

  /**
   * Assigns the specified byte value to each element of the specified array
   * of bytes.
   *
   * @param a the array to be filled.
   * @param val the value to be stored in all elements of the array.
   */
  public static void fill(byte[] a, byte val) {
    fill(a, 0, a.length, val);
  }

  /**
   * Assigns the specified byte value to each element of the specified
   * range of the specified array of bytes.  The range to be filled
   * extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be filled is empty.)
   *
   * @param a the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be
   *        filled with the specified value.
   * @param toIndex the index of the last element (exclusive) to be
   *        filled with the specified value.
   * @param val the value to be stored in all elements of the array.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void fill(byte[] a, int fromIndex, int toIndex, byte val) {
    rangeCheck(a.length, fromIndex, toIndex);
    for (int i = fromIndex; i < toIndex; i++) a[i] = val;
  }

  /**
   * Assigns the specified boolean value to each element of the specified
   * array of booleans.
   *
   * @param a the array to be filled.
   * @param val the value to be stored in all elements of the array.
   */
  public static void fill(boolean[] a, boolean val) {
    fill(a, 0, a.length, val);
  }

  /**
   * Assigns the specified boolean value to each element of the specified
   * range of the specified array of booleans.  The range to be filled
   * extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be filled is empty.)
   *
   * @param a the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be
   *        filled with the specified value.
   * @param toIndex the index of the last element (exclusive) to be
   *        filled with the specified value.
   * @param val the value to be stored in all elements of the array.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void fill(boolean[] a, int fromIndex, int toIndex, boolean val) {
    rangeCheck(a.length, fromIndex, toIndex);
    for (int i = fromIndex; i < toIndex; i++) a[i] = val;
  }

  /**
   * Assigns the specified double value to each element of the specified
   * array of doubles.
   *
   * @param a the array to be filled.
   * @param val the value to be stored in all elements of the array.
   */
  public static void fill(double[] a, double val) {
    fill(a, 0, a.length, val);
  }

  /**
   * Assigns the specified double value to each element of the specified
   * range of the specified array of doubles.  The range to be filled
   * extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be filled is empty.)
   *
   * @param a the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be
   *        filled with the specified value.
   * @param toIndex the index of the last element (exclusive) to be
   *        filled with the specified value.
   * @param val the value to be stored in all elements of the array.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void fill(double[] a, int fromIndex, int toIndex, double val) {
    rangeCheck(a.length, fromIndex, toIndex);
    for (int i = fromIndex; i < toIndex; i++) a[i] = val;
  }

  /**
   * Assigns the specified float value to each element of the specified array
   * of floats.
   *
   * @param a the array to be filled.
   * @param val the value to be stored in all elements of the array.
   */
  public static void fill(float[] a, float val) {
    fill(a, 0, a.length, val);
  }

  /**
   * Assigns the specified float value to each element of the specified
   * range of the specified array of floats.  The range to be filled
   * extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be filled is empty.)
   *
   * @param a the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be
   *        filled with the specified value.
   * @param toIndex the index of the last element (exclusive) to be
   *        filled with the specified value.
   * @param val the value to be stored in all elements of the array.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void fill(float[] a, int fromIndex, int toIndex, float val) {
    rangeCheck(a.length, fromIndex, toIndex);
    for (int i = fromIndex; i < toIndex; i++) a[i] = val;
  }

  /**
   * Assigns the specified Object reference to each element of the specified
   * array of Objects.
   *
   * @param a the array to be filled.
   * @param val the value to be stored in all elements of the array.
   */
  public static void fill(Object[] a, Object val) {
    fill(a, 0, a.length, val);
  }

  /**
   * Assigns the specified Object reference to each element of the specified
   * range of the specified array of Objects.  The range to be filled
   * extends from index <tt>fromIndex</tt>, inclusive, to index
   * <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex==toIndex</tt>, the
   * range to be filled is empty.)
   *
   * @param a the array to be filled.
   * @param fromIndex the index of the first element (inclusive) to be
   *        filled with the specified value.
   * @param toIndex the index of the last element (exclusive) to be
   *        filled with the specified value.
   * @param val the value to be stored in all elements of the array.
   * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
   * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
   *	       <tt>toIndex &gt; a.length</tt>
   */
  public static void fill(Object[] a, int fromIndex, int toIndex, Object val) {
    rangeCheck(a.length, fromIndex, toIndex);
    for (int i = fromIndex; i < toIndex; i++) a[i] = val;
  }

  // Misc

  /**
   * Returns a fixed-size list backed by the specified array.  (Changes to
   * the returned list "write through" to the array.)  This method acts
   * as bridge between array-based and collection-based APIs, in
   * combination with <tt>Collection.toArray</tt>.  The returned list is
   * serializable and implements {@link RandomAccess}.
   *
   * @param a the array by which the list will be backed.
   * @return a list view of the specified array.
   * @see Collection#toArray()
   */
  public static List asList(Object[] a) {
    return new ArrayList(a);
  }

  /**
   * @serial include
   */
  private static class ArrayList extends AbstractList
      implements RandomAccess, java.io.Serializable {
    private static final long serialVersionUID = -2764017481108945198L;
    private Object[] a;

    ArrayList(Object[] array) {
      if (array == null) throw new NullPointerException();
      a = array;
    }

    public int size() {
      return a.length;
    }

    public Object[] toArray() {
      return (Object[]) a.clone();
    }

    public Object get(int index) {
      return a[index];
    }

    public Object set(int index, Object element) {
      Object oldValue = a[index];
      a[index] = element;
      return oldValue;
    }

    public int indexOf(Object o) {
      if (o == null) {
        for (int i = 0; i < a.length; i++) if (a[i] == null) return i;
      } else {
        for (int i = 0; i < a.length; i++) if (o.equals(a[i])) return i;
      }
      return -1;
    }

    public boolean contains(Object o) {
      return indexOf(o) != -1;
    }
  }
}
