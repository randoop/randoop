package randoop.types;

import java.util.List;
import java.util.Objects;

/**
 * Represents a wildcard type argument to a parameterized type.
 *
 * <p>The subclasses represent the type bound as given for the wildcard.
 */
public class WildcardArgument extends TypeArgument {

  /** the wildcard type */
  private final WildcardType argumentType;

  /**
   * Initializes the bound type.
   *
   * @param argumentType the wildcard type
   */
  WildcardArgument(WildcardType argumentType) {
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

  public static WildcardArgument forType(ReferenceType argumentType) {
    assert argumentType instanceof WildcardType : "argument type must be wildcard type";
    return new WildcardArgument((WildcardType) argumentType);
  }

  @Override
  public boolean equals(Object obj) {
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
    return Objects.hash(argumentType);
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
    if (argType.equals(this.argumentType)) {
      return this;
    }
    return new WildcardArgument(argType);
  }

  /**
   * Applies a capture conversion to the bound of this {@link WildcardArgument}.
   *
   * @return this wildcard argument with capture conversion applied to the type bound
   * @see ReferenceType#applyCaptureConversion()
   */
  public WildcardArgument applyCaptureConversion() {
    WildcardType wildcardType = argumentType.applyCaptureConversion();
    if (wildcardType.equals(argumentType)) {
      return this;
    }
    return new WildcardArgument(wildcardType);
  }

  @Override
  public boolean contains(TypeArgument argument) {
    return argument.isWildcard()
        && argumentType.contains(((WildcardArgument) argument).argumentType);
  }

  /**
   * Return the type of the upper/lower bound of this wildcard argument.
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
   * Indicates whether this wildcard argument has an upper bound. (If not, then it has a lower
   * bound.)
   *
   * @return true if this wildcard argument has an upper bound, false if it has a lower bound
   */
  boolean hasUpperBound() {
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
  public Substitution getInstantiatingSubstitution(TypeArgument goalType) {
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

  WildcardType getWildcardType() {
    return argumentType;
  }
}
