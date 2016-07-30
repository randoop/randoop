/*
 * @(#)Random.java	1.39 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java2.util2;

import java.io.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An instance of this class is used to generate a stream of
 * pseudorandom numbers. The class uses a 48-bit seed, which is
 * modified using a linear congruential formula. (See Donald Knuth,
 * <i>The Art of Computer Programming, Volume 2</i>, Section 3.2.1.)
 * <p>
 * If two instances of <code>Random</code> are created with the same
 * seed, and the same sequence of method calls is made for each, they
 * will generate and return identical sequences of numbers. In order to
 * guarantee this property, particular algorithms are specified for the
 * class <tt>Random</tt>. Java implementations must use all the algorithms
 * shown here for the class <tt>Random</tt>, for the sake of absolute
 * portability of Java code. However, subclasses of class <tt>Random</tt>
 * are permitted to use other algorithms, so long as they adhere to the
 * general contracts for all the methods.
 * <p>
 * The algorithms implemented by class <tt>Random</tt> use a
 * <tt>protected</tt> utility method that on each invocation can supply
 * up to 32 pseudorandomly generated bits.
 * <p>
 * Many applications will find the <code>random</code> method in
 * class <code>Math</code> simpler to use.
 *
 * @author  Frank Yellin
 * @version 1.39, 01/23/03
 * @see     java.lang.Math#random()
 * @since   JDK1.0
 */
public class Random implements java.io.Serializable {
  /** use serialVersionUID from JDK 1.1 for interoperability */
  static final long serialVersionUID = 3905348978240129619L;

  /**
   * The internal state associated with this pseudorandom number generator.
   * (The specs for the methods in this class describe the ongoing
   * computation of this value.)
   *
   * @serial
   */
  private AtomicLong seed;

  private final static long multiplier = 0x5DEECE66DL;
  private final static long addend = 0xBL;
  private final static long mask = (1L << 48) - 1;

  /**
   * Creates a new random number generator. Its seed is initialized to
   * a value based on the current time:
   * <blockquote><pre>
   * public Random() { this(System.currentTimeMillis()); }</pre></blockquote>
   * Two Random objects created within the same millisecond will have
   * the same sequence of random numbers.
   *
   * @see     java.lang.System#currentTimeMillis()
   */
  public Random() {
    this(System.currentTimeMillis());
  }

  /**
   * Creates a new random number generator using a single
   * <code>long</code> seed:
   * <blockquote><pre>
   * public Random(long seed) { setSeed(seed); }</pre></blockquote>
   * Used by method <tt>next</tt> to hold
   * the state of the pseudorandom number generator.
   *
   * @param   seed   the initial seed.
   * @see     java2.util2.Random#setSeed(long)
   */
  public Random(long seed) {
    this.seed = new AtomicLong(0L);
    setSeed(seed);
  }

  /**
   * Sets the seed of this random number generator using a single
   * <code>long</code> seed. The general contract of <tt>setSeed</tt>
   * is that it alters the state of this random number generator
   * object so as to be in exactly the same state as if it had just
   * been created with the argument <tt>seed</tt> as a seed. The method
   * <tt>setSeed</tt> is implemented by class Random as follows:
   * <blockquote><pre>
   * synchronized public void setSeed(long seed) {
   *       this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
   *       haveNextNextGaussian = false;
   * }</pre></blockquote>
   * The implementation of <tt>setSeed</tt> by class <tt>Random</tt>
   * happens to use only 48 bits of the given seed. In general, however,
   * an overriding method may use all 64 bits of the long argument
   * as a seed value.
   *
   * Note: Although the seed value is an AtomicLong, this method
   *       must still be synchronized to ensure correct semantics
   *       of haveNextNextGaussian.
   *
   * @param   seed   the initial seed.
   */
  synchronized public void setSeed(long seed) {
    seed = (seed ^ multiplier) & mask;
    this.seed.set(seed);
    haveNextNextGaussian = false;
  }

