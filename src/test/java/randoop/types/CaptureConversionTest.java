package randoop.types;

import static org.junit.Assert.assertEquals;
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
    assertEquals(
        new IntersectionTypeBound(
            Arrays.asList(ParameterBound.forType(JavaTypes.OBJECT_TYPE), wildcard.getTypeBound())),
        capture.getUpperTypeBound());
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
