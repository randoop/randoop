package randoop.types;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import randoop.operation.TypedOperation;
import randoop.types.test.CaptureTestClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by bjkeller on 6/2/16.
 */
public class CaptureConversionTest {

  private static GenericClassType genericType;
  private static List<TypedOperation> genericOperations;

  @BeforeClass
  public static void setup() {
    Class<?> c = CaptureTestClass.class;
    genericType = GenericClassType.forClass(c);
    genericOperations = new ArrayList<>();
    try {
      genericOperations.add(TypedOperation.forMethod(c.getMethod("a", List.class)));
      genericOperations.add(TypedOperation.forMethod(c.getMethod("b", List.class)));
      genericOperations.add(TypedOperation.forMethod(c.getMethod("c", List.class)));
      genericOperations.add(TypedOperation.forMethod(c.getMethod("d", List.class)));
    } catch (NoSuchMethodException e) {
      fail("didn't find method: " + e.getMessage());
    }

  }

  @Test
  public void captureStringTest() {
    checkCapture(ConcreteTypes.STRING_TYPE);
  }

  @Test
  public void captureArrayTest() {
    checkCapture(ArrayType.ofElementType(ReferenceType.forClass(Integer.class)));
  }

  private void checkCapture(ReferenceType paramType) {
    InstantiatedType finalType = JDKTypes.LIST_TYPE.instantiate(paramType);
    InstantiatedType instantiatedType = genericType.instantiate(paramType);
    Substitution<ReferenceType> substitution = instantiatedType.getTypeSubstitution();
    for (TypedOperation op : genericOperations) {

      InstantiatedType argumentType = getArgumentType(op).apply(substitution);
      InstantiatedType convertedArgumentType = argumentType.applyCaptureConversion();
      List<AbstractTypeVariable> arguments = convertedArgumentType.getTypeParameters();
      if (arguments.size() > 0) {
        Substitution<ReferenceType> wcSubst = Substitution.forArgs(arguments, paramType);
        convertedArgumentType = convertedArgumentType.apply(wcSubst);
      }
      if (op.hasWildcardTypes()) {
        assertEquals("should be instantiated type for method " + op.getName() + " argument.", finalType, convertedArgumentType);
      } else {
        assertEquals("should not be converted " + op.getName(), argumentType, convertedArgumentType);
      }
    }
  }


  private InstantiatedType getArgumentType(TypedOperation op) {
    TypeTuple inputTypes = op.getInputTypes();
    assert inputTypes.size() == 2;
    return (InstantiatedType) inputTypes.get(1);
  }

}
