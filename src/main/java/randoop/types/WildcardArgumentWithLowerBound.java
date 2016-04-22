package randoop.types;

import java.lang.reflect.Type;

/**
 * Represents a wildcard type that has a lower bound (e.g., {@code ? super T}).
 * See JLS, section 4.5.1
 */
public class WildcardArgumentWithLowerBound extends WildcardArgument {

  /**
   * Creates a wildcard argument with a lower bound.
   * Assumes that the type array has a single element as defined in
   * JLS, section 4.5.1
   *
   * @param lowerBounds  the lower bound type array
   */
  public WildcardArgumentWithLowerBound(Type[] lowerBounds) {
    super(ReferenceType.forType(lowerBounds[0]));
  }

  @Override
  public boolean canBeInstantiatedAs(GeneralType generalType, Substitution substitution) {
    return generalType.isAssignableFrom(getBoundType());
  }
}
