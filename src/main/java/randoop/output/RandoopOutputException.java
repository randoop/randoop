package randoop.output;

import java.io.IOException;

/** An exception (usually an IOException) occurred while writing tests to a file. */
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

  /**
   * Creates a {@link RandoopOutputException} with the cause.
   *
   * @param cause the exception for the error
   */
  public RandoopOutputException(IOException cause) {
    super(cause);
  }
}
