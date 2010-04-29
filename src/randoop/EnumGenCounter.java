package randoop;

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

import randoop.main.GenInputsAbstract;
import randoop.util.CollectionsExt;
import randoop.util.Files;
import randoop.util.Log;
import randoop.util.OneMoreElementList;
import randoop.util.Reflection;
import randoop.util.ReversibleMultiMap;
import randoop.util.ReversibleSet;
import randoop.util.SimpleList;
import randoop.util.Util;
import plume.Pair;

public class EnumGenCounter extends AbstractGenerator {

  public int LIMIT = 1;

  private static final boolean printcode = false;

  long count = 0;

  Sequence sequence;
  SubTypeSet availableTypes = new SubTypeSet(true);
  ReversibleMultiMap<Class<?>, Integer> typesToVals;
  ReversibleSet<StatementKind> enabledStatements;
  ReversibleMultiMap<Class<?>, StatementKind> missingTypesToStatements;
  ReversibleMultiMap<StatementKind, Class<?>> inactiveStatements;

  // Used only in rep check.
  private List<StatementKind> allStatements;

  private static SequenceCollection prims = new SequenceCollection(
      SeedSequences.defaultSeeds());

  public EnumGenCounter(List<StatementKind> allStatements,
      List<Class<?>> covClasses, long timeMillis, int maxSequences, SequenceCollection seeds) {
    super(allStatements, covClasses, timeMillis, maxSequences, seeds);

    this.allStatements = allStatements;

    List<StatementKind> initialEnabledStatements = new ArrayList<StatementKind>();
    Map<Class<?>, Set<StatementKind>> initialMissingTypesToStatements = new LinkedHashMap<Class<?>, Set<StatementKind>>();
    Map<StatementKind, Set<Class<?>>> initialInactiveStatements = new LinkedHashMap<StatementKind, Set<Class<?>>>();

    initialEnabledStatements = new ArrayList<StatementKind>();
    initialMissingTypesToStatements = new LinkedHashMap<Class<?>, Set<StatementKind>>();
    initialInactiveStatements = new LinkedHashMap<StatementKind, Set<Class<?>>>();
    for (StatementKind st : allStatements) {
      boolean isStatic = (st instanceof RMethod) && ((RMethod) st).isStatic();
      List<Class<?>> inputTypes = new ArrayList<Class<?>>(st.getInputTypes());
      boolean first = true;
      for (java.util.Iterator<Class<?>> it = inputTypes.iterator(); it.hasNext();) {
        Class<?> next = it.next();
        if (isStatic || !first || next.isPrimitive() || next.equals(String.class)) {
          it.remove();
        }
        first = false;
      }
      if (inputTypes.isEmpty()) {
        initialEnabledStatements.add(st);
        continue;
      }

      initialInactiveStatements
          .put(st, new LinkedHashSet<Class<?>>(inputTypes));
      for (Class<?> cls : inputTypes) {
        Set<StatementKind> s = initialMissingTypesToStatements.get(cls);
        if (s == null) {
          s = new LinkedHashSet<StatementKind>();
          initialMissingTypesToStatements.put(cls, s);
        }
        s.add(st);
      }
    }

    if (initialEnabledStatements.isEmpty())
      throw new IllegalArgumentException("No active statements.");

    sequence = new Sequence();

    availableTypes = new SubTypeSet(true);
    typesToVals = new ReversibleMultiMap<Class<?>, Integer>();
    sequence = new Sequence();
    enabledStatements = new ReversibleSet<StatementKind>();
    for (StatementKind stk : initialEnabledStatements) {
      enabledStatements.add(stk);
    }
    inactiveStatements = new ReversibleMultiMap<StatementKind, Class<?>>();
    for (Map.Entry<StatementKind, Set<Class<?>>> e : initialInactiveStatements
        .entrySet()) {
      for (Class<?> c2 : e.getValue()) {
        inactiveStatements.add(e.getKey(), c2);
      }
    }
    missingTypesToStatements = new ReversibleMultiMap<Class<?>, StatementKind>();
    for (Map.Entry<Class<?>, Set<StatementKind>> e : initialMissingTypesToStatements
        .entrySet())
      for (StatementKind stk : e.getValue()) {
        missingTypesToStatements.add(e.getKey(), stk);
      }
  }

  @Override
  public ExecutableSequence step() {

    assert sequence.size() == 0;

    if (Log.isLoggingOn()) logState();

    count = extend();

    if (GenInputsAbstract.output_sequence_space != null) {
      try {
        Files.writeToFile(Long.toString(count), GenInputsAbstract.output_sequence_space);
      } catch (IOException e) {
        throw new Error(e);
      }
    }
    System.out.println("COUNT = " + count);
    System.out.println("EXITING.");
    System.exit(0);
    return null; // pacify compiler.
  }

