package randoop.sequence;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.ExecutionVisitor;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.condition.ExpectedOutcomeTable;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.test.Check;
import randoop.test.InvalidChecks;
import randoop.test.InvalidValueCheck;
import randoop.test.TestCheckGenerator;
import randoop.test.TestChecks;
import randoop.types.ReferenceType;
import randoop.types.Type;
import randoop.util.IdentityMultiMap;
import randoop.util.ProgressDisplay;

/**
 * An ExecutableSequence wraps a {@link Sequence} with functionality for executing the sequence. It
 * also lets the client add {@link Check}s that check expected behaviors of the execution.
 *
 * <p>An ExecutableSequence augments a sequence with three additional pieces of data:
 *
 * <ul>
 *   <li><b>Execution results.</b> An ExecutableSequence can be executed, and the results of the
 *       execution (meaning the objects created during execution, and any exceptions thrown) are
 *       made available to clients or execution visitors to inspect.
 *   <li><b>Checks.</b> A check is an object representing an expected runtime behavior of the
 *       sequence. Clients can add checks to specific indices of the sequence. For example, a client
 *       might add a {@code NotNull} check to the ith index of a sequence to signify that the value
 *       returned by the statement at index i should not be null.
 *   <li><b>Check evaluation results.</b> Corresponding to every check is a boolean value that
 *       represents whether the check passed or failed during the last execution of the sequence.
 * </ul>
 *
 * <p>Of the three pieces of data above, an ExecutableSequence only directly manages the first one,
 * i.e. the execution results. Other pieces of data, including checks and check evaluation results,
 * are added or removed by the client of the ExecutableSequence. One way of doing this is by
 * implementing an {@link ExecutionVisitor} and passing it as an argument to the {@code execute}
 * method.
 *
 * <p>The method {@code execute(ExecutionVisitor v)} executes the code that the sequence represents.
 * This method uses reflection to execute each element in the sequence (method call, constructor
 * call, primitive or array declaration, etc). Before executing each statement (e.g. the i-th
 * statement), execute(v) calls v.visitBefore(this, i), and after executing each statement, it calls
 * v.visitAfter(this, i). The purpose of the visitor is to examine the unfolding execution, and take
 * some action depending on its intended purpose. For example, it may decorate the sequence with
 * {@link Check}s about the execution.
 *
 * <p>NOTES.
 *
 * <ul>
 *   <li>It only makes sense to call the following methods <b>after</b> executing the i-th statement
 *       in a sequence:
 *       <ul>
 *         <li>{@link #isNormalExecution}
 *         <li>{@link #getResult}
 *         <li>{@link #getValue}
 *       </ul>
 *
 * </ul>
 *
 * <h3 id="eval-algorithm">Condition Evaluation Algorithm</h3>
 *
 * The {@link #execute(randoop.ExecutionVisitor, randoop.test.TestCheckGenerator, boolean)} method
 * implements the condition evaluation algorithm described below.
 *
 * <p><i>Input</i>: a {@link randoop.operation.TypedClassOperation}, the {@link
 * randoop.condition.ExecutableSpecification} for the method, and arguments for a call to the
 * operation.
 *
 * <p><i>Goal</i>: classify the call to the operation using the arguments as {@link
 * randoop.main.GenInputsAbstract.BehaviorType#EXPECTED}, {@link
 * randoop.main.GenInputsAbstract.BehaviorType#INVALID} or {@link
 * randoop.main.GenInputsAbstract.BehaviorType#ERROR} based on the elements of {@link
 * randoop.condition.ExecutableSpecification}.
 *
 * <p><i>Definitions</i>: Let {@code expression} be either a {@link
 * randoop.condition.ExecutableBooleanExpression} representing a {@link
 * randoop.condition.specification.Precondition} or {@link randoop.condition.specification.Guard};
 * or the {@link randoop.condition.ExecutableBooleanExpression} for a {@link
 * randoop.condition.specification.Property}. Then {@code expression} evaluated on the method
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
 *       randoop.condition.ExecutableSpecification#checkPrestate(java.lang.Object[])}, which creates
 *       a table entry corresponding to each specification of the operation, recording:
 *       <ol>
 *         <li>Whether the {@link randoop.condition.ExecutableBooleanExpression}s of the {@link
 *             randoop.condition.specification.Precondition}s fail or are satisfied. The expressions
 *             fail if any expression is false on the arguments. Otherwise, the preconditions are
 *             satisfied.
 *         <li>A set of {@link randoop.condition.ThrowsClause} objects for expected exceptions.
 *         <li>The expected {@link randoop.condition.ExecutableBooleanExpression}, if any. This is
 *             the {@link randoop.condition.ExecutableBooleanExpression}, of the first {@link
 *             randoop.condition.GuardPropertyPair} for which the guard {@link
 *             randoop.condition.ExecutableBooleanExpression} is satisfied.
 *       </ol>
 *
 *   <li>If {@link randoop.condition.ExpectedOutcomeTable#isInvalidCall()} then classify as {@link
 *       randoop.main.GenInputsAbstract.BehaviorType#INVALID}, and don't make the call. This avoids
 *       making a call on invalid arguments unless the specification indicates that exceptions
 *       should be thrown.
 *   <li>Otherwise, create a {@link randoop.test.TestCheckGenerator} by calling {@link
 *       randoop.condition.ExpectedOutcomeTable#addPostCheckGenerator(randoop.test.TestCheckGenerator)}.
 *       This method selects the check generator as follows:
 *       <ol>
 *         <li>If any table entry contains an expected exception set, a {@link
 *             randoop.test.ExpectedExceptionGenerator} is returned.
 *         <li>If there are no expected exceptions, and no satisfied {@link
 *             randoop.condition.ExecutableBooleanExpression}s for any {@link
 *             randoop.condition.specification.Precondition}, return an {@link
 *             randoop.test.InvalidCheckGenerator}.
 *         <li>Otherwise, if there are {@link randoop.condition.ExecutableBooleanExpression} to
 *             evaluate, then extend the current generator with a {@link
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
 *       randoop.condition.ExecutableBooleanExpression}, if one exists. If any such expression
 *       fails, then classify as {@link randoop.main.GenInputsAbstract.BehaviorType#ERROR}.
 * </ol>
 */
