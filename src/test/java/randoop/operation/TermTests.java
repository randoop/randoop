package randoop.operation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import org.junit.Test;
import randoop.types.JavaTypes;
import randoop.types.Type;

/** Test for Term operations. */
public class TermTests {

  @Test
  public void testClassLiterals() {
    NonreceiverTerm classTerm = new NonreceiverTerm(JavaTypes.CLASS_TYPE, Comparator.class);
    assertEquals(
        "toString should be qualified literal name",
        "java.util.Comparator.class",
        classTerm.toString());

    StringBuilder b = new StringBuilder();
    classTerm.appendCode(null, null, null, null, b);
    assertThat("append code returns class literal", "java.util.Comparator.class", is(b.toString()));

    assertThat(
        "getValue returns Class object", classTerm.getValue(), is((Object) Comparator.class));
    assertThat("getType returns Class type", classTerm.getType(), is((Type) JavaTypes.CLASS_TYPE));
    assertTrue("Class<T> is a nonreceiver type", NonreceiverTerm.isNonreceiverType(Class.class));

    NonreceiverTerm term = NonreceiverTerm.createNullOrZeroTerm(JavaTypes.CLASS_TYPE);
    NonreceiverTerm nullClassTerm = new NonreceiverTerm(JavaTypes.CLASS_TYPE, null);
    assertThat("null terms are equal ", term, is(nullClassTerm));
  }
}
