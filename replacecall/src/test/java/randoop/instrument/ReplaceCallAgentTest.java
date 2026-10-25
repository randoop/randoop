package randoop.instrument;

import static org.junit.Assert.assertArrayEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

/** Tests for {@link ReplaceCallAgent}. */
public class ReplaceCallAgentTest {

  @Test
  public void createAgentArgsTest() {
    Path replacementFile = Paths.get("my replacements.txt");
    Path dontTransformFile = Paths.get("don't transform.txt");
    String args = ReplaceCallAgent.createAgentArgs(replacementFile, dontTransformFile);
    String[] expected = {
      "--replacement-file=" + replacementFile.toAbsolutePath(),
      "--dont-transform=" + dontTransformFile.toAbsolutePath()
    };
    assertArrayEquals(expected, ReplaceCallAgent.tokenize(args));
  }
}
