package randoop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import randoop.main.GenInputsAbstract;
import randoop.util.ProgressDisplay;
import randoop.util.Reflection;

/**
 * An ExecutableSequence wraps a {@link Sequence} with functionality for
 * executing the sequence. It also lets the client add {@link Check}s
 * that check expected behaviors of the execution.
 * <p>
 * An ExecutableSequence augments a sequence with three additional pieces
 * of data:
 * <p>
 * <ul>
 * <li> <b>Execution results.</b> An ExecutableSequence can be executed, and the
 *      results of the execution (meaning the objects created during execution,
 *      and any exceptions thrown) are made available to clients or execution
 *      visitors to inspect.
 * <li> <b>Checks.</b> A check is an object representing an expected runtime behavior
 *      of the sequence. Clients can add checks to specific indices of the sequence.
 *      For example, a client might add a <code>NotNull</code> check to the ith index 
 *      of a sequence to signify that the value returned by the statement at index i
 *      should not be null.
 * <li> <b>Check evaluation results.</b> Corresponding to every check is a boolean value that
 *      represents whether the check passed or failed during the last execution
 *      of the sequence.
 * </ul>
 * <p>
 * 
 * Of the three pieces of data above, an ExecutableSequence only directly manages
 * the first one, i.e. the execution results. Other pieces of data, including
 * checks and check evaluation results, are added or removed by the
 * client of the ExecutableSequence. One way of doing this is by implementing an
 * {@link ExecutionVisitor} and passing it as an argument to the <code>execute</code>
 * method.
 * 
 * <p>
 * 
 * The method <code>execute(ExecutionVisitor v)</code> executes the code that the 
 * sequence represents. This method uses reflection to execute each element in the 
 * sequence (method call, constructor call, primitive or array declaration, etc).
 * Before executing each statement (e.g. the i-th statement), execute(v)
 * calls v.visitBefore(this, i), and after executing each statement, it calls
 * v.visitAfter(this, i). The purpose of the visitor is to examine the unfolding
 * execution, and take some action depending on its intended purpose. For example,
 * it may decorate the sequence with {@link Check}s about the
 * execution.
 * 
 * <p>
 * 
 * NOTES.
 *
 * <ul>
 * <li> It only makes sense to call the following methods <b>after</b> executing the
 * i-th statement in a sequence:
 *    <ul>
 *        <li> isNormalExecution(i)
 *        <li> isExceptionalExecution(i)
 *        <li> getExecutionResult(i)
 *        <li> getResult(i)
 *        <li> getExeception(i)
 *    </ul>
 * </ul>
 *
 */
public class ExecutableSequence implements Serializable {

  private static final long serialVersionUID = 2337273514619824184L;

  // The underlying sequence.
  public Sequence sequence;

  // The i-th element of this list contains the checks for the i-th
  // sequence element. Invariant: sequence.size() == checks.size().
  // TODO: don't expose this field directly.
  protected List<List<Check>> checks;
  
  // Contains the fail/pass results of executing the checks in this.checks.
  // The <i,j>-th element of this list is true if during execution,
  // the <i,j>-th check passed, and false if it failed.
  // TODO: don't expose this field directly.
  protected List<List<Boolean>> checksResults;

  // Contains the runtime objects created and exceptions thrown (if any)
  // during execution of this sequence.
  // Invariant: Invariant: sequence.size() == executionResults.size().
  // Transient because it can contain arbitrary objects that may not be
  // serializable.
  protected transient /*final*/ Execution executionResults;

  // How long it took to generate this sequence in nanoseconds,
  // excluding execution time.
  // Must be directly set by the generator that creates this object
  // (no code in this class sets its value).
  // TODO doesn't this more properly belong in Sequence class?
  public long gentime = -1;

  // How long it took to execute this sequence in nanoseconds,
  // excluding generation time.
  // Must be directly set by the generator that creates this object.
  // (no code in this class sets its value).
  public long exectime = -1;