  /**
   * Generates the next pseudorandom number. Subclass should
   * override this, as this is used by all other methods.<p>
   * The general contract of <tt>next</tt> is that it returns an
   * <tt>int</tt> value and if the argument bits is between <tt>1</tt>
   * and <tt>32</tt> (inclusive), then that many low-order bits of the
   * returned value will be (approximately) independently chosen bit
   * values, each of which is (approximately) equally likely to be
   * <tt>0</tt> or <tt>1</tt>. The method <tt>next</tt> is implemented
   * by class <tt>Random</tt> as follows:
   * <blockquote><pre>
   * synchronized protected int next(int bits) {
   *       seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
   *       return (int)(seed >>> (48 - bits));
   * }</pre></blockquote>
   * This is a linear congruential pseudorandom number generator, as
   * defined by D. H. Lehmer and described by Donald E. Knuth in <i>The
   * Art of Computer Programming,</i> Volume 2: <i>Seminumerical
   * Algorithms</i>, section 3.2.1.
   *
   * @param   bits random bits
   * @return  the next pseudorandom value from this random number generator's sequence.
   * @since   JDK1.1
   */
  protected int next(int bits) {
    long oldseed, nextseed;
    do {
      oldseed = seed.get();
      nextseed = (oldseed * multiplier + addend) & mask;
    } while (!seed.compareAndSet(oldseed, nextseed));
    return (int) (nextseed >>> (48 - bits));
  }

  private static final int BITS_PER_BYTE = 8;
  private static final int BYTES_PER_INT = 4;

  /**
   * Generates random bytes and places them into a user-supplied
   * byte array.  The number of random bytes produced is equal to
   * the length of the byte array.
   *
   * @param bytes  the non-null byte array in which to put the
   *               random bytes.
   * @since   JDK1.1
   */
  public void nextBytes(byte[] bytes) {
    int numRequested = bytes.length;

    int numGot = 0, rnd = 0;

    while (true) {
      for (int i = 0; i < BYTES_PER_INT; i++) {
        if (numGot == numRequested) {
          return;
        }

        rnd = (i == 0 ? next(BITS_PER_BYTE * BYTES_PER_INT) : rnd >> BITS_PER_BYTE);
        bytes[numGot++] = (byte) rnd;
      }
    }
  }

  /**
   * Returns the next pseudorandom, uniformly distributed <code>int</code>
   * value from this random number generator's sequence. The general
   * contract of <tt>nextInt</tt> is that one <tt>int</tt> value is
   * pseudorandomly generated and returned. All 2<font size="-1"><sup>32
   * </sup></font> possible <tt>int</tt> values are produced with
   * (approximately) equal probability. The method <tt>nextInt</tt> is
   * implemented by class <tt>Random</tt> as follows:
   * <blockquote><pre>
   * public int nextInt() {  return next(32); }</pre></blockquote>
   *
   * @return  the next pseudorandom, uniformly distributed <code>int</code>
   *          value from this random number generator's sequence.
   */
  public int nextInt() {
    return next(32);
  }

