package randoop.types;

/** Exception used to indicate when a type problem has occurred in Randoop. */
public class RandoopTypeException extends RuntimeException {
  /** ID for serializing this class. */
  private static final long serialVersionUID = 1777971534091630396L;

  /**
   * Creates a RandoopTypeException with the given message.
   *
   * @param msg the error message
   */
  public RandoopTypeException(String msg) {
    super(msg);
  }
}
