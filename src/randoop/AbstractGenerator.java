package randoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.Globals;
import randoop.SequenceGeneratorStats;
import randoop.experiments.SizeEqualizer;
import randoop.experiments.StatsWriter;
import randoop.main.GenInputsAbstract;
import randoop.util.Files;
import randoop.util.Log;
import randoop.util.ReflectionExecutor;
import randoop.util.Timer;
import utilpag.Invisible;
import utilpag.Option;
import cov.Branch;
import cov.Coverage;
import cov.CoverageAtom;

public abstract class AbstractGenerator {

  @Invisible
  @Option("Print detailed statistics after generation.")
  public static boolean print_stats = false;
  @Invisible
  @Option("When branch coverage fails to increase for the given number of seconds (>0), stop generation.")
  public static int stop_when_plateau = -1;
  @Option ("Dump each seqeunce to the log file")
  public static boolean dump_sequences = false;

  private final Timer timer = new Timer();
  protected final long timeMillis;
  protected final int maxSequences;
  public final Map<CoverageAtom,Set<Sequence>> branchesToCoveringSeqs = new LinkedHashMap<CoverageAtom, Set<Sequence>>();
  public final MultiVisitor executionVisitor;
  public List<StatementKind> statements;
  public SequenceGeneratorStats stats;
  public List<Class<?>> covClasses;
  public SequenceCollection seeds;
  public SizeEqualizer sizeEqualizer = new SizeEqualizer();

  /**
   *
   * @param statements
   * @param covClasses can be null.
   * @param timeMillis
   * @param maxSequences
   * @param seeds can be null.
   */
  public AbstractGenerator(List<StatementKind> statements,
      List<Class<?>> covClasses, long timeMillis, int maxSequences, SequenceCollection seeds) {

    this.timeMillis = timeMillis;

    this.maxSequences = maxSequences;

    this.statements = statements;

    if (covClasses == null)
      this.covClasses = new ArrayList<Class<?>>();
    else
      this.covClasses = new ArrayList<Class<?>>(covClasses);

    this.executionVisitor = new MultiVisitor();

    this.stats =  new SequenceGeneratorStats(statements, this.covClasses);

    if (seeds == null) {
      this.seeds = new SequenceCollection();
    } else {
      this.seeds = seeds;
    }
  }

  protected boolean stop() {
    long now = System.currentTimeMillis();
    return
    (stop_when_plateau > 0
     && stats.getGlobalStats().getCount(SequenceGeneratorStats.STAT_BRANCHTOT) == 0)
    || (stop_when_plateau > 0
        && (now - stats.progressDisplay.lastCovIncrease) > stop_when_plateau * 1000)
    || (timer.getTimeElapsedMillis() >= timeMillis)
    || (numSequences() >= maxSequences);
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

      stats.startProgressDisplay();

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

        stats.updateStatistics(eSeq, cov, fa);

        if (dump_sequences) {
          System.out.printf ("Sequence after execution:%n%s%n",
                             eSeq.toString());
          System.out.printf ("allSequences.size() = %d%n", numSequences());
        }

        if (Log.isLoggingOn()) {
          Log.logLine("Sequence after execution: " + Globals.lineSep + eSeq.toString());
          Log.logLine("allSequences.size()=" + numSequences());
        }
      }
      stats.stopProgressDisplay();

      if (print_stats)
        stats.printStatistics();

      System.out.println();
      System.out.println("Normal method executions:" + ReflectionExecutor.normalExecs());
      System.out.println("Exceptional method executions:" + ReflectionExecutor.excepExecs());
      System.out.println();
      System.out.println("Average method execution time (normal termination):" + ReflectionExecutor.normalExecAvgMillis());
      System.out.println("Average method execution time (exceptional termination):" + ReflectionExecutor.excepExecAvgMillis());

      if (GenInputsAbstract.output_coverage_plot != null) {
        try {
          Files.writeToFile(stats.covPlot,GenInputsAbstract.output_coverage_plot);
        } catch (Exception e) {
          throw new Error(e);
        }
      }

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
