package randoop.types;

import java.util.ArrayList;
import java.util.List;

/**
 * A lazy representation of a type bound in which a type variable occurs.
 * Similar in purpose to {@link LazyParameterBound}, but this class uses
 * {@link ReferenceType} as the bound instead of {@code java.lang.reflect.Type}.
 * Also, prevents access to recursive type bounds, that would otherwise result in
 * nonterminating calls to {@link #getTypeParameters()}.
 * <p>
 * Objects of this class are created by {@link LazyParameterBound#apply(Substitution)}
 * when the substitution would replace a type variable with another type variable.
 */
class LazyReferenceBound extends ReferenceBound {

  LazyReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public ReferenceBound apply(Substitution<ReferenceType> substitution) {
    if (substitution.isEmpty()) {
      return this;
    }

    ReferenceType referenceType = getBoundType().apply(substitution);

    if (referenceType.equals(getBoundType())) {
      return this;
    }

    if (getBoundType().isVariable()) {
      if (referenceType.isVariable()) {
        return new LazyReferenceBound(referenceType);
      }
      return new EagerReferenceBound(referenceType);
    }

    if (getBoundType().isParameterized()) {
      // XXX technically, need to check if variable argument was replaced by variable
      // if so should return new LazyReferenceBound(referenceType)
      // But highly unlikely so for now only including code to do else case
      return new EagerReferenceBound(referenceType);
    }
    return this;
  }

  @Override
  public ReferenceBound applyCaptureConversion() {
    return null;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    List<TypeVariable> parameters = new ArrayList<>();
    if (getBoundType().isVariable()) {
      parameters.add((TypeVariable) getBoundType());
    }
    return parameters;
  }

  @Override
  public boolean isLowerBound(Type argType, Substitution<ReferenceType> substitution) {
    ReferenceBound b = this.apply(substitution);
    return b.isLowerBound(argType, substitution);
  }

  @Override
  public boolean isSubtypeOf(ParameterBound boundType) {
    assert false : "subtype not implemented for LazyReferenceBound";
    return false;
  }

  @Override
  public boolean isUpperBound(Type argType, Substitution<ReferenceType> substitution) {
    ReferenceBound b = this.apply(substitution);
    return b.isUpperBound(argType, substitution);
  }

  @Override
  boolean isUpperBound(ParameterBound bound, Substitution<ReferenceType> substitution) {
    assert false : "isUpperBound(ParameterBound, Substitution<ReferenceType>) not implemented";
    return false;
  }
}
