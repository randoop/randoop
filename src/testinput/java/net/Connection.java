package net;

/**
 * Note: this file is a slighly modified version of the tutorial file for Toradocu, and was used to
 * generate the {@code net_Connection} class at the top level of the testinput source set.
 * If this class is modified, it should be run through Toradocu again to generate that input file
 * again.
 * java -jar ../build/libs/toradocu-1.0-all.jar org.toradocu.Toradocu \
 * --target-class net.Connection \
 * --source-dir src --class-dir src \
 * --condition-translator-output ConnectionConditions.json \
 * --oracle-generation false \
 * --export-conditions connection-conditions
 */
public class Connection {

  private boolean open = false;

  /**
   * Indicates whether this connection is open.
   *
   * @returns true if this connection is open, false otherwise
   */
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
}