  /** Output buffer used to capture the output from the executed sequence**/
  private static ByteArrayOutputStream output_buffer
    = new ByteArrayOutputStream ();
  private static PrintStream ps_output_buffer = new PrintStream (output_buffer);

  // Re-initialize executionResults list.
  private void readObject(ObjectInputStream s) throws IOException,
  ClassNotFoundException {
    s.defaultReadObject();
    // customized deserialization code
    this.executionResults = new Execution(sequence);
  }

  /** Create an executable sequence that executes the given sequence. */
  public ExecutableSequence(Sequence sequence) {
    this.sequence = sequence;
    this.checks = new ArrayList<List<Check>>(sequence.size());
    this.checksResults = new ArrayList<List<Boolean>>(sequence.size());
    for (int i = 0 ; i < this.sequence.size() ; i++) {
      this.checks.add(new ArrayList<Check>(1));
      this.checksResults.add(new ArrayList<Boolean>(1));
    }
    this.executionResults = new Execution(sequence);
  }

  /**
   * Create an executable sequence directly using the given arguments.
   *
   * Don't use this constructor! (Unless you know what you're doing.)
   */
  public ExecutableSequence(Sequence sequence,
      Execution exec, List<List<Check>> checks) {
    this.sequence = sequence;
    this.executionResults = exec;
    this.checks = checks;
  }

  /** Get the checks for the i-th element of the sequence. */
  public List<Check> getChecks(int i) {
    sequence.checkIndex(i);
    return checks.get(i);
  }

