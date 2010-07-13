package daikon.test;

import daikon.*;
import utilMDE.*;

import daikon.config.Configuration;

import java.io.*;

import java.net.URL;

import java.util.List;
import java.util.Vector;

import junit.framework.*;

/**
 * This is a tester for the formatting of invariants in different
 * modes that is configurable by file input. It can test practically
 * any invariant in the Daikon system given the appropriate commands.
 * The test are configured from the InvariantFormatTest.commands file
 * and errors that occur are written to the InvariantFormatTest.diffs
 * file. More detail on the expected formats of these files is in
 * Daikon developer manual
 **/
public class InvariantFormatTester extends TestCase
{

  /**
   * Maximum file size that can currently be examined by the program.
   * It is arbitrary, but a length must be supplied to
   * LineNumberReader.mark().
   */
  private static final int MAX_FILE_SIZE = 262144;

  /**
   * Indicates a string that when it starts a line signifies that the
   * line is a comment.
   **/
  public static final String COMMENT_STARTER_STRING = ";";

  /**
   * A list containing all of the test formats.
   **/
  public static final List<String> TEST_FORMAT_LIST = getTestFormatList();

  /**
   * Allows for the configuring of Daikon options.
   **/
  static Configuration config;

  /**
   * File that contains the format test commands.  Must be found as a
   * resource
   **/
  private static String command_file = "InvariantFormatTest.commands";

  @Option ("-d File to write any differences to.  Will be deleted on success")
  public static File diff_file = new File ("InvariantFormatTest.diffs");

  /**
   * Determines whether the object will generate goal statements.
   **/
  @Option ("-g Filename to write goals to")
  public static File generate_goals = null;

  /**
   * This function allows this test to be run from the command line
   * instead of its usual method, which is through the Daikon
   * MasterTester.
   *
   * @param args arguments to the main function, which control options
   *        to the program. As of now there is only one option,
   *        "--generate_goals", which will generate goal information for
   *        the selected tests assuming the output that the tests provide
   *        is the correct output
   **/
  public static void main(String[] args) {
    daikon.LogHelper.setupLogs (daikon.LogHelper.INFO);

    String usage = "java daikon.test.InvariantFormatTester";
    Options options = new Options (usage, InvariantFormatTester.class);
    String[] other_args = options.parse_and_usage (args);
    if (other_args.length > 0) {
      options.print_usage("unexpected arguments");
      return;
    }

    junit.textui.TestRunner.run(new TestSuite(InvariantFormatTester.class));
  }

  /**
   * This constructor allows the test to be created from the
   * MasterTester class.
   *
   * @param name the desired name of the test case
   **/
  public InvariantFormatTester(String name) {
    super(name);
    config = Configuration.getInstance();
  }

  /**
   * This function produces the format list for intialization of the
   * static format list variable.
   **/
  static List<String> getTestFormatList() {
    List<String> result = new Vector<String>();

    // Add test formats - hard coded in
    result.add("daikon");
    result.add("java");
    result.add("esc");
    result.add("ioa");
    result.add("jml");
    result.add("dbc");
    result.add("simplify");

    return result;
  }

  /**
   * This function is the actual function performed when this class is
   * run through JUnit.
   **/
  public void testFormats() {

    // Don't care about comparability info because we are only
    // creating variables for the purpose of being compared (thus they
    // should all be comparable)
    Daikon.ignore_comparability = true;

    // run the actual test

    if (!execute()) {
      fail("At least one test failed." +
           " Inspect " + diff_file + " for error report.");
    }
  }

  /**
   * Returns the next non-comment, non-whitespace line of the input buffer.
   *
   * @param input the input buffer
   * @return the next non-comment, non-whitespace line of the input buffer or
   *         null if the end of the buffer is reached before such a line can be found
   **/
  static String getNextRealLine(BufferedReader input) {
    String currentLine = "";

    try {
      while (currentLine != null) {
        currentLine = input.readLine();
        if (currentLine != null && !isComment(currentLine) && !isWhitespace(currentLine))
          return currentLine;
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e.toString());
    }
    return null;
  }

