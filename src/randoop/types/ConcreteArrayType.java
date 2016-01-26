package randoop.types;

import java.lang.reflect.Array;
import java.util.Objects;

/**
 * Represents an array type where the element type is a concrete type.
 */
public class ConcreteArrayType extends ConcreteType {

  /** The (concrete) element type of this array type. */
  private ConcreteType elementType;
  
  /** The runtime type of this array type. */
  private Class<? extends Object> runtimeType;

  /**
   * Creates a concrete array type for the given element type.
   * 
   * @param elementType  the element type for the array
   */
  public ConcreteArrayType(ConcreteType elementType) {
    if (elementType == null) {
      throw new IllegalArgumentException("Element type should be non-null");
    }
    this.elementType = elementType;
    this.runtimeType = Array.newInstance(elementType.getRuntimeClass(), 0).getClass();
  }

  /**
   * (Package private) Creates a concrete array type from the given {@code Class}
   * representing an array type.
   * 
   * @param runtimeType  the runtime 
   */
  ConcreteArrayType(Class<?> runtimeType) {
    if (runtimeType == null) {
      throw new IllegalArgumentException("runtime class may not be null");
    }
    if (! runtimeType.isArray()) {
      throw new IllegalArgumentException("Class must be array");
    }
    this.elementType = ConcreteType.forClass(runtimeType.getComponentType());
    this.runtimeType = runtimeType;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof ConcreteArrayType)) {
      return false;
    }
    ConcreteArrayType t = (ConcreteArrayType)obj;
    return this.elementType.equals(t.elementType);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(elementType, runtimeType);
  }
  
  /**
   * {@inheritDoc}
   * @return true since this is an array
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
  public boolean isAssignableFrom(ConcreteType sourceType) {
    if (! sourceType.isArray()) {
      return false;
    }
    // if both element types are parameterized, then must be identical
    ConcreteArrayType t = (ConcreteArrayType)sourceType;
    if (this.elementType.isParameterized()
        && t.elementType.isParameterized()) {
      return this.elementType.equals(t.elementType);
    }
    // otherwise, check identity and widening reference conversions on 
    // runtime class
    return runtimeType.isAssignableFrom(sourceType.getRuntimeClass());
  }
 
  /**
   * {@inheritDoc}
   * @return the name of this array type
   */
  @Override
  public String toString() {
    return this.getName();
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
  public Class<?> getRuntimeClass() {
    return runtimeType;
  }
  
}
