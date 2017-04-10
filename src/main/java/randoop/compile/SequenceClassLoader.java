package randoop.compile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import javax.tools.JavaFileObject;

/**
 * based on {@code javaxtools.compiler.ClassLoaderImpl} from <a
 * href="https://www.ibm.com/developerworks/library/j-jcomp/index.html">Create dynamic applications
 * with javax.tools</a>.
 */
public final class SequenceClassLoader extends ClassLoader {

  private final HashMap<String, JavaFileObject> classes;

  public SequenceClassLoader(final ClassLoader parent) {
    super(parent);
    this.classes = new HashMap<>();
  }

  @Override
  protected Class<?> findClass(final String qualifiedClassName) throws ClassNotFoundException {
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
