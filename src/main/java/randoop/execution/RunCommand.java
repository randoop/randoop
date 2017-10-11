package randoop.execution;

import java.io.File;
import java.io.IOException;
import java.util.List;
import plume.TimeLimitProcess;
import plume.UtilMDE;

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
      // Ignore exception, but record fact that process timed out in Status
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
