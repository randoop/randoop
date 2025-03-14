package randoop.reflection;

public class RandoopInstantiationError extends Error {

  private static final long serialVersionUID = 3611612630372756721L;

  private final String opName;
  private final Throwable exception;

  public RandoopInstantiationError(String opName, Throwable exception) {
    this.opName = opName;
    this.exception = exception;
  }

  public String getOpName() {
    return opName;
  }

  public Throwable getException() {
    return exception;
  }
}
