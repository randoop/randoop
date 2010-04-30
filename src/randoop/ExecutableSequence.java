package randoop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.*;

import randoop.Sequence.RelativeNegativeIndex;
import randoop.util.ArrayListSimpleList;
import randoop.util.ProgressDisplay;
import randoop.util.Reflection;
import randoop.main.GenInputsAbstract;

/**
 * An ExecutableSequence adds two functionalities to a Sequence:
 *
 * (1) The ability to execute the code that the sequence represents.
 * (2) Observations can be added to elements in the sequence.
 *
 * The two are related--keep reading.
 *
 * Method execute(ExecutionVisitor v) executes the code that the sequence represents.
 * This method uses reflection to execute each element in the sequence (method call,
 * constructor call, primitive or array declaration, etc).
 *
 * Before executing each statement (e.g. the i-th statement), execute(v)
 * calls v.visitBefore(this, i), and after executing each statement, it calls
 * v.visitAfter(this, i). The purpose of the visitor is to examine the unfolding
 * execution, and take some action depending on its intended purpose. For example,
 * it may decorate the sequence with observations about the
 * execution. Below are some examples.
 *
 * <ul>
 * <li> A ToStringVisitor calls val.toString() on each value created during
 *      execution. and adds observations indicating the result of each call. For
 *      example, consider executing the sequence
 *
 *      ArrayList var0 = new ArrayList();
 *      int var1 = 3;
 *      var0.add(var1)  ;
 *
 *      After executing the sequence with a ToStringVisitor, the sequence contains
 *      the following observations:
 *
 *      observations at index 0: var0.String()=="[]"
 *      observations at index 1: var0.String()=="[]", var1.toString()=="3"
 *      observations at index 2: var0.String()=="[3]", var1.toString()=="3"
 *
 * <li> A ContractCheckingVisitor v adds observations that represent contract violations.
 *      For example, when v.visitAfter(this, i) is invoked, this visitor checks
 *      (among other things) that for every Variable val, "val.equals(val)==true". If
 *      this property fails for some val, it adds a observation (at index i)
 *      that records the failure.
 * </ul>
 *
 * NOTES
 *
 * <ul>
 * <li> It only makes sense to call the following methods *after* executing the
 * i-th statement in a sequence:
 *    <ul>
 *        <li> isNormalExecution(i)
 *        <li> isExceptionalExecution(i)
 *        <li> getExecutionResult(i)
 *        <li> getResult(i)
 *        <li> getExecption(i)
 *    </ul>
 * </ul>
 *
 */
public class ExecutableSequence implements Serializable {

  private static final long serialVersionUID = 2337273514619824184L;

  // The underlying sequence.
  public Sequence sequence;

  // The i-th element of this list contains the observations for the i-th
  // sequence element. Invariant: sequence.size() == observations.size().
  protected List<List<Observation>> observations;

  // Container for putting the results of the execution: values created (the
  // actual values, created via reflection) and exceptions thrown.
  // Invariant: Invariant: sequence.size() == executionResults.size().
  // Transient because it can contain arbitrary objects that may not be
  // serializable.
  protected transient /*final*/ Execution executionResults;

  // How long it took to generate this sequence in nanoseconds,
  // excluding execution time.
  // Must be directly set by the generator that creates this object
  // (no code in this class sets its value).
  public long gentime = -1;

  // How long it took to execute this sequence in nanoseconds,
  // excluding generation time.
  // Must be directly set by the generator that creates this object.
  // (no code in this class sets its value).
  public long exectime = -1;

