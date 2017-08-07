package randoop.output;

import java.io.IOException;

/** Exception for test output errors in Randoop. */
public class RandoopOutputException extends Throwable {

  private static final long serialVersionUID = -9104568425559719500L;

  /**
   * Creates a {@link RandoopOutputException} with the message.
   *
   * @param message the error message
   */
  public RandoopOutputException(String message) {
    super(message);
  }

  /**
   * Creates a {@link RandoopOutputException} with the message and cause.
   *
   * @param message the error message
   * @param cause the exception for the error
   */
  public RandoopOutputException(String message, IOException cause) {
    super(message, cause);
  }
}
