package randoop.mock.java.awt;

/**
 * Class of mock methods for {@code java.awt.GraphicsEnvironment}.
 *
 * <p>The original is an abstract class that provides methods that manage the graphical display for
 * AWT. The relevant behavior of the class for Randoop is indicating whether the environment is
 * headless. Certain interface objects may not be created in a headless environment. Since other
 * mocks prevent the display of these objects, these methods pretend that all environments are not
 * headless.
 *
 * <p>Default replacement for {@link randoop.instrument.ReplaceCallAgent}. Replacement should be
 * specified in {@code "resources/default-replacements.txt"}
 */
public class GraphicsEnvironment {
  public static boolean isHeadless() {
    return false;
  }

  public static boolean isHeadlessInstance(java.awt.GraphicsEnvironment graphicsEnvironment) {
    return false;
  }

  public static void checkHeadless() {
    // original throws HeadlessException if isHeadless(), do nothing
  }
}
