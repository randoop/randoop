package randoop.condition;

import java.util.Objects;
import randoop.types.ClassOrInterfaceType;

/** Represents an expected exception in a throws-condition. */
public class ThrowsClause {

  /** The descriptive text for this exception. */
  private final String comment;

  /** The type of the exception. */
  private final ClassOrInterfaceType exceptionType;

  /**
   * Creates a {@link ThrowsClause} object with the given type, and comment.
   *
   * @param exceptionType the type of the expected exception
   * @param comment the text description of the throws clause
   */
  ThrowsClause(ClassOrInterfaceType exceptionType, String comment) {
    this.exceptionType = exceptionType;
    this.comment = comment;
  }

  /**
   * Returns the type of the exception.
   *
   * @return the type of the exception
   */
  public ClassOrInterfaceType getExceptionType() {
    return exceptionType;
  }

  /**
   * Returns the descriptive comment for this {@link ThrowsClause}.
   *
   * @return the comment for this {@link ThrowsClause}
   */
  public String getComment() {
    return comment;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ThrowsClause)) {
      return false;
    }
    ThrowsClause other = (ThrowsClause) object;
    return this.exceptionType.equals(other.exceptionType) && this.comment.equals(other.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exceptionType, comment);
  }

  @Override
  public String toString() {
    return exceptionType + " // " + comment;
  }
}
