package randoop.mock.java.awt;

/**
 * Class of mock methods for {@code java.awt.Component}
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class Component {
  public static void setVisible(java.awt.Component component, boolean visible) {
    // can't call dispose on object, so not clear what to do -- do nothing
  }

  public static void show(java.awt.Component component, boolean visible) {}

  public static void show(java.awt.Component component) {}

  /**
   * Mock of method {@code java.awt.Component.requestFocus()} to prevent component getting focus.
   *
   * @param component the component that should not receive focus
   */
  public static void requestFocus(java.awt.Component component) {
    component.setFocusable(false);
  }

  /**
   * Mock of method {@code java.awt.Component.requestFocus()} to prevent component getting focus.
   *
   * @param component the component that should not receive focus
   * @param temporary flag that indicates that focus should be temporary (ignored)
   */
  public static void requestFocus(java.awt.Component component, boolean temporary) {
    component.setFocusable(false);
  }
}
