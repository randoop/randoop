package randoop.execution;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

/** Tests for {@link RunCommand}. */
public class RunCommandTest {

  @Test
  public void toShellCommandTest() {
    assertEquals(
        "java -cp a.jar:b/c.jar 'with space' 'it'\\''s' '' '$HOME' '-javaagent:x.jar=--f='\\''a b'\\'''",
        RunCommand.toShellCommand(
            Arrays.asList(
                "java",
                "-cp",
                "a.jar:b/c.jar",
                "with space",
                "it's",
                "",
                "$HOME",
                "-javaagent:x.jar=--f='a b'")));
  }
}
