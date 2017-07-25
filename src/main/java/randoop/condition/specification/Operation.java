package randoop.condition.specification;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an operation for an {@link OperationSpecification} so that the {@code
 * java.lang.reflect.AccessibleObject} can be loaded.
 *
 * <p>The JSON serialization of this class is used to read the specifications for an operation given
 * using the {@code --specifications} command-line option. The JSON should include a JSON object
 * labeled by the name of each field of this class, as in
 *
 * <pre>
 *   {
 *     "classname": "net.Connection",
 *     "name": "send",
 *     "parameterTypes": [
 *       "int"
 *     ]
 *   }
 * </pre>
 *
 * <p>Note that the class name and parameter type names should be given as fully-qualified class
 * names. Generic types should be given as rawtypes.
 *
 * <p>The <code>classname</code> must be the declaring class of the method. (For a constructor, the
 * <code>classname</code> and operation <code>name</code> will be identical.) If named class is not
 * the declaring class of the method, the method will not be found, and the enclosing specification
 * will not be used.
 */
public class Operation {

  /** The fully-qualified name of the declaring class of this operation */
  private final String classname;

  /** The name of this operation */
  private final String name;

  /** The list of fully-qualified type names for the parameters of this operation */
  private final List<String> parameterTypes;

  /** A default constructor is expected for Gson serialization. */
  @SuppressWarnings("unused")
  private Operation() {
    this.classname = "";
    this.name = "";
    this.parameterTypes = new ArrayList<>();
  }

  /**
   * Create an {@link Operation} object given the names of the declaring class, method or
   * constructor, the parameter types.
   *
   * @param classname the fully-qualified name of the declaring class
   * @param name the name of the method or constructor
   * @param parameterTypes the list of fully-qualified parameter type names
   */
  private Operation(String classname, String name, List<String> parameterTypes) {
    this.classname = classname;
    this.name = name;
    this.parameterTypes = parameterTypes;
  }

  /**
   * Create a {@link Operation} for the constructor of the class with the parameter types. The name
   * of the constructor is the same as the class name.
   *
   * @param classname the fully-qualified name of the declaring class
   * @param parameterTypes the list of fully-qualified parameter type names
   * @return the {@link Operation} for a constructor of the declaring class with the parameter
   *     types.
   */
  public static Operation forConstructorName(String classname, List<String> parameterTypes) {
    return new Operation(classname, classname, parameterTypes);
  }

  /**
   * Create a {@link Operation} for the method in the named class, with the method name and
   * parameter types.
   *
   * @param classname the name of the declaring class
   * @param name the name of the method
   * @param parameterTypes the list of fully-qualified parameter type names
   * @return the {@link Operation} for the named method in the declaring class, with the parameter
   *     types
   */
  public static Operation forMethodName(
      String classname, String name, List<String> parameterTypes) {
    return new Operation(classname, name, parameterTypes);
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
      classname = m.getDeclaringClass().getName();
      name = m.getName();
      parameterTypes = Operation.getTypeNames(m.getParameterTypes());
    } else if (op instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) op;
      classname = constructor.getDeclaringClass().getName();
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

  @Override
  public String toString() {
    return String.format(
        "{%n \"classname\": \"%s\",%n \"name\": \"%s\",%n \"parameterTypes\": \"%s\"%n}",
        classname, name, parameterTypes);
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
      parameterTypes.add(aClass.getName());
    }
    return parameterTypes;
  }

  /**
   * Return the name of the declaring class of this {@link Operation}.
   *
   * @return the name of the declaring class of this operation
   */
  public String getClassname() {
    return classname;
  }

  /**
   * Return the name of this {@link Operation}.
   *
   * @return the name of this operation
   */
  public String getName() {
    return name;
  }

  /**
   * Return the list of parameter type names for this {@link Operation}.
   *
   * @return the list of parameter type names for this operation
   */
  public List<String> getParameterTypeNames() {
    return parameterTypes;
  }

  /**
   * Indicates whether this {@link Operation} represents a constructor.
   *
   * @return {@code true} if this {@link Operation} represents a constructor, {@code false}
   *     otherwise
   */
  public boolean isConstructor() {
    return name.equals(classname);
  }

  /**
   * Indicates whether this {@link Operation} is a valid representation of a method or constructor.
   *
   * @return {@code true} if the class and operation names are both non-null, non-empty and the type
   *     name list is non-null.
   */
  public boolean isValid() {
    return classname != null
        && !classname.isEmpty()
        && name != null
        && !name.isEmpty()
        && parameterTypes != null;
  }
}
