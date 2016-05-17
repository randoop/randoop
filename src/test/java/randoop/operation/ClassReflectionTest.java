package randoop.operation;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.test.AnIntegerPredicate;
import randoop.test.ClassWithInnerClass;
import randoop.types.ClassOrInterfaceType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by bjkeller on 4/14/16.
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
    return getConcreteOperations(c, new DefaultReflectionPredicate(), new PublicVisibilityPredicate());
  }

  private Set<TypedOperation> getConcreteOperations(Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    OperationExtractor extractor = new OperationExtractor(classType, operations, predicate);
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.apply(extractor, c);
    return operations;
  }

  @Test
  public void innerClassTest() {
    Class<?> outer = ClassWithInnerClass.class;
    Class<?> inner = null;
    try {
      inner = Class.forName("randoop.test.ClassWithInnerClass$A");
    } catch (ClassNotFoundException e) {
      fail("could not load inner class" + e.getMessage());
    }

    Set<TypedOperation> innerActual = getConcreteOperations(inner);

    for (TypedOperation op : innerActual) {
      System.out.println(op);
    }
    assertEquals("number of inner class operations", 5, innerActual.size());

    Set<TypedOperation> outerActual = getConcreteOperations(outer);
    for(TypedOperation op : outerActual) {
      System.out.println(op);
    }
    assertEquals("number of outer operations", 2, outerActual.size());

    // TODO be more sophisticated in checking operations
  }
}
