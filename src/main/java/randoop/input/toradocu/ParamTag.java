package randoop.input.toradocu;

import java.util.Objects;

/**
 * This is a Toradocu class borrowed to allow deserialization of JSON.
 *
 * This class represents a param tag in a method. Each param tag consist of the name of the param
 * and a brief description, which may contain a condition. A condition is the translation of the
 * comment into a Java boolean condition.
 */
public class ParamTag extends AbstractTag {

  /** The parameter associated with the param tag */
  private final Parameter parameter;

  /**
   * Constructs a {@code ParamTag} with the given exception and comment.
   *
   * @param parameter the parameter that the tag refers to
   * @param comment the comment associated with the tag
   * @throws NullPointerException if parameter or comment is null
   */
  public ParamTag(Parameter parameter, String comment) {
    super(Kind.PARAM, comment);
    this.parameter = parameter;
  }

  /**
   * Returns the parameter associated to the tag.
   *
   * @return the parameter associated to the tag.
   */
  public Parameter parameter() {
    return parameter;
  }

  /**
   * Returns true if this {@code ParamTag} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ParamTag)) return false;

    ParamTag that = (ParamTag) obj;
    return this.parameter.equals(that.parameter) && super.equals(that);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), parameter);
  }

  /**
   * Returns a string representation of this param tag. The returned string is in the format "@param
   * PARAMNAME COMMENT" where PARAMNAME is the fully qualified name of the parameter in the param
   * tag and COMMENT is the text of the comment in the param tag. If translation has been attempted
   * on this tag, then the returned string is also appended with " ==&gt; CONDITION" where CONDITION
   * is the translated condition as a Java expression or the empty string if translation failed.
   *
   * @return a string representation of this param tag
   */
  @Override
  public String toString() {
    String result = super.getKind() + " " + parameter.getName() + " " + super.getComment();
    if (super.getCondition() != null
        && super.getCondition() != null
        && !super.getCondition().isEmpty()) {
      result += " ==> " + super.getCondition();
    }
    return result;
  }
}