  /**
   * Returns a pseudorandom, uniformly distributed <tt>int</tt> value
   * between 0 (inclusive) and the specified value (exclusive), drawn from
   * this random number generator's sequence.  The general contract of
   * <tt>nextInt</tt> is that one <tt>int</tt> value in the specified range
   * is pseudorandomly generated and returned.  All <tt>n</tt> possible
   * <tt>int</tt> values are produced with (approximately) equal
   * probability.  The method <tt>nextInt(int n)</tt> is implemented by
   * class <tt>Random</tt> as follows:
   * <blockquote><pre>
   * public int nextInt(int n) {
   *     if (n<=0)
   *		throw new IllegalArgumentException("n must be positive");
   *
   *     if ((n & -n) == n)  // i.e., n is a power of 2
   *         return (int)((n * (long)next(31)) >> 31);
   *
   *     int bits, val;
   *     do {
   *         bits = next(31);
   *         val = bits % n;
   *     } while(bits - val + (n-1) < 0);
   *     return val;
   * }
   * </pre></blockquote>
   * <p>
   * The hedge "approximately" is used in the foregoing description only
   * because the next method is only approximately an unbiased source of
   * independently chosen bits.  If it were a perfect source of randomly
   * chosen bits, then the algorithm shown would choose <tt>int</tt>
   * values from the stated range with perfect uniformity.
   * <p>
   * The algorithm is slightly tricky.  It rejects values that would result
   * in an uneven distribution (due to the fact that 2^31 is not divisible
   * by n). The probability of a value being rejected depends on n.  The
   * worst case is n=2^30+1, for which the probability of a reject is 1/2,
   * and the expected number of iterations before the loop terminates is 2.
   * <p>
   * The algorithm treats the case where n is a power of two specially: it
   * returns the correct number of high-order bits from the underlying
   * pseudo-random number generator.  In the absence of special treatment,
   * the correct number of <i>low-order</i> bits would be returned.  Linear
   * congruential pseudo-random number generators such as the one
   * implemented by this class are known to have short periods in the
   * sequence of values of their low-order bits.  Thus, this special case
   * greatly increases the length of the sequence of values returned by
   * successive calls to this method if n is a small power of two.
   *
   * @param n the bound on the random number to be returned.  Must be
   *	      positive.
   * @return  a pseudorandom, uniformly distributed <tt>int</tt>
   *          value between 0 (inclusive) and n (exclusive).
   * @exception IllegalArgumentException n is not positive.
   * @since 1.2
   */
  public int nextInt(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("n must be positive");
    }

    if ((n & -n) == n) // i.e., n is a power of 2
    return (int) ((n * (long) next(31)) >> 31);

