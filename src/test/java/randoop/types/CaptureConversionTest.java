package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
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
    }
  }

  /**
   * List tests check capture conversion where the type parameter of the generic type is bound by
   * Object. In this case, the GLB is trivial
   */
  @Test
  public void captureStringTest() {
    checkCapture(JDKTypes.LIST_TYPE, JavaTypes.STRING_TYPE, listOperations);
  }

  /** Tests capture when wildcard bound is not a class type */
  @Test
  public void captureArrayTest() {
    checkCapture(
        JDKTypes.LIST_TYPE,
        ArrayType.ofComponentType(ReferenceType.forClass(Integer.class)),
        listOperations);
  }

  /** Container tests involve a generic class with a type parameter T bound by Comparable<T> */
  @Test
  public void captureStringContainerTest() {
    checkCapture(containerType, JavaTypes.STRING_TYPE, containerOperations);
  }

  /**
   * This test makes the wildcard bound be the class Nonsense, which does not implement
   * Comparable<Nonsense> but ends up with Container<Gibberish> where Gibberish extends Nonsense and
   * implements Comparable<Gibberish>
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
    Substitution<ReferenceType> substitution = instantiatedType.getTypeSubstitution();
    for (TypedOperation op : genericOperations) {
      InstantiatedType argumentType = getArgumentType(op).apply(substitution);
      InstantiatedType convertedArgumentType = argumentType.applyCaptureConversion();
      List<TypeVariable> arguments = convertedArgumentType.getTypeParameters();
      if (arguments.size() > 0) {
        Substitution<ReferenceType> wcSubst = Substitution.forArgs(arguments, actualArgType);
        convertedArgumentType = convertedArgumentType.apply(wcSubst);
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
