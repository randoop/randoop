package randoop.condition.specification;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents the reflection type of an operation for an {@link OperationSpecification}. */
public class Operation {

  /** the fully-qualified name of the declaring class of this operation */
  private final String classname;

  /** the name of this operation */
  private final String name;

  /** the list of fully-qualified type names for the parameters of this operation */
  private final List<String> parameterTypes;

  /**
   * Create an {@link Operation} object given the names of the declaring class, method or
   * constructor, the parameter types, parameter names, receiver name and return value name.
   *
   * @param classname the fully-qualified name of the declaring class
   * @param name the name of the method or constructor
   * @param parameterTypes the list of fully-qualified parameter type names
   */
  public Operation(String classname, String name, List<String> parameterTypes) {
    this.classname = classname;
    this.name = name;
    this.parameterTypes = parameterTypes;
  }

  /**
   * Returns an operation for a method or constructor given as a {@code
   * java.lang.reflect.AccessibleObject}.
   *
   * @param op the method or constructor
   * @return an {@link Operation} if {@code op} is a constructor or method, null otherwise
   */
  public static Operation getOperation(AccessibleObject op) {
    if (op instanceof Field) {
      return null;
    }

    String classname = null;
    String name = null;
    List<String> parameterTypes = null;
    if (op instanceof Method) {
      Method m = (Method) op;
      classname = m.getDeclaringClass().getCanonicalName();
      name = m.getName();
      parameterTypes = Operation.getTypeNames(m.getParameterTypes());
    } else if (op instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) op;
      classname = constructor.getDeclaringClass().getCanonicalName();
      name = constructor.getName();
      parameterTypes = Operation.getTypeNames(constructor.getParameterTypes());
    }
    if (classname != null && name != null && parameterTypes != null) {
      return new Operation(classname, name, parameterTypes);
    }
    return null;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Operation)) {
      return false;
    }
    Operation other = (Operation) object;
    return this.classname.equals(other.classname)
        && this.name.equals(other.name)
        && this.parameterTypes.equals(other.parameterTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.classname, this.name, this.parameterTypes);
  }

  /**
   * Creates a list of fully-qualified type names from the array of {@code Class<?>} objects.
   *
   * @param classes the array of {@code Class<?>} objects
   * @return the list of fully-qualified type names for the objects in {@code classes}
   */
  private static List<String> getTypeNames(Class<?>[] classes) {
    List<String> parameterTypes = new ArrayList<>();
    for (Class<?> aClass : classes) {
      parameterTypes.add(aClass.getCanonicalName());
    }
    return parameterTypes;
  }

  public String getClassname() {
    return classname;
  }

  public String getName() {
    return name;
  }

  public List<String> getParameterTypeNames() {
    return parameterTypes;
  }

  public AccessibleObject getReflectionObject() {
    return null;
  }
}
