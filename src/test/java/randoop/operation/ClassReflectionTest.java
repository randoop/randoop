package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.util.List;
import org.junit.Test;
import randoop.Globals;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationExtractor;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.JavaTypes;

/** Tests of reflection. */
public class ClassReflectionTest {

  // TODO: reinstate
  // @Test
  // public void implementsParameterizedTypeTest() {
  //   Class<?> c = AnIntegerPredicate.class;
  //   Set<TypedOperation> actual = getConcreteOperations(c);
  //   // TODO be sure the types of the inherited method has the proper type arguments
  //   assertEquals(5, actual.size());
  // }

  private List<TypedOperation> getConcreteOperations(Class<?> c) {
    return OperationExtractor.operations(
        c, new DefaultReflectionPredicate(), OmitMethodsPredicate.NO_OMISSION, IS_PUBLIC);
  }

  @Test
  public void innerClassTest() {
    Class<?> outer = randoop.test.ClassWithInnerClass.class;
    Class<?> inner;
    try {
      inner = Class.forName("randoop.test.ClassWithInnerClass$A");
    } catch (ClassNotFoundException e) {
      fail("could not load inner class" + e.getMessage());
      throw new Error("unreachable");
    }

    List<TypedOperation> innerActual = getConcreteOperations(inner);
    assertEquals(7, innerActual.size());

    List<TypedOperation> outerActual = getConcreteOperations(outer);
    assertEquals(3, outerActual.size());

    TypedOperation constructorOp = null;
    for (TypedOperation op : outerActual) {
      if (op.isConstructorCall()) {
        constructorOp = op;
      }
    }
    assertNotNull("should find outer class constructor", constructorOp);

    Sequence sequence = new Sequence();
    randoop.test.ClassWithInnerClass classWithInnerClass1 = new randoop.test.ClassWithInnerClass(1);
    TypedOperation nextOp = TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, 1);
    sequence = sequence.extend(nextOp);
    sequence = sequence.extend(constructorOp, new Variable(sequence, 0));

    @SuppressWarnings("UnusedVariable")
    randoop.test.ClassWithInnerClass.A a4 = classWithInnerClass1.new A("blah", 29);
    sequence =
        sequence.extend(
            TypedOperation.createPrimitiveInitialization(JavaTypes.STRING_TYPE, "blah"));
    sequence =
        sequence.extend(TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, 29));

    TypedOperation innerConstructorOp = null;
    for (TypedOperation op : innerActual) {
      if (op.isConstructorCall()) {
        innerConstructorOp = op;
      }
    }
    assertNotNull(innerConstructorOp);
    sequence =
        sequence.extend(
            innerConstructorOp,
            new Variable(sequence, 1),
            new Variable(sequence, 2),
            new Variable(sequence, 3));

    String expectedCode =
        "randoop.test.ClassWithInnerClass classWithInnerClass1 = new randoop.test.ClassWithInnerClass(1);"
            + Globals.lineSep
            + "randoop.test.ClassWithInnerClass.A a4 = classWithInnerClass1.new A(\"blah\", 29);"
            + Globals.lineSep;

    assertEquals(expectedCode, sequence.toCodeString());

    // TODO be more sophisticated in checking operations
  }
}
