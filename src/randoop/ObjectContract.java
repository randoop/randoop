package randoop;

/**
 * An object contract is an expression that represents an expected behavior of
 * an object or a collection of objects. If the expression returns <code>true</code>,
 * the contract is said to succeed. If it returns a value other than <code>true</code>
 * it is said to fail. If the expression throws an exception during execution,
 * the result is undefined.
 * <p>
 * Randoop outputs sequences that lead to failing contracts as potentially
 * error-revealing test cases.
 * <p>
 * Object contracts are only evaluated on non-null objects.
 * <p>
 * For example, the <code>randoop.EqualsToNull</code> contract represents the
 * expression <code>!o.equals(null)</code>, which is expected to return
 * <code>true</code> and throw no exceptions.
 */
public interface ObjectContract extends Expression {

}
