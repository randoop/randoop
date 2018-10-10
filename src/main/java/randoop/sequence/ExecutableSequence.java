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
 * An ExecutableSequence wraps a {@link Sequence} with functionality for executing the sequence, via
 * methods {@link #execute(ExecutionVisitor, TestCheckGenerator)} and {@link
 * #execute(ExecutionVisitor, TestCheckGenerator, boolean)}. It also lets the client add {@link
 * Check}s that check expected behaviors of the execution.
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
 * <p>An ExecutableSequence only directly manages the execution results. Other data, including
 * checks and check evaluation results, are added or removed by the client of the
 * ExecutableSequence. One way of doing this is by implementing an {@link ExecutionVisitor} and
 * passing it as an argument to the {@code execute} method.
 */
public class ExecutableSequence {

  /** The underlying sequence. */
  public Sequence sequence;

  /** The checks for the last statement in this sequence. */
  private TestChecks<?> checks;

  /**
   * Contains the runtime objects created and exceptions thrown (if any) during execution of this
   * sequence. Invariant: sequence.size() == executionResults.size(). Transient because it can
   * contain arbitrary objects that may not be serializable.
   */
  private transient /*final*/ Execution executionResults;

  /**
   * How long it took to generate this sequence in nanoseconds, excluding execution time. Must be
   * directly set by the generator that creates this object (No code in this class sets its value.)
   */
  public long gentime = -1;

  /**
   * How long it took to execute this sequence in nanoseconds. Is -1 until the sequence completes
   * execution.
   */
  public long exectime = -1;

  /**
   * Flag to record whether execution of sequence has a null input.
   *
   * <p>TODO: This is wonky, it really belongs to execution.
   */
  private boolean hasNullInput = false;

  /** Captures output from the executed sequence. */
  // static, so initializing eagerly is not a large cost.
  private static ByteArrayOutputStream output_buffer = new ByteArrayOutputStream();

  /** Used to populate {@link #output_buffer}. */
  // static, so initializing eagerly is not a large cost.
  private static PrintStream output_buffer_stream = new PrintStream(output_buffer);

  /* Maps a value to the set of variables that hold it. */
  private IdentityMultiMap<Object, Variable> variableMap = new IdentityMultiMap<>();

  /**
   * Create an executable sequence that executes the given sequence.
   *
   * @param sequence the underlying sequence for this executable sequence
   */
  public ExecutableSequence(Sequence sequence) {
    this.sequence = sequence;
    this.executionResults = new Execution(sequence);
  }

