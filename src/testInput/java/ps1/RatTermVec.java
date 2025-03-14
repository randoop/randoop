package ps1;

import java.util.Vector;

/** RatTermVec is a mutable, growable array of RatTerms.  Such arrays
 * are commonly referred to as <i>vectors</i>.  One can access
 * RatTerms in a RatTermVec using an integer index (starting at
 * zero).  The size of a RatTermVec grows (or shrinks) as needed to
 * accommodate the addition and removal of RatTerms.
 * <p>
 * The current state of a RatTermVec can be notated as a sequence:
 * [Ta, Tb, ...].  Examples of RatTermVecs include [] (an empty
 * vector), [Ta], [Ta, Tb], and [Ta, Tb, Ta], where Ta and Tb are
 * RatTerm objects.
 */
public class RatTermVec {
  // Definitions:
  // For a RatTermVec r, let T(r,i) be "r.terms.get(i)"

  // AF(r) = forall i=0 to r.terms.size()-1, [T(r,0), T(r,1), ..., T(r,i), ...]

  // RI(r) = r.terms != null && forall i=0 to r.terms.size()-1, T(r,i) is-a Term

  // CP: Daikon detects properties of the terms in this RatTermVec
  // more easily if this.terms is an array instaed. Daikon (or dfej)
  // should be changed so that this class doesn't have to be
  // modified, but for now, I'll modify it manually and make
  // this.terms an array. Since this is anyway a hack, I'll simplify
  // things by keeping the original vector as is, and as the last
  // step in each method that modifies the vector, creating a new
  // this.terms array that is a copy of the vector.

  public RatTerm[] terms;

  // CP: the field firstTermIdx didn't generate very useful invariants, and it
  // generates lots of garbage. May be interesting to use but for now, not.
  //public static int firstTermIdx = 0;
  //private int termsSize;
  public Vector underlying_terms;

  // CP: The best way to testclasses correctness is through equality: a
  // student's result is correct if its terms equal the terms
  // computed by the correct solution.
  /**
   * Equal iff both have the same number of RatTerms and those
   * RatTerms are pairwise equal.
   */
  public boolean equals(RatTermVec other) {
    if (other.size() != this.size()) {
      return false;
    }
    for (int i = 0; i < this.size(); i++) {
      if (!this.get(i).equals(other.get(i))) {
        return false;
      }
    }
    return true;
  }

  /** effects: constructs a new empty RatTermVec, []. */
  public RatTermVec() {
    underlying_terms = new Vector();
    terms =
        (RatTerm[])
            underlying_terms.toArray(new RatTerm[] {}); /*termsSize = underlying_terms.size();*/
  }

  /** @return the size of this RatTermVec. */
  public int size() {
    return underlying_terms.size();
  }

  /** Indexing operation.
   * requires: 0 <= index < this.size()
   * @return the RatTerm at the specified index.
   * <br>
   * e.g. Given a RatTermVec v = [t2, t3, t4], the expression
   * "v.get(1)" will return the RatTerm t3.
   */
  public RatTerm get(int index) {
    return (RatTerm) underlying_terms.get(index);
  }

  /** Appending operation.
   * requires: t != null
   * modifies: this
   * effects: Adds the specified RatTerm, 't', to the end of this
   * vector, increasing the vector's size by one.
   * <br>
   * e.g. Given a RatTermVec v = [t2, t3, t4], the statement
   * "v.addElement(t3);" will make v_post = [t2, t3, t4, t3].
   */
  public void addElement(RatTerm t) {
    underlying_terms.add(t);
    terms =
        (RatTerm[])
            underlying_terms.toArray(new RatTerm[] {}); /*termsSize = underlying_terms.size();*/
  }

  /** Insertion operation.
   * requires: t != null && 0 <= index <= this.size()
   * modifies: this
   * effects: Inserts 't' as a component in this RatTermVec at the
   * specified index. Each component in this vector with an index
   * greater or equal to the specified index is shifted upward to
   * have an index one greater than the value it had previously.
   * The size of this vector is increased by 1.
   * <br>
   * e.g. Given a RatTermVec v = [t2, t3, t4], the statement
   * "v.insert(t5, 1);" will make v_post = [t2, t5, t3, t4].
   */
  public void insert(RatTerm t, int index) {
    underlying_terms.add(index, t);
    terms =
        (RatTerm[])
            underlying_terms.toArray(new RatTerm[] {}); /*termsSize = underlying_terms.size();*/
  }

  /** Deletion operation.
   * requires: 0 <= index < this.size()
   * modifies: this
   * effects: Deletes the RatTerm at the specified index. Each
   * RatTerm in this vector with an index greater or equal to the
   * specified index is shifted downward to have an index one
   * smaller than the value it had previously. The size of this
   * vector is decreased by 1.
   * <br>
   * e.g. Given a RatTermVec v = [t2, t3, t4], the statement
   * "v.remove(1);" will make v_post = [t2, t4].
   */
  public void remove(int index) {
    underlying_terms.remove(index);
    terms =
        (RatTerm[])
            underlying_terms.toArray(new RatTerm[] {}); /*termsSize = underlying_terms.size();*/
  }

  /** Replacement operation.
   * requires: t != null && 0 < index < this.size()
   * modifies: this
   * effects: Sets the RatTerm at the 'index' of this vector to be
   * 't'. The previous RatTerm at 'index' is discarded.
   * <br>
   * e.g. Given a RatTermVec v = [t2, t3, t4], the statement
   * "v.set(t5, 1);" will make v_post = [t2, t5, t4].
   */
  public void set(RatTerm t, int index) {
    underlying_terms.setElementAt(t, index);
    terms =
        (RatTerm[])
            underlying_terms.toArray(new RatTerm[] {}); /*termsSize = underlying_terms.size();*/
  }

  /** Copy operation.
   * @return a new RatTermVec whose initial state matches that of
   * this RatTermVec.  Changes made to the state of the returned vector
   * will NOT be reflected in this vector, and vice versa.  (Also recall
   * that RatTerm objects are immutable.)
   */
  public RatTermVec copy() {
    RatTermVec tv = new RatTermVec();
    tv.underlying_terms = (Vector) this.underlying_terms.clone();

    tv.terms =
        (RatTerm[])
            tv.underlying_terms.toArray(
                new RatTerm[] {}); /*tv.termsSize = tv.underlying_terms.size();*/

    return tv;
  }

  /** @return implementation specific debugging string. */
  public String printDebug() {
    return "RatTermVec<underlying_terms:" + this.underlying_terms + ">";
  }

  @Override
  public String toString() {
    return printDebug();
  }
}
