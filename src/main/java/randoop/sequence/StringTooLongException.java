package randoop.sequence;

import randoop.main.RandoopBug;

/** An exception indicating a string is too long. */
public class StringTooLongException extends RandoopBug {

  private static final long serialVersionUID = 20200205;

  /**
   * Create a StringTooLongException.
   *
   * @param s the long string (not the message as is customary for exception constructors)
   * @param cause the underlying exception, if any
   */
  public StringTooLongException(String s, Exception cause) {
    super(
        String.format(
            "String's formatted length is too long, length = %s: %s...%s",
            s.length(), s.substring(0, 48), s.substring(s.length() - 48)),
        cause);
  }
  /**
   * Create a StringTooLongException.
   *
   * @param s the long string (not the message as is customary for exception constructors)
   */
  public StringTooLongException(String s) {
    this(s, null);
  }
}
