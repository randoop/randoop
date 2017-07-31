package randoop.condition;

import java.lang.reflect.Method;

/**
 * The executable form of a {@link randoop.condition.specification.Property}.
 *
 * <p>Though a property may refer to pre-state, at the moment this just behaves as a {@link
 * Condition} that is applied to the post-state.
 */
public class PostCondition extends Condition {

  /**
   * Creates a {@link PostCondition} that calls the given condition method.
   *
   * @param conditionMethod the reflection object for the condition method
   * @param comment the comment describing this condition
   * @param conditionText the text for this condition
   */
  PostCondition(Method conditionMethod, String comment, String conditionText) {
    super(conditionMethod, comment, conditionText);
  }

  /**
   * Returns the {@link PostCondition} that checks the condition with the given argument values as
   * the pre-state.
   *
   * <p>Since pre-state is not implemented, this method just returns this object.
   *
   * @param args the pre-state values to the arguments
   * @return the {@link PostCondition} with the pre-state set
   */
  PostCondition addPrestate(Object[] args) {
    return this;
  }
}
