package randoop.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.AbstractGenerator;
import randoop.Check;
import randoop.ComponentManager;
import randoop.DummyVisitor;
import randoop.ExceptionalExecution;
import randoop.ExecutableSequence;
import randoop.Execution;
import randoop.ExecutionOutcome;
import randoop.Globals;
import randoop.IStopper;
import randoop.ITestFilter;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.RandoopListenerManager;
import randoop.RandoopStat;
import randoop.SeedSequences;
import randoop.Sequence;
import randoop.SequenceCollection;
import randoop.Statement;
import randoop.SubTypeSet;
import randoop.Variable;
import randoop.main.GenInputsAbstract;
import randoop.operation.Operation;
import randoop.util.ArrayListSimpleList;
import randoop.util.CollectionsExt;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.OneMoreElementList;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.ReversibleMultiMap;
import randoop.util.ReversibleSet;
import randoop.util.SimpleList;
import randoop.util.Util;

import plume.Pair;

/**
 * Experimental code.
 */
public class RandomWalkGenerator extends AbstractGenerator {

  private List<Operation> initialEnabledStatements = new ArrayList<Operation>();
  private Map<Class<?>, Set<Operation>> initialMissingTypesToStatements = new LinkedHashMap<Class<?>, Set<Operation>>();
  private Map<Operation, Set<Class<?>>> initialInactiveStatements = new LinkedHashMap<Operation, Set<Class<?>>>();

  List<Operation> allStatements;

  private int numSeqs = 0;
  
  @RandoopStat("Number of resets")
  private int num_resets = 0;

  SubTypeSet availableTypes = new SubTypeSet(true);
  ReversibleMultiMap<Class<?>, Integer> typesToVals;
  ReversibleSet<Operation> enabledStatements;
  ReversibleMultiMap<Class<?>, Operation> missingTypesToStatements;
  ReversibleMultiMap<Operation, Class<?>> inactiveStatements;

  Sequence sequence;

  public static int normals = 0;
  public static int exceptions = 0;

  private List<ExecutionOutcome> exec;

  private List<List<Check>> obs;

  private long gentime;
  private long exectime;

  private static SequenceCollection prims = new SequenceCollection(SeedSequences.defaultSeeds());

  public RandomWalkGenerator(List<Operation> statements,
      long timeMillis, int maxSequences, ComponentManager componentMgr,
      IStopper stopper, RandoopListenerManager listenerManager, List<ITestFilter> fs) {
    super(statements, timeMillis, maxSequences, componentMgr, stopper, listenerManager, fs);

    this.allStatements = statements;

    initialEnabledStatements = new ArrayList<Operation>();
    initialMissingTypesToStatements = new LinkedHashMap<Class<?>, Set<Operation>>();
    initialInactiveStatements = new LinkedHashMap<Operation, Set<Class<?>>>();
    for (Operation st : allStatements) {
      List<Class<?>> inputTypes = new ArrayList<Class<?>>(st.getInputTypes());
      for (java.util.Iterator<Class<?>> it = inputTypes.iterator(); it
          .hasNext();) {
        Class<?> next = it.next();
        if (next.isPrimitive() || next.equals(String.class)) {
          it.remove();
        }
      }
      if (inputTypes.isEmpty()) {
        initialEnabledStatements.add(st);
        continue;
      }

      initialInactiveStatements
          .put(st, new LinkedHashSet<Class<?>>(inputTypes));
      for (Class<?> cls : inputTypes) {
        Set<Operation> s = initialMissingTypesToStatements.get(cls);
        if (s == null) {
          s = new LinkedHashSet<Operation>();
          initialMissingTypesToStatements.put(cls, s);
        }
        s.add(st);
      }
    }

    if (initialEnabledStatements.isEmpty())
      throw new IllegalArgumentException("No active statements.");

    resetState();
  }

