package randoop.instrument;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.plumelib.options.Options;

/** Tests for {@link ReplaceCallAgent}. */
public class ReplaceCallAgentTest {

  @Test
  public void createAgentArgsTest() {
    Path replacementFile = Paths.get("my replacements.txt");
    Path dontTransformFile = Paths.get("don't transform.txt");
    ReplaceCallAgent.replacement_file = replacementFile;
    ReplaceCallAgent.dont_transform = dontTransformFile;
    String args = ReplaceCallAgent.createAgentArgs();

    // Parse the arguments as the agent does, into options that have different values.
    ReplaceCallAgent.replacement_file = Paths.get("other");
    ReplaceCallAgent.dont_transform = Paths.get("other");
    String[] targetArgs =
        new Options(ReplaceCallAgent.class).parse(true, ReplaceCallAgent.tokenize(args));

    assertArrayEquals(new String[0], targetArgs);
    assertEquals(replacementFile.toAbsolutePath(), ReplaceCallAgent.replacement_file);
    assertEquals(dontTransformFile.toAbsolutePath(), ReplaceCallAgent.dont_transform);
  }
}
