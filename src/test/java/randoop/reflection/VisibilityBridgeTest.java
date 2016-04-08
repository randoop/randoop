package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import randoop.operation.ConcreteOperation;
import randoop.reflection.visibilitytest.PackageSubclass;
import randoop.types.ConcreteSimpleType;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;

/**
 * A test to ensure that reflective collection of methods includes the
 * visibility bridge, which is a bridge method that fakes the "inheritance" of
 * a public method from a package private class.
 */
public class VisibilityBridgeTest {

  //can't compare method of superclass directly to method of subclass
  //so need to convert to abstraction to allow list search
  private class FormalMethod {
    private ConcreteType returnType;
    private String name;
    private ConcreteTypeTuple parameterTypes;

    FormalMethod(Method m, ConcreteType declaringType) {
      this.returnType = new ConcreteSimpleType(m.getReturnType());
      this.name = m.getName();
      List<ConcreteType> paramTypes = new ArrayList<>();
      if (! Modifier.isStatic(m.getModifiers() & Modifier.methodModifiers())) {
        paramTypes.add(declaringType);
      }
      for (Class<?> p : m.getParameterTypes()) {
        paramTypes.add(new ConcreteSimpleType(p));
      }
      this.parameterTypes = new ConcreteTypeTuple(paramTypes);
    }

    FormalMethod(ConcreteOperation op) {
      this.returnType = op.getOutputType();
      this.parameterTypes = op.getInputTypes();
      this.name = op.getOperation().getName();
    }

    public boolean equals(FormalMethod m) {
      return this.returnType.equals(m.returnType)
              && this.name.equals(m.name)
              && this.parameterTypes.equals(m.parameterTypes);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof FormalMethod)) return false;
      FormalMethod m = (FormalMethod) obj;
      return this.equals(m);
    }

    public String getName() {
      return name;
    }
  }

  /**
   * This test is simply to ensure that reflective collection of methods
   * includes the visibility bridge, which looks like an "inherited" public
   * method of package private class.
   */
  @Test
  public void testVisibilityBridge() {
    Class<?> sub = PackageSubclass.class;
    ConcreteType declaringType = new ConcreteSimpleType(sub);

    //should only inherit public non-synthetic methods of package private superclass
    List<FormalMethod> include = new ArrayList<>();
    try {
      Class<?> sup = Class.forName("randoop.reflection.visibilitytest.PackagePrivateBase");
      for (Method m : sup.getDeclaredMethods()) {
        if (Modifier.isPublic(m.getModifiers()) && !m.isBridge() && !m.isSynthetic()) {
          include.add(new FormalMethod(m, declaringType));
        }
      }
    } catch (ClassNotFoundException e) {
      fail("test failed because unable to find base class");
    }

    Set<ConcreteOperation> actualOps = getConcreteOperations(sub);
    assertEquals(
        "expect operations count to be inherited methods plus constructor",
        include.size() + 1,
        actualOps.size());

    List<FormalMethod> actual = new ArrayList<>();
    for (ConcreteOperation op : actualOps) {
      if (op.isMethodCall()) {
        actual.add(new FormalMethod(op));
      }
    }

    for (FormalMethod m : include) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(m));
    }
  }

  private Set<ConcreteOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(c, new DefaultReflectionPredicate(), new PublicVisibilityPredicate());
  }

  private Set<ConcreteOperation> getConcreteOperations(Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    final Set<ConcreteOperation> operations = new LinkedHashSet<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        operations.add(operation);
      }
    });
    OperationExtractor extractor = new OperationExtractor(operationManager, predicate);
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.add(extractor);
    manager.apply(c);
    return operations;
  }
}
