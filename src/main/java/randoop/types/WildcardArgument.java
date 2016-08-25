package randoop.types;

import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Objects;

/**
 * Represents a wildcard type argument to a parameterized type.
 * A wildcard may have either an upper or lower bound as defined in
 * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.5.1">JLS Section 4.5.1</a>.
 * <pre>
 *   ? [ extends ReferenceType ]
 *   ? [ super ReferenceType ]
 * </pre>
 * <p>
 * The subclasses represent the type bound as given for the wildcard.
 *
 * @see WildcardArgumentWithLowerBound
 * @see WildcardArgumentWithUpperBound
 */
abstract class WildcardArgument extends TypeArgument {

  /** the bound type */
  private final ParameterBound typeBound;

  /**
   * Initializes the bound type.
   *
   * @param boundType  the bound type
   */
  WildcardArgument(ParameterBound boundType) {
    this.typeBound = boundType;
  }

  /**
   * Creates a {@code WildcardArgument} from a {@code java.lang.reflect.Type}.
   * A wildcard may have either an upper or lower bound.
   *
   * @param type  the {@code Type} object
   * @return the {@code WildcardArgument} created from the given {@code Type}
   */
  public static WildcardArgument forType(java.lang.reflect.Type type) {
    if (!(type instanceof WildcardType)) {
      throw new IllegalArgumentException("Must be a wildcard type " + type);
    }
    WildcardType wildcardType = (WildcardType) type;

    // Note: every wildcard has an upper bound, so need to check lower first
    if (wildcardType.getLowerBounds().length > 0) {
      assert wildcardType.getLowerBounds().length == 1
          : "a wildcard is defined by the JLS to only have one bound";
      return new WildcardArgumentWithLowerBound(wildcardType.getLowerBounds());
    }
    if (wildcardType.getUpperBounds().length > 0) {
      assert wildcardType.getUpperBounds().length == 1
          : "a wildcard is defined by the JLS to only have one bound";
      return new WildcardArgumentWithUpperBound(wildcardType.getUpperBounds());
    }

    throw new IllegalArgumentException("A wildcard must have either upper or lower bounds");
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof WildcardArgument)) {
      return false;
    }
    WildcardArgument wildcardArgument = (WildcardArgument) obj;
    return this.typeBound.equals(wildcardArgument.typeBound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeBound);
  }

  /**
   * Applies a capture conversion to the bound of this {@link WildcardArgument}.
   *
   * @see ReferenceType#applyCaptureConversion()
   *
   * @return this wildcard argument with capture conversion applied to the type bound
   */
  public WildcardArgument applyCaptureConversion() {
    if (typeBound.hasWildcard()) {
      ReferenceBound convertedType = (ReferenceBound) typeBound.applyCaptureConversion();
      if (this.hasUpperBound()) {
        return new WildcardArgumentWithUpperBound(convertedType);
      } else {
        return new WildcardArgumentWithLowerBound(convertedType);
      }
    }
    return this;
  }

  /**
   * Return the type of the upper/lower bound of this wildcard argument.
   *
   * @return the type of the bound of this wildcard argument
   */
  ParameterBound getTypeBound() {
    return typeBound;
  }

  /**
   * {@inheritDoc}
   * Returns the type parameters of the bound of this wildcard argument
   */
  @Override
  public List<TypeVariable> getTypeParameters() {
    return typeBound.getTypeParameters();
  }

  /**
   * Indicates whether this wildcard argument has an upper bound.
   * (If not, then it has a lower bound.)
   *
   * @return true if this wildcard argument has an upper bound, false if it has a lower bound
   */
  public abstract boolean hasUpperBound();

  @Override
  public boolean hasWildcard() {
    return true;
  }

  @Override
  public boolean isGeneric() {
    return false;
  }

  @Override
  public boolean isWildcard() {
    return true;
  }
}
