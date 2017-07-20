package randoop.execution;

import java.io.File;
import java.io.IOException;
import java.util.List;
import plume.TimeLimitProcess;
import randoop.util.StreamUtils;

/**
 * Class providing the {@link #run(List, File, long)} method to run a command in a separate process.
 */
public class RunCommand {

  /**
   * Runs the given command in the working directory in a new process using the given timeout. If
   * returns normally, returns a {@link Status} object capturing the command, exit status, and
   * output from the process.
   *
   * @param command the command to be run in the process
   * @param workingDirectory the working directory for this command
   * @param timeout the timeout in milliseconds for executing the process
   * @return the {@link Status} capturing the outcome of executing the command
   * @throws ProcessException if there is an error running the command
   */
  static Status run(List<String> command, File workingDirectory, long timeout)
      throws ProcessException {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDirectory);

    TimeLimitProcess p;

    try {
      p = new TimeLimitProcess(processBuilder.start(), timeout, true);
    } catch (IOException e) {
      throw new ProcessException("Exception starting process", e);
    }

    int exitValue;
    try {
      exitValue = p.waitFor();
    } catch (InterruptedException e) {
      throw new ProcessException("Exception running process", e);
    }

    List<String> standardOutputLines;
    try {
      standardOutputLines = StreamUtils.captureLinesFromStream(p.getInputStream());
    } catch (IOException e) {
      throw new ProcessException("Exception getting process stream output", e);
    }

    List<String> errorOutputLines;
    try {
      errorOutputLines = StreamUtils.captureLinesFromStream(p.getErrorStream());
    } catch (IOException e) {
      throw new ProcessException("Error getting process error output", e);
    }

    if (p.timed_out()) {
      for (String line : standardOutputLines) {
        System.out.println(line);
      }
      assert !p.timed_out() : "Process timed out after " + p.timeout_msecs() + " msecs";
    }

    return new Status(command, exitValue, standardOutputLines, errorOutputLines);
  }

  /**
   * Represents the status of a process that has been executed. Captures the command, exit status,
   * and lines written to standard output and error.
   */
  public static class Status {

    /** The command executed. */
    public final List<String> command;

    /** The exit status of the command */
    public final int exitStatus;

    /** The output from running the command. */
    public final List<String> standardOutputLines;

    /** The error output from running the command */
    public final List<String> errorOutputLines;

    /**
     * Creates a {@link Status} object for the command with captured exit status, and output.
     *
     * @param command the command
     * @param exitStatus the exit status
     * @param standardOutputLines the lines of process output
     * @param errorOutputLines the lines of process output to standard error
     */
    Status(
        List<String> command,
        int exitStatus,
        List<String> standardOutputLines,
        List<String> errorOutputLines) {
      this.command = command;
      this.exitStatus = exitStatus;
      this.standardOutputLines = standardOutputLines;
      this.errorOutputLines = errorOutputLines;
    }
  }
}
