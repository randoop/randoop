package randoop.types;

import java.util.HashSet;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a wildcard type, which occurs as a type argument to a parameterized type.
 *
 * <p>A wildcard may have either an upper or lower bound as defined in <a
 * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1">JLS Section
 * 4.5.1</a>.
 *
 * <pre>
 *   ? [ extends ReferenceType ]
 *   ? [ super ReferenceType ]
 * </pre>
 *
 * @see WildcardArgument
 */
class WildcardType extends ParameterType {

  /** True if this wildcard has an upper bound ({@code extends}), false if a lower bound. */
  private final boolean hasUpperBound;

  /** Do not create a wildcard type without a bound. */
  private WildcardType() {
    super();
    //  do not create object
    this.hasUpperBound = false;
  }

  /**
   * Creates a wildcard type with the given bound.
   *
   * @param bound the bound of the wildcard type
   * @param hasUpperBound true if {@code bound} is an upper bound, false if it is a lower bound
   */
  /*package*/
  @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
  WildcardType(ParameterBound bound, boolean hasUpperBound) {
    super();
    this.hasUpperBound = hasUpperBound;
    if (hasUpperBound) {
      this.setUpperBound(bound);
    } else {
      this.setLowerBound(bound);
    }
  }

  /**
   * Creates a wildcard type from a given reflection type. Assumes that the bounds array has a
   * single element as defined in <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1">JLS Section
   * 4.5.1</a>.
   *
   * @param type the {@code java.lang.reflect.WildcardType} object
   * @return a {@link WildcardType} with the bounds from the given reflection type
   */
  public static WildcardType forType(java.lang.reflect.WildcardType type) {
    // Note: every wildcard has an upper bound, so need to check lower first
    java.lang.reflect.Type[] lowerBounds = type.getLowerBounds();
    if (lowerBounds.length > 0) {
      assert lowerBounds.length == 1 : "a wildcard is defined by the JLS to only have one bound";
      return new WildcardType(ParameterBound.forTypes(new HashSet<>(0), lowerBounds), false);
    }
    java.lang.reflect.Type[] upperBounds = type.getUpperBounds();
    if (upperBounds.length > 0) {
      assert upperBounds.length == 1 : "a wildcard is defined by the JLS to only have one bound";
      return new WildcardType(ParameterBound.forTypes(new HashSet<>(0), upperBounds), true);
    }
    throw new IllegalArgumentException("A wildcard must have either upper or lower bounds");
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof WildcardType)) {
      return false;
    }
    WildcardType otherType = (WildcardType) obj;
    return otherType.hasUpperBound == this.hasUpperBound && super.equals(otherType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hasUpperBound, super.hashCode());
  }

  @Override
  public String toString() {
    if (hasUpperBound) {
      if (this.getUpperTypeBound().isObject()) {
        return "?";
      }
      return "? extends " + this.getUpperTypeBound().toString();
    }
    return "? super " + this.getLowerTypeBound().toString();
  }

  @Override
  public String getFqName() {
    if (hasUpperBound) {
      if (this.getUpperTypeBound().isObject()) {
        return "?";
      }
      return "? extends " + this.getUpperTypeBound().toString();
    }
    return "? super " + this.getLowerTypeBound().toString();
  }

  @Override
  public String getBinaryName() {
    if (hasUpperBound) {
      if (this.getUpperTypeBound().isObject()) {
        return "?";
      }
      return "? extends " + this.getUpperTypeBound().toString();
    }
    return "? super " + this.getLowerTypeBound().toString();
  }

  @Override
  public String getSimpleName() {
    return toString();
  }

  /**
   * Returns the bound of this -- either the upper or lower bound.
   *
   * @return the bound of this -- either the upper or lower bound
   */
  public ParameterBound getTypeBound() {
    if (hasUpperBound) {
      return getUpperTypeBound();
    }
    return getLowerTypeBound();
  }

  @Override
  public WildcardType substitute(Substitution substitution) {
    ParameterBound bound = getTypeBound().substitute(substitution);
    if (sameType(bound, this.getTypeBound())) {
      return this;
    }
    return new WildcardType(bound, hasUpperBound);
  }

  /**
   * Returns true if this wildcard contains the other wildcard (JLS section 4.5.1). From a
   * reflection perspective, {@code ?} is just {@code ? extends Object}. The cases are:
   *
   * <ul>
   *   <li>{@code ? extends T} contains {@code ? extends S} if {@code S.isSubtypeOfOrEqualTo(T)}
   *   <li>{@code ? extends Object} (that is, {@code ?}) contains {@code ? super S}
   *   <li>{@code ? super T} contains {@code ? super S} if {@code T.isSubtypeOfOrEqualTo(S)}
   *   <li>{@code ? super T} does not contain any upper-bounded wildcard, including {@code ?}
   * </ul>
   *
   * <p>A {@link LazyParameterBound}, such as {@code Comparable<T>} in {@code ? extends
   * Comparable<T>}, cannot be compared directly, so it is converted to the equivalent {@link
   * ReferenceBound} before the bounds are compared.
   *
   * @param otherType the type to check for
   * @return true if this type contains the other type, false otherwise
   */
  public boolean contains(WildcardType otherType) {
    if (this.hasUpperBound) {
      if (otherType.hasUpperBound) {
        ParameterBound otherBound = resolveLazyBound(otherType.getUpperTypeBound());
        ParameterBound thisBound = resolveLazyBound(this.getUpperTypeBound());
        return otherBound != null
            && thisBound != null
            && otherBound.isSubtypeOfOrEqualTo(thisBound);
      }
      // `? extends Object` contains every `? super S`; `? extends T` for other T does not.
      return this.getUpperTypeBound().isObject();
    } else {
      if (otherType.hasUpperBound) {
        // `? super T` does not contain an upper-bounded wildcard, including `?`.
        return false;
      }
      ParameterBound thisBound = resolveLazyBound(this.getLowerTypeBound());
      ParameterBound otherBound = resolveLazyBound(otherType.getLowerTypeBound());
      return thisBound != null && otherBound != null && thisBound.isSubtypeOfOrEqualTo(otherBound);
    }
  }

  /**
   * Returns the given bound, or, if it is a {@link LazyParameterBound}, the equivalent {@link
   * ReferenceBound}, in which each type variable remains a type variable.
   *
   * @param bound a bound of a wildcard
   * @return a bound that can be compared with {@link ParameterBound#isSubtypeOfOrEqualTo}, or null
   *     if {@code bound} is a lazy bound that cannot be converted
   */
  private static @Nullable ParameterBound resolveLazyBound(ParameterBound bound) {
    if (bound instanceof LazyParameterBound) {
      return ((LazyParameterBound) bound).toReferenceBound();
    }
    return bound;
  }

  @Override
  public boolean hasWildcard() {
    return true;
  }

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    return getTypeBound().isGeneric(ignoreWildcards);
  }

  /**
   * Returns true if this wildcard has an upper bound.
   *
   * @return true if this wildcard has an upper bound
   */
  /*package*/ boolean hasUpperBound() {
    return hasUpperBound;
  }
}
