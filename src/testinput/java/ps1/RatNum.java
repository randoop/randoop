package ps1;

/** RatNum represents an immutable rational number.
 * It includes all of the elements of the set of rationals, as well
 * as the special "NaN" (not-a-number) element that results from
 * division by zero.
 * <p>
 * The "NaN" element is special in many ways.  Any arithmetic
 * operation (such as addition) involving "NaN" will return "NaN".
 * With respect to comparison operations, such as less-than, "NaN" is
 * considered equal to itself, and larger than all other rationals.
 * <p>
 * Examples of RatNums include "-1/13", "53/7", "4", "NaN", and "0".
 */
public class RatNum {

  public int numer;
  public int denom;
  public double approx;

  // Abstraction Function:
  // A RatNum r is NaN if r.denom = 0, (r.numer / r.denom) otherwise.
  // (An abstraction function explains what the state of the fields in a
  // RatNum represents.  In this case, a rational number can be
  // understood as the result of dividing two integers, or not-a-number
  // if we would be dividing by zero.)

  // Representation invariant for every RatNum r:
  //  (r.denom >= 0) &&
  //  (r.denom > 0 ==> there does not exist integer i > 1 such that
  //                     r.numer mod i = 0 and r.denom mod i = 0)
  // (A representation invariant tells us something that is true for all
  // instances of a RatNum; in this case, that the denominator is always
  // greater than zero and that if the denominator is non-zero, the
  // fraction represented is in reduced form.)

  /** effects Constructs a new RatNum = n. */
  public RatNum(int n) {
    numer = n;
    denom = 1;
    checkRep();
    approx = this.approx();
  }

  /** effects If d = 0, constructs a new RatNum = NaN.  Else
   * constructs a new RatNum = (n / d).
   */
  public RatNum(int n, int d) {
    // special case for zero denominator; gcd(n,d) requires d != 0
    if (d == 0) {
      numer = n;
      denom = 0;
      checkRep();
      approx = this.approx();
      return;
    }

    // reduce ratio to lowest terms
    int g = gcd(n, d);
    numer = n / g;
    denom = d / g;

    if (denom < 0) {
      numer = -numer;
      denom = -denom;
      checkRep();
    }
    approx = this.approx();
  }

  /** Checks to see if the representation invariant is being violated and if so, throws RuntimeException
   * @throws RuntimeException if representation invariant is violated
   */
  private void checkRep() throws RuntimeException {
    if (denom < 0) {
      throw new RuntimeException("Denominator of a RatNum cannot be less than zero");
    }

    if (denom > 0) {
      int thisGcd = gcd(numer, denom);
      if (thisGcd != 1 && thisGcd != -1) {
        throw new RuntimeException("RatNum not in lowest form");
      }
    }
  }

  /** @return true iff this is NaN (not-a-number) */
  public boolean isNaN() {
    checkRep();
    return (denom == 0);
  }

  /** @return true iff this < 0. */
  public boolean isNegative() {
    checkRep();
    return (compareTo(new RatNum(0)) < 0);
  }

  /** @return true iff this > 0. */
  public boolean isPositive() {
    checkRep();
    return (compareTo(new RatNum(0)) > 0);
  }

  /** requires rn != null
   * @return a negative number if this < rn,
   * 0 if this = rn,
   * a positive number if this > rn
   */
  public int compareTo(RatNum rn) {
    checkRep();
    if (this.isNaN() && rn.isNaN()) {
      checkRep();
      return 0;
    } else if (this.isNaN()) {
      checkRep();
      return 1;
    } else if (rn.isNaN()) {
      checkRep();
      return -1;
    } else {
      RatNum diff = this.sub(rn);
      checkRep();
      return diff.numer;
    }
  }

  /** Approximates the value of this rational.
   * @return a double approximation for this.  Note that "NaN" is
   * mapped to {@link Double#NaN}, and the {@link Double#NaN} value
   * is treated in a special manner by several arithmetic operations,
   * such as the comparison and equality operators.  See the
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/typesVariables.doc.html#9208">
   * Java Language Specification, section 4.2.3</a>, for more details.
   */
  public double approx() {
    checkRep();
    if (isNaN()) {
      checkRep();
      return Double.NaN;
    } else {
      // convert int values to doubles before dividing.
      checkRep();
      return ((double) numer) / ((double) denom);
    }
  }

  /** @return a String representing this, in reduced terms.
   * The returned string will either be "NaN", or it will take on
   * either of the forms "N" or "N/M", where N and M are both
   * integers in decimal notation and M != 0.
   */
  public String unparse() {
    // using '+' as String concatenation operator in this method
    checkRep();
    if (isNaN()) {
      checkRep();
      return "NaN";
    } else if (denom != 1) {
      checkRep();
      return numer + "/" + denom;
    } else {
      checkRep();
      return Integer.toString(numer);
    }
  }

