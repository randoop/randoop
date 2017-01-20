package randoop.input.toradocu;

/** This is a Toradocu class borrowed to allow deserialization of JSON. */
public interface Tag {

  enum Kind {
    THROWS,
    PARAM;

    @Override
    public String toString() {
      switch (name()) {
        case "THROWS":
          return "@throws";
        case "PARAM":
          return "@param";
        default:
          throw new IllegalStateException("The value " + name() + " has no string representation.");
      }
    }
  }

  /**
   * Returns the kind of this tag (e.g., @throws, @param).
   *
   * @return the kind of this tag
   */
  Kind getKind();

  /**
   * Returns the translated Java boolean condition for this tag as an optional which is empty if
   * translation has not been attempted yet.
   *
   * @return the translated conditions for this tag if translation attempted, else empty optional
   */
  String getCondition();

  /**
   * Sets the translated condition for this tag to the given condition.
   *
   * @param condition the translated condition for this tag (as a Java boolean condition)
   * @throws NullPointerException if condition is null
   */
  void setCondition(String condition);

  /**
   * Returns the comment associated with the exception in this tag.
   *
   * @return the comment associated with the exception in this tag
   */
  String getComment();
}
