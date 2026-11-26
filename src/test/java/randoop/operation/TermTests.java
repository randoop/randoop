package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import org.junit.Test;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
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

  @Test
  public void testPrimitiveValueHasDeclaredType() {
    NonreceiverTerm fromByte = new NonreceiverTerm(JavaTypes.SHORT_TYPE, (byte) 7);
    assertEquals(Short.valueOf((short) 7), fromByte.getValue());
    ExecutionOutcome outcome = fromByte.execute(new Object[0]);
    assertEquals(Short.valueOf((short) 7), ((NormalExecution) outcome).getRuntimeValue());

    NonreceiverTerm fromShort = new NonreceiverTerm(JavaTypes.SHORT_TYPE, (short) 7);
    assertEquals(fromShort, fromByte);
    assertEquals(fromShort.hashCode(), fromByte.hashCode());

    NonreceiverTerm fromChar = new NonreceiverTerm(JavaTypes.INT_TYPE, 'a');
    assertEquals(Integer.valueOf(97), fromChar.getValue());

    NonreceiverTerm fromInt = new NonreceiverTerm(JavaTypes.DOUBLE_TYPE, 3);
    assertEquals(Double.valueOf(3.0), fromInt.getValue());
  }
}
