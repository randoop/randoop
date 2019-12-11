package randoop.types;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an array type as defined in JLS, Section 4.3.
 *
 * <pre>
 *   ArrayType:
 *     PrimitiveType [ ] { [ ] }
 *     ClassOrInterfaceType [ ] { [ ] }
 *     TypeVariable [ ] { [ ] }
 * </pre>
 *
 * The type preceding the rightmost set of brackets is the <i>component</i> type, while the type
 * preceding the brackets is the <i>element</i> type. An array may have components of any type.
 */
public class ArrayType extends ReferenceType {

  /** The type of components in this array. */
  private final Type componentType;

  /** The runtime type for this array. */
  private final Class<?> runtimeClass;

  /**
   * Creates an {@code ArrayType} with the given component type and runtime class.
   *
   * @param componentType the component type
   * @param runtimeClass the runtime class
   */
  private ArrayType(Type componentType, Class<?> runtimeClass) {
    this.componentType = componentType;
    this.runtimeClass = runtimeClass;
  }

  /**
   * Creates an array type for the given {@code java.lang.reflect.Class} object.
   *
   * @param arrayClass the {@code Class} object for array type
   * @return the {@code ArrayType} for the given class object
   */
  public static ArrayType forClass(Class<?> arrayClass) {
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("type must be an array");
    }

    Type componentType = Type.forClass(arrayClass.getComponentType());
    return new ArrayType(componentType, arrayClass);
  }

  /**
   * Creates an {@code ArrayType} from a {@code java.lang.reflect.Type} reference. First checks
   * whether reference has type {@code java.lang.reflectGenericArrayType}, and if so performs the
   * conversion. If the reference is to a {@code Class} object, then delegates to {@link
   * #forClass(Class)}.
   *
   * @param type the {@link java.lang.reflect.Type} reference
   * @return the {@code Type} for the array type
   */
  public static ArrayType forType(java.lang.reflect.Type type) {
    if (type instanceof java.lang.reflect.GenericArrayType) {
      java.lang.reflect.GenericArrayType arrayType = (java.lang.reflect.GenericArrayType) type;
      Type componentType = Type.forType(arrayType.getGenericComponentType());
      return ArrayType.ofComponentType(componentType);
    }

    if ((type instanceof Class<?>) && ((Class<?>) type).isArray()) {
      Type componentType = Type.forType(((Class<?>) type).getComponentType());
      return ArrayType.ofComponentType(componentType);
    }

    throw new IllegalArgumentException("type " + type + " must be an array type");
  }

  /**
   * Creates an {@code ArrayType} for the given component type. If the component type is a type
   * variable then creates a type with an {@link Object} array as the rawtype.
   *
   * @param componentType the component type
   * @return an {@code ArrayType} with the given component type
   */
  public static ArrayType ofComponentType(Type componentType) {
    if (componentType instanceof TypeVariable) {
      return new ArrayType(componentType, Array.newInstance(Object.class, 0).getClass());
    }
    return new ArrayType(
        componentType, Array.newInstance(componentType.getRuntimeClass(), 0).getClass());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ArrayType)) {
      return false;
    }
    ArrayType t = (ArrayType) obj;
    return componentType.equals(t.componentType) && runtimeClass.equals(t.runtimeClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(componentType, runtimeClass);
  }

  @Override
  public String toString() {
    return componentType + "[]";
  }

  @Override
  public ArrayType substitute(Substitution substitution) {
    Type type = componentType.substitute(substitution);
    if (!type.equals(this)) {
      return ArrayType.ofComponentType(type);
    } else {
      return this;
    }
  }

  /**
   * Returns the component type of this array type.
   *
   * @return the component type of this array type
   */
  public Type getComponentType() {
    return componentType;
  }

  /**
   * Returns the element type of this array type. If the component type of this array type is not an
   * array type, the element type is the component type. Otherwise, the element type is the element
   * type of the component type.
   *
   * @return the element type of this array type
   */
  public Type getElementType() {
    if (componentType.isArray()) {
      return ((ArrayType) componentType).getElementType();
    }
    return componentType;
  }

  @Override
  public String getName() {
    return componentType.getName() + "[]";
  }

  @Override
  public String getSimpleName() {
    return componentType.getSimpleName() + "[]";
  }

  @Override
  public Class<?> getRuntimeClass() {
    return runtimeClass;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    if (componentType.isReferenceType()) {
      return ((ReferenceType) componentType).getTypeParameters();
    } else {
      return new ArrayList<>();
    }
  }

  @Override
  public boolean isArray() {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>For an array type, check for assignability by reference widening. If not otherwise
   * assignable, check for unchecked conversion, which occurs when this type is {@code
   * C<T1,...,Tk>[]} and other type is {@code C[]} (e.g., the component type is the rawtype {@code
   * C}).
   */
  @Override
  public boolean isAssignableFrom(Type otherType) {
    if (super.isAssignableFrom(otherType)) {
      return true;
    }

    if (otherType.isArray() && this.componentType.isParameterized()) {
      Type otherElementType = ((ArrayType) otherType).componentType;
      return otherElementType.isRawtype()
          && otherElementType.runtimeClassIs(this.componentType.getRuntimeClass());
    }

    return false;
  }

  @Override
  public boolean isGeneric() {
    return componentType.isGeneric();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This method specifically uses the definition in <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10.3">section 4.10.2
   * of JLS for JavaSE 8</a>.
   */
  @Override
  public boolean isSubtypeOf(Type otherType) {
    if (super.isSubtypeOf(otherType)) {
      return true;
    }

    if (otherType.equals(JavaTypes.CLONEABLE_TYPE)) {
      return true;
    }

    if (otherType.equals(JavaTypes.SERIALIZABLE_TYPE)) {
      return true;
    }

    if (otherType.isArray() && componentType.isReferenceType()) {
      ArrayType otherArrayType = (ArrayType) otherType;
      return otherArrayType.componentType.isReferenceType()
          && this.componentType.isSubtypeOf(otherArrayType.componentType);
    }

    return false;
  }

  @Override
  public Type getRawtype() {
    if (!componentType.isGeneric()) {
      return this;
    }
    return new ArrayType(componentType.getRawtype(), runtimeClass);
  }

  @Override
  public boolean hasWildcard() {
    return componentType.hasWildcard();
  }

  /**
   * Indicates whether this array type has a parameterized element type.
   *
   * @return true if the element type is parameterized; false otherwise
   */
  public boolean hasParameterizedElementType() {
    return getElementType().isParameterized();
  }

  /**
   * Returns the non-parameterized form for this array type. For instance, converts {@code
   * List<String>[]} to {@code List[]}, {@code List<String>[][]} to {@code List[][]}, and {@code
   * int[]} to {@code int[]}.
   *
   * @return the non-parameterized form of this array type
   */
  public ArrayType getRawTypeArray() {
    Type rawElementType;
    if (this.componentType.isArray()) {
      rawElementType = ((ArrayType) componentType).getRawTypeArray();
    } else if (this.componentType.isClassOrInterfaceType()) {
      rawElementType = ((ClassOrInterfaceType) componentType).getRawtype();
    } else {
      return this;
    }
    return ArrayType.ofComponentType(rawElementType);
  }

  public int getDimensions() {
    int dimensions = 1;
    if (componentType.isArray()) {
      dimensions += ((ArrayType) componentType).getDimensions();
    }
    return dimensions;
  }
}
