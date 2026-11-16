package randoop.types;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ParameterBoundTest {

  /** A field whose type is a generic array type that contains no type variable. */
  @SuppressWarnings("unused")
  private static List<String>[] concreteGenericArray;

  @Test
  public void testConcreteGenericArrayBoundWithEmptySubstitution() throws NoSuchFieldException {
    java.lang.reflect.Type arrayType =
        ParameterBoundTest.class.getDeclaredField("concreteGenericArray").getGenericType();
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), arrayType);
    Type argType = Type.forType(arrayType);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
  }
}
