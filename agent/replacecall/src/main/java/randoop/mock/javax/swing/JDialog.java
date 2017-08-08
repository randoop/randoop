package randoop.mock.javax.swing;

/**
 * Class of mock methods for {@code javax.swing.JDialog}.
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class JDialog {
  /**
   * Mock of {@code javax.swing.JDialog.pack()} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   */
  public static void pack(javax.swing.JDialog dialog) {
    dialog.dispose();
  }

  /**
   * Mock of {@code javax.swing.JDialog.setVisible(boolean)} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void setVisible(javax.swing.JDialog dialog, boolean visible) {
    dialog.dispose();
  }

  /**
   * Mock of {@code javax.swing.JDialog.show(boolean)} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void show(javax.swing.JDialog dialog, boolean visible) {
    dialog.dispose();
  }

  /**
   * Mock of {@code javax.swing.JDialog.show()} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   */
  public static void show(javax.swing.JDialog dialog) {
    dialog.dispose();
  }

  /**
   * Mock of {@code javax.swing.JDialog.toFront()} to avoid scenario where a window could assume
   * focus by moving to the front (this only applies to some windowing systems).
   *
   * @param dialog the dialog that shouldn't assume focus
   */
  public static void toFront(javax.swing.JDialog dialog) {
    setWindowState(dialog);
  }

  /**
   * Mock of method {@code javax.swing.JDialog.requestFocus()} to prevent component getting focus.
   *
   * @param dialog the dialog that should not receive focus
   */
  public static void requestFocus(javax.swing.JDialog dialog) {
    dialog.setFocusable(false);
  }

  /**
   * Helper method for mock methods to set the state of a {@code javax.swing.JDialog} so that it is
   * not focusable, and attempt to dispose of it.
   *
   * @param dialog the window to set to unfocusable, and to dispose
   */
  private static void setWindowState(javax.swing.JDialog dialog) {
    dialog.setFocusableWindowState(false);
    dialog.dispose();
  }
}
