package randoop.instrument;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Tests for {@link MethodSignature#toRawSignatureString}. */
public class RawSignatureStringTest {

  @Test
  public void methodRawSignatureStringTest() {
    assertEquals(
        "java.lang.Math.max(int,int)",
        MethodSignature.of("java.lang.Math.max(int,int)").toRawSignatureString());
    assertEquals(
        "java.lang.String.valueOf(char[])",
        MethodSignature.of("java.lang.String.valueOf(char[])").toRawSignatureString());
  }

  @Test
  public void nestedClassMethodRawSignatureStringTest() {
    assertEquals(
        "java.util.AbstractMap$SimpleEntry.getKey()",
        MethodSignature.of("java.util.AbstractMap$SimpleEntry.getKey()").toRawSignatureString());
    assertEquals(
        "java.util.AbstractMap$SimpleEntry.equals(java.lang.Object)",
        MethodSignature.of("java.util.AbstractMap$SimpleEntry.equals(java.lang.Object)")
            .toRawSignatureString());
  }

  @Test
  public void constructorRawSignatureStringTest() {
    assertEquals(
        "java.util.Random()",
        MethodSignature.of("java.util.Random.<init>()").toRawSignatureString());
    assertEquals(
        "java.util.AbstractMap$SimpleEntry(java.util.Map$Entry)",
        MethodSignature.of("java.util.AbstractMap$SimpleEntry.<init>(java.util.Map$Entry)")
            .toRawSignatureString());
  }
}
