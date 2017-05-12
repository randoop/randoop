package randoop.generation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import plume.Option;
import plume.OptionGroup;
import plume.Unpublicized;
import randoop.DummyVisitor;
import randoop.ExecutionVisitor;
import randoop.Globals;
import randoop.RandoopStat;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.TestCheckGenerator;
import randoop.util.Log;
import randoop.util.ProgressDisplay;
import randoop.util.ReflectionExecutor;
import randoop.util.Timer;
import randoop.util.predicate.AlwaysFalse;
import randoop.util.predicate.Predicate;

/**
 * Algorithm template for implementing a test generator.
 *
 * <p>The main generation loop is defined in method <code>explore()</code>, which repeatedly
 * generates a new sequence, determines if it a failing sequence, and stops the process when the
 * time or sequence limit expires. The process of generating a new sequences is left abstract.
 *
 * @see ForwardGenerator
 */
public abstract class AbstractGenerator {

  @OptionGroup(value = "AbstractGenerator unpublicized options", unpublicized = true)
  @Unpublicized
  @Option("Dump each sequence to the log file")
  public static boolean dump_sequences = false;

  @RandoopStat(
      "Number of generation steps (one step consists of an attempt to generate and execute a new, distinct sequence)")
  public int num_steps = 0;

  @RandoopStat("Number of sequences generated.")
  public int num_sequences_generated = 0;

  @RandoopStat("Number of failing sequences generated.")
  public int num_failing_sequences = 0;

  @RandoopStat("Number of invalid sequences generated.")
  public int invalidSequenceCount = 0;

  private EnumMap<GenInputsAbstract.BehaviorType, EnumMap<GenInputsAbstract.BehaviorType, Integer>>
      transitionCountMap = new EnumMap<>(GenInputsAbstract.BehaviorType.class);

  private FileWriter transitionLog;

  private void handleConditionTransition(ExecutableSequence eSeq) {
    // count transitions
    addConditionTransition(eSeq);

    if (transitionLog != null) {
      if (eSeq.standardClassification != eSeq.conditionClassification) {
        try {
          transitionLog.write(
              "transition: "
                  + eSeq.standardClassification.name()
                  + " => "
                  + eSeq.conditionClassification
                  + Globals.lineSep);
          transitionLog.write(eSeq.toCodeString() + Globals.lineSep);
          transitionLog.write("-----------------------" + Globals.lineSep);
          transitionLog.flush();
        } catch (IOException e) {
          throw new Error("error while writing transition log: " + transitionLog);
        }
      }
    }
  }

  private void addConditionTransition(ExecutableSequence eSeq) {
    EnumMap<GenInputsAbstract.BehaviorType, Integer> countMap =
        transitionCountMap.get(eSeq.standardClassification);
    if (countMap == null) {
      countMap = new EnumMap<>(GenInputsAbstract.BehaviorType.class);
    }
    Integer count = countMap.get(eSeq.conditionClassification);
    if (count == null) {
      countMap.put(eSeq.conditionClassification, 1);
    } else {
      countMap.put(eSeq.conditionClassification, count + 1);
    }
    transitionCountMap.put(eSeq.standardClassification, countMap);
  }

  public void printTransitionTable(PrintStream out) {
    out.printf("Classification");
    for (GenInputsAbstract.BehaviorType behaviorType : GenInputsAbstract.BehaviorType.values()) {
      out.printf(",%s", behaviorType.name());
    }
    out.printf("%n");
    for (GenInputsAbstract.BehaviorType type : GenInputsAbstract.BehaviorType.values()) {
      out.printf("%s", type.name());
      EnumMap<GenInputsAbstract.BehaviorType, Integer> transitionMap = transitionCountMap.get(type);
      if (transitionMap == null) {
        for (GenInputsAbstract.BehaviorType conditionType :
            GenInputsAbstract.BehaviorType.values()) {
          out.printf(",%d", 0);
        }
      } else {
        for (GenInputsAbstract.BehaviorType conditionType :
            GenInputsAbstract.BehaviorType.values()) {
          Integer count = transitionMap.get(conditionType);
          if (count != null) {
            out.printf(",%d", (int) count);
          } else {
            out.printf(",%d", 0);
          }
        }
      }
      out.printf("%n");
    }
  }