    int bits, val;
    do {
      bits = next(31);
      val = bits % n;
    } while (bits - val + (n - 1) < 0);
    return val;
  }

  /**
   * Returns the next pseudorandom, uniformly distributed <code>long</code>
   * value from this random number generator's sequence. The general
   * contract of <tt>nextLong</tt> is that one long value is pseudorandomly
   * generated and returned. All 2<font size="-1"><sup>64</sup></font>
   * possible <tt>long</tt> values are produced with (approximately) equal
   * probability. The method <tt>nextLong</tt> is implemented by class
   * <tt>Random</tt> as follows:
   * <blockquote><pre>
   * public long nextLong() {
   *       return ((long)next(32) << 32) + next(32);
   * }</pre></blockquote>
   *
   * @return  the next pseudorandom, uniformly distributed <code>long</code>
   *          value from this random number generator's sequence.
   */
  public long nextLong() {
    // it's okay that the bottom word remains signed.
    return ((long) (next(32)) << 32) + next(32);
  }

  /**
   * Returns the next pseudorandom, uniformly distributed
   * <code>boolean</code> value from this random number generator's
   * sequence. The general contract of <tt>nextBoolean</tt> is that one
   * <tt>boolean</tt> value is pseudorandomly generated and returned.  The
   * values <code>true</code> and <code>false</code> are produced with
   * (approximately) equal probability. The method <tt>nextBoolean</tt> is
   * implemented by class <tt>Random</tt> as follows:
   * <blockquote><pre>
   * public boolean nextBoolean() {return next(1) != 0;}
   * </pre></blockquote>
   * @return  the next pseudorandom, uniformly distributed
   *          <code>boolean</code> value from this random number generator's
   *		sequence.
   * @since 1.2
   */
  public boolean nextBoolean() {
    return next(1) != 0;
  }

  /**
   * Returns the next pseudorandom, uniformly distributed <code>float</code>
   * value between <code>0.0</code> and <code>1.0</code> from this random
   * number generator's sequence. <p>
   * The general contract of <tt>nextFloat</tt> is that one <tt>float</tt>
   * value, chosen (approximately) uniformly from the range <tt>0.0f</tt>
   * (inclusive) to <tt>1.0f</tt> (exclusive), is pseudorandomly
   * generated and returned. All 2<font size="-1"><sup>24</sup></font>
   * possible <tt>float</tt> values of the form
   * <i>m&nbsp;x&nbsp</i>2<font size="-1"><sup>-24</sup></font>, where
   * <i>m</i> is a positive integer less than 2<font size="-1"><sup>24</sup>
   * </font>, are produced with (approximately) equal probability. The
   * method <tt>nextFloat</tt> is implemented by class <tt>Random</tt> as
   * follows:
   * <blockquote><pre>
   * public float nextFloat() {
   *      return next(24) / ((float)(1 << 24));
   * }</pre></blockquote>
   * The hedge "approximately" is used in the foregoing description only
   * because the next method is only approximately an unbiased source of
   * independently chosen bits. If it were a perfect source or randomly
   * chosen bits, then the algorithm shown would choose <tt>float</tt>
   * values from the stated range with perfect uniformity.<p>
   * [In early versions of Java, the result was incorrectly calculated as:
   * <blockquote><pre>
   * return next(30) / ((float)(1 << 30));</pre></blockquote>
   * This might seem to be equivalent, if not better, but in fact it
   * introduced a slight nonuniformity because of the bias in the rounding
   * of floating-point numbers: it was slightly more likely that the
   * low-order bit of the significand would be 0 than that it would be 1.]
   *
   * @return  the next pseudorandom, uniformly distributed <code>float</code>
   *          value between <code>0.0</code> and <code>1.0</code> from this
   *          random number generator's sequence.
   */
  public float nextFloat() {
    int i = next(24);
    return i / ((float) (1 << 24));
  }

  /**
   * Returns the next pseudorandom, uniformly distributed
   * <code>double</code> value between <code>0.0</code> and
   * <code>1.0</code> from this random number generator's sequence. <p>
   * The general contract of <tt>nextDouble</tt> is that one
   * <tt>double</tt> value, chosen (approximately) uniformly from the
   * range <tt>0.0d</tt> (inclusive) to <tt>1.0d</tt> (exclusive), is
   * pseudorandomly generated and returned. All
   * 2<font size="-1"><sup>53</sup></font> possible <tt>float</tt>
   * values of the form <i>m&nbsp;x&nbsp;</i>2<font size="-1"><sup>-53</sup>
   * </font>, where <i>m</i> is a positive integer less than
   * 2<font size="-1"><sup>53</sup></font>, are produced with
   * (approximately) equal probability. The method <tt>nextDouble</tt> is
   * implemented by class <tt>Random</tt> as follows:
   * <blockquote><pre>
   * public double nextDouble() {
   *       return (((long)next(26) << 27) + next(27))
   *           / (double)(1L << 53);
   * }</pre></blockquote><p>
   * The hedge "approximately" is used in the foregoing description only
   * because the <tt>next</tt> method is only approximately an unbiased
   * source of independently chosen bits. If it were a perfect source or
   * randomly chosen bits, then the algorithm shown would choose
   * <tt>double</tt> values from the stated range with perfect uniformity.
   * <p>[In early versions of Java, the result was incorrectly calculated as:
   * <blockquote><pre>
   *  return (((long)next(27) << 27) + next(27))
   *      / (double)(1L << 54);</pre></blockquote>
   * This might seem to be equivalent, if not better, but in fact it
   * introduced a large nonuniformity because of the bias in the rounding
   * of floating-point numbers: it was three times as likely that the
   * low-order bit of the significand would be 0 than that it would be
   * 1! This nonuniformity probably doesn't matter much in practice, but
   * we strive for perfection.]
   *
   * @return  the next pseudorandom, uniformly distributed
   *          <code>double</code> value between <code>0.0</code> and
   *          <code>1.0</code> from this random number generator's sequence.
   */
  public double nextDouble() {
    long l = ((long) (next(26)) << 27) + next(27);
    return l / (double) (1L << 53);
  }

  private double nextNextGaussian;
  private boolean haveNextNextGaussian = false;

  /**
   * Returns the next pseudorandom, Gaussian ("normally") distributed
   * <code>double</code> value with mean <code>0.0</code> and standard
   * deviation <code>1.0</code> from this random number generator's sequence.
   * <p>
   * The general contract of <tt>nextGaussian</tt> is that one
   * <tt>double</tt> value, chosen from (approximately) the usual
   * normal distribution with mean <tt>0.0</tt> and standard deviation
   * <tt>1.0</tt>, is pseudorandomly generated and returned. The method
   * <tt>nextGaussian</tt> is implemented by class <tt>Random</tt> as follows:
   * <blockquote><pre>
   * synchronized public double nextGaussian() {
   *    if (haveNextNextGaussian) {
   *            haveNextNextGaussian = false;
   *            return nextNextGaussian;
   *    } else {
   *            double v1, v2, s;
   *            do {
   *                    v1 = 2 * nextDouble() - 1;   // between -1.0 and 1.0
   *                    v2 = 2 * nextDouble() - 1;   // between -1.0 and 1.0
   *                    s = v1 * v1 + v2 * v2;
   *            } while (s >= 1 || s == 0);
   *            double multiplier = Math.sqrt(-2 * Math.log(s)/s);
   *            nextNextGaussian = v2 * multiplier;
   *            haveNextNextGaussian = true;
   *            return v1 * multiplier;
   *    }
   * }</pre></blockquote>
   * This uses the <i>polar method</i> of G. E. P. Box, M. E. Muller, and
   * G. Marsaglia, as described by Donald E. Knuth in <i>The Art of
   * Computer Programming</i>, Volume 2: <i>Seminumerical Algorithms</i>,
   * section 3.4.1, subsection C, algorithm P. Note that it generates two
   * independent values at the cost of only one call to <tt>Math.log</tt>
   * and one call to <tt>Math.sqrt</tt>.
   *
   * @return  the next pseudorandom, Gaussian ("normally") distributed
   *          <code>double</code> value with mean <code>0.0</code> and
   *          standard deviation <code>1.0</code> from this random number
   *          generator's sequence.
   */
  synchronized public double nextGaussian() {
    // See Knuth, ACP, Section 3.4.1 Algorithm C.
    if (haveNextNextGaussian) {
      haveNextNextGaussian = false;
      return nextNextGaussian;
    } else {
      double v1, v2, s;
      do {
        v1 = 2 * nextDouble() - 1; // between -1 and 1
        v2 = 2 * nextDouble() - 1; // between -1 and 1
        s = v1 * v1 + v2 * v2;
      } while (s >= 1 || s == 0);
      double multiplier = Math.sqrt(-2 * Math.log(s) / s);
      nextNextGaussian = v2 * multiplier;
      haveNextNextGaussian = true;
      return v1 * multiplier;
    }
  }

  /**
   * Serializable fields for Random.
   *
   * @serialField    seed long;
   *              seed for random computations
   * @serialField    nextNextGaussian double;
   *              next Gaussian to be returned
   * @serialField      haveNextNextGaussian boolean
   *              nextNextGaussian is valid
   */
  private static final ObjectStreamField[] serialPersistentFields = {
    new ObjectStreamField("seed", Long.TYPE),
    new ObjectStreamField("nextNextGaussian", Double.TYPE),
    new ObjectStreamField("haveNextNextGaussian", Boolean.TYPE)
  };

  /**
   * Reconstitute the <tt>Random</tt> instance from a stream (that is,
   * deserialize it). The seed is read in as long for
   * historical reasons, but it is converted to an AtomicLong.
   */
  private void readObject(java.io.ObjectInputStream s)
      throws java.io.IOException, ClassNotFoundException {

    ObjectInputStream.GetField fields = s.readFields();
    long seedVal;

    seedVal = (long) fields.get("seed", -1L);
    if (seedVal < 0) throw new java.io.StreamCorruptedException("Random: invalid seed");
    seed = new AtomicLong(seedVal);
    nextNextGaussian = fields.get("nextNextGaussian", 0.0);
    haveNextNextGaussian = fields.get("haveNextNextGaussian", false);
  }

  /**
   * Save the <tt>Random</tt> instance to a stream.
   * The seed of a Random is serialized as a long for
   * historical reasons.
   *
   */
  synchronized private void writeObject(ObjectOutputStream s) throws IOException {
    // set the values of the Serializable fields
    ObjectOutputStream.PutField fields = s.putFields();
    fields.put("seed", seed.get());
    fields.put("nextNextGaussian", nextNextGaussian);
    fields.put("haveNextNextGaussian", haveNextNextGaussian);

    // save them
    s.writeFields();
  }
}
