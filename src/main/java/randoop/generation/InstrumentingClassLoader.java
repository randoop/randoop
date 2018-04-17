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
   * Instruments and loads the class with the given name into this {@code ClassLoader}.
   *
   * @param name name of the class that is being loaded
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

    // Check if the returned byte array is null. The byte array will be null for a class that
    // is not either explicitly under test or is not a nested class of a class that is
    // explicitly under test.
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
