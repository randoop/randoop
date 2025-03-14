package randoop.mock.java.awt;

/**
 * Class of mock methods for {@code java.awt.Window}.
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class Window {
  /**
   * Mock of {@code java.awt.Window.pack()} to avoid triggering display events.
   *
   * @param window the window that should not be displayed
   */
  public static void pack(java.awt.Window window) {
    setWindowState(window);
  }

  /**
   * Mock of {@code java.awt.Window.setVisible(boolean)} to avoid triggering display events.
   *
   * @param window the window that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void setVisible(java.awt.Window window, boolean visible) {
    setWindowState(window);
  }

  /**
   * Mock of {@code java.awt.Window.show(boolean)} to avoid triggering display events.
   *
   * @param window the window that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void show(java.awt.Window window, boolean visible) {
    setWindowState(window);
  }

  /**
   * Mock of {@code java.awt.Window.show()} to avoid triggering display events.
   *
   * @param window the window that should not be displayed
   */
  public static void show(java.awt.Window window) {
    setWindowState(window);
  }

  /**
   * Mock of {@code java.awt.Window.toFront()} to avoid scenario where a window could assume focus
   * by moving to the front (this only applies to some windowing systems).
   *
   * @param window the window that shouldn't assume focus
   */
  public static void toFront(java.awt.Window window) {
    setWindowState(window);
  }

  /**
   * Mock of method {@code java.awt.Window.requestFocus()} to prevent component getting focus.
   *
   * @param window the window that should not receive focus
   */
  public static void requestFocus(java.awt.Window window) {
    window.setFocusable(false);
  }

  /**
   * Helper method for mock methods to set the state of a {@code java.awt.Window} so that it is not
   * focusable, and attempt to dispose of it.
   *
   * @param window the window to set to unfocusable, and to dispose
   */
  private static void setWindowState(java.awt.Window window) {
    window.setFocusableWindowState(false);
    window.dispose();
  }
}