  private void update() {
    Class<?> retType = sequence.getLastStatement().getOutputType();

    // update availableTypes
    availableTypes.add(retType);

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
    List<Pair<Class<?>, StatementKind>> pairsToRemove =
      new ArrayList<Pair<Class<?>, StatementKind>>();

    for (Class<?> c2 : missingTypesToStatements.keySet()) {
      Set<StatementKind> sts = missingTypesToStatements.getValues(c2);
      if (c2.isAssignableFrom(retType)) {
        for (StatementKind st2 : sts) {
          pairsToRemove.add(new Pair<Class<?>, StatementKind>(c2, st2));
          inactiveStatements.remove(st2, c2);
          if (!inactiveStatements.keySet().contains(st2)) {
            enabledStatements.add(st2);
          }
        }
      }
    }

    for (Pair<Class<?>, StatementKind> p : pairsToRemove) {
      missingTypesToStatements.remove(p.a, p.b);
    }
  }

  private void logState() {

    if (Log.isLoggingOn()) {
      Log.logLine("AVAILABLE-TYPES:\n\n" + availableTypes.typesWithsequences);
      Log.logLine("TYPES-TO-VALS:\n\n" + typesToVals);
      Log.logLine("ENABLED STATEMENTS:\n\n" + enabledStatements);
      Log.logLine("MISSING-TYPES-TO_STATEMENTS:\n\n" + missingTypesToStatements);
      Log.logLine("INACTIVE-STATEMENTS:\n\n" + inactiveStatements);
    }
  }

  private void undoLastStep() {

    if (Log.isLoggingOn()) Log.logLine("UNDOING LAST STEP FOR SEQUENCE:\n\n" + sequence);

    availableTypes.undoLastStep();
    typesToVals.undoToLastMark();
    enabledStatements.undoToLastMark();
    missingTypesToStatements.undoToLastMark();
    inactiveStatements.undoToLastMark();

    // Remove last statement.
    removeLast();

    logState();
  }

  // Removes last statement, and also the last execution outcome and decoration list.
  private void removeLast() {
    assert sequence.statements instanceof OneMoreElementList<?>;
    OneMoreElementList<Statement> statements = (OneMoreElementList<Statement>) sequence.statements;
    sequence = new Sequence(statements.list);
  }

  private void markState() {
    availableTypes.mark();
    typesToVals.mark();
    enabledStatements.mark();
    missingTypesToStatements.mark();
    inactiveStatements.mark();
  }

  private long extend() {
    if (sequence.getNetSize() == LIMIT) {
      if (printcode) {
        StringBuilder b = new StringBuilder();
        for (String s : code) {
          b.append(s);
        }
        String line = b.toString();
        System.err.println(line);
      }
      //assert line.equals(sequence.toCodeString().replaceAll("\\s", ""));
      if (Log.isLoggingOn()) Log.logLine("REACHED LIMIT.");
      stats.globalStats.addToCount(SequenceGeneratorStats.STAT_NOT_DISCARDED, 1);
      //count += factor;
      return 1;
    }
    Set<StatementKind> sts = new LinkedHashSet<StatementKind>(enabledStatements.getElements());
    long ret = 0;
    for (StatementKind st : sts) {
      if (Log.isLoggingOn()) Log.logLine("Selected statement: " + st);
      boolean isInstance = (st instanceof RMethod) && (!((RMethod) st).isStatic());
      ret += extend(st, new ArrayList<Integer>(), isInstance);
    }
    return ret;
  }

  List<String> code = new ArrayList<String>();

