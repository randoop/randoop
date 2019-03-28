package net;

/**
 * Note: this file is a slighly modified version of the tutorial file for Toradocu, and was used to
 * generate the {@code net_Connection} class at the top level of the testInput source set.
 * If this class is modified, it should be run through Toradocu again to generate that input file
 * again.
 */
public class Connection {

  private boolean open = false;

  /** @returns true if the connection is open, false otherwise */
  public boolean isOpen() {
    return open;
  }

  /**
   * Establishes a new connection.
   *
   * @throws IllegalStateException if the connection is already open
   */
  public void open() {

    // implement throws condition
    if (this.isOpen()) {
      throw new IllegalStateException();
    }

    open = true;
  }

  /**
   * Sends a message.
   *
   * @throws NullPointerException if message is null
   * @throws IllegalStateException if the connection is not open
   */
  public void send(String message) {
    // will throw NullPointerException if message == null
    if (message.isEmpty()) {
      return;
    }

    // intentionally does not throw IllegalStateException

  }

  /**
   * @param code  the code, must be positive
   * @throws IllegalStateException if the connection is not open
   */
  public void send(int code) {
    if (!this.isOpen()) {
      throw new IllegalStateException();
    }
    if (code < 1) { // sanity check on precondition
      throw new IllegalArgumentException();
    }
  }

  /**
   * @return the received non-negative code value
   * @throws IllegalStateException if the connection is not open
   */
  public int receive() {
    if (!this.isOpen()) {
      throw new IllegalStateException();
    }
    return 1; //dummy value
  }
}