  /**
   * The timer used to determine how much time has elapsed since the start of generator and whether
   * generation should stop.
   */
  public final Timer timer = new Timer();

  /**
   * Time limit for generation. If generation reaches the specified time limit (in milliseconds),
   * the generator stops generating sequences.
   */
  public final long maxTimeMillis;

  /**
   * Sequence limit for generation. If generation reaches the specified sequence limit, the
   * generator stops generating sequences.
   */
  public final int maxGeneratedSequences;

  /**
   * Limit for output. Once the specified number of sequences are in the output lists, the generator
   * will stop.
   */
  public final int maxOutputSequences;

  /**
   * The list of statement kinds (methods, constructors, primitive value declarations, etc.) used to
   * generate sequences. In other words, statements specifies the universe of operations from which
   * sequences are generated.
   */
  public List<TypedOperation> operations;

  /** Container for execution visitors used during execution of sequences. */
  protected ExecutionVisitor executionVisitor;

  /** Component manager responsible for storing previously-generated sequences. */
  public ComponentManager componentManager;

  /** Customizable stopping criterion in addition to time and sequence limits. */
  private IStopper stopper;

  /**
   * Manages notifications for listeners.
   *
   * @see randoop.generation.IEventListener
   */
  public RandoopListenerManager listenerMgr;

  /** Updates the progress display message printed to the console. */
  private ProgressDisplay progressDisplay;

  /**
   * This field is set by Randoop to point to the sequence currently being executed. In the event
   * that Randoop appears to hang, this sequence is printed out to console to help the user debug
   * the cause of the hanging behavior.
   */
  public static Sequence currSeq = null;

  /**
   * The list of error test sequences to be output as JUnit tests. May include subsequences of other
   * sequences in the list.
   */
  public List<ExecutableSequence> outErrorSeqs = new ArrayList<>();

  /**
   * The list of regression sequences to be output as JUnit tests. May include subsequences of other
   * sequences in the list.
   */
  public List<ExecutableSequence> outRegressionSeqs = new ArrayList<>();

  /** A filter to determine whether a sequence should be added to the output sequence lists. */
  public Predicate<ExecutableSequence> outputTest;

  /** Visitor to generate checks for a sequence. */
  protected TestCheckGenerator checkGenerator;

  private int returnPostConditionCount = 0;
  private int returnPostConditionFailureCount = 0;

  /**
   * Constructs a generator with the given parameters.
   *
   * @param operations Statements (e.g. methods and constructors) used to create sequences. Cannot
   *     be null.
   * @param timeMillis maximum time to spend in generation. Must be non-negative.
   * @param maxGeneratedSequences the maximum number of sequences to generate. Must be non-negative.
   * @param maxOutSequences the maximum number of sequences to output. Must be non-negative.
   * @param componentManager the component manager to use to store sequences during component-based
   *     generation. Can be null, in which case the generator's component manager is initialized as
   *     <code>new ComponentManager()</code>.
   * @param stopper Optional, additional stopping criterion for the generator. Can be null.
   * @param listenerManager Manager that stores and calls any listeners to use during generation.
   *     Can be null.
   */
  public AbstractGenerator(
      List<TypedOperation> operations,
      long timeMillis,
      int maxGeneratedSequences,
      int maxOutSequences,
      ComponentManager componentManager,
      IStopper stopper,
      RandoopListenerManager listenerManager,
      FileWriter transitionLog) {
    assert operations != null;

    this.maxTimeMillis = timeMillis;
    this.maxGeneratedSequences = maxGeneratedSequences;
    this.maxOutputSequences = maxOutSequences;
    this.operations = operations;
    this.executionVisitor = new DummyVisitor();
    this.outputTest = new AlwaysFalse<>();

    if (componentManager == null) {
      this.componentManager = new ComponentManager();
    } else {
      this.componentManager = componentManager;
    }

    this.stopper = stopper;
    this.listenerMgr = listenerManager;
    this.transitionLog = transitionLog;
  }

