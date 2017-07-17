package ps1;

import java.util.StringTokenizer;

/** RatPoly represents an immutable single-variate polynomial
 * expression.  RatPolys have RatNum coefficients and integer
 * exponents.
 * <p>
 *
 * Examples of RatPolys include "0", "x-10", and "x^3-2*x^2+5/3*x+3",
 * and "NaN".
 */
public class RatPoly {
  // holds terms of this
  public RatTermVec terms;

  public final static RatNum ZERO = new RatNum(0);
  public final static RatNum ONE = new RatNum(1);

  // BEGIN CP: modified to get rid of the static { } code below, which
  // interacts badly with jmlc refinement files.

  //private final static RatPoly NAN;
  private final static RatPoly NAN = new RatPoly(new RatTerm(new RatNum(1, 0), 0));

  //   static {
  //     RatTermVec vec = new RatTermVec();
  //     vec.addElement(new RatTerm(new RatNum(1,0), 0));
  //     NAN = new RatPoly(vec);
  //   }

  // END CP

  // Definitions:
  //   For a RatPoly p, let C(p,i) be "p.terms.get(i).coeff" and
  //                        E(p,i) be "p.terms.get(i).expt"
  //                        length(p) be "p.terms.size()"
  // (These are helper functions that will make it easier for us
  // to write the remainder of the specifications.  They are not
  // executable code; they just represent complex expressions in a
  // concise manner, so that we can stress the important parts of
  // other expressions in the spec rather than get bogged down in
  // the details of how we extract the coefficient for the 2nd term
  // or the exponent for the 5th term.  So when you see C(p,i),
  // think "coefficient for the ith term in p".)

  // Abstraction Function:
  //   A RatPoly p is the Sum, from i=0 to length(p), of C(p,i)*x^E(p,i)
  // (This explains what the state of the fields in a RatPoly
  // represents: it is the sum of a series of terms, forming an
  // expression like "C_0 + C_1*x^1 + C_2*x^2 + ...".  If there are no
  // terms, then the RatPoly represents the zero polynomial.)

  // Representation Invariant for every RatPoly p:
  //   terms != null &&
  //   forall i such that (0 <= i < length(p), C(p,i) != 0 &&
  //   forall i such that (0 <= i < length(p), E(p,i) >= 0 &&
  //   forall i such that (0 <= i < length(p) - 1), E(p,i) > E(p, i+1)
  // (This tells us four important facts about every RatPoly:
  //   * the terms field always points to some usable object,
  //   * no term in a RatPoly has a zero coefficient,
  //   * no term in a RatPoly has a negative exponent, and
  //   * the terms in a RatPoly are sorted in descending exponent order.)

  /** effects: Constructs a new Poly, "0".
   */
  public RatPoly() {
    terms = new RatTermVec();
  }

  /** requires: e >= 0
   * effects: Constructs a new Poly equal to "c * x^e".
   * If c is zero, constructs a "0" polynomial.
   */
  public RatPoly(int c, int e) {
    this();
    if (c != 0) {
      terms.addElement(new RatTerm(new RatNum(c), e));
    }
  }

  /** requires: 'rt' satisfies clauses given in rep. invariant
   * effects: Constructs a new Poly using 'rt' (does not make a copy)
   */
  private RatPoly(RatTermVec rt) {
    terms = rt;
  }

  private RatPoly(RatTerm rt) {
    terms = new RatTermVec();
    terms.addElement(rt);
  }

  /** Returns the degree of this.
   * requires: !this.isNaN()
   * @return the largest exponent with a non-zero coefficient, or 0
   * if this is "0"
   */
  public int degree() {
    if (terms.size() > 0) {
      return terms.get(0).expt;
    } else {
      return 0;
    }
  }

