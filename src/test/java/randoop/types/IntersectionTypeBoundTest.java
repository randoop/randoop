package randoop.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

/** Tests {@link IntersectionTypeBound}. */
public class IntersectionTypeBoundTest {

  /** An intersection is a subtype of a type if any of its members is. */
  @Test
  public void testIsLowerBound() {
    ReferenceType integerType = ReferenceType.forClass(Integer.class);
    ParameterBound integerAndComparable =
        new IntersectionTypeBound(
            Arrays.asList(
                ParameterBound.forType(integerType),
                ParameterBound.forType(
                    GenericClassType.forClass(Comparable.class).instantiate(integerType))));

    assertTrue(
        integerAndComparable.isLowerBound(
            ReferenceType.forClass(Number.class), new Substitution()));
    assertTrue(
        integerAndComparable.isLowerBound(
            GenericClassType.forClass(Comparable.class).instantiate(integerType),
            new Substitution()));
    assertFalse(integerAndComparable.isLowerBound(JavaTypes.STRING_TYPE, new Substitution()));
  }
}
