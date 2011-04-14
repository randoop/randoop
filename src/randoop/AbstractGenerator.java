package randoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import plume.Option;
import plume.OptionGroup;
import plume.Unpublicized;
import randoop.experiments.StatsWriter;
import randoop.main.GenInputsAbstract;
import randoop.util.Log;
import randoop.util.ProgressDisplay;
import randoop.util.ReflectionExecutor;
import randoop.util.Timer;

/**
 * Algorithm template for implementing a test generator.
 * 
 * The main generation loop is defined in method <code>explore()</code>,
 * which repeatedly generates a new sequence, determines if it a failing
 * sequence, and stops the process when the time or sequence limit expires.
 * The process of generating a new sequences is left abstract.
 * 
 * @see randoop.ForwardGenerator
 */
public abstract class AbstractGenerator {

  @OptionGroup(value="AbstractGenerator unpublicized options", unpublicized=true)

  @Unpublicized
  @Option("Dump each sequence to the log file")
  public static boolean dump_sequences = false;
  
  @RandoopStat("Number of generation steps (one step consistents of an attempt to generate and execute a new, distinct sequence)")
  public int num_steps = 0;

  @RandoopStat("Number of sequences generated.")
  public int num_sequences_generated = 0;
  
  @RandoopStat("Number of sequences generated that reveal a failure.")
  public int num_failing_sequences = 0;

  /**
   * The timer used to determine how much time has elapsed since the start of
   * generator and whether generation should stop.
   */
  public final Timer timer = new Timer();
  
  /**
   * Time limit for generation. If generation reaches the specified time
   * limit (in milliseconds), the generator stops generating sequences.
   */
  public final long maxTimeMillis;
  
  /**
   * Sequence limit for generation. If generation reaches the specified sequence
   * limit, the generator stops generating sequences.
   */
  public final int maxSequences;
  
  /**
   * The list of statement kinds (methods, constructors, primitive value declarations, etc.)
   * used to generate sequences. In other words, statements specifies the universe
   * of operations from which sequences are generated.
   */
  public List<StatementKind> statements;
  
  /**
   * Container for execution visitors used during execution of sequences. 
   */
  public final MultiVisitor executionVisitor;

  /**
   * Component manager responsible for storing previously-generated sequences.
   */
  public ComponentManager componentManager;
  
  /**
   * Customizable stopping criterion in addition to time and sequence limits.
   */
  private IStopper stopper;
  
  /**
   * Manages notifications for listeners.
   * 
   * @see randoop.IEventListener
   */
  public RandoopListenerManager listenerMgr;
  
  /**
   * Updates the progress display message printed to the console.
   */
  private ProgressDisplay progressDisplay;
  
  /**
   * This field is set by Randoop to point to the sequence currently being executed.
   * In the event that Randoop appears to hang, this sequence is printed out to console
   * to help the user debug the cause of the hanging behavior.
   */
  public static Sequence currSeq = null;
  
  /**
   * The list of final sequences that are printed out as JUnit tests (i.e. Randoop's output). 
   */
  public List<ExecutableSequence> outSeqs = new ArrayList<ExecutableSequence>();