  // in the implementation comments for the following methods, <this>
  // is notated as "a/b" and <arg> likewise as "x/y"

  /** @return a new Rational equal to (0 - this). */
  public RatNum negate() {
    checkRep();
    return new RatNum(-this.numer, this.denom);
  }

  /** requires arg != null
   * @return a new RatNum equal to (this + arg).
   * If either argument is NaN, then returns NaN.
   */
  public RatNum add(RatNum arg) {
    // a/b + x/y = ay/by + bx/by = (ay + bx)/by
    checkRep();
    return new RatNum(this.numer * arg.denom + arg.numer * this.denom, this.denom * arg.denom);
  }

  /** requires arg != null
   * @return a new RatNum equal to (this - arg).
   * If either argument is NaN, then returns NaN.
   */
  public RatNum sub(RatNum arg) {
    // a/b - x/y = a/b + -x/y
    checkRep();
    return this.add(arg.negate());
  }

  /** requires arg != null
   * @return a new RatNum equal to (this * arg).
   * If either argument is NaN, then returns NaN.
   */
  public RatNum mul(RatNum arg) {
    // (a/b) * (x/y) = ax/by
    checkRep();
    return new RatNum(this.numer * arg.numer, this.denom * arg.denom);
  }

  /** requires arg != null
   * @return a new RatNum equal to (this / arg).
   * If arg is zero, or if either argument is NaN, then returns NaN.
   */
  public RatNum div(RatNum arg) {
    // (a/b) / (x/y) = ay/bx
    checkRep();
    if (arg.isNaN()) {
      checkRep();
      return arg;
    } else {
      checkRep();
      return new RatNum(this.numer * arg.denom, this.denom * arg.numer);
    }
  }

  /** Returns the greatest common divisor of 'a' and 'b'.
   * requires b != 0
   * @return d such that a % d = 0 and b % d = 0
   */
  private static int gcd(int a, int b) {
    // Euclid's method
    if (b == 0) return 0;
    while (b != 0) {
      int tmp = b;
      b = a % b;
      a = tmp;
    }
    return a;
  }

  /** Standard hashCode function.
   * @return an int that all objects equal to this will also
   * return
   */
  @Override
  public int hashCode() {
    checkRep();
    // all instances that are NaN must return the same hashcode;
    if (this.isNaN()) {
      return 0;
    }
    return this.numer * 2 + this.denom * 3;
  }

  /** Standard equality operation.
   * @return true if and only if 'obj' is an instance of a RatNum
   * and 'this' = 'obj'.  Note that NaN = NaN for RatNums.
   */
  @Override
  public boolean equals(Object obj) {
    checkRep();
    if (obj instanceof RatNum) {
      RatNum rn = (RatNum) obj;

      // special case: check if both are NaN
      if (this.isNaN() && rn.isNaN()) {
        checkRep();
        return true;
      } else {
        checkRep();
        return this.numer == rn.numer && this.denom == rn.denom;
      }
    } else {
      checkRep();
      return false;
    }
  }

  /** @return implementation specific debugging string. */
  public String debugPrint() {
    checkRep();
    return "RatNum<numer:" + this.numer + " denom:" + this.denom + ">";
  }

  // When debugging, or when interfacing with other programs that have
  // specific I/O requirements, you might change this.
  @Override
  public String toString() {
    checkRep();
    return debugPrint();
  }

  /** Makes a RatNum from a string describing it.
   * requires 'ratStr' is an instance of a string, with no spaces,
   * of the form: <UL>
   * <LI> "NaN"
   * <LI> "N/M", where N and M are both integers in
   * decimal notation, and M != 0, or
   * <LI> "N", where N is an integer in decimal
   * notation.
   * </UL>
   * @returns NaN if ratStr = "NaN".  Else returns a
   * RatNum r = ( N / M ), letting M be 1 in the case
   * where only "N" is passed in.
   */
  public static RatNum parse(String ratStr) {
    int slashLoc = ratStr.indexOf('/');
    if (ratStr.equals("NaN")) {
      return new RatNum(1, 0);
    } else if (slashLoc == -1) {
      // not NaN, and no slash, must be an Integer
      return new RatNum(Integer.parseInt(ratStr));
    } else {
      // slash, need to parse the two parts seperately
      int n = Integer.parseInt(ratStr.substring(0, slashLoc));
      int d = Integer.parseInt(ratStr.substring(slashLoc + 1, ratStr.length()));
      return new RatNum(n, d);
    }
  }
}
