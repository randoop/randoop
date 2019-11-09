package randoop.condition.specification;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.signature.qual.ClassGetName;

/**
 * Represents the signature of a method or constructor for an {@link OperationSpecification} so that
 * the {@code java.lang.reflect.AccessibleObject} can be loaded, which is done by
 * <!-- private, so can't use @link: -->
 * {@code SpecificationCollection.getAccessibleObject}.
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
 * <p>The {@code classname} must be the declaring class of the method. (For a constructor, the
 * {@code classname} and operation {@code name} will be identical.) If named class is not the
 * declaring class of the method, the method will not be found, and the enclosing specification will
 * not be used.
 */
public class OperationSignature {

  // NOTE: changing field names or @SerializedName annotations could affect integration with other
  // tools

  /** The fully-qualified name of the declaring class of this operation. */
  private final @ClassGetName String classname;

  /**
   * The name of this operation. For a method, is its simple name. For a constructor, is the
   * fully-qualified name of the class.
   */
  private final String name;

  /** The list of fully-qualified raw type names for the parameters of this operation. */
  private final List<@ClassGetName String> parameterTypes;

  /** Gson serialization requires a default constructor. */
  @SuppressWarnings({
    "unused",
    "signature" // dummy value in default constructor for Gson
  })
  private OperationSignature() {
    this.classname = "";
    this.name = "";
    this.parameterTypes = new ArrayList<>();
  }

  /**
   * Create an {@link OperationSignature} object given the names of the declaring class, method or
   * constructor, and parameter types.
   *
   * @param classname the fully-qualified name of the declaring class
   * @param name the name of the method or constructor
   * @param parameterTypes the list of fully-qualified raw parameter type names
   */
  private OperationSignature(
      @ClassGetName String classname, String name, List<@ClassGetName String> parameterTypes) {
    this.classname = classname;
    this.name = name;
    this.parameterTypes = parameterTypes;
  }

  /**
   * Create a {@link OperationSignature} for the constructor of the class with the parameter types.
   *
   * @param classname the fully-qualified raw name of the declaring class
   * @param simpleName the simple name of the class (and of the constructor)
   * @param parameterTypes the list of fully-qualified parameter type names
   * @return the {@link OperationSignature} for a constructor of the declaring class with the
   *     parameter types
   */
  public static OperationSignature forConstructorName(
      @ClassGetName String classname,
      String simpleName,
      List<@ClassGetName String> parameterTypes) {
    return new OperationSignature(classname, simpleName, parameterTypes);
  }

  /**
   * Create a {@link OperationSignature} for the method in the named class, with the method name and
   * parameter types.
   *
   * @param classname the name of the declaring class
   * @param name the name of the method
   * @param parameterTypes the list of fully-qualified parameter type names
   * @return the {@link OperationSignature} for the named method in the declaring class, with the
   *     parameter types
   */
  public static OperationSignature forMethodName(
      @ClassGetName String classname, String name, List<@ClassGetName String> parameterTypes) {
    return new OperationSignature(classname, name, parameterTypes);
  }

  /**
   * Returns an {@link OperationSignature} for a method given as a {@code java.lang.reflect.Method}.
   *
   * @param method the {@code Method} for which operation is to be created
   * @return the {@link OperationSignature} with the class, name and parameter types of {@code
   *     method}
   */
  public static OperationSignature of(Method method) {
    return new OperationSignature(
        method.getDeclaringClass().getName(),
        method.getName(),
        getTypeNames(method.getParameterTypes()));
  }

  /**
   * Returns an {@link OperationSignature} for a constructor given as a {@code
   * java.lang.reflect.Constructor}.
   *
   * <p>Note: the name and classname of a constructor are equal
   *
   * @param constructor the {@code Constructor} for which an operation is to be created
   * @return the {@link OperationSignature} with the class and parameter types of {@code
   *     constructor}
   */
  public static OperationSignature of(Constructor<?> constructor) {
    return new OperationSignature(
        // Class.getName returns JVML format for arrays, but this isn't an array, so the call is OK.
        constructor.getDeclaringClass().getName(),
        constructor.getName(),
        getTypeNames(constructor.getParameterTypes()));
  }

  /**
   * Returns an operation for a method or constructor given as a {@code
   * java.lang.reflect.AccessibleObject}.
   *
   * @param op the method or constructor
   * @return an {@link OperationSignature} if {@code op} is a constructor or method, null if field
   */
  public static OperationSignature of(AccessibleObject op) {
    if (op instanceof Field) {
      return null;
    } else if (op instanceof Method) {
      return of((Method) op);
    } else if (op instanceof Constructor) {
      return of((Constructor) op);
    } else {
      throw new Error("how did this happen?");
    }
  }

  /**
   * Return the name of the declaring class of this {@link OperationSignature}.
   *
   * @return the name of the declaring class of this operation
   */
  public @ClassGetName String getClassname() {
    return classname;
  }

  /**
   * Return the name of this {@link OperationSignature}.
   *
   * @return the name of this operation
   */
  public String getName() {
    return name;
  }

  /**
   * Return the list of parameter type names for this {@link OperationSignature}.
   *
   * @return the list of parameter type names for this operation
   */
  public List<@ClassGetName String> getParameterTypeNames() {
    return parameterTypes;
  }

  /**
   * Indicates whether this {@link OperationSignature} represents a constructor.
   *
   * @return {@code true} if this {@link OperationSignature} represents a constructor, {@code false}
   *     otherwise
   */
  public boolean isConstructor() {
    return name.equals(classname);
  }

  /**
   * Indicates whether this {@link OperationSignature} is a valid representation of a method or
   * constructor.
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

  /**
   * Creates a list of fully-qualified type names from the array of {@code Class<?>} objects.
   *
   * @param classes the array of {@code Class<?>} objects
   * @return the list of fully-qualified type names for the objects in {@code classes}
   */
  private static List<@ClassGetName String> getTypeNames(Class<?>[] classes) {
    List<@ClassGetName String> parameterTypes = new ArrayList<>();
    for (Class<?> aClass : classes) {
      parameterTypes.add(aClass.getName());
    }
    return parameterTypes;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof OperationSignature)) {
      return false;
    }
    OperationSignature other = (OperationSignature) object;
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
}
