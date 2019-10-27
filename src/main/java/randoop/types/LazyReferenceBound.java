package randoop.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A lazy representation of a type bound in which a type variable occurs. Similar in purpose to
 * {@link LazyParameterBound}, but this class uses {@link ReferenceType} as the bound instead of
 * {@code java.lang.reflect.Type}. Also, prevents access to recursive type bounds, that would
 * otherwise result in nonterminating calls to {@link #getTypeParameters()}.
 *
 * <p>Objects of this class are created by {@link LazyParameterBound#substitute(Substitution)} when
 * the substitution would replace a type variable with another type variable.
 */
class LazyReferenceBound extends ReferenceBound {

  LazyReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  /**
   * {@inheritDoc}
   *
   * <p>{@link LazyReferenceBound} can be part of a recursive type, and so the hash code is based on
   * the string representation of the bound to avoid recursive calls on {@code hashCode()}.
   *
   * @return the hashCode for the string representation of this bound
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.toString());
  }

  @Override
  public ReferenceBound substitute(Substitution substitution) {
    // if the substitution has no effect on this bound just return this
    if (substitution.isEmpty()) {
      return this;
    }
    for (TypeVariable parameter : getTypeParameters()) {
      if (substitution.get(parameter) == null) {
        return this;
      }
    }

    ReferenceType referenceType = getBoundType().substitute(substitution);

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
    } else if (getBoundType().isParameterized()) {
      for (ReferenceType argType : ((InstantiatedType) getBoundType()).getReferenceArguments()) {
        if (argType.isVariable()) {
          parameters.add((TypeVariable) argType);
        }
      }
    }
    return parameters;
  }

  @Override
  public boolean isLowerBound(Type argType, Substitution substitution) {
    ReferenceBound b = this.substitute(substitution);
    return !this.equals(b) && b.isLowerBound(argType, substitution);
  }

  @Override
  public boolean isSubtypeOf(ParameterBound boundType) {
    assert false : "subtype not implemented for LazyReferenceBound";
    return false;
  }

  @Override
  public boolean isUpperBound(Type argType, Substitution substitution) {
    ReferenceBound b = this.substitute(substitution);
    return !this.equals(b) && b.isUpperBound(argType, substitution);
  }

  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    assert false : "isUpperBound(ParameterBound, Substitution) not implemented";
    return false;
  }
}
