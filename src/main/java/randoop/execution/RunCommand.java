package randoop.execution;

import static org.apache.commons.codec.CharEncoding.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import plume.TimeLimitProcess;
import plume.UtilMDE;
import randoop.Globals;
import randoop.util.Log;

/**
 * Class providing the {@link #run(List, File, long)} method to run a command in a separate process
 * with a timeout.
 */
public class RunCommand {

  /**
   * Runs the given command synchronously in the given directory using the given timeout. If the
   * command completes normally, returns a {@link Status} object capturing the command, exit status,
   * and output from the process.
   *
   * @param command the command to be run in the process
   * @param workingDirectory the working directory for the command
   * @param timeout the timeout in milliseconds for executing the process
   * @return the {@link Status} capturing the outcome of executing the command
   * @throws CommandException if there is an error running the command
   */
  static Status run(List<String> command, File workingDirectory, long timeout)
      throws CommandException {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDirectory);

    Log.logPrintf("RunCommand.run():%n");
    Log.logPrintf("  cd %s; %s%n", workingDirectory, UtilMDE.join(command, " "));
    Log.logPrintf("  timeout=%s, environment: %s%n", timeout, processBuilder.environment());
    // Temporary debugging output
    Log.logPrintf("  sun.boot.class.path=%s%n", System.getProperty("sun.boot.class.path"));
    Log.logPrintf("  java.class.path=%s%n", System.getProperty("java.class.path"));
    Log.logPrintf("  which java:%n");
    try {
      ProcessBuilder ps = new ProcessBuilder("which", "java");
      Log.logPrintf("  which java (2):%n");
      ps.redirectErrorStream(true);
      Log.logPrintf("  which java (3):%n");
      Process pr = ps.start();
      Log.logPrintf("  which java (4):%n");

      BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream(), UTF_8));
      Log.logPrintf("  which java (5):%n");
      String line;
      Log.logPrintf("  which java (6):%n");
      while ((line = in.readLine()) != null) {
        System.out.println(line);
      }
      Log.logPrintf("  which java (7):%n");
      pr.waitFor();
      Log.logPrintf("  which java (8):%n");

      in.close();
      Log.logPrintf("  which java (9):%n");
    } catch (IOException | InterruptedException t) {
      throw new Error(t);
    }

    TimeLimitProcess p;

    try {
      p = new TimeLimitProcess(processBuilder.start(), timeout, true);
    } catch (IOException e) {
      throw new CommandException("Exception starting process", e);
    }

    int exitValue = -1;
    try {
      exitValue = p.waitFor();
    } catch (InterruptedException e) {
      // Ignore exception, but p.timed_out() records that the process timed out.
    }

    List<String> standardOutputLines;
    try {
      standardOutputLines = UtilMDE.streamLines(p.getInputStream());
    } catch (IOException e) {
      throw new CommandException("Exception getting process standard output", e);
    }

    List<String> errorOutputLines;
    try {
      errorOutputLines = UtilMDE.streamLines(p.getErrorStream());
    } catch (IOException e) {
      throw new CommandException("Exception getting process error output", e);
    }

    return new Status(command, exitValue, p.timed_out(), standardOutputLines, errorOutputLines);
  }

  /**
   * Represents the status of a process that has been executed. Captures the command, exit status,
   * and lines written to standard output and error.
   */
  public static class Status {

    /** The command executed. */
    public final List<String> command;

    /** The exit status of the command. */
    public final int exitStatus;

    /** Whether the command process timed out. */
    public final boolean timedOut;

    /** The output from running the command. */
    public final List<String> standardOutputLines;

    /** The error output from running the command */
    public final List<String> errorOutputLines;

    /**
     * Creates a {@link Status} object for the command with captured exit status and output.
     *
     * <p>The output from command execution is captured as a {@code List} of output lines. This
     * avoids losing output from the command if the process is destroyed.
     *
     * @param command the command
     * @param exitStatus the exit status
     * @param timedOut whether the process timed out
     * @param standardOutputLines the lines of process output to standard output
     * @param errorOutputLines the lines of process output to standard error
     */
    Status(
        List<String> command,
        int exitStatus,
        boolean timedOut,
        List<String> standardOutputLines,
        List<String> errorOutputLines) {
      this.command = command;
      this.exitStatus = exitStatus;
      this.timedOut = timedOut;
      this.standardOutputLines = standardOutputLines;
      this.errorOutputLines = errorOutputLines;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(
          String.format(
              "Status for command \"%s\" = %d (timedOut=%s)",
              UtilMDE.join(command, " "), exitStatus, timedOut));
      describeLines("stdout", standardOutputLines, sb);
      describeLines("stderr", errorOutputLines, sb);
      return sb.toString();
    }

    /**
     * Print to sb the lines, or say how many lines there were.
     *
     * @param source the source of the lines, such as "stdout" or "stderr"
     * @param lines the lines
     * @param sb where to print the represenation
     */
    private void describeLines(String source, List<String> lines, StringBuilder sb) {
      if (lines.size() <= 2) {
        sb.append(", ");
        sb.append(source);
        sb.append("=\"");
        sb.append(UtilMDE.join(lines, Globals.lineSep));
        sb.append("\"");
        sb.append(Globals.lineSep);
      } else {
        sb.append(", ");
        sb.append(source);
        sb.append(" lines=");
        sb.append(lines.size());
      }
    }
  }

  /**
   * Exception representing an error that occured while running a process with {@link
   * RunCommand#run(List, File, long)}.
   */
  public static class CommandException extends Throwable {

    private static final long serialVersionUID = 736230736083495268L;

    /**
     * Creates a {@link CommandException} with a message and causing exception.
     *
     * @param message the exception message
     * @param cause the causing exception
     */
    CommandException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
