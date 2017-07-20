package randoop.execution;

import static org.apache.commons.codec.CharEncoding.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import plume.TimeLimitProcess;

/**
 * Class to hold the return status from running a command assuming that it is run in a process where
 * stderr and stdout are linked. Includes the exit status, and the list of output lines.
 */
class RunEnvironment {

  /**
   * Runs the given command in a new process using the given timeout.
   *
   * @param command the command to be run in the process
   * @param workingDirectory the working directory for this command
   * @param timeout the timeout for executing the process
   * @return the {@link RunEnvironment} capturing the outcome of executing the command
   * @throws ProcessException if there is an error running the command
   */
  static RunStatus run(List<String> command, File workingDirectory, long timeout)
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
      standardOutputLines = captureLinesFromStream(p.getInputStream());
    } catch (IOException e) {
      throw new ProcessException("Exception getting process stream output", e);
    }

    List<String> errorOutputLines;
    try {
      errorOutputLines = captureLinesFromStream(p.getErrorStream());
    } catch (IOException e) {
      throw new ProcessException("Error getting process error output", e);
    }

    if (p.timed_out()) {
      for (String line : standardOutputLines) {
        System.out.println(line);
      }
      assert !p.timed_out() : "Process timed out after " + p.timeout_msecs() + " msecs";
    }

    return new RunStatus(command, exitValue, standardOutputLines, errorOutputLines);
  }

  /**
   * Captures output from the stream as a {@code List<String>}.
   *
   * @param stream the stream to read from
   * @return the list of lines read from the stream
   * @throws IOException if there is an error reading from the stream
   */
  private static List<String> captureLinesFromStream(InputStream stream) throws IOException {
    List<String> outputLines = new ArrayList<>();
    try (BufferedReader rdr = new BufferedReader(new InputStreamReader(stream, UTF_8))) {
      String line = rdr.readLine();
      while (line != null) {
        outputLines.add(line);
        line = rdr.readLine();
      }
    }
    return outputLines;
  }
}
