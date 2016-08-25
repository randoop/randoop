package randoop.operation;

import org.junit.Test;

import java.util.Comparator;

import randoop.types.JavaTypes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for Term operations.
 */
public class TermTests {

  @Test
  public void testClassLiterals() {
    NonreceiverTerm classTerm = new NonreceiverTerm(JavaTypes.CLASS_TYPE, Comparator.class);
    assertThat(
        "toString should be qualified literal name",
        classTerm.toString(),
        is(equalTo("java.util.Comparator.class")));
  }
}
