package randoop.operation;

import java.io.PrintStream;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Variable;

/**
 * Operation represents the constructs that can occur in a statement as part of a test sequence. 
 * These include method calls, constructor calls, field accesses, enum constant values, or 
 * primitive values. They are used both as symbols in constructing statements as part of tests, 
 * and as a computational action in reflective execution of test sequences.
 * 
 * The concept of an operation comes from logic and universal algebra, where operations are 
 * used to build terms, which represent values. 
 * (To dive into the formality, see, e.g., the <a href="https://en.wikipedia.org/wiki/Term_algebra>Term Algebra</a> Wikipedia entry.)
 * 
 * An operation op has a type-signature 
 * op: [T1, T2, ..., Tn] -> T, where [T1, T2, ..., Tn] is the list of input types, and 
 * T is the output type. The input types are represented by an ordered list of 
 * {@link Class} objects, and the output type is a single {@link Class} object. 
 *  
 * For a non-static method call or instance field access, the first input type
 * is always the class to which the method or field belongs. If we have a method 
 * <code>int A.m(double d)</code>, it is represented as an operation m : [A, double] -> int. 
 * A value, such as an int or enum constant, can be represented as an operation with no input 
 * types, and its own type as the output type. So, the number 5 is represented by the operation 
 * 5 : [] -> int. Non-class values are represented by {@link NonreceiverTerm} objects.
 * 
 * When an Operation is used in a statement the actual inputs have to be identified. Execution of the
 * statement will call {@link Operation#execute(Object[], PrintStream)} with concrete values for each
 * of the inputs.
 * @see Statement
 * @see randoop.sequence.ExecutableSequence#execute(randoop.ExecutionVisitor)
 * 
 * To support text-based serialization, an implementing class C should also provide:
 * 
 *  <ul>
 *  <li> A public static String field that contains a unique ID for the statement kind.
 *  <li> A public static parse(String) method that returns a new Operation given
 *       a string description. The following property should hold:
 *         C.parse(x.toParseableString()).equals(x)
 *  <li> Update method Operations.parse(String) to parse statement kinds of type C.
 *  <li> Update method OperationParser.getId(Operation) to handle statement kinds of type C.
 *  </ul> 
 */
public interface Operation extends Comparable<Operation> {

  /**
   * getInputTypes returns the ordered list of input types for this operation.
   * If a method call or field access, the first input corresponds to the
   * receiver, which must be an object of the declaring class.
   * 
   * @return list of types as {@link Class} objects.
   */
  List<Class<?>> getInputTypes();

  /**
   * getOutputTypes gives the type returned by the operation.
   * 
   * @return type returned by the {@link Operation} as a {@link Class} object.
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
   * toParseableString returns a string representation of this Operation, which can
   * subsequently be used in this class's parse method. For a class C implementing the 
   * Operation interface, this method should return a String s such that parsing the string 
   * returns an object equivalent to this object, i.e. C.parse(this.s).equals(this).
   * 
   * @return string descriptor of {@link Operation} object.
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

  /**
   * satisfies determines whether the reflective object in this {@link Operation} satisfies
   * the <code>canUse</code> criteria of the given {@link ReflectionPredicate}.
   * 
   * @param reflectionPredicate a {@link ReflectionPredicate} to be checked.
   * @return result of applying reflectionPredicate to object.
   */
  boolean satisfies(ReflectionPredicate reflectionPredicate);


}
