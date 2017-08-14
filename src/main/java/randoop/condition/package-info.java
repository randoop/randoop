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
 *   <li>Create a {@link randoop.condition.ExpectedOutcomeTable} by calling {@link
 *       randoop.condition.OperationConditions#checkPrestate(java.lang.Object[])}, which creates a
 *       table entry corresponding to each specification of the operation, recording:
 *       <ol>
 *         <li>Whether the {@link randoop.condition.BooleanExpression}s of the {@link
 *             randoop.condition.specification.PreSpecification}s fail or are satisfied. The
 *             expressions fail if any expression is false on the arguments. Otherwise, the
 *             preconditions are satisfied.
 *         <li>A set of {@link randoop.condition.ThrowsClause} objects for expected exceptions.
 *         <li>The expected {@link randoop.condition.PropertyExpression}, if any. This is the {@link
 *             randoop.condition.PropertyExpression}, of the first {@link
 *             randoop.condition.GuardPropertyPair} for which the guard {@link
 *             randoop.condition.BooleanExpression} is satisfied.
 *       </ol>
 *
 *   <li>If {@link randoop.condition.ExpectedOutcomeTable#isInvalidPrestate()} then classify as
 *       {@link randoop.main.GenInputsAbstract.BehaviorType#INVALID}, and don't make the call. This
 *       avoids making a call on invalid arguments unless the specification indicates that
 *       exceptions should be thrown.
 *   <li>Otherwise, create a {@link randoop.test.TestCheckGenerator} by calling {@link
 *       randoop.condition.ExpectedOutcomeTable#addPostCheckGenerator(randoop.test.TestCheckGenerator)}.
 *       This method selects the check generator as follows:
 *       <ol>
 *         <li>If any table entry contains an expected exception set, a {@link
 *             randoop.test.ExpectedExceptionGenerator} is returned.
 *         <li>If there are no expected exceptions, and no satisfied {@link
 *             randoop.condition.BooleanExpression}s for any {@link
 *             randoop.condition.specification.PreSpecification}, return an {@link
 *             randoop.test.InvalidCheckGenerator}.
 *         <li>Otherwise, if there are {@link randoop.condition.PropertyExpression} to evaluate,
 *             then extend the current generator with a {@link
 *             randoop.test.PostConditionCheckGenerator}.
 *       </ol>
 *
 * </ol>
 *
 * <p><i>After call</i>:
 *
 * <p>The check generator created before the call is applied to the results of the call.
 *
 * <ol>
 *   <li>The {@link randoop.test.ExpectedExceptionGenerator} is evaluated over the expected
 *       exception set such that
 *       <ul>
 *         <li>If an exception is thrown by the call and the thrown exception is a member of the
 *             set, then classify as {@link randoop.main.GenInputsAbstract.BehaviorType#EXPECTED}.
 *         <li>If an exception is thrown by the call and the thrown exception is not a member of the
 *             set, classify as {@link randoop.main.GenInputsAbstract.BehaviorType#ERROR} (because
 *             the specification required an exception to be thrown, but it was not thrown).
 *         <li>If no exception is thrown, then classify as {@link
 *             randoop.main.GenInputsAbstract.BehaviorType#ERROR}.
 *       </ul>
 *
 *   <li>The {@link randoop.test.InvalidCheckGenerator} will classify the call as {@link
 *       randoop.main.GenInputsAbstract.BehaviorType#INVALID}.
 *   <li>The {@link randoop.test.PostConditionCheckGenerator} will, for each table entry where all
 *       guards were satisfied, check the corresponding {@link
 *       randoop.condition.PropertyExpression}, if one exists. If any such expression fails, then
 *       classify as {@link randoop.main.GenInputsAbstract.BehaviorType#ERROR}.
 * </ol>
 */
package randoop.condition;
