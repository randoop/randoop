package randoop.instrument;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.plumelib.options.Options;
import org.plumelib.options.Options.ArgException;

/** Tests for {@link ReplaceCallAgent}. */
public class ReplaceCallAgentTest {

  /** The value of {@link ReplaceCallAgent#replacement_file} before the test. */
  private Path origReplacementFile;

  /** The value of {@link ReplaceCallAgent#dont_transform} before the test. */
  private Path origDontTransform;

  /** The value of {@link ReplaceCallAgent#disable_default_replacement} before the test. */
  private List<String> origDisableDefaultReplacement;

  /** The value of {@link ReplaceCallAgent#disable_default_replacements_file} before the test. */
  private Path origDisableDefaultReplacementsFile;

  /** Saves the agent's options, which each test changes. */
  @Before
  public void saveOptions() {
    origReplacementFile = ReplaceCallAgent.replacement_file;
    origDontTransform = ReplaceCallAgent.dont_transform;
    origDisableDefaultReplacement = ReplaceCallAgent.disable_default_replacement;
    origDisableDefaultReplacementsFile = ReplaceCallAgent.disable_default_replacements_file;
  }

  /** Restores the agent's options. */
  @After
  @SuppressWarnings("nullness:monotonic") // restores the options' original values, possibly null
  public void restoreOptions() {
    ReplaceCallAgent.replacement_file = origReplacementFile;
    ReplaceCallAgent.dont_transform = origDontTransform;
    ReplaceCallAgent.disable_default_replacement = origDisableDefaultReplacement;
    ReplaceCallAgent.disable_default_replacements_file = origDisableDefaultReplacementsFile;
  }

  /**
   * Parses an agent argument string as the agent does, and checks that it sets the replacement file
   * and the exclusion file to the given values.
   *
   * @param agentArgs an agent argument string
   * @param replacementFile the expected replacement file
   * @param dontTransform the expected exclusion file
   * @throws ArgException if {@code agentArgs} cannot be parsed
   */
  private static void checkParse(String agentArgs, Path replacementFile, Path dontTransform)
      throws ArgException {
    // Start from options that have different values.
    ReplaceCallAgent.replacement_file = Paths.get("other");
    ReplaceCallAgent.dont_transform = Paths.get("other");
    String[] targetArgs =
        new Options(ReplaceCallAgent.class).parse(true, ReplaceCallAgent.tokenize(agentArgs));

    assertArrayEquals(new String[0], targetArgs);
    assertEquals(replacementFile, ReplaceCallAgent.replacement_file);
    assertEquals(dontTransform, ReplaceCallAgent.dont_transform);
  }

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
  public void createAgentArgsTest() throws ArgException {
    Path replacementFile = Paths.get("my replacements.txt");
    Path dontTransformFile = Paths.get("dont-transform.txt");
    Path disabledFile = Paths.get("don't replace.txt");
    ReplaceCallAgent.replacement_file = replacementFile;
    ReplaceCallAgent.dont_transform = dontTransformFile;
    ReplaceCallAgent.disable_default_replacement =
        new ArrayList<>(
            Arrays.asList("java.lang.System.exit(int)", "a.B.m(int, java.lang.String)"));
    ReplaceCallAgent.disable_default_replacements_file = disabledFile;
    String args = ReplaceCallAgent.createAgentArgs();
    String[] expected = {
      "--replacement-file=" + replacementFile.toAbsolutePath(),
      "--dont-transform=" + dontTransformFile.toAbsolutePath(),
      "--disable-default-replacement=java.lang.System.exit(int)",
      "--disable-default-replacement=a.B.m(int, java.lang.String)",
      "--disable-default-replacements-file=" + disabledFile.toAbsolutePath()
    };
    assertArrayEquals(expected, ReplaceCallAgent.tokenize(args));

    checkParse(args, replacementFile.toAbsolutePath(), dontTransformFile.toAbsolutePath());
  }

  @Test
  public void parseWhitespaceSeparated() throws ArgException {
    checkParse(
        "--replacement-file=r.txt  --dont-transform=d.txt", Paths.get("r.txt"), Paths.get("d.txt"));
  }

  @Test
  public void parseCommaSeparated() throws ArgException {
    checkParse(
        "--replacement-file=r.txt,--dont-transform=d.txt", Paths.get("r.txt"), Paths.get("d.txt"));
  }

  @Test
  public void parseQuoted() throws ArgException {
    checkParse(
        "--replacement-file='my r.txt' --dont-transform=\"my d.txt\"",
        Paths.get("my r.txt"),
        Paths.get("my d.txt"));
    // Quotes may surround part of a value, and a quoted value may contain the other quote.
    checkParse(
        "--replacement-file=dir/'my r.txt' \"--dont-transform=it's d.txt\"",
        Paths.get("dir/my r.txt"),
        Paths.get("it's d.txt"));
  }

  @Test
  public void getJarPathFromURLTest() throws MalformedURLException {
    URL url = new URL("jar:file:/home/u/my%20tools/replacecall.jar!/randoop/instrument/A.class");
    assertEquals(
        Paths.get("/home/u/my tools/replacecall.jar").toString(),
        ReplaceCallAgent.getJarPathFromURL(url));
  }
}
