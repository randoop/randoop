package randoop.instrument;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for the {@link ReplacementFileReader}. */
public class ReplacementFileTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void missingClassMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/missingclassreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "missingclassreplacement.txt:2: Class randoop.mock.Gamma not found for line:"
            + " randoop.MethodReplacements.getAgentPath()"
            + " randoop.mock.Gamma.delta(alpha.beta.Gamma)";
    thrown.expectMessage(msg);
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void missingMethodMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/missingmethodreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "missingmethodreplacement.txt:2: Method delta not found for line:"
            + " randoop.MethodReplacements.getAgentPath() randoop.mock.java.awt.Component.delta()";
    thrown.expectMessage(msg);
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badFormatMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/badformatreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "badformatreplacement.txt:2: Error in replacement file: bad format for line: 19 ways"
            + " alpha.beta.Gamma.delta() 3 fine randoop.mock.alpha.beta.Gamma.delta() zip";
    thrown.expectMessage(msg);

    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badArgTypeMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/badargumenttypereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "badargumenttypereplacement.txt:2: Method show not found for line:"
            + " randoop.MethodReplacements.getAgentPath()"
            + " randoop.mock.java.awt.Component.show(alpha.beta.Gamma)";
    thrown.expectMessage(msg);
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void missingClassClassTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/missingclass.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "missingclass.txt:1: No package for replacement randoop.mock.Gamma found on classpath for"
            + " line: alpha.beta.Gamma randoop.mock.Gamma");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void classReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/classreplacement.txt");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
    assertEquals(5, map.size());
  }

  @Test
  public void constructorReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/constructorreplacement.txt");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
    assertEquals(1, map.size());
    assertEquals(
        MethodSignature.of("randoop.mock.java.util.RandomReplace.randomWithSeedZero()"),
        map.get(MethodSignature.of("java.util.Random.<init>()")));
  }

  @Test
  public void missingConstructorClassTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/missingconstructorclass.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "missingconstructorclass.txt:2: Class alpha.beta not found (nor class alpha.beta.Gamma,"
            + " for a constructor) for line: alpha.beta.Gamma(int)"
            + " randoop.mock.java.util.RandomReplace.randomWithSeedZero()");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void inheritedConstructorShorthandTest() throws IOException, ReplacementFileException {
    Path file =
        Paths.get("build/resources/test/randoop/instrument/inheritedconstructorshorthand.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "inheritedconstructorshorthand.txt:2: Constructor java.lang.Integer.<init>() not found"
            + " for line: java.lang.Integer() randoop.mock.java.util.RandomReplace.randomWithSeedZero()");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void packageReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/packagereplacement.txt");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
    assertEquals(75, map.size());

    Path defaultFile = Paths.get("build/resources/main/default-replacements.txt");
    Map<MethodSignature, MethodSignature> defaultMap =
        ReplacementFileReader.readReplacements(defaultFile);
    assertEquals(75, map.size());
  }
}
