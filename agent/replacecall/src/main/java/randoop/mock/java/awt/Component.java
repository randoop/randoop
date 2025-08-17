package randoop.mock.java.awt;

/**
 * Class of mock methods for {@code java.awt.Component}
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class Component {

  /** Creates a (mock) Component. */
  public Component() {}

  /**
   * Shows or hides this component depending on the value of parameter {@code b}.
   *
   * <p>This method changes layout-related information, and therefore, invalidates the component
   * hierarchy.
   *
   * @param component a component
   * @param visible if {@code true}, shows this component; otherwise, hides this component
   */
  public static void setVisible(java.awt.Component component, boolean visible) {
    // can't call dispose on object, so not clear what to do -- do nothing
  }

  /**
   * Makes this component visible or invisible.
   *
   * @param component a component
   * @param visible {@code true} to make this component visible; otherwise {@code false}
   * @deprecated As of JDK version 1.1, replaced by {@code setVisible(boolean)}.
   */
  @Deprecated
  public static void show(java.awt.Component component, boolean visible) {}

  /**
   * Shows or hides this component depending on the value of parameter {@code b}.
   *
   * @param component a component
   * @deprecated As of JDK version 1.1, replaced by {@code setVisible(boolean)}.
   */
  @Deprecated
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
