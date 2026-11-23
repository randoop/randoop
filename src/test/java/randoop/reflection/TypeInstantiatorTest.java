package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import randoop.types.TypeVariable;

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

    // Without String among the input types, K can still be String, which is a type argument of
    // HashMap<String, Node>, a supertype of Node.
    Set<Type> inputTypesWithoutKey = new LinkedHashSet<>();
    addTypes(listType.instantiate(nodeType), inputTypesWithoutKey);
    TypeInstantiator instantiatorWithoutKey = new TypeInstantiator(inputTypesWithoutKey);
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiatorWithoutKey.instantiate(kOnlyInBound);
      assertNotNull("no instantiation of " + kOnlyInBound, operation);
      assertEquals(nodeType, typeArgument(operation.getInputTypes().get(0), 0));
    }

    // String is not a Map<K, String> for any K, so List<String> cannot instantiate List<V>.
    Set<Type> incompatibleInputTypes = new LinkedHashSet<>();
    addTypes(listType.instantiate(JavaTypes.STRING_TYPE), incompatibleInputTypes);
    TypeInstantiator incompatibleInstantiator = new TypeInstantiator(incompatibleInputTypes);
    for (int i = 0; i < ATTEMPTS; i++) {
      assertNull("ill-typed instantiation", incompatibleInstantiator.instantiate(kOnlyInBound));
    }
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
   * Tests that each operation is instantiated when the input types allow only one choice. The tests
   * with several choices succeed if any one choice is instantiated, so they would not detect that a
   * valid choice is always rejected.
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
        GenericClassType.forClass(List.class)
            .instantiate(ClassOrInterfaceType.forClass(Node.class)),
        inputTypes);
    // The bound `V extends Map<K, V>` requires that K be String, since Node extends
    // HashMap<String, Node>.  The candidate types for K include the input types, such as String.
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
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
   * A class in which only {@code T} and {@code U} are related by a bound.
   *
   * @param <T> a type parameter whose bound mentions {@code U}
   * @param <U> a type parameter that occurs in the bound of {@code T}
   * @param <A> an unrelated type
   * @param <B> an unrelated type
   * @param <C> an unrelated type
   * @param <D> an unrelated type
   */
  @SuppressWarnings("UnusedTypeParameter") // the type parameters are used via reflection
  static class UnrelatedClassVariables<T extends Comparable<U>, U, A, B, C, D> {
    /** Creates an object. */
    public UnrelatedClassVariables() {}
  }

  /**
   * Returns {@code Comparable<type>}.
   *
   * @param type a type
   * @return {@code Comparable<type>}
   */
  private static ReferenceType comparableOf(ReferenceType type) {
    return JavaTypes.COMPARABLE_TYPE.substitute(
        new Substitution(JavaTypes.COMPARABLE_TYPE.getTypeParameters(), type));
  }

  /**
   * In {@code UnrelatedClassVariables<T extends Comparable<U>, U, A, B, C, D>}, only {@code T} and
   * {@code U} are related by a bound. When the instantiator creates a new instantiation of the
   * class, it enumerates the instantiations of {@code T} and {@code U} together, and selects the
   * instantiations of the other variables once, independently, rather than enumerating every
   * combination of instantiations of all the variables.
   */
  @Test(timeout = 10000)
  public void testUnrelatedVariablesAreSelectedIndependently() throws Exception {
    List<TypeVariable> parameters =
        GenericClassType.forClass(UnrelatedClassVariables.class).getTypeParameters();
    TypeVariable t = parameters.get(0);
    TypeVariable u = parameters.get(1);
    List<TypeVariable> independent = parameters.subList(2, parameters.size());
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes());

    // Only T and U enter the joint enumeration:  every substitution has the same types for the
    // independent variables, and a distinct pair of types for T and U.
    List<Substitution> substitutions =
        instantiator.allExtendingSubstitutions(parameters, new Substitution());
    assertFalse("no substitution found", substitutions.isEmpty());
    Substitution first = substitutions.get(0);
    Set<List<ReferenceType>> relatedTypes = new HashSet<>();
    for (Substitution substitution : substitutions) {
      for (TypeVariable variable : independent) {
        assertNotNull(substitution.get(variable));
        assertEquals(first.get(variable), substitution.get(variable));
      }
      ReferenceType tType = substitution.get(t);
      ReferenceType uType = substitution.get(u);
      assertNotNull(tType);
      assertNotNull(uType);
      assertTrue(
          "bad substitution: " + substitution, tType.isSubtypeOfOrEqualTo(comparableOf(uType)));
      assertTrue(
          "duplicate types for T and U: " + substitution,
          relatedTypes.add(Arrays.asList(tType, uType)));
    }

    TypedClassOperation constructor =
        TypedOperation.forConstructor(UnrelatedClassVariables.class.getDeclaredConstructor());
    int instantiations = 0;
    for (int i = 0; i < 100; i++) {
      TypedClassOperation instantiated = instantiator.instantiate(constructor);
      if (instantiated == null) {
        continue;
      }
      instantiations++;
      ReferenceType tType = typeArgument(instantiated.getOutputType(), 0);
      ReferenceType uType = typeArgument(instantiated.getOutputType(), 1);
      assertTrue(
          "bad instantiation: " + instantiated, tType.isSubtypeOfOrEqualTo(comparableOf(uType)));
    }
    assertTrue("no instantiation found", instantiations > 0);
  }

  /**
   * In {@code <T, A, B, C, D> List<T> unrelatedVariables(Collection<? extends T> c, A a, B b, C cc,
   * D d)}, only {@code T} is related to the bound of the capture variable. The search for an
   * instantiation of the method's type variables abandons each partial substitution that violates a
   * bound, so it terminates quickly although there are many combinations of instantiations of all
   * the variables.
   */
  @Test(timeout = 10000)
  public void testUnrelatedMethodVariables() throws Exception {
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
   * A method whose second parameter type has a wildcard whose lower bound is the type variable of
   * the first parameter type.
   *
   * @param <T> the element type
   * @param list a list
   * @param comparator a comparator
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Number> void numberAndComparator(
      List<T> list, Comparator<? super T> comparator) {}

  /**
   * Tests that matching an input type to a parameter type uses the types already chosen for the
   * earlier parameter types. In {@code <T extends Number> void numberAndComparator(List<T>,
   * Comparator<? super T>)}, once {@code T} is {@code Integer}, {@code
   * Comparator<Comparable<Integer>>} instantiates the second parameter type, even though {@code
   * Comparable<Integer>} cannot instantiate {@code T}.
   */
  @Test
  public void testMatchUsesEarlierChoices()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    GenericClassType comparableType = GenericClassType.forClass(Comparable.class);
    InstantiatedType comparatorOfComparable =
        GenericClassType.forClass(Comparator.class)
            .instantiate(comparableType.instantiate(INTEGER_TYPE));
    addTypes(INTEGER_TYPE, inputTypes);
    addTypes(GenericClassType.forClass(List.class).instantiate(INTEGER_TYPE), inputTypes);
    addTypes(comparatorOfComparable, inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation numberAndComparator =
        TypedOperation.forMethod(
            TypeInstantiatorTest.class.getDeclaredMethod(
                "numberAndComparator", List.class, Comparator.class));

    int instantiated = 0;
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(numberAndComparator);
      if (operation == null) {
        continue;
      }
      instantiated++;
      assertEquals(INTEGER_TYPE, typeArgument(operation.getInputTypes().get(0), 0));
      assertEquals(comparatorOfComparable, operation.getInputTypes().get(1));
    }
    assertTrue("no instantiation of " + numberAndComparator, instantiated > 0);
  }

  /**
   * A method whose return type mentions only a type variable whose bound mentions another type
   * variable.
   *
   * @param <T> the result element type
   * @param <U> the argument element type
   * @param list a list
   * @return a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Comparable<U>, U> List<T> comparableToElements(List<U> list) {
    return Collections.emptyList();
  }

  /**
   * Tests that instantiating an operation for a goal type checks the bounds of the type variables
   * that the goal type determines, once the type variables in those bounds are chosen. For the goal
   * type {@code List<String>}, {@code <T extends Comparable<U>, U> List<T>
   * comparableToElements(List<U>)} must not be instantiated with a {@code List<Integer>} argument,
   * because {@code String} is not a {@code Comparable<Integer>}.
   */
  @Test
  public void testGoalTypeChecksBoundsOfDeterminedVariables()
      throws NoSuchMethodException, RandoopSpecificationError {
    GenericClassType listType = GenericClassType.forClass(List.class);
    InstantiatedType listOfString = listType.instantiate(JavaTypes.STRING_TYPE);
    TypedClassOperation comparableToElements =
        TypedOperation.forMethod(
            TypeInstantiatorTest.class.getDeclaredMethod("comparableToElements", List.class));

    // With only List<Integer> as an argument, there is no instantiation.
    Set<Type> integerInputTypes = new LinkedHashSet<>();
    addTypes(INTEGER_TYPE, integerInputTypes);
    addTypes(listType.instantiate(INTEGER_TYPE), integerInputTypes);
    TypeInstantiator integerInstantiator = new TypeInstantiator(integerInputTypes);
    for (int i = 0; i < ATTEMPTS; i++) {
      assertNull(integerInstantiator.instantiate(comparableToElements, listOfString));
    }

    Set<Type> inputTypes = new LinkedHashSet<>(integerInputTypes);
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(listOfString, inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);

    int instantiated = 0;
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(comparableToElements, listOfString);
      if (operation == null) {
        continue;
      }
      instantiated++;
      assertEquals(listOfString, operation.getOutputType());
      assertEquals(listOfString, operation.getInputTypes().get(0));
    }
    assertTrue("no instantiation of " + comparableToElements, instantiated > 0);
  }

  /**
   * A method in whose signature the type variable {@code U} occurs only in the bounds of other type
   * variables.
   *
   * @param <T> the key type
   * @param <S> the value type
   * @param <U> a type variable that occurs only in bounds
   * @return a map
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Comparable<U>, S extends Comparable<U>, U> Map<T, S> sharedBound() {
    return Collections.emptyMap();
  }

  /**
   * Tests that instantiating an operation for a goal type chooses a type for each type variable
   * that occurs only in bounds, even if the goal type determines the whole signature. For the goal
   * type {@code Map<String, Integer>}, {@code <T extends Comparable<U>, S extends Comparable<U>, U>
   * Map<T, S> sharedBound()} has no instantiation, because no {@code U} makes both {@code String}
   * and {@code Integer} a {@code Comparable<U>}.
   */
  @Test
  public void testGoalTypeChecksVariablesOnlyInBounds()
      throws NoSuchMethodException, RandoopSpecificationError {
    GenericClassType mapType = GenericClassType.forClass(Map.class);
    TypedClassOperation sharedBound =
        TypedOperation.forMethod(TypeInstantiatorTest.class.getDeclaredMethod("sharedBound"));

    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(INTEGER_TYPE, inputTypes);
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);

    InstantiatedType stringToInteger = mapType.instantiate(JavaTypes.STRING_TYPE, INTEGER_TYPE);
    InstantiatedType stringToString =
        mapType.instantiate(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE);
    for (int i = 0; i < ATTEMPTS; i++) {
      assertNull(instantiator.instantiate(sharedBound, stringToInteger));
      TypedClassOperation operation = instantiator.instantiate(sharedBound, stringToString);
      assertNotNull(operation);
      assertEquals(stringToString, operation.getOutputType());
    }
  }

  /** A class that is comparable to strings. */
  @SuppressWarnings({
    "ComparableType", // comparable to another type, to test bounds
    "PMD.OverrideBothEqualsAndHashCodeOnComparable" // tests bounds, not equality or ordering
  })
  static class Ranked implements Comparable<String> {
    @Override
    public int compareTo(String other) {
      return 0;
    }
  }

  /** Another class that is comparable to strings. */
  @SuppressWarnings({
    "ComparableType", // comparable to another type, to test bounds
    "PMD.OverrideBothEqualsAndHashCodeOnComparable" // tests bounds, not equality or ordering
  })
  static class RankedToo implements Comparable<String> {
    @Override
    public int compareTo(String other) {
      return 0;
    }
  }

  /**
   * A method whose return type mentions only a type variable whose bound mentions another type
   * variable, which occurs nowhere else.
   *
   * @param <T> the result element type
   * @param <U> the type that the elements are comparable to
   * @return a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Comparable<U>, U> List<T> comparableList() {
    return Collections.emptyList();
  }

  /**
   * Tests that instantiating an operation for a goal type derives the type of a type variable that
   * occurs only in bounds from the types that the goal type determines, even if it is not an input
   * type. For the goal type {@code List<Ranked>}, {@code <T extends Comparable<U>, U> List<T>
   * comparableList()} is instantiated with {@code String} for {@code U}, because {@code Ranked} is
   * a {@code Comparable<String>}. Incompatible shared bounds still have no instantiation.
   */
  @Test
  public void testGoalTypeImpliesVariableOnlyInBounds()
      throws NoSuchMethodException, RandoopSpecificationError {
    ClassOrInterfaceType rankedType = ClassOrInterfaceType.forClass(Ranked.class);
    ClassOrInterfaceType rankedTooType = ClassOrInterfaceType.forClass(RankedToo.class);
    TypeInstantiator instantiator = new TypeInstantiator(new LinkedHashSet<>());

    TypedClassOperation comparableList =
        TypedOperation.forMethod(TypeInstantiatorTest.class.getDeclaredMethod("comparableList"));
    InstantiatedType listOfRanked = GenericClassType.forClass(List.class).instantiate(rankedType);

    TypedClassOperation sharedBound =
        TypedOperation.forMethod(TypeInstantiatorTest.class.getDeclaredMethod("sharedBound"));
    GenericClassType mapType = GenericClassType.forClass(Map.class);
    InstantiatedType rankedToRankedToo = mapType.instantiate(rankedType, rankedTooType);
    InstantiatedType rankedToInteger = mapType.instantiate(rankedType, INTEGER_TYPE);

    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(comparableList, listOfRanked);
      assertNotNull(operation);
      assertEquals(listOfRanked, operation.getOutputType());

      operation = instantiator.instantiate(sharedBound, rankedToRankedToo);
      assertNotNull(operation);
      assertEquals(rankedToRankedToo, operation.getOutputType());

      // No U makes both Ranked and Integer a Comparable<U>.
      assertNull(instantiator.instantiate(sharedBound, rankedToInteger));
    }
  }

  /** A serializable class that is comparable to strings. */
  @SuppressWarnings({
    "ComparableType", // comparable to another type, to test bounds
    "PMD.OverrideBothEqualsAndHashCodeOnComparable" // tests bounds, not equality or ordering
  })
  static class SerializableRanked implements Comparable<String>, Serializable {
    /** ID for serializing this class. */
    private static final long serialVersionUID = 20261122;

    @Override
    public int compareTo(String other) {
      return 0;
    }
  }

  /**
   * A method whose return type mentions only a type variable with an intersection bound, one of
   * whose members mentions another type variable, which occurs nowhere else.
   *
   * @param <T> the result element type
   * @param <U> the type that the elements are comparable to
   * @return a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Comparable<U> & Serializable, U> List<T> serializableComparableList() {
    return Collections.emptyList();
  }

  /**
   * Tests that instantiating an operation for a goal type derives the type of a type variable that
   * occurs only in a member of an intersection bound. For the goal type {@code
   * List<SerializableRanked>}, {@code <T extends Comparable<U> & Serializable, U> List<T>
   * serializableComparableList()} is instantiated with {@code String} for {@code U}. For the goal
   * type {@code List<Ranked>}, there is no instantiation, because {@code Ranked} is not {@code
   * Serializable}.
   */
  @Test
  public void testGoalTypeImpliesVariableInIntersectionBound()
      throws NoSuchMethodException, RandoopSpecificationError {
    TypeInstantiator instantiator = new TypeInstantiator(new LinkedHashSet<>());
    TypedClassOperation serializableComparableList =
        TypedOperation.forMethod(
            TypeInstantiatorTest.class.getDeclaredMethod("serializableComparableList"));
    GenericClassType listType = GenericClassType.forClass(List.class);
    InstantiatedType listOfSerializableRanked =
        listType.instantiate(ClassOrInterfaceType.forClass(SerializableRanked.class));
    InstantiatedType listOfRanked =
        listType.instantiate(ClassOrInterfaceType.forClass(Ranked.class));

    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation =
          instantiator.instantiate(serializableComparableList, listOfSerializableRanked);
      assertNotNull(operation);
      assertEquals(listOfSerializableRanked, operation.getOutputType());
      assertNull(instantiator.instantiate(serializableComparableList, listOfRanked));
    }
  }

  /**
   * A method whose return type mentions only a type variable whose bound mentions two other type
   * variables, which occur nowhere else.
   *
   * @param <T> the result element type
   * @param <K> the key type of {@code T}
   * @param <V> the value type of {@code T}
   * @return a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Map<K, V>, K, V> List<T> mapList() {
    return Collections.emptyList();
  }

  /**
   * Tests that instantiating an operation for a goal type derives the types of type variables that
   * occur only in a bound that mentions several of them. For the goal type {@code
   * List<HashMap<String, Integer>>}, {@code <T extends Map<K, V>, K, V> List<T> mapList()} is
   * instantiated with {@code String} for {@code K} and {@code Integer} for {@code V}, though
   * neither is an input type.
   */
  @Test
  public void testGoalTypeImpliesSeveralVariablesOnlyInBound()
      throws NoSuchMethodException, RandoopSpecificationError {
    TypeInstantiator instantiator = new TypeInstantiator(new LinkedHashSet<>());
    TypedClassOperation mapList =
        TypedOperation.forMethod(TypeInstantiatorTest.class.getDeclaredMethod("mapList"));
    InstantiatedType listOfMap =
        GenericClassType.forClass(List.class)
            .instantiate(
                GenericClassType.forClass(HashMap.class)
                    .instantiate(JavaTypes.STRING_TYPE, INTEGER_TYPE));
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(mapList, listOfMap);
      assertNotNull(operation);
      assertEquals(listOfMap, operation.getOutputType());
    }
  }

  /**
   * A generic class with an inner class that declares no type parameters.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  static class MemberOuter<T> {
    /** An inner class that declares no type parameters. */
    class Inner {}
  }

  /** A field whose type is a list of a member class of a parameterized type. */
  @SuppressWarnings({"UnusedVariable", "PMD.UnusedPrivateField"}) // the field is read reflectively
  private static List<MemberOuter<String>.Inner> stringInnerList;

  /**
   * A method whose return type mentions only a type variable whose bound is a member class whose
   * enclosing type mentions another type variable.
   *
   * @param <U> the type argument of the enclosing type of the bound of {@code T}
   * @param <T> the element type
   * @return a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <U, T extends MemberOuter<U>.Inner> List<T> memberClassBound() {
    return Collections.emptyList();
  }

  /**
   * Tests that instantiating an operation for a goal type derives a type variable that occurs only
   * in the enclosing type of a member-class bound. For the goal type {@code
   * List<MemberOuter<String>.Inner>}, {@code <U, T extends MemberOuter<U>.Inner> List<T>
   * memberClassBound()} is instantiated with {@code String} for {@code U}, though {@code String} is
   * not an input type.
   */
  @Test
  public void testGoalTypeImpliesVariableInMemberClassBound()
      throws NoSuchMethodException, NoSuchFieldException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.OBJECT_TYPE, inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation memberClassBound =
        TypedOperation.forMethod(TypeInstantiatorTest.class.getDeclaredMethod("memberClassBound"));
    ReferenceType goalType =
        (ReferenceType)
            Type.forType(
                TypeInstantiatorTest.class.getDeclaredField("stringInnerList").getGenericType());

    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(memberClassBound, goalType);
      assertNotNull(operation);
      assertEquals(goalType, operation.getOutputType());
    }
  }

  /**
   * A method whose parameter type mentions a type variable twice.
   *
   * @param <T> the key and value type
   * @param map a map
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T> void sameKeyAndValue(Map<T, T> map) {}

  /**
   * Tests that an input type that matches each type argument of a parameter type separately, but
   * that maps a type variable to two different types, is rejected without preventing the choice of
   * another input type. {@code <T> void sameKeyAndValue(Map<T, T>)} is instantiated only as {@code
   * sameKeyAndValue(Map<String, String>)}, not with {@code Map<String, Integer>}.
   */
  @Test
  public void testConflictingMatchIsRejected()
      throws NoSuchMethodException, RandoopSpecificationError {
    GenericClassType hashMapType = GenericClassType.forClass(HashMap.class);
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(INTEGER_TYPE, inputTypes);
    addTypes(hashMapType.instantiate(JavaTypes.STRING_TYPE, INTEGER_TYPE), inputTypes);
    addTypes(hashMapType.instantiate(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE), inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation sameKeyAndValue =
        TypedOperation.forMethod(
            TypeInstantiatorTest.class.getDeclaredMethod("sameKeyAndValue", Map.class));

    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(sameKeyAndValue);
      assertNotNull(operation);
      assertEquals(JavaTypes.STRING_TYPE, typeArgument(operation.getInputTypes().get(0), 0));
      assertEquals(JavaTypes.STRING_TYPE, typeArgument(operation.getInputTypes().get(0), 1));
    }
  }

  /**
   * A method in whose signature the type variables {@code K} and {@code V} occur only in the bound
   * of another type variable.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param <T> the element type
   * @return a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <K, V, T extends Map<K, V>> List<T> listOfMaps() {
    return Collections.emptyList();
  }

  /**
   * Tests that instantiating an operation for a goal type finds types for the type variables that
   * occur only in a bound that mentions several of them, even if those types are not input types.
   * For the goal type {@code List<HashMap<String, Integer>>}, {@code <K, V, T extends Map<K, V>>
   * List<T> listOfMaps()} is instantiated with {@code K} as {@code String} and {@code V} as {@code
   * Integer}.
   */
  @Test
  public void testGoalTypeFindsTypesForVariablesInSharedBound()
      throws NoSuchMethodException, RandoopSpecificationError {
    GenericClassType listType = GenericClassType.forClass(List.class);
    GenericClassType hashMapType = GenericClassType.forClass(HashMap.class);
    TypedClassOperation listOfMaps =
        TypedOperation.forMethod(TypeInstantiatorTest.class.getDeclaredMethod("listOfMaps"));

    Set<Type> inputTypes = new LinkedHashSet<>();
    inputTypes.add(JavaTypes.OBJECT_TYPE);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);

    InstantiatedType goalType =
        listType.instantiate(hashMapType.instantiate(JavaTypes.STRING_TYPE, INTEGER_TYPE));
    for (int i = 0; i < ATTEMPTS; i++) {
      TypedClassOperation operation = instantiator.instantiate(listOfMaps, goalType);
      assertNotNull(operation);
      assertEquals(goalType, operation.getOutputType());
    }
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