  /**
   * Registers test predicate with this generator for use while filtering generated tests for
   * output.
   *
   * @param outputTest the predicate to be added to object
   */
  public void addTestPredicate(Predicate<ExecutableSequence> outputTest) {
    if (outputTest == null) {
      throw new IllegalArgumentException("outputTest must be non-null");
    }
    this.outputTest = outputTest;
  }

  /**
   * Registers a visitor with this object for use while executing each generated sequence.
   *
   * @param executionVisitor the visitor
   */
  public void addExecutionVisitor(ExecutionVisitor executionVisitor) {
    if (executionVisitor == null) {
      throw new IllegalArgumentException("executionVisitor must be non-null");
    }
    this.executionVisitor = executionVisitor;
  }

  /**
   * Registers a visitor with this object to generate checks following execution of each generated
   * test sequence.
   *
   * @param checkGenerator the check generating visitor
   */
  public void addTestCheckGenerator(TestCheckGenerator checkGenerator) {
    if (checkGenerator == null) {
      throw new IllegalArgumentException("checkGenerator must be non-null");
    }
    this.checkGenerator = checkGenerator;
  }

  /**
   * Tests stopping criteria and determines whether generation should stop. Criteria are checked in
   * this order:
   *
   * <ul>
   *   <li>if there is a listener manager, {@link RandoopListenerManager#stopGeneration()} returns
   *       true,
   *   <li>the elapsed generation time is greater than or equal to the max time in milliseconds,
   *   <li>the number of output sequences is equal to the maximum output,
   *   <li>the number of generated sequences is equal to the maximum generated sequence count, or
   *   <li>if there is a stopper, {@link IStopper#stop()} returns true.
   * </ul>
   *
   * @return true if any of stopping criteria are met, otherwise false
   */
  protected boolean stop() {
    return (listenerMgr != null && listenerMgr.stopGeneration())
        || (timer.getTimeElapsedMillis() >= maxTimeMillis)
        || (GenInputsAbstract.stop_on_error_test && numErrorSequences() > 0)
        || (numOutputSequences() >= maxOutputSequences)
        || (numGeneratedSequences() >= maxGeneratedSequences)
        || (stopper != null && stopper.stop());
  }

  /**
   * Generate an individual test sequence
   *
   * @return a test sequence, may be null
   */
  public abstract ExecutableSequence step();

  /**
   * Returns the count of generated sequence currently for output.
   *
   * @return the sum of the number of error and regression test sequences for output
   */
  public int numOutputSequences() {
    return outErrorSeqs.size() + outRegressionSeqs.size();
  }

  /**
   * Returns the count of generated error-revealing sequences.
   *
   * @return the number of error test sequences
   */
  private int numErrorSequences() {
    return outErrorSeqs.size();
  }

  /**
   * Returns the count of sequences generated so far by the generator.
   *
   * @return the number of sequences generated
   */
  public abstract int numGeneratedSequences();

