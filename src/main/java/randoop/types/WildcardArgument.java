package randoop.types;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a wildcard type argument to a parameterized type. */
public class WildcardArgument extends TypeArgument {

  /** The wildcard type. */
  private final WildcardType argumentType;

  /**
   * The (wildcard type, type) pairs whose {@link #contains(TypeArgument)} check of a non-wildcard
   * argument is executing on the current thread. The subtype test of the check can lead back to the
   * same check. For example, where {@code C<T>} implements {@code G<G<? super C<T>>>}, checking
   * that {@code ? super C<String>} contains {@code G<? super C<String>>} tests whether {@code
   * C<String>} is a subtype of {@code G<? super C<String>>}. That test compares the supertype
   * {@code G<G<? super C<String>>>} of {@code C<String>}, which repeats the check. As in {@link
   * EagerReferenceBound}, the repeated check returns true: it succeeds if the rest of the original
   * check succeeds.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<Set<List<Type>>> containsChecksInProgress =
      ThreadLocal.withInitial(HashSet::new);

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
   */
  @Override
  public boolean contains(TypeArgument argument) {
    if (argument.isWildcard()) {
      WildcardType otherWildcard = ((WildcardArgument) argument).argumentType;
      if (argumentType.equals(otherWildcard)) {
        return true;
      }
      return argumentType.contains(otherWildcard);
    }
    ReferenceType otherType = ((ReferenceArgument) argument).getReferenceType();
    ReferenceType boundType = ReferenceBound.asReferenceType(argumentType.getTypeBound());
    if (boundType == null) {
      return false;
    }
    if (argumentType.hasUpperBound() && boundType.isObject()) {
      return true;
    }
    List<Type> check = Arrays.asList(argumentType, otherType);
    Set<List<Type>> inProgress = containsChecksInProgress.get();
    if (!inProgress.add(check)) {
      // This check depends on itself, so assume it holds. Any other condition still holds or fails
      // on its own.
      return true;
    }
    try {
      if (argumentType.hasUpperBound()) {
        return otherType.isSubtypeOfOrEqualTo(boundType);
      } else {
        return boundType.isSubtypeOfOrEqualTo(otherType);
      }
    } finally {
      inProgress.remove(check);
    }
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
