package randoop.compile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import javax.tools.JavaFileObject;
import org.checkerframework.checker.signature.qual.BinaryName;

/**
 * A {@code ClassLoader} for loading classes managed in memory.
 *
 * <p>Based on {@code javaxtools.compiler.ClassLoaderImpl} from <a
 * href="https://www.ibm.com/developerworks/library/j-jcomp/index.html">Create dynamic applications
 * with javax.tools</a>.
 */
public final class SequenceClassLoader extends ClassLoader {

  /** The map from fully-qualified class name to the class object. */
  private final HashMap<String, JavaFileObject> classes;

  /**
   * Creates a {@link SequenceClassLoader} that forwards to the parent loader.
   *
   * @param parent the class loader to call if a class is not found in this loader
   */
  public SequenceClassLoader(final ClassLoader parent) {
    super(parent);
    this.classes = new HashMap<>();
  }

  /**
   * Returns the class for the qualified class name.
   *
   * @param qualifiedClassName the fully-qualified name of the class
   * @return the {@code Class<?>} object for the class with the name
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  protected Class<?> findClass(final @BinaryName String qualifiedClassName)
      throws ClassNotFoundException {
    JavaFileObject file = classes.get(qualifiedClassName);
    if (file != null) {
      byte[] bytes = ((SequenceJavaFileObject) file).getByteCode();
      return defineClass(qualifiedClassName, bytes, 0, bytes.length);
    }
    // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6434149
    try {
      return Class.forName(qualifiedClassName);
    } catch (ClassNotFoundException nf) {
      // Ignore and fall through
    }
    return super.findClass(qualifiedClassName);
  }

  /**
   * Add the class to this {@code ClassLoader}.
   *
   * @param qualifiedName the name of the class
   * @param file the class file object
   */
  void add(String qualifiedName, JavaFileObject file) {
    classes.put(qualifiedName, file);
  }

  @Override
  public InputStream getResourceAsStream(final String name) {
    if (name.endsWith(".class")) {
      String qualifiedClassName =
          name.substring(0, name.length() - ".class".length()).replace('/', '.');
      SequenceJavaFileObject file = (SequenceJavaFileObject) classes.get(qualifiedClassName);
      if (file != null) {
        return new ByteArrayInputStream(file.getByteCode());
      }
    }
    return super.getResourceAsStream(name);
  }
}
