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
   * type contains a type variable, as in {@code Map<T, String>}, because the bounds of that type
   * variable might refer back to the type. The bound is an {@link EagerReferenceBound} otherwise.
   * Other code also creates an {@link EagerReferenceBound} whose type contains a type variable, so
   * not every bound that contains a type variable is a {@link LazyReferenceBound}.
   *
   * @param boundType a reference type
   * @return a bound whose type is {@code boundType}
   */
  static EagerReferenceBound forTypeLazyIfGeneric(ReferenceType boundType) {
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

  /**
   * Returns true if the given result of applying a substitution to the type of this bound is the
   * same as the type of this bound, so that the substitution can return this bound.
   *
   * <p>A copy of a type variable equals the original even if its bounds differ. So if the type of
   * this bound is a type variable, as {@code Y} in {@code Y extends List<X>}, then a substitution
   * for {@code X} yields a copy that equals {@code Y} but whose bound is the substituted one. That
   * copy is kept, rather than the original.
   *
   * @param substitutedType the type of this bound, with a substitution applied
   * @return true if {@code substitutedType} is the same as the type of this bound
   */
  @SuppressWarnings("ReferenceEquality") // the identical type is unchanged, even a type variable
  boolean isUnchangedBy(ReferenceType substitutedType) {
    if (substitutedType == boundType) {
      return true;
    }
    return substitutedType.equals(boundType) && !substitutedType.isVariable();
  }

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

  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    // Mapping each type variable that the substitution does not replace to itself makes it an
    // unknown.
    Substitution completeSubstitution = substitution.extendWithIdentity(getTypeParameters());
    return bound.hasUpperBound(boundType.substitute(completeSubstitution), completeSubstitution);
  }

  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    return lowerBound.isLowerBound(boundType, substitution);
  }

  @Override
  boolean hasUpperBound(ReferenceType upperBound, Substitution substitution) {
    return Subtyping.isSubtypeOrEqual(boundType, upperBound, substitution);
  }
}
