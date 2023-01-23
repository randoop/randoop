package randoop.operation;

import randoop.reflection.ReflectionPredicate;
import randoop.types.Type;

/**
 * Operation represents the constructs that can occur in a statement as part of a test sequence.
 * These include method calls, constructor calls, field accesses, enum constant values, or primitive
 * values. They are used both as symbols in constructing statements as part of tests, and as a
 * computational action in reflective execution of test sequences.
 *
 * <p>The concept of an operation comes from logic and universal algebra, where operations are used
 * to build terms, which represent values. (To dive into the formality, see, e.g., the <a
 * href="https://en.wikipedia.org/wiki/Term_algebra">Term Algebra</a> Wikipedia entry.)
 *
 * <p>An operation op has a type-signature op: [T1, T2, ..., Tn] &rarr; T, where [T1, T2, ..., Tn]
 * is the list of input types, and T is the output type. The input types are represented by an
 * ordered list of {@link Type} objects, and the output type is a single {@link Type} object.
 *
 * <p>For a non-static method call or an instance field access, the first input type is always the
 * declaring class of the method or field. If we have a method {@code int A.m(double d)}, it is
 * represented as an operation m : [A, double] &rarr; int. A value, such as an int or enum constant,
 * can be represented as an operation with no input types, and its own type as the output type. So,
 * the number 5 is represented by the operation 5 : [] &rarr; int. Non-class values are represented
 * by {@link NonreceiverTerm} objects.
 *
 * <p>When an Operation is used in a statement the actual inputs have to be identified. Execution of
 * the statement will call {@link CallableOperation#execute(Object[])} with concrete values for each
 * of the inputs.
 *
 * <p>To support text-based serialization, an implementing class C should also provide:
 *
 * <ul>
 *   <li>A public static parse(String) method that returns a new Operation given a string
 *       description. The following property should hold: {@code
 *       C.parse(x.toParsableString()).equals(x)}
 *   <li>Update method OperationParser.parse(String) to parse operations of type C.
 * </ul>
 *
 * @see randoop.sequence.Statement
 * @see randoop.sequence.ExecutableSequence#execute(randoop.ExecutionVisitor,
 *     randoop.test.TestCheckGenerator)
 */
public interface Operation {

  /**
   * Predicate to indicate whether object represents a static operation on the declaring class.
   *
   * @return true if operation corresponds to static method or field of a class, and false otherwise
   */
  boolean isStatic();

  /**
   * Predicate to indicate whether object represents a method-call-like operation (either static or
   * instance). This includes field getters and setters, which are operations that access fields.
   *
   * @return true if operation is method-like, and false otherwise
   */
  boolean isMessage();

  /**
   * Indicates whether this object represents a method-call operation (either static or instance).
   * This excludes getters and setters.
   *
   * @return true if this operation is a method call, and false otherwise
   */
  boolean isMethodCall();

  /**
   * Predicate to indicate whether object represents a call to a constructor.
   *
   * @return true if operation is a constructor call, and false otherwise
   */
  boolean isConstructorCall();

  /**
   * Predicate to indicate whether this object represents a constant field.
   *
   * @return true if this operation is a constant field, and false otherwise
   */
  boolean isConstantField();

  /**
   * Predicate to indicate whether this object represents a value of a non-receiving type (includes
   * numbers, strings, and null).
   *
   * @return true if object is a non-receiving value, and false otherwise
   */
  boolean isNonreceivingValue();

  /**
   * Predicate to indicate whether this object represents an unchecked cast.
   *
   * @return true if the this object is a cast, and false otherwise
   */
  boolean isUncheckedCast();

  /**
   * Returns the "value" of an operation that is actually a ground term, meaning a constant of some
   * form. Only null if value is null, otherwise throws an exception if there is not a reasonable
   * meaning of value for type of operation.
   *
   * <p>This is a hack to allow randoop.main.GenBranchDir to do mutation.
   *
   * @return object reference to value
   * @throws IllegalArgumentException if no meaningful definition of "value" for type
   */
  Object getValue();

  /**
   * Determines whether the reflective object in this {@link Operation} satisfies the {@code canUse}
   * criteria of the given {@link ReflectionPredicate}.
   *
   * @param reflectionPredicate a {@link ReflectionPredicate} to be checked
   * @return result of applying reflectionPredicate to object
   */
  boolean satisfies(ReflectionPredicate reflectionPredicate);

  /**
   * Returns the name for the operation.
   *
   * @return the name for this operation
   */
  String getName();
}
