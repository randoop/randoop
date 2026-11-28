package randoop.types;

import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a wildcard type argument to a parameterized type. */
public class WildcardArgument extends TypeArgument {

  /** The wildcard type. */
  private final WildcardType argumentType;

  /**
   * Creates a WildcardArgument.
   *
   * @param argumentType the wildcard type
   */
  /*package*/ WildcardArgument(WildcardType argumentType) {
    super();
    this.argumentType = argumentType;
  }

  /**
   * Creates a {@code WildcardArgument} from a {@code java.lang.reflect.Type}. A wildcard may have
   * either an upper or lower bound.
   *
   * @param type the {@code Type} object
   * @return the {@code WildcardArgument} created from the given {@code Type}
   */
  public static WildcardArgument forType(java.lang.reflect.Type type) {
    if (!(type instanceof java.lang.reflect.WildcardType)) {
      throw new IllegalArgumentException("Must be a wildcard type " + type);
    }
    java.lang.reflect.WildcardType wildcardType = (java.lang.reflect.WildcardType) type;

    return new WildcardArgument(WildcardType.forType(wildcardType));
  }

  /**
   * Creates a {@code WildcardArgument} from a {@link ReferenceType}, which must be a {@link
   * WildcardType}.
   *
   * @param argumentType the wildcard type
   * @return the {@code WildcardArgument} for the given wildcard type
   */
  public static WildcardArgument forType(ReferenceType argumentType) {
    assert argumentType instanceof WildcardType : "argument type must be wildcard type";
    return new WildcardArgument((WildcardType) argumentType);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof WildcardArgument)) {
      return false;
    }
    WildcardArgument wildcardArgument = (WildcardArgument) obj;
    return this.argumentType.equals(wildcardArgument.argumentType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(argumentType);
  }

  @Override
  public String getFqName() {
    return argumentType.getFqName();
  }

  @Override
  public String getBinaryName() {
    return argumentType.getBinaryName();
  }

  @Override
  public String toString() {
    return argumentType.toString();
  }

  @Override
  public WildcardArgument substitute(Substitution substitution) {
    WildcardType argType = this.argumentType.substitute(substitution);
    if (Type.sameType(argType, this.argumentType)) {
      return this;
    }
    return new WildcardArgument(argType);
  }

  /**
   * {@inheritDoc}
   *
   * <p>For a non-wildcard argument {@code R}, considers the cases (JLS section 4.5.1):
   *
   * <ul>
   *   <li>{@code ? extends B} (including {@code ?}) contains {@code R} if {@code R} is a subtype of
   *       {@code B}
   *   <li>{@code ? super B} contains {@code R} if {@code B} is a subtype of {@code R}
   * </ul>
   *
   * <p>For a wildcard argument, considers the cases:
   *
   * <ul>
   *   <li>{@code ? extends B} contains {@code ? extends S} if {@code S} is a subtype of {@code B}
   *   <li>{@code ?} (that is, {@code ? extends Object}) contains every wildcard, including {@code ?
   *       super S}
   *   <li>{@code ? super B} contains {@code ? super S} if {@code B} is a subtype of {@code S}
   *   <li>{@code ? super B} does not contain {@code ? extends S}, not even {@code ?}
   * </ul>
   *
   * <p>A bound that is a {@link LazyParameterBound}, such as {@code Comparable<T>} for {@code T
   * extends Comparable<T>}, is converted to the equivalent {@link ReferenceBound} before it is
   * compared.
   */
  @Override
  public boolean contains(TypeArgument argument) {
    if (argument.isWildcard() && argumentType.equals(((WildcardArgument) argument).argumentType)) {
      return true;
    }
    ReferenceType boundType = ReferenceBound.asReferenceType(argumentType.getTypeBound());
    if (boundType == null) {
      return false;
    }
    if (argument.isWildcard()) {
      // `?`, which is `? extends Object`, contains every wildcard, including every `? super S`.
      if (argumentType.hasUpperBound() && boundType.isObject()) {
        return true;
      }
      WildcardArgument otherWildcard = (WildcardArgument) argument;
      ReferenceType otherBoundType = ReferenceBound.asReferenceType(otherWildcard.getTypeBound());
      if (otherBoundType == null) {
        return false;
      }
      if (argumentType.hasUpperBound()) {
        return otherWildcard.hasUpperBound() && isSubtypeOfOrEqualTo(otherBoundType, boundType);
      } else {
        return !otherWildcard.hasUpperBound() && isSubtypeOfOrEqualTo(boundType, otherBoundType);
      }
    }
    ReferenceType otherType = ((ReferenceArgument) argument).getReferenceType();
    // Subtyping compares the type arguments of enclosing types by containment, so that, for
    // example, `? extends Outer<?>.Plain` contains Outer<String>.Plain.
    if (argumentType.hasUpperBound()) {
      return boundType.isObject() || isSubtypeOfOrEqualTo(otherType, boundType);
    } else {
      return isSubtypeOfOrEqualTo(boundType, otherType);
    }
  }

  /**
   * Returns true if {@code type} is a subtype of, or equal to, {@code otherType}. Besides {@link
   * ReferenceType#isSubtypeOfOrEqualTo(Type)}, uses {@link Subtyping}, which compares the type
   * arguments of enclosing types by containment: {@code Outer<String>.Plain} is a subtype of {@code
   * Outer<?>.Plain}.
   *
   * @param type a type
   * @param otherType a type
   * @return true if {@code type} is a subtype of, or equal to, {@code otherType}
   */
  private static boolean isSubtypeOfOrEqualTo(ReferenceType type, ReferenceType otherType) {
    return type.isSubtypeOfOrEqualTo(otherType)
        || Subtyping.isSubtypeOrEqual(type, otherType, new Substitution());
  }

  /**
   * Returns the type of the upper/lower bound of this wildcard argument.
   *
   * @return the type of the bound of this wildcard argument
   */
  public ParameterBound getTypeBound() {
    return argumentType.getTypeBound();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the type parameters of the bound of this wildcard argument
   */
  @Override
  public List<TypeVariable> getTypeParameters() {
    return argumentType.getTypeParameters();
  }

  /**
   * Returns true if this wildcard argument has an upper bound. (If not, then it has a lower bound.)
   *
   * @return true if this wildcard argument has an upper bound, false if it has a lower bound
   */
  /*package*/ boolean hasUpperBound() {
    return argumentType.hasUpperBound();
  }

  @Override
  public boolean hasWildcard() {
    return true;
  }

  @Override
  public boolean hasCaptureVariable() {
    return false;
  }

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    return argumentType.isGeneric(ignoreWildcards);
  }

  /*package*/
  @Override
  boolean isInstantiationOfTypeArgument(TypeArgument otherArgument) {
    if (this.equals(otherArgument)) {
      return true;
    }

    if (otherArgument instanceof ReferenceArgument) {
      ReferenceType otherReferenceType = ((ReferenceArgument) otherArgument).getReferenceType();
      if (otherReferenceType instanceof CaptureTypeVariable) {
        CaptureTypeVariable otherCaptureTypeVar = (CaptureTypeVariable) otherReferenceType;
        if (this.equals(otherCaptureTypeVar.getWildcard())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public @Nullable Substitution getInstantiatingSubstitution(TypeArgument goalType) {
    if (this.equals(goalType)) {
      return new Substitution();
    }

    if (goalType instanceof WildcardArgument) {
      return argumentType.getInstantiatingSubstitution(((WildcardArgument) goalType).argumentType);
    }

    if (goalType instanceof ReferenceArgument) {
      ReferenceType otherReferenceType = ((ReferenceArgument) goalType).getReferenceType();
      if (otherReferenceType instanceof CaptureTypeVariable) {
        CaptureTypeVariable otherCaptureTypeVar = (CaptureTypeVariable) otherReferenceType;
        return this.getInstantiatingSubstitution(otherCaptureTypeVar.getWildcard());
      }
    }
    return null;
  }

  @Override
  public boolean isWildcard() {
    return true;
  }

  @Override
  public boolean isVariable() {
    return false;
  }

  /**
   * Returns the wildcard type of this argument.
   *
   * @return the wildcard type of this argument
   */
  /*package*/ WildcardType getWildcardType() {
    return argumentType;
  }
}
