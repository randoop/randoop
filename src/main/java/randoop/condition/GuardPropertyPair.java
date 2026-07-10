package randoop.condition;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a pair of an executable guard and an executable property.
 *
 * <p>Corresponds to a {@link randoop.condition.specification.Postcondition}.
 */
public class GuardPropertyPair {

  /** The expression that must hold before the operation is called. */
  final ExecutableBooleanExpression guard;

  /**
   * The expression that must be true after the operation is called when the {@link #guard} is true.
   */
  final ExecutableBooleanExpression property;

  /**
   * Creates a {@link GuardPropertyPair} object for the guard and property expressions.
   *
   * @param guard the {@link ExecutableBooleanExpression} to be evaluated before the operation is
   *     called
   * @param property the {@link ExecutableBooleanExpression} to be evaluated after the operation is
   *     called
   */
  GuardPropertyPair(ExecutableBooleanExpression guard, ExecutableBooleanExpression property) {
    this.guard = guard;
    this.property = property;
  }

  @Override
  public boolean equals(@Nullable Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof GuardPropertyPair)) {
      return false;
    }
    GuardPropertyPair other = (GuardPropertyPair) object;
    return this.guard.equals(other.guard) && this.property.equals(other.property);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guard, property);
  }

  @Override
  public String toString() {
    return String.format("GuardPropertyPair{guard=%s, property=%s}", guard, property);
  }
}
