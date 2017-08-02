package randoop.main;

import java.io.IOException;

/** Exception to signal an error in input to Randoop that is likely user-originated. */
public class RandoopInputException extends Throwable {

  private static final long serialVersionUID = 3470114649273676197L;

  /**
   * Creates a {@link RandoopInputException} with the given message and causing exception.
   *
   * @param message the exception message
   * @param cause the causing exception
   */
  RandoopInputException(String message, IOException cause) {
    super(message, cause);
  }
}
