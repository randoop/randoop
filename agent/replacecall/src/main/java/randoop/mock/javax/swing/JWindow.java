package randoop.mock.javax.swing;

/**
 * Class of mock methods for {@code javax.swing.JWindow}.
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class JWindow {
  /**
   * Mock of {@code javax.swing.JWindow.pack()} to avoid triggering display events.
   *
   * @param window the window that should not be displayed
   */
  public static void pack(javax.swing.JWindow window) {
    setWindowState(window);
  }

  /**
   * Mock of {@code javax.swing.JWindow.setVisible(boolean)} to avoid triggering display events.
   *
   * @param window the window that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void setVisible(javax.swing.JWindow window, boolean visible) {
    setWindowState(window);
  }

  /**
   * Mock of {@code javax.swing.JWindow.show(boolean)} to avoid triggering display events.
   *
   * @param window the window that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void show(javax.swing.JWindow window, boolean visible) {
    setWindowState(window);
  }

  /**
   * Mock of {@code javax.swing.JWindow.show()} to avoid triggering display events.
   *
   * @param window the window that should not be displayed
   */
  public static void show(javax.swing.JWindow window) {
    setWindowState(window);
  }

  /**
   * Mock of {@code javax.swing.JWindow.toFront()} to avoid scenario where a dialog could assume
   * focus by moving to the front (this only applies to some dialoging systems).
   *
   * @param window the window that shouldn't assume focus
   */
  public static void toFront(javax.swing.JWindow window) {
    setWindowState(window);
  }

  /**
   * Mock of method {@code javax.swing.JWindow.requestFocus()} to prevent component getting focus.
   *
   * @param window the window that should not receive focus
   */
  public static void requestFocus(javax.swing.JWindow window) {
    window.setFocusable(false);
  }

  /**
   * Helper method for mock methods to set the state of a {@code javax.swing.JWindow} so that it is
   * not focusable, and attempt to dispose of it.
   *
   * @param window the window to set to unfocusable, and to dispose
   */
  private static void setWindowState(javax.swing.JWindow window) {
    window.setFocusableWindowState(false);
    window.dispose();
  }
}
