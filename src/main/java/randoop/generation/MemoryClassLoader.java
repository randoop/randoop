package randoop.generation;

import java.util.HashMap;
import java.util.Map;

/** A class loader that loads classes from in-memory data. */
public class MemoryClassLoader extends ClassLoader {
  /** Map from fully-qualified class name to class object. */
  private final Map<String, Class<?>> loadedClasses = new HashMap<>();

  /** Coverage tracker for instrumenting classes. */
  private final CoverageTracker coverageTracker;

  public MemoryClassLoader(CoverageTracker coverageTracker) {
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
    // Check class cache first.
    Class<?> loadedClass = loadedClasses.get(name);
    if (loadedClass != null) {
      return loadedClass;
    }

    // Attempt to instrument the class identified by the class name.
    final byte[] bytes = coverageTracker.instrumentClass(name);
    if (bytes != null) {
      loadedClass = defineClass(name, bytes, 0, bytes.length);
    } else {
      loadedClass = super.loadClass(name, resolve);
    }

    loadedClasses.put(name, loadedClass);
    return loadedClass;
  }
}