  /**
   * A list of filters that can be installed to help determine if a sequence
   * should be added to the final sequence list outSeqs.
   */
  public List<ITestFilter> outputTestFilters;
  
 
  /**
   * Constructs a generator with the given parameters.
   * 
   * @param statements Statements (e.g. methods and constructors) used to create sequences. Cannot be null.
   * 
   * @param timeMillis maximum time to spend in generation. Must be non-negative.
   * 
   * @param maxSequences maximum number of sequences to generate. Must be non-negative.
   * 
   * @param componentManager component manager to use to store sequences during component-based generation.
   *        Can be null, in which case the generator's component manager is initialized as <code>new ComponentManager()</code>.
   *        
   * @param stopper Optional, additional stopping criterion for the generator. Can be null.
   * 
   * @param listenerManager Manager that stores and calls any listeners to use during generation. Can be null.
   *  
   * @param testfilters List of filters to determine which sequences to output. Can be null or empty.
   */
  public AbstractGenerator(List<StatementKind> statements, long timeMillis, int maxSequences, ComponentManager componentManager,
      IStopper stopper, RandoopListenerManager listenerManager, List<ITestFilter> testfilters) {
    assert statements != null;

    this.maxTimeMillis = timeMillis;

    this.maxSequences = maxSequences;

    this.statements = statements;

    this.executionVisitor = new MultiVisitor();

    if (componentManager == null) {
      this.componentManager = new ComponentManager();
    } else {
      this.componentManager = componentManager;
    }
    
    this.stopper = stopper;
    
    this.listenerMgr = listenerManager;
    
    outputTestFilters = new LinkedList<ITestFilter>();
    if (testfilters == null || testfilters.isEmpty()) {
      outputTestFilters.add(new DefaultTestFilter());
    } else {
      outputTestFilters.addAll(testfilters);
    }
  }

  protected boolean stop() {
    return
    (listenerMgr != null && listenerMgr.stopGeneration())
    || (timer.getTimeElapsedMillis() >= maxTimeMillis)
    || (numSequences() >= maxSequences)
    || (stopper != null && stopper.stop());
  }

  public abstract ExecutableSequence step();

  public abstract int numSequences();

  /**
   * Creates and executes new sequences in a loop.
   */
  public void explore() {

      Log.log(this.statements);

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
          System.out.printf ("seq before run: %s%n", eSeq);
        }
        
        // Notify listeners we just completed generation step.
        if (listenerMgr != null) {
          listenerMgr.generationStepPost(eSeq);
        }

        if (eSeq == null)
          continue;
        
        num_sequences_generated++;

        FailureSet fa = new FailureSet(eSeq);
        
        if (fa.getFailures().size() > 0) {
          num_failing_sequences++;
        }

        // Output results to file.
        if (GenInputsAbstract.expfile != null) {
          try {
              StatsWriter.write(GenInputsAbstract.expfile, eSeq, fa);
          } catch (IOException e) {
            throw new Error(e);
          }
        }

        boolean outputSequence = true;
        for (ITestFilter f : outputTestFilters) {
          if (!f.outputSequence(eSeq, fa)) {
            outputSequence = false;
            break;
          }
        }
        if (outputSequence) {
          outSeqs.add(eSeq);
        }
     
        if (dump_sequences) {
          System.out.printf ("Sequence after execution:%n%s%n", eSeq.toString());
          System.out.printf ("allSequences.size() = %d%n", numSequences());
        }

        if (Log.isLoggingOn()) {
          Log.logLine("Sequence after execution: " + Globals.lineSep + eSeq.toString());
          Log.logLine("allSequences.size()=" + numSequences());
        }
        
    }
      
    if (!GenInputsAbstract.noprogressdisplay && progressDisplay != null) {
      progressDisplay.display();
      progressDisplay.shouldStop = true;
    }

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println();
      System.out.println("Normal method executions:" + ReflectionExecutor.normalExecs());
      System.out.println("Exceptional method executions:" + ReflectionExecutor.excepExecs());
      System.out.println();
      System.out.println("Average method execution time (normal termination):     " + String.format("%.3g", ReflectionExecutor.normalExecAvgMillis()));
      System.out.println("Average method execution time (exceptional termination):" + String.format("%.3g", ReflectionExecutor.excepExecAvgMillis()));
    }

      // Notify listeners that exploration is ending.
      if (listenerMgr != null) {
        listenerMgr.explorationEnd();
      }
    }

  
  /**
   * Returns the set of sequences that are used as inputs in other sequences
   * (and can thus be thought of as subsumed by another sequence). This should
   * only be called for subclasses that support this.
   */
  public Set<Sequence> subsumed_sequences() {
    throw new Error("subsumed_sequences not supported for " + this.getClass());
  }
}
