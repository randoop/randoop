package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedOperation;
import randoop.types.test.CaptureTestClass;
import randoop.types.test.Container;
import randoop.types.test.Gibberish;
import randoop.types.test.Nonsense;

/** Tests capture conversion over the input types in the operations of {@link CaptureTestClass}. */
public class CaptureConversionTest {

  private static GenericClassType sourceType;
  private static GenericClassType containerType;
  private static List<TypedOperation> listOperations;
  private static List<TypedOperation> containerOperations;

  @BeforeClass
  public static void setup() {
    Class<?> c = CaptureTestClass.class;
    sourceType = GenericClassType.forClass(c);
    containerType = GenericClassType.forClass(Container.class);
    listOperations = new ArrayList<>();
    containerOperations = new ArrayList<>();
    try {
      listOperations.add(TypedOperation.forMethod(c.getMethod("a", List.class)));
      listOperations.add(TypedOperation.forMethod(c.getMethod("b", List.class)));
      listOperations.add(TypedOperation.forMethod(c.getMethod("c", List.class)));
      listOperations.add(TypedOperation.forMethod(c.getMethod("d", List.class)));
      containerOperations.add(TypedOperation.forMethod(c.getMethod("a", Container.class)));
      containerOperations.add(TypedOperation.forMethod(c.getMethod("b", Container.class)));
      containerOperations.add(TypedOperation.forMethod(c.getMethod("c", Container.class)));
    } catch (NoSuchMethodException e) {
      fail("didn't find method: " + e.getMessage());
    } catch (RandoopSpecificationError e) {
      fail("bad specification: " + e.getMessage());
    }
  }

  /**
   * List tests check capture conversion where the type parameter of the generic type is bound by
   * Object. In this case, the GLB is trivial.
   */
  @Test
  public void captureStringTest() {
    checkCapture(JDKTypes.LIST_TYPE, JavaTypes.STRING_TYPE, listOperations);
  }

  /** Tests capture when wildcard bound is not a class type. */
  @Test
  public void captureArrayTest() {
    checkCapture(
        JDKTypes.LIST_TYPE,
        ArrayType.ofComponentType(ReferenceType.forClass(Integer.class)),
        listOperations);
  }

