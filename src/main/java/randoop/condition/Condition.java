package randoop.condition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import randoop.util.Log;

/**
 * Represents a condition on an operation. Kind of like a predicate, but with names changed to
 * protect the innocent.
 */
public class Condition {

  /** The {@code java.lang.reflect.Method} to test this condition */
  private final Method conditionMethod;

  /** The comment describing this condition */
  private final String comment;

  /** The code text for this condition */
  private final String conditionText;

  /**
   * Creates a {@link Condition} that calls the given condition method.
   *
   * @param conditionMethod the reflection Method for the condition method
   * @param comment the comment describing this condition
   * @param conditionText the text for this condition
   */
  Condition(Method conditionMethod, String comment, String conditionText) {
    this.conditionMethod = conditionMethod;
    this.comment = comment;
    this.conditionText = conditionText;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Condition)) {
      return false;
    }
    Condition other = (Condition) object;
    return this.conditionMethod.equals(other.conditionMethod)
        && this.comment.equals(other.comment)
        && this.conditionText.equals(other.conditionText);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conditionMethod, comment, conditionText);
  }

  @Override
  public String toString() {
    return conditionText + " // " + comment;
  }

  /**
   * Indicate whether this condition is satisfied by the given values.
   *
   * @param values the values to check the condition against
   * @return true if this condition is satisfied by the values, false otherwise
   */
  public boolean check(Object[] values) {
    try {
      return (boolean) conditionMethod.invoke(null, values);
    } catch (IllegalAccessException e) {
      throw new RandoopConditionError("Failure executing condition method", e);
    } catch (InvocationTargetException e) {
      String message =
          "Failure executing condition method: "
              + conditionMethod
              + "(invoke threw "
              + e.getCause()
              + ")";
      if (Log.isLoggingOn()) {
        Log.logLine(message);
      }
    }
    return false;
  }

  /**
   * Return text that describes this condition.
   *
   * @return text that describes this condition
   */
  public String getComment() {
    return comment;
  }

  /**
   * Return this condition as a string representation of Java code. Arguments to the condition
   * {@link randoop.contract.ObjectContract} convention where variables are represented by {@code
   * x0}, ..., {@code xn} for some number {@code n}. If the operation takes a receiver it will be
   * {@code x0}, and if the operation has a return value it will be {@code xn} (the last variable).
   *
   * @return the Java representation of the condition as a {@code String}
   */
  public String getConditionString() {
    return conditionText;
  }
}
