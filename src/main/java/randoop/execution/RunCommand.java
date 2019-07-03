package randoop.execution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.plumelib.util.UtilPlume;
import randoop.Globals;
import randoop.util.Log;

/**
 * Class providing the {@link #run(List, Path, long)} method to run a command in a separate process
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
  static Status run(List<String> command, Path workingDirectory, long timeout)
      throws CommandException {

    String[] args = command.toArray(new String[0]);
    CommandLine cmdLine = new CommandLine(args[0]); // constructor requires executable name
    cmdLine.addArguments(Arrays.copyOfRange(args, 1, args.length));

    DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    DefaultExecutor executor = new DefaultExecutor();
    executor.setWorkingDirectory(workingDirectory.toFile());

    ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
    executor.setWatchdog(watchdog);

    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outStream, errStream);
    executor.setStreamHandler(streamHandler);

    Log.logPrintf("RunCommand.run():%n");
    Log.logPrintf("  cd %s; %s%n", workingDirectory, UtilPlume.join(command, " "));
    Log.logPrintf("  timeout=%s, environment: %s%n", timeout, System.getenv());

    try {
      executor.execute(cmdLine, resultHandler);
    } catch (IOException e) {
      throw new CommandException("Exception starting process", e);
    }

    int exitValue = -1;
    try {
      resultHandler.waitFor();
      exitValue = resultHandler.getExitValue();
    } catch (InterruptedException e) {
      // Ignore exception, but watchdog.killedProcess() records that the process timed out.
    }
    boolean timedOut = executor.isFailure(exitValue) && watchdog.killedProcess();

    List<String> standardOutputLines;
    try {
      standardOutputLines = Arrays.asList(outStream.toString().split(Globals.lineSep));
    } catch (RuntimeException e) {
      throw new CommandException("Exception getting process standard output", e);
    }

    List<String> errorOutputLines;
    try {
      errorOutputLines = Arrays.asList(errStream.toString().split(Globals.lineSep));
    } catch (RuntimeException e) {
      throw new CommandException("Exception getting process error output", e);
    }

    return new Status(command, exitValue, timedOut, standardOutputLines, errorOutputLines);
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

    /** The error output from running the command. */
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
              "Status %d (timedOut=%s) for command \"%s\"",
              exitStatus, timedOut, UtilPlume.join(command, " ")));
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
        sb.append(UtilPlume.join(lines, Globals.lineSep));
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
   * RunCommand#run(List, Path, long)}.
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
