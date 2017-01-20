package randoop.input.toradocu;

import java.util.Objects;

/**
 * This is a Toradocu class borrowed to allow deserialization of JSON.
 */
public class Type {

  /**
   * Separator used in Java to separate identifiers (e.g., Foo.toString() where "." separates class
   * and method identifiers)
   */
  static final String SEPARATOR = ".";
  /** Void type that can be used to represent the void return type */
  public static final Type VOID = new Type("void");

  /** Fully qualified name of this {@code Type} */
  private final String qualifiedName;
  // The following fields are derived from qualifiedName
  /** Simple name of this {@code Type}. */
  private final String name;

  /** Flag {@code true} when this {@code Type} is an array type (e.g., java.lang.String[]). */
  private final boolean isArray;
  /** If this type is an array, componentType is the type of the contained elements. */
  private final Type componentType;

  /**
   * Creates a new {@code Type} with a given fully qualified name.
   *
   * @param qualifiedName fully qualified name of this {@code Type}
   * @throws NullPointerException if {@code qualifiedName} is null
   */
  public Type(String qualifiedName) {
    Objects.requireNonNull(qualifiedName);
    if (qualifiedName.startsWith(SEPARATOR) || qualifiedName.endsWith(SEPARATOR)) {
      throw new IllegalArgumentException(
          qualifiedName + " is not a valid fully qualified type name.");
    }

    this.qualifiedName = qualifiedName;
    if (qualifiedName.contains(SEPARATOR)) {
      name = qualifiedName.substring(qualifiedName.lastIndexOf(SEPARATOR) + 1);
    } else {
      name = qualifiedName;
    }
    isArray = name.endsWith("]");
    componentType = isArray ? new Type(qualifiedName.replaceAll("\\[\\]", "")) : null;
  }

  /**
   * Returns the fully qualified name of this {@code Type}.
   *
   * @return the fully qualified name of this {@code Type}
   */
  public String getQualifiedName() {
    return qualifiedName;
  }

  /**
   * Returns the simple name of this {@code Type}.
   *
   * @return the simple name of this {@code Type}
   */
  public String getSimpleName() {
    return name;
  }

  public String getPackageName() {
    return qualifiedName.substring(0, qualifiedName.lastIndexOf(SEPARATOR));
  }

  /**
   * Returns true if this Type represents an array type (e.g. int[], java.lang.String[][]). Returns
   * false otherwise.
   *
   * @return true if this Type is an array type, false otherwise
   */
  public boolean isArray() {
    return isArray;
  }

  /**
   * Returns the {@code Type} representing the component type of an array. If this class does not
   * represent an array class this method returns {@code null}.
   *
   * @return the {@code Type} representing the component type of this type if this type is an array
   */
  public Type getComponentType() {
    return componentType;
  }

  /**
   * Returns the dimension of this type. For example, the dimension of "Integer" is 0, the dimension
   * of "String[]" is 1, the dimension of "String[][]" is 2.
   *
   * @return the dimension of this type
   */
  public int dimension() {
    return getDimension(getQualifiedName());
  }

  /**
   * Returns the fully qualified name of this {@code Type}.
   *
   * @return the fully qualified name of this {@code Type}
   */
  @Override
  public String toString() {
    return qualifiedName;
  }

  /**
   * Returns true if this {@code Type} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Type)) return false;

    Type that = (Type) obj;
    return qualifiedName.equals(that.qualifiedName);
  }

  /**
   * Compares this type with a Java Class type.
   *
   * @param type the Class to be compared with this type
   * @return {@code true} if this type and {@code type} are equal, {@code false} otherwise
   */
  public boolean equalsTo(Class<?> type) {
    if (!isArray() && !type.isArray()) {
      /* Replacement of '$' with '.' needed for nested classes. Toradocu Type uses '.' while
       * Java reflection uses '$' as specified by the Java Language Specification. */
      return getQualifiedName().equals(type.getName().replace("$", "."));
    }
    return isArray() == type.isArray()
        && dimension() == getDimension(type.getName())
        && getComponentType().equalsTo(type.getComponentType());
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(qualifiedName);
  }

  /**
   * Returns the dimension of the given type. For example, the dimension of "Integer" is 0, the
   * dimension of "String[]" is 1, the dimension of "String[][]" is 2.
   *
   * @param type a type (e.g. "java.lang.String[][]")
   * @return the dimension of the given type
   */
  private int getDimension(String type) {
    int typeDimension = 0;
    for (char ch : type.toCharArray()) {
      if (ch == '[') {
        typeDimension++;
      }
    }
    return typeDimension;
  }
}
