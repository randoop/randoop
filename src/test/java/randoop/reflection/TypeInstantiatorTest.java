package randoop.reflection;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.InstantiatedType;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Type;

/** Tests for {@link TypeInstantiator}. */
public class TypeInstantiatorTest {

  /** Declares fields whose types are the input types for the tests. */
  @SuppressWarnings("UnusedVariable") // the fields' types are used, via reflection
  private static class InputTypes {
    /** A collection of comparables. */
    Collection<Comparable<String>> comparableCollection;

    /** An iterable of comparables. */
    Iterable<Comparable<String>> comparableIterable;

    /** A comparable. */
    Comparable<String> comparable;

    /** A string. */
    String string;
  }

  /**
   * Returns the types of the fields of {@link InputTypes}.
   *
   * @return the types of the fields of {@link InputTypes}
   */
  private static Set<Type> inputTypes() {
    Set<Type> result = new LinkedHashSet<>();
    for (java.lang.reflect.Field f : InputTypes.class.getDeclaredFields()) {
      if (!f.isSynthetic()) {
        result.add(Type.forType(f.getGenericType()));
      }
    }
    return result;
  }

  /**
   * Returns the type argument of the given parameterized type, which has exactly one.
   *
   * @param type a parameterized type with one type argument
   * @return the type argument of {@code type}
   */
  private static ReferenceType typeArgument(Type type) {
    return ((ReferenceArgument) ((InstantiatedType) type).getTypeArguments().get(0))
        .getReferenceType();
  }

  /**
   * In {@code <T> Collection<T> unmodifiableCollection(Collection<? extends T> c)}, the element
   * type of the argument must be a subtype of {@code T}.
   */
  @Test
  public void testCaptureBoundRefersToTypeParameter() throws Exception {
    Method m = Collections.class.getMethod("unmodifiableCollection", Collection.class);
    TypedClassOperation operation = TypedOperation.forMethod(m);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes());
    int instantiations = 0;
    for (int i = 0; i < 100; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(operation);
      if (instantiated == null) {
        continue;
      }
      instantiations++;
      ReferenceType argElementType = typeArgument(instantiated.getInputTypes().get(0));
      ReferenceType resultElementType = typeArgument(instantiated.getOutputType());
      assertTrue(
          "bad instantiation: " + instantiated,
          argElementType.isSubtypeOfOrEqualTo(resultElementType));
    }
    assertTrue("no instantiation found", instantiations > 0);
  }
}
