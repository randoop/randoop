package randoop.mock.javax.swing;

/**
 * Class of mock methods for {@code javax.swing.JFrame}.
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class JFrame {
  /**
   * Mock of {@code javax.swing.JFrame.pack()} to avoid triggering display events.
   *
   * @param frame the frame that should not be displayed
   */
  public static void pack(javax.swing.JFrame frame) {
    setWindowState(frame);
  }

  /**
   * Mock of {@code javax.swing.JFrame.setVisible(boolean)} to avoid triggering display events.
   *
   * @param frame the frame that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void setVisible(javax.swing.JFrame frame, boolean visible) {
    setWindowState(frame);
  }

  /**
   * Mock of {@code javax.swing.JFrame.show(boolean)} to avoid triggering display events.
   *
   * @param frame the frame that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void show(javax.swing.JFrame frame, boolean visible) {
    setWindowState(frame);
  }

  /**
   * Mock of {@code javax.swing.JFrame.show()} to avoid triggering display events.
   *
   * @param frame the frame that should not be displayed
   */
  public static void show(javax.swing.JFrame frame) {
    setWindowState(frame);
  }

  /**
   * Mock of {@code javax.swing.JFrame.toFront()} to avoid scenario where a dialog could assume
   * focus by moving to the front (this only applies to some dialoging systems).
   *
   * @param frame the frame that shouldn't assume focus
   */
  public static void toFront(javax.swing.JFrame frame) {
    setWindowState(frame);
  }

  /**
   * Mock of method {@code javax.swing.JFrame.requestFocus()} to prevent component getting focus.
   *
   * @param frame the frame that should not receive focus
   */
  public static void requestFocus(javax.swing.JFrame frame) {
    frame.setFocusable(false);
  }

  /**
   * Helper method for mock methods to set the state of a {@code javax.swing.JFrame} so that it is
   * not focusable, and attempt to dispose of it.
   *
   * @param frame the frame to set to unfocusable, and to dispose
   */
  private static void setWindowState(javax.swing.JFrame frame) {
    frame.setFocusableWindowState(false);
    frame.dispose();
  }
}
