package randoop.types;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a Java array type.
 * An array can have elements of any type.
 */
public class ArrayType extends ReferenceType {

  /** The type of elements in this array */
  private final GeneralType elementType;

  private final Class<?> runtimeClass;

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

  @Override
  public boolean isGeneric() {
    return elementType.isGeneric();
  }

  @Override
  public GeneralType apply(Substitution substitution) throws RandoopTypeException {
    GeneralType type = elementType.apply(substitution);
    if (type != null && ! type.equals(this)) {
        return new ArrayType(type, runtimeClass);
      } else {
        return this;
      }
  }

  @Override
  public GeneralType getSuperclass() {
    return ConcreteTypes.OBJECT_TYPE;
  }
}
