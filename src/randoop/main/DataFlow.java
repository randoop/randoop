package randoop.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import randoop.Globals;
import randoop.Sequence;
import randoop.Variable;
import randoop.experiments.DFResultsOneSeq;
import randoop.experiments.DataFlowInput;
import randoop.experiments.DataFlowOutput;
import randoop.experiments.DFResultsOneSeq.VariableInfo;
import randoop.util.Files;
import randoop.util.SerializationHelper;
import randoop.util.StreamRedirectThread;
import randoop.util.Timer;
import plume.Option;
import plume.Options;
import plume.UtilMDE;
import plume.Options.ArgException;
import cov.Branch;

/**
 * Computes dynamic data flow information affecting specific branches in
 * sequences. Takes as input a DataFlowInput object, which specifies a set of
 * frontier branches and sequences that reach them. Outputs a DataFlowOutput
 * object, which contains data flow information about the sequences.
 *
 * Usage:
 *
 * DataFlow --scratchdir=dir [ --overwrite ] INPUT_FILE
 *
 * Where
 *
 * dir is a directory where DataFlow will place temporary files.
 *
 * INPUT_FILE is a text file that specifies the input to data flow.
 * It is parsed using the DataFlowInput.parse(File) method.
 */
public class DataFlow {

  @Option("Scratch directory for intermediate files (REQUIRED OPTION)")
  public static String scratchdir = null;

  @Option("Overwrite contents of scratch directory, if present")
  public static boolean overwrite = false;

  @Option("(REQUIRED) Name of the file where results will be written")
  public static String outputfile = null;

  @Option ("Dump instrumented files and other debug information for dataflow")
  public static boolean debug_df = false;

