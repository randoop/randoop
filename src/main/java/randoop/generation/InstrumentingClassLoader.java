package randoop.generation;

import java.util.HashMap;
import java.util.Map;

/**
 * A class loader that instruments each class that is loaded for the purpose of collecting branch
 * coverage data. The class keeps a cache of the loaded classes and ensures that each class is
 * defined at most once.
 */
public class InstrumentingClassLoader extends ClassLoader {
  /** Map from fully-qualified class name to class object. */
  private final Map<String, Class<?>> loadedClasses = new HashMap<>();

  /** Coverage tracker for instrumenting classes. */
  private final CoverageTracker coverageTracker;

  public InstrumentingClassLoader(CoverageTracker coverageTracker) {
    this.coverageTracker = coverageTracker;
  }

  /**
   * Instruments and loads the class with the given name into this {@code ClassLoader}. The
   * documentation for ClassLoader suggests that we override findClass rather than loadClass.
   * However, if we override findClass, the parent ClassLoader will first load the un-instrumented
   * version of the class under test, preventing us from instrumenting the specified class.
   *
   * @param name fully-qualified name of the class that is being loaded
   * @param resolve if true, resolve the class
   * @return the resulting {@code Class<?>} object
   * @throws ClassNotFoundException if class with name is not found
   */
  @Override
  protected Class<?> loadClass(final String name, final boolean resolve)
      throws ClassNotFoundException {
    // Check class cache first to avoid defining a class more than once.
    Class<?> loadedClass = loadedClasses.get(name);
    if (loadedClass != null) {
      return loadedClass;
    }

    // Attempt to instrument the class identified by the class name.
    final byte[] bytes = coverageTracker.instrumentClass(name);

    // The byte array is null for a class that is not under test and is not a nested class of a
    // class that is under test.
    if (bytes != null) {
      // Use the instrumented bytes to define the class.
      loadedClass = defineClass(name, bytes, 0, bytes.length);
    } else {
      loadedClass = super.loadClass(name, resolve);
    }

    loadedClasses.put(name, loadedClass);
    return loadedClass;
  }
}
