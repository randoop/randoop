package randoop.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import randoop.Globals;



/**
 * The important method in this class is <code>exec(String[])</code>. It
 * executes its argument and pipes both stdout and stderr to System.out. Each
 * line in the piped output from stdout is prefixed with "OUT>" and the output
 * from stderr is prefixed with "ERR>"
 *
 * <p>
 * Credit: Producer code modified (and augmented) from Michael Daconta's
 * "Java Traps" column ("When Runtime.exec() won't"), found at
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 */
public class Command {

  public static void runCommand(String[] command,
      String prompt,
      boolean verbose,
      String nonVerboseMessage,
      boolean gobbleChars) {

    runCommand(command, prompt, verbose, nonVerboseMessage, false, gobbleChars);
  }

  public static void runCommandOKToFail(String[] command,
      String prompt,
      boolean verbose,
      String nonVerboseMessage,
      boolean gobbleChars) {

    runCommand(command, prompt, verbose, nonVerboseMessage, true, gobbleChars);
  }

  public static void runCommand(String[] command,
      String prompt,
      boolean verbose,
      String nonVerboseMessage,
      boolean okToFail,
      boolean gobbleChars) {


    System.out.println(nonVerboseMessage);

    ByteArrayOutputStream out = null;
    int exitFlag = 0;

    if (verbose) {
      exitFlag = Command.exec(command, System.out, prompt, gobbleChars);
    } else {
      out = new ByteArrayOutputStream();
      exitFlag = Command.exec(command, new PrintStream(out), prompt, gobbleChars);
    }

    if (!okToFail && exitFlag != 0) {
      throw new Error("Non-zero exit flag when running command "
          + java.util.Arrays.toString(command)
          + Globals.lineSep
          + (verbose
              ? "" // already output to System.out
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
    StreamGobbler(InputStream is, String type, PrintStream out, boolean gobbleChars) {
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

  private static String[] tokenize(String s) {
    if (s.length() == 0)
      throw new IllegalArgumentException("Empty command");
    return s.split(" ");
  }

  /**
   * Runs cmd, redirecting stdout and stderr to System.out.
   */
  public static int exec(String cmd) {
    return exec(tokenize(cmd), System.out);
  }

  /**
   * Runs cmd, redirecting stdout and stderr to System.out.
   */
  public static int exec(String[] cmd) {
    return exec(cmd, System.out);
  }

  /**
   * Runs cmd, redirecting stdout and stderr to `out' and prefixing the output
   * from stout with "OUT>" and the output from stderr with "ERR>".
   *
   * Returns whatever exit number is returned by the subprocess invoking the
   * command.
   */
  //     public static int exec(String cmd, PrintStream out) {
  //         return exec(cmd,  out, new File(System.getProperty("user.dir")));
  //     }

  public static int exec(String[] cmd, PrintStream out) {
    return exec(cmd, out, "");
  }

  public static int exec(String[] cmd, PrintStream out, String prompt) {
    return exec(cmd, out, prompt, false);
  }

  public static int exec(String[] cmd, PrintStream out, String prompt, boolean gobbleChars) {
    int exitVal;
    try {
      Runtime rt = Runtime.getRuntime();
      Process proc = rt.exec(cmd);

      // any error message?
      StreamGobbler errorGobbler =
        new StreamGobbler(proc.getErrorStream(), prompt, out, gobbleChars);

      // any output?
      StreamGobbler outputGobbler =
        new StreamGobbler(proc.getInputStream(), prompt, out, gobbleChars);

      // kick them off
      errorGobbler.start();
      outputGobbler.start();

      exitVal = proc.waitFor();
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(); // CP improve
    }
    return exitVal;
  }


}
