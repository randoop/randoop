package randoop;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.main.GenInputsAbstract;
import randoop.util.CollectionsExt;
import randoop.util.ProgressDisplay;
import randoop.util.ReflectionExecutor.TimeoutExceeded;
import plume.Option;
import plume.Pair;
import plume.UtilMDE;
import cov.Branch;
import cov.Coverage;
import cov.CoverageAtom;

public class SequenceGeneratorStats {

  /* @Invisible*/
  @Option("Output sequence generation stats during generation.")
  public static boolean stats_generation = false;

  /* @Invisible*/
  @Option("Output coverage stats during generation.")
  public static boolean stats_coverage = false;

  /* @Invisible*/
  @Option("Outputs extra information for Randoop experiments.")
  public static boolean randoop_exp = false;

  public static long steps = 0;

  public static Sequence currSeq = null;

  protected final Map<String, Integer> exceptionTypes;

  public final Set<Branch> branchesCovered;

  public final List<String> covPlot = new ArrayList<String>();

  private final Map<StatementKind, StatsForMethod> methodStats;
  public final StatsForMethod globalStats;
  private List<StatName> keys = new ArrayList<StatName>();

  public static final StatName STAT_BRANCHTOT = new StatName("TOTAL NUMBER OF BRANCHES IN METHOD",
      "Brtot", "Total number of branches in method", true);

  private static final StatName STAT_BRANCHCOV = new StatName("BRANCHES",
      "Brcov", "Number of branches covered in method", true);

  private static final StatName STAT_SEQUENCE_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT =
    new StatName("STAT_SEQUENCE_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT", "ObjVio",
        "Non-repetitive number of sequences where object contract violated after last statement.", true);

  private static final StatName STAT_SEQUENCE_RAW_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT =
    new StatName("STAT_SEQUENCE__RAW_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT", "ObjVioR",
        "Number of sequences where object contract violated after last statement.", true);

  private static final StatName STAT_SEQUENCE_FORBIDDEN_EXCEPTION_LAST_STATEMENT =
    new StatName("STAT_SEQUENCE_FORBIDDEN_EXCEPTION_LAST_STATEMENT", "ExVio",
        "Number of sequences where bad exception was thrown after last statement.", true);


  private static final StatName STAT_SELECTED = new StatName("SELECTED",
      "Select", "Selected method to create new sequence.", stats_generation);

  private static final StatName STAT_DID_NOT_FIND_INPUT_ARGUMENTS = new StatName(
      "DID NOT FIND SEQUENCE ARGUMENTS",
      "NoArgs",
      "Did not create a new sequence: could not find components that create sequence argument types.",
      stats_generation);

  private static final StatName STAT_DID_NOT_FIND_INPUT_ARGUMENTS_CYCLE = new StatName(
      "DID NOT FIND SEQUENCE ARGUMENTS DUE TO A CYCLE",
      "NoArgsC",
      "Did not create a new sequence: could not find components that create sequence argument types due to a dependency cycle between the ctors.",
      stats_generation);

  private static final StatName STAT_DISCARDED_SIZE = new StatName(
      "DISCARDED (EXCEEDS SIZE LIMIT)",
      "TooBig",
      "Did not create a new sequence: sequence exceeded maximum allowed size.",
      stats_generation);

  private static final StatName STAT_DISCARDED_REPEATED = new StatName(
      "DISCARDED (ALREADY CREATED SEQUENCE)",
      "Repeat",
      "Did not create a new sequence: sequence was already created.",
      stats_generation);

  public static final StatName STAT_NOT_DISCARDED = new StatName(
      "DID NOT DISCARD", "NewInp",
      "Created a new test input.",
      true /* always printable */);

  private static final StatName STAT_SEQUENCE_STOPPED_EXEC_BEFORE_LAST_STATEMENT = new StatName(
      "STAT_SEQUENCE_STOPPED_EXEC_BEFORE_LAST_STATEMENT",
      "Abort",
      "Execution outcome 1 (of 3): stopped before last statement (due to exception or contract violation).",
      stats_generation);

