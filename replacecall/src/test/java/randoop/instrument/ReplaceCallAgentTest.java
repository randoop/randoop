package randoop.instrument;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/** Tests for {@link ReplaceCallAgent}. */
public class ReplaceCallAgentTest {

  @Test
  public void removeDisabledReplacementsTest() throws IOException, ReplacementFileException {
    Path defaultFile = Paths.get("build/resources/main/default-replacements.txt");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(defaultFile);
    int originalSize = map.size();
    MethodSignature exit = MethodSignature.of("java.lang.System.exit(int)");
    MethodSignature size = MethodSignature.of("java.util.ArrayList.size()");
    assertTrue(map.containsKey(exit));
    assertFalse(map.containsKey(size));

    Set<MethodSignature> disabled = new LinkedHashSet<>(Arrays.asList(exit, size));
    Set<MethodSignature> unmatched = ReplaceCallAgent.removeDisabledReplacements(map, disabled);

    assertEquals(Collections.singleton(size), unmatched);
    assertFalse(map.containsKey(exit));
    assertFalse(map.containsKey(size));
    assertEquals(originalSize - 1, map.size());
  }

  @Test
  public void createAgentArgsTest() {
    Path replacementFile = Paths.get("my replacements.txt");
    Path dontTransformFile = Paths.get("dont-transform.txt");
    Path disabledFile = Paths.get("don't \"replace\".txt");
    String args =
        ReplaceCallAgent.createAgentArgs(
            replacementFile,
            dontTransformFile,
            Arrays.asList("java.lang.System.exit(int)", "a.B.m(int, java.lang.String)"),
            disabledFile);
    String[] expected = {
      "--replacement-file=" + replacementFile.toAbsolutePath(),
      "--dont-transform=" + dontTransformFile.toAbsolutePath(),
      "--disable-default-replacement=java.lang.System.exit(int)",
      "--disable-default-replacement=a.B.m(int, java.lang.String)",
      "--disable-default-replacements-file=" + disabledFile.toAbsolutePath()
    };
    assertArrayEquals(expected, ReplaceCallAgent.tokenize(args));
  }
}
