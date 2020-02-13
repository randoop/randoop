package randoop.reflection;

import static org.junit.Assert.assertTrue;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.Test;
import randoop.operation.TypedOperation;
import randoop.reflection.visibilitytest.PackageSubclass;
import randoop.types.ClassOrInterfaceType;
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * A test to ensure that reflective collection of methods includes the visibility bridge, which is a
 * bridge method that fakes the "inheritance" of a public method from a package private class.
 */
public class VisibilityBridgeTest {

  /**
   * Represents a method signature: its name, parameter types, and return type. This is needed
   * because we can't use equals() to compare a method of superclass directly to a method of
   * subclass. This method has an equals() method, permitting list search.
   */
  private static class MethodSignature {
    private Type returnType;
    private String name;
    private TypeTuple parameterTypes;

    MethodSignature(Method m, ClassOrInterfaceType declaringType) {
      this.returnType = Type.forClass(m.getReturnType());
      this.name = m.getName();
      List<Type> paramTypes = new ArrayList<>();
      if (!Modifier.isStatic(m.getModifiers() & Modifier.methodModifiers())) {
        paramTypes.add(declaringType);
      }
      for (Class<?> p : m.getParameterTypes()) {
        paramTypes.add(new NonParameterizedType(p));
      }
      this.parameterTypes = new TypeTuple(paramTypes);
    }

    MethodSignature(TypedOperation op) {
      this.returnType = op.getOutputType();
      this.parameterTypes = op.getInputTypes();
      this.name = op.getOperation().getName();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof MethodSignature)) {
        return false;
      }
      MethodSignature m = (MethodSignature) obj;
      return this.returnType.equals(m.returnType)
          && this.name.equals(m.name)
          && this.parameterTypes.equals(m.parameterTypes);
    }

    @Override
    public int hashCode() {
      return Objects.hash(returnType, name, parameterTypes);
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
   * This test is simply to ensure that reflective collection of methods includes the visibility
   * bridge, which looks like an "inherited" public method of package private class.
   */
  @Test
  public void testVisibilityBridge() throws ClassNotFoundException {
    Class<?> sub = PackageSubclass.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(sub);

    // should only inherit public non-synthetic methods of package private superclass
    List<MethodSignature> superclassMethods = new ArrayList<>();
    Class<?> sup = Class.forName("randoop.reflection.visibilitytest.PackagePrivateBase");
    for (Method m : sup.getDeclaredMethods()) {
      if (Modifier.isPublic(m.getModifiers()) && !m.isBridge() && !m.isSynthetic()) {
        superclassMethods.add(new MethodSignature(m, declaringType));
      }
    }

    List<MethodSignature> subclassMethods = new ArrayList<>();
    for (TypedOperation op : getConcreteOperations(sub)) {
      if (op.isMethodCall()) {
        subclassMethods.add(new MethodSignature(op));
      }
    }

    for (MethodSignature m : superclassMethods) {
      assertTrue(
          "superclass method "
              + m.getName()
              + " should occur in subclassMethods: "
              + subclassMethods,
          subclassMethods.contains(m));
    }
  }

  private Set<TypedOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(c, new DefaultReflectionPredicate(), IS_PUBLIC);
  }

  private Set<TypedOperation> getConcreteOperations(
      Class<?> c,
      ReflectionPredicate reflectionPredicate,
      VisibilityPredicate visibilityPredicate) {
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    OperationExtractor extractor =
        new OperationExtractor(classType, reflectionPredicate, visibilityPredicate);
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.add(extractor);
    manager.apply(c);
    return new LinkedHashSet<>(extractor.getOperations());
  }
}