  private static final StatName STAT_SEQUENCE_EXECUTED_NORMALLY = new StatName(
      "STAT_SEQUENCE_EXECUTED_NORMALLY", "NoEx",
      "Execution outcome 2 (of 3): executed to the end and threw no exceptions.",
      stats_generation);

  private static final StatName STAT_SEQUENCE_OTHER_EXCEPTION_LAST_STATEMENT = new StatName(
      "STAT_SEQUENCE_OTHER_EXCEPTION_LAST_STATEMENT", "Excep",
      "Execution outcome 3 (or 3): threw an exception when executing last statement.",
      stats_generation);

  private static final StatName STAT_SEQUENCE_ADDED_TO_COMPONENTS = new StatName(
      "STAT_SEQUENCE_ADDED_TO_COMPONENTS", "Comp",
      "Post-execution outcome 1 (of 1): Added sequence to components.",
      stats_generation);

  private static final StatName STAT_STATEMENT_EXECUTION_TIME = new StatName(
      "STAT_STATEMENT_EXECUTION_TIME", "Time",
      "Milliseconds spent executing statement (across all sequences).",
      stats_generation);

  private static final StatName STAT_STATEMENT_EXCEPTION_OTHER = new StatName(
      "STAT_STATEMENT_EXCEPTION_OTHER", "OthEx",
      "Times statement threw non-VM exception (across all sequences).",
      stats_generation);

  private static final StatName STAT_STATEMENT_EXCEPTION_RESOURCE_EXHAUSTION = new StatName(
      "STAT_STATEMENT_EXCEPTION_RESOURCE_EXHAUSTION",
      "VmEx",
      "Times statement threw VM exception, e.g. stack overflow (across all sequences).",
      stats_generation);

  private static final StatName STAT_STATEMENT_EXCEPTION_TIMEOUT_EXCEEDED = new StatName(
      "STAT_STATEMENT_EXCEPTION_TIMEOUT_EXCEEDED",
      "Killed",
      "Times statement killed because it exceeded time allowed (across all sequences).",
      stats_generation);

  private static final StatName STAT_STATEMENT_NORMAL = new StatName(
      "STAT_STATEMENT_NORMAL", "NoEx",
      "Times statement executed normally (across all sequences).",
      stats_generation);

  public SequenceGeneratorStats(List<StatementKind> statements, List<Class<?>> coverageClasses) {
    this.methodStats = new LinkedHashMap<StatementKind,StatsForMethod>();
    this.globalStats = new StatsForMethod(new DummyStatement("Total"));
    for (StatementKind s : statements) {
      addStatement(s);
    }
    this.exceptionTypes = new LinkedHashMap<String, Integer>();
    this.branchesCovered = new LinkedHashSet<Branch>();
    addStats();

    // Setup STAT_BRANCHTOT for the coverage classes.
    for (Class<?> cls : coverageClasses) {
      Set<CoverageAtom> atoms = Coverage.getBranches(cls);
      assert atoms != null : cls.toString();
      for (CoverageAtom ca : atoms) {
        Member member = Coverage.getMemberContaining(ca);
        if (member == null) {
          // Atom does not belong to method or constructor.
          // Add only to global stats.
          globalStats.addToCount(STAT_BRANCHTOT, 1);
          continue;
        }

        if (member instanceof Method) {
          // Atom belongs to a method.
          // Add to method stats (and implicitly, global stats).
          Method method = (Method)member;
          addToCount(RMethod.getRMethod(method), STAT_BRANCHTOT, 1);
          continue;
        }

        // Atom belongs to a constructor.
        // Add to constructor stats (and implicitly, global stats).
        assert member instanceof Constructor<?> : member.toString();
        Constructor<?> cons = (Constructor<?>)member;
        addToCount(RConstructor.getRConstructor(cons), STAT_BRANCHTOT, 1);
      }
    }
  }

  public StatsForMethod addStatement(StatementKind s) {
    if (s == null)
      throw new IllegalArgumentException("s cannot be null.");
    StatsForMethod st = new StatsForMethod(s);
    addKeys(st);
    this.methodStats.put(s, st);
    return st;
  }

  public boolean containsStatement(StatementKind statement) {
    return methodStats.containsKey(statement);
  }

