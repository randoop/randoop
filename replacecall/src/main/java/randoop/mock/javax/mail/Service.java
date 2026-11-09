package randoop.mock.javax.mail;

/**
 * Class of mock methods for {@code javax.mail.Service}. The mock methods do not connect to the
 * network.
 *
 * <p>This is not a default replacement, because {@code javax.mail} is not part of the JDK. To use
 * it, put this line in a replacement file:
 *
 * <pre>javax.mail.Service randoop.mock.javax.mail.Service</pre>
 *
 * <p>The replacecall agent only replaces calls whose compile-time receiver type is {@code
 * javax.mail.Service}. It does not replace calls whose compile-time receiver type is a subclass,
 * such as {@code javax.mail.Transport}.
 *
 * <p>These methods do not refer to any {@code javax.mail} class other than in their signatures,
 * because the bootstrap class loader, which loads this class, cannot load {@code javax.mail}
 * classes.
 */
public final class Service {

  /** Do not instantiate. */
  private Service() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * Mock of {@code javax.mail.Service.connect()}. Does nothing.
   *
   * @param service the receiver (ignored)
   */
  public static void connect(javax.mail.Service service) {}

  /**
   * Mock of {@code javax.mail.Service.connect(String, String)}. Does nothing.
   *
   * @param service the receiver (ignored)
   * @param user the user name (ignored)
   * @param password the password (ignored)
   */
  public static void connect(javax.mail.Service service, String user, String password) {}

  /**
   * Mock of {@code javax.mail.Service.connect(String, String, String)}. Does nothing.
   *
   * @param service the receiver (ignored)
   * @param host the host (ignored)
   * @param user the user name (ignored)
   * @param password the password (ignored)
   */
  public static void connect(
      javax.mail.Service service, String host, String user, String password) {}

  /**
   * Mock of {@code javax.mail.Service.connect(String, int, String, String)}. Does nothing.
   *
   * @param service the receiver (ignored)
   * @param host the host (ignored)
   * @param port the port (ignored)
   * @param user the user name (ignored)
   * @param password the password (ignored)
   */
  public static void connect(
      javax.mail.Service service, String host, int port, String user, String password) {}
}
