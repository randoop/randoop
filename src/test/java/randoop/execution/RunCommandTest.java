package randoop.execution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** Tests for {@link RunCommand}. */
public class RunCommandTest {

  /** Prints each command-line argument on its own line. */
  public static class PrintArgs {
    /**
     * Prints each command-line argument on its own line.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
      for (String arg : args) {
        System.out.println(arg);
      }
    }
  }

  @Test
  public void argumentsArePassedVerbatim() {
    List<String> args =
        Arrays.asList(
            "plain",
            "with space",
            "-javaagent:x.jar=--replacement-file='/a b' --dont-transform=/c",
            "it's",
            "say \"hi\"");
    List<String> command = new ArrayList<>();
    command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
    command.add("-cp");
    command.add(System.getProperty("java.class.path"));
    command.add(PrintArgs.class.getName());
    command.addAll(args);

    Path workingDirectory = Paths.get(System.getProperty("user.dir"));
    RunCommand.Status status = RunCommand.run(command, workingDirectory, 60_000);
    assertFalse(status.timedOut);
    assertEquals(status.toString(), 0, status.exitStatus);
    assertEquals(args, status.standardOutputLines);
  }
}
