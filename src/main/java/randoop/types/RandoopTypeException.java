package randoop.types;

/** Exception used to indicate when a type problem has occurred in Randoop. */
public class RandoopTypeException extends Throwable {
  private static final long serialVersionUID = 1777971534091630396L;

  public RandoopTypeException(String msg) {
    super(msg);
  }
}
