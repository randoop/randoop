package randoop.compile;

import java.util.ArrayList;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * Compiles a Java class given as a {@code String}.
 *
 * <p>A simplified version of the {@code javaxtools.compiler.CharSequenceCompiler} from <a
 * href="https://www.ibm.com/developerworks/library/j-jcomp/index.html">Create dynamic applications
 * with javax.tools</a>.
 */
public class SequenceCompiler {

  /** The {@code ClassLoader} for this compiler. */
  private final SequenceClassLoader classLoader;

  /** the options to the compiler */
  private final List<String> options;

  /** the Java compiler */
  private final JavaCompiler compiler;

  /** The {@code FileManager} for this compiler. */
  private final SequenceJavaFileManager fileManager;

  /**
   * Creates a {@link SequenceCompiler} with the given {@code ClassLoader}, options list, and {@code
   * DiagnosticsCollector}.
   *
   * @param classLoader the class loader for this compiler
   * @param options the compiler options
   */
  public SequenceCompiler(SequenceClassLoader classLoader, List<String> options) {
    this.classLoader = classLoader;
    this.options = new ArrayList<>(options);
    this.compiler = ToolProvider.getSystemJavaCompiler();

    if (this.compiler == null) {
      throw new IllegalStateException(
          "Cannot find the Java compiler. Check that classpath includes tools.jar");
    }

    JavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
    this.fileManager = new SequenceJavaFileManager(standardFileManager, classLoader);
  }

  /**
   * Indicates whether the given class is compilable.
   *
   * @param packageName the package name for the class, null if default package
   * @param classname the simple name of the class
   * @param javaSource the source text of the class
   * @return true if class source was successfully compiled, false otherwise
   */
  public boolean isCompilable(
      final String packageName, final String classname, final String javaSource) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    return compile(packageName, classname, javaSource, diagnostics);
  }

  /**
   * Compiles the given class. If this method returns normally, compilation was successful.
   *
   * @param packageName the package of the class, null if default package
   * @param classname the simple name of the class
   * @param javaSource the source text of the class
   * @throws SequenceCompilerException if the compilation fails
   */
  public void compile(final String packageName, final String classname, final String javaSource)
      throws SequenceCompilerException {

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    boolean success = compile(packageName, classname, javaSource, diagnostics);
    if (!success) {
      throw new SequenceCompilerException("Compilation failed", javaSource, diagnostics);
    }
  }

  /**
   * A helper method for the {@link #compile(String, String, String)} and {@link
   * #isCompilable(String, String, String)} methods: compiles the given class using the given
   * diagnostics collector.
   *
   * @param packageName the package of the class, null if default package
   * @param classname the simple name of the class
   * @param javaSource the source text of the class
   * @param diagnostics the {@code DiagnosticsCollector} object to use for the compilation. Always
   *     use a new diagnostics collector each compilation to avoid accumulating errors.
   * @return true if the class source is successfully compiled, false otherwise
   */
  private boolean compile(
      final String packageName,
      final String classname,
      final String javaSource,
      DiagnosticCollector<JavaFileObject> diagnostics) {
    String classFileName = classname + CompileUtil.JAVA_EXTENSION;
    List<JavaFileObject> sources = new ArrayList<>();
    JavaFileObject source = new SequenceJavaFileObject(classFileName, javaSource);
    sources.add(source);
    fileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName, classFileName, source);
    JavaCompiler.CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, options, null, sources);
    Boolean succeeded = task.call();
    return (succeeded != null && succeeded);
  }

  /**
   * Loads the {@code Class<T>} object for the named class.
   *
   * @param packageName the package of the class, null if default package
   * @param classname the name of the class, without the package
   * @param <T> the class type
   * @return the {@code Class<T>} object with the class name
   * @throws ClassNotFoundException if the class cannot be loaded
   */
  @SuppressWarnings({
    "unchecked",
    "signature" // string concatenation
  })
  public <T> Class<T> loadClass(String packageName, String classname)
      throws ClassNotFoundException {
    String qualifiedName = (packageName == null ? "" : (packageName + ".")) + classname;
    return (Class<T>) classLoader.loadClass(qualifiedName);
  }
}
