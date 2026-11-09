package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