  /** requires: !this.isNaN()
   * @return the coefficient associated with term of degree 'deg'.
   * If there is no term of degree 'deg' in this poly, then returns
   * zero.
   */
  public RatNum coeff(int deg) {
    for (int i = 0, size = terms.size(); i < size; i++) {
      if (terms.get(i).expt == deg) {
        return terms.get(i).coeff;
      }
    }
    return ZERO;
  }

  /** @return true if and only if this has some coefficient = "NaN"
   */
  public boolean isNaN() {
    if (terms == null) throw new RuntimeException("terms is null");
    for (int i = 0, size = terms.size(); i < size; i++) {
      if (terms.get(i).coeff.isNaN()) {
        return true;
      }
    }
    return false;
  }

  /** Returns a string representation of this.
   * @return a String representation of the expression represented
   * by this, with the terms sorted in order of degree from highest
   * to lowest.
   * <p>
   * There is no whitespace in the returned string.
   * <p>
   * Terms with zero coefficients do not appear in the returned
   * string, unless the polynomial is itself zero, in which case
   * the returned string will just be "0".
   * <p>
   * If this.isNaN(), then the returned string will be just "NaN"
   * <p>
   * The string for a non-zero, non-NaN poly is in the form
   * "(-)T(+|-)T(+|-)...",where for each
   * term, T takes the form "C*x^E" or "C*x", UNLESS:
   * (1) the exponent E is zero, in which case T takes the form "C", or
   * (2) the coefficient C is one, in which case T takes the form
   * "x^E" or "x"
   * <p>
   * Note that this format allows only the first term to be output
   * as a negative expression.
   * <p>
   * Valid example outputs include "x^17-3/2*x^2+1", "-x+1", "-1/2",
   * and "0".
   * <p>
   */
  public String unparse() {
    //NaN case
    if (this.isNaN()) {
      return "NaN";
    }

    //zero polynomial case
    if (terms.size() == 0) {
      return "0";
    }

    StringBuilder sb = new StringBuilder();
    boolean isNegative;
    for (int i = 0, size = terms.size(); i < size; i++) {
      isNegative = terms.get(i).coeff.isNegative();

      RatNum coeff = terms.get(i).coeff;
      if (isNegative) {
        coeff = coeff.negate();
      }

      if (isNegative
          && coeff.numer != Integer.MIN_VALUE /* negating min_val gives back min_val! */) {
        sb.append("-");
      } else if (i != 0 && !isNegative) {
        sb.append("+");
      }

      if (terms.get(i).expt == 0) {
        sb.append(coeff.unparse());
      } else {
        if (terms.get(i).expt == 1) {
          if (coeff.equals(ONE)) {
            sb.append("x");
          } else {
            sb.append((coeff.unparse() + "*x"));
          }

        } else {
          if (coeff.equals(ONE)) {
            sb.append(("x^" + terms.get(i).expt));
          } else {
            sb.append((coeff.unparse() + "*x^" + terms.get(i).expt));
          }
        }
      }
    }
    return sb.toString();
  }

  /** Scales coefficients within 'vec' by 'scalar' (helper procedure).
   * requires: vec, scalar != null
   * modifies: vec
   * effects: Forall i s.t. 0 <= i < vec.size(),
   * if (C . E) = vec.get(i)
   * then vec_post.get(i) = (C*scalar . E)
   * @see ps1.RatTerm regarding (C . E) notation
   */
  private static void scaleCoeff(RatTermVec vec, RatNum scalar) {
    for (int i = 0, size = vec.size(); i < size; i++) {
      vec.set(new RatTerm(vec.get(i).coeff.mul(scalar), vec.get(i).expt), i);
    }
  }

  /** Increments exponents within 'vec' by 'degree' (helper procedure).
   * requires: vec != null
   * modifies: vec
   * effects: Forall i s.t. 0 <= i < vec.size(),
   * if (C . E) = vec.get(i)
   * then vec_post.get(i) = (C . E+degree)
   * @see ps1.RatTerm regarding (C . E) notation
   */
  private static void incremExpt(RatTermVec vec, int degree) {
    for (int i = 0, size = vec.size(); i < size; i++) {
      vec.set(new RatTerm(vec.get(i).coeff, vec.get(i).expt + degree), i);
    }
  }

