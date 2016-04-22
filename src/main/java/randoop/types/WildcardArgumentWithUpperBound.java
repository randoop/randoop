package randoop.types;

import java.lang.reflect.Type;

/**
 * Represents a wildcard type that has an upper bound (e.g., {@code ? extends T}).
 * See JLS, section 4.5.1
 */
public class WildcardArgumentWithUpperBound extends WildcardArgument {

  /**
   * Creates a wildcard argument with an upper bound.
   * Assumes that the type array has a single element as defined in
   * JLS, section 4.5.1
   *
   * @param upperBounds  the upper bound type array
   */
  public WildcardArgumentWithUpperBound(Type[] upperBounds) {
    super(ReferenceType.forType(upperBounds[0]));
  }

  @Override
  public boolean canBeInstantiatedAs(GeneralType generalType, Substitution substitution) {
    return getBoundType().isAssignableFrom(generalType);
  }
}
