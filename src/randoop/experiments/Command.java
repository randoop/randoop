package randoop.experiments;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import randoop.Globals;


/**
 * The important method in this class is <code>exec(String)</code>. It
 * executes its argument and pipes both stdout and stderr to System.out. Each
 * line in the piped output from stdout is prefixed with "OUT>" and the output
 * from stderr is prefixed with "ERR>"
 *
 * <p>
 * Credit: Producer code modified (and augmented) from Michael Daconta's "Java
 * Traps" column ("When Runtime.exec() won't"), found at
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 */
public class Command {

  /** If true, commands will be printed, but not executed **/
  public static boolean no_execute = false;

  /** Argument to java command to set heap size */
  public static String javaHeapSize = "-Xmx1000m";

  public static void runCommand(String[] command, String prompt,
      boolean verbose, String nonVerboseMessage, boolean gobbleChars) {

    runCommand(command, prompt, verbose, nonVerboseMessage, false,
        gobbleChars);
  }

  public static void runCommandOKToFail(String[] command, String prompt,
      boolean verbose, String nonVerboseMessage, boolean gobbleChars) {

    runCommand(command, prompt, verbose, nonVerboseMessage, true,
        gobbleChars);
  }

  public static void runCommand(String[] command, String prompt,
      boolean verbose, String nonVerboseMessage, boolean okToFail,
      boolean gobbleChars) {

    System.out.println(nonVerboseMessage);

    ByteArrayOutputStream out = null;
    int exitFlag = 0;

    if (verbose) {
      exitFlag = Command.exec(command, System.out, System.err, prompt, gobbleChars);
    } else {
      out = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(out);
      exitFlag = Command.exec(command, printStream, printStream, prompt,
          gobbleChars);
    }

    if (!okToFail && exitFlag != 0) {
      throw new Error("Non-zero exit flag when running command "
          + java.util.Arrays.toString(command) + Globals.lineSep + (verbose ? "" // already
              // output
              // to
              // System.out
              : " output: " + String.valueOf(out)));
    }
  }

  /**
   * Helper class for Command. A StreamGobbler thread is Responsible for
   * redirecting an InputStream, prefixing its redirected output with a
   * user-specified String (see construtors for more details).
   *
   */
  public static class StreamGobbler extends Thread {
    InputStream is;

    String type;

    OutputStream os;

    PrintStream out;

    boolean gobbleChars;

    /**
     * Redirects `is' to out, prefixing each line with the String `type'.
     */
    StreamGobbler(InputStream is, String type, PrintStream out,
        boolean gobbleChars) {
      this(is, type, null, out, gobbleChars);

    }

    /*
     * Redirects `is' to out and also to `redirect' (that is, the input from
     * `is' is duplicated to both streams), prefixing each line with the
     * String `type'.
     */
    StreamGobbler(InputStream is, String type, OutputStream redirect,
        PrintStream out, boolean gobbleChars) {
      this.is = is;
      this.type = type;
      this.os = redirect;
      this.out = out;
      this.gobbleChars = gobbleChars;
    }

