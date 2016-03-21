package randoop.types;

import java.util.Objects;

/**
 * Represents a concrete type that occurs as a bound on a type parameter in a
 * class, interface, method or constructor (see JLS 7 section 4.4).
 */
public class ConcreteTypeBound extends TypeBound {

  /** the type of the bound */
  private ConcreteType boundType;

  /**
   * Constructs a concrete type bound from a particular concrete type.
   *
   * @param boundType  the type for the bound
   */
  public ConcreteTypeBound(ConcreteType boundType) {
    this.boundType = boundType;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ConcreteTypeBound)) {
      return false;
    }
    ConcreteTypeBound b = (ConcreteTypeBound) obj;
    return this.boundType.equals(b.boundType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(boundType);
  }

  @Override
  public String toString() {
    return boundType.toString();
  }

  /**
   * {@inheritDoc}
   * @return true if this concrete type is assignable by argument type, false otherwise
   */
  @Override
  public boolean isSatisfiedBy(ConcreteType argType, Substitution substitution) {
    return boundType.isAssignableFrom(argType);
  }

  @Override
  public Class<?> getRuntimeClass() {
    return boundType.getRuntimeClass();
  }
}
