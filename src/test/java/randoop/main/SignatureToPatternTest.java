package randoop.main;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;
import org.junit.Test;

/** Tests {@link GenTests#signatureToPattern}. */
public class SignatureToPatternTest {

  @Test
  public void testConstructor() {
    Pattern p = GenTests.signatureToPattern("java.util.Random.<init>()");
    assertTrue(p.matcher("java.util.Random()").find());
    assertFalse(p.matcher("java.util.Random(long)").find());
  }

  @Test
  public void testMethod() {
    Pattern p =
        GenTests.signatureToPattern(
            "java.lang.System.getProperty(java.lang.String, java.lang.String)");
    assertTrue(p.matcher("java.lang.System.getProperty(java.lang.String,java.lang.String)").find());
    assertFalse(p.matcher("java.lang.System.getProperty(java.lang.String)").find());
    assertFalse(
        p.matcher("my.java.lang.System.getProperty(java.lang.String,java.lang.String)").find());
  }
}
