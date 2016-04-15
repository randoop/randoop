package randoop.types;

import java.util.Objects;

/**
 * Represents a concrete type that occurs as a bound on a type parameter in a
 * class, interface, method or constructor (see JLS 7 section 4.4).
 */
public class ConcreteTypeBound extends TypeBound {

  /** the type of the bound */
  private final ConcreteType boundType;

  private final TypeOrdering typeOrdering;

  /**
   * Constructs a concrete type bound from a particular concrete type using the given type order.
   *
   * @param boundType  the type for the bound
   */
  public ConcreteTypeBound(ConcreteType boundType, TypeOrdering typeOrdering) {
    this.boundType = boundType;
    this.typeOrdering = typeOrdering;
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
    return isSatisfiedBy(argType);
  }

  public ConcreteType getBoundType() {
    return boundType;
  }

  public boolean isSatisfiedBy(ConcreteType argType) {
    return typeOrdering.isLessThanOrEqualTo(boundType, argType);
  }

  @Override
  public Class<?> getRuntimeClass() {
    return boundType.getRuntimeClass();
  }

  @Override
  public TypeBound apply(Substitution substitution) {
    return this;
  }


}