  /**
   * A class with a field whose type is a parameterized type with a {@code ? super T} type argument.
   *
   * @param <T> a type parameter with a self-referential bound
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class SuperSelfComparable<T extends Comparable<T>> {
    /**
     * A parameterized type with a wildcard type argument, whose lower bound is a type parameter
     * with a self-referential bound.
     */
    Comparable<? super T> superT;
  }

  /** Tests capture when the wildcard bound is a type variable with a self-referential bound. */
  @Test
  public void captureSelfComparableBoundTest() throws NoSuchFieldException {
    InstantiatedType type =
        (InstantiatedType)
            Type.forType(SuperSelfComparable.class.getDeclaredField("superT").getGenericType());
    WildcardArgument wildcard = (WildcardArgument) type.getTypeArguments().get(0);
    CaptureTypeVariable capture = getCaptureVariable(type.applyCaptureConversion());
    assertEquals(wildcard.getTypeBound(), capture.getLowerTypeBound());
    assertTrue(capture.getUpperTypeBound().isObject());
  }

  /**
   * A class with a field whose type is a parameterized type with a {@code ? super T} type argument,
   * where the bounds of T and U refer to each other.
   *
   * @param <T> a type parameter whose bound refers to U
   * @param <U> a type parameter whose bound refers to T
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class SuperMutuallyComparable<T extends Comparable<U>, U extends Comparable<T>> {
    /**
     * A parameterized type with a wildcard type argument, whose lower bound is a type parameter
     * with a mutually recursive bound.
     */
    Comparable<? super T> superT;
  }

  /** Tests capture when the wildcard bound is a type variable with a mutually recursive bound. */
  @Test
  public void captureMutuallyComparableBoundTest() throws NoSuchFieldException {
    InstantiatedType type =
        (InstantiatedType)
            Type.forType(SuperMutuallyComparable.class.getDeclaredField("superT").getGenericType());
    WildcardArgument wildcard = (WildcardArgument) type.getTypeArguments().get(0);
    CaptureTypeVariable capture = getCaptureVariable(type.applyCaptureConversion());
    assertEquals(wildcard.getTypeBound(), capture.getLowerTypeBound());
    assertTrue(capture.getUpperTypeBound().isObject());
  }

  /**
   * A class with a field whose type has a wildcard type argument, whose bound contains a wildcard
   * whose bound is a type parameter.
   *
   * @param <T> a type parameter whose bound contains a wildcard
   */
  @SuppressWarnings("UnusedVariable") // the field is read reflectively
  static class ExtendsSuperWildcardComparable<T extends Comparable<? super T>> {
    /** A parameterized type whose wildcard type argument has a bound that contains a wildcard. */
    List<? extends Comparable<? super T>> extendsSuperT;
  }

  /**
   * Tests capture when the wildcard bound is a parameterized type with a wildcard argument. Capture
   * conversion leaves the wildcard bound unchanged.
   */
  @Test
  public void captureNestedWildcardBoundTest() throws NoSuchFieldException {
    InstantiatedType type =
        (InstantiatedType)
            Type.forType(
                ExtendsSuperWildcardComparable.class
                    .getDeclaredField("extendsSuperT")
                    .getGenericType());
    WildcardArgument wildcard = (WildcardArgument) type.getTypeArguments().get(0);
    CaptureTypeVariable capture = getCaptureVariable(type.applyCaptureConversion());
    assertTrue(isGlbWithObject(capture.getUpperTypeBound(), wildcard.getTypeBound()));
  }

  /**
   * Returns true if {@code bound} is equivalent to {@code glb(Object, other)}: that is, if it is
   * {@code other} or the intersection of {@code Object} and {@code other}.
   *
   * @param bound the bound to test
   * @param other the bound that {@code bound} should be equivalent to
   * @return true if {@code bound} is {@code other}, possibly intersected with {@code Object}
   */
  private static boolean isGlbWithObject(ParameterBound bound, ParameterBound other) {
    ParameterBound objectBound = ParameterBound.forType(JavaTypes.OBJECT_TYPE);
    return bound.equals(other)
        || bound.equals(new IntersectionTypeBound(Arrays.asList(objectBound, other)))
        || bound.equals(new IntersectionTypeBound(Arrays.asList(other, objectBound)));
  }

  /**
   * Returns the capture variable that is the sole type argument of the given type.
   *
   * @param type a parameterized type with one type argument, which is a capture variable
   * @return the type argument of {@code type}
   */
  private static CaptureTypeVariable getCaptureVariable(InstantiatedType type) {
    TypeArgument argument = type.getTypeArguments().get(0);
    ReferenceType referenceType = ((ReferenceArgument) argument).getReferenceType();
    assertTrue(referenceType.isCaptureVariable());
    return (CaptureTypeVariable) referenceType;
  }

  /** Container tests involve a generic class with a type parameter T bound by Comparable<T>. */
  @Test
  public void captureStringContainerTest() {
    checkCapture(containerType, JavaTypes.STRING_TYPE, containerOperations);
  }

  /**
   * This test makes the wildcard bound be the class Nonsense, which does not implement
   * Comparable<Nonsense> but ends up with Container<Gibberish> where Gibberish extends Nonsense and
   * implements Comparable<Gibberish>.
   */
  @Test
  public void captureNonsenseContainerTest() {
    ClassOrInterfaceType nonsenseType = ClassOrInterfaceType.forClass(Nonsense.class);
    ClassOrInterfaceType gibberishType = ClassOrInterfaceType.forClass(Gibberish.class);
    checkCapture(containerType, nonsenseType, gibberishType, containerOperations);
  }

  @Test
  public void captureArrayContainerTest() {
    try {
      checkCapture(
          containerType,
          ArrayType.ofComponentType(ReferenceType.forClass(Integer.class)),
          containerOperations);
      fail("instantiate should throw exception");
    } catch (IllegalArgumentException e) {
      assertTrue(
          "instantiate exception mismatch: " + e.getMessage(),
          e.getMessage().contains("type argument java.lang.Integer[] does not match"));
    }
  }

  /** A field whose type has a wildcard only within a type argument. */
  public List<List<?>> nestedWildcardField;

  /** Tests that capture conversion does not convert a wildcard nested within a type argument. */
  @Test
  public void nestedWildcardTest() throws NoSuchFieldException {
    InstantiatedType nestedWildcardType =
        (InstantiatedType)
            Type.forType(
                CaptureConversionTest.class.getField("nestedWildcardField").getGenericType());
    InstantiatedType capturedType = nestedWildcardType.applyCaptureConversion();
    assertEquals(nestedWildcardType, capturedType);

    // List<List<String>> is not a subtype of List<List<?>>.
    InstantiatedType stringListListType =
        JDKTypes.LIST_TYPE.instantiate(JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE));
    assertFalse(stringListListType.isSubtypeOfOrEqualTo(capturedType));
  }

  /** A field whose type has a wildcard within the bound of a wildcard type argument. */
  public List<? extends List<?>> wildcardInBoundField;

  /** Tests that capture conversion does not convert a wildcard within the bound of a wildcard. */
  @Test
  public void wildcardInBoundTest() throws NoSuchFieldException {
    InstantiatedType wildcardInBoundType =
        (InstantiatedType)
            Type.forType(
                CaptureConversionTest.class.getField("wildcardInBoundField").getGenericType());
    InstantiatedType capturedType = wildcardInBoundType.applyCaptureConversion();
    TypeVariable captureVariable =
        (TypeVariable)
            ((ReferenceArgument) capturedType.getTypeArguments().get(0)).getReferenceType();
    assertTrue(captureVariable.isCaptureVariable());
    // The upper bound includes List<?>, not List<CaptureN>.
    assertTrue(captureVariable.getUpperTypeBound().hasWildcard());
  }

  /** A field whose type has a wildcard type argument. */
  public ArrayList<?> wildcardField;

  /**
   * Tests that a parameterized type is not a subtype of a same-class type whose type argument is a
   * capture variable.
   */
  @Test
  public void captureVariableSubtypeTest() throws NoSuchFieldException {
    InstantiatedType capturedType =
        ((InstantiatedType)
                Type.forType(
                    CaptureConversionTest.class.getField("wildcardField").getGenericType()))
            .applyCaptureConversion();
    InstantiatedType stringArrayListType =
        JDKTypes.ARRAY_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    assertFalse(stringArrayListType.isSubtypeOfOrEqualTo(capturedType));
    assertTrue(stringArrayListType.isSubtypeOfOrEqualTo(stringArrayListType));
  }

  /**
   * A generic class with a generic member class.
   *
   * @param <T> the type parameter of the outer class
   */
  public static class Outer<T> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter of the member class
     */
    public class Inner<U> {}
  }

  /**
   * A field whose type is a member class of a generic class. {@link #wildcardInEnclosingTypeTest}
   * replaces the enclosing type by one that has a wildcard type argument.
   */
  public Outer<String>.Inner<String> memberField;

  /** A field whose type has a wildcard type argument. */
  public Outer<?> wildcardOuterField;

  /** Tests that capture conversion converts a wildcard in the enclosing type. */
  @Test
  public void wildcardInEnclosingTypeTest() throws NoSuchFieldException {
    InstantiatedType memberType =
        (InstantiatedType)
            Type.forType(CaptureConversionTest.class.getField("memberField").getGenericType());
    // Type.forType does not record the type arguments of an enclosing type, so set it directly.
    memberType.enclosingType =
        (ClassOrInterfaceType)
            Type.forType(
                CaptureConversionTest.class.getField("wildcardOuterField").getGenericType());
    assertTrue(memberType.hasWildcardArgument());

    InstantiatedType capturedType = memberType.applyCaptureConversion();
    assertFalse(capturedType.hasWildcardArgument());
    assertTrue(((InstantiatedType) capturedType.enclosingType).hasCaptureVariable());
  }

  /**
   * Checks the capture conversion by calling {@link #checkCapture(GenericClassType, ReferenceType,
   * ReferenceType, List)} where the bound type is substituted into the final type. The wildcard
   * types are carried as input types to the generic operation.
   *
   * @param genericClassType the generic class being instantiated
   * @param paramType the wildcard bound
   * @param genericOperations the set of operations with the wildcard types
   */
  private void checkCapture(
      GenericClassType genericClassType,
      ReferenceType paramType,
      List<TypedOperation> genericOperations) {
    checkCapture(genericClassType, paramType, paramType, genericOperations);
  }

  /**
   * Checks the capture conversion over a set of types with wildcard (given as the input types to
   * operations). Checks that the conversion followed by the substitution for the capture variable
   * result in the class type instantiated by the actual argument type.
   *
   * @param genericClassType the generic class being instantiated
   * @param paramType the wildcard bound
   * @param actualArgType the actual argument type
   * @param genericOperations the set of operations with the wildcard types
   */
  private void checkCapture(
      GenericClassType genericClassType,
      ReferenceType paramType,
      ReferenceType actualArgType,
      List<TypedOperation> genericOperations) {
    InstantiatedType finalType = genericClassType.instantiate(actualArgType);
    InstantiatedType instantiatedType = sourceType.instantiate(paramType);
    Substitution substitution = instantiatedType.getTypeSubstitution();
    for (TypedOperation op : genericOperations) {
      InstantiatedType argumentType = getArgumentType(op).substitute(substitution);
      InstantiatedType convertedArgumentType = argumentType.applyCaptureConversion();
      List<TypeVariable> arguments = convertedArgumentType.getTypeParameters();
      if (!arguments.isEmpty()) {
        Substitution wcSubst = new Substitution(arguments, actualArgType);
        convertedArgumentType = convertedArgumentType.substitute(wcSubst);
      }
      if (op.hasWildcardTypes()) {
        assertEquals(
            "should be instantiated type for method " + op.getName() + " argument.",
            finalType,
            convertedArgumentType);
      } else {
        assertEquals(
            "should not be converted " + op.getName(), argumentType, convertedArgumentType);
      }
    }
  }

  private InstantiatedType getArgumentType(TypedOperation op) {
    TypeTuple inputTypes = op.getInputTypes();
    assertEquals(inputTypes.size(), 2);
    return (InstantiatedType) inputTypes.get(1);
  }
}
