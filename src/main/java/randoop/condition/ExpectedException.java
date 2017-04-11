package randoop.condition;

import java.util.Objects;
import randoop.types.ClassOrInterfaceType;

/** Represents an expected exception in a throws-condition. */
public class ExpectedException {
  private final String comment;
  private final ClassOrInterfaceType exceptionType;

  ExpectedException(ClassOrInterfaceType exceptionType, String comment) {
    this.exceptionType = exceptionType;
    this.comment = comment;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ExpectedException)) {
      return false;
    }
    ExpectedException other = (ExpectedException) object;
    return this.exceptionType.equals(other.exceptionType) && this.comment.equals(other.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(exceptionType, comment);
  }

  @Override
  public String toString() {
    return exceptionType + " " + comment;
  }
}
