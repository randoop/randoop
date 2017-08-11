/**
 * Package containing classes for handling specifications on operations. The specifications for an
 * operation are represented by an {@link randoop.condition.specification.OperationSpecification}
 * object that contains lists of pre-, post-, or throws-specifications. The specifications are read
 * from JSON files that serialize the specification classes using {@link
 * randoop.condition.SpecificationCollection#create(java.util.List)}. The conditions for an
 * operation are represented as an {@link randoop.condition.OperationConditions} object that is
 * created using {@link
 * randoop.condition.SpecificationCollection#getOperationConditions(java.lang.reflect.AccessibleObject)}.
 *
 * <p>The operations are used in the {@link
 * randoop.sequence.ExecutableSequence#execute(randoop.ExecutionVisitor,
 * randoop.test.TestCheckGenerator, boolean)} method that implements the condition evaluation
 * algorithm described below.
 *
 * <h3 id="eval-algorithm">Condition Evaluation Algorithm</h3>
 *
 * <p><i>Input</i>: a method, the set of specifications for the method, and arguments for a call to
 * the method.
 *
 * <p><i>Goal</i>: classify the call to the method using the arguments as <b>Expected</b>,
 * <b>Invalid</b> or <b>Error-revealing</b> based on the specifications.
 *
 * <p><i>Definitions</i>: A precondition, guard, or property evaluated on the method arguments is
 * <i>satisfied</i> if the underlying Boolean expression evaluates to true, and <i>fails</i>
 * otherwise.
 *
 * <p><i>Description</i>: The algorithm consists of two phases: (1) evaluating guards of the
 * specifications before the call, and (2) checking for expected behavior after the call.
 * Specifically, the first phase saves the results of the evaluation in a {@link
 * randoop.condition.PreconditionOutcomeTable} table that is used in the second phase.
 *
 * <p>This algorithm is applied before the standard rules for classification, and so unclassified
 * calls will fall through to contract checking and exception classification.
 *
 * <p><i>Before call</i>:
 *
 * <ol>
 *   <li>For each specification of method there is a {@link PreconditionOutcomeTable}, with the
 *       following:
 *       <ol>
 *         <li>Whether the preconditions of the specification fail or are satisfied. The
 *             preconditions fail if the Boolean expression of any precondition in the specification
 *             is false. Otherwise, the preconditions are satisfied. See {@link
 *             randoop.condition.OperationConditions#checkPreconditions(java.lang.Object[])}.
 *         <li>A set of expected exceptions. Evaluate the guard of each throws-condition, and for
 *             each one satisfied, add the exception to the set of expected exceptions. (There will
 *             be one set per specification.) See {@link
 *             randoop.condition.OperationConditions#checkThrowsPreconditions(java.lang.Object[])}.
 *         <li>The expected postcondition, if any. If the preconditions are satisfied, test the
 *             guards of the normal postconditions of the specification in order, and save the
 *             property for the first guard satisfied, if there is one. See {@link
 *             randoop.condition.OperationConditions#checkPostconditionGuards(java.lang.Object[])}.
 *       </ol>
 *
 *   <li>If for each table entry, the preconditions failed and the expected exception set is empty,
 *       then classify as <b>Invalid</b> and don't make the call. This avoids making a call on
 *       invalid arguments unless the specification indicates that exceptions should be thrown.
 * </ol>
 *
 * <p><i>After call</i>:
 *
 * <ol>
 *   <li>For each table entry with a non-empty expected exception set (see {@link
 *       randoop.condition.PreconditionOutcomeTable#addPostCheckGenerator(randoop.test.TestCheckGenerator)})
 *       <ul>
 *         <li>If an exception is thrown by the call and the thrown exception is a member of the
 *             set, then classify as <b>Expected</b>.
 *         <li>If an exception is thrown by the call and the thrown exception is not a member of the
 *             set, classify as <b>Error-revealing</b> (because the specification required an
 *             exception to be thrown, but it was not thrown).
 *         <li>If no exception is thrown, then classify as <b>Error-revealing</b>.
 *       </ul>
 *
 *   <li>If for each table entry, the preconditions failed, classify as <b>Invalid</b>.
 *   <li>For each table entry where all preconditions were satisfied, check the corresponding normal
 *       post-condition property, if one exists. If any such property fails, then classify as
 *       <b>Error-revealing</b>.
 * </ol>
 */
package randoop.condition;
