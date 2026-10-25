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
        "missingclassreplacement.txt:2: Bad replacement method: Class randoop.mock.Gamma not found"
            + " for line:"
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
        "missingmethodreplacement.txt:2: Bad replacement method: Method"
            + " randoop.mock.java.awt.Component.delta() not found for line:"
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
  public void badSignatureMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/badsignaturereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "badsignaturereplacement.txt:2: Bad original signature: Bad parameter type \"int x\" for"
            + " line: randoop.MethodReplacements.getAgentPath(int x)"
            + " randoop.mock.java.awt.Component.show()";
    thrown.expectMessage(msg);
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badMethodNameMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/badmethodnamereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "badmethodnamereplacement.txt:2: Bad original signature: Method signature expected, bad"
            + " fully-qualified method name \"foo\" for line:"
            + " foo(int) randoop.mock.java.awt.Component.show()";
    thrown.expectMessage(msg);
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badReplacementSignatureMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/badreplacementsignature.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "badreplacementsignature.txt:2: Bad replacement signature: Bad parameter type \"int x\""
            + " for line: randoop.MethodReplacements.getAgentPath()"
            + " randoop.mock.java.awt.Component.show(int x)";
    thrown.expectMessage(msg);
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badClassFileMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/badclassfilereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "badclassfilereplacement.txt:2: Bad original method: Unable to read"
            + " randoop.instrument.NotAClass: Error reading class file:"
            + " randoop/instrument/NotAClass.class: ";
    thrown.expectMessage(msg);
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badArgTypeMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/badargumenttypereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "badargumenttypereplacement.txt:2: Bad replacement method: Method"
            + " randoop.mock.java.awt.Component.show(alpha.beta.Gamma) not found for line:"
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
