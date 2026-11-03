package randoop.types;

import java.util.List;

/**
 * A lazy representation of a type bound in which a type variable occurs. Similar in purpose to
 * {@link LazyParameterBound}, but this class uses {@link ReferenceType} as the bound instead of
 * {@code java.lang.reflect.Type}. Its methods that test bounds apply a substitution to the bound
 * type before evaluating it.
 *
 * <p>Objects of this class are created by {@link ReferenceBound#forBoundType(ReferenceType)} when
 * the bound type contains a type variable.
 */
class LazyReferenceBound extends ReferenceBound {

  /**
   * Creates a bound for the given reference type.
   *
   * @param boundType the bound type
   */
  LazyReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public ReferenceBound substitute(Substitution substitution) {
    if (substitution.isEmpty()) {
      return this;
    }
    ReferenceType referenceType = getBoundType().substitute(substitution);
    if (referenceType.equals(getBoundType())) {
      return this;
    }
    return ReferenceBound.forBoundType(referenceType);
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return getBoundType().getTypeParameters();
  }

  @Override
  public boolean isLowerBound(Type argType, Substitution substitution) {
    ReferenceBound b = this.substitute(substitution);
    return !this.equals(b) && b.isLowerBound(argType, substitution);
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(ParameterBound bound) {
    if (bound instanceof ReferenceBound) {
      return getBoundType().isSubtypeOfOrEqualTo(((ReferenceBound) bound).getBoundType());
    }
    assert false : "not handling LazyReferenceBound subtype of other bound type";
    return false;
  }

  @Override
  public boolean isUpperBound(Type argType, Substitution substitution) {
    ReferenceBound b = this.substitute(substitution);
    return !this.equals(b) && b.isUpperBound(argType, substitution);
  }

  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    ReferenceBound b = this.substitute(substitution);
    return !this.equals(b) && b.isUpperBound(bound, substitution);
  }
}
