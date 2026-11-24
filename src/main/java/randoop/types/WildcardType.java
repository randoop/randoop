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
   * Returns true if this wildcard contains the given one, following <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1">JLS section
   * 4.5.1</a>:
   *
   * <ul>
   *   <li>{@code ? extends T} contains {@code ? extends S} if {@code S} is a subtype of {@code T}.
   *       This includes {@code ?}, which is {@code ? extends Object}.
   *   <li>{@code ?} contains {@code ? super S}
   *   <li>{@code ? super T} contains {@code ? super S} if {@code T} is a subtype of {@code S}
   * </ul>
   *
   * A wildcard {@code ? super T} does not contain {@code ?}, nor any other wildcard with an upper
   * bound.
   *
   * <p>A {@link LazyParameterBound}, which cannot be compared directly, is first converted to the
   * equivalent {@link ReferenceBound}.
   *
   * @param otherType the type to check for
   * @return true if this type contains the other type, false otherwise
   */
  public boolean contains(WildcardType otherType) {
    ReferenceType thisBound = ReferenceBound.asReferenceType(this.getTypeBound());
    if (thisBound == null) {
      return false;
    }
    // `?`, which is `? extends Object`, contains every wildcard, including every `? super S`.
    if (this.hasUpperBound && thisBound.isObject()) {
      return true;
    }
    if (this.hasUpperBound != otherType.hasUpperBound) {
      return false;
    }
    ReferenceType otherBound = ReferenceBound.asReferenceType(otherType.getTypeBound());
    if (otherBound == null) {
      return false;
    }
    if (this.hasUpperBound) {
      return otherBound.isSubtypeOfOrEqualTo(thisBound);
    } else {
      return thisBound.isSubtypeOfOrEqualTo(otherBound);
    }
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
