package randoop.main;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * Class to hold the return status from running a command assuming that it is run in a process where
 * stderr and stdout are linked. Includes the exit status, and the list of output lines.
 */
class ProcessStatus {

  /** The command executed by the process. */
  final List<String> command;

  /** The exit status of the command. */
  final int exitStatus;

  /** The output from running the command. */
  final List<String> outputLines;

  /**
   * Creates a {@link ProcessStatus} object for the command with captured exit status, and output.
   *
   * @param command the command
   * @param exitStatus the exit status
   * @param outputLines the lines of process output
   */
  private ProcessStatus(List<String> command, int exitStatus, List<String> outputLines) {
    this.command = command;
    this.exitStatus = exitStatus;
    this.outputLines = outputLines;
  }

  static final String lineSep = System.lineSeparator();

  /** Outputs a verbose representation of this. */
  public String dump() {
    StringBuilder sb = new StringBuilder();
    sb.append("ProcessStatus dump:").append(lineSep);
    sb.append("command: ").append(command).append(lineSep);
    sb.append("exitStatus: ").append(exitStatus);
    if (exitStatus == 137) {
      sb.append(" (status 137 might mean out of memory)");
    }
    sb.append(lineSep);
    sb.append("outputlines:").append(lineSep);
    for (String line : outputLines) {
      sb.append(line).append(lineSep);
    }
    sb.append("End of ProcessStatus dump.").append(lineSep);
    return sb.toString();
  }

  /**
   * Runs the given command in a new process using the given timeout.
   *
   * <p>The process is run with a timeout of 15 minutes.
   *
   * @param command the command to be run in the process
   * @return the exit status and combined standard stream output
   */
  static ProcessStatus runCommand(List<String> command) {
    // The timeout limits are extremely generous.  Setting tight timeout limits
    // for individual tests has caused headaches when tests are run on Travis CI.
    // 15 minutes is longer than all tests currently take, even for a slow Travis run.
    long timeout = 15 * 60 * 1000; // use 15 minutes for timeout

    ProcessBuilder randoopBuilder = new ProcessBuilder(command);
    randoopBuilder.redirectErrorStream(true); // join standard output error & standard error streams

    String[] args = command.toArray(new String[0]);
    CommandLine cmdLine = new CommandLine(args[0]); // constructor requires executable name
    cmdLine.addArguments(Arrays.copyOfRange(args, 1, args.length));

    DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    DefaultExecutor executor = new DefaultExecutor();
    ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
    executor.setWatchdog(watchdog);

    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    PumpStreamHandler streamHandler =
        new PumpStreamHandler(outStream); // send both stderr and stdout
    executor.setStreamHandler(streamHandler);

    try {
      executor.execute(cmdLine, resultHandler);
    } catch (IOException e) {
      fail("Exception starting process: " + e);
    }

    int exitValue = -1;
    try {
      resultHandler.waitFor();
      exitValue = resultHandler.getExitValue();
    } catch (InterruptedException e) {
      fail("Exception running process: " + e);
    }
    boolean timedOut = executor.isFailure(exitValue) && watchdog.killedProcess();

    List<String> outputLines;
    try {
      String buf = outStream.toString();
      if (buf.length() == 0) {
        // Don't create a list with a single, empty element.
        outputLines = new ArrayList<>();
      } else {
        outputLines = Arrays.asList(buf.split(lineSep));
      }
    } catch (RuntimeException e) {
      fail("Exception getting output " + e); // do we need to ignore this?
      throw new Error("this can't happen");
    }

    if (timedOut) {
      for (String line : outputLines) {
        System.out.println(line);
      }
      fail("Process timed out after " + (timeout / 1000.0) + " msecs");
    }
    return new ProcessStatus(command, exitValue, outputLines);
  }
}
