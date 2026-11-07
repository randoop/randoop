package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;

/** Tests for {@link TypeInstantiator}. */
public class TypeInstantiatorTest {

  /** A map whose values are of the map's own type. */
  private static class Node extends HashMap<String, Node> {
    /** ID for serializing this class. */
    private static final long serialVersionUID = 20261025;
  }

  /** Declares fields whose types, and their supertypes, are the input types for the tests. */
  @SuppressWarnings("UnusedVariable") // the fields' types are used, via reflection
  private static class InputTypes {
    /** A collection of comparables. */
    Collection<Comparable<String>> comparableCollection;

    /** A string. */
    String string;

    /** A list of strings. */
    List<String> strings;

    /** A list of integers. */
    List<Integer> integers;

    /** A list of objects. */
    List<Object> objects;

    /** A list of nodes. */
    List<Node> nodes;
  }

  /**
   * A method whose type parameter {@code K} occurs only in the bound of another type parameter.
   *
   * @param <K> the key type of {@code V}
   * @param <V> the element type
   * @param list a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <K, V extends Map<K, V>> void kOnlyInBound(List<V> list) {}

  /**
   * A method in which only {@code T} is related to the bound of a capture variable.
   *
   * @param <T> the element type of {@code c}
   * @param <A> an unrelated type
   * @param <B> an unrelated type
   * @param <C> an unrelated type
   * @param <D> an unrelated type
   * @param c a collection
   * @param a an A
   * @param b a B
   * @param cc a C
   * @param d a D
   * @return a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T, A, B, C, D> List<T> unrelatedVariables(
      Collection<? extends T> c, A a, B b, C cc, D d) {
    return null;
  }

  /**
   * A method in which the bound of the type of a parameter mentions the type variable of an earlier
   * parameter.
   *
   * @param <U> the element type of {@code list}
   * @param <T> the type of {@code t}
   * @param list a list
   * @param t a comparable
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <U, T extends Comparable<U>> void boundMentionsEarlierVariable(
      List<U> list, T t) {}

  /**
   * A method in which the bound of the return type mentions the type variable of a parameter.
   *
   * @param <U> the element type of {@code list}
   * @param <T> the return type
   * @param list a list
   * @return a comparable
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <U, T extends Comparable<U>> T returnBoundMentionsEarlierVariable(List<U> list) {
    return null;
  }

  /**
   * Returns the types of the fields of {@link InputTypes}, and all their supertypes.
   *
   * @return the types of the fields of {@link InputTypes}, and all their supertypes
   */
  private static Set<Type> inputTypes() {
    Set<Type> result = new LinkedHashSet<>();
    result.add(JavaTypes.OBJECT_TYPE);
    for (java.lang.reflect.Field f : InputTypes.class.getDeclaredFields()) {
      if (!f.isSynthetic()) {
        Type type = Type.forType(f.getGenericType());
        result.add(type);
        if (type instanceof ClassOrInterfaceType) {
          result.addAll(((ClassOrInterfaceType) type).getSupertypesInclusive());
        }
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

  /**
   * In {@code <T> void copy(List<? super T> dest, List<? extends T> src)}, {@code T} occurs only in
   * the bounds of the capture variables. The element type of {@code src} must be a subtype of the
   * element type of {@code dest}.
   */
  @Test
  public void testTypeParameterOnlyInCaptureBounds() throws Exception {
    Method m = Collections.class.getMethod("copy", List.class, List.class);
    TypedClassOperation operation = TypedOperation.forMethod(m);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes());
    int instantiations = 0;
    for (int i = 0; i < 100; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(operation);
      if (instantiated == null) {
        continue;
      }
      instantiations++;
      ReferenceType destElementType = typeArgument(instantiated.getInputTypes().get(0));
      ReferenceType srcElementType = typeArgument(instantiated.getInputTypes().get(1));
      assertTrue(
          "bad instantiation: " + instantiated,
          srcElementType.isSubtypeOfOrEqualTo(destElementType));
    }
    assertTrue("no instantiation found", instantiations > 0);
  }

  /**
   * In {@code <K, V extends Map<K, V>> void kOnlyInBound(List<V> list)}, the bound of {@code V}
   * mentions {@code K}, which occurs in no parameter type.
   */
  @Test
  public void testBoundMentionsUninstantiatedVariable() throws Exception {
    Method m = TypeInstantiatorTest.class.getDeclaredMethod("kOnlyInBound", List.class);
    TypedClassOperation operation = TypedOperation.forMethod(m);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes());
    ReferenceType nodeType = (ReferenceType) Type.forClass(Node.class);
    int instantiations = 0;
    for (int i = 0; i < 100; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(operation);
      if (instantiated == null) {
        continue;
      }
      instantiations++;
      assertEquals(nodeType, typeArgument(instantiated.getInputTypes().get(0)));
    }
    assertTrue("no instantiation found", instantiations > 0);
  }

  /**
   * In {@code <T, A, B, C, D> List<T> unrelatedVariables(Collection<? extends T> c, A a, B b, C cc,
   * D d)}, only {@code T} is related to the bound of the capture variable. The instantiator
   * searches the instantiations of {@code T}, and selects the instantiations of the other variables
   * independently, rather than enumerating every combination of instantiations of all the
   * variables.
   */
  @Test(timeout = 10000)
  public void testUnrelatedVariablesAreSelectedIndependently() throws Exception {
    Method m =
        TypeInstantiatorTest.class.getDeclaredMethod(
            "unrelatedVariables",
            Collection.class,
            Object.class,
            Object.class,
            Object.class,
            Object.class);
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

  /**
   * In {@code <U, T extends Comparable<U>> void boundMentionsEarlierVariable(List<U> list, T t)},
   * {@code U} is instantiated by the type of {@code list}, and the bound of {@code T} mentions
   * {@code U}.
   */
  @Test
  public void testBoundMentionsEarlierVariable() throws Exception {
    Method m =
        TypeInstantiatorTest.class.getDeclaredMethod(
            "boundMentionsEarlierVariable", List.class, Comparable.class);
    TypedClassOperation operation = TypedOperation.forMethod(m);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes());
    int instantiations = 0;
    for (int i = 0; i < 100; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(operation);
      if (instantiated == null) {
        continue;
      }
      instantiations++;
      ReferenceType elementType = typeArgument(instantiated.getInputTypes().get(0));
      ReferenceType comparableType = (ReferenceType) instantiated.getInputTypes().get(1);
      assertTrue(
          "bad instantiation: " + instantiated,
          comparableType.isSubtypeOfOrEqualTo(
              JavaTypes.COMPARABLE_TYPE.substitute(
                  new Substitution(JavaTypes.COMPARABLE_TYPE.getTypeParameters(), elementType))));
    }
    assertTrue("no instantiation found", instantiations > 0);
  }

  /**
   * In {@code <U, T extends Comparable<U>> T returnBoundMentionsEarlierVariable(List<U> list)},
   * {@code U} is instantiated by the type of {@code list}, and the bound of the return type {@code
   * T} mentions {@code U}.
   */
  @Test
  public void testReturnBoundMentionsEarlierVariable() throws Exception {
    Method m =
        TypeInstantiatorTest.class.getDeclaredMethod(
            "returnBoundMentionsEarlierVariable", List.class);
    TypedClassOperation operation = TypedOperation.forMethod(m);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes());
    int instantiations = 0;
    for (int i = 0; i < 100; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(operation);
      if (instantiated == null) {
        continue;
      }
      instantiations++;
      ReferenceType elementType = typeArgument(instantiated.getInputTypes().get(0));
      ReferenceType comparableType = (ReferenceType) instantiated.getOutputType();
      assertTrue(
          "bad instantiation: " + instantiated,
          comparableType.isSubtypeOfOrEqualTo(
              JavaTypes.COMPARABLE_TYPE.substitute(
                  new Substitution(JavaTypes.COMPARABLE_TYPE.getTypeParameters(), elementType))));
    }
    assertTrue("no instantiation found", instantiations > 0);
  }
}