  /**
   * Adds the given check to the i-th element of the sequence. Only one
   * check of class StatementThrowsException is allowed for each index,
   * and attempting to add a second check of this type will result in an
   * IllegalArgumentException.
   *
   * @throws IllegalArgumentException
   *           If the given check's class is StatementThrowsException and
   *           there is already an check of this class at the give index.
   */
  public void addCheck(int i, Check check, boolean passed) {
    sequence.checkIndex(i);

    if (check instanceof ExpectedExceptionCheck &&
        hasCheck(i, ExpectedExceptionCheck.class))
      throw new IllegalArgumentException("Sequence already has a check"
          + " of type " + ExpectedExceptionCheck.class.toString());

    this.checks.get(i).add(check);
    this.checksResults.get(i).add(passed);
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0 ; i < sequence.size() ; i++) {
      sequence.printStatement(b, i);
      if (executionResults.size() > i)
        b.append(executionResults.get(i).toString());
      List<Check> cks = getChecks(i);
      List<Boolean> ckres = checksResults.get(i);
      for (int j = 0 ; j < cks.size() ; j++) {
        b.append (Globals.lineSep);
        b.append(cks.get(j).toString());
        b.append(" : ");
        b.append(ckres.get(j).toString());
      }
      b.append(Globals.lineSep);
    }
    return b.toString();
  }

  /*package-accessible*/
  static boolean canUseShortFormat(StatementKind statementCreatingVar) {
    return (statementCreatingVar instanceof PrimitiveOrStringOrNullDecl
            // Do not use the short output format if the value is null, because
            // the variable type may disambiguate among overloaded methods.
            // (It would be even nicer to use the short output format unless
            // disambiguation is truly needed.)
            && ((PrimitiveOrStringOrNullDecl) statementCreatingVar).getValue() != null);
  }

  /** 
   * Returns an id based on the contents of the sequence alone.  The id
   * will match for any identical sequences (no matter how they are
   * constructed).  Used for debugging
   **/
  public int seq_id() {
    StringBuilder b = new StringBuilder();
    for (int i = 0 ; i < sequence.size() ; i++) {
      
      // If short format, don't print out primitive declarations
      // because primitive values will be directly added to methods
      // (e.g. "foo(3)" instead of "int x = 3 ; foo(x)".
      if (!GenInputsAbstract.long_format
          && ExecutableSequence.canUseShortFormat(sequence.getStatementKind(i))) {
        continue;
      }
      
      StringBuilder oneStatement = new StringBuilder();
      sequence.printStatement(oneStatement, i);

      b.append(oneStatement);
    }
    return b.toString().hashCode();
  }

  /**
   * Output this sequence as code. In addition to printing out the statements,
   * this method prints the checks.
   *
   * If for a given statement there is an check of type
   * StatementThrowsException, that check's pre-statement code is printed
   * immediately before the statement, and its post-statement code is printed
   * immediately after the statement.
   */
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0 ; i < sequence.size() ; i++) {
      
      // If short format, don't print out primitive declarations
      // because primitive values will be directly added to methods
      // (e.g. "foo(3)" instead of "int x = 3 ; foo(x)".
      if (!GenInputsAbstract.long_format
          && ExecutableSequence.canUseShortFormat(sequence.getStatementKind(i))) {
        continue;
      }
      
      StringBuilder oneStatement = new StringBuilder();
      sequence.printStatement(oneStatement, i);

      // Print exception check first, if present.
      List<Check> exObs = getChecks(i, ExpectedExceptionCheck.class);
      if (!exObs.isEmpty()) {
        assert exObs.size() == 1 : toString();
        Check o = exObs.get(0);
        oneStatement.insert(0, o.toCodeStringPreStatement());
        oneStatement.append(o.toCodeStringPostStatement());
        oneStatement.append(Globals.lineSep);
      }

      // Print the rest of the checks.
      for (Check d : getChecks(i)) {
        if (d instanceof ExpectedExceptionCheck)
          continue;
        oneStatement.insert(0, d.toCodeStringPreStatement());
        oneStatement.append(d.toCodeStringPostStatement());
        oneStatement.append(Globals.lineSep);
      }
      b.append(oneStatement);
    }
    return b.toString(); // + "/*" + this.toString() + "*/";
  }

  /**
   * Executes sequence, stopping on exceptions
   */
  public void execute (ExecutionVisitor visitor) {
    execute (visitor, true);
  }

  /**
   * Execute this sequence, invoking the given visitor as the execution
   * unfolds. After invoking this method, the client can query the outcome
   * of executing each statement via the method <code>getResult(i)</code>.
   * 
   * <ul>
   * <li> Before the sequence is executed, clears execution results and
   *      calls <code>visitor.initialize(this)</code>.
   * <li> Executes each statement in the sequence. Before executing each statement
   *      calls the given visitor's <code>visitBefore</code> method. After executing
   *      each statement, calls the visitor's <code>visitAfter</code> method.
   * <li> Execution stops if one of the following conditions holds:
   *   <ul>
   *   <li> All statements in the sequences have been executed.
   *   <li> A statement's execution results in an exception and
   *        <code>stop_on_exception==true</code>.
   *   <li> A <code>null</code> input value is implicitly passed to the statement
            (i.e., not throught explicitly declaring like  x = null)
   *   <li> After executing the i-th statement and calling the visitor's
   *        <code>visitAfter</code> method, a <code>ContractViolation</code>
   *        check is present at index i.
   *   </ul>
   * </ul>
   *
   * @param visitor can be null, in which case no visitor will be invoked
   *        during execution.
   */
  public void execute(ExecutionVisitor visitor, boolean stop_on_exception) {

    // System.out.printf ("Executing sequence %s%n", this);
    if (visitor != null) {
      visitor.initialize(this);
    }

    executionResults.theList.clear();
    for (int i = 0 ; i < sequence.size() ; i++) {
      executionResults.theList.add(NotExecuted.create());
    }

    for (int i = 0 ; i < this.sequence.size() ; i++) {

      if (visitor != null) {
        visitor.visitBefore(this, i);
      }

      // Find and collect the input values to i-th statement.
      List<Variable> inputs = sequence.getInputs(i);
      Object[] inputVariables = new Object[inputs.size()];

      // If a null value is implicitly passed to the statement (e.g., to the receiver)
      // stop execution.
      if (!getRuntimeInputs(sequence, executionResults.theList, i, inputs, inputVariables))
        break;

      executeStatement(sequence, executionResults.theList, i, inputVariables);

      if (visitor != null) {
        visitor.visitAfter(this, i);
      }

      if (executionResults.get(i) instanceof ExceptionalExecution) {
        if (stop_on_exception)
          break;
      }

      if (hasFailure(i))
        break;
    }
  }

  public static boolean getRuntimeInputs(Sequence s, List<ExecutionOutcome> outcome,
      int i, List<Variable> inputs, Object[] runtimeObjects) {
    
    Object[] ros = getRuntimeValuesForVars(inputs, outcome);
    for (int ri = 0 ; ri < ros.length ; ri++) {
      runtimeObjects[ri] = ros[ri];
    }
    
    for (int ri = 0 ; ri < runtimeObjects.length ; ri++) {
      if (runtimeObjects[ri] == null) {
        int creatingStatementIdx = inputs.get(ri).getDeclIndex();
        StatementKind creatingStatement = s.getStatementKind(creatingStatementIdx);

        // If receiver position of a method, don't continue execution.
        if (ri == 0) {
          StatementKind st = s.getStatementKind(i);
          if (st instanceof RMethod && (!((RMethod)st).isStatic())) {
            return false;
          }
        }
        // If null value is implicitly passed (i.e. not passed from a
        // statement like "x = null;" don't continue execution.
        if (!(creatingStatement instanceof PrimitiveOrStringOrNullDecl)) {
          return false;
        }
      }
    }
    return true;
  }
  
  protected static Object[] getRuntimeValuesForVars(List<Variable> vars, List<ExecutionOutcome> execution) {    
    Object[] runtimeObjects = new Object[vars.size()];
    for (int j = 0 ; j < runtimeObjects.length ; j++) {
      int creatingStatementIdx = vars.get(j).getDeclIndex();
      assert execution.get(creatingStatementIdx) instanceof NormalExecution :
        execution.get(creatingStatementIdx).getClass();
      NormalExecution ne = (NormalExecution)execution.get(creatingStatementIdx);
      runtimeObjects[j] = ne.getRuntimeValue();
    }
    return runtimeObjects;
  }

  // Execute the index-th statement in the sequence.
  // Precondition: this method has been invoked on 0..index-1.
  public static void executeStatement(Sequence s, List<ExecutionOutcome> outcome,
      int index, Object[] inputVariables) {
    StatementKind statement = s.getStatementKind(index);

    // Capture any output  Syncronize with ProgressDisplay so that
    // we don't capture its output as well.
    synchronized (ProgressDisplay.print_synchro) {
      PrintStream orig_out = System.out;
      PrintStream orig_err = System.err;
      if (GenInputsAbstract.capture_output) {
        System.out.flush();
        System.err.flush();
        System.setOut (ps_output_buffer);
        System.setErr (ps_output_buffer);
      }
      
      assert ((statement instanceof RMethod && !((RMethod)statement).isStatic()) ? inputVariables[0] != null : true);
      
      ExecutionOutcome r = statement.execute(inputVariables, Globals.blackHole);
      assert r != null;
      if (GenInputsAbstract.capture_output) {
        System.setOut (orig_out);
        System.setErr (orig_err);
        r.set_output (output_buffer.toString());
        output_buffer.reset();
      }
      outcome.set(index, r);
    }
  }

  /**
   * This method is typically used by ExecutionVisitors.
   *
   * The result of executing the i-th element of the sequence.
   */
  public ExecutionOutcome getResult(int index) {
    sequence.checkIndex(index);
    return executionResults.get(index);
  }
  
  public List<List<Boolean>> getChecksResults() {
    return checksResults;
  }

  /**
   *
   * @return all the execution outcomes for this sequence.
   */
  public ExecutionOutcome[] getAllResults() {
    ExecutionOutcome[] ret = new ExecutionOutcome[executionResults.size()];
    for (int i = 0 ; i < executionResults.size() ; i++) {
      ret[i] = executionResults.get(i);
    }
    return ret;
  }

  /**
   * @return the number of elements in the sequence that were executed before
   * an execution result of type randoop.NotExecuted.
   */
  public int executedSize() {
    int count = 0;
    for ( ; count < executionResults.size(); count++) {
      if (executionResults.get(count) instanceof NotExecuted) {
        break;
      }
    }
    return count;
  }

  /**
   * This method is typically used by ExecutionVisitors.
   *
   * True if execution of the i-th statement terminated normally.
   */
  public boolean isNormalExecution(int i) {
    sequence.checkIndex(i);
    return getResult(i) instanceof NormalExecution;
  }

  public boolean isNormalExecution() {
    for (int i = 0 ; i < this.sequence.size() ; i++) {
      if (!isNormalExecution(i))
        return false;
    }
    return true;
  }

  public List<Check> getChecks(int i, Class<? extends Check> clazz) {
    sequence.checkIndex(i);
    List<Check> matchingObs = new ArrayList<Check>();
    for (Check d : checks.get(i)) {
      if (Reflection.canBeUsedAs(d.getClass(), clazz)) {
        matchingObs.add(d);
      }
    }
    return matchingObs;
  }

  public void removeChecks() {
    for (int i = 0 ; i < checks.size() ; i++) {
      checks.get(i).clear();
    }
  }
  
  


  public boolean hasCheck(Class<? extends Check> clazz) {
    for (int i = 0 ; i < sequence.size() ; i++) {
      if (hasCheck(i, clazz))
        return true;
    }
    return false;
  }

  /**
   * @return the first index at which an check of the given type occurs,
   *         or -1 if there is no check of the given type in this
   *         sequence.
   */
  public int getContractCheckIndex(Class<? extends Check> clazz) {
    for (int i = 0 ; i < sequence.size() ; i++) {
      if (hasCheck(i, clazz))
        return i;
    }
    return -1;
  }

  /** True iff this sequences has at least one check of the given type at index i. */
  public boolean hasCheck(int i, Class<? extends Check> clazz) {
    sequence.checkIndex(i);
    for (Check d : checks.get(i)) {
      if (Reflection.canBeUsedAs(d.getClass(), clazz)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasChecks(int i) {
    sequence.checkIndex(i);
    return checks.get(i).size() > 0;
  }

  public boolean hasFailure(int i) {
    for (boolean b : checksResults.get(i)) {
      if (!b) {
        return true;
      }
    }
    return false;
  }
  
  public boolean hasFailure() {
    for (int i = 0 ; i < sequence.size() ; i++) {
      if (hasFailure(i)) {
        return true;
      }
    }
    return false;
  }
  
  public List<Check> getFailures(int idx) {
    List<Check> failedContracts = new ArrayList<Check>();
    for (int i = 0 ; i < checksResults.get(idx).size() ; i++) {
      if (!checksResults.get(idx).get(i)) {
        failedContracts.add(checks.get(idx).get(i));
      }
    }
    return failedContracts;
  }

  public int getFailureIndex() {
    for (int i = 0 ; i < sequence.size() ; i++) {
      if (hasFailure(i)) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * @return The index in the sequence at which an exception of the
   * given class (or a class compatible with it) was thrown. If no such
   * exception, returns -1.
   */
  public int getExceptionIndex(Class<?> exceptionClass) {
    if (exceptionClass == null) throw new IllegalArgumentException("exceptionClass<?> cannot be null");
    for (int i = 0 ; i < this.sequence.size() ; i++)
      if ((getResult(i) instanceof ExceptionalExecution)) {
        ExceptionalExecution e = (ExceptionalExecution)getResult(i);
        if (Reflection.canBeUsedAs(e.getException().getClass(), exceptionClass))
          return i;
      }
    return -1;
  }

  /**
   * @return True if an exception of the given class (or a class compatible with
   *         it) has been thrown during this sequence's execution.
   */
  public boolean throwsException(Class<?> exceptionClass) {
    return getExceptionIndex(exceptionClass) >= 0;
  }

  /**
   *
   * @return True if an exception has been thrown during this sequence's execution.
   */
  public boolean throwsException() {
    for (int i = 0 ; i < this.sequence.size() ; i++)
      if (getResult(i) instanceof ExceptionalExecution)
        return true;
    return false;
  }

  public boolean hasNonExecutedStatements() {
    // Starting from the end of the sequence is always faster to find non-executed statements.
    for (int i = this.sequence.size() - 1 ; i >= 0 ; i--)
      if (getResult(i) instanceof NotExecuted)
        return true;
    return false;
  }

  public ExecutableSequence duplicate() {
    ExecutableSequence newSequence = new ExecutableSequence(this.sequence);
    // Add checks
    for (int i = 0 ; i < newSequence.sequence.size() ; i++) {
      newSequence.checks.get(i).addAll(getChecks(i));
    }
    return newSequence;
  }

  /**
   * Returns the index i for which this.isExceptionalExecution(i), or -1 if
   * there is no such index.
   */
  public int exceptionIndex() {
    if (!throwsException())
      throw new RuntimeException("Execution does not throw an exception");
    for (int i = 0; i < this.sequence.size(); i++) {
      if (this.getResult(i) instanceof ExceptionalExecution) {
        return i;
      }
    }
    return -1;
  }

  public static <D extends Check> List<Sequence> getSequences(List<ExecutableSequence> exec) {
    List<Sequence> result= new ArrayList<Sequence>(exec.size());
    for (ExecutableSequence execSeq : exec) {
      result.add(execSeq.sequence);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return sequence.hashCode() * 3 +
    checks.hashCode() * 5;
    // results are not part of this because they contain actual runtime objects. XXX is that bogus?
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof ExecutableSequence))
      return false;
    ExecutableSequence that= (ExecutableSequence)obj;
    if (! this.sequence.equals(that.sequence))
      return false;

    if (! this.checks.equals(that.checks))
      return false;

    // results are not part of this because they contain actual runtime objects. XXX is that bogus?

    return true;
  }

  /**
   * Compares the results of the checks of two sequences (the code in
   * each sequence should be the same).  Returns the number of
   * different checks.  If remove_diffs is true any differing checks
   * are removed from both sequences.  Checks found in one sequence
   * but not the other are also removed.  Prints any differences to
   * stdout if print_diffs is true.
   *
   * Different checks can be generated for two runs of the same test
   * sequence because of changes in
   * the environment (eg, static variables) between runs.  Some
   * checks are only created when the value involved in the
   * check meets certain criteria and since the values can
   * change, the checks may not always be created.  Its
   * also possible to throw exceptions in one run and not another.
   *
   * The get_id() method from the Check
   * interface) is used to identify check uniquely.  The id contains
   * both the observer type (primitive, non-null, observer method call,
   * etc) and the related variables.  When comparing two lists of
   * Checks, the code presumes that the checks will occur in the same
   * order on each execution.  Thus if the first remaining check in
   * the first execution matches the second check from the second execution,
   * the first check from the second execution is presumed to not match.
   * The algorithm is far from completely optimized, but it should work
   * pretty well if the two lists are pretty much the same (which
   * they usually are).
   */
  public int compare_checks (ExecutableSequence es, boolean remove_diffs,
                                   boolean print_diffs) {

    int cnt = 0;
    List<Integer> diff_obs1 = new ArrayList<Integer>();
    List<Integer> diff_obs2 = new ArrayList<Integer>();

    for (int ii = 0; ii < checks.size(); ii++) {
      // System.out.printf ("Sequence1: %n%s%n", this);
      // System.out.printf ("Sequence2: %n%s%n", es);
      List<Check> obs1 = checks.get(ii);
      List<Check> obs2 = es.checks.get(ii);
      // System.out.printf ("checks 1/%d = %s%n", ii, obs1);
      // System.out.printf ("checks 2/%d = %s%n", ii, obs2);
      boolean debug = false;
      if (false) {
        if (obs1.size() != obs2.size()) {
          if ((ii < (checks.size()-1))
              && (obs1.size() == 0) && (obs2.size() == 1)) {
            System.out.printf ("keeping mismatched check %s%n", obs2);
          } else { // number of checks must match
            System.out.printf ("obs %d size mismatch %d - %d\n", ii,obs1.size(),
                               obs2.size());
            System.out.printf ("Sequence1: %n%s%n", this);
            System.out.printf ("Sequence2: %n%s%n", es);
            System.out.printf ("Sequence1 [%08x]: %n%s%n", seq_id(), 
                               toCodeString());
            System.out.printf ("Sequence2 [%08x]: %n%s%n", seq_id(),
                               es.toCodeString());
            System.out.printf ("obs1: %s%n", obs1);
            System.out.printf ("obs2: %s%n", obs2);
            debug = true;
            // assert false;
          }
        }
      }

      // Find any checks that mismatch between the two runs of the
      // Sequence.  
      int o2i = 0;
      for (int o1i = 0; o1i < obs1.size(); o1i++) {
        Check ob1 = obs1.get(o1i);
        if (o2i >= obs2.size()) {
          diff_obs1.add (0, o1i);
          print_diffs (print_diffs, "extra obs1", ob1, null, o1i, -1, es);
        }
        boolean match = false;
        int o2i_start = o2i;
        while (o2i < obs2.size()) {
          Check ob2 = obs2.get(o2i);
          if (ob1.get_id().equals (ob2.get_id())) {
            assert ob1.getClass().equals (ob2.getClass());
            if (!ob1.get_value().equals (ob2.get_value())) {
              diff_obs1.add (0, o1i);
              diff_obs2.add (0, o2i);
              cnt++;
              print_diffs (print_diffs, "mismatch", ob1, ob2, o1i, o2i, es);
            } else { // values match
              if (debug)
                print_diffs (print_diffs, "match", ob1, ob2, o1i, o2i, null);
            }
            match = true;
            break;
          } else { // Different checks
            o2i++;
          }
        }
        // If we found a match any skipped checks in obs2 must be non-matched
        if (match) {
          for (int jj = o2i_start; jj < o2i; jj++) {
            diff_obs2.add (0, jj);
            print_diffs (print_diffs, "obs2 missing", null, obs2.get(jj), o1i, 
                         o2i, es);
          }
          o2i++;
        } else { // no match for obs1
          diff_obs1.add (0, o1i);
          print_diffs (print_diffs, "obs1 missing", ob1, null, o1i, -1, es);
          o2i = o2i_start;
        }
      }
      for ( ; o2i < obs2.size(); o2i++) {
        diff_obs2.add (0, o2i);
        print_diffs (print_diffs, "extra obs2", null, obs2.get(o2i), -1, o2i,
                     es);
      }
            
      // Remove any checks that don't match
      if (remove_diffs) {
        for (int obs : diff_obs1) {
          obs1.remove (obs);
        }
        for (int obs : diff_obs2) {
          obs2.remove (obs);
        }
      }
    }

    return diff_obs2.size() + diff_obs1.size() - cnt;

  }

  /** Prints information about any diffs found when comparing checks **/
  private void print_diffs (boolean print_diffs, String msg, Check obs1, 
                         Check obs2, int o1i, int o2i, ExecutableSequence es) {

    if (!print_diffs)
      return;

    System.out.printf ("Difference in seq %s%n", msg);
    System.out.printf ("obs1 @%d = %s%n", o1i, obs1);
    System.out.printf ("obs2 @%d = %s%n", o2i, obs2);
    if (es != null) {
      System.out.printf ("Seq 1 [%08X]%n%s%n", seq_id(), toCodeString());
      System.out.printf ("Seq 2 [%08X]%n%s%n", es.seq_id(), es.toCodeString());
    }
  }

  /**
   * Return the total number of checks in a list of sequences
   */
  public static int checks_count (List<ExecutableSequence> seqs) {

    int cnt = 0;

    for (ExecutableSequence es : seqs) {
      for (List<Check> obs : es.checks) {
        cnt += obs.size();
      }
    }

    return cnt;
  }
}
