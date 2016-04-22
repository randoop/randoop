package randoop.types;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Objects;

/**
 * Represents a wildcard type argument to a parameterized type.
 * (See JLS, 4.5.1)
 *
 * @see WildcardArgumentWithLowerBound
 * @see WildcardArgumentWithUpperBound
 */
public abstract class WildcardArgument extends TypeArgument{

  /** the bound type */
  private final ReferenceType boundType;

  /**
   * Initializes the bound type.
   *
   * @param boundType  the bound type
   */
  WildcardArgument(ReferenceType boundType) {
    this.boundType = boundType;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof WildcardArgument)) {
      return false;
    }
    WildcardArgument wildcardArgument = (WildcardArgument)obj;
    return this.boundType.equals(wildcardArgument.boundType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(boundType);
  }

  @Override
  public boolean isGeneric() {
    return true;
  }

  public ReferenceType getBoundType() {
    return boundType;
  }

  /**
   * Creates a {@code WildcardArgument} from a {@code java.lang.reflect.Type}.
   * A wild card may have either an upper or lower bound.
   *
   * @param type  the {@code Type} object
   * @return the {@code WildcardArgument} created from the given {@code Type}
   */
  public static WildcardArgument forType(Type type) {
    if (! (type instanceof WildcardType)) {
      throw new IllegalArgumentException("Must be a wildcard type " + type);
    }
    WildcardType wildcardType = (WildcardType)type;

    if (wildcardType.getUpperBounds().length > 0) {
      assert wildcardType.getUpperBounds().length == 1 : "a wildcard is defined by the JLS to only have one bound";
      return new WildcardArgumentWithUpperBound(wildcardType.getUpperBounds());
    }
    if (wildcardType.getLowerBounds().length > 0) {
      assert wildcardType.getLowerBounds().length == 1 : "a wildcard is defined by the JLS to only have one bound";
      return new WildcardArgumentWithLowerBound(wildcardType.getLowerBounds());
    }

    throw new IllegalArgumentException("A wildcard must have either upper or lower bounds");
  }
}
