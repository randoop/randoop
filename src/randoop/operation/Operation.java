package randoop.operation;

import java.io.PrintStream;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.sequence.Variable;

/**
 * Represents an operation (in the term algebra sense) that can occur in a statement 
 * as part of a test sequence. Operations include method calls, constructor calls, field
 * accesses, enum constant values, or primitive values.
 * 
 * To support text-based serialization, an implementing class C should also provide:
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
public interface Operation {

  /**
   * getInputTypes returns the ordered list of input types for this operation.
   * If a method call or field access, the first input corresponds to the
   * receiver, which must be an object of the declaring class.
   */
  List<Class<?>> getInputTypes();

  /**
   * getOutputTypes gives the type returned by the operation.
   */
  Class<?> getOutputType();

  /**
   * execute performs this operation using the array of input values. Returns
   * the results of execution as an ResultOrException object and can
   * output results to specified PrintStream.
   * @param input array containing appropriate inputs to operation
   * @param out stream to output results of execution;
   *            can be null if you don't want to print.
   * @return results of executing this statement
   */
  ExecutionOutcome execute(Object[] input, PrintStream out);

  /**
   * Produces a Java source code representation of this statement and append it
   * to the given StringBuilder.
   */
  void appendCode(List<Variable> inputVars, StringBuilder b);

  /**
   * Returns a string representation of this StatementKind, which can
   * subsequently be used in this class's parse method. For a class C
   * implementing the StatementKind interface, this method should return a
   * String s such that parsing the string returns an object equivalent to
   * this object, i.e. C.parse(this.s).equals(this).
   */
  String toParseableString();

  /**
   * isStatic is a predicate to indicate whether object represents a static
   * operation on the declaring class.
   * 
   * @return true if operation corresponds to static method or field of a class, and false, otherwise.
   */
  boolean isStatic();

  /**
   * isMessage is a predicate to indicate whether object represents a method-call-like
   * operation (either static or instance). This include non-method operations that access 
   * fields.
   * 
   * @return true if operation is method-like, and false otherwise.
   */
  boolean isMessage();

  /**
   * getDeclaringClass returns the type to which the operation belongs. If a constructor
   * or value, then the type should be the same as the output type.
   * 
   * @return class to which the operation belongs.
   */
  Class<?> getDeclaringClass();

  /**
   * isConstructorCall is a predicate to indicate whether object represents a call to a constructor.
   * 
   * @return true if operation is a constructor call, and false otherwise.
   */
  boolean isConstructorCall();

  /**
   * isNonreceivingValue is a predicate to indicate whether this object represents a value of a
   * non-receiving type (includes numbers, strings and null).
   * 
   * @return true if object is a non-receiving value, and false, otherwise.
   */
  boolean isNonreceivingValue();

  /**
   * getValue returns the "value" of an operation that is actuall a ground term, meaning
   * a constant of some form.  Only null if value is null, otherwise throws an exception
   * if not a reasonable meaning of value for type of operation.
   * 
   * This is a hack to allow randoop.main.GenBranchDir to do mutation.
   * 
   * @return object reference to value.
   * @throws IllegalArgumentException if no meaningful definition of "value" for type.
   */
  Object getValue();


}
