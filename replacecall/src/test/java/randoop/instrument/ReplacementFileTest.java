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
  public void nonStaticReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/nonstaticreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "nonstaticreplacement.txt:2: Replacement method must be public and static:"
            + " randoop.mock.java.util.RandomReplace.<init>()");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void instanceReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/instancereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "instancereplacement.txt:2: Replacement method must be public and static:"
            + " java.lang.Object.hashCode()");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void instanceMethodReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/instancemethodreplacement.txt");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
    assertEquals(1, map.size());
  }

  @Test
  public void receiverReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/receiverreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "receiverreplacement.txt:2: Replacement method int java.lang.Math.abs(int) must have"
            + " signature int abs(java.lang.String)");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void returnTypeReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/returntypereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "returntypereplacement.txt:2: Replacement method int java.lang.Math.abs(int) must have"
            + " signature void abs(int)");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void privateReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/privatereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "privatereplacement.txt:2: Replacement method must be public and static:"
            + " randoop.instrument.testmocks.MethodMocks.abs(int)");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void constructorReturnTypeReplacementTest() throws IOException, ReplacementFileException {
    Path file =
        Paths.get("build/resources/test/randoop/instrument/constructorreturntypereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "constructorreturntypereplacement.txt:2: Replacement method java.lang.String"
            + " randoop.instrument.testmocks.MethodMocks.newObject() must have signature java.lang.Object"
            + " newObject()");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void nonPublicReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/nonpublicreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "nonpublicreplacement.txt:2: Replacement method must be public and static:"
            + " randoop.instrument.ReplacementTestMethods.packagePrivateNextInt(java.util.Random)");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void nonPublicClassReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/nonpublicclassreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "nonpublicclassreplacement.txt:2: Replacement class must be public:"
            + " randoop.instrument.NonPublicReplacementClass");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void interfaceReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/interfacereplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "interfacereplacement.txt:2: Replacement class must not be an interface:"
            + " randoop.instrument.ReplacementTestMethods$ReplacementInterface");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void missingReceiverReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/missingreceiverreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "missingreceiverreplacement.txt:2: Replacement method int"
            + " randoop.instrument.ReplacementTestMethods.nextIntWithoutReceiver() must have"
            + " signature int nextIntWithoutReceiver(java.util.Random)");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void wrongReturnReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/wrongreturnreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "wrongreturnreplacement.txt:2: Replacement method long"
            + " randoop.instrument.ReplacementTestMethods.nextIntReturningLong(java.util.Random)"
            + " must have signature int nextIntReturningLong(java.util.Random)");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void nonPublicMethodClassReplacementTest() throws IOException, ReplacementFileException {
    Path file =
        Paths.get("build/resources/test/randoop/instrument/nonpublicmethodclassreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "nonpublicmethodclassreplacement.txt:2: Replacement method must be public and static:"
            + " randoop.instrument.ReplacementTestMethods$PackagePrivateMethodClass.nextInt(java.util.Random)");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void nonPublicClassClassReplacementTest() throws IOException, ReplacementFileException {
    Path file =
        Paths.get("build/resources/test/randoop/instrument/nonpublicclassclassreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "nonpublicclassclassreplacement.txt:2: Replacement class must be public:"
            + " randoop.instrument.NonPublicReplacementClass");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void wrongReturnClassReplacementTest() throws IOException, ReplacementFileException {
    Path file =
        Paths.get("build/resources/test/randoop/instrument/wrongreturnclassreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "wrongreturnclassreplacement.txt:2: Replacement method"
            + " randoop.instrument.ReplacementTestMethods$WrongReturnClass.nextInt(java.util.Random)"
            + " must have return type int");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
  }

  @Test
  public void missingReceiverClassReplacementTest() throws IOException, ReplacementFileException {
    Path file =
        Paths.get("build/resources/test/randoop/instrument/missingreceiverclassreplacement.txt");
    thrown.expect(ReplacementFileException.class);
    thrown.expectMessage(
        "missingreceiverclassreplacement.txt:2: Replacement method"
            + " randoop.instrument.ReplacementTestMethods$MissingReceiverClass.compareTo(java.lang.String)"
            + " has no matching original in java.lang.String");
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

  @Test
  public void jcommanderReplacementTest() throws IOException, ReplacementFileException {
    Path file = Paths.get("build/resources/test/randoop/instrument/jcommanderreplacement.txt");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
    assertEquals(1, map.size());
    assertEquals(
        MethodSignature.of(
            "randoop.mock.com.beust.jcommander.internal.Console.readPassword("
                + "com.beust.jcommander.internal.Console, boolean)"),
        map.get(MethodSignature.of("com.beust.jcommander.internal.Console.readPassword(boolean)")));
  }
}
