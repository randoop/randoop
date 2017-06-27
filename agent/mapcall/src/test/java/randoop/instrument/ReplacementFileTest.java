package randoop.instrument;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Created by bjkeller on 6/16/17. */
public class ReplacementFileTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void missingClassMethodTest() throws IOException, ReplacementFileException {
    File file = new File("build/resources/test/randoop/instrument/missingclassreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg = "Class randoop.mock.Gamma not found for replacement method";
    thrown.expectMessage(msg);
    ConcurrentHashMap<MethodDef, MethodDef> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void missingMethodMethodTest() throws IOException, ReplacementFileException {
    File file = new File("build/resources/test/randoop/instrument/missingmethodreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg = "Replacement method not found randoop.mock.java.awt.Component.delta()";
    thrown.expectMessage(msg);
    ConcurrentHashMap<MethodDef, MethodDef> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badFormatMethodTest() throws IOException, ReplacementFileException {
    File file = new File("build/resources/test/randoop/instrument/badformatreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg = "Error in replacement file: bad format";
    thrown.expectMessage(msg);
    ConcurrentHashMap<MethodDef, MethodDef> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void badArgTypeMethodTest() throws IOException, ReplacementFileException {
    File file = new File("build/resources/test/randoop/instrument/badargumenttypereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    String msg = "Class not found for replacement argument type alpha.beta.Gamma";
    thrown.expectMessage(msg);
    ConcurrentHashMap<MethodDef, MethodDef> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void missingClassClassTest() throws IOException, ReplacementFileException {
    File file = new File("build/resources/test/randoop/instrument/missingclass.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage("No package or class for replacement randoop.mock.Gamma");
    ConcurrentHashMap<MethodDef, MethodDef> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void classReplacementTest() throws IOException, ReplacementFileException {
    File file = new File("build/resources/test/randoop/instrument/classreplacement.txt");
    ConcurrentHashMap<MethodDef, MethodDef> map = ReplacementFileReader.readReplacements(file);
    assertThat("mock Component file has 5 methods", map.size(), is(equalTo(5)));
  }

  @Test
  public void packageReplacementTest() throws IOException, ReplacementFileException {
    File file = new File("build/resources/test/randoop/instrument/packagereplacement.txt");
    ConcurrentHashMap<MethodDef, MethodDef> map = ReplacementFileReader.readReplacements(file);
    assertThat("mock package has more methods than I want to count", map.size(), is(equalTo(75)));

    File defaultFile = new File("build/resources/main/default-replacements.txt");
    ConcurrentHashMap<MethodDef, MethodDef> defaultMap =
        ReplacementFileReader.readReplacements(defaultFile);
    assertThat("default file has more methods than I want to count", map.size(), is(equalTo(75)));
  }
}
