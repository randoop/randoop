package randoop.execution;

import java.util.List;

/** Created by bjkeller on 6/30/17. */
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
