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
            + " for line: java.lang.Integer()"
            + " randoop.mock.java.util.RandomReplace.randomWithSeedZero()");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void inheritedConstructorTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/inheritedconstructor.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "inheritedconstructor.txt:2: Constructor java.lang.Integer.<init>() not found for line:"
            + " java.lang.Integer.<init>()"
            + " randoop.mock.java.util.RandomReplace.randomWithSeedZero()");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void missingMethodClassTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/missingmethodclass.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "missingmethodclass.txt:2: Class alpha.beta not found for line: alpha.beta.gamma(int)"
            + " randoop.mock.java.util.RandomReplace.randomWithSeedZero()");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void nestedConstructorDotTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/nestedconstructordot.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "nestedconstructordot.txt:2: Method SimpleEntry not found (for nested class"
            + " java.util.AbstractMap$SimpleEntry, write java.util.AbstractMap$SimpleEntry(...))");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void constructorReplacementWrongTypeTest() throws IOException, ReplacementFileException {
    Path file =
        Paths.get("build/resources/test/randoop/instrument/constructorreplacementwrongtype.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "constructorreplacementwrongtype.txt:2: Replacement"
            + " randoop.mock.java.util.RandomReplace.randomWithSeedZero() for"
            + " java.lang.Object.<init>() must have parameter types () and return type"
            + " java.lang.Object");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void nonStaticReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/nonstaticreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "nonstaticreplacement.txt:2: Replacement method java.lang.Object.hashCode() must be static"
            + " and non-private");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void constructorReplacementFormsTest() throws IOException, ReplacementFileException {
    Path file =
        Paths.get("build/resources/test/randoop/instrument/constructorreplacementforms.txt");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
    assertEquals(3, map.size());
    assertEquals(
        MethodSignature.of(
            "randoop.instrument.SampleReplacements.newStringBuilder(java.lang.String)"),
        map.get(MethodSignature.of("java.lang.StringBuilder.<init>(java.lang.String)")));
    assertEquals(
        MethodSignature.of(
            "randoop.instrument.SampleReplacements.newSimpleEntry(java.lang.Object,java.lang.Object)"),
        map.get(
            MethodSignature.of(
                "java.util.AbstractMap$SimpleEntry.<init>(java.lang.Object,java.lang.Object)")));
    assertEquals(
        MethodSignature.of("UnnamedPackageClass.make(int)"),
        map.get(MethodSignature.of("UnnamedPackageClass.<init>(int)")));
  }

  @Test
  public void nestedInitDotTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/nestedinitdot.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "nestedinitdot.txt:2: Class java.util.AbstractMap.SimpleEntry not found (for nested class"
            + " java.util.AbstractMap$SimpleEntry, write"
            + " java.util.AbstractMap$SimpleEntry.<init>(...))");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void doublyNestedConstructorDotTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/doublynestedconstructordot.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "doublynestedconstructordot.txt:2: Class randoop.instrument.SampleReplacements.Middle.Inner"
            + " not found (for nested class randoop.instrument.SampleReplacements$Middle$Inner,"
            + " write randoop.instrument.SampleReplacements$Middle$Inner(...))");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void privateReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/privatereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "privatereplacement.txt:2: Replacement method"
            + " randoop.instrument.SampleReplacements.privateNewRandom() must be static and"
            + " non-private");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void methodReplacementWrongTypeTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/methodreplacementwrongtype.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "methodreplacementwrongtype.txt:2: Replacement"
            + " randoop.instrument.SampleReplacements.badLength(java.lang.String) for"
            + " java.lang.String.length() must have parameter types (java.lang.String) and"
            + " return type int");
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
