package randoop.instrument;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
        "missingclassreplacement.txt:2: Class not found: randoop.mock.Gamma for line: alpha.beta.Gamma.delta() randoop.mock.Gamma.delta(alpha.beta.Gamma)";
    thrown.expectMessage(msg);
    HashMap<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void missingMethodMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/missingmethodreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "missingmethodreplacement.txt:2: Method not found: randoop.mock.java.awt.Component.delta() for line: alpha.beta.Component.delta() randoop.mock.java.awt.Component.delta()";
    thrown.expectMessage(msg);
    HashMap<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badFormatMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/badformatreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "badformatreplacement.txt:2: Error in replacement file: bad format for line: 19 ways alpha.beta.Gamma.delta() 3 fine randoop.mock.alpha.beta.Gamma.delta() zip";
    thrown.expectMessage(msg);

    HashMap<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badArgTypeMethodTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/badargumenttypereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg =
        "badargumenttypereplacement.txt:1: Class not found: alpha.beta.Gamma for line: alpha.beta.Gamma.delta() randoop.mock.java.awt.Component.show(alpha.beta.Gamma)";
    thrown.expectMessage(msg);
    HashMap<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void missingClassClassTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/missingclass.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "missingclass.txt:1: No package for replacement randoop.mock.Gamma found on classpath for line: alpha.beta.Gamma randoop.mock.Gamma");
    HashMap<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void classReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/classreplacement.txt");
    HashMap<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
    assertThat("mock Component file has 5 methods", map.size(), is(equalTo(5)));
  }

  @Test
  public void packageReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/packagereplacement.txt");
    HashMap<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
    assertThat("mock package has 75 methods", map.size(), is(equalTo(75)));

    Path defaultFile = Paths.get("build/resources/main/default-replacements.txt");
    HashMap<MethodSignature, MethodSignature> defaultMap =
        ReplacementFileReader.readReplacements(defaultFile);
    assertThat("default file loads 75 methods", map.size(), is(equalTo(75)));
  }
}
