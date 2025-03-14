package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import org.junit.Test;
import randoop.types.JavaTypes;

/** Test for Term operations. */
public class TermTests {

  @Test
  public void testClassLiterals() {
    NonreceiverTerm classTerm = new NonreceiverTerm(JavaTypes.CLASS_TYPE, Comparator.class);
    assertEquals("java.util.Comparator.class", classTerm.toString());

    StringBuilder b = new StringBuilder();
    classTerm.appendCode(null, null, null, null, b);
    assertEquals("java.util.Comparator.class", b.toString());

    assertEquals(Comparator.class, classTerm.getValue());
    assertEquals(JavaTypes.CLASS_TYPE, classTerm.getType());
    assertTrue(NonreceiverTerm.isNonreceiverType(Class.class));

    NonreceiverTerm term = NonreceiverTerm.createNullOrZeroTerm(JavaTypes.CLASS_TYPE);
    NonreceiverTerm nullClassTerm = new NonreceiverTerm(JavaTypes.CLASS_TYPE, null);
    assertEquals(nullClassTerm, term);
  }
}
