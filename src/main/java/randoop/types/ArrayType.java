package randoop.types;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Represents a Java array type as defined in JLS, Section 4.3.
 * An array may have elements of any type.
 */
public class ArrayType extends ReferenceType {

  /** The type of elements in this array */
  private final GeneralType elementType;

  private final Class<?> runtimeClass;

  /**
   * Creates an {@code ArrayType} with the given element type and runtime class.
   *
   * @param elementType  the element type
   * @param runtimeClass  the runtime class
   */
  public ArrayType(GeneralType elementType, Class<?> runtimeClass) {
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
    return elementType.equals(t.elementType)
            && runtimeClass.equals(t.runtimeClass);
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
   * An array is assignable from an array of same element type, or by an array
   * of raw type if element type is parameterized.
   */
  @Override
  public boolean isAssignableFrom(GeneralType sourceType) {
    if (!sourceType.isArray()) {
      return false;
    }
    // if both element types are parameterized, then must be identical
    ArrayType t = (ArrayType) sourceType;
    if (this.elementType.isParameterized() && t.elementType.isParameterized()) {
      return this.elementType.equals(t.elementType);
    }
    // otherwise, check identity and widening reference conversions on
    // runtime class
    return runtimeClass.isAssignableFrom(sourceType.getRuntimeClass());
  }

  /**
   * {@inheritDoc}
   * @return true if the element type is generic, false otherwise
   */
  @Override
  public boolean isGeneric() {
    return elementType.isGeneric();
  }

  @Override
  public GeneralType apply(Substitution substitution) {
    GeneralType type = elementType.apply(substitution);
    if (type != null && ! type.equals(this)) {
        return new ArrayType(type, runtimeClass);
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
    if (! arrayClass.isArray()) {
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
      java.lang.reflect.GenericArrayType arrayType = (java.lang.reflect.GenericArrayType)type;
      GeneralType elementType = GeneralType.forType(arrayType.getGenericComponentType());
      return ArrayType.ofElementType(elementType);
    }

    if (type instanceof Class<?>) {
      return ArrayType.forClass((Class<?>)type);
    }

    throw new IllegalArgumentException("type must be an array type");
  }

  /**
   * Creates an {@code ArrayType} for the given element type.
   *
   * @param elementType  the element type
   * @return an {@code ArrayType} with the given element type
   */
  public static ArrayType ofElementType(GeneralType elementType) {
    return new ArrayType(elementType, Array.newInstance(elementType.getRuntimeClass(), 0).getClass());
  }
}
