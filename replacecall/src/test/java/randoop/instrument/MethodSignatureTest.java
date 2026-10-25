package randoop.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

/** Tests for {@link MethodSignature}. */
public class MethodSignatureTest {

  @Test
  public void ofWhitespaceParametersTest() {
    assertEquals(MethodSignature.of("a.B.m()"), MethodSignature.of("a.B.m( )"));
    assertEquals(MethodSignature.of("a.B.m(int,int)"), MethodSignature.of("a.B.m( int , int )"));
  }

  @Test
  public void ofEmptyParameterTest() {
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> MethodSignature.of("a.B.m(int,)"));
    assertEquals("Empty parameter type", e.getMessage());
    e = assertThrows(IllegalArgumentException.class, () -> MethodSignature.of("a.B.m(,int)"));
    assertEquals("Empty parameter type", e.getMessage());
  }
}