  private void addKeys(StatsForMethod st) {
    for(StatName key:keys) {
      st.addKey(key);
    }
  }

  public StatsForMethod getGlobalStats() {
    return globalStats;
  }

  public StatsForMethod getStatsForStatement(StatementKind statement) {
    if (!containsStatement(statement)) {
      addStatement(statement);
    }
    StatsForMethod retval = methodStats.get(statement);
    if (retval == null)
      throw new IllegalArgumentException("No stats for statement:" + statement + " Only:" + Globals.lineSep + CollectionsExt.toStringInLines(methodStats.keySet()));
    return retval;
  }

  public void addToCount(StatementKind statement, StatName key, long value) {
    if (!containsStatement(statement)) {
      addStatement(statement);
    }
    globalStats.addToCount(key, value);
    StatsForMethod s = methodStats.get(statement);
    if (s == null)
      throw new IllegalArgumentException("No stats for statement:" + statement + " Only:" + Globals.lineSep + CollectionsExt.toStringInLines(methodStats.keySet()));
    s.addToCount(key, value);
  }

  public void addKey(StatName newKey) {
    keys.add(newKey);
    globalStats.addKey(newKey);
    for (StatementKind s : methodStats.keySet()) {
      StatsForMethod st = methodStats.get(s);
      if (st == null)
        throw new IllegalArgumentException("No stats for statement:" + s + " Only:" + Globals.lineSep + CollectionsExt.toStringInLines(methodStats.keySet()));
      st.addKey(newKey);
    }
  }

  public String toStringGlobal() {
    return globalStats.toString();
  }

  public String keyExplanationString() {
    return globalStats.keyExplanationString();
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    int counter = 0;
    for (Map.Entry<StatementKind, StatsForMethod> entry : methodStats.entrySet()) {
      if (counter ++ % 5 == 0) {
        b.append(getTitle());
      }
      b.append(entry.getValue().toString());
      b.append(Globals.lineSep);
    }
    b.append(getTitle());
    b.append(globalStats.toString());
    b.append(Globals.lineSep);
    return b.toString();
  }

  public String getTitle() {
    return globalStats.getTitle();
  }

  public void addSeparator() {
    addKey(StatsForMethod.getSeparator());
  }

  public ProgressDisplay progressDisplay;

  /** Kills the progress-display thread. */
  public void stopProgressDisplay() {
    if (!GenInputsAbstract.noprogressdisplay) {
      if (progressDisplay != null) {
        progressDisplay.shouldStop = true;
      }
    }
  }

  /** Starts the progress-display thread. */
  public void startProgressDisplay() {
    if (!GenInputsAbstract.noprogressdisplay) {
      printLegend();
      progressDisplay = new ProgressDisplay(this,ProgressDisplay.Mode.MULTILINE, 200);
      progressDisplay.display();
      progressDisplay.start();
    }
  }

  public void printLegend() {
    System.out.println("STATISTICS KEY:");
    System.out.println(keyExplanationString());
  }

  private void addStats() {

    addKey(STAT_SELECTED);
    addKey(STAT_BRANCHTOT);
    addKey(STAT_BRANCHCOV);
    addKey(STAT_SEQUENCE_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT);
    addKey(STAT_SEQUENCE_RAW_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT);
    addKey(STAT_SEQUENCE_FORBIDDEN_EXCEPTION_LAST_STATEMENT);
    if (stats_generation) addSeparator();
    addKey(STAT_DID_NOT_FIND_INPUT_ARGUMENTS);
    addKey(STAT_DID_NOT_FIND_INPUT_ARGUMENTS_CYCLE);
    addKey(STAT_DISCARDED_SIZE);
    addKey(STAT_DISCARDED_REPEATED);
    if (stats_generation) addSeparator();
    addKey(STAT_NOT_DISCARDED);
    addSeparator();
    addKey(STAT_SEQUENCE_STOPPED_EXEC_BEFORE_LAST_STATEMENT);
    addKey(STAT_SEQUENCE_EXECUTED_NORMALLY);
    addKey(STAT_SEQUENCE_OTHER_EXCEPTION_LAST_STATEMENT);
    if (stats_generation) addSeparator();
    addKey(STAT_SEQUENCE_ADDED_TO_COMPONENTS);
    if (stats_generation) addSeparator();
    addKey(STAT_STATEMENT_EXECUTION_TIME);
    if (stats_generation) addSeparator();
    addKey(STAT_STATEMENT_NORMAL);
    addKey(STAT_STATEMENT_EXCEPTION_RESOURCE_EXHAUSTION);
    addKey(STAT_STATEMENT_EXCEPTION_OTHER);
    addKey(STAT_STATEMENT_EXCEPTION_TIMEOUT_EXCEEDED);
    if (stats_generation) addSeparator();
  }

