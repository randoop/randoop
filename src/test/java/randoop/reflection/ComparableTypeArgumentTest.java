package randoop.reflection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.ReferenceArgument;
import randoop.types.Type;

/** Tests that {@link TypeInstantiator} favors type arguments that implement Comparable. */
public class ComparableTypeArgumentTest {

  /** How many times to instantiate the constructor, since instantiation is random. */
  private static final int TRIALS = 200;

  @Test
  public void testComparableIsFavored() throws NoSuchMethodException {
    // Few of these input types implement Comparable:  String and Comparable<String>.
    Set<Type> inputTypes = new LinkedHashSet<>();
    for (Class<?> c : new Class<?>[] {String.class, Thread.class, BitSet.class}) {
      inputTypes.addAll(ClassOrInterfaceType.forClass(c).getSupertypesInclusive());
    }
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation constructor =
        TypedOperation.forConstructor(ArrayList.class.getConstructor());

    int comparableCount = 0;
    for (int i = 0; i < TRIALS; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(constructor);
      assertNotNull("instantiation of " + constructor, instantiated);
      InstantiatedType outputType = (InstantiatedType) instantiated.getOutputType();
      Type typeArgument =
          ((ReferenceArgument) outputType.getTypeArguments().get(0)).getReferenceType();
      if (Comparable.class.isAssignableFrom(typeArgument.getRuntimeClass())) {
        comparableCount++;
      }
    }
    // Choosing uniformly would yield about 20% Comparable type arguments.
    assertTrue(
        "Comparable type arguments: " + comparableCount + " of " + TRIALS,
        comparableCount > TRIALS * 0.4);
  }
}
