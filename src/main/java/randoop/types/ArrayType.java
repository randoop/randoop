package randoop.types;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an array type as defined in JLS, Section 4.3.
 * <pre>
 *   ArrayType:
 *     PrimitiveType [ ] { [ ] }
 *     ClassOrInterfaceType [ ] { [ ] }
 *     TypeVariable [ ] { [ ] }
 * </pre>
 * An array may have elements of any type.
 */
public class ArrayType extends ReferenceType {

  /** The type of elements in this array */
  private final Type elementType;

  /** The runtime type for this array */
  private final Class<?> runtimeClass;

  /**
   * Creates an {@code ArrayType} with the given element type and runtime class.
   *
   * @param elementType  the element type
   * @param runtimeClass  the runtime class
   */
  private ArrayType(Type elementType, Class<?> runtimeClass) {
    this.elementType = elementType;
    this.runtimeClass = runtimeClass;
  }

  /**
   * Creates an array type for the given {@code java.lang.reflect.Class} object.
   *
   * @param arrayClass  the {@code Class} object for array type
   * @return the {@code ArrayType} for the given class object
   */
  public static ArrayType forClass(Class<?> arrayClass) {
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("type must be an array");
    }

    Type elementType = Type.forClass(arrayClass.getComponentType());
    return new ArrayType(elementType, arrayClass);
  }

  /**
   * Creates an {@code ArrayType} from a {@code java.lang.reflect.Type} reference.
   * First checks whether reference has type {@code java.lang.reflectGenericArrayType},
   * and if so performs the conversion.
   * If the reference is to a {@code Class} object, then delegates to {@link #forClass(Class)}.
   *
   * @param type  the {@link java.lang.reflect.Type} reference
   * @return the {@code Type} for the array type
   */
  public static ArrayType forType(java.lang.reflect.Type type) {
    if (type instanceof java.lang.reflect.GenericArrayType) {
      java.lang.reflect.GenericArrayType arrayType = (java.lang.reflect.GenericArrayType) type;
      Type elementType = Type.forType(arrayType.getGenericComponentType());
      return ArrayType.ofElementType(elementType);
    }

    if (type instanceof Class<?>) {
      return ArrayType.forClass((Class<?>) type);
    }

    throw new IllegalArgumentException("type must be an array type");
  }

  /**
   * Creates an {@code ArrayType} for the given element type.
   * If the element type is a type variable then creates a type with an {@link Object} array as the
   * rawtype.
   *
   * @param elementType  the element type
   * @return an {@code ArrayType} with the given element type
   */
  public static ArrayType ofElementType(Type elementType) {
    if (elementType instanceof TypeVariable) {
      return new ArrayType(elementType, Array.newInstance(Object.class, 0).getClass());
    }
    return new ArrayType(
        elementType, Array.newInstance(elementType.getRuntimeClass(), 0).getClass());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ArrayType)) {
      return false;
    }
    ArrayType t = (ArrayType) obj;
    return elementType.equals(t.elementType) && runtimeClass.equals(t.runtimeClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(elementType, runtimeClass);
  }

  @Override
  public String toString() {
    return elementType + "[]";
  }

  @Override
  public ArrayType apply(Substitution<ReferenceType> substitution) {
    Type type = elementType.apply(substitution);
    if (!type.equals(this)) {
      return ArrayType.ofElementType(type);
    } else {
      return this;
    }
  }

  /**
   * Returns the element type of this array type.
   *
   * @return the element type of this array type
   */
  public Type getElementType() {
    return elementType;
  }

  @Override
  public String getName() {
    return elementType.getName() + "[]";
  }

  @Override
  public Class<?> getRuntimeClass() {
    return runtimeClass;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    if (elementType.isReferenceType()) {
      return ((ReferenceType) elementType).getTypeParameters();
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
   * For an array type, check for assignability by reference widening.
   * If not otherwise assignable, check for unchecked conversion, which
   * occurs when this type is
   * <code>C&lt;T<sub>1</sub>,&hellip;,T<sub>k</sub>&gt;[]</code>
   * and other type is
   * <code>C[]</code> (e.g., the element type is the rawtype <code>C</code>).
   */
  @Override
  public boolean isAssignableFrom(Type otherType) {
    if (super.isAssignableFrom(otherType)) {
      return true;
    }

    if (otherType.isArray() && this.elementType.isParameterized()) {
      Type otherElementType = ((ArrayType) otherType).elementType;
      return otherElementType.isRawtype()
          && otherElementType.hasRuntimeClass(this.elementType.getRuntimeClass());
    }

    return false;
  }

  @Override
  public boolean isGeneric() {
    return elementType.isGeneric();
  }

  /**
   * {@inheritDoc}
   * This method specifically uses the definition in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10.3">section 4.10.2 of JLS for JavaSE 8</a>.
   */
  @Override
  public boolean isSubtypeOf(Type otherType) {
    if (super.isSubtypeOf(otherType)) {
      return true;
    }

    if (otherType.equals(ConcreteTypes.CLONEABLE_TYPE)) {
      return true;
    }

    if (otherType.equals(ConcreteTypes.SERIALIZABLE_TYPE)) {
      return true;
    }

    if (otherType.isArray() && elementType.isReferenceType()) {
      ArrayType otherArrayType = (ArrayType) otherType;
      return otherArrayType.elementType.isReferenceType()
          && this.elementType.isSubtypeOf(otherArrayType.elementType);
    }

    return false;
  }

  /**
   * Indicate whether this type has a wildcard either as or in a type argument.
   *
   * @return true if this type has a wildcard, and false otherwise
   */
  public boolean hasWildcard() {
    return false;
  }
}
