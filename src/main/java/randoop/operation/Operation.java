package randoop.operation;

import java.io.PrintStream;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Variable;

/**
 * Operation represents the constructs that can occur in a statement as part of
 * a test sequence. These include method calls, constructor calls, field
 * accesses, enum constant values, or primitive values. They are used both as
 * symbols in constructing statements as part of tests, and as a computational
 * action in reflective execution of test sequences.
 * <p>
 * The concept of an operation comes from logic and universal algebra, where
 * operations are used to build terms, which represent values. (To dive into the
 * formality, see, e.g., the
 * <a href="https://en.wikipedia.org/wiki/Term_algebra">Term Algebra</a>
 * Wikipedia entry.)
 * <p>
 * An operation op has a type-signature op: [T1, T2, ..., Tn] &rarr; T, where
 * [T1, T2, ..., Tn] is the list of input types, and T is the output type. The
 * input types are represented by an ordered list of {@link Class} objects, and
 * the output type is a single {@link Class} object.
 * <p>
 * For a non-static method call or instance field access, the first input type
 * is always the declaring class of the method or field. If we have a method
 * <code>int A.m(double d)</code>, it is represented as an operation m : [A,
 * double] &rarr; int. A value, such as an int or enum constant, can be
 * represented as an operation with no input types, and its own type as the
 * output type. So, the number 5 is represented by the operation 5 : [] &rarr;
 * int. Non-class values are represented by {@link NonreceiverTerm} objects.
 * <p>
 * When an Operation is used in a statement the actual inputs have to be
 * identified. Execution of the statement will call
 * {@link Operation#execute(Object[], PrintStream)} with concrete values for
 * each of the inputs.
 *
 * @see randoop.sequence.Statement
 * @see randoop.sequence.ExecutableSequence#execute(randoop.ExecutionVisitor,
 *      randoop.test.TestCheckGenerator)
 *      <p>
 *      To support text-based serialization, an implementing class C should also
 *      provide:
 *      <ul>
 *      <li>A public static String field that contains a unique ID for the
 *      operation.
 *      <li>A public static parse(String) method that returns a new Operation
 *      given a string description. The following property should hold:
 *      <code>C.parse(x.toParseableString()).equals(x)</code>
 *      <li>Update method OperationParser.parse(String) to parse operations of
 *      type C.
 *      <li>Update method OperationParser.getId(Operation) to handle operations
 *      of type C.
 *      </ul>
 */
public interface Operation extends Comparable<Operation> {

  /**
   * Returns the ordered list of input types for this operation. If a method
   * call or field access, the first input corresponds to the receiver, which
   * must be an object of the declaring class.
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
   * Performs this operation using the array of input values. Returns the
   * results of execution as an ResultOrException object and can output results
   * to specified PrintStream.
   *
   * @param input
   *          array containing appropriate inputs to operation
   * @param out
   *          stream to output results of execution; can be null if you don't
   *          want to print.
   * @return results of executing this statement
   */
  ExecutionOutcome execute(Object[] input, PrintStream out);

  /**
   * Produces a Java source code representation of this statement and append it
   * to the given StringBuilder.
   *
   * @param inputVars
   *          the list of variables that are inputs to operation.
   * @param b
   *          the {@link StringBuilder} to which code is added.
   */
  void appendCode(List<Variable> inputVars, StringBuilder b);

  /**
   * Returns a string representation of this Operation, which can be read by
   * static parse method for class. For a class C implementing the Operation
   * interface, this method should return a String s such that parsing the
   * string returns an object equivalent to this object, i.e.
   * C.parse(this.s).equals(this).
   *
   * @return string descriptor of {@link Operation} object.
   */
  String toParseableString();

  /**
   * Predicate to indicate whether object represents a static operation on the
   * declaring class.
   *
   * @return true if operation corresponds to static method or field of a class,
   *         and false otherwise.
   */
  boolean isStatic();

  /**
   * Predicate to indicate whether object represents a method-call-like
   * operation (either static or instance). This includes field getters and
   * setters, which are operations that access fields.
   *
   * @return true if operation is method-like, and false otherwise.
   */
  boolean isMessage();

  /**
   * Returns the class in which the operation is defined, or, if the operation
   * represents a value, the type of the value.
   *
   * @return class to which the operation belongs.
   */
  Class<?> getDeclaringClass();

  /**
   * Predicate to indicate whether object represents a call to a constructor.
   *
   * @return true if operation is a constructor call, and false otherwise.
   */
  boolean isConstructorCall();

  /**
   * Predicate to indicate whether this object represents a value of a
   * non-receiving type (includes numbers, strings, and null).
   *
   * @return true if object is a non-receiving value, and false otherwise.
   */
  boolean isNonreceivingValue();

  /**
   * Returns the "value" of an operation that is actually a ground term, meaning
   * a constant of some form. Only null if value is null, otherwise throws an
   * exception if there is not a reasonable meaning of value for type of
   * operation.
   * <p>
   * This is a hack to allow randoop.main.GenBranchDir to do mutation.
   *
   * @return object reference to value.
   * @throws IllegalArgumentException
   *           if no meaningful definition of "value" for type.
   */
  Object getValue();

  /**
   * Determines whether the reflective object in this {@link Operation}
   * satisfies the <code>canUse</code> criteria of the given
   * {@link ReflectionPredicate}.
   *
   * @param reflectionPredicate
   *          a {@link ReflectionPredicate} to be checked.
   * @return result of applying reflectionPredicate to object.
   */
  boolean satisfies(ReflectionPredicate reflectionPredicate);
}