public class ExecutableSequence {

  /** The underlying sequence. */
  public Sequence sequence;

  /** The checks for this sequence */
  private TestChecks<?> checks;

  /**
   * Contains the runtime objects created and exceptions thrown (if any) during execution of this
   * sequence. Invariant: sequence.size() == executionResults.size(). Transient because it can
   * contain arbitrary objects that may not be serializable.
   */
  private transient /*final*/ Execution executionResults;

  /**
   * How long it took to generate this sequence in nanoseconds, excluding execution time. Must be
   * directly set by the generator that creates this object (no code in this class sets its value).
   */
  public long gentime = -1;

  /**
   * How long it took to execute this sequence in nanoseconds, excluding generation time. Must be
   * directly set by the generator that creates this object. (no code in this class sets its value).
   */
  public long exectime = -1;

  /**
   * Flag to record whether execution of sequence has a null input.
   *
   * <p>[This is wonky, it really belongs to execution.]
   */
  private boolean hasNullInput;

  /** Output buffer used to capture the output from the executed sequence */
  private static ByteArrayOutputStream output_buffer = new ByteArrayOutputStream();

  private static PrintStream ps_output_buffer = new PrintStream(output_buffer);

  /* Maps values to the variables that hold them. */
  private IdentityMultiMap<Object, Variable> variableMap;