  /**
   * Creates and executes new sequences until stopping criteria is met.
   *
   * @see AbstractGenerator#stop()
   * @see AbstractGenerator#step()
   */
  public void explore() {
    if (checkGenerator == null) {
      throw new Error("Generator not properly initialized - must have a TestCheckGenerator");
    }

    timer.startTiming();

    if (!GenInputsAbstract.noprogressdisplay) {
      progressDisplay = new ProgressDisplay(this, listenerMgr, ProgressDisplay.Mode.MULTILINE, 200);
      progressDisplay.start();
    }

    if (Log.isLoggingOn()) {
      Log.logLine("Initial sequences (seeds):");
      for (Sequence s : componentManager.getAllGeneratedSequences()) {
        Log.logLine(s.toString());
      }
    }

    // Notify listeners that exploration is starting.
    if (listenerMgr != null) {
      listenerMgr.explorationStart();
    }

    while (!stop()) {

      // Notify listeners we are about to perform a generation step.
      if (listenerMgr != null) {
        listenerMgr.generationStepPre();
      }

      num_steps++;

      ExecutableSequence eSeq = step();

      if (dump_sequences) {
        System.out.printf("seq before run: %s%n", eSeq);
      }

      // Notify listeners we just completed generation step.
      if (listenerMgr != null) {
        listenerMgr.generationStepPost(eSeq);
      }

      if (eSeq == null) {
        continue;
      }

      num_sequences_generated++;

      handleConditionTransition(eSeq);

      if (outputTest.test(eSeq)) {
        if (!eSeq.hasInvalidBehavior()) {
          if (eSeq.hasFailure()) {
            num_failing_sequences++;
            outErrorSeqs.add(eSeq);
          } else {
            outRegressionSeqs.add(eSeq);
          }
        } else {
          invalidSequenceCount++;
        }
      }

      if (dump_sequences) {
        System.out.printf("Sequence after execution:%n%s%n", eSeq.toString());
        System.out.printf("allSequences.size() = %d%n", numGeneratedSequences());
      }

      if (Log.isLoggingOn()) {
        Log.logLine("Sequence after execution: " + Globals.lineSep + eSeq.toString());
        Log.logLine("allSequences.size()=" + numGeneratedSequences());
      }
    }

    if (!GenInputsAbstract.noprogressdisplay && progressDisplay != null) {
      progressDisplay.display();
      progressDisplay.shouldStop = true;
    }

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println();
      System.out.println("Normal method executions: " + ReflectionExecutor.normalExecs());
      System.out.println("Exceptional method executions: " + ReflectionExecutor.excepExecs());
      System.out.println();
      System.out.println(
          "Average method execution time (normal termination):      "
              + String.format("%.3g", ReflectionExecutor.normalExecAvgMillis()));
      System.out.println(
          "Average method execution time (exceptional termination): "
              + String.format("%.3g", ReflectionExecutor.excepExecAvgMillis()));
    }

    // Notify listeners that exploration is ending.
    if (listenerMgr != null) {
      listenerMgr.explorationEnd();
    }
  }

  /**
   * Return all sequences generated by this object.
   *
   * @return return all generated sequences
   */
  public abstract Set<Sequence> getAllSequences();

  /**
   * Returns the set of sequences that are used as inputs in other sequences (and can thus be
   * thought of as subsumed by another sequence). This should only be called for subclasses that
   * support this.
   *
   * @return the set of sequences subsumed by other sequences
   */
  public Set<Sequence> getSubsumedSequences() {
    throw new Error("subsumed_sequences not supported for " + this.getClass());
  }

  /**
   * Returns the generated regression test sequences for output. Filters out subsequences, which can
   * be retrieved using {@link #getSubsumedSequences()}
   *
   * @return regression test sequences that do not occur in a longer sequence
   */
  // TODO replace this with filtering during generation
  public List<ExecutableSequence> getRegressionSequences() {
    List<ExecutableSequence> unique_seqs = new ArrayList<>();
    Set<Sequence> subsumed_seqs = this.getSubsumedSequences();
    for (ExecutableSequence es : outRegressionSeqs) {
      if (!subsumed_seqs.contains(es.sequence)) {
        unique_seqs.add(es);
      }
    }
    return unique_seqs;
  }

  /**
   * Returns the generated error-revealing test sequences for output.
   *
   * @return the generated error test sequences
   */
  public List<ExecutableSequence> getErrorTestSequences() {
    return outErrorSeqs;
  }

  /**
   * Returns the total number of test sequences generated to output, including both regression tests
   * and error-revealing tests.
   *
   * @return the total number of test sequences saved for output
   */
  public int outputSequenceCount() {
    return outRegressionSeqs.size() + outErrorSeqs.size();
  }

  /**
   * Sets the current sequence during exploration
   *
   * @param s the current sequence
   */
  protected void setCurrentSequence(Sequence s) {
    currSeq = s;
  }
}