  private long extend(StatementKind st, List<Integer> varsIndices, boolean isInstance) {

    // All the input are there.
    if (varsIndices.size() == st.getInputTypes().size()) {

      // Extend the sequence.
      List<Variable> inputs = new ArrayList<Variable>();
      for (Integer i : varsIndices)
        inputs.add(sequence.getVariable(i));
      markState();
      int oldsize = sequence.getNetSize();
      sequence = sequence.extend(st, inputs);
      if (printcode) { StringBuilder b = new StringBuilder(); sequence.printStatement(b, sequence.size() - 1); code.add(b.toString().replaceAll("\\s", "")); }
      assert sequence.getNetSize() == oldsize + 1;
      update();
      // Recursively extend the new sequence.
      long ret = extend();
      undoLastStep();
      if (printcode) { code.remove(code.size() - 1); }
      return ret;
    }

    Class<?> tc = st.getInputTypes().get(varsIndices.size());

    if (tc.isPrimitive() || tc.equals(String.class)) {

      SimpleList<Sequence> l = prims.getSequencesForType(tc, true);
      assert l.size() > 0;
      Sequence news = l.get(0);
      sequence = sequence.extend(news.getStatementKind(0), Collections.<Variable>emptyList());
      if (printcode) { StringBuilder b = new StringBuilder(); sequence.printStatement(b, sequence.size() - 1); code.add(b.toString().replaceAll("\\s", "")); }
      varsIndices.add(sequence.getLastVariable().index);
      long ret = l.size() * extend(st, varsIndices, isInstance);
      removeLast();
      if (printcode) { code.remove(code.size() - 1); }
      varsIndices.remove(varsIndices.size() - 1);
      return ret;
    }

    Set<Class<?>> possibleTypes = new LinkedHashSet<Class<?>>(availableTypes.getMatches(tc));

    if (possibleTypes.size() == 0) {
      assert varsIndices.size() == 0 ? !isInstance : true;
      sequence = sequence.extend(PrimitiveOrStringOrNullDecl.nullOrZeroDecl(tc), Collections.<Variable>emptyList());
      if (printcode) { StringBuilder b = new StringBuilder(); sequence.printStatement(b, sequence.size() - 1); code.add(b.toString().replaceAll("\\s", "")); }
      varsIndices.add(sequence.getLastVariable().index);
      long ret = extend(st, varsIndices, isInstance);
      removeLast();
      if (printcode) { code.remove(code.size() - 1); }
      varsIndices.remove(varsIndices.size() - 1);
      return ret;

    }

    long ret = 0;
    int nullcount = (isInstance && varsIndices.size() == 0) ? 0 : 1;
    Integer chosenVal = null;
    int possibleValsCount = 0;
    for (Class<?> possibleType : possibleTypes) {
      Set<Integer> possibleVals = typesToVals.getValues(possibleType);
      assert possibleVals.size() > 0;
      possibleValsCount += possibleVals.size();
      if (chosenVal == null)
        chosenVal = possibleVals.iterator().next();
    }

    varsIndices.add(sequence.getVariable(chosenVal).index);
    ret += (nullcount + possibleValsCount) * extend(st, varsIndices, isInstance);
    varsIndices.remove(varsIndices.size() - 1);
    return ret;
  }

  @Override
  public long numSequences() {
    return count;
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

    Set<StatementKind> activeStatementsAsSet = new LinkedHashSet<StatementKind>(
        enabledStatements.getElements());

    // activeStatements is a subset of allStatements.
    assert allStatements.containsAll(activeStatementsAsSet);

    // activeStatements and inactiveStatements partition allStatements.
    assert CollectionsExt.intersection(activeStatementsAsSet,
        inactiveStatements.keySet()).isEmpty();
    Set<StatementKind> union = new LinkedHashSet<StatementKind>(
        enabledStatements.getElements());
    union.addAll(inactiveStatements.keySet());
    assert union.equals(new LinkedHashSet<StatementKind>(allStatements));

    // missingTypes and activeTypes are disjoint.
    assert CollectionsExt.intersection(availableTypes.getElements(),
        missingTypesToStatements.keySet()).isEmpty();

    // missingTypesToStatements and inactiveStatements: the collections of
    // missing classes is the same.
    Set<Class<?>> classSet1 = missingTypesToStatements.keySet();
    Set<Class<?>> classSet2 = new LinkedHashSet<Class<?>>();
    for (StatementKind sta : inactiveStatements.keySet()) {
      classSet2.addAll(inactiveStatements.getValues(sta));
    }
    assert classSet1.equals(classSet2) : classSet1 + "," + classSet2;

    // missingTypesToStatements and inactiveStatements: the collections of
    // inactive statements is the same.
    Set<StatementKind> stSet1 = inactiveStatements.keySet();
    Set<StatementKind> stSet2 = new LinkedHashSet<StatementKind>();
    for (Class<?> c2 : missingTypesToStatements.keySet()) {
      for (StatementKind s : missingTypesToStatements.getValues(c2)) {
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
      if (sequence.getStatementKind(i) instanceof PrimitiveOrStringOrNullDecl)
        continue;
      assert valuesSoFar.contains(i) : i;
    }

    for (Class<?> c : typesToVals.keySet()) {
      assert c != null;
    }

    for (Class<?> c : missingTypesToStatements.keySet()) {
      assert c != null;
    }

    for (StatementKind st : inactiveStatements.keySet()) {
      assert st != null;
    }

    // TODO move this to repCheck for List<StatementKind>.
    for (StatementKind st : allStatements) {
      assert st != null;
    }

    for (StatementKind st : allStatements) {

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
        Set<StatementKind> sts = missingTypesToStatements.getValues(c3);
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
      Set<StatementKind> sts = missingTypesToStatements.getValues(c2);
      writer.write(c2 + Globals.lineSep);
      for (StatementKind s : sts)
        writer.write("   " + s.toString() + Globals.lineSep);
    }
    writer.write(Globals.lineSep);

    writer.write("===ACTIVESTATEMENTS" + Globals.lineSep);
    for (StatementKind st : enabledStatements.getElements()) {
      writer.write(st.toString() + Globals.lineSep);
    }
    writer.write(Globals.lineSep);

    writer.write("===INACTIVESTATEMENTS" + Globals.lineSep);
    for (StatementKind sta : inactiveStatements.keySet()) {
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

  private Set<Class<?>> getInputTypesSet(StatementKind st) {
    return new LinkedHashSet<Class<?>>(st.getInputTypes());
  }
}
