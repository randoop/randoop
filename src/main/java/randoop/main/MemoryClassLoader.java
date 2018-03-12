package randoop.main;

import java.util.HashMap;
import java.util.Map;

/** A class loader that loads classes from in-memory data. */
public class MemoryClassLoader extends ClassLoader {
  // Map from class name to the byte representation of the class.
  private final Map<String, byte[]> definitions = new HashMap<>();

  private final Map<String, Class<?>> loadedClasses = new HashMap<>();

  /**
   * Add a in-memory representation of a class.
   *
   * @param name name of the class
   * @param bytes class definition
   */
  public void addDefinition(final String name, final byte[] bytes) {
    definitions.put(name, bytes);
  }

  @Override
  protected Class<?> loadClass(final String name, final boolean resolve)
      throws ClassNotFoundException {
    Class<?> loadedClass = loadedClasses.get(name);
    if (loadedClass != null) {
      System.out.println("LOADED " + name);
      return loadedClass;
    }
    System.out.println("LOADING " + name);

    final byte[] bytes = definitions.get(name);
    if (bytes != null) {
      loadedClass = defineClass(name, bytes, 0, bytes.length);
    } else {
      try {
        loadedClass = CoverageTracker.instance.getInstrumentedClass(name);
      } catch (SecurityException e) {
        loadedClass = null;
      }
      if (loadedClass == null) {
        loadedClass = super.loadClass(name, resolve);
      }
    }

    loadedClasses.put(name, loadedClass);
    return loadedClass;
  }
}
