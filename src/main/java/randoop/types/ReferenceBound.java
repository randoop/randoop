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
  /*package*/ ReferenceBound(ReferenceType boundType) {
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

  /*package*/
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

  /*package*/
  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    // Mapping each type variable that the substitution does not replace to itself makes it an
    // unknown.
    Substitution completeSubstitution = substitution.extendWithIdentity(getTypeParameters());
    return bound.hasUpperBound(boundType.substitute(completeSubstitution), completeSubstitution);
  }

  /**
   * {@inheritDoc}
   *
   * <p>As specified, the substitution is applied to {@code lowerBound} but not to the type of this
   * bound, even if that type contains type variables, as in {@code T[]}. So, with {@code T :=
   * String}, the bound {@code String[]} is not a lower bound of {@code T[]}, but {@code T[]} is a
   * lower bound of {@code String[]}. If the type of this bound contains type variables but is not
   * itself a type variable, it is compared as by {@link #hasLowerBoundWithFixedVariables}: for
   * example, {@code List<String>} is not a lower bound of {@code List<T>}.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    // The substitution applies to lowerBound but not to this bound.  An eager bound can contain
    // type variables, as in T[], where T is not recursive; they stand for themselves.
    // lowerBound.isLowerBound(Type, Substitution) would treat them as instantiable, accepting
    // List<String> for List<T>.
    if (boundType.isGeneric() && !boundType.isVariable()) {
      return hasLowerBoundWithFixedVariables(lowerBound, substitution);
    }
    return lowerBound.isLowerBound(boundType, substitution);
  }

  /**
   * Returns true if {@code lowerBound}, after the substitution is applied to it, is a lower bound
   * of the type of this bound, in which each type variable stands for itself.
   *
   * <p>Returns true if {@code lowerBound} is the null type, or if, after the substitution is
   * applied to it, it is a subtype of the type of this bound (see {@link
   * Subtyping#isSubtypeOrEqual}). Examples are {@code List<T>} itself, {@code ArrayList<T>[]} for
   * {@code List<T>[]}, and {@code NullType[]} for {@code T[]}. A lower bound that the substitution
   * does not instantiate is compared with its remaining type variables standing for themselves. An
   * intersection is a lower bound if any of its members is. The type of this bound stands for
   * every instantiation of its type variables; a type such as {@code List<String>} is not a subtype
   * of each instantiation of {@code List<T>}, but {@code List<T>} itself is.
   *
   * @param lowerBound the possible lower bound
   * @param substitution the type substitution to apply to {@code lowerBound}
   * @return true if {@code lowerBound} is a lower bound of the type of this bound, with its type
   *     variables fixed
   */
  final boolean hasLowerBoundWithFixedVariables(
      ParameterBound lowerBound, Substitution substitution) {
    if (lowerBound.isNullType(substitution)) {
      return true;
    }
    // Subtyping.isIdentical compares enclosing types too:  Outer<Integer>.Inner<X> is not
    // Outer<Number>.Inner<X>.
    if (lowerBound instanceof ReferenceBound
        && Subtyping.isIdentical(
            ((ReferenceBound) lowerBound).getBoundType().substitute(substitution), boundType)) {
      return true;
    }
    ParameterBound b = lowerBound.substitute(substitution);
    if (b instanceof LazyParameterBound) {
      // The substitution did not instantiate the lower bound.  Convert it, keeping each of its
      // remaining type variables, so that it can be compared with the type of this bound.
      b = ((LazyParameterBound) b).toReferenceBound();
    }
    if (b instanceof IntersectionTypeBound) {
      // An intersection is a subtype of each of its members, such as List<T> & Serializable of
      // List<T>.  The substitution has already been applied to the members.
      for (ParameterBound member : ((IntersectionTypeBound) b).getBounds()) {
        if (hasLowerBoundWithFixedVariables(member, new Substitution())) {
          return true;
        }
      }
      return false;
    }
    if (!(b instanceof ReferenceBound)) {
      return false;
    }
    ReferenceType lowerType = ((ReferenceBound) b).getBoundType();
    // The substitution is not applied to the type of this bound, in which each type variable
    // stands for itself.  So, a subtype of it is a subtype of each instantiation of it, as
    // ArrayList<T>[] is of List<T>[].  The null type is a subtype of every reference type,
    // including a type variable, so array covariance makes, for example, `NullType[]` a subtype of
    // `T[]`.  Subtyping does not treat a type as a subtype of the generic form of its class, as
    // `isSubtypeOfOrEqualTo` does; for example, List<String>[] is not a subtype of each
    // instantiation of List<E>[].
    return Subtyping.isSubtypeOrEqual(lowerType, boundType, new Substitution());
  }

  @Override
  boolean hasUpperBound(ReferenceType upperBound, Substitution substitution) {
    return Subtyping.isSubtypeOrEqual(boundType, upperBound, substitution);
  }
}
