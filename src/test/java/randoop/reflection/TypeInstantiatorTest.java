package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;

/** Tests {@link TypeInstantiator}. */
public class TypeInstantiatorTest {

  /** The number of times that each test instantiates its operation. */
  private static final int ATTEMPTS = 50;

  /** The {@code Integer} type. */
  private static final ClassOrInterfaceType INTEGER_TYPE =
      ClassOrInterfaceType.forClass(Integer.class);

  /**
   * Tests that the types chosen for two wildcards whose bounds contain the same type variable are
   * consistent. {@code <T> void copy(List<? super T> dest, List<? extends T> src)} must not be
   * instantiated as {@code copy(List<Integer>, List<String>)}.
   */
  @Test
  public void testWildcardBoundTypeVariable()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    for (ClassOrInterfaceType elementType :
        new ClassOrInterfaceType[] {JavaTypes.STRING_TYPE, INTEGER_TYPE}) {
      addTypes(elementType, inputTypes);
      addTypes(GenericClassType.forClass(ArrayList.class).instantiate(elementType), inputTypes);
    }
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation copy =
        TypedOperation.forMethod(Collections.class.getMethod("copy", List.class, List.class));

    int instantiated = 0;
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(copy);
      if (operation == null) {
        continue;
      }
      instantiated++;
      ReferenceType destElementType = typeArgument(operation.getInputTypes().get(0), 0);
      ReferenceType srcElementType = typeArgument(operation.getInputTypes().get(1), 0);
      assertTrue(
          "ill-typed instantiation " + operation,
          srcElementType.isSubtypeOfOrEqualTo(destElementType));
    }
    assertTrue("no instantiation of " + copy, instantiated > 0);
  }

  /**
   * Tests that the types chosen for two wildcards whose bounds contain the same type variable are
   * consistent, when the operation's declaring type is generic. {@code <U> Comparator<T>
   * thenComparing(Function<? super T, ? extends U>, Comparator<? super U>)} must not be
   * instantiated with a {@code Function<String, Integer>} and a {@code Comparator<String>}.
   */
  @Test
  public void testWildcardBoundTypeVariableOfGenericClass()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    GenericClassType comparatorType = GenericClassType.forClass(Comparator.class);
    GenericClassType functionType = GenericClassType.forClass(Function.class);
    for (ClassOrInterfaceType elementType :
        new ClassOrInterfaceType[] {JavaTypes.STRING_TYPE, INTEGER_TYPE}) {
      addTypes(elementType, inputTypes);
      addTypes(comparatorType.instantiate(elementType), inputTypes);
      addTypes(functionType.instantiate(elementType, elementType), inputTypes);
    }
    addTypes(functionType.instantiate(JavaTypes.STRING_TYPE, INTEGER_TYPE), inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation thenComparing =
        TypedOperation.forMethod(
            Comparator.class.getMethod("thenComparing", Function.class, Comparator.class));

    int instantiated = 0;
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(thenComparing);
      if (operation == null) {
        continue;
      }
      instantiated++;
      // The input types are the receiver, the key extractor, and the key comparator.
      ReferenceType receiverElementType = typeArgument(operation.getInputTypes().get(0), 0);
      ReferenceType extractorInputType = typeArgument(operation.getInputTypes().get(1), 0);
      ReferenceType keyType = typeArgument(operation.getInputTypes().get(1), 1);
      ReferenceType comparedType = typeArgument(operation.getInputTypes().get(2), 0);
      assertTrue(
          "ill-typed instantiation " + operation,
          receiverElementType.isSubtypeOfOrEqualTo(extractorInputType));
      assertTrue(
          "ill-typed instantiation " + operation, keyType.isSubtypeOfOrEqualTo(comparedType));
    }
    assertTrue("no instantiation of " + thenComparing, instantiated > 0);
  }

  /** A map whose values are of the map's own type. */
  private static class Node extends HashMap<String, Node> {
    /** ID for serializing this class. */
    private static final long serialVersionUID = 20261025;
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
   * Tests a type variable that occurs only in the bound of another type variable. In {@code <K, V
   * extends Map<K, V>> void kOnlyInBound(List<V> list)}, the bound of {@code V} mentions {@code K},
   * which occurs in no parameter type, so {@code K} is not instantiated when an input type is
   * matched to {@code List<V>}. Among the input types, only {@code List<Node>} has an element type
   * that is a {@code Map<K, V>} for some {@code K}.
   */
  @Test
  public void testBoundMentionsUninstantiatedVariable()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    ClassOrInterfaceType nodeType = ClassOrInterfaceType.forClass(Node.class);
    GenericClassType listType = GenericClassType.forClass(List.class);
    addTypes(
        GenericClassType.forClass(Collection.class)
            .instantiate(
                GenericClassType.forClass(Comparable.class).instantiate(JavaTypes.STRING_TYPE)),
        inputTypes);
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    for (ClassOrInterfaceType elementType :
        new ClassOrInterfaceType[] {
          JavaTypes.STRING_TYPE, INTEGER_TYPE, JavaTypes.OBJECT_TYPE, nodeType
        }) {
      addTypes(listType.instantiate(elementType), inputTypes);
    }
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation kOnlyInBound =
        TypedOperation.forMethod(
            TypeInstantiatorTest.class.getDeclaredMethod("kOnlyInBound", List.class));

    int instantiated = 0;
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(kOnlyInBound);
      if (operation == null) {
        continue;
      }
      instantiated++;
      assertEquals(nodeType, typeArgument(operation.getInputTypes().get(0), 0));
    }
    assertTrue("no instantiation of " + kOnlyInBound, instantiated > 0);
  }

  /**
   * Tests that each operation is instantiated when the input types allow only one choice. The tests
   * with several choices succeed if any one choice is instantiated, so they would not detect that
   * a valid choice is always rejected.
   */
  @Test
  public void testSingleValidChoice() throws NoSuchMethodException, RandoopSpecificationError {
    TypedClassOperation copy =
        TypedOperation.forMethod(Collections.class.getMethod("copy", List.class, List.class));
    for (ClassOrInterfaceType elementType :
        new ClassOrInterfaceType[] {JavaTypes.STRING_TYPE, INTEGER_TYPE}) {
      Set<Type> inputTypes = new LinkedHashSet<>();
      addTypes(elementType, inputTypes);
      addTypes(GenericClassType.forClass(ArrayList.class).instantiate(elementType), inputTypes);
      assertSomeInstantiation(new TypeInstantiator(inputTypes), copy);
    }

    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(
        GenericClassType.forClass(List.class).instantiate(ClassOrInterfaceType.forClass(Node.class)),
        inputTypes);
    assertSomeInstantiation(
        new TypeInstantiator(inputTypes),
        TypedOperation.forMethod(
            TypeInstantiatorTest.class.getDeclaredMethod("kOnlyInBound", List.class)));
  }

  /**
   * Asserts that the given instantiator instantiates the given operation in at least one of {@link
   * #ATTEMPTS} attempts.
   *
   * @param instantiator the type instantiator
   * @param operation the operation to instantiate
   */
  private static void assertSomeInstantiation(
      TypeInstantiator instantiator, TypedClassOperation operation) {
    for (int i = 0; i < ATTEMPTS; i++) {
      if (instantiator.instantiate(operation) != null) {
        return;
      }
    }
    throw new AssertionError("no instantiation of " + operation);
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
      ReferenceType argElementType = typeArgument(instantiated.getInputTypes().get(0), 0);
      ReferenceType resultElementType = typeArgument(instantiated.getOutputType(), 0);
      assertTrue(
          "bad instantiation: " + instantiated,
          argElementType.isSubtypeOfOrEqualTo(resultElementType));
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
      ReferenceType argElementType = typeArgument(instantiated.getInputTypes().get(0), 0);
      ReferenceType resultElementType = typeArgument(instantiated.getOutputType(), 0);
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
      ReferenceType elementType = typeArgument(instantiated.getInputTypes().get(0), 0);
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
      ReferenceType elementType = typeArgument(instantiated.getInputTypes().get(0), 0);
      ReferenceType comparableType = (ReferenceType) instantiated.getOutputType();
      assertTrue(
          "bad instantiation: " + instantiated,
          comparableType.isSubtypeOfOrEqualTo(
              JavaTypes.COMPARABLE_TYPE.substitute(
                  new Substitution(JavaTypes.COMPARABLE_TYPE.getTypeParameters(), elementType))));
    }
    assertTrue("no instantiation found", instantiations > 0);
  }

  /**
   * A method whose parameter type has a wildcard whose lower bound is a bounded type parameter.
   *
   * @param <T> the element type
   * @param list a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Number> void consume(List<? super T> list) {}

  /**
   * Tests a wildcard whose lower bound is a type variable that is not yet instantiated when the
   * parameter type is matched. {@code <T extends Number> void consume(List<? super T>)} can be
   * instantiated with a {@code List<Object>}, by choosing {@code Number} for {@code T}, though
   * {@code Object} cannot instantiate {@code T}.
   */
  @Test
  public void testWildcardLowerBoundUninstantiatedVariable()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(INTEGER_TYPE, inputTypes);
    addTypes(GenericClassType.forClass(List.class).instantiate(JavaTypes.OBJECT_TYPE), inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation consume =
        TypedOperation.forMethod(
            TypeInstantiatorTest.class.getDeclaredMethod("consume", List.class));

    int instantiated = 0;
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(consume);
      if (operation == null) {
        continue;
      }
      instantiated++;
      assertEquals(JavaTypes.OBJECT_TYPE, typeArgument(operation.getInputTypes().get(0), 0));
    }
    assertTrue("no instantiation of " + consume, instantiated > 0);
  }

  /** An enum that is not an input type in {@link #testBoundVariableFromTypeArgument}. */
  private enum Color {
    /** The only constant. */
    RED
  }

  /**
   * A method whose type parameter {@code E} occurs only in the bounds of other type parameters.
   *
   * @param <E> an enum type
   * @param <T> the element type
   * @param list a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <E extends Enum<E>, T extends E> void consumeEnum(List<? super T> list) {}

  /**
   * Tests a type variable that occurs only in bounds, whose only possible type is a type argument
   * of an input type rather than an input type itself. {@code <E extends Enum<E>, T extends E> void
   * consumeEnum(List<? super T>)} can be instantiated with a {@code List<Color>}, by choosing
   * {@code Color} for {@code E} and {@code T}, though {@code Color} is not an input type.
   */
  @Test
  public void testBoundVariableFromTypeArgument()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    ClassOrInterfaceType colorType = ClassOrInterfaceType.forClass(Color.class);
    addTypes(GenericClassType.forClass(List.class).instantiate(colorType), inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation consumeEnum =
        TypedOperation.forMethod(
            TypeInstantiatorTest.class.getDeclaredMethod("consumeEnum", List.class));

    int instantiated = 0;
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(consumeEnum);
      if (operation == null) {
        continue;
      }
      instantiated++;
      assertEquals(colorType, typeArgument(operation.getInputTypes().get(0), 0));
    }
    assertTrue("no instantiation of " + consumeEnum, instantiated > 0);
  }

  /**
   * A method whose type parameter {@code T} occurs only, and twice, in the bound of another type
   * parameter.
   *
   * @param <S> the element type
   * @param <T> the key and value type of {@code S}
   * @param list a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <S extends Map<T, T>, T> void repeatedInBound(List<S> list) {}

  /**
   * Tests that the occurrences of a type variable that occurs only in the bound of another type
   * variable are instantiated consistently. In {@code <S extends Map<T, T>, T> void
   * repeatedInBound(List<S> list)}, {@code S} must not be instantiated as {@code HashMap<String,
   * Integer>}, for which no single type for {@code T} satisfies the bound.
   */
  @Test
  public void testBoundRepeatsUninstantiatedVariable()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    GenericClassType listType = GenericClassType.forClass(List.class);
    GenericClassType hashMapType = GenericClassType.forClass(HashMap.class);
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(INTEGER_TYPE, inputTypes);
    InstantiatedType consistentMap =
        hashMapType.instantiate(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE);
    InstantiatedType inconsistentMap = hashMapType.instantiate(JavaTypes.STRING_TYPE, INTEGER_TYPE);
    addTypes(listType.instantiate(inconsistentMap), inputTypes);
    addTypes(listType.instantiate(consistentMap), inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation repeatedInBound =
        TypedOperation.forMethod(
            TypeInstantiatorTest.class.getDeclaredMethod("repeatedInBound", List.class));

    int instantiated = 0;
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(repeatedInBound);
      if (operation == null) {
        continue;
      }
      instantiated++;
      assertEquals(consistentMap, typeArgument(operation.getInputTypes().get(0), 0));
    }
    assertTrue("no instantiation of " + repeatedInBound, instantiated > 0);
  }

  /**
   * Returns the given type argument of the given parameterized type.
   *
   * @param type a parameterized type, such as {@code List<String>}
   * @param index the index of a type argument of {@code type}
   * @return the type argument of {@code type} at {@code index}
   */
  private static ReferenceType typeArgument(Type type, int index) {
    return ((ReferenceArgument) ((InstantiatedType) type).getTypeArguments().get(index))
        .getReferenceType();
  }

  /**
   * Adds the given type and its supertypes to the given set.
   *
   * @param type a type
   * @param inputTypes the set to which to add {@code type} and its supertypes
   */
  private static void addTypes(ClassOrInterfaceType type, Set<Type> inputTypes) {
    inputTypes.addAll(type.getSupertypesInclusive());
  }
}
