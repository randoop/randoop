package randoop.compile;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import static randoop.compile.CompileUtil.toURI;

/**
 * based on {@code javaxtools.compiler.JavaFileObjectImple}  from <a href="http://www.ibm.com/developerworks/library/j-jcomp/index.html">Create dynamic applications with javax.tools</a>.
 */
public class SequenceJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

  private final SequenceClassLoader classLoader;
  private final HashMap<URI, JavaFileObject> fileObjects;

  SequenceJavaFileManager(JavaFileManager fileManager, SequenceClassLoader classLoader) {
    super(fileManager);
    this.classLoader = classLoader;
    this.fileObjects = new HashMap<>();
  }

  @Override
  public FileObject getFileForInput(Location location, String packageName, String relativeName)
      throws IOException {
    FileObject obj = fileObjects.get(uri(location, packageName, relativeName));
    if (obj != null) {
      return obj;
    }
    return super.getFileForInput(location, packageName, relativeName);
  }

  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location, String qualifiedName, Kind kind, FileObject outputFile)
      throws IOException {
    JavaFileObject file = new SequenceJavaFileObject(qualifiedName, kind);
    classLoader.add(qualifiedName, file);
    return file;
  }

  void putFileForInput(
      StandardLocation sourcePath,
      String packageName,
      String classFileName,
      JavaFileObject source) {
    fileObjects.put(uri(sourcePath, packageName, classFileName), source);
  }

  private URI uri(Location sourcePath, String packageName, String classFileName) {
    return toURI(sourcePath.getName() + '/' + packageName + '/' + classFileName);
  }
}
