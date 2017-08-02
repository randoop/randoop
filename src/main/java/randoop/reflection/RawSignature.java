package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import plume.UtilMDE;

/**
 * Represents the raw type signature for an {@code java.lang.reflect.AccessibleObject}. This
 * signature consists of the classname as a fully-qualified raw type, the method name, and the
 * argument types as fully-qualified raw types.
 *
 * <p>The raw type signature for a constructor <code>C()</code> is <code>C()</code> instead of the
 * reflection form <code>C.&lt;init&gt;</code>. Also, the name and the classname of a constructor
 * are the same.
 */
public class RawSignature {

  /** The package name of the class */
  private final String packageName;

  /** The name of the declaring class of the method */
  private final String classname;

  /** The method name */
  private final String name;

  /** The method parameter types */
  private final Class<?>[] parameterTypes;

  /**
   * Create a {@link RawSignature} object with the name and parameterTypes.
   *
   * @param packageName the package name of the class
   * @param classname the name of the class
   * @param name the method name
   * @param parameterTypes the method parameter types
   */
  public RawSignature(
      String packageName, String classname, String name, Class<?>[] parameterTypes) {
    this.packageName = packageName;
    this.classname = classname;
    this.name = name;
    this.parameterTypes = parameterTypes;
  }

  /**
   * Create a {@link RawSignature} object from the {@code java.lang.reflect.Method}.
   *
   * @param method the method from which to extract the signature
   * @return the {@link RawSignature} object for {@code method}
   */
  public static RawSignature of(Method method) {
    Package classPackage = method.getDeclaringClass().getPackage();
    String packageName = (classPackage != null) ? classPackage.getName() : "";
    String classname = method.getDeclaringClass().getName().substring(packageName.length() + 1);

    return new RawSignature(packageName, classname, method.getName(), method.getParameterTypes());
  }

  /**
   * Create a {@link RawSignature} object from the {@code java.lang.reflect.Constructor}.
   *
   * @param constructor the constructor from which signature is extracted
   * @return the {@link RawSignature} object for {@code constructor}
   */
  public static RawSignature of(Constructor<?> constructor) {
    Package classPackage = constructor.getDeclaringClass().getPackage();
    String packageName = (classPackage != null) ? classPackage.getName() : "";
    String classname =
        constructor.getDeclaringClass().getName().substring(packageName.length() + 1);
    String name = constructor.getName().substring(packageName.length() + 1);

    return new RawSignature(packageName, classname, name, constructor.getParameterTypes());
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof RawSignature)) {
      return false;
    }
    RawSignature signature = (RawSignature) object;
    if (!this.classname.equals(signature.classname)) {
      return false;
    }
    if (!this.name.equals(signature.name)) {
      return false;
    }
    if (this.parameterTypes.length != signature.parameterTypes.length) {
      return false;
    }
    for (int i = 0; i < parameterTypes.length; i++) {
      if (!this.parameterTypes[i].equals(signature.parameterTypes[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(classname, name, Arrays.hashCode(parameterTypes));
  }

  /** {@inheritDoc} */
  // TODO: reinstate this Javadoc comment text, with correct link.
  // Commented out because the Javadoc error is breaking the build.
  // * <p>Returns the string representation of this signature in the format read by {@link
  // * SignatureParser#parse(String, VisibilityPredicate, ReflectionPredicate)}.
  @Override
  public String toString() {
    List<String> typeNames = new ArrayList<>();
    for (Class<?> type : parameterTypes) {
      typeNames.add(type.getCanonicalName());
    }

    return ((packageName.isEmpty()) ? "" : packageName + ".")
        + ((classname.equals(name)) ? name : classname + "." + name)
        + "("
        + UtilMDE.join(typeNames, ",")
        + ")";
  }

  /**
   * Return package name for method in this signature.
   *
   * @return the package name for this signature
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Return the class name for method in this signature.
   *
   * @return the class name for this signature
   */
  public String getClassname() {
    return classname;
  }

  /**
   * Return the method name for this signature.
   *
   * @return the method name for this signature
   */
  public String getName() {
    return name;
  }

  /**
   * Construct a parameter declaration string using the parameter names. This string contains
   * type-parameter name pairs in the format needed for a method declaration and wrapped in
   * parentheses.
   *
   * @param parameterNames the parameter names to use to create declaration, length should be the
   *     same as the number of parameter types in this signature.
   * @return the parameter declarations for this signature using the given parameter names
   */
  public String getDeclarationArguments(List<String> parameterNames) {
    if (parameterNames.size() != parameterTypes.length) {
      throw new IllegalArgumentException(
          "Number of parameter names must match the number of parameter types");
    }

    List<String> paramDeclarations = new ArrayList<>();
    for (int i = 0; i < parameterTypes.length; i++) {
      paramDeclarations.add(parameterTypes[i].getCanonicalName() + " " + parameterNames.get(i));
    }
    return "(" + UtilMDE.join(paramDeclarations, ", ") + ")";
  }

  /**
   * Return the array of parameter types for this signature.
   *
   * @return the array of parameter types for this signature
   */
  public Class<?>[] getParameterTypes() {
    return parameterTypes;
  }
}
