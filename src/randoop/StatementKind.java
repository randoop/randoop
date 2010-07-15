package randoop;

import java.io.PrintStream;
import java.util.List;

/**
 * Represents a kind of statement that can be part of a sequence, for example, a
 * method call, constructor call, or primitive value declaration.
 * </p>
 * 
 * An implementing class C should additionally provide the following:
 * 
 *  <ul>
 *  <li> A public static String field that contains a unique ID for the statement kind.
 *  <li> A public static parse(String) method that returns a new StatementKind given
 *       a string description. The following property should hold:
 *         C.parse(x.toParseableString()).equals(x)
 *  <li> Update method StatementKinds.parse(String) to parse statement kinds of type C.
 *  <li> Update method StatementKinds.getId(StatementKing) to handle statement kinds of type C.
 *  </ul> 
 */
public interface StatementKind {

  /**
   * Returns the (ordered) list of input types for this statement kind.
   */
  List<Class<?>> getInputTypes();

  /**
   * Returns the type of this statement kind's output.
   */
  Class<?> getOutputType();


  /**
   * Executes this statement, given the inputs to the statement. Returns
   * the results of execution as an ResultOrException object and can
   * output results to specified PrintStream.
   * @param statementInput array containing appropriate inputs to statement
   * @param out stream to output results of execution;
   *            can be null if you don't want to print.
   * @return results of executing this statement
   */
  ExecutionOutcome execute(Object[] statementInput, PrintStream out);


  /**
   * Produces a Java source code representation of this statement and append it
   * to the given StringBuilder.
   */
  void appendCode(Variable newVar, List<Variable> inputVars, StringBuilder b);

  /**
   * Returns a string representation of this StatementKind, which can
   * subsequently be used in this class's parse method. For a class C
   * implementing the StatementKind interface, this method should return a
   * String s such that parsing the string returns an object equivalent to
   * this object, i.e. C.parse(this.s).equals(this).
   */
  String toParseableString();

}
