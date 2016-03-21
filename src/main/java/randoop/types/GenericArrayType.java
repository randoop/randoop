package randoop.types;

import java.lang.reflect.Array;
import java.util.Objects;

public class GenericArrayType extends GenericType {

  /** The (generic) element type of this array type */
  private GenericType elementType;

  /** The runtime type of this array type */
  private Class<?> runtimeType;

  /**
   * Create a generic array type for the given element type.
   *
   * @param elementType  the element type for the array
   */
  public GenericArrayType(GenericType elementType) {
    if (elementType == null) {
      throw new IllegalArgumentException("element type must be non-null");
    }

    this.elementType = elementType;
    this.runtimeType = Array.newInstance(elementType.getRuntimeClass(), 0).getClass();
  }

  /**
   * {@inheritDoc}
   * @return the {@code Class} object for the array
   */
  @Override
  public Class<?> getRuntimeClass() {
    return runtimeType;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GenericArrayType)) {
      return false;
    }
    GenericArrayType t = (GenericArrayType) obj;
    return elementType.equals(t.elementType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(elementType);
  }

  /**
   * {@inheritDoc}
   * @return a {@code ConcreteArrayType} created by instantiating the type
   * parameters of this generic array type with the type arguments
   */
  @Override
  public ConcreteType instantiate(ConcreteType... typeArguments) {
    return new ConcreteArrayType(elementType.instantiate(typeArguments));
  }

  /**
   * {@inheritDoc}
   * @return a {@code ConcreteArrayType} created by instantiating the type
   * parameters of this generic array type using the substitution
   */
  @Override
  public ConcreteType instantiate(Substitution substitution) {
    return new ConcreteArrayType(elementType.instantiate(substitution));
  }
}