  private void resetState() {

    num_resets++;

    sequence = new Sequence();
    exec = new ArrayList<ExecutionOutcome>();
    obs = new ArrayList<List<Check>>();

    availableTypes = new SubTypeSet(true);
    typesToVals = new ReversibleMultiMap<Class<?>, Integer>();
    sequence = new Sequence();
    enabledStatements = new ReversibleSet<Operation>();
    for (Operation stk : initialEnabledStatements) {
      enabledStatements.add(stk);
    }
    inactiveStatements = new ReversibleMultiMap<Operation, Class<?>>();
    for (Map.Entry<Operation, Set<Class<?>>> e : initialInactiveStatements
        .entrySet()) {
      for (Class<?> c2 : e.getValue()) {
        inactiveStatements.add(e.getKey(), c2);
      }
    }
    missingTypesToStatements = new ReversibleMultiMap<Class<?>, Operation>();
    for (Map.Entry<Class<?>, Set<Operation>> e : initialMissingTypesToStatements
        .entrySet())
      for (Operation stk : e.getValue()) {
        missingTypesToStatements.add(e.getKey(), stk);
      }

    gentime = 0;
    exectime = 0;
  }

  private Set<Class<?>> getInputTypesSet(Operation st) {
    return new LinkedHashSet<Class<?>>(st.getInputTypes());
  }

  @Override
  public ExecutableSequence step() {

    long startTime = System.nanoTime();

    assert sequence.size() < GenInputsAbstract.maxsize : sequence.size();

    if (GenInputsAbstract.debug_checks) {
      repInvariantCheck();
    }

    int oldsize = sequence.size();

    markState();

    extendRandomly();
    if (Log.isLoggingOn()) Log.logLine("EXTENDED SEQUENCE: " + sequence.toString());

    logState();

    numSeqs++;

    AbstractGenerator.currSeq = sequence;

    if (GenInputsAbstract.dontexecute) {
      // We may exceed seqence size because of primitive declarations.
      if (sequence.size() >= GenInputsAbstract.maxsize) {
        ExecutableSequence ret = new ExecutableSequence(sequence);
        ret.gentime = gentime;
        resetState();
        return ret;
      } else {
        update();
        return null;
      }
    }

    // We're done with generation.
    long stopTime = System.nanoTime();
    gentime += stopTime - startTime;
    startTime = stopTime; // Reset start time.

    ExecutableSequence eseq =
      new ExecutableSequence(sequence, new Execution(sequence, exec), obs);

    // First, may need to execute primitive declarations.
    for (int i = oldsize ; i < sequence.size() - 1 ; i++) {
      assert sequence.getStatement(i).isPrimitiveInitialization();
      executionVisitor.visitBefore(eseq, i);
      ExecutableSequence.executeStatement(sequence, exec, i, new Object[0]);
      executionVisitor.visitAfter(eseq, i);
      normals++;
    }

    // Execute the last statement.
    int last = sequence.size() - 1;
    executionVisitor.visitBefore(eseq, last);
    List<Variable> inputs = sequence.getInputs(last);
    Object[] inputVariables = new Object[inputs.size()];
    if (!ExecutableSequence.getRuntimeInputs(sequence, exec, last, inputs, inputVariables)) {
      if (!undoLastStep()) {
        resetState();
      }
     return null;
    }


    startTime = System.nanoTime();

    ExecutableSequence.executeStatement(sequence, exec, last, inputVariables);

    if (exec.get(last) instanceof ExceptionalExecution) {
      // We're done with execution.
      stopTime = System.nanoTime();
      exectime += stopTime - startTime;
      exceptions++;
    } else {
      assert (exec.get(last) instanceof NormalExecution);
      normals++;
    }


    executionVisitor.visitAfter(eseq, last);

    if (Log.isLoggingOn()) {
      Log.logLine("EXEC: ");
      for (ExecutionOutcome o : exec) {
        Log.logLine(o.toString());
      }
    }

    if (exec.get(last) instanceof ExceptionalExecution) {
      ExecutableSequence ret = new ExecutableSequence(sequence,
          new Execution(sequence, new ArrayList<ExecutionOutcome>(exec)),
          new ArrayList<List<Check>>(obs));
      ret.exectime = exectime;
      ret.gentime = gentime;
      resetState();
      return ret;
    }

    if (eseq.hasFailure()) {
      if (Log.isLoggingOn()) Log.logLine("@@@ CONTRACT VIOLATED");

      ExecutableSequence ret = new ExecutableSequence(sequence,
          new Execution(sequence, new ArrayList<ExecutionOutcome>(exec)),
          new ArrayList<List<Check>>(obs));
      ret.exectime = exectime;
      ret.gentime = gentime;
      resetState();
      return ret;
    }

    assert eseq.isNormalExecution();

    if (sequence.size() >= GenInputsAbstract.maxsize) {
      ExecutableSequence ret = new ExecutableSequence(sequence,
          new Execution(sequence, new ArrayList<ExecutionOutcome>(exec)),
          new ArrayList<List<Check>>(obs));
      ret.exectime = exectime;
      ret.gentime = gentime;
      resetState();
      return ret;
    }

    if (((NormalExecution)eseq.getResult(last)).getRuntimeValue() != null) {
      startTime = System.nanoTime();
      update();
      stopTime = System.nanoTime();
      gentime += stopTime - startTime;
    }

    return null;
  }

