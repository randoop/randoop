package randoop.main;

/**
 * Thrown to indicate a user error in invoking Randoop from the command line. Randoop prints the
 * message and also prints how to get help.
 */
public class RandoopTextuiException extends Exception {

  private static final long serialVersionUID = 1L;

  public RandoopTextuiException(String string) {
    super(string);
  }
}
