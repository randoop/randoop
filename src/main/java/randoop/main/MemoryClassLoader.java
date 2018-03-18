package randoop.main;

import java.util.HashMap;
import java.util.Map;

/** A class loader that loads classes from in-memory data. */
public class MemoryClassLoader extends ClassLoader {
  /** Map from fully-qualified class name to the byte representation of the class. */
  private final Map<String, byte[]> definitions = new HashMap<>();

  /** Map from fully-qualified class name to class object. */
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

  /**
   * Loads the class with the given name into this ClassLoader.
   *
   * @param name name of the class that is being loaded.
   * @param resolve if true, resolve the class.
   * @return the resulting {@code Class<?>} object
   * @throws ClassNotFoundException if class with name is not found.
   */
  @Override
  protected Class<?> loadClass(final String name, final boolean resolve)
      throws ClassNotFoundException {
    Class<?> loadedClass = loadedClasses.get(name);
    if (loadedClass != null) {
      return loadedClass;
    }

    final byte[] bytes = definitions.get(name);
    if (bytes != null) {
      loadedClass = defineClass(name, bytes, 0, bytes.length);
    } else {
      loadedClass = super.loadClass(name, resolve);
    }

    loadedClasses.put(name, loadedClass);

    return loadedClass;
  }
}