  private void update() {
    Class<?> retType = sequence.getLastStatement().getOutputType();

    // update availableTypes
    if (Log.isLoggingOn()) Log.logLine("START UPDATING AVAILABLE TYPES.");
    Log.logLine("TYPES WITH SEQS marks:\n\n" + ((ReversibleSet<Class<?>>)availableTypes.typesWithsequences).map.marks);
    Log.logLine("SUBTYPES WITH SEQS marks:\n\n" + ((ReversibleMultiMap<Class<?>, Class<?>>)availableTypes.subTypesWithsequences).marks);
    ReversibleMultiMap.verbose_log = true;
    availableTypes.add(retType);
    ReversibleMultiMap.verbose_log = false;
    Log.logLine("TYPES WITH SEQS marks:\n\n" + ((ReversibleSet<Class<?>>)availableTypes.typesWithsequences).map.marks);
    Log.logLine("SUBTYPES WITH SEQS marks:\n\n" + ((ReversibleMultiMap<Class<?>, Class<?>>)availableTypes.subTypesWithsequences).marks);
    if (Log.isLoggingOn()) Log.logLine("END UPDATING AVAILABLE TYPES.");

    // update typesToVals
    if (typesToVals.keySet().contains(retType)
    && typesToVals.getValues(retType).contains(sequence.getLastVariable().index)) {
      try {
        writeLog(null, false);
      } catch (IOException e) {
        throw new Error(e);
      }
      assert false;
    }

    typesToVals.add(retType, sequence.getLastVariable().index);

    // update missingTypesToStatements, activeStatements and
    // inactiveStatements.
    List<Pair<Class<?>, Operation>> pairsToRemove =
      new ArrayList<Pair<Class<?>, Operation>>();

    for (Class<?> c2 : missingTypesToStatements.keySet()) {
      Set<Operation> sts = missingTypesToStatements.getValues(c2);
      if (c2.isAssignableFrom(retType)) {
        for (Operation st2 : sts) {
          pairsToRemove.add(new Pair<Class<?>, Operation>(c2, st2));
          inactiveStatements.remove(st2, c2);
          if (!inactiveStatements.keySet().contains(st2)) {
            enabledStatements.add(st2);
          }
        }
      }
    }

    for (Pair<Class<?>, Operation> p : pairsToRemove) {
      missingTypesToStatements.remove(p.a, p.b);
    }

  }

