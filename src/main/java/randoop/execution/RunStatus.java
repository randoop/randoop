package randoop.execution;

import java.util.List;

/**
 * Represents the status of a process that has been executed. Captures the command, exit status, and
 * stream output on standared output and error.
 */
public class RunStatus {

  /** The command executed. */
  public final List<String> command;

  /** The exit status of the command */
  public final int exitStatus;

  /** The output from running the command. */
  public final List<String> standardOutputLines;

  /** The error output from running the command */
  public final List<String> errorOutputLines;

  /**
   * Creates a {@link RunStatus} object for the command with captured exit status, and output.
   *
   * @param command the command
   * @param exitStatus the exit status
   * @param standardOutputLines the lines of process output
   * @param errorOutputLines the lines of process output to standard error
   */
  public RunStatus(
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