  /** Merges a term into a sequence of terms, preserving the
   * sorted nature of the sequence (helper procedure).
   *
   * Definitions:
   * Let a "Sorted RatTermVec" be a RatTermVec V such that
   * [1] V is sorted in descending exponent order &&
   * [2] there are no two RatTerms with the same exponent in V &&
   * [3] there is no RatTerm in V with a coefficient equal to zero
   *
   * For a Sorted(RatTermVec) V and integer e, let cofind(V, e)
   * be either the coefficient for a RatTerm rt in V whose
   * exponent is e, or zero if there does not exist any such
   * RatTerm in V.  (This is like the coeff function of RatPoly.)
   *
   * requires: vec != null && sorted(vec)
   * modifies: vec
   * effects: sorted(vec_post) &&
   * cofind(vec_post,newTerm.expt)
   * = cofind(vec,newTerm.expt) + newTerm.coeff
   */
  private static void sortedAdd(RatTermVec vec, RatTerm newTerm) {
    int deg = newTerm.expt;
    for (int i = 0, size = vec.size(); i < size; i++) {
      if (vec.get(i).expt == deg) {
        if (vec.get(i).coeff.add(newTerm.coeff).equals(ZERO)) {
          vec.remove(i);
          return;
        } else {
          vec.set(new RatTerm(vec.get(i).coeff.add(newTerm.coeff), deg), i);
          return;
        }
      }
      if (vec.get(i).expt < deg) {
        vec.insert(newTerm, i);
        return;
      }
    }
    vec.addElement(newTerm);
    return;
  }

  /** @return a new Poly equal to "0 - this";
   * if this.isNaN(), returns some r such that r.isNaN()
   */
  public RatPoly negate() {
    RatPoly result = new RatPoly(terms.copy());
    scaleCoeff(result.terms, new RatNum(-1));
    return result;
  }

  /** Addition operation.
   * requires: p != null
   * @return a new RatPoly, r, such that r = "this + p";
   * if this.isNaN() or p.isNaN(), returns some r such that r.isNaN()
   */
  public RatPoly add(RatPoly p) {
    if (p.isNaN() || this.isNaN()) {
      return NAN;
    }
    RatPoly result = new RatPoly(terms.copy());
    for (int i = 0, size = p.terms.size(); i < size; i++) {
      sortedAdd(result.terms, p.terms.get(i));
    }
    return result;
  }

  /** Subtraction operation.
   * requires: p != null
   * @return a new RatPoly, r, such that r = "this - p";
   * if this.isNaN() or p.isNaN(), returns some r such that r.isNaN()
   */
  public RatPoly sub(RatPoly p) {
    if (p.isNaN() || this.isNaN()) {
      return NAN;
    }
    RatPoly result = new RatPoly(terms.copy());
    for (int i = 0, size = p.terms.size(); i < size; i++) {
      sortedAdd(result.terms, new RatTerm(p.terms.get(i).coeff.negate(), p.terms.get(i).expt));
    }
    return result;
  }

  /** Multiplication operation.
   * requires: p != null
   * @return a new RatPoly, r, such that r = "this * p";
   * if this.isNaN() or p.isNaN(), returns some r such that r.isNaN()
   */
  public RatPoly mul(RatPoly p) {
    if (p.isNaN() || this.isNaN()) {
      return NAN;
    }
    RatPoly result = new RatPoly();
    for (int i = 0, size = this.terms.size(); i < size; i++) {
      for (int j = 0, psize = p.terms.size(); j < psize; j++) {
        if (this.terms.get(i).coeff.equals(ZERO) || p.terms.get(j).coeff.equals(ZERO)) {
          throw new RuntimeException("REP BROKEN");
        }
        sortedAdd(
            result.terms,
            new RatTerm(
                this.terms.get(i).coeff.mul(p.terms.get(j).coeff),
                this.terms.get(i).expt + p.terms.get(j).expt));
      }
    }
    return result;
  }