  public void checkStatsConsistent() {
    StatsForMethod globalStats = getGlobalStats();
    if (globalStats.getCount(STAT_SELECTED) != globalStats
        .getCount(STAT_NOT_DISCARDED)
        + globalStats.getCount(STAT_DID_NOT_FIND_INPUT_ARGUMENTS)
        + globalStats.getCount(STAT_DID_NOT_FIND_INPUT_ARGUMENTS_CYCLE)
        + globalStats.getCount(STAT_DISCARDED_REPEATED)
        + globalStats.getCount(STAT_DISCARDED_SIZE)) {
      throw new BugInRandoopException();
    }
  }

  public Set<Pair<StatementKind,Class<?>>> errors = new LinkedHashSet<Pair<StatementKind,Class<?>>>();
  //public Set<FailureAnalyzer.Failure> errors = new LinkedHashSet<FailureAnalyzer.Failure>();

  public List<ExecutableSequence> outSeqs = new ArrayList<ExecutableSequence>();

  public void updateStatistics(ExecutableSequence es, Set<Branch> coveredBranches, FailureAnalyzer fa) {

    boolean addedToOutSeqs = false;
    if ((GenInputsAbstract.output_nonexec || !es.hasNonExecutedStatements())
        && (GenInputsAbstract.output_tests.equals(GenInputsAbstract.pass)
            || GenInputsAbstract.output_tests.equals(GenInputsAbstract.all))) {
      outSeqs.add(es);
      addedToOutSeqs = true;
    }

    boolean counted = false;
    for (FailureAnalyzer.Failure failure : fa.getFailures()) {

      if (!counted) {
        globalStats.addToCount(STAT_SEQUENCE_RAW_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT, 1);
        counted = true;
      }

      if (errors.add(new Pair<StatementKind,Class<?>>(failure.st, failure.viocls))) {

        if (randoop_exp) {
          System.out.println();
          System.out.println("POTENTIAL ERROR FOUND: " + failure);
          System.out.println();
          System.out.println(es.toCodeString());
          System.out.println(es.toDotString());
        }

        if (!addedToOutSeqs
            && (GenInputsAbstract.output_nonexec || !es.hasNonExecutedStatements())
            && (GenInputsAbstract.output_tests.equals(GenInputsAbstract.fail)
                || GenInputsAbstract.output_tests.equals(GenInputsAbstract.all))) {
          outSeqs.add(es);
        }

        globalStats.addToCount(STAT_SEQUENCE_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT, 1);
      }
    }

    // Update coverage information.
    for (Branch ca : coveredBranches) {

      // This branch was already counted.
      if (branchesCovered.contains(ca))
        continue;

      branchesCovered.add(ca);

      Member member = Coverage.getMemberContaining(ca);
      if (member == null) {
        // Atom does not belong to method or constructor.
        // Add only to global stats.
        globalStats.addToCount(STAT_BRANCHCOV, 1);
        continue;
      }

      if (member instanceof Method) {
        // Atom belongs to a method.
        // Add to method stats (and implicitly, global stats).
        Method method = (Method)member;
        addToCount(RMethod.getRMethod(method), STAT_BRANCHCOV, 1);
        continue;
      }

      // Atom belongs to a constructor.
      // Add to constructor stats (and implicitly, global stats).
      assert member instanceof Constructor<?> : member.toString();
      Constructor<?> cons = (Constructor<?>)member;
      addToCount(RConstructor.getRConstructor(cons), STAT_BRANCHCOV, 1);
    }

    for (int i = 0; i < es.sequence.size(); i++) {
      StatementKind statement = es.sequence.getStatementKind(i);

      ExecutionOutcome o = es.getResult(i);

      if (!(statement instanceof RMethod || statement instanceof RConstructor)) {
        continue;
      }

      if (o instanceof NotExecuted) {
        // We don't record this fact (it's not interesting at the
        // statement-level, because
        // a statement not being executed is unrelated to the statement.
        // (It's often due to a previous statement throwing an exception).
        continue;
      }

      addToCount(statement, STAT_STATEMENT_EXECUTION_TIME, o
          .getExecutionTime());

      if (o instanceof NormalExecution) {
        addToCount(statement, STAT_STATEMENT_NORMAL, 1);
        continue;
      }

      assert o instanceof ExceptionalExecution;
      ExceptionalExecution exc = (ExceptionalExecution) o;

      Class<?> exceptionClass = exc.getException().getClass();
      Integer count = exceptionTypes.get(exceptionClass);
      exceptionTypes.put(exceptionClass.getPackage().toString() + "." + exceptionClass.getSimpleName(),
          count == null ? 1 : count
              .intValue() + 1);

      if (exc.getException() instanceof StackOverflowError
          || exc.getException() instanceof OutOfMemoryError) {
        addToCount(statement,
            STAT_STATEMENT_EXCEPTION_RESOURCE_EXHAUSTION, 1);

      } else if (exc.getException() instanceof TimeoutExceeded) {
        addToCount(statement,
            STAT_STATEMENT_EXCEPTION_TIMEOUT_EXCEEDED, 1);
      } else {
        addToCount(statement, STAT_STATEMENT_EXCEPTION_OTHER, 1);
      }
    }

    StatementKind statement = es.sequence.getLastStatement();
    // if(statement instanceof MethodCall && !statement.isVoidMethod()) {
    // MethodCall sm = ((MethodCall)statement);
    // statement = MethodCall.getDefaultStatementInfo(sm.getMethod());
    // }
    if (es.hasNonExecutedStatements()) {
      addToCount(statement,
          STAT_SEQUENCE_STOPPED_EXEC_BEFORE_LAST_STATEMENT, 1);
      return;
    }

    ExecutionOutcome o = es.getResult(es.sequence.size() - 1);

    if (o instanceof ExceptionalExecution) {
      addToCount(statement, STAT_SEQUENCE_OTHER_EXCEPTION_LAST_STATEMENT,
          1);
      return;
    }

    assert o instanceof NormalExecution;
    addToCount(statement, STAT_SEQUENCE_EXECUTED_NORMALLY, 1);
  }

