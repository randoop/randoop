package randoop.reflection;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.operation.TypedOperation;
import randoop.reflection.visibilitytest.PackageSubclass;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GeneralType;
import randoop.types.SimpleClassOrInterfaceType;
import randoop.types.TypeTuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test to ensure that reflective collection of methods includes the
 * visibility bridge, which is a bridge method that fakes the "inheritance" of
 * a public method from a package private class.
 */
public class VisibilityBridgeTest {

  //can't compare method of superclass directly to method of subclass
  //so need to convert to abstraction to allow list search
  private class FormalMethod {
    private GeneralType returnType;
    private String name;
    private TypeTuple parameterTypes;

    FormalMethod(Method m, ClassOrInterfaceType declaringType) {
      this.returnType = GeneralType.forClass(m.getReturnType());
      this.name = m.getName();
      List<GeneralType> paramTypes = new ArrayList<>();
      if (!Modifier.isStatic(m.getModifiers() & Modifier.methodModifiers())) {
        paramTypes.add(declaringType);
      }
      for (Class<?> p : m.getParameterTypes()) {
        paramTypes.add(new SimpleClassOrInterfaceType(p));
      }
      this.parameterTypes = new TypeTuple(paramTypes);
    }

    FormalMethod(TypedOperation op) {
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

    @Override
    public String toString() {
      return name + " : " + parameterTypes + " -> " + returnType;
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
    ClassOrInterfaceType declaringType = new SimpleClassOrInterfaceType(sub);

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

    Set<TypedOperation> actualOps = getConcreteOperations(sub);
    assertEquals(
        "expect operations count to be inherited methods plus constructor",
        include.size() + 1,
        actualOps.size());

    List<FormalMethod> actual = new ArrayList<>();
    for (TypedOperation op : actualOps) {
      if (op.isMethodCall()) {
        actual.add(new FormalMethod(op));
      }
    }

    for (FormalMethod m : include) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(m));
    }
  }

  private Set<TypedOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(
        c, new DefaultReflectionPredicate(), new PublicVisibilityPredicate());
  }

  private Set<TypedOperation> getConcreteOperations(
      Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    OperationExtractor extractor = new OperationExtractor(classType, operations, predicate);
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.add(extractor);
    manager.apply(c);
    return operations;
  }
}
