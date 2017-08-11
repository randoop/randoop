package randoop.mock.javax.swing;

/**
 * Class of mock methods for {@code javax.swing.JOptionPane}.
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class JOptionPane {
  public static int showConfirmDialog(java.awt.Component parent, Object message) {
    return 0;
  }

  public static int showConfirmDialog(
      java.awt.Component parent, Object message, String title, int optionType) {
    return 0;
  }

  public static int showConfirmDialog(
      java.awt.Component parent, Object message, String title, int optionType, int messageType) {
    return 0;
  }

  public static int showConfirmDialog(
      java.awt.Component parent,
      Object message,
      String title,
      int optionType,
      int messageType,
      javax.swing.Icon icon) {
    return 0;
  }

  public static String showInputDialog(Object message) {
    return "";
  }

  public static String showInputDialog(Object message, Object initialSelectionValue) {
    return "";
  }

  public static String showInputDialog(java.awt.Component parentComponent, Object message) {
    return "";
  }

  public static String showInputDialog(
      java.awt.Component parentComponent, Object message, Object initialSelectionValue) {
    return "";
  }

  public static String showInputDialog(
      java.awt.Component parentComponent, Object message, String title, int messageType) {
    return "";
  }

  public static Object showInputDialog(
      java.awt.Component parentComponent,
      Object message,
      String title,
      int messageType,
      javax.swing.Icon icon,
      Object[] selectionValues,
      Object initialSelectionValue) {
    return "";
  }

  public static void showMessageDialog(java.awt.Component parentComponent, Object message) {}

  public static void showMessageDialog(
      java.awt.Component parentComponent, Object message, String title, int messageType) {}

  public static void showMessageDialog(
      java.awt.Component parentComponent,
      Object message,
      String title,
      int messageType,
      javax.swing.Icon icon) {}

  public static int showOptionDialog(
      java.awt.Component parentComponent,
      Object message,
      String title,
      int optionType,
      int messageType,
      javax.swing.Icon icon,
      Object[] options,
      Object initialValue) {
    return 0;
  }

  public static void showInternalMessageDialog(
      java.awt.Component parentComponent, Object message) {}

  public static void showInternalMessageDialog(
      java.awt.Component parentComponent, Object message, String title, int messageType) {}

  public static void showInternalMessageDialog(
      java.awt.Component parentComponent,
      Object message,
      String title,
      int messageType,
      javax.swing.Icon icon) {}

  public static int showInternalConfirmDialog(java.awt.Component parentComponent, Object message) {
    return 0;
  }

  public static int showInternalConfirmDialog(
      java.awt.Component parentComponent, Object message, String title, int optionType) {
    return 0;
  }

  public static int showInternalConfirmDialog(
      java.awt.Component parentComponent,
      Object message,
      String title,
      int optionType,
      int messageType) {
    return 0;
  }

  public static int showInternalConfirmDialog(
      java.awt.Component parentComponent,
      Object message,
      String title,
      int optionType,
      int messageType,
      javax.swing.Icon icon) {
    return 0;
  }

  public static int showInternalOptionDialog(
      java.awt.Component parentComponent,
      Object message,
      String title,
      int optionType,
      int messageType,
      javax.swing.Icon icon,
      Object[] options,
      Object initialValue) {
    return 0;
  }

  public static String showInternalInputDialog(java.awt.Component parentComponent, Object message) {
    return "";
  }

  public static String showInternalInputDialog(
      java.awt.Component parentComponent, Object message, String title, int messageType) {
    return "";
  }

  public static Object showInternalInputDialog(
      java.awt.Component parentComponent,
      Object message,
      String title,
      int messageType,
      javax.swing.Icon icon,
      Object[] selectionValues,
      Object initialSelectionValue) {
    return "";
  }
}
