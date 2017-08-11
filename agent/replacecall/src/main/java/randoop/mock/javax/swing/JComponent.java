package randoop.mock.javax.swing;

/**
 * Class of mock methods for {@code javax.swing.JComponent}.
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class JComponent {
  public static void setVisible(javax.swing.JComponent component, boolean visible) {
    // do nothing
  }

  public static void show(javax.swing.JComponent component, boolean visible) {}

  public static void show(javax.swing.JComponent component) {}

  /**
   * Mock of method {@code javax.swing.JComponent.requestFocus()} to prevent component getting
   * focus.
   *
   * @param component the component that should not receive focus
   */
  public static void requestFocus(javax.swing.JComponent component) {
    component.setFocusable(false);
  }

  /**
   * Mock of method {@code javax.swing.JComponent.requestFocus()} to prevent component getting
   * focus.
   *
   * @param component the component that should not receive focus
   * @param temporary flag that indicates that focus should be temporary (ignored)
   */
  public static void requestFocus(javax.swing.JComponent component, boolean temporary) {
    component.setFocusable(false);
  }
}
