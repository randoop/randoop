package randoop.input.toradocu;

/**
 * This class represents a throws tag in a method. Each @throws tag consists of an exception, a
 * comment, and can have an optional condition. A condition is the translation of the comment into a
 * Java boolean condition. When the condition evaluates to {@code true}, an exception is expected.
 */
public class ReturnTag extends AbstractTag {

  /**
   * Constructs a {@code ThrowsTag} with the given exception, comment, and words tagged with @code
   *
   * @param comment the comment associated to the return tag
   */
  public ReturnTag(String comment) {
    super(Kind.RETURN, comment);
  }

  /**
   * Returns true if this {@code ReturnTag} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ReturnTag)) return false;

    return super.equals(obj);
  }
}