  public static void main(String[] args) throws RandoopTextuiException, ArgException, IOException {
    
    // Turn on long output format for sequences, which is required
    // in order for the dataflow instrumentation tracking to work correctly.
    GenInputsAbstract.long_format = true;

    // Parse options and ensure that a scratch directory was specified.
    Options options = new Options(DataFlow.class);
    String[] args2 = options.parse(args);
    if (scratchdir == null) {
      System.out.println("Missing required option --scratchdir=<dir>");
      System.exit(1);
    }
    File scratchDir = new File(scratchdir);
    if (scratchDir.exists() && !overwrite) {
      System.out.println("Scratch directory \"" + scratchDir +
      "\" exists but --overwrite option was not given. Will not proceed.");
      System.exit(1);
    }

    if (!scratchDir.exists())
      scratchDir.mkdirs();

    if (args2.length != 1) {
      throw new RandoopTextuiException("data-flow takes a single non-option argument.");
    }

    String inputFile = args2[0];
    DataFlowInput dfInput = null;
    if (inputFile.endsWith(".txt") || inputFile.endsWith(".goal") || inputFile.endsWith(".gz")) {
      System.out.println("Reading input file.");
      dfInput = DataFlowInput.parse(inputFile);
    } else if (inputFile.endsWith(".ser")) { // TODO remove. No longer supported.
      dfInput = (DataFlowInput)SerializationHelper.readSerialized(inputFile);
    } else {
      throw new IllegalArgumentException("input file must end with .txt, .goal, .gz or .ser.");
    }

    System.out.println(dfInput.frontierMap.size() + " FRONTIER BRANCHES");

    // The results of the analysis.
    List<DFResultsOneSeq> results = new ArrayList<DFResultsOneSeq>();

    Timer pre = new Timer();
    Timer df = new Timer();
    Timer post = new Timer();

    for (Map.Entry<Branch, Set<Sequence>> test
        : dfInput.frontierMap.entrySet()) {

      for (Sequence seq : test.getValue()) {
        pre.startTiming();

        // Create a new sequence using the hints.
        System.out.println("===================================================");


        // Get the branch
        Branch br = test.getKey();

        System.out.println("COVERED BRANCH (OPPOSITE UNCOVERED): "
            + br + " in " + br.getClassName() + "." + br.getMethodName());

        System.out.println("WITNESS SEQUENCE:" + seq);

        if (true) {
          // Create directory <scratchdir>/seqs, if it does not exist.
          File seqsDir = new File(scratchDir, "seqs");
          if (!seqsDir.exists())
            seqsDir.mkdirs();
          // Open the sequence java file
          PrintWriter seq_fp = null;
          File seq_file = new File(seqsDir, "Seqs.java");
          try {
            seq_fp = new PrintWriter (seq_file);
          } catch (Exception e) {
            throw new RuntimeException ("Can't open seqs file " + seq_file, e);
          }

          // Write out the test sequence
          seq_fp.printf ("package seqs;%n%n");
          seq_fp.printf ("class Seqs {%n%n");
          seq_fp.printf ("  public static void seq() {%n%n");
          seq_fp.println (seq.toCodeString());
          seq_fp.println ("  } ");
          seq_fp.println ("}");
          seq_fp.close();

          Files.writeToFile(getDriverCode(), new File(seqsDir, "Driver.java"));
        }

        // Get the current environment and set the classpath to match ours
        Map<String,String> env
        = new LinkedHashMap<String, String>(System.getenv());
        String cp = System.getProperty ("java.class.path");
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
          System.out.println("Error: java.home system property not found");
          System.out.println("You may be able to fix this problem by setting the JAVA_HOME environment variable to an appropriate value");
          System.out.println("Exiting with error.");
          System.exit(1);
        }
        
        // If last directory in java.home is "jre" then tools.jar is in ../lib/tools.jar.
        // Otherwise it is in "lib/toosl.jar.
        // See http://java.sun.com/j2se/1.5.0/docs/tooldocs/solaris/jdkfiles.html
        File jdkHomeDir = new File(javaHome);
        File libDir = null;
        if (jdkHomeDir.getName().equals("jre")) {
          libDir = new File(new File(javaHome).getParentFile(), "lib");
        } else {
          libDir = new File(new File(javaHome), "lib");
        }
        assert libDir != null;
        File toolsJar = new File(libDir, "tools.jar");
                
        if (!toolsJar.exists()) {
           System.out.println("Error: Expected to find required library tools.jar under JAVA_HOME/lib directory: ");
           System.out.println("  JAVA_HOME (i.e. \"java.home\" system property) was \"" + javaHome + "\"."); 
           System.out.println("Ensure JAVA_HOME is properly set.");
           System.out.println("Exiting with error.");
           System.exit(1);
        }
        
        // Add scratch dir path to classpath.
        cp = scratchDir.getAbsolutePath() + ":" + cp + ":" + toolsJar.getAbsolutePath();
        env.put ("CLASSPATH", cp);
        String[] env_array = new String[env.size()];
        int env_index = 0;
        for (Map.Entry<String,String> ee : env.entrySet()) {
          // System.out.printf ("%s=%s%n", ee.getKey(), ee.getValue());
          env_array[env_index++] = String.format ("%s=%s", ee.getKey(),
              ee.getValue());
        }

        // Compile the test sequence
        String compile_cmd = "javac -g seqs/Seqs.java seqs/Driver.java";
        System.out.printf ("Compiling: %s%n", compile_cmd);
        run_cmd (compile_cmd, env_array, scratchDir);

        // The file dfResult will contain the results of the analysis.
        // Create the file under scratch dir.
        File dfResult = new File(scratchDir, "dataflow.txt");

        // The directory debugDir will contain debugging information
        // generated by DynComp.
        File debugDir = new File(scratchDir, "debug");

        pre.stopTiming();
        df.startTiming();

        // Run the DF system on the sequence
        String debug_options = "";
        if (debug_df)
          debug_options = " --verbose -d ";
        String df_cmd = "java -ea daikon.DynComp --branch "
          + get_branch_description(br, dfInput)
          + debug_options
          + " --input_method seqs.Seqs:seq" + " --no-jdk "
          + " --dataflow-out " + dfResult.getAbsolutePath() + " "
          + " --debug-dir " + debugDir.getAbsolutePath() + " "
          + "seqs.Driver";
        System.out.printf ("Running DF: %s%n", df_cmd);
        run_cmd (df_cmd, env_array, null);

        df.stopTiming();
        post.startTiming();

        // Read back the results.  The output file from DynComp contains
        // the locals that contributed to the specified branch.
        Set<VariableInfo> vis = new LinkedHashSet<VariableInfo>();
        String local_str = UtilMDE.readFile (dfResult);
        if (local_str.startsWith ("Error:")) {
          System.out.println (local_str);
          System.exit (1);
        }
        for (String local : local_str.split (Globals.lineSep)) {
          Scanner scan = new Scanner (local);
          if (!scan.hasNext())
            continue;
          String varname = scan.next();
          Variable v = find_value_by_name (seq, varname);
          if (v == null) {
            System.out.printf ("Error: variable %s cannot be found%n",
                varname);
            continue;
          }
          VariableInfo vi = new VariableInfo(v);
          while (scan.hasNext()) {
            String compared_to = scan.next();
            vi.add_branch_compare (compared_to);
          }
          vis.add (vi);
        }
        System.out.printf ("Locals: %s%n", vis);

        // Build the results for Randoop
        DFResultsOneSeq one_result = new DFResultsOneSeq(seq, br, vis);
        System.out.printf ("Result = %s\n", one_result);
        results.add(one_result);

        post.stopTiming();
      }
    }