  /** Output buffer used to capture the output from the executed sequence**/
  private static ByteArrayOutputStream output_buffer
    = new ByteArrayOutputStream (1024);
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
    this.observations = new ArrayList<List<Observation>>();
    for (int i = 0 ; i < this.sequence.size() ; i++) {
      this.observations.add(new ArrayList<Observation>(1));
    }
    this.executionResults = new Execution(sequence);
  }

  /**
   * Create an executable sequence directly using the given arguments.
   *
   * Don't use this constructor! (Unless you know what you're doing.)
   */
  protected ExecutableSequence(Sequence sequence,
      Execution exec, List<List<Observation>> observations) {
    this.sequence = sequence;
    this.executionResults = exec;
    this.observations = observations;
  }

  /** Get the observations for the i-th element of the sequence. */
  public List<Observation> getObservations(int i) {
    sequence.checkIndex(i);
    return observations.get(i);
  }

  /**
   * Adds the given observation to the i-th element of the sequence. Only one
   * observation of class StatementThrowsException is allowed for each index,
   * and attempting to add a second observation of this type will result in an
   * IllegalArgumentException.
   *
   * @throws IllegalArgumentException
   *           If the given observation's class is StatementThrowsException and
   *           there is already an observation of this class at the give index.
   */
  public void addObservation(int i, Observation observation) {
    sequence.checkIndex(i);

    if (observation instanceof StatementThrowsException &&
        hasObservation(i, StatementThrowsException.class))
      throw new IllegalArgumentException("Sequence already has an observation"
          + " of type " + StatementThrowsException.class.toString());

    this.observations.get(i).add(observation);
  }

  /**
   * Add the given observations to the i-th element of the sequence.
   * Equivalent to multiple invocations of addObservation(int,Observation).
   * See also documentation for that method.
   */
  public void addObservations(int i, Collection<? extends Observation> ds) {
    for (Observation d : ds) {
      addObservation(i, d);
    }
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0 ; i < sequence.size() ; i++) {
      sequence.printStatement(b, i);
      if (executionResults.size() > i)
        b.append(executionResults.get(i).toString());
      for (Observation d : getObservations(i)) {
        b.append (Globals.lineSep);
        b.append(d.toString());
      }
      b.append(Globals.lineSep);
    }
    return b.toString();
  }

  /**
   * Output this sequence as code. In addition to printing out the statements,
   * this method prints the observations.
   *
   * If for a given statement there is an observation of type
   * StatementThrowsException, that observation's pre-statement code is printed
   * immediately before the statement, and its post-statement code is printed
   * immediately after the statement.
   */
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0 ; i < sequence.size() ; i++) {
      StringBuilder oneStatement = new StringBuilder();
      sequence.printStatement(oneStatement, i);

      // Print exception observation first, if present.
      List<Observation> exObs = getObservations(i, StatementThrowsException.class);
      if (!exObs.isEmpty()) {
        assert exObs.size() == 1 : toString();
        Observation o = exObs.get(0);
        oneStatement.insert(0, o.toCodeStringPreStatement());
        oneStatement.append(o.toCodeStringPostStatement());
        oneStatement.append(Globals.lineSep);
      }

      // Print the rest of the observations.
      for (Observation d : getObservations(i)) {
        if (d instanceof StatementThrowsException)
          continue;
        oneStatement.insert(0, d.toCodeStringPreStatement());
        oneStatement.append(d.toCodeStringPostStatement());
        oneStatement.append(Globals.lineSep);
      }
      b.append(oneStatement);
    }
    return b.toString();
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
   * of executing each statement via the method getResult(i)
   *
   * @param visitor can be null, in which case no visitor will be invoked
   *        during execution.
   */
  public void execute(ExecutionVisitor visitor, boolean stop_on_exception) {

    // System.out.printf ("Executing sequence %s%n", this);

    executionResults.theList.clear();
    observations.clear();
    for (int i = 0 ; i < sequence.size() ; i++) {
      executionResults.theList.add(NotExecuted.create());
      observations.add(new ArrayList<Observation>(1));
    }

    for (int i = 0 ; i < this.sequence.size() ; i++) {

      if (visitor != null)
        visitor.visitBefore(this, i);

      // Find and collect the input values to i-th statement.
      List<Variable> inputs = sequence.getInputs(i);
      Object[] inputVariables = new Object[inputs.size()];

      if (!getRuntimeInputs(sequence, executionResults.theList, i, inputs, inputVariables))
        break;

      executeStatement(sequence, executionResults.theList, i, inputVariables);

      if (visitor != null) {
        visitor.visitAfter(this, i);
        // System.out.printf ("%d observations for sequence %d/%d%n",
        //                    this.observations.get(i).size(), i,
        //                    this.observations.size());
      }

      if (executionResults.get(i) instanceof ExceptionalExecution) {
        // Debug print generated exceptions
        if (false) {
          ExceptionalExecution ee
            = (ExceptionalExecution) executionResults.get(i);
          Throwable t = ee.getException();
          System.out.printf ("Exception %s from stateent %s%n", t,
                             sequence.getStatementKind(i));
          t.printStackTrace();
        }
        if (stop_on_exception)
          break;
      }

      if (hasObservation(i, ContractViolation.class))
        break;
    }
  }

  protected static boolean getRuntimeInputs(Sequence s, List<ExecutionOutcome> outcome,
      int i, List<Variable> inputs, Object[] inputVariables) {
    assert s.size() == outcome.size();
    for (int j = 0 ; j < inputVariables.length ; j++) {
      int creatingStatementIdx = inputs.get(j).getDeclIndex();
      assert outcome.get(creatingStatementIdx) instanceof NormalExecution :
        outcome.get(creatingStatementIdx).getClass();
      NormalExecution ne = (NormalExecution)outcome.get(creatingStatementIdx);
      inputVariables[j] = ne.getRuntimeValue();

      // If null value and not explicity null, stop execution.
      if (inputVariables[j] == null) {

        StatementKind creatingStatement = s.getStatementKind(creatingStatementIdx);

        // If receiver position of a method, don't continue execution.
        if (j == 0) {
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

  // Execute the index-th statement in the sequence.
  // Precondition: this method has been invoked on 0..index-1.
  protected static void executeStatement(Sequence s, List<ExecutionOutcome> outcome,
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

  /**
   * Returns true if there was an exception on any statement other than
   * the last statement.  Exceptions within a sequence are unexpected, because
   * Randoop only builds on sequences that don't throw exceptions.  They
   * can happen in some cases, though, when changes to the global state
   * cause an existing sequence to throw an exception early
   */
  public boolean hasUnexpectedException() {
    for (int i = 0 ; i < (this.sequence.size()-1) ; i++) {
      if (!isNormalExecution(i))
        return true;
    }
    return false;
  }

  public List<Observation> getObservations(int i, Class<? extends Observation> clazz) {
    sequence.checkIndex(i);
    List<Observation> matchingObs = new ArrayList<Observation>();
    for (Observation d : observations.get(i)) {
      if (Reflection.canBeUsedAs(d.getClass(), clazz)) {
        matchingObs.add(d);
      }
    }
    return matchingObs;
  }

  /**
   * Remove all the observations at index i that are of type clazz
   * (or a subtype).
   */
  private void removeObservations(int i) {
    sequence.checkIndex(i);
    this.observations.set(i, new ArrayList<Observation>(1));
  }


  public boolean hasObservation(Class<? extends Observation> clazz) {
    for (int i = 0 ; i < sequence.size() ; i++) {
      if (hasObservation(i, clazz))
        return true;
    }
    return false;
  }

  /**
   * @return the first index at which an observation of the given type occurs,
   *         or -1 if there is no observation of the given type in this
   *         sequence.
   */
  public int getObservationIndex(Class<? extends Observation> clazz) {
    for (int i = 0 ; i < sequence.size() ; i++) {
      if (hasObservation(i, clazz))
        return i;
    }
    return -1;
  }

  /** True iff this sequences has at least one observation of the given type at index i. */
  public boolean hasObservation(int i, Class<? extends Observation> clazz) {
    sequence.checkIndex(i);
    for (Observation d : observations.get(i)) {
      if (Reflection.canBeUsedAs(d.getClass(), clazz)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasObservation(int i) {
    sequence.checkIndex(i);
    return observations.get(i).size() > 0;
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
    // Add observations
    for (int i = 0 ; i < newSequence.sequence.size() ; i++) {
      newSequence.observations.get(i).addAll(getObservations(i));
    }
    return newSequence;
  }

  // TODO Document better. This is only used by minimizer; perhaps more there?
  /**
   * Returns a new ExecutableSequence that is the same as this, except that
   * at the given index, it has the given statement/inputs, and has no observations.
   */
  public void replaceStatement(StatementKind statement, List<Variable> inputs, int index) {
    sequence.checkIndex(index);

    // Create the new Sequence.
    Sequence newSequence = new Sequence();
    for (int i = 0 ; i < this.sequence.size() ; i++) {
      if (i == index) {
        newSequence = newSequence.extend(statement, mapVals(newSequence, inputs));
      } else {
        newSequence = newSequence.extend(sequence.getStatementKind(i), mapVals(newSequence, sequence.getInputs(i)));
      }
    }
    this.sequence = newSequence;
    removeObservations(index);
  }

  // TODO Document. This is only used by minimizer; perhaps more there?
  private List<Variable> mapVals(Sequence newSequence, List<Variable> inputs) {
    List<Variable> ret = new ArrayList<Variable>(inputs.size());
    for (Variable v : inputs)
      ret.add(newSequence.getVariable(v.getDeclIndex()));
    return ret;
  }

  // TODO Document. This is only used by minimizer; perhaps more there?
  public final boolean canRemoveStatement(int statementIndex) {
    sequence.checkIndex(statementIndex);
    Variable removedStatementVariable = sequence.getVariable(statementIndex);
    for (int i = statementIndex + 1 ; i < sequence.size() ; i++) {
      Statement currStatement = sequence.statements.get(i);
      for (RelativeNegativeIndex relIndex : currStatement.inputs) {
        if (sequence.getVariableForInput(i, relIndex).equals(removedStatementVariable)) {
          return false;
        }
      }
    }
    return true;
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

  // TODO Document. This is only used by minimizer; perhaps more there?
//  public final void removeStatement(int statementIndex) {
//    sequence.checkIndex(statementIndex);
//    if (!canRemoveStatement(statementIndex))
//      throw new IllegalArgumentException("cannot replace statement at index " + statementIndex +
//      " with a dummy statement, because its result is used later in the sequence.");
//
//    ArrayListSimpleList<Statement> newStatements = new ArrayListSimpleList<Statement>(sequence.size());
//
//    for (int i = 0 ; i < sequence.size() ; i++) {
//      if (i == statementIndex) {
//        newStatements.add(new Statement(new DummyStatement(), new ArrayList<RelativeNegativeIndex>(0)));
//      } else {
//        newStatements.add(sequence.statements.get(i));
//      }
//    }
//
//    this.sequence = new Sequence(newStatements);
//    this.removeObservations(statementIndex);
//  }

  public static <D extends Observation> List<Sequence> getSequences(List<ExecutableSequence> exec) {
    List<Sequence> result= new ArrayList<Sequence>(exec.size());
    for (ExecutableSequence execSeq : exec) {
      result.add(execSeq.sequence);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return sequence.hashCode() * 3 +
    observations.hashCode() * 5;
    //results are not part of this because they contain actual runtime objects. XXX is that bogus?
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof ExecutableSequence))
      return false;
    ExecutableSequence that= (ExecutableSequence)obj;
    if (! this.sequence.equals(that.sequence))
      return false;

    if (! this.observations.equals(that.observations))
      return false;

    //results are not part of this because they contain actual runtime objects. XXX is that bogus?

    return true;
  }

  /**
   * Compares the results of the observations of two sequences.  Returns the
   * number of different observations.  If remove_diffs is true any
   * differing observations are removed from both sequences.
   * Prints any differences to stdout if print_diffs is true.
   */
  public int compare_observations (ExecutableSequence es, boolean remove_diffs,
                                   boolean print_diffs){

    int cnt = 0;

    for (int ii = 0; ii < observations.size(); ii++) {
      // System.out.printf ("Sequence1: %n%s%n", this);
      // System.out.printf ("Sequence2: %n%s%n", es);
      List<Observation> obs1 = observations.get(ii);
      List<Observation> obs2 = es.observations.get(ii);
      List<Integer> diff_obs = new ArrayList<Integer>();
      // System.out.printf ("observations 1/%d = %s%n", ii, obs1);
      // System.out.printf ("observations 2/%d = %s%n", ii, obs2);
      if (obs1.size() != obs2.size()) {
        if ((ii < (observations.size()-1))
            && (obs1.size() == 0) && (obs2.size() == 1)) {
          System.out.printf ("keeping mismatched observation %s%n", obs2);
        } else { // number of observations must match
          System.out.printf ("obs %d size mismatch %d - %d\n", ii, obs1.size(),
                             obs2.size());
          System.out.printf ("Sequence1: %n%s%n", this);
          System.out.printf ("Sequence2: %n%s%n", es);
          System.out.printf ("Sequence1: %n%s%n", toCodeString());
          System.out.printf ("Sequence2: %n%s%n", es.toCodeString());
          System.out.printf ("obs1: %s%n", obs1);
          System.out.printf ("obs2: %s%n", obs2);
          assert false;
        }
      }
      for (int jj = 0; jj < obs1.size(); jj++) {
        Observation ob1 = obs1.get(jj);
        Observation ob2 = obs2.get(jj);
        if (!ob1.get_value().equals (ob2.get_value())) {
          diff_obs.add (0, jj);
          cnt++;
          if (print_diffs) {
            System.out.printf ("observation mismatch in seq [%b]%n%s%n",
                               remove_diffs, es);
            System.out.printf ("Line %d, obs %d%n", ii, jj);
            System.out.printf ("ob1 = %s, ob 2 = %s%n", ob1, ob2);
          }
        } else { // they match
          if (ob1.get_value().contains ("EquipmentHolder@")) {
            System.out.printf ("observation match in seq [%b]%n%s%n",
                               remove_diffs, es);
            System.out.printf ("Line %d, obs %d%n", ii, jj);
            System.out.printf ("ob1 = %s, ob 2 = %s%n", ob1, ob2);
            assert false;
          }
        }

      }

      // Remove any observations that don't match
      if (remove_diffs && (diff_obs.size() > 0)) {
        // System.out.printf ("obs1 size before = %d%n", obs1.size());
        for (int obs : diff_obs) {
          // System.out.printf ("Removing obs %d from sequence%n", obs);
          obs1.remove (obs);
          obs2.remove (obs);
        }
        // System.out.printf ("obs1 size after = %d%n", obs1.size());
      }
    }

    return cnt;

  }

  public String toDotString() {
    StringBuilder b = new StringBuilder();

    b.append("digraph G {\n");

    for (int i = 0 ; i < sequence.size() ; i++) {
      b.append("s" + i + " [color=" + getColor(i) + ",style=filled];\n");
    }

    for (int i = 0; i < sequence.size() ; i++) {
      StatementKind st = sequence.getStatementKind(i);
      List<Variable> inputs = sequence.getInputs(i);
      if (st instanceof PrimitiveOrStringOrNullDecl) {
        continue;
      }
      if (inputs.isEmpty()) {
        continue;
      }
      for (Variable input : inputs) {
        if (sequence.getStatementKind(input.getDeclIndex())
            instanceof PrimitiveOrStringOrNullDecl) {
          continue;
        }
        b.append("s" + sequence.lastUseBefore(i, input) + " -> " + "s" + i + ";\n");
      }
    }
    b.append("}\n");
    return b.toString();
  }

  private String getColor(int i) {
    ExecutionOutcome res = getResult(i);
    if (res instanceof NotExecuted) {
      return "white";
    } else if (res instanceof ExceptionalExecution) {
      return "yellow";
    } else {
      assert res instanceof NormalExecution;
      if (hasObservation(i, StatementThrowsNPE.class)) {
        return "brown";
      } else if (hasObservation(i, ExpressionEqFalse.class)) {
        return "red";
      } else {
        return "green";
      }
    }
  }

  /**
   * Return the total number of observations in a list of sequences
   */
  public static int observations_count (List<ExecutableSequence> seqs) {

    int cnt = 0;

    for (ExecutableSequence es : seqs) {
      for (List<Observation> obs : es.observations) {
        cnt += obs.size();
      }
    }

    return cnt;
  }


}
