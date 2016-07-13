package randoop.types;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Java array type as defined in JLS, Section 4.3.
 * An array may have elements of any type.
 */
public class ArrayType extends ReferenceType {

  /** The type of elements in this array */
  private final GeneralType elementType;

  /** The runtime type for this array */
  private final Class<?> runtimeClass;

  /**
   * Creates an {@code ArrayType} with the given element type and runtime class.
   *
   * @param elementType  the element type
   * @param runtimeClass  the runtime class
   */
  private ArrayType(GeneralType elementType, Class<?> runtimeClass) {
    this.elementType = elementType;
    this.runtimeClass = runtimeClass;
  }

  /**
   * {@inheritDoc}
   * @return the {@code Class} object for the array
   */
  @Override
  public Class<?> getRuntimeClass() {
    return runtimeClass;
  }

  /**
   * {@inheritDoc}
   * @return the name of this array type
   */
  @Override
  public String getName() {
    return elementType.getName() + "[]";
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

  /**
   * {@inheritDoc}
   * @return true, since this is an array
   */
  @Override
  public boolean isArray() {
    return true;
  }

  /**
   * {@inheritDoc}
   * For an array type, check for assignability by reference widening.
   * If not otherwise assignable, check for unchecked conversion:
   * occurs when this type is
   * <code>C&lt;T<sub>1</sub>,&hellip;,T<sub>k</sub>&gt;[]</code>
   * and other type is
   * <code>C[]</code> (e.g., the element type is the rawtype <code>C</code>).
   */
  @Override
  public boolean isAssignableFrom(GeneralType otherType) {
    if (super.isAssignableFrom(otherType)) {
      return true;
    }

    if (otherType.isArray() && this.elementType.isParameterized()) {
      GeneralType otherElementType = ((ArrayType) otherType).elementType;
      return otherElementType.isRawtype()
          && otherElementType.hasRuntimeClass(this.elementType.getRuntimeClass());
    }

    return false;
  }

  /**
   * {@inheritDoc}
   * @return true if the element type is generic, false otherwise
   */
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
  public boolean isSubtypeOf(GeneralType otherType) {
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

  @Override
  public ArrayType apply(Substitution<ReferenceType> substitution) {
    GeneralType type = elementType.apply(substitution);
    if (type != null && !type.equals(this)) {
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
  public GeneralType getElementType() {
    return elementType;
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

    GeneralType elementType = GeneralType.forClass(arrayClass.getComponentType());
    return new ArrayType(elementType, arrayClass);
  }

  /**
   * Creates an {@code ArrayType} from a {@code java.lang.reflect.Type} reference.
   * First checks whether reference has type {@code java.lang.reflectGenericArrayType},
   * and if so performs the conversion.
   * If the reference is to a {@code Class} object, then delegates to {@link #forClass(Class)}.
   *
   * @param type  the {@link java.lang.reflect.Type} reference
   * @return the {@code GeneralType} for the array type
   */
  public static ArrayType forType(Type type) {
    if (type instanceof java.lang.reflect.GenericArrayType) {
      java.lang.reflect.GenericArrayType arrayType = (java.lang.reflect.GenericArrayType) type;
      GeneralType elementType = GeneralType.forType(arrayType.getGenericComponentType());
      return ArrayType.ofElementType(elementType);
    }

    if (type instanceof Class<?>) {
      return ArrayType.forClass((Class<?>) type);
    }

    throw new IllegalArgumentException("type must be an array type");
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return elementType.getTypeParameters();
  }

  /**
   * Creates an {@code ArrayType} for the given element type.
   * If the element type is a type variable then creates a type with an {@link Object} array as the
   * rawtype.
   *
   * @param elementType  the element type
   * @return an {@code ArrayType} with the given element type
   */
  public static ArrayType ofElementType(GeneralType elementType) {
    if (elementType instanceof TypeVariable) {
      return new ArrayType(elementType, Array.newInstance(Object.class, 0).getClass());
    }
    return new ArrayType(
        elementType, Array.newInstance(elementType.getRuntimeClass(), 0).getClass());
  }
}
