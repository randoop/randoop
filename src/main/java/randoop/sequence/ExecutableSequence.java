package randoop.sequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import randoop.Check;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.ExecutionVisitor;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.main.GenInputsAbstract;
import randoop.test.TestCheckGenerator;
import randoop.test.TestChecks;
import randoop.util.ProgressDisplay;
import randoop.util.Reflection;

/**
 * An ExecutableSequence wraps a {@link Sequence} with functionality for
 * executing the sequence. It also lets the client add {@link Check}s that check
 * expected behaviors of the execution.
 * <p>
 * An ExecutableSequence augments a sequence with three additional pieces of
 * data:
 * <ul>
 * <li><b>Execution results.</b> An ExecutableSequence can be executed, and the
 * results of the execution (meaning the objects created during execution, and
 * any exceptions thrown) are made available to clients or execution visitors to
 * inspect.
 * <li><b>Checks.</b> A check is an object representing an expected runtime
 * behavior of the sequence. Clients can add checks to specific indices of the
 * sequence. For example, a client might add a <code>NotNull</code> check to the
 * ith index of a sequence to signify that the value returned by the statement
 * at index i should not be null.
 * <li><b>Check evaluation results.</b> Corresponding to every check is a
 * boolean value that represents whether the check passed or failed during the
 * last execution of the sequence.
 * </ul>
 * <p>
 *
 * Of the three pieces of data above, an ExecutableSequence only directly
 * manages the first one, i.e. the execution results. Other pieces of data,
 * including checks and check evaluation results, are added or removed by the
 * client of the ExecutableSequence. One way of doing this is by implementing an
 * {@link ExecutionVisitor} and passing it as an argument to the
 * <code>execute</code> method.
 *
 * <p>
 *
 * The method <code>execute(ExecutionVisitor v)</code> executes the code that
 * the sequence represents. This method uses reflection to execute each element
 * in the sequence (method call, constructor call, primitive or array
 * declaration, etc). Before executing each statement (e.g. the i-th statement),
 * execute(v) calls v.visitBefore(this, i), and after executing each statement,
 * it calls v.visitAfter(this, i). The purpose of the visitor is to examine the
 * unfolding execution, and take some action depending on its intended purpose.
 * For example, it may decorate the sequence with {@link Check}s about the
 * execution.
 *
 * <p>
 *
 * NOTES.
 *
 * <ul>
 * <li>It only makes sense to call the following methods <b>after</b> executing
 * the i-th statement in a sequence:
 * <ul>
 * <li>isNormalExecution(i)
 * <li>isExceptionalExecution(i)
 * <li>getExecutionResult(i)
 * <li>getResult(i)
 * <li>getExeception(i)
 * </ul>
 * </ul>
 *
 */
public class ExecutableSequence implements Serializable {

  private static final long serialVersionUID = 2337273514619824184L;

  /** The underlying sequence. */
  public Sequence sequence;

  /** The checks for this sequence */
  private TestChecks checks;

  /**
   * Contains the runtime objects created and exceptions thrown (if any) during
   * execution of this sequence. Invariant: sequence.size() ==
   * executionResults.size(). Transient because it can contain arbitrary objects
   * that may not be serializable.
   */
  protected transient /* final */ Execution executionResults;

  /**
   * How long it took to generate this sequence in nanoseconds, excluding
   * execution time. Must be directly set by the generator that creates this
   * object (no code in this class sets its value).
   */
  // TODO doesn't this more properly belong in Sequence class?
  public long gentime = -1;

  /**
   * How long it took to execute this sequence in nanoseconds, excluding
   * generation time. Must be directly set by the generator that creates this
   * object. (no code in this class sets its value).
   */
  public long exectime = -1;

  /**
   * Flag to record whether execution of sequence has a null input. [this is
   * wonky really belongs to execution]
   */
  private boolean hasNullInput;

  /** Output buffer used to capture the output from the executed sequence **/
  private static ByteArrayOutputStream output_buffer = new ByteArrayOutputStream();
  private static PrintStream ps_output_buffer = new PrintStream(output_buffer);