    @Override
    public void run() {
      try {
        PrintWriter pw = null;
        if (os != null)
          pw = new PrintWriter(os, true);

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        if (gobbleChars) {
          char[] oneChar = new char[1];
          while (br.read(oneChar, 0, 1) != -1) {
            char c = oneChar[0];
            if (pw != null) {
              pw.print(c);
            }
            out.print(oneChar[0]);
          }
        } else {
          String line = null;
          while ((line = br.readLine()) != null) {
            if (pw != null)
              pw.println(line);
            out.println(type + line);
          }
        }
        if (pw != null)
          pw.flush();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  /**
   * Runs cmd, redirecting stdout and stderr to System.out.
   */
  public static int exec(String[] cmd) {
    return exec(cmd, System.out, System.err);
  }

  /**
   * Runs cmd, redirecting stdout and stderr to `out' and prefixing the output
   * from stout with "OUT>" and the output from stderr with "ERR>".
   *
   * Returns whatever exit number is returned by the subprocess invoking the
   * command.
   */
  // public static int exec(String cmd, PrintStream out) {
  // return exec(cmd, out, new File(System.getProperty("user.dir")));
  // }
  public static int exec(String[] cmd, PrintStream out, PrintStream err) {
    return exec(cmd, out, err, "");
  }

  public static int exec(String[] cmd, PrintStream out, PrintStream err, String prompt) {
    return exec(cmd, out, err, prompt, false);
  }

  /** Convenience method. Equivalent to exec(cmd, out, err, prompt, gobbleChars, null). */
  public static int exec(String[] cmd, PrintStream out, PrintStream err, String prompt, boolean gobbleChars) {
    return exec(cmd, out, err, prompt, gobbleChars, 0, null);
  }

  public static int exec(String[] cmd, PrintStream out, PrintStream err, String prompt, boolean gobbleChars, File f) {
    return exec(cmd, out, err, prompt, gobbleChars, 0, null);
  }


  private static class DestroyableProcessThread extends Thread {

    private PrintStream out;
    private PrintStream err;
    private String prompt;
    private boolean gobbleChars;
    private int exitVal;
    private boolean processFinished;
    private Process process;

    /** workingDir can be null. */
    public DestroyableProcessThread(String[] cmd, PrintStream out, PrintStream err, String prompt,
        boolean gobbleChars, File workingDir) {
      this.out = out;
      this.err = err;
      this.prompt =prompt;
      this.gobbleChars = gobbleChars;
      this.processFinished = false;
      this.exitVal = 1;
      try {
        this.process = Runtime.getRuntime().exec(cmd, null, workingDir);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void run() {

      // any error message?
      StreamGobbler errorGobbler = new StreamGobbler(process
          .getErrorStream(), prompt, err, gobbleChars);

      // any output?
      StreamGobbler outputGobbler = new StreamGobbler(process
          .getInputStream(), prompt, out, gobbleChars);

      // kick them off
      errorGobbler.start();
      outputGobbler.start();

      try {
        this.exitVal = process.waitFor();
        errorGobbler.join();
        outputGobbler.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      this.processFinished = true;
    }
  }

  public static class KillBecauseTimeLimitExceed extends RuntimeException {

    public KillBecauseTimeLimitExceed(String message) {
      super(message);
    }

    private static final long serialVersionUID = 1L;

  }

  @SuppressWarnings("deprecation")
  public static int exec(String[] cmd, PrintStream out, PrintStream err, String prompt,
      boolean gobbleChars, int killAfterMillisPassed, File workingDir) {

    if (killAfterMillisPassed < 0) {
      throw new IllegalArgumentException("killAfterMillis must be >= 0.");
    }

    // If no_execute is set, just print the commands rather than executing them
    if (no_execute) {
      System.out.printf ("Not executing command: ");
      for (String c : cmd)
        System.out.printf ("%s ", c);
      System.out.printf ("%n");
      return 0;
    }

    DestroyableProcessThread thread = new DestroyableProcessThread(cmd, out, err, prompt, gobbleChars, workingDir);

    try {

      // Start the testclasses.
      thread.start();

      // If testclasses doesn't finish in testtime, suspend it.
      thread.join(killAfterMillisPassed);

      if (!thread.processFinished) {
        System.err.println("Exceeded max wait: destroying process.");
        thread.process.destroy();
        thread.stop();
        throw new KillBecauseTimeLimitExceed("Process did not finish after "
            + killAfterMillisPassed + "ms.");
      }
      // We use this deprecated method because it's the only way to
      // stop a thread no matter what it's doing.
      thread.stop();

    } catch (java.lang.InterruptedException e) {
      throw new RuntimeException(e);
    }
    return thread.exitVal;
  }

}
