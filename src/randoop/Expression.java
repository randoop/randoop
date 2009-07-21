package randoop;

/**
 * Expressions are used in Randoop to record observations about the execution
 * of a statement sequence. See randoop.Observation.
 *
 * A randoop Expression is much like a regular Java expression: it denotes some
 * computation that produces a value. Typically, the expression is over a
 * collection of values. For example, the expression "o1.equals(o2)" is an
 * expression over the values o1 and o2 and produces a boolean value.
 *
 * An implementing class should ensure that:
 *
 * <ul>
 * <li> A call to evaluate() returns null, a primitive value, or a String.
 * <li> The implementing class declares a constructor with zero parameters.
 * </ul>
 */
public interface Expression {

  /**
   * The number of objects that this expression is over.
   */
  int getArity();

  /**
   * Evaluates the expression on the given objects.
   */
  Object evaluate(Object... objects);

  /**
   * A human-readable string representing this expression. The string must span
   * a single line.
   *
   * The comment should be formatted as follows: the N-th object that
   * participates in the expression should be referred to as "xN" (for N one of
   * 0, ... , 9). For example, if the expression or arity 2 represents a call of
   * the equals method between two objects, the comment should be something like
   * "x0.equals(x1)".
   *
   * When printing out the comments in code, the xN's are replaced with actual
   * variable names appearing in the sequence.
   */
  String toCommentString();

  /**
   * A string that can be used as Java source code and will result in the
   * expression being evaluated.
   *
   * The method can return null, in which case a default representatin of the
   * expression will be used, which looks something like "new
   * randoop.EXP().evaluate(VARS)" where EXP is the name of the expression
   * class, and VARS is the list of variables that participate in the
   * expression.
   *
   * The returned string should be formatted similarly to method
   * toCommentString(), with "xN" standing for the N-th variable.
   */
  String toCodeString();

}