  public void statStatementSelected(StatementKind statement) {
    addToCount(statement, STAT_SELECTED, 1);
  }

  public void statStatementRepeated(StatementKind statement) {
    addToCount(statement, STAT_DISCARDED_REPEATED, 1);
  }

  public void statStatementToBig(StatementKind statement) {
    addToCount(statement, STAT_DISCARDED_SIZE, 1);
  }

  public void statStatementNoArgs(StatementKind statement) {
    addToCount(statement, STAT_DID_NOT_FIND_INPUT_ARGUMENTS, 1);
  }

  public void statStatementNoArgsCycle(StatementKind statement) {
    addToCount(statement, STAT_DID_NOT_FIND_INPUT_ARGUMENTS_CYCLE, 1);
  }

  public void statStatementNotDiscarded(StatementKind statement) {
    addToCount(statement, STAT_NOT_DISCARDED, 1);
  }

  public void printStatistics() {
    // TODO make this printout optional - it's too overwhelming
    System.out.println(Globals.lineSep + "Stats:" + Globals.lineSep + toString());

    System.out.println(Globals.lineSep + "Exceptions thrown:");
    for (Map.Entry<String, Integer> e : exceptionTypes.entrySet()) {
      System.out.println("   " + UtilMDE.rpad(e.getValue().toString(), 8)
          + " of " + e.getKey().toString());
    }
  }

}