  /** Division operation (truncating).
   * requires: p != null
   * @return a new RatPoly, q, such that q = "this / p";
   * if p = 0 or this.isNaN() or p.isNaN(),
   * returns some q such that q.isNaN()
   * <p>
   *
   * Division of polynomials is defined as follows:
   * Given two polynomials u and v, with v != "0", we can divide u by
   * v to obtain a quotient polynomial q and a remainder polynomial
   * r satisfying the condition u = "q * v + r", where
   * the degree of r is strictly less than the degree of v,
   * the degree of q is no greater than the degree of u, and
   * r and q have no negative exponents.
   * <p>
   *
   * For the purposes of this class, the operation "u / v" returns
   * q as defined above.
   * <p>
   *
   * Thus, "x^3-2*x+3" / "3*x^2" = "1/3*x" (with the corresponding
   * r = "-2*x+3"), and "x^2+2*x+15 / 2*x^3" = "0" (with the
   * corresponding r = "x^2+2*x+15").
   * <p>
   *
   * Note that this truncating behavior is similar to the behavior
   * of integer division on computers.
   */
  public RatPoly div(RatPoly p) {
    if (p.isNaN() || this.isNaN() || p.terms.size() == 0) {
      return NAN;
    }

    RatPoly result = new RatPoly();
    RatPoly thisCopy = new RatPoly(this.terms.copy());

    RatTerm tempTerm;
    while (thisCopy.degree() >= p.degree() && thisCopy.terms.size() > 0) {

      tempTerm =
          new RatTerm(
              thisCopy.terms.get(0).coeff.div(p.terms.get(0).coeff),
              thisCopy.terms.get(0).expt - p.terms.get(0).expt);
      sortedAdd(result.terms, tempTerm);
      thisCopy = thisCopy.sub(p.mul(new RatPoly(tempTerm)));
    }

    return result;
  }

  /** @return the value of this polynomial when evaluated at 'd'.
   * For example, "x+2" evaluated at 3 is 5, and "x^2-x"
   * evaluated at 3 is 6.  Should always return
   * Double.NaN if this.isNaN() == true.
   */

  /**
   * @return a new RatPoly that, q, such that q = dy/dx,
   *         where this == y.  In other words, q is the
   *         derivative of this.  If this.isNaN(), then return
   *         some q such that q.isNaN()
   *
   *         <p>The derivative of a polynomial is the sum of the
   *         derivative of each term.
   *
   *         Given a term, a*x^b,
   *         the derivativee of the term is:  (a*b)*x^(b-1)  for b &gt; 0
   *                                            0         for b == 0
   */
  public RatPoly differentiate() {
    if (this.isNaN()) {
      return NAN;
    }

    RatPoly result = new RatPoly();
    for (int i = 0, size = this.terms.size(); i < size; i++) {
      if (terms.get(i).expt > 0) {
        sortedAdd(
            result.terms,
            new RatTerm(
                terms.get(i).coeff.mul(new RatNum(terms.get(i).expt)), this.terms.get(i).expt - 1));
      }
    }
    return result;
  }

  /**
   * requires integrationConstant != null
   * @return a new RatPoly that, q, such that dq/dx = this
   *         and the constant of integration is "integrationConstant"
   *         In other words, q is the antiderivative of this.
   *         If this.isNaN() or integrationConstant.isNaN(),
   *         then return some q such that q.isNaN()
   *
   *         <p>The antiderivative of a polynomial is the sum of the
   *         antiderivative of each term plus some constant.
   *
   *         Given a term, a*x^b,  (where b &ge; 0)
   *         the antiderivativee of the term is:  a/(b+1)*x^(b+1)
   */
  public RatPoly antiDifferentiate(RatNum integrationConstant) {
    if (integrationConstant.isNaN() || this.isNaN()) {
      return NAN;
    }

    RatPoly result = new RatPoly();
    if (!integrationConstant.equals(ZERO)) {
      result.terms.addElement(new RatTerm(integrationConstant, 0));
    }
    for (int i = 0, size = this.terms.size(); i < size; i++) {
      sortedAdd(
          result.terms,
          new RatTerm(
              terms.get(i).coeff.div(new RatNum(terms.get(i).expt + 1)),
              this.terms.get(i).expt + 1));
    }
    return result;
  }

