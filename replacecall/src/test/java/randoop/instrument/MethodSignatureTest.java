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

    sig = MethodSignature.of("a.B.c( int , int )");
    assertArrayEquals(new Type[] {Type.INT, Type.INT}, sig.getParameterTypes());

    sig = MethodSignature.of("a.B.<init>(int , java.lang.String)");
    assertEquals("a.B", sig.getClassname());
    assertEquals("<init>", sig.getName());
    assertArrayEquals(
        new Type[] {Type.INT, new ObjectType("java.lang.String")}, sig.getParameterTypes());
  }

  @Test
  public void malformedSignatures() {
    String noOpenParen = "Method signature expected, did not find beginning parenthesis";
    String noCloseParen = "Method signature expected, does not end with closing parenthesis";
    String badName = "Method signature expected, bad fully-qualified method name ";
    String emptyParam = "Empty parameter type";
    String badParam = "Bad parameter type ";
    // Each element is a malformed signature and the error message for it.
    String[][] malformed = {
      {"a.B.c", noOpenParen},
      {"a.B.c(int", noCloseParen},
      {"a.B.c)", noOpenParen},
      {"a.B.c(int))", badParam + "\"int)\""},
      {"a.B.c(int) junk", noCloseParen},
      {"a.B.c d()", badName + "\"a.B.c d\""},
      {"a.B.c (int)", badName + "\"a.B.c \""},
      {"a.B.1c()", badName + "\"a.B.1c\""},
      {"a.B.<clinit>()", badName + "\"a.B.<clinit>\""},
      {"c()", badName + "\"c\""},
      {".c()", badName + "\".c\""},
      {"(int)", badName + "\"\""},
      {"a.B.c(,int)", emptyParam},
      {"a.B.c(int,)", emptyParam},
      {"a.B.c(int, , )", emptyParam},
      {"a.B.c(int int)", badParam + "\"int int\""},
    };
    for (String[] signatureAndMessage : malformed) {
      String signature = signatureAndMessage[0];
      IllegalArgumentException e =
          assertThrows(
              signature, IllegalArgumentException.class, () -> MethodSignature.of(signature));
      assertEquals(signature, signatureAndMessage[1], e.getMessage());
    }
  }
}