  /**
   * Create an executable sequence that executes the given sequence.
   *
   * @param sequence the underlying sequence for this executable sequence
   */
  public ExecutableSequence(Sequence sequence) {
    this.sequence = sequence;
    this.executionResults = new Execution(sequence);
    this.hasNullInput = false;
    this.variableMap = new IdentityMultiMap<>();
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < sequence.size(); i++) {
      sequence.appendCode(b, i);
      // It's a bit confusing, but the commented execution results refer
      // to the statement ABOVE, not below as is standard for comments.
      if (executionResults.size() > i) {
        b.append(" // ");
        b.append(executionResults.get(i).toString());
      }
      if ((i == sequence.size() - 1) && (checks != null)) {
        for (Check check : checks.checks()) {
          b.append(Globals.lineSep);
          b.append(check.toString());
        }
      }
      b.append(Globals.lineSep);
    }
    return b.toString();
  }

  /**
   * Return this sequence as code. Similar to {@link Sequence#toCodeString()} except includes the
   * checks.
   *
   * <p>If for a given statement there is a check of type {@link randoop.test.ExceptionCheck}, that
   * check's pre-statement code is printed immediately before the statement, and its post-statement
   * code is printed immediately after the statement.
   *
   * @return the sequence as a string
   */
  private List<String> toCodeLines() {
    List<String> lines = new ArrayList<>();
    for (int i = 0; i < sequence.size(); i++) {

      // Only print primitive declarations if the last/only statement
      // of the sequence, because, otherwise, primitive values will be used as
      // actual parameters: e.g. "foo(3)" instead of "int x = 3 ; foo(x)"
      if (sequence.canUseShortForm()
          && sequence.getStatement(i).getShortForm() != null
          && i < sequence.size() - 1) {
        continue;
      }

      StringBuilder oneStatement = new StringBuilder();
      sequence.appendCode(oneStatement, i);

      if (i == sequence.size() - 1 && checks != null) {
        // Print exception check first, if present.
        Check exObs = checks.getExceptionCheck();
        if (exObs != null) {
          oneStatement.insert(0, exObs.toCodeStringPreStatement());
          oneStatement.append(exObs.toCodeStringPostStatement());
        }

        // Print the rest of the checks.
        for (Check d : checks.checks()) {
          oneStatement.insert(0, d.toCodeStringPreStatement());
          oneStatement.append(Globals.lineSep).append(d.toCodeStringPostStatement());
        }
      }
      lines.add(oneStatement.toString());
    }
    return lines;
  }

  /**
   * Return this sequence as code. Similar to {@link Sequence#toCodeString()} except includes the
   * checks.
   *
   * <p>If for a given statement there is a check of type {@link randoop.test.ExceptionCheck}, that
   * check's pre-statement code is printed immediately before the statement, and its post-statement
   * code is printed immediately after the statement.
   *
   * @return the sequence as a string
   */
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    for (String line : toCodeLines()) {
      b.append(line).append(Globals.lineSep);
    }
    return b.toString();
  }

  /**
   * Return the code representation of the i'th statement.
   *
   * @param i the statement index
   * @return the string representation of the statement
   */
  public String statementToCodeString(int i) {
    StringBuilder oneStatement = new StringBuilder();
    sequence.appendCode(oneStatement, i);
    return oneStatement.toString();
  }

  /**
   * Executes sequence, stopping on exceptions.
   *
   * @param visitor the {@link ExecutionVisitor} that collects checks from results
   * @param gen the check generator for tests
   */
  public void execute(ExecutionVisitor visitor, TestCheckGenerator gen) {
    execute(visitor, gen, true);
  }

  /**
   * Execute this sequence, invoking the given visitor as the execution unfolds. After invoking this
   * method, the client can query the outcome of executing each statement via the method {@link
   * #getResult}.
   *
   * <ul>
   *   <li>Before the sequence is executed, clears execution results and calls {@code
   *       visitor.initialize(this)}.
   *   <li>Executes each statement in the sequence. Before executing each statement calls the given
   *       visitor's {@code visitBefore} method. After executing each statement, calls the visitor's
   *       {@code visitAfter} method.
   *   <li>Tests the pre-, post-, and throws-conditions for the last statement.
   *   <li>Execution stops if one of the following conditions holds:
   *       <ul>
   *         <li>All statements in the sequences have been executed.
   *         <li>A pre-condition for the final statement fails
   *         <li>A statement's execution results in an exception and {@code ignoreException==false}.
   *         <li>A {@code null} input value is implicitly passed to the statement (i.e., not via
   *             explicit declaration like x = null)
   *         <li>After executing the i-th statement and calling the visitor's {@code visitAfter}
   *             method, a {@code ContractViolation} check is present at index i.
   *       </ul>
   *
   * </ul>
   *
   * @param visitor the {@code ExecutionVisitor}
   * @param gen the check generator
   * @param ignoreException the flag to indicate exceptions should be ignored
   * @throws Error if execution of the sequence throws an exception and {@code
   *     ignoreException==false}
   */
  @SuppressWarnings("SameParameterValue")
  private void execute(ExecutionVisitor visitor, TestCheckGenerator gen, boolean ignoreException) {

    visitor.initialize(this);

    // reset execution result values
    hasNullInput = false;
    executionResults.theList.clear();
    for (int i = 0; i < sequence.size(); i++) {
      executionResults.theList.add(NotExecuted.create());
    }

    for (int i = 0; i < this.sequence.size(); i++) {

      // Find and collect the input values to i-th statement.
      List<Variable> inputs = sequence.getInputs(i);
      Object[] inputValues;

      inputValues = getRuntimeInputs(executionResults.theList, inputs);

      if (i == this.sequence.size() - 1) {
        TypedOperation operation = this.sequence.getStatement(i).getOperation();
        if (operation.isConstructorCall() || operation.isMethodCall()) {
          ExpectedOutcomeTable outcomeTable = operation.checkConditions(inputValues);
          if (outcomeTable.isInvalidCall()) {
            checks = new InvalidChecks(new InvalidValueCheck(this, i));
            return;
          }
          gen = outcomeTable.addPostCheckGenerator(gen);
        }
      }

      visitor.visitBeforeStatement(this, i);
      executeStatement(sequence, executionResults.theList, i, inputValues);

      // make sure statement executed
      ExecutionOutcome statementResult = getResult(i);
      if (statementResult instanceof NotExecuted) {
        throw new Error("Unexecuted statement in sequence: " + this.toString());
      }
      // make sure no exception before final statement of sequence
      if ((statementResult instanceof ExceptionalExecution) && i < sequence.size() - 1) {
        if (ignoreException) {
          // this preserves previous behavior, which was simply to return if
          // exception occurred
          break;
        } else {
          String msg =
              "Encountered exception before final statement of error-revealing test (statement "
                  + i
                  + "): ";
          throw new Error(
              msg + ((ExceptionalExecution) statementResult).getException().getMessage());
        }
      }

      visitor.visitAfterStatement(this, i);
    }

    visitor.visitAfterSequence(this);

    // This is the only client call to generateTestChecks().
    checks = gen.generateTestChecks(this);
  }

  public Object[] getRuntimeInputs(List<Variable> inputs) {
    return getRuntimeInputs(executionResults.theList, inputs);
  }

  private Object[] getRuntimeInputs(List<ExecutionOutcome> outcome, List<Variable> inputs) {
    Object[] ros = getRuntimeValuesForVars(inputs, outcome);
    for (Object ro : ros) {
      if (ro == null) {
        this.hasNullInput = true;
      }
    }
    return ros;
  }

  /**
   * Returns the values for the given variables in the {@link Execution} object. The variables are
   * {@link Variable} objects in the {@link Sequence} of this {@link ExecutableSequence} object.
   *
   * @param vars a list of {@link Variable} objects
   * @param execution the object representing outcome of executing this sequence
   * @return array of values corresponding to variables
   */
  public static Object[] getRuntimeValuesForVars(List<Variable> vars, Execution execution) {
    return getRuntimeValuesForVars(vars, execution.theList);
  }

  private static Object[] getRuntimeValuesForVars(
      List<Variable> vars, List<ExecutionOutcome> execution) {
    Object[] runtimeObjects = new Object[vars.size()];
    for (int j = 0; j < runtimeObjects.length; j++) {
      int creatingStatementIdx = vars.get(j).getDeclIndex();
      assert execution.get(creatingStatementIdx) instanceof NormalExecution
          : execution.get(creatingStatementIdx).getClass();
      NormalExecution ne = (NormalExecution) execution.get(creatingStatementIdx);
      runtimeObjects[j] = ne.getRuntimeValue();
    }
    return runtimeObjects;
  }

  // Execute the index-th statement in the sequence.
  // Precondition: this method has been invoked on 0..index-1.
  private static void executeStatement(
      Sequence s, List<ExecutionOutcome> outcome, int index, Object[] inputVariables) {
    Statement statement = s.getStatement(index);

    // Capture any output Synchronize with ProgressDisplay so that
    // we don't capture its output as well.
    synchronized (ProgressDisplay.print_synchro) {
      PrintStream orig_out = System.out;
      PrintStream orig_err = System.err;
      if (GenInputsAbstract.capture_output) {
        System.out.flush();
        System.err.flush();
        System.setOut(ps_output_buffer);
        System.setErr(ps_output_buffer);
      }

      // assert ((statement.isMethodCall() && !statement.isStatic()) ?
      // inputVariables[0] != null : true);

      ExecutionOutcome r;
      try {
        r = statement.execute(inputVariables, Globals.blackHole);
      } catch (SequenceExecutionException e) {
        throw new SequenceExecutionException("Exception during execution of " + statement, e);
      }
      assert r != null;
      if (GenInputsAbstract.capture_output) {
        System.setOut(orig_out);
        System.setErr(orig_err);
        r.set_output(output_buffer.toString());
        output_buffer.reset();
      }
      outcome.set(index, r);
    }
  }

  /**
   * This method is typically used by ExecutionVisitors.
   *
   * <p>The result of executing the i-th element of the sequence.
   *
   * @param index the statement index
   * @return the outcome of the statement at index
   */
  public ExecutionOutcome getResult(int index) {
    sequence.checkIndex(index);
    return executionResults.get(index);
  }

  /**
   * Return the set of test checks for the most recent execution.
   *
   * @return the {@code TestChecks} generated from the most recent execution
   */
  public TestChecks<?> getChecks() {
    return checks;
  }

  /**
   * The result of executing the i-th element of the sequence.
   *
   * @param index which element to obtain
   * @return the result of executing the i-th element of the sequence, if that element's execution
   *     completed normally
   */
  private Object getValue(int index) {
    ExecutionOutcome result = getResult(index);
    if (result instanceof NormalExecution) {
      return ((NormalExecution) result).getRuntimeValue();
    }
    throw new Error("Abnormal execution in sequence: " + this);
  }

  /**
   * Returns the list of (reference type) values created and used by the last statement of this
   * sequence. Null output values are not included.
   *
   * @return the list of values created and used by the last statement of this sequence
   */
  public List<ReferenceValue> getLastStatementValues() {
    Set<ReferenceValue> values = new LinkedHashSet<>();

    Object outputValue = getValue(sequence.size() - 1);
    if (outputValue != null) {
      Variable outputVariable = sequence.getLastVariable();

      Type outputType = outputVariable.getType();

      if (outputType.isReferenceType() && !outputType.isString()) {
        ReferenceValue value = new ReferenceValue((ReferenceType) outputType, outputValue);
        values.add(value);
        variableMap.put(outputValue, outputVariable);
      }
    }

    for (Variable inputVariable : sequence.getInputs(sequence.size() - 1)) {
      Object inputValue = getValue(inputVariable.index);
      if (inputValue != null) {
        Type inputType = inputVariable.getType();
        if (inputType.isReferenceType() && !inputType.isString()) {
          values.add(new ReferenceValue((ReferenceType) inputType, inputValue));
          variableMap.put(inputValue, inputVariable);
        }
      }
    }

    return new ArrayList<>(values);
  }

  /**
   * Returns the list of input reference type values used to compute the input values of the last
   * statement.
   *
   * @return the list of input values used to compute values in last statement
   */
  public List<ReferenceValue> getInputValues() {
    Set<Integer> skipSet = new HashSet<>();
    for (Variable inputVariable : sequence.getInputs(sequence.size() - 1)) {
      skipSet.add(inputVariable.index);
    }

    Set<ReferenceValue> values = new LinkedHashSet<>();
    for (int i = 0; i < sequence.size() - 1; i++) {
      if (!skipSet.contains(i)) {
        Object value = getValue(i);
        if (value != null) {
          Variable variable = sequence.getVariable(i);
          Type type = variable.getType();
          if (type.isReferenceType() && !type.isString()) {
            values.add(new ReferenceValue((ReferenceType) type, value));
            variableMap.put(value, variable);
          }
        }
      }
    }
    return new ArrayList<>(values);
  }

  /**
   * Returns the set of variables that have the given value in the outcome of executing this
   * sequence.
   *
   * @param value the value
   * @return the set of variables that have the given value, or null if none
   */
  public List<Variable> getVariables(Object value) {
    Set<Variable> variables = variableMap.get(value);
    if (variables == null) {
      return null;
    } else {
      return new ArrayList<>(variables);
    }
  }

  /**
   * Returns some variable that has the given value in the outcome of executing this sequence.
   *
   * @param value the value
   * @return a variable that has the given value
   */
  public Variable getVariable(Object value) {
    return variableMap.get(value).iterator().next();
  }

  /**
   * This method is typically used by ExecutionVisitors.
   *
   * @param i the statement index to test for normal execution
   * @return true if execution of the i-th statement terminated normally
   */
  private boolean isNormalExecution(int i) {
    sequence.checkIndex(i);
    return getResult(i) instanceof NormalExecution;
  }

  public int getNonNormalExecutionIndex() {
    for (int i = 0; i < this.sequence.size(); i++) {
      if (!isNormalExecution(i)) {
        return i;
      }
    }
    return -1;
  }

  public boolean isNormalExecution() {
    return getNonNormalExecutionIndex() == -1;
  }

  /**
   * @param exceptionClass the exception thrown
   * @return the index in the sequence at which an exception of the given class (or a class
   *     compatible with it) was thrown. If no such exception, returns -1.
   */
  private int getExceptionIndex(Class<?> exceptionClass) {
    if (exceptionClass == null) {
      throw new IllegalArgumentException("exceptionClass<?> cannot be null");
    }
    for (int i = 0; i < this.sequence.size(); i++) {
      if ((getResult(i) instanceof ExceptionalExecution)) {
        ExceptionalExecution e = (ExceptionalExecution) getResult(i);
        if (exceptionClass.isAssignableFrom(e.getException().getClass())) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * @param exceptionClass the exception class
   * @return true if an exception of the given class (or a class compatible with it) has been thrown
   *     during this sequence's execution
   */
  public boolean throwsException(Class<?> exceptionClass) {
    return getExceptionIndex(exceptionClass) >= 0;
  }

  /**
   * Returns whether the sequence contains a non-executed statement. That happens if some statement
   * before the last one throws an exception.
   *
   * @return true if this sequence has non-executed statements, false otherwise
   */
  public boolean hasNonExecutedStatements() {
    return getNonExecutedIndex() != -1;
  }

  /**
   * Returns the index i for a non-executed statement, or -1 if there is no such index. Note that a
   * statement is considered executed even if it throws an exception.
   *
   * @return the index of a non-executed statement in this sequence
   */
  public int getNonExecutedIndex() {
    // Starting from the end of the sequence is always faster to find
    // non-executed statements.
    for (int i = this.sequence.size() - 1; i >= 0; i--) {
      if (getResult(i) instanceof NotExecuted) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sequence.hashCode(), checks.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ExecutableSequence)) {
      return false;
    }
    ExecutableSequence that = (ExecutableSequence) obj;
    if (!this.sequence.equals(that.sequence)) {
      return false;
    }
    if (this.checks == null) {
      return (that.checks == null);
    }
    return this.checks.equals(that.checks);
  }

  /**
   * Indicates whether the executed sequence has any null input values.
   *
   * @return true if there is a null input value in this sequence, false otherwise
   */
  public boolean hasNullInput() {
    return hasNullInput;
  }

  /**
   * Indicate whether checks are failing or passing.
   *
   * @return true if checks are all failing, or false if all passing
   */
  public boolean hasFailure() {
    return checks != null && checks.hasErrorBehavior();
  }

  /**
   * Indicate whether there are any invalid checks.
   *
   * @return true if the test checks have been set and are for invalid behavior, false otherwise
   */
  public boolean hasInvalidBehavior() {
    return checks != null && checks.hasInvalidBehavior();
  }

  /**
   * Adds a covered class to the most recent execution results of this sequence.
   *
   * @param c the class covered by the execution of this sequence
   */
  public void addCoveredClass(Class<?> c) {
    executionResults.addCoveredClass(c);
  }

  /**
   * Indicates whether the given class is covered by the most recent execution of this sequence.
   *
   * @param c the class to be covered
   * @return true if the class is covered by the sequence, false otherwise
   */
  public boolean coversClass(Class<?> c) {
    return executionResults.getCoveredClasses().contains(c);
  }

  /**
   * Return the operation from which this sequence was generated -- the operation of the last
   * statement of this sequence.
   *
   * @return the operation of the last statement of this sequence
   */
  public TypedOperation getOperation() {
    return this.sequence.getOperation();
  }

  /**
   * Return the number of statements in this sequence.
   *
   * @return the number of statements in this sequence
   */
  public int size() {
    return sequence.size();
  }
}
