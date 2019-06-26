package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.signature.qual.DotSeparatedIdentifiers;
import org.plumelib.util.UtilPlume;

/**
 * Represents the raw type signature for an {@code java.lang.reflect.AccessibleObject}. This
 * signature consists of the classname as a fully-qualified raw type, the method name, and the
 * argument types as fully-qualified raw types. It does not include the method or constructor name.
 *
 * <p>The raw type signature for a constructor {@code C()} is {@code C()} instead of the reflection
 * form {@code C.<init>()}. Also, the name and the classname of a constructor are the same.
 */
public class RawSignature {

  /** The package name of the class; null for the unnamed package. */
  private final @DotSeparatedIdentifiers String packageName;

  /** The name of the declaring class of the method. */
  private final String classname;

  /** The method name; for a constructor, same as the classname. */
  private final String name;

  /** The method parameter types. */
  private final Class<?>[] parameterTypes;

  /**
   * Create a {@link RawSignature} object with the name and parameterTypes.
   *
   * @param packageName the package name of the class. An unnamed package is indicated by a null in
   *     Java 8 and an emptry string in Java 9.
   * @param classname the name of the class
   * @param name the method name; for a constructor, same as the classname
   * @param parameterTypes the method parameter types, including the receiver type if any
   */
  public RawSignature(
      @DotSeparatedIdentifiers String packageName,
      String classname,
      String name,
      Class<?>[] parameterTypes) {
    this.packageName = packageName;
    this.classname = classname;
    this.name = name;
    this.parameterTypes = parameterTypes;
  }

  /**
   * Create a {@link RawSignature} object from the {@code java.lang.reflect.Method}.
   *
   * @param executable the method from which to extract the signature
   * @return the {@link RawSignature} object for {@code executable}
   */
  public static RawSignature of(Method executable) {
    Package classPackage = executable.getDeclaringClass().getPackage();
    String packageName = (classPackage == null) ? null : classPackage.getName();
    String fullclassname = executable.getDeclaringClass().getName();
    String classname =
        (packageName == null) ? fullclassname : fullclassname.substring(packageName.length() + 1);

    return new RawSignature(
        packageName, classname, executable.getName(), executable.getParameterTypes());
  }

  /**
   * Create a {@link RawSignature} object from the {@code java.lang.reflect.Constructor}.
   *
   * @param executable the constructor from which signature is extracted
   * @return the {@link RawSignature} object for {@code executable}
   */
  public static RawSignature of(Constructor<?> executable) {
    Package classPackage = executable.getDeclaringClass().getPackage();
    String packageName = (classPackage == null) ? null : classPackage.getName();
    String fullclassname = executable.getDeclaringClass().getName();
    String classname =
        (packageName == null) ? fullclassname : fullclassname.substring(packageName.length() + 1);
    String fullname = executable.getName();
    String name = (packageName == null) ? fullname : fullname.substring(packageName.length() + 1);

    return new RawSignature(packageName, classname, name, executable.getParameterTypes());
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof RawSignature)) {
      return false;
    }
    RawSignature that = (RawSignature) object;
    return Objects.equals(this.packageName, that.packageName)
        && this.classname.equals(that.classname)
        && this.name.equals(that.name)
        && Arrays.equals(this.parameterTypes, that.parameterTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(classname, name, Arrays.hashCode(parameterTypes));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the string representation of this signature in the format read by {@link
   * SignatureParser#parse(String, VisibilityPredicate, ReflectionPredicate)}.
   */
  @Override
  public String toString() {
    List<String> typeNames = new ArrayList<>();
    for (Class<?> type : parameterTypes) {
      typeNames.add(type.getCanonicalName());
    }

    return ((packageName == null) ? "" : packageName + ".")
        + (classname.equals(name) ? name : classname + "." + name)
        + "("
        + UtilPlume.join(typeNames, ",")
        + ")";
  }

  /**
   * Return package name for method in this signature.
   *
   * @return the package name for this signature, null if default package
   */
  public @DotSeparatedIdentifiers String getPackageName() {
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
   *     same as the number of parameter types in this signature
   * @return the parameter declarations for this signature using the given parameter names
   */
  public String getDeclarationArguments(List<String> parameterNames) {
    if (parameterNames.size() != parameterTypes.length) {
      throw new IllegalArgumentException(
          String.format(
              "Number of parameter names %d (%s) must match the number of parameter types %d (%s) for %s",
              parameterNames.size(),
              parameterNames,
              parameterTypes.length,
              Arrays.toString(parameterTypes),
              this));
    }

    List<String> paramDeclarations = new ArrayList<>();
    for (int i = 0; i < parameterTypes.length; i++) {
      paramDeclarations.add(parameterTypes[i].getCanonicalName() + " " + parameterNames.get(i));
    }
    return "(" + UtilPlume.join(paramDeclarations, ", ") + ")";
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
