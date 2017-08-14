/**
 * Package containing classes for handling specifications on operations.
 *
 * <h3 id="specifications">Operation Specifications and Evaluation</h3>
 *
 * The specifications for an operation are represented by an {@link
 * randoop.condition.specification.OperationSpecification} object that contains lists of pre-,
 * post-, or throws-specifications. The specifications are read from JSON files that serialize the
 * specification classes using {@link
 * randoop.condition.SpecificationCollection#create(java.util.List)}.
 *
 * <p>The specifications are translated to objects that allow the underlying Boolean expressions to
 * be evaluated A {@link randoop.condition.specification.PreSpecification} is translated to a {@link
 * randoop.condition.BooleanExpression} object, a {@link
 * randoop.condition.specification.PostSpecification} to a {@link
 * randoop.condition.GuardPropertyPair}, and a {@link
 * randoop.condition.specification.ThrowsSpecification} a {@link randoop.condition.GuardThrowsPair}.
 * The pair classes each have a {@link randoop.condition.BooleanExpression} for the {@link
 * randoop.condition.specification.Guard} that is to be evaluated before the operation call, along
 * with either a {@link randoop.condition.PropertyExpression} or a {@link
 * randoop.condition.ThrowsClause} to be evaluated after the operation call.
 *
 * <p>All of the pre-condition expressions and pairs for an operation are represented as an {@link
 * randoop.condition.OperationConditions} object that is created using {@link
 * randoop.condition.SpecificationCollection#getOperationConditions(java.lang.reflect.AccessibleObject)}.
 *
 * <p>The operations are used in the {@link
 * randoop.sequence.ExecutableSequence#execute(randoop.ExecutionVisitor,
 * randoop.test.TestCheckGenerator, boolean)} method that implements the condition evaluation
 * algorithm described below.
 *
 * <h3 id="eval-algorithm">Condition Evaluation Algorithm</h3>
 *
 * <p><i>Input</i>: a {@link randoop.operation.TypedClassOperation}, the {@link
 * randoop.condition.OperationConditions} for the method, and arguments for a call to the operation.
 *
 * <p><i>Goal</i>: classify the call to the operation using the arguments as {@link
 * randoop.main.GenInputsAbstract.BehaviorType#EXPECTED}, {@link
 * randoop.main.GenInputsAbstract.BehaviorType#INVALID} or {@link
 * randoop.main.GenInputsAbstract.BehaviorType#ERROR} based on the elements of {@link
 * randoop.condition.OperationConditions}.
 *
 * <p><i>Definitions</i>: Let {@code expression} be either a {@link
 * randoop.condition.BooleanExpression} representing a {@link
 * randoop.condition.specification.PreSpecification} or {@link
 * randoop.condition.specification.Guard}; or the {@link randoop.condition.PropertyExpression} for a
 * {@link randoop.condition.specification.Property}. Then {@code expression} evaluated on the method
 * arguments is <i>satisfied</i> if {@code expression.check(values)} evaluates to true, and
 * <i>fails</i> otherwise.
 *
 * <p><i>Description</i>: The algorithm consists of two phases: (1) evaluating guards of the
 * specifications before the call, and (2) checking for expected behavior after the call.
 * Specifically, the first phase saves the results of the evaluation in a {@link
 * randoop.condition.ExpectedOutcomeTable} table that is used in the second phase.
 *
 * <p>This algorithm is applied before the standard rules for classification, and so unclassified
 * calls will fall through to contract checking and exception classification.
 *
 * <p><i>Before call</i>:
 *
 * <ol>
 *   <li>For each specification of method there is a {@link randoop.condition.ExpectedOutcomeTable},
 *       with the following:
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
 *       randoop.condition.ExpectedOutcomeTable#addPostCheckGenerator(randoop.test.TestCheckGenerator)})
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
