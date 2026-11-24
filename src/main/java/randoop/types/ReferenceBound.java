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
   * <p>The substitution is applied only to {@code lowerBound}. A type variable in this bound, which
   * an eager bound can also contain, as in {@code T[]} or {@code List<T>}, stands for itself. So,
   * with {@code T := String}, the bound {@code String[]} is not a lower bound of {@code T[]}, but
   * {@code T[]} is a lower bound of {@code String[]}. Likewise, with the empty substitution, {@code
   * List<String>} is not a lower bound of {@code List<T>}, though {@link
   * EagerReferenceBound#isLowerBound(Type, Substitution)} would accept it, because {@code
   * List<String>} is an instantiation of {@code List<T>}. The null type is a lower bound of every
   * bound, and an intersection is a lower bound if any of its members is. Only a {@link
   * LazyReferenceBound} whose type is a type variable is compared by {@link
   * ParameterBound#isLowerBound(Type, Substitution)}, which lets that type variable be chosen.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    // The substitution applies to lowerBound but not to this bound.  An eager bound can contain
    // type variables, as in T[], where T is not recursive; they stand for themselves.
    // For target T with T := Number, Integer is not known to be a subtype of T.
    if (lowerBound instanceof IntersectionTypeBound) {
      // An intersection type is a subtype of each supertype of one of its members.  Each member is
      // compared by this method, so that this bound stays fixed:  `List<String> & Serializable` is
      // not a lower bound of `List<T>`, though `List<String>` is an instantiation of `List<T>`.
      for (ParameterBound member : ((IntersectionTypeBound) lowerBound).getBounds()) {
        if (hasLowerBound(member, substitution)) {
          return true;
        }
      }
      return false;
    }
    // A lazy bound whose type is compound, such as ArrayList<T>, is compared as an eager one:  the
    // type variables of this bound are fixed, so they must not be inferred.  Only a lazy bound
    // whose type is a type variable is compared by isLowerBound, which lets the variable be chosen.
    if (lowerBound instanceof LazyReferenceBound
        && ((LazyReferenceBound) lowerBound).getBoundType().isVariable()) {
      return lowerBound.isLowerBound(boundType, substitution);
    }
    return isSubtypeOfFixed(lowerBound.substitute(substitution), boundType);
  }

  /**
   * Returns true if the type of the given bound is a subtype of, or equal to, the given type, in
   * which every type variable stands for itself. An intersection bound is a subtype of the type if
   * any of its members is. A {@link LazyParameterBound} is converted to the equivalent {@link
   * ReferenceBound}.
   *
   * @param bound a bound, to which a substitution has already been applied
   * @param type a type whose type variables stand for themselves
   * @return true if the type of {@code bound} is a subtype of, or equal to, {@code type}
   */
  static boolean isSubtypeOfFixed(ParameterBound bound, ReferenceType type) {
    if (bound instanceof LazyParameterBound) {
      ReferenceBound converted = ((LazyParameterBound) bound).toReferenceBound();
      return converted != null && isSubtypeOfFixed(converted, type);
    }
    if (bound instanceof IntersectionTypeBound) {
      for (ParameterBound member : ((IntersectionTypeBound) bound).getBounds()) {
        if (isSubtypeOfFixed(member, type)) {
          return true;
        }
      }
      return false;
    }
    if (bound instanceof ReferenceBound) {
      // A substitution with no unknowns makes each type variable stand for itself.
      return Subtyping.isSubtypeOrEqual(
          ((ReferenceBound) bound).getBoundType(), type, new Substitution());
    }
    return false;
  }

  @Override
  boolean hasUpperBound(ReferenceType upperBound, Substitution substitution) {
    return Subtyping.isSubtypeOrEqual(boundType, upperBound, substitution);
  }

  /**
   * Returns the type of the given bound, if it is a {@link ReferenceBound} or a {@link
   * LazyParameterBound} that can be converted to a {@link ReferenceBound}. Otherwise, returns null.
   *
   * @param bound a bound
   * @return the type of {@code bound}, or null
   */
  /*package*/ static @Nullable ReferenceType asReferenceType(ParameterBound bound) {
    if (bound instanceof LazyParameterBound) {
      ReferenceBound converted = ((LazyParameterBound) bound).toReferenceBound();
      return converted == null ? null : converted.getBoundType();
    }
    if (bound instanceof ReferenceBound) {
      return ((ReferenceBound) bound).getBoundType();
    }
    return null;
  }
}
