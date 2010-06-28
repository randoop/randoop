package randoop;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plume.Option;
import plume.Pair;
import plume.Unpublicized;
import randoop.experiments.SizeEqualizer;
import randoop.experiments.StatsWriter;
import randoop.main.GenInputsAbstract;
import randoop.runtime.IMessage;
import randoop.runtime.MessageSender;
import randoop.runtime.PercentDone;
import randoop.runtime.RandoopFinished;
import randoop.runtime.RandoopStarted;
import randoop.util.Log;
import randoop.util.ReflectionExecutor;
import randoop.util.Timer;
import randoop.util.ReflectionExecutor.TimeoutExceeded;
import cov.Branch;
import cov.Coverage;
import cov.CoverageAtom;

public abstract class AbstractGenerator {

  @Unpublicized @Option("Print detailed statistics after generation.")
  public static boolean print_stats = false;
  @Unpublicized @Option("When branch coverage fails to increase for the given number of seconds (>0), stop generation.")
  public static int stop_when_plateau = -1;
  @Unpublicized @Option ("Dump each sequence to the log file")
  public static boolean dump_sequences = false;

  private final Timer timer = new Timer();
  protected final long timeMillis;
  protected final int maxSequences;
  public final Map<CoverageAtom,Set<Sequence>> branchesToCoveringSeqs = new LinkedHashMap<CoverageAtom, Set<Sequence>>();
  public final MultiVisitor executionVisitor;
  public List<StatementKind> statements;
  public SequenceGeneratorStats stats;
  public List<Class<?>> covClasses;
  public SizeEqualizer sizeEqualizer = new SizeEqualizer();
  private MessageSender msgSender;
  public ComponentManager componentManager;
  private IStoppingCriterion stopper;

  /**
   *
   * @param statements
   * @param covClasses can be null.
   * @param timeMillis
   * @param maxSequences
   * @param seeds can be null.
   */
  public AbstractGenerator(List<StatementKind> statements,
      List<Class<?>> covClasses, long timeMillis, int maxSequences, ComponentManager componentManager,
      IStoppingCriterion stopper) {
    assert statements != null;

    this.timeMillis = timeMillis;

    this.maxSequences = maxSequences;

    this.statements = statements;

    if (covClasses == null)
      this.covClasses = new ArrayList<Class<?>>();
    else
      this.covClasses = new ArrayList<Class<?>>(covClasses);

    this.executionVisitor = new MultiVisitor();

    this.stats = new SequenceGeneratorStats(statements, this.covClasses);

    if (componentManager == null) {
      this.componentManager = new ComponentManager();
    } else {
      this.componentManager = componentManager;
    }
    
    this.msgSender = null;
    
    this.stopper = stopper;
  }

  public AbstractGenerator(List<StatementKind> statements,
      List<Class<?>> covClasses, long timeMillis, int maxSequences,
      ComponentManager componentMgr, MessageSender msgSender, IStoppingCriterion stopper) {
    this(statements, covClasses, timeMillis, maxSequences, componentMgr, stopper);
    
    this.msgSender = msgSender;
  }

  protected boolean stop() {
    long now = System.currentTimeMillis();
    return
    (stop_when_plateau > 0
     && stats.getGlobalStats().getCount(SequenceGeneratorStats.STAT_BRANCHTOT) == 0)
    || (stop_when_plateau > 0
        && (now - stats.progressDisplay.lastCovIncrease) > stop_when_plateau * 1000)
    || (timer.getTimeElapsedMillis() >= timeMillis)
    || (numSequences() >= maxSequences)
    || (stopper != null && stopper.stop());
  }

  public abstract ExecutableSequence step();

  public abstract long numSequences();

