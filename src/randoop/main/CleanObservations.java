package randoop.main;

import java.io.IOException;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import randoop.ExecutableSequence;
import randoop.MultiVisitor;
import randoop.RegressionCaptureVisitor;
import randoop.Sequence;
import randoop.util.Files;
import randoop.util.ReflectionExecutor;
import utilpag.Option;
import utilpag.Options;

public class CleanObservations extends CommandHandler {

  private static final String command = "cleanobs";
  private static final String pitch
    = "Creates clean observations for a serialized sequence";
  private static final String commandGrammar = "cleanobs OPTIONS";
  private static final String where = null;
  private static final String summary
    = "Recreates observations for the sequence, thus ensuring that they"
    + " are created with the correct global state";
  private static final String input
    = "Serialized file containing List<ExecutableSequence>";
  private static final String output
    = "Serialized file with List<ExecutableSequence> with clean observations";
  private static final String example
    = "java randoop.main.Main cleanobs in-seq-file out-seq-file";
  private static final List<String> notes;
  static {
    notes = new ArrayList<String>();
    notes.add("This command is needed because discarded sequences in the ");
    notes.add("initial run of Randoop may have changed the global state in a ");
    notes.add("way that might change the values of the observations");
  }

  private static Options options = new Options (CleanObservations.class,
                                                GenInputsAbstract.class,
                                                ReflectionExecutor.class);

  public CleanObservations() {
    super(command, pitch, commandGrammar, where, summary, notes, input, output,
        example, options);
  }

  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {

    String[] nonargs = null;
    try {
      nonargs = options.parse (args);
    } catch (Exception e) {
      throw new Error ("error parsing command-line arguments", e);
    }
    assert nonargs.length == 2 : "should be two arguments to cleanobs "
      + Arrays.toString(nonargs);

    String input_file = nonargs[0];
    String output_file = nonargs[1];

    // If an initializer routine was specified, execute it
    GenTests.execute_init_routine();

    // Read the list of sequences from the serialized file
    List<ExecutableSequence> seqs = GenTests.read_sequences(input_file);

    // Generate observations and compare them to the first runs
    RegressionCaptureVisitor rcv = new RegressionCaptureVisitor();
    List<ExecutableSequence> clean_seq = new ArrayList<ExecutableSequence>();
    for (ExecutableSequence es : seqs) {
      ExecutableSequence es2 = new ExecutableSequence (es.sequence);
      es2.execute (rcv);
      clean_seq.add (es2);
      es.compare_observations (es2, false, GenInputsAbstract.print_diff_obs);
    }

    // Write out the new observations
    GenTests.write_sequences (clean_seq, output_file);

    return true;
  }

}
