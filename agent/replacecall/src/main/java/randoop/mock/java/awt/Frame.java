package randoop.mock.java.awt;

/**
 * Class of mock methods for {@code java.awt.Frame}.
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class Frame {
  /**
   * Mock of {@code java.awt.Frame.pack()} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   */
  public static void pack(java.awt.Frame dialog) {
    setWindowState(dialog);
  }

  /**
   * Mock of {@code java.awt.Frame.setVisible(boolean)} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void setVisible(java.awt.Frame dialog, boolean visible) {
    setWindowState(dialog);
  }

  /**
   * Mock of {@code java.awt.Frame.show(boolean)} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void show(java.awt.Frame dialog, boolean visible) {
    setWindowState(dialog);
  }

  /**
   * Mock of {@code java.awt.Frame.show()} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   */
  public static void show(java.awt.Frame dialog) {
    setWindowState(dialog);
  }

  /**
   * Mock of {@code java.awt.Frame.toFront()} to avoid scenario where a dialog could assume focus by
   * moving to the front (this only applies to some dialoging systems).
   *
   * @param dialog the dialog that shouldn't assume focus
   */
  public static void toFront(java.awt.Frame dialog) {
    setWindowState(dialog);
  }

  /**
   * Mock of method {@code java.awt.Frame.requestFocus()} to prevent component getting focus.
   *
   * @param frame the frame that should not receive focus
   */
  public static void requestFocus(java.awt.Frame frame) {
    frame.setFocusable(false);
  }

  /**
   * Helper method for mock methods to set the state of a {@code java.awt.Frame} so that it is not
   * focusable, and attempt to dispose of it.
   *
   * @param dialog the dialog to set to unfocusable, and to dispose
   */
  private static void setWindowState(java.awt.Frame dialog) {
    dialog.setFocusableWindowState(false);
    dialog.dispose();
  }
}