  /** Reset this object to its initial state. */
  private void reset() {
    executionResults = new Execution(sequence);
    exectime = -1;
    hasNullInput = false;
    variableMap = new IdentityMultiMap<>();
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
      // of the sequence.  Usually, primitive values are used as arguments:
      // e.g. "foo(3)" instead of "int x = 3 ; foo(x)".
      if (sequence.shouldInlineLiterals()
          && sequence.getStatement(i).getInlinedForm() != null
          && i < sequence.size() - 1) {
        continue;
      }

      StringBuilder oneStatement = new StringBuilder();
      sequence.appendCode(oneStatement, i);

      if (i == sequence.size() - 1 && checks != null) {
        // Print exception check first, if present.
        // This makes its pre-statement part the last pre-statement part,
        // and its post-statement part the first post-statement part.
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
   * @see #execute(ExecutionVisitor, TestCheckGenerator, boolean)
   * @param visitor the {@link ExecutionVisitor} that collects checks from results
   * @param gen the check generator for tests
   */
  public void execute(ExecutionVisitor visitor, TestCheckGenerator gen) {
    // TODO: Setting the third argument to false would mask fewer errors.  Doing so causes 3 Randoop
    // system tests to fail (because some sequence throws an exception before the last statement).
    // One is innocuous:  java.lang.OutOfMemoryError due to creation of a very large object --
    // repeated executions evenutally exhaust memory.  Two others are odd: failures in
    // sun.reflect.DelegatingMethodAccessorImpl.invoke called by java.lang.reflect.Method.invoke.
    execute(visitor, gen, true);
  }

  /**
   * Execute this sequence, invoking the given visitor as the execution unfolds. For example, the
   * visitor may decorate the sequence with {@link Check}s about the execution.
   *
   * <p>This method operates as follows:
   *
   * <ul>
   *   <li>Clear execution results and call {@code visitor.initialize(this)}.
   *   <li>For each statement in the sequence:
   *       <ul>
   *         <li>call {@code visitor.visitBefore(this, i)}
   *         <li>execute the i-th statement, using reflection
   *         <li>call {@code visitor.visitAfter(this, i)}
   *       </ul>
   *   <li>For the last statement, check its specifications (pre-, post-, and throws-conditions).
   * </ul>
   *
   * Execution stops if one of the following conditions holds:
   *
   * <ul>
   *   <li>All statements in the sequences have been executed.
   *   <li>A pre-condition for the final statement fails
   *   <li>A statement's execution results in an exception and {@code ignoreException==false}.
   *   <li>A {@code null} input value is implicitly passed to the statement (i.e., not via explicit
   *       declaration like x = null)
   *   <li>After executing the i-th statement and calling the visitor's {@code visitAfter} method, a
   *       {@code ContractViolation} check is present at index i.
   * </ul>
   *
   * <p>After invoking this method, the client can query the outcome of executing each statement via
   * the method {@link #getResult}.
   *
   * @param visitor the {@code ExecutionVisitor}
   * @param gen the initial check generator, which this augments then uses
   * @param ignoreException if true, ignore exceptions thrown before the last statement
   * @throws Error if execution of the sequence throws an exception and {@code
   *     ignoreException==false}
   */
  @SuppressWarnings("SameParameterValue")
  private void execute(ExecutionVisitor visitor, TestCheckGenerator gen, boolean ignoreException) {

    long startTime = System.nanoTime();
    try { // try statement for timing

      visitor.initialize(this);

      this.reset();

      for (int i = 0; i < this.sequence.size(); i++) {

        // Collect the input values to i-th statement.
        Object[] inputValues = getRuntimeInputs(executionResults.outcomes, sequence.getInputs(i));

        if (i == this.sequence.size() - 1) {
          // This is the last statement in the sequence.
          TypedOperation operation = this.sequence.getStatement(i).getOperation();
          if (operation.isConstructorCall() || operation.isMethodCall()) {
            // Phase 1 of specification checking:  evaluate guards of the specifications before the
            // call.
            ExpectedOutcomeTable outcomeTable = operation.checkPrestate(inputValues);
            if (outcomeTable.isInvalidCall()) {
              checks = new InvalidChecks(new InvalidValueCheck(this, i));
              return;
            }
            gen = outcomeTable.addPostCheckGenerator(gen);
          }
        }

        visitor.visitBeforeStatement(this, i);
        executeStatement(sequence, executionResults.outcomes, i, inputValues);

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
            Throwable e = ((ExceptionalExecution) statementResult).getException();
            String msg =
                String.format(
                    "Exception before final statement%n  statement %d = %s, input = %s):%n  %s%n%s",
                    i,
                    sequence.getStatement(i),
                    inputValues,
                    (e.getMessage() == null ? "[no detail message]" : e.getMessage()),
                    sequence);
            throw new Error(msg, e);
          }
        }

        visitor.visitAfterStatement(this, i);
      }

      visitor.visitAfterSequence(this);

      // Phase 2 of specification checking: check for expected behavior after the call.
      // This is the only client call to generateTestChecks().
      checks = gen.generateTestChecks(this);

    } finally {
      exectime = System.nanoTime() - startTime;
    }
  }

  public Object[] getRuntimeInputs(List<Variable> inputs) {
    return getRuntimeInputs(executionResults.outcomes, inputs);
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
    return getRuntimeValuesForVars(vars, execution.outcomes);
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
        System.setOut(output_buffer_stream);
        System.setErr(output_buffer_stream);
      }

      // assert ((statement.isMethodCall() && !statement.isStatic()) ?
      // inputVariables[0] != null : true);

      ExecutionOutcome r;
      try {
        r = statement.execute(inputVariables, Globals.blackHole);
      } catch (SequenceExecutionException e) {
        throw new SequenceExecutionException("Problem while executing " + statement, e);
      }
      assert r != null;
      if (GenInputsAbstract.capture_output) {
        output_buffer_stream.flush();
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
   * <p>The result of executing the index-th element of the sequence.
   *
   * @param index the statement index
   * @return the outcome of the statement at index
   */
  public ExecutionOutcome getResult(int index) {
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
   * The result of executing the index-th element of the sequence.
   *
   * @param index which element to obtain
   * @return the result of executing the index-th element of the sequence, if that element's
   *     execution completed normally
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
    Variable outputVariable = sequence.getLastVariable();
    addReferenceValue(outputVariable, outputValue, values);

    for (Variable inputVariable : sequence.getInputs(sequence.size() - 1)) {
      Object inputValue = getValue(inputVariable.index);
      addReferenceValue(inputVariable, inputValue, values);
    }

    return new ArrayList<>(values);
  }

  /**
   * If the variable has a non-String reference type, add its value to the set and also add a
   * mapping to {@link #variableMap}.
   *
   * @param variable the variable to use as a value in variableMap
   * @param value the Java value to use as a key in variableMap
   * @param refValues the set of all reference values; is side-effected by this method
   */
  private void addReferenceValue(Variable variable, Object value, Set<ReferenceValue> refValues) {
    if (value != null) {
      Type type = variable.getType();
      if (type.isReferenceType() && !type.isString()) {
        refValues.add(new ReferenceValue((ReferenceType) type, value));
        variableMap.put(value, variable);
      }
    }
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
        Variable variable = sequence.getVariable(i);
        addReferenceValue(variable, value, values);
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
    return this.sequence.equals(that.sequence) && Objects.equals(this.checks, that.checks);
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
