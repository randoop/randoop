package randoop.input.toradocu;

import java.lang.reflect.Method;

/**
 * Represents a component of a Toradocu harvested return condition.
 * May be a pre-condition or post-condition.
 */
public class ToradocuReturnCondition extends ToradocuCondition {

  private final String conditionString;

  ToradocuReturnCondition(ReturnTag tag, String conditionString, Method conditionMethod) {
    super(tag, conditionMethod);
    this.conditionString = conditionString;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ToradocuReturnCondition)) {
      return false;
    }
    ToradocuReturnCondition other = (ToradocuReturnCondition) obj;
    return super.equals(other) && this.conditionString.equals(other.conditionString);
  }

  @Override
  public String getComment() {
    return null;
  }
}