  /**
   * This function performs the testing for a particular format
   * indicated by the format string. It subsequently sets up
   * appropriate input and output streams for the format test,
   * performs the test, and the compares the test results to the
   * goals.  If the goals differ from the actual results the test
   * fails.
   **/
  private boolean execute() {

    // Open the input stream.
    InputStream inputStream = InvariantFormatTester.class
      .getResourceAsStream(command_file);
    if (inputStream == null) {
      fail("Input file for invariant format tests missing." +
           " (Should be in " + command_file +
           " and it must be within the classpath)");
      throw new Error("This can't happen"); // to quiet Findbugs
    }
    LineNumberReader commandReader =
      new LineNumberReader(new InputStreamReader(inputStream));

    // Create a stream for the output
    OutputStream out = new ByteArrayOutputStream();
    boolean result;

    // Run the test
    try {
      result = performTest(commandReader, new PrintStream(out));
    }
    catch (RuntimeException e) {
      throw new RuntimeException ("Error detected on line " +
               commandReader.getLineNumber() + " of " +
               InvariantFormatTester.class.getResource(command_file), e);
    }

    // Close the command file
    try {
      inputStream.close();
    }
    catch (IOException e) {
      // Can't write the goals into the commands file if it can't be cleared,
      // otherwise not important.  Only matters if output file is the same
      // as the input file
      if (generate_goals != null)
        throw new RuntimeException("Can't close commands file " +
                   InvariantFormatTester.class.getResource(command_file));
    }

    // Get all of the output as a string
    String output = out.toString();

    // If we are generating a new goal file
    if (generate_goals != null) {

      // Create the goal file and write the output to it
      try {
        PrintStream out_fp = new PrintStream (generate_goals);
        out_fp.printf ("%s", output);
        out_fp.close();
        System.out.println("Goals generated");
      } catch (Exception e) {
        throw new RuntimeException ("Can't write goal file " + generate_goals,
                                    e);
      }
    } else { // handle any differences

      // Delete any previous diffs
      diff_file.delete();

      // If the test failed, write the differences to the diff file
      if (!result) {
        try {
          PrintStream diff_fp = new PrintStream (diff_file);
          diff_fp.printf ("%s", output);
          diff_fp.close();
        } catch (Exception e) {
          throw new RuntimeException ("Can't write diff file " + diff_file, e);
        }
        return false;
      }
    }
    return true;
  }

  /**
   * This function performs an individual formatting test after the
   * input and output streams have been created.
   *
   * @param commands the input that decides which tests to perform
   * @param output the place to where the test output is written
   **/
  private boolean performTest(LineNumberReader commands, PrintStream output) {
    List<FormatTestCase> invariantTestCases = new Vector<FormatTestCase>();
    boolean noTestFailed = true;

    // Need to be able to go to beginning of buffer for combining goals with the input
    if (generate_goals != null) {
      try {
        commands.mark(MAX_FILE_SIZE);
      }
      catch (IOException e) {
        throw new RuntimeException("Cannot mark file in order to generate goals");
      }
    }

    while (true) {
      // Create a new test case
      FormatTestCase currentCase
        = FormatTestCase.instantiate(commands, (generate_goals != null));
      if (currentCase == null)
        break;
      else {
        invariantTestCases.add(currentCase);
        if ((generate_goals == null) && !currentCase.passes()) {
          output.print(currentCase.getDiffString());
          noTestFailed = false;
        }
      }
    }

    if (generate_goals != null) {

      // Go to beginning of the commands buffer
      try {
        commands.reset();
      }
      catch (IOException e) {
        throw new RuntimeException("Cannot reset to mark, thus cannot write goals");
      }

      String debugTemp;

      try {
        for (int i=0; i<invariantTestCases.size(); i++) {
          FormatTestCase currentCase = invariantTestCases.get(i);
          // System.out.println("Goal output #" + i);
          debugTemp = currentCase.generateGoalOutput(commands);
          // System.out.println(debugTemp);

          output.println(debugTemp);
        }

        String currentLineOfText = commands.readLine();

        while (currentLineOfText != null) {
          if (FormatTestCase.parseGoal(currentLineOfText) == null)
            output.println(currentLineOfText);
          currentLineOfText = commands.readLine();
        }
      }
      catch (IOException e) {
        throw new RuntimeException("Writing goal output failed");
      }
    }
    return noTestFailed;
  }

  /**
   * Determines whether a line is a comment or not.
   *
   * @param line the line in question
   * @return true if the line is a comment (that is, not to be interpretted as a command)
   *         false otherwise
   **/
  static boolean isComment(String line) {
    return line.startsWith(COMMENT_STARTER_STRING);
  }

  /**
   * Determines whether a given line is made only of whitespace.
   *
   * @param line the line in question
   * @return true if the line is made up only of whitespace, false otherwise
   **/
  static boolean isWhitespace(String line) {
    for (int x=0; x<line.length(); x++) {
      if (!Character.isWhitespace(line.charAt(x)))
        return false;
    }
    return true;
  }
}
