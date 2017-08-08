package randoop.mock.java.awt;

/**
 * Class of mock methods for {@code java.awt.Dialog}.
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class Dialog {

  /**
   * Mock of {@code java.awt.Dialog.pack()} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   */
  public static void pack(java.awt.Dialog dialog) {
    setDialogState(dialog);
  }

  /**
   * Mock of {@code java.awt.Dialog.setVisible(boolean)} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void setVisible(java.awt.Dialog dialog, boolean visible) {
    setDialogState(dialog);
  }

  /**
   * Mock of {@code java.awt.Dialog.show(boolean)} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   * @param visible the visibility state flag, ignored by this method
   */
  public static void show(java.awt.Dialog dialog, boolean visible) {
    setDialogState(dialog);
  }

  /**
   * Mock of {@code java.awt.Dialog.show()} to avoid triggering display events.
   *
   * @param dialog the dialog that should not be displayed
   */
  public static void show(java.awt.Dialog dialog) {
    setDialogState(dialog);
  }

  /**
   * Mock of {@code java.awt.Dialog.toFront()} to avoid scenario where a dialog could assume focus
   * by moving to the front (this only applies to some windowing systems).
   *
   * @param dialog the dialog that shouldn't assume focus
   */
  public static void toFront(java.awt.Dialog dialog) {
    setDialogState(dialog);
  }

  /**
   * Mock of method {@code java.awt.Dialog.requestFocus()} to prevent component getting focus.
   *
   * @param dialog the dialog that should not receive focus
   */
  public static void requestFocus(java.awt.Dialog dialog) {
    dialog.setFocusable(false);
  }

  /**
   * Helper method for mock methods to set the state of a {@code java.awt.Dialog} so that it is not
   * focusable, and attempt to dispose of it.
   *
   * @param dialog the dialog to set to unfocusable, and to dispose
   */
  private static void setDialogState(java.awt.Dialog dialog) {
    dialog.setFocusableWindowState(false);
    dialog.dispose();
  }
}
