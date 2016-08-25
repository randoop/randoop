package randoop.operation;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.Globals;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.OperationModel;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.AnIntegerPredicate;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests of reflection.
 */
public class ClassReflectionTest {

  @Test
  public void implementsParameterizedTypeTest() {
    Class<?> c = AnIntegerPredicate.class;
    Set<TypedOperation> actual = getConcreteOperations(c);

    // TODO be sure the types of the inherited method has the proper type arguments
    assertEquals("number of operations", 4, actual.size());
  }

  private Set<TypedOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(
        c, new DefaultReflectionPredicate(), new PublicVisibilityPredicate());
  }

  private Set<TypedOperation> getConcreteOperations(
      Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    OperationExtractor extractor =
        new OperationExtractor(classType, operations, predicate, new OperationModel());
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.apply(extractor, c);
    return operations;
  }

  @Test
  public void innerClassTest() {
    Class<?> outer = randoop.test.ClassWithInnerClass.class;
    Class<?> inner = null;
    try {
      inner = Class.forName("randoop.test.ClassWithInnerClass$A");
    } catch (ClassNotFoundException e) {
      fail("could not load inner class" + e.getMessage());
    }

    Set<TypedOperation> innerActual = getConcreteOperations(inner);
    assertEquals("number of inner class operations", 6, innerActual.size());

    Set<TypedOperation> outerActual = getConcreteOperations(outer);
    assertEquals("number of outer operations", 2, outerActual.size());

    TypedOperation constructorOp = null;
    for (TypedOperation op : outerActual) {
      if (op.isConstructorCall()) {
        constructorOp = op;
      }
    }
    assert constructorOp != null : "should find outer class constructor";

    Sequence sequence = new Sequence();
    randoop.test.ClassWithInnerClass classWithInnerClass1 = new randoop.test.ClassWithInnerClass(1);
    TypedOperation nextOp = TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, 1);
    sequence = sequence.extend(nextOp);
    sequence = sequence.extend(constructorOp, new Variable(sequence, 0));

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
    assert innerConstructorOp != null : "should find inner class constructor";
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

    assertEquals("code test", expectedCode, sequence.toCodeString());

    // TODO be more sophisticated in checking operations
  }
}