  // Re-initialize executionResults list.
  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    // customized deserialization code
    this.executionResults = new Execution(sequence);
  }

  /**
   * Create an executable sequence that executes the given sequence.
   *
   * @param sequence  the underlying sequence for this executable sequence
   */
  public ExecutableSequence(Sequence sequence) {
    this.sequence = sequence;
    this.executionResults = new Execution(sequence);
    this.hasNullInput = false;
  }

  /**
   * Create an executable sequence directly using the given arguments.
   *
   * Don't use this constructor! (Unless you know what you're doing.)
   *
   * @param sequence  the base sequence of this object
   * @param exec  the execution for this sequences
   * @param checks over the sequence
   */
  public ExecutableSequence(Sequence sequence, Execution exec, TestChecks checks) {
    this.sequence = sequence;
    this.executionResults = exec;
    this.checks = checks;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < sequence.size(); i++) {
      sequence.appendCode(b, i);
      // It's a bit confusing, but the commented execution results refer
      // to the statement ABOVE, not below as is standard for comments.
      if (executionResults.size() > i) b.append(executionResults.get(i).toString());
      if ((i == sequence.size() - 1) && (checks != null)) {
        Map<Check, Boolean> ckMap = checks.get();
        for (Map.Entry<Check, Boolean> entry : ckMap.entrySet()) {
          b.append(Globals.lineSep);
          b.append(entry.getKey().toString());
          b.append(" : ");
          b.append(entry.getValue().toString());
        }
      }
      b.append(Globals.lineSep);
    }
    return b.toString();
  }

  /**
   * Returns an id based on the contents of the sequence alone. The id will
   * match for any identical sequences (no matter how they are constructed).
   * Used for debugging
   *
   * @return the ID for the sequence
   */
  public int seq_id() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < sequence.size(); i++) {

      // If short format, don't print out primitive declarations
      // because primitive values will be directly added to methods
      // (e.g. "foo(3)" instead of "int x = 3 ; foo(x)".
      if (sequence.getStatement(i).getShortForm() != null) {
        continue;
      }

      StringBuilder oneStatement = new StringBuilder();
      sequence.appendCode(oneStatement, i);

      b.append(oneStatement);
    }
    return b.toString().hashCode();
  }

  /**
   * Return this sequence as code. Similar to {@link Sequence#toCodeString()}
   * except includes the checks.
   *
   * If for a given statement there is a check of type
   * {@link randoop.ExceptionCheck}, that check's pre-statement code is printed
   * immediately before the statement, and its post-statement code is printed
   * immediately after the statement.
   *
   * @return the sequence as a string
   */
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < sequence.size(); i++) {

      // Only print primitive declarations if the last/only statement
      // of the sequence, because, otherwise, primitive values will be used as
      // actual parameters: e.g. "foo(3)" instead of "int x = 3 ; foo(x)"
      if (sequence.getStatement(i).getShortForm() != null && i < sequence.size() - 1) {
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
          oneStatement.append(Globals.lineSep);
        }

        // Print the rest of the checks.
        for (Check d : checks.get().keySet()) {
          oneStatement.insert(0, d.toCodeStringPreStatement());
          oneStatement.append(d.toCodeStringPostStatement());
          oneStatement.append(Globals.lineSep);
        }
      }
      b.append(oneStatement);
    }
    return b.toString();
  }

  /**
   * Return the code representation of the i'th statement.
   * @param i  the statement index
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
   * @param visitor
   *          the {@code ExecutionVistior} that collects checks from results
   * @param gen
   *          the check generator for tests
   */
  public void execute(ExecutionVisitor visitor, TestCheckGenerator gen) {
    execute(visitor, gen, true);
  }

  /**
   * Execute this sequence, invoking the given visitor as the execution unfolds.
   * After invoking this method, the client can query the outcome of executing
   * each statement via the method <code>getResult(i)</code>.
   *
   * <ul>
   * <li>Before the sequence is executed, clears execution results and calls
   * <code>visitor.initialize(this)</code>.
   * <li>Executes each statement in the sequence. Before executing each
   * statement calls the given visitor's <code>visitBefore</code> method. After
   * executing each statement, calls the visitor's <code>visitAfter</code>
   * method.
   * <li>Execution stops if one of the following conditions holds:
   * <ul>
   * <li>All statements in the sequences have been executed.
   * <li>A statement's execution results in an exception and
   * <code>stop_on_exception==true</code>.
   * <li>A <code>null</code> input value is implicitly passed to the statement
   * (i.e., not via explicit declaration like x = null)
   * <li>After executing the i-th statement and calling the visitor's
   * <code>visitAfter</code> method, a <code>ContractViolation</code> check is
   * present at index i.
   * </ul>
   * </ul>
   *
   * @param visitor
   *          the {@code ExecutionVisitor}
   * @param gen
   *          the check generator
   * @param ignoreException
   *          the flag to indicate exceptions should be ignored
   */
  public void execute(ExecutionVisitor visitor, TestCheckGenerator gen, boolean ignoreException) {

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
      Object[] inputVariables = new Object[inputs.size()];

      inputVariables = getRuntimeInputs(sequence, executionResults.theList, i, inputs);

      visitor.visitBeforeStatement(this, i);
      executeStatement(sequence, executionResults.theList, i, inputVariables);

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

    checks = gen.visit(this);
  }

  public Object[] getRuntimeInputs(
      Sequence s, List<ExecutionOutcome> outcome, int i, List<Variable> inputs) {

    Object[] ros = getRuntimeValuesForVars(inputs, outcome);

    // TODO move this check elsewhere -- results shouldn't be part of ES
    for (int ri = 0; ri < ros.length; ri++) {
      if (ros[ri] == null) {
        this.hasNullInput = true;
      }
    }

    return ros;
  }

  /**
   * Returns the values for the given variables in the {@link Execution} object.
   * The variables are {@link Variable} objects in the {@link Sequence} of this
   * {@link ExecutableSequence} object.
   *
   * @param vars
   *          a list of {@link Variable} objects.
   * @param execution
   *          the object representing outcome of executing this sequence.
   * @return array of values corresponding to variables.
   */
  public static Object[] getRuntimeValuesForVars(List<Variable> vars, Execution execution) {
    return getRuntimeValuesForVars(vars, execution.theList);
  }

  protected static Object[] getRuntimeValuesForVars(
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
  public static void executeStatement(
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

      ExecutionOutcome r = statement.execute(inputVariables, Globals.blackHole);
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
   * The result of executing the i-th element of the sequence.
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
  public TestChecks getChecks() {
    return checks;
  }

  /**
   * Return the results of each statement for the most recent execution.
   *
   * @return all the execution outcomes for this sequence.
   */
  public ExecutionOutcome[] getAllResults() {
    ExecutionOutcome[] ret = new ExecutionOutcome[executionResults.size()];
    for (int i = 0; i < executionResults.size(); i++) {
      ret[i] = executionResults.get(i);
    }
    return ret;
  }

  /**
   * @return the number of elements in the sequence that were executed before an
   *         execution result of type randoop.NotExecuted.
   */
  public int executedSize() {
    int count = 0;
    for (; count < executionResults.size(); count++) {
      if (executionResults.get(count) instanceof NotExecuted) {
        break;
      }
    }
    return count;
  }

  /**
   * This method is typically used by ExecutionVisitors.
   *
   * @param i  the statement index to test for normal execution
   * @return true if execution of the i-th statement terminated normally
   */
  public boolean isNormalExecution(int i) {
    sequence.checkIndex(i);
    return getResult(i) instanceof NormalExecution;
  }

  public int getNonNormalExecutionIndex() {
    for (int i = 0; i < this.sequence.size(); i++) {
      if (!isNormalExecution(i)) return i;
    }
    return -1;
  }

  public boolean isNormalExecution() {
    return getNonNormalExecutionIndex() == -1;
  }

  /**
   * Returns the index i for which this.isExceptionalExecution(i), or -1 if
   * there is no such index.
   *
   * @return the statement index where an exception was thrown
   */
  public int exceptionIndex() {
    if (!throwsException()) throw new RuntimeException("Execution does not throw an exception");
    for (int i = 0; i < this.sequence.size(); i++) {
      if (this.getResult(i) instanceof ExceptionalExecution) {
        return i;
      }
    }
    return -1;
  }

  /**
   * @param exceptionClass  the exception thrown
   * @return The index in the sequence at which an exception of the given class
   *         (or a class compatible with it) was thrown. If no such exception,
   *         returns -1.
   */
  public int getExceptionIndex(Class<?> exceptionClass) {
    if (exceptionClass == null)
      throw new IllegalArgumentException("exceptionClass<?> cannot be null");
    for (int i = 0; i < this.sequence.size(); i++)
      if ((getResult(i) instanceof ExceptionalExecution)) {
        ExceptionalExecution e = (ExceptionalExecution) getResult(i);
        if (Reflection.canBeUsedAs(e.getException().getClass(), exceptionClass)) return i;
      }
    return -1;
  }

  /**
   * @param exceptionClass  the exception class
   * @return True if an exception of the given class (or a class compatible with
   *         it) has been thrown during this sequence's execution.
   */
  public boolean throwsException(Class<?> exceptionClass) {
    return getExceptionIndex(exceptionClass) >= 0;
  }

  /**
   *
   * @return True if an exception has been thrown during this sequence's
   *         execution.
   */
  public boolean throwsException() {
    for (int i = 0; i < this.sequence.size(); i++)
      if (getResult(i) instanceof ExceptionalExecution) return true;
    return false;
  }

  /**
   * Returns whether the sequence contains a non-executed statement. That
   * happens if some statement before the last one throws an exception.
   *
   * @return true if this sequence has non-executed statements, false otherwise
   */
  public boolean hasNonExecutedStatements() {
    return getNonExecutedIndex() != -1;
  }

  /**
   * Returns the index i for a non-executed statement, or -1 if there is no such
   * index. Note that a statement is considered executed even if it throws an
   * exception.
   */
  public int getNonExecutedIndex() {
    // Starting from the end of the sequence is always faster to find
    // non-executed statements.
    for (int i = this.sequence.size() - 1; i >= 0; i--)
      if (getResult(i) instanceof NotExecuted) return i;
    return -1;
  }

  public static <D extends Check> List<Sequence> getSequences(List<ExecutableSequence> exec) {
    List<Sequence> result = new ArrayList<Sequence>(exec.size());
    for (ExecutableSequence execSeq : exec) {
      result.add(execSeq.sequence);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sequence.hashCode(), checks.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ExecutableSequence)) return false;
    ExecutableSequence that = (ExecutableSequence) obj;
    if (!this.sequence.equals(that.sequence)) return false;
    if (this.checks == null) return (that.checks == null);
    if (!this.checks.equals(that.checks)) return false;

    return true;
  }

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
   * Indicate whether there are any checks.
   *
   * @return true if the test checks are nonempty, and false otherwise
   */
  public boolean hasChecks() {
    return checks != null && checks.hasChecks();
  }

  /**
   * Indicate whether there are any invalid checks.
   *
   * @return true if the test checks have been set and are for invalid behavior,
   *         false otherwise
   */
  public boolean hasInvalidBehavior() {
    return checks != null && checks.hasInvalidBehavior();
  }

  /**
   * Adds a covered class to the most recent execution results of this sequence.
   *
   * @param c
   *          the class covered by the execution of this sequence
   */
  public void addCoveredClass(Class<?> c) {
    executionResults.addCoveredClass(c);
  }

  /**
   * Indicates whether the given class is covered by the most recent execution
   * of this sequence.
   *
   * @param c
   *          the class to be covered
   * @return true if the class is covered by the sequence, false otherwise
   */
  public boolean coversClass(Class<?> c) {
    return executionResults.getCoveredClasses().contains(c);
  }
}
