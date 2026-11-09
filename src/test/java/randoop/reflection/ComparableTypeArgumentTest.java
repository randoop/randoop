package randoop.reflection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Type;

/** Tests that {@link TypeInstantiator} favors comparable type arguments. */
public class ComparableTypeArgumentTest {

  /** How many times to instantiate an operation, since instantiation is random. */
  private static final int TRIALS = 200;

  @Test
  public void testComparableIsFavoredForClass() throws NoSuchMethodException {
    assertComparableIsFavored(TypedOperation.forConstructor(ArrayList.class.getConstructor()));
  }

  @Test
  public void testComparableIsFavoredForMethod()
      throws NoSuchMethodException, RandoopSpecificationError {
    assertComparableIsFavored(TypedOperation.forMethod(Collections.class.getMethod("emptyList")));
  }

  /**
   * Asserts that instantiating the given operation, whose output type has one type argument,
   * chooses a comparable type argument more often than choosing uniformly among the input types
   * would.
   *
   * @param operation an operation whose output type is a generic class with one type parameter
   */
  private void assertComparableIsFavored(TypedClassOperation operation) {
    // Only one of these input types, String, is comparable.
    Set<Type> inputTypes = new LinkedHashSet<>();
    for (Class<?> c : new Class<?>[] {String.class, Thread.class, BitSet.class}) {
      inputTypes.addAll(ClassOrInterfaceType.forClass(c).getSupertypesInclusive());
    }
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);

    int comparableCount = 0;
    for (int i = 0; i < TRIALS; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(operation);
      assertNotNull("instantiation of " + operation, instantiated);
      InstantiatedType outputType = (InstantiatedType) instantiated.getOutputType();
      ReferenceType typeArgument =
          ((ReferenceArgument) outputType.getTypeArguments().get(0)).getReferenceType();
      if (TypeInstantiator.isComparable(typeArgument)) {
        comparableCount++;
      }
    }
    // Choosing uniformly would yield about 10% comparable type arguments.
    assertTrue(
        "Comparable type arguments: " + comparableCount + " of " + TRIALS,
        comparableCount > TRIALS * 0.4);
  }

  @Test
  public void testIsComparable() {
    assertTrue(TypeInstantiator.isComparable(JavaTypes.STRING_TYPE));
    assertTrue(TypeInstantiator.isComparable(ClassOrInterfaceType.forClass(Integer.class)));
    assertTrue(TypeInstantiator.isComparable(ClassOrInterfaceType.forClass(TimeUnit.class)));
    assertFalse(TypeInstantiator.isComparable(JavaTypes.OBJECT_TYPE));
    assertFalse(TypeInstantiator.isComparable(ClassOrInterfaceType.forClass(Thread.class)));
    // A Comparable<String> can be compared to a String, but not to another Comparable<String>.
    assertFalse(
        TypeInstantiator.isComparable(
            JavaTypes.COMPARABLE_TYPE.instantiate(JavaTypes.STRING_TYPE)));
  }
}