    assert !pre.isRunning();
    assert !df.isRunning();
    assert !post.isRunning();

    System.out.println("+++ PRE: " + pre.getTimeElapsedMillis());
    System.out.println("+++ DF: " + df.getTimeElapsedMillis());
    System.out.println("+++ POST: " + post.getTimeElapsedMillis());

    // Output results to file.
    System.out.flush();
    DataFlowOutput dfOutput = new DataFlowOutput(results);

    // Determine output file name.
    String outFile = null;
    if (outputfile == null)
      outFile = inputFile + ".output";
    else
      outFile = outputfile;

    dfOutput.toParseableFile(outFile);

    System.out.printf ("Results:%n");
    for (DFResultsOneSeq result : results) {
      System.out.printf ("branch %s: %s%n", result.frontierBranch,
          result.values);
    }
  }

  public static Sequence getSingletonElt(Set<Sequence> witnesses) {
    assert witnesses.size() == 1;
    List<Sequence> li = new ArrayList<Sequence>(witnesses);
    return li.get(0);
  }

  /**
   * Returns the value in the sequence that has the specified name.
   * Returns null if varname is not found
   */
  public static Variable find_value_by_name (Sequence seq, String varname) {

    for (Variable vv : seq.getAllVariables()) {
      if (varname.equals ("var" + vv.getDeclIndex())) {
        return (vv);
      }
    }
    return (null);
  }

  /**
   * Returns the DF --branch information in the form
   * class:method:line-number
   */
  private static String get_branch_description (Branch br,
      DataFlowInput df_input) {
    return (br.getClassName() + ":" + br.getMethodName() + ":" + br.getLineNumber());
  }

  /**
   * Runs the specified command and waits for it to finish.  Throws an
   * Error if the command fails for any reason or does not return a 0
   * status.
   */
  private static void run_cmd (String cmd, String[] env_arr, File dir) {

    int result = 0;
    try {
      Process p = java.lang.Runtime.getRuntime().exec (cmd, env_arr, dir);
      StreamRedirectThread err_thread
      = new StreamRedirectThread ("stderr", p.getErrorStream(), System.out);
      StreamRedirectThread out_thread
      = new StreamRedirectThread ("stdout", p.getInputStream(), System.out);
      err_thread.start();
      out_thread.start();
      result = p.waitFor();
      err_thread.join();
      out_thread.join();
    } catch (Exception e) {
      throw new Error (String.format ("error running cmd '%s'", cmd), e);
    }
    if (result != 0)
      throw new Error (String.format ("Bad result (%s) from cmd '%s' in directory '%s' with environment %s",
                                      result, cmd, dir, Arrays.toString(env_arr)));
  }

  /**
   * @return A string containing the code of the main class
   * passed to DynComp.
   */
  private static String getDriverCode() {
    StringBuilder b = new StringBuilder();
    b.append("package seqs;\n");
    b.append("\n");
    b.append("/**\n");
    b.append(" * Driver for generated test sequences.\n");
    b.append(" */\n");
    b.append("class Driver {\n");
    b.append("\n");
    b.append("  public static void main (String args[]) {\n");
    b.append("\n");
    b.append("    try {\n");
    b.append("      Seqs.seq();\n");
    b.append("    } catch (Exception e) {\n");
    b.append("      System.out.println(\"Exception in seq: \"+e);\n");
    b.append("      daikon.dcomp.DCRuntime.exit_exception = e;\n");
    b.append("      System.out.flush();\n");
    b.append("      System.exit (255);\n");
    b.append("    }\n");
    b.append("  }\n");
    b.append("}\n");
    return b.toString();
  }

}
