package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.Test;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.ReferenceArgument;
import randoop.types.Type;

/**
 * Tests that {@link TypeInstantiator} instantiates the ordered type parameter of a class such as
 * {@code TreeSet} with a type that has an ordering.
 */
public class OrderedTypeInstantiationTest {

  /** A comparator interface that is not {@code java.util.Comparator}. */
  public interface MyComparator<T> {
    int compare(T o1, T o2);
  }

  /** A comparator for strings. */
  public static class MyStringComparator implements MyComparator<String> {
    @Override
    public int compare(String o1, String o2) {
      return o1.compareTo(o2);
    }
  }

  /** A class whose first type parameter is unordered and whose second is ordered. */
  public static class MyOrderedMap<V, K> {
    public MyOrderedMap() {}

    public MyOrderedMap(MyComparator<? super K> comparator) {}
  }

  /** How many times to instantiate each constructor, since instantiation is random. */
  private static final int TRIALS = 50;

  /**
   * Returns input types that contain some types that implement Comparable and some that do not.
   *
   * @return a set of input types, closed under supertypes
   */
  private static Set<Type> inputTypes() {
    Set<Type> result = new LinkedHashSet<>();
    for (Class<?> c :
        new Class<?>[] {String.class, Thread.class, BitSet.class, MyStringComparator.class}) {
      result.addAll(ClassOrInterfaceType.forClass(c).getSupertypesInclusive());
    }
    return result;
  }

  /**
   * Instantiates the given constructor many times, and checks the type argument at the given index
   * each time.
   *
   * @param constructor a constructor of a generic class
   * @param index the index of the ordered type parameter
   * @param expected the only type that may instantiate the ordered type parameter
   */
  private static void checkOrderedTypeArgument(
      TypedClassOperation constructor, int index, Type expected) {
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes());
    for (int i = 0; i < TRIALS; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(constructor);
      assertNotNull("instantiation of " + constructor, instantiated);
      InstantiatedType outputType = (InstantiatedType) instantiated.getOutputType();
      ReferenceArgument typeArgument = (ReferenceArgument) outputType.getTypeArguments().get(index);
      assertEquals(outputType.toString(), expected, typeArgument.getReferenceType());
    }
  }

  @Test
  public void testTreeSet() throws NoSuchMethodException {
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(TreeSet.class.getConstructor()), 0, JavaTypes.STRING_TYPE);
  }

  @Test
  public void testTreeMap() throws NoSuchMethodException {
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(TreeMap.class.getConstructor()), 0, JavaTypes.STRING_TYPE);
  }

  @Test
  public void testUserDefinedComparator() throws NoSuchMethodException {
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(MyOrderedMap.class.getConstructor()),
        1,
        JavaTypes.STRING_TYPE);
    checkOrderedTypeArgument(
        TypedOperation.forConstructor(MyOrderedMap.class.getConstructor(MyComparator.class)),
        1,
        JavaTypes.STRING_TYPE);
  }
}