  /**
   * @return a double that is the definite integral of this with
   * bounds of integration between lowerBound and upperBound.
   *
   *<p>
   * The Fundamental Theorem of Calculus states that definite integral
   * of f(x) with bounds a to b is F(b) - F(a) where dF/dx = f(x)
   * NOTE: Remember that the lowerBound can be higher than the upperBound
   */
  public double integrate(double lowerBound, double upperBound) {
    return antiDifferentiate(ZERO).eval(upperBound) - antiDifferentiate(ZERO).eval(lowerBound);
  }

  public double eval(double d) {
    double sum = 0.0;
    for (int i = 0, size = terms.size(); i < size; i++) {
      sum += Math.pow(d, terms.get(i).expt) * terms.get(i).coeff.approx();
    }
    return sum;
  }

  /** requires: 'polyStr' is an instance of a string with no spaces
   * that expresses a poly in the form defined in the
   * unparse() method.
   * @return a RatPoly p such that p.unparse() = polyStr
   */
  public static RatPoly parse(String polyStr) {
    RatPoly result = new RatPoly();

    // First we decompose the polyStr into its component terms;
    // third arg orders "+" and "-" to be returned as tokens.
    StringTokenizer termStrings = new StringTokenizer(polyStr, "+-", true);

    boolean nextTermIsNegative = false;
    while (termStrings.hasMoreTokens()) {
      String termToken = termStrings.nextToken();

      if (termToken.equals("-")) {
        nextTermIsNegative = true;
      } else if (termToken.equals("+")) {
        nextTermIsNegative = false;
      } else {
        // Not "+" or "-"; must be a term

        // Term is: "R" or "R*x" or "R*x^N" or "x^N" or "x",
        // where R is a rational num and N is a natural num.

        // Decompose the term into its component parts.
        // third arg orders '*' and '^' to act purely as delimiters.
        StringTokenizer numberStrings = new StringTokenizer(termToken, "*^", false);

        RatNum coeff;
        int expt;

        String c1 = numberStrings.nextToken();
        if (c1.equals("x")) {
          // ==> "x" or "x^N"
          coeff = new RatNum(1);

          if (numberStrings.hasMoreTokens()) {
            // ==> "x^N"
            String N = numberStrings.nextToken();
            expt = Integer.parseInt(N);

          } else {
            // ==> "x"
            expt = 1;
          }
        } else {
          // ==> "R" or "R*x" or "R*x^N"
          String R = c1;
          coeff = RatNum.parse(R);

          if (numberStrings.hasMoreTokens()) {
            // ==> "R*x" or "R*x^N"
            String x = numberStrings.nextToken();

            if (numberStrings.hasMoreTokens()) {
              // ==> "R*x^N"
              String N = numberStrings.nextToken();
              expt = Integer.parseInt(N);
            } else {
              // ==> "R*x"
              expt = 1;
            }

          } else {
            // ==> "R"
            expt = 0;
          }
        }

        // at this point, coeff and expt are initialized.
        // Need to fix coeff if it was preceeded by a '-'
        if (nextTermIsNegative) {
          coeff = coeff.negate();
        }

        // accumulate terms of polynomial in 'result'
        if (!coeff.equals(new RatNum(0))) {
          RatPoly termPoly = new RatPoly();
          termPoly.terms.addElement(new RatTerm(coeff, expt));
          result = result.add(termPoly);
        }
      }
    }
    return result;
  }
}
