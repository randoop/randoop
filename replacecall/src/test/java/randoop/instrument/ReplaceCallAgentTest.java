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
import org.plumelib.options.Options;

/** Tests for {@link ReplaceCallAgent}. */
public class ReplaceCallAgentTest {

  @Test
  public void removeExcludedReplacementsTest() throws IOException, ReplacementFileException {
    Path defaultFile = Paths.get("build/resources/main/default-replacements.txt");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(defaultFile);
    int originalSize = map.size();
    MethodSignature exit = MethodSignature.of("java.lang.System.exit(int)");
    MethodSignature size = MethodSignature.of("java.util.ArrayList.size()");
    assertTrue(map.containsKey(exit));
    assertFalse(map.containsKey(size));

    Set<MethodSignature> excluded = new LinkedHashSet<>(Arrays.asList(exit, size));
    Set<MethodSignature> unmatched = ReplaceCallAgent.removeExcludedReplacements(map, excluded);

    assertEquals(Collections.singleton(size), unmatched);
    assertFalse(map.containsKey(exit));
    assertFalse(map.containsKey(size));
    assertEquals(originalSize - 1, map.size());
  }

  @Test
  public void createAgentArgsTest() {
    Path replacementFile = Paths.get("replacements.txt");
    Path dontTransformFile = Paths.get("dont-transform.txt");
    Path excludeFile = Paths.get("exclude.txt");
    String args =
        ReplaceCallAgent.createAgentArgs(
            replacementFile,
            dontTransformFile,
            Arrays.asList("java.lang.System.exit(int)", "a.B.m(int,java.lang.String)"),
            excludeFile);
    String[] expected = {
      "--replacement-file=" + replacementFile.toAbsolutePath(),
      "--dont-transform=" + dontTransformFile.toAbsolutePath(),
      "--replacecall-exclude=java.lang.System.exit(int)",
      "--replacecall-exclude=a.B.m(int,java.lang.String)",
      "--replacecall-exclude-file=" + excludeFile.toAbsolutePath()
    };
    assertArrayEquals(expected, Options.tokenize(args));
  }
}
