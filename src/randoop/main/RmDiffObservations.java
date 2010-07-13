package randoop.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import plume.Options;
import randoop.ExecutableSequence;
import randoop.RegressionCaptureVisitor;
import randoop.util.ReflectionExecutor;

public class RmDiffObservations extends CommandHandler {

  private static final String command = "rm-diff-obs";
  private static final String pitch
    = "Removes any non-determinstic checks from a serialized sequence";
  private static final String commandGrammar = "rm-diff-obs OPTIONS";
  private static final String where = null;
  private static final String summary
    = "Recreates checks for the sequence, and removes any that "
    + "don't match";
  private static final String input
    = "Serialized file containing List<ExecutableSequence>";
  private static final String output
    = "Serialized file with List<ExecutableSequence> with deterministic obs";
  private static final String example
    = "java randoop.main.Main rm-diff-obs in-seq-file out-seq-file";
  private static final List<String> notes;
  static {
    notes = new ArrayList<String>();
    notes.add("This command is needed because checks may cover ");
    notes.add("values that are not repeateable, such as values that depend");
    notes.add("on the current date/time");
  }

  private static Options options = new Options (RmDiffObservations.class,
                                                GenInputsAbstract.class,
                                                ReflectionExecutor.class);

  public RmDiffObservations() {
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
    assert nonargs.length == 2 : "should be two arguments to rm-diff-obs "
      + Arrays.toString(nonargs);

    String input_file = nonargs[0];
    String output_file = nonargs[1];

    // Allocate some memory of various sizes to to help make sure that
    // addresses (such as those returned from Object.toString() won't
    // coincidentally be the same
    List<int[]> space = new ArrayList<int[]>();
    for (int jj = 0; jj < 100; jj++) {
      for (int ii = 0 ; ii < 500; ii++)
        space.add (new int[ii]);
    }

    // If an initializer routine was specified, execute it
    GenTests.execute_init_routine(3);

    // Read the list of sequences from the serialized file
    List<ExecutableSequence> seqs = GenTests.read_sequences(input_file);

    System.out.printf ("%d total checks in original sequence%n",
                       ExecutableSequence.checks_count (seqs));

    // Generate checks and compare them to the first runs
    int diffs = 0;
    RegressionCaptureVisitor rcv = new RegressionCaptureVisitor();
    List<ExecutableSequence> clean_seq = new ArrayList<ExecutableSequence>();
        for (ExecutableSequence es : seqs) {
      ExecutableSequence es2 = new ExecutableSequence (es.sequence);
      // System.out.printf ("Executing test %d%n", test_no++);
      es2.execute (rcv, false);
      clean_seq.add (es2);
      diffs += es.compare_checks (es2, true,
                                        GenInputsAbstract.print_diff_obs);
    }
    System.out.printf ("Removed %d inconsistent checks%n", diffs);
    System.out.printf ("%d total checks in final sequence%n",
                       ExecutableSequence.checks_count (clean_seq));

    // Write out the new checks
    GenTests.write_sequences (clean_seq, output_file);

    return true;
  }

}
