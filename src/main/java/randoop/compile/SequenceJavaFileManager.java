package randoop.compile;

import static randoop.compile.CompileUtil.toURI;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

/**
 * A {@code ForwardingJavaFileManager} to maintain class files in memory.
 *
 * <p>Based on {@code javaxtools.compiler.JavaFileObjectImple} from <a
 * href="https://www.ibm.com/developerworks/library/j-jcomp/index.html">Create dynamic applications
 * with javax.tools</a>.
 */
public class SequenceJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

  /** The class loader for managing Java classes in memory. */
  private final SequenceClassLoader classLoader;

  /** The map from the location of a class file to the class object. */
  private final HashMap<URI, JavaFileObject> fileObjects;

  /**
   * Create a {@link SequenceJavaFileManager} that manages class files in memory. When searching for
   * a class file, will forward to the given file manager if not located in this manager.
   *
   * @param fileManager the file manager to which calls are forwarded
   * @param classLoader the class loader to which loaded classes are added
   */
  SequenceJavaFileManager(JavaFileManager fileManager, SequenceClassLoader classLoader) {
    super(fileManager);
    this.classLoader = classLoader;
    this.fileObjects = new HashMap<>();
  }

  /**
   * Returns the class file indicated by the combination of the location, package name and relative
   * name. (See the documentation for {@code ForwardingJavaFileManager} for details.)
   *
   * @param location the location (path) for the file
   * @param packageName the package name for the class, null if default package
   * @param relativeName the name relative to the package
   * @return the file object, or null if the file does not exist
   * @throws IOException if an I/O error occurred, or the file manager has been closed and cannot be
   *     reopened
   */
  @Override
  public FileObject getFileForInput(Location location, String packageName, String relativeName)
      throws IOException {
    FileObject obj = fileObjects.get(uri(location, packageName, relativeName));
    if (obj != null) {
      return obj;
    }
    return super.getFileForInput(location, packageName, relativeName);
  }

  /**
   * Gets a file object for input for the class, of the kind, at the indicated location.
   *
   * @param location the location
   * @param qualifiedName the fully-qualified name of the class
   * @param kind the kind of file (either {@code SOURCE} or {@code CLASS})
   * @param outputFile a file used as a hint for placement; may be null
   * @return the file object, or null if the file does not exist
   * @throws IOException if an I/O error occurred, or the file manager has been closed and cannot be
   *     reopend.
   */
  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location, String qualifiedName, Kind kind, FileObject outputFile)
      throws IOException {
    JavaFileObject file = new SequenceJavaFileObject(qualifiedName, kind);
    classLoader.add(qualifiedName, file);
    return file;
  }

  /**
   * Adds the source file object to this file manager.
   *
   * @param sourcePath the path for the source
   * @param packageName the package name for the class, null if default package
   * @param classFileName the name of the class
   * @param source the source file object
   */
  void putFileForInput(
      StandardLocation sourcePath,
      String packageName,
      String classFileName,
      JavaFileObject source) {
    fileObjects.put(uri(sourcePath, packageName, classFileName), source);
  }

  /**
   * Creates the path to a file and converts it to a {@code URI}.
   *
   * @param sourcePath the path for the source directory
   * @param packageName the name of the class package, null if no package
   * @param classFileName the name of the class
   * @return the {@code URI} for the file
   */
  private URI uri(Location sourcePath, String packageName, String classFileName) {
    return toURI(
        sourcePath.getName()
            + '/'
            + (packageName == null ? "" : (packageName + '/'))
            + classFileName);
  }
}