  private void logState() {

    if (Log.isLoggingOn()) {
//       Log.logLine("AVAILABLE-TYPES:\n\n" + availableTypes.typesWithsequences);
//       Log.logLine("TYPES-TO-VALS:\n\n" + typesToVals);
//       Log.logLine("ENABLED STATEMENTS:\n\n" + enabledStatements);
//       Log.logLine("MISSING-TYPES-TO_STATEMENTS:\n\n" + missingTypesToStatements);
//       Log.logLine("INACTIVE-STATEMENTS:\n\n" + inactiveStatements);
    }
  }

  private boolean undoLastStep() {

    if (Log.isLoggingOn()) Log.logLine("UNDOING LAST STEP FOR SEQUENCE:\n\n" + sequence);

    availableTypes.undoLastStep();
    typesToVals.undoToLastMark();
    enabledStatements.undoToLastMark();
    missingTypesToStatements.undoToLastMark();
    inactiveStatements.undoToLastMark();

    // Remove last statement.
    removeLast();

    // There may be primitive declarations that fed into the last
    // statement; remove them also.
    while (sequence.size() > 0 && sequence.getLastStatement().isPrimitiveInitialization()) {
      removeLast();
    }

    // Finally, re-execute sequence to get back to previous state.
    ExecutableSequence eseq = new ExecutableSequence(sequence, new Execution(sequence, exec), obs);
    eseq.execute(new DummyVisitor());

    if (!eseq.isNormalExecution()) {
      return false;
    }

    logState();

    return true;
  }

  // Removes last statement, and also the last execution outcome and decoration list.
  private void removeLast() {

    assert sequence.statements instanceof OneMoreElementList<?>;
    OneMoreElementList<Statement> statements = (OneMoreElementList<Statement>) sequence.statements;

    sequence = new Sequence(statements.list);

    exec.remove(exec.size() - 1);
    obs.remove(obs.size() - 1);

    assert sequence.size() == exec.size();
    assert sequence.size() == obs.size();

  }

  private void markState() {
    availableTypes.mark();
    typesToVals.mark();
    enabledStatements.mark();
    missingTypesToStatements.mark();
    inactiveStatements.mark();
  }

  Set<Operation> errors = new LinkedHashSet<Operation>();

  private boolean extendRandomly() {

    Operation st = Randomness.randomSetMember(enabledStatements.getElements());

    if (Log.isLoggingOn()) Log.logLine("Selected statement: " + st);

    List<Integer> varsIndices = new ArrayList<Integer>();

    List<Variable> vars = new ArrayList<Variable>();

    for (Class<?> tc : st.getInputTypes()) {

      if (tc.isPrimitive() || tc.equals(String.class)) {
        //XXX why selecting from sequences when just selecting an Operation?
        Sequence news = Randomness.randomMember(prims.getSequencesForType(tc, true));
        assert news.size() == 1;
        //TODO make this select operation instead of sequence
        sequence = sequence.extend(news.getStatement(0), Collections.<Variable>emptyList());
        // Increase the size of exec, obs.
        exec.add(NotExecuted.create());
        obs.add(new ArrayList<Check>());

        varsIndices.add(sequence.getLastVariable().index);

        continue;
      }

      // Randomly select a type.

      List<SimpleList<Integer>> possibleVars = new ArrayList<SimpleList<Integer>>();
      List<Class<?>> possibleTypes = new ArrayList<Class<?>>(new LinkedHashSet<Class<?>>(availableTypes.getMatches(tc)));
      for (Class<?> possibleType : possibleTypes) {
        possibleVars.add(new ArrayListSimpleList<Integer>(
            new ArrayList<Integer>(typesToVals.getValues(possibleType))));
      }
      SimpleList<Integer> possible2 = new ListOfLists<Integer>(possibleVars);
      Integer chosenVal = possible2.get(Randomness.nextRandomInt(possible2.size()));

      vars.add(sequence.getVariable(chosenVal));

      varsIndices.add(sequence.getVariable(chosenVal).index);

    }

    List<Variable> inputs = new ArrayList<Variable>(varsIndices.size());
    for (Integer i : varsIndices) {
      inputs.add(sequence.getVariable(i));
    }
    sequence = sequence.extend(st, inputs);
    // Increase the size of exec, obs.
    exec.add(NotExecuted.create());
    obs.add(new ArrayList<Check>());

    return true;

    // repInvariantCheck();
  }

