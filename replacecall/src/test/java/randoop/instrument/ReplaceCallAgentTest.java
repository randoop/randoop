package randoop.instrument;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.plumelib.options.Options;

/** Tests for {@link ReplaceCallAgent}. */
public class ReplaceCallAgentTest {

  @Test
  public void createAgentArgsTest() {
    Path debugDirectory = Paths.get("my debug dir").toAbsolutePath();
    Path replacementFile = Paths.get("my replacements.txt");
    Path dontTransformFile = Paths.get("don't transform.txt");
    ReplaceCallAgent.debug = true;
    ReplaceCallAgent.debugPath = debugDirectory;
    ReplaceCallAgent.verbose = true;
    ReplaceCallAgent.replacement_file = replacementFile;
    ReplaceCallAgent.dont_transform = dontTransformFile;
    String args = ReplaceCallAgent.createAgentArgs();

    // Parse the arguments as the agent does, into options that have different values.
    ReplaceCallAgent.debug = false;
    ReplaceCallAgent.debug_directory = "other";
    ReplaceCallAgent.verbose = false;
    ReplaceCallAgent.replacement_file = Paths.get("other");
    ReplaceCallAgent.dont_transform = Paths.get("other");
    String[] targetArgs =
        new Options(ReplaceCallAgent.class).parse(true, ReplaceCallAgent.tokenize(args));

    assertArrayEquals(new String[0], targetArgs);
    assertTrue(ReplaceCallAgent.debug);
    assertEquals(debugDirectory.toString(), ReplaceCallAgent.debug_directory);
    assertTrue(ReplaceCallAgent.verbose);
    assertEquals(replacementFile.toAbsolutePath(), ReplaceCallAgent.replacement_file);
    assertEquals(dontTransformFile.toAbsolutePath(), ReplaceCallAgent.dont_transform);
  }
}
