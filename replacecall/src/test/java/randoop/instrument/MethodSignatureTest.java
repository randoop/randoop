package randoop.instrument;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.junit.Test;

/** Tests for {@link MethodSignature#of(String)}. */
public class MethodSignatureTest {

  @Test
  public void wellFormedSignatures() {
    MethodSignature sig = MethodSignature.of("java.util.Random.nextInt(int)");
    assertEquals("java.util.Random", sig.getClassname());
    assertEquals("nextInt", sig.getName());
    assertArrayEquals(new Type[] {Type.INT}, sig.getParameterTypes());

    sig = MethodSignature.of("a.B.c()");
    assertEquals("a.B", sig.getClassname());
    assertEquals("c", sig.getName());
    assertArrayEquals(new Type[0], sig.getParameterTypes());

    sig = MethodSignature.of("a.B.c( )");
    assertArrayEquals(new Type[0], sig.getParameterTypes());

    sig = MethodSignature.of("a.B.<init>(int , java.lang.String)");
    assertEquals("a.B", sig.getClassname());
    assertEquals("<init>", sig.getName());
    assertArrayEquals(
        new Type[] {Type.INT, new ObjectType("java.lang.String")}, sig.getParameterTypes());
  }

  @Test
  public void malformedSignatures() {
    String[] malformed = {
      "a.B.c",
      "a.B.c(int",
      "a.B.c)",
      "a.B.c(int))",
      "a.B.c(int) junk",
      "a.B.c d()",
      "a.B.1c()",
      "a.B.<clinit>()",
      "c()",
      ".c()",
      "(int)",
      "a.B.c(,int)",
      "a.B.c(int,)",
      "a.B.c(int, , )",
      "a.B.c(int int)",
    };
    for (String signature : malformed) {
      assertThrows(signature, IllegalArgumentException.class, () -> MethodSignature.of(signature));
    }
  }
}