  @Override
  public int numSequences() {
    return numSeqs;
  }

  void repInvariantCheck() {
    try {
      repInvariantCheck2();
    } catch (Exception e) {
      try {
        writeLog(e, true);
      } catch (IOException e1) {
        System.out.println("Error while writing log:" + e1.getMessage());
      }
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings( { "unchecked" })
  void repInvariantCheck2() {

    Set<Operation> activeStatementsAsSet = new LinkedHashSet<Operation>(
        enabledStatements.getElements());

    // activeStatements is a subset of allStatements.
    assert allStatements.containsAll(activeStatementsAsSet);

    // activeStatements and inactiveStatements partition allStatements.
    assert CollectionsExt.intersection(activeStatementsAsSet,
        inactiveStatements.keySet()).isEmpty();
    Set<Operation> union = new LinkedHashSet<Operation>(
        enabledStatements.getElements());
    union.addAll(inactiveStatements.keySet());
    assert union.equals(new LinkedHashSet<Operation>(allStatements));

    // missingTypes and activeTypes are disjoint.
    assert CollectionsExt.intersection(availableTypes.getElements(),
        missingTypesToStatements.keySet()).isEmpty();

    // missingTypesToStatements and inactiveStatements: the collections of
    // missing classes is the same.
    Set<Class<?>> classSet1 = missingTypesToStatements.keySet();
    Set<Class<?>> classSet2 = new LinkedHashSet<Class<?>>();
    for (Operation sta : inactiveStatements.keySet()) {
      classSet2.addAll(inactiveStatements.getValues(sta));
    }
    assert classSet1.equals(classSet2) : classSet1 + "," + classSet2;

    // missingTypesToStatements and inactiveStatements: the collections of
    // inactive statements is the same.
    Set<Operation> stSet1 = inactiveStatements.keySet();
    Set<Operation> stSet2 = new LinkedHashSet<Operation>();
    for (Class<?> c2 : missingTypesToStatements.keySet()) {
      for (Operation s : missingTypesToStatements.getValues(c2)) {
        stSet2.add(s);
      }
    }
    assert stSet1.equals(stSet2);

    // A type in in availableTypes iff it is in typesToVals.
    typesToVals.keySet().equals(availableTypes.getElements());

    // (Without feedback only) Every value in the sequence is in exactly one
    // entry.
    Set<Integer> valuesSoFar = new LinkedHashSet<Integer>();
    for (Class<?> key : typesToVals.keySet()) {
    for (Integer v : typesToVals.getValues(key)) {
        assert !valuesSoFar.contains(v);
        valuesSoFar.add(v);
      }
    }

    // valuesSoFar contains all the variables
    for (int i = 0 ; i < sequence.size() ; i++) {
      if (sequence.getStatement(i).isPrimitiveInitialization())
        continue;
      assert valuesSoFar.contains(i) : i;
    }

    for (Class<?> c : typesToVals.keySet()) {
      assert c != null;
    }

    for (Class<?> c : missingTypesToStatements.keySet()) {
      assert c != null;
    }

    for (Operation st : inactiveStatements.keySet()) {
      assert st != null;
    }

    // TODO move this to repCheck for List<StatementKind>.
    for (Operation st : allStatements) {
      assert st != null;
    }

    for (Operation st : allStatements) {

      boolean isInActiveStatements = enabledStatements.contains(st);

      boolean isInInactiveStatements = inactiveStatements.keySet().contains(st);

      boolean hasAllNonPrimArgTypesInAvailableTypes = true;
      for (Class<?> c : getInputTypesSet(st)) {
        if (c.isPrimitive() || c.equals(String.class))
          continue;
        if (!availableTypes.containsAssignableType(c,
            Reflection.Match.COMPATIBLE_TYPE)) {
          hasAllNonPrimArgTypesInAvailableTypes = false;
          break;
        }
      }

      boolean hasSomeArgTypesInMissingTypes = !CollectionsExt.intersection(
          missingTypesToStatements.keySet(), getInputTypesSet(st)).isEmpty();

      // A statement is in the active set iff it has all its argument
      // types in the availableTypes set.
      assert Util.iff(isInActiveStatements, hasAllNonPrimArgTypesInAvailableTypes) : st
          .toString();
      assert Util.iff(isInActiveStatements, !hasSomeArgTypesInMissingTypes);

      assert Util.iff(isInInactiveStatements, !hasAllNonPrimArgTypesInAvailableTypes);
      assert Util.iff(isInInactiveStatements, hasSomeArgTypesInMissingTypes);

      Set<Class<?>> missingTypesForSt1 = inactiveStatements.getValues(st);
      if (missingTypesForSt1 == null)
        missingTypesForSt1 = new LinkedHashSet<Class<?>>();

      Set<Class<?>> missingTypesForSt2 = new LinkedHashSet<Class<?>>();
      for (Class<?> c3 : missingTypesToStatements.keySet()) {
        Set<Operation> sts = missingTypesToStatements.getValues(c3);
        if (sts.contains(st)) {
          missingTypesForSt2.add(c3);
        }
      }

      assert missingTypesForSt1.equals(missingTypesForSt2);

      assert Util.iff(missingTypesForSt2.isEmpty(), activeStatementsAsSet
          .contains(st));
    }
  }

  private void writeLog(Exception ex, boolean append) throws IOException {

    File f = new File("/tmp/carloslog.txt");
    BufferedWriter writer = new BufferedWriter(new FileWriter(f, append));

    writer.write("***********************************************************");

    writer.write("===SEQUENCE" + Globals.lineSep);
    writer.write(sequence.toString());
    writer.write(Globals.lineSep);

    if (ex != null) {
      writer.write("===EXCEPTION THROWN" + Globals.lineSep);
      writer.write("===MESSAGE" + Globals.lineSep + ex.getMessage()
          + Globals.lineSep);
      writer.write("STACK TRACE" + Globals.lineSep);
      ex.printStackTrace(new PrintWriter(writer)); // TODO close?
    }

    writer.write("===AVAILABLETYPES" + Globals.lineSep);
    for (Class<?> c : availableTypes.getElements()) {
      writer.write(c + Globals.lineSep);
    }
    writer.write(Globals.lineSep);

    writer.write("===MISSINGTYPES" + Globals.lineSep);
    for (Class<?> c2 : missingTypesToStatements.keySet()) {
      Set<Operation> sts = missingTypesToStatements.getValues(c2);
      writer.write(c2 + Globals.lineSep);
      for (Operation s : sts)
        writer.write("   " + s.toString() + Globals.lineSep);
    }
    writer.write(Globals.lineSep);

    writer.write("===ACTIVESTATEMENTS" + Globals.lineSep);
    for (Operation st : enabledStatements.getElements()) {
      writer.write(st.toString() + Globals.lineSep);
    }
    writer.write(Globals.lineSep);

    writer.write("===INACTIVESTATEMENTS" + Globals.lineSep);
    for (Operation sta : inactiveStatements.keySet()) {
      writer.write(sta.toString() + Globals.lineSep);
      for (Class<?> cla : inactiveStatements.getValues(sta)) {
        writer.write("   " + cla + Globals.lineSep);
      }
    }
    writer.write(Globals.lineSep);

    writer.write("===TYPESTOVALS" + Globals.lineSep);
    for (Class<?> key : typesToVals.keySet()) {
      writer.write(key + Globals.lineSep);
      for (Integer v : typesToVals.getValues(key)) {
        writer.write("   " + v + Globals.lineSep);
      }
    }
    writer.write(Globals.lineSep);

    writer.flush();
    writer.close();
  }


}