  /**
   * Creates and executes new sequences in a loop, using the sequences
   * in s. New sequences are themselves added to s. Stops when timer
   * says it's testtime to stop.
   */
  public void explore() {

      Log.log(this.statements);

      timer.startTiming();

      if (!GenInputsAbstract.noprogressdisplay) {
        stats.startProgressDisplay();
      }
      
      if (Log.isLoggingOn()) {
        Log.logLine("Initial sequences (seeds):");
        for (Sequence s : componentManager.getAllGeneratedSequences()) {
          Log.logLine(s.toString());          
        }
      }
      
      long timeOfLastUpdate = 0;
      if (msgSender != null) {
        IMessage msg = new RandoopStarted();
        msgSender.send(msg);
        timeOfLastUpdate = timer.getTimeElapsedMillis();
      }
      
      while (!stop()) {

        Coverage.clearCoverage(covClasses);

        ExecutableSequence eSeq = step();
        if (dump_sequences)
          System.out.printf ("seq before run: %s%n", eSeq);

        Set<Branch> cov = new LinkedHashSet<Branch>();
        for (CoverageAtom ca : Coverage.getCoveredAtoms(covClasses)) {
          cov.add((Branch)ca);
          Set<Sequence> seqs = branchesToCoveringSeqs.get(ca);
          if (seqs == null) {
            seqs = new LinkedHashSet<Sequence>();
            branchesToCoveringSeqs.put(ca, seqs);
          }
          if (eSeq != null && seqs.isEmpty()) {
            seqs.add(eSeq.sequence);
          }
        }

        if (eSeq == null)
          continue;

        FailureAnalyzer fa = new FailureAnalyzer(eSeq);

        // Output results to file.
        if (GenInputsAbstract.expfile != null) {
          try {
            if (!GenInputsAbstract.size_equalizer
                || sizeEqualizer.add(eSeq)) {
              StatsWriter.write(GenInputsAbstract.expfile, eSeq, cov, fa);
            }
          } catch (IOException e) {
            throw new Error(e);
          }
        }

        updateStatistics(stats, eSeq, cov, fa);

        if (dump_sequences) {
          System.out.printf ("Sequence after execution:%n%s%n",
                             eSeq.toString());
          System.out.printf ("allSequences.size() = %d%n", numSequences());
        }

        if (Log.isLoggingOn()) {
          Log.logLine("Sequence after execution: " + Globals.lineSep + eSeq.toString());
          Log.logLine("allSequences.size()=" + numSequences());
        }
        
      if (msgSender != null) {
        long timeSoFar = timer.getTimeElapsedMillis();
        double percentTimeDone = timeSoFar / (double)timeMillis;
        double percentSequencesDone = numSequences() / (double)maxSequences;

        // Randoop has more than one stopping criteria. To determine how close we are
        // to finishing, we calculate how close we are wrt the time limit, and wrt the
        // input (sequence) limit. We report whichever is greater, but never report
        // more than 100% (which can happen if Randoop went just a bit over before stopping).
        double percentDone = Math.min(Math.max(percentTimeDone, percentSequencesDone), 1.0);
        
        // Send a message once a second
        if (timeSoFar - timeOfLastUpdate > 250) {

          // Convert to percentage, between 0-100.
          IMessage msg = new PercentDone(percentDone);
          msgSender.send(msg);
          timeOfLastUpdate = timeSoFar;
        }
      }
    }
      
      if (!GenInputsAbstract.noprogressdisplay) {
        stats.progressDisplay.display();
        stats.stopProgressDisplay();
      }

      if (print_stats)
        stats.printStatistics();

      System.out.println();
      System.out.println("Normal method executions:" + ReflectionExecutor.normalExecs());
      System.out.println("Exceptional method executions:" + ReflectionExecutor.excepExecs());
      System.out.println();
      System.out.println("Average method execution time (normal termination):     " + String.format("%.3g", ReflectionExecutor.normalExecAvgMillis()));
      System.out.println("Average method execution time (exceptional termination):" + String.format("%.3g", ReflectionExecutor.excepExecAvgMillis()));

      if (msgSender != null) {
        IMessage msg = new RandoopFinished();
        msgSender.send(msg);
        msgSender.close();
      }
    }

  

  public Set<Pair<StatementKind,Class<?>>> errors = new LinkedHashSet<Pair<StatementKind,Class<?>>>();
  // public Set<FailureAnalyzer.Failure> errors = new LinkedHashSet<FailureAnalyzer.Failure>();

  public List<ExecutableSequence> outSeqs = new ArrayList<ExecutableSequence>();

  // TODO: This method is doing two things: (1) maintaining the list
  // of sequences generated that will ultimately be output to the user, and (2) updating
  // statistics regarding the generation process. The first thing does not belong in this class/method,
  // it just ended up here. This method/class should only keep track of statistics.
  public void updateStatistics(SequenceGeneratorStats stats, ExecutableSequence es, Set<Branch> coveredBranches, FailureAnalyzer fa) {

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
        stats.globalStats.addToCount(SequenceGeneratorStats.STAT_SEQUENCE_RAW_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT, 1);
        counted = true;
      }

