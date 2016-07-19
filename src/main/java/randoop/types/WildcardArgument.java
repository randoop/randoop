package randoop.types;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Objects;

/**
 * Represents a wildcard type argument to a parameterized type.
 * A wildcard may have either an upper or lower bound.
 * (See JLS, 4.5.1)
 * <p>
 *   Note that in the context of a
 *   <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.10">capture conversion</a>,
 *   a wildcard has both an upper and a lower bound, computed from the explicit wildcard bound and
 *   bound on the formal type parameter.
 *   The subclasses represent the bound as given for the wildcard.
 *
 * @see WildcardArgumentWithLowerBound
 * @see WildcardArgumentWithUpperBound
 */
abstract class WildcardArgument extends TypeArgument {

  /** the bound type */
  private final ParameterBound boundType;

  /**
   * Initializes the bound type.
   *
   * @param boundType  the bound type
   */
  WildcardArgument(ParameterBound boundType) {
    this.boundType = boundType;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof WildcardArgument)) {
      return false;
    }
    WildcardArgument wildcardArgument = (WildcardArgument) obj;
    return this.boundType.equals(wildcardArgument.boundType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(boundType);
  }

  @Override
  public boolean isGeneric() {
    return false;
  }

  @Override
  public boolean isWildcard() {
    return true;
  }

  @Override
  public boolean hasWildcard() {
    return true;
  }

  /**
   * Indicates whether this wildcard argument has an upper bound.
   * (If not, then it has a lower bound.)
   *
   * @return true if this wildcard argument has an upper bound, false if it has a lower bound
   */
  public abstract boolean hasUpperBound();

  /**
   * Return the type of the upper/lower bound of this wildcard argument.
   *
   * @return the type of the bound of this wildcard argument
   */
  ParameterBound getBoundType() {
    return boundType;
  }

  /**
   * Creates a {@code WildcardArgument} from a {@code java.lang.reflect.Type}.
   * A wildcard may have either an upper or lower bound.
   *
   * @param type  the {@code Type} object
   * @return the {@code WildcardArgument} created from the given {@code Type}
   */
  public static WildcardArgument forType(Type type) {
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

  public WildcardArgument applyCaptureConversion() {
    if (boundType.hasWildcard()) {
      ReferenceBound convertedType = (ReferenceBound) boundType.applyCaptureConversion();
      if (this.hasUpperBound()) {
        return new WildcardArgumentWithUpperBound(convertedType);
      } else {
        return new WildcardArgumentWithLowerBound(convertedType);
      }
    }
    return this;
  }
}
