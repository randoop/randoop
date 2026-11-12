package randoop.types;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a bound on a type variable where the bound is a {@link ReferenceType}. */
public abstract class ReferenceBound extends ParameterBound {

  /** The type for this bound. */
  private final ReferenceType boundType;

  /**
   * Creates a {@link ReferenceBound} with the given bound type.
   *
   * @param boundType the {@link ReferenceType} of this bound
   */
  ReferenceBound(ReferenceType boundType) {
    super();
    this.boundType = boundType;
  }

  /**
   * Returns a bound whose type is the given type. The bound is a {@link LazyReferenceBound} if the
   * type contains a type variable, as in {@code Map<T, String>}. The bound is an {@link
   * EagerReferenceBound} otherwise. Other code also creates an {@link EagerReferenceBound} whose
   * type contains a type variable, so not every bound that contains a type variable is a {@link
   * LazyReferenceBound}.
   *
   * @param boundType a reference type
   * @return a bound whose type is {@code boundType}
   */
  static ReferenceBound forTypeLazyIfGeneric(ReferenceType boundType) {
    if (boundType.isGeneric()) {
      return new LazyReferenceBound(boundType);
    }
    return new EagerReferenceBound(boundType);
  }

  /**
   * Returns the {@link ReferenceType} bound of this type.
   *
   * @return the type for this bound
   */
  public ReferenceType getBoundType() {
    return boundType;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ReferenceBound)) {
      return false;
    }
    ReferenceBound bound = (ReferenceBound) obj;
    return this.boundType.equals(bound.boundType);
  }

  /**
   * {@inheritDoc}
   *
   * <p>A {@link LazyReferenceBound} can be part of a recursive type, through the bounds of a type
   * variable in the bound type. The hash code of a type variable does not depend on its bounds, so
   * computing the hash code of the bound type terminates. That is also consistent with {@link
   * #equals(Object)}: copies of a type variable are equal even when their bounds differ, so their
   * string representations, which include the bounds, can differ. All subclasses use this
   * implementation, so that equal bounds have equal hash codes regardless of their subclass.
   *
   * @return the hash code of the type of this bound
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(boundType);
  }

  @Override
  public String toString() {
    return boundType.toString();
  }

  @Override
  public abstract ReferenceBound substitute(Substitution substitution);

  @Override
  public boolean hasCaptureVariable() {
    return boundType.hasCaptureVariable();
  }

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    return boundType.isGeneric(ignoreWildcards);
  }

  @Override
  public boolean isObject() {
    return boundType.isObject();
  }

  @Override
  public boolean isVariable() {
    return boundType.isVariable();
  }

  /**
   * {@inheritDoc}
   *
   * <p>If {@code bound} is an {@link IntersectionTypeBound}, returns true if this is a lower bound
   * on every one of its members, since a type is a subtype of the intersection if and only if it is
   * a subtype of each member. A {@link LazyParameterBound} is compared after the substitution is
   * applied to it; a type variable in it that the substitution does not instantiate stands for an
   * unknown type. Returns false if {@code bound} is a {@link LazyParameterBound} that contains a
   * kind of type that it cannot convert to a {@link ReferenceBound}.
   */
  @Override
  boolean isLowerBound(ParameterBound bound, Substitution substitution) {
    if (bound instanceof IntersectionTypeBound) {
      for (ParameterBound member : ((IntersectionTypeBound) bound).getBounds()) {
        if (!isLowerBound(member, substitution)) {
          return false;
        }
      }
      return true;
    }
    ReferenceType type = asReferenceType(bound, substitution);
    return type != null && isLowerBound(type, substitution);
  }

  /**
   * {@inheritDoc}
   *
   * <p>If {@code bound} is an {@link IntersectionTypeBound}, returns true if this is an upper bound
   * on any of its members, since the intersection is a subtype of each member. A {@link
   * LazyParameterBound} is compared after the substitution is applied to it; a type variable in it
   * that the substitution does not instantiate stands for an unknown type. Returns false if {@code
   * bound} is a {@link LazyParameterBound} that contains a kind of type that it cannot convert to a
   * {@link ReferenceBound}.
   */
  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    if (bound instanceof IntersectionTypeBound) {
      for (ParameterBound member : ((IntersectionTypeBound) bound).getBounds()) {
        if (isUpperBound(member, substitution)) {
          return true;
        }
      }
      return false;
    }
    ReferenceType type = asReferenceType(bound, substitution);
    return type != null && isUpperBound(type, substitution);
  }

  /**
   * Returns the type of the given bound, if it is a {@link ReferenceBound}. If it is a {@link
   * LazyParameterBound}, returns its type with the given substitution applied, in which a type
   * variable that the substitution does not instantiate stands for an unknown type. Otherwise, or
   * if the {@link LazyParameterBound} cannot be converted to a {@link ReferenceBound}, returns
   * null.
   *
   * @param bound a bound
   * @param substitution a type substitution
   * @return the type of {@code bound}, or null
   */
  private static @Nullable ReferenceType asReferenceType(
      ParameterBound bound, Substitution substitution) {
    if (bound instanceof LazyParameterBound) {
      ParameterBound converted = ((LazyParameterBound) bound).substituteWithUnknowns(substitution);
      return converted instanceof ReferenceBound
          ? ((ReferenceBound) converted).getBoundType()
          : null;
    }
    if (bound instanceof ReferenceBound) {
      return ((ReferenceBound) bound).getBoundType();
    }
    return null;
  }
}