      if (errors.add(new Pair<StatementKind,Class<?>>(failure.st, failure.viocls))) {

        if (!addedToOutSeqs
            && (GenInputsAbstract.output_nonexec || !es.hasNonExecutedStatements())
            && (GenInputsAbstract.output_tests.equals(GenInputsAbstract.fail)
                || GenInputsAbstract.output_tests.equals(GenInputsAbstract.all))) {
          outSeqs.add(es);
        }

        stats.globalStats.addToCount(SequenceGeneratorStats.STAT_SEQUENCE_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT, 1);
      }
    }

    // Update coverage information.
    for (Branch ca : coveredBranches) {

      // This branch was already counted.
      if (stats.branchesCovered.contains(ca))
        continue;

      stats.branchesCovered.add(ca);

      Member member = Coverage.getMemberContaining(ca);
      if (member == null) {
        // Atom does not belong to method or constructor.
        // Add only to global stats.
        stats.globalStats.addToCount(SequenceGeneratorStats.STAT_BRANCHCOV, 1);
        continue;
      }

      if (member instanceof Method) {
        // Atom belongs to a method.
        // Add to method stats (and implicitly, global stats).
        Method method = (Method)member;
        stats.addToCount(RMethod.getRMethod(method), SequenceGeneratorStats.STAT_BRANCHCOV, 1);
        continue;
      }

      // Atom belongs to a constructor.
      // Add to constructor stats (and implicitly, global stats).
      assert member instanceof Constructor<?> : member.toString();
      Constructor<?> cons = (Constructor<?>)member;
      stats.addToCount(RConstructor.getRConstructor(cons), SequenceGeneratorStats.STAT_BRANCHCOV, 1);
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

      stats.addToCount(statement, SequenceGeneratorStats.STAT_STATEMENT_EXECUTION_TIME, o
          .getExecutionTime());

      if (o instanceof NormalExecution) {
        stats.addToCount(statement, SequenceGeneratorStats.STAT_STATEMENT_NORMAL, 1);
        continue;
      }

      assert o instanceof ExceptionalExecution;
      ExceptionalExecution exc = (ExceptionalExecution) o;

      Class<?> exceptionClass = exc.getException().getClass();
      Integer count = stats.exceptionTypes.get(exceptionClass);
      stats.exceptionTypes.put(exceptionClass.getPackage().toString() + "." + exceptionClass.getSimpleName(),
          count == null ? 1 : count
              .intValue() + 1);

      if (exc.getException() instanceof StackOverflowError
          || exc.getException() instanceof OutOfMemoryError) {
        stats.addToCount(statement,
            SequenceGeneratorStats.STAT_STATEMENT_EXCEPTION_RESOURCE_EXHAUSTION, 1);

      } else if (exc.getException() instanceof TimeoutExceeded) {
        stats.addToCount(statement,
            SequenceGeneratorStats.STAT_STATEMENT_EXCEPTION_TIMEOUT_EXCEEDED, 1);
      } else {
        stats.addToCount(statement, SequenceGeneratorStats.STAT_STATEMENT_EXCEPTION_OTHER, 1);
      }
    }

    StatementKind statement = es.sequence.getLastStatement();
    // if(statement instanceof MethodCall && !statement.isVoidMethod()) {
    // MethodCall sm = ((MethodCall)statement);
    // statement = MethodCall.getDefaultStatementInfo(sm.getMethod());
    // }
    if (es.hasNonExecutedStatements()) {
      stats.addToCount(statement,
          SequenceGeneratorStats.STAT_SEQUENCE_STOPPED_EXEC_BEFORE_LAST_STATEMENT, 1);
      return;
    }

    ExecutionOutcome o = es.getResult(es.sequence.size() - 1);

    if (o instanceof ExceptionalExecution) {
      stats.addToCount(statement, SequenceGeneratorStats.STAT_SEQUENCE_OTHER_EXCEPTION_LAST_STATEMENT,
          1);
      return;
    }

    assert o instanceof NormalExecution;
    stats.addToCount(statement, SequenceGeneratorStats.STAT_SEQUENCE_EXECUTED_NORMALLY, 1);
  }

  
  /**
   * Returns the set of sequences that are used as inputs in other sequences
   * (and can thus be thought of as subsumed by another sequence).  This should
   * only be called for subclasses that support this.
   */
  public Set<Sequence> subsumed_sequences() {
    throw new Error ("subsumed_sequences not supported for " + this.getClass());
  }
}
