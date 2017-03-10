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
 * A simplified version of the {@code javaxtools.compiler.CharSequenceCompiler}  from
 * <a href="http://www.ibm.com/developerworks/library/j-jcomp/index.html">Create dynamic applications with javax.tools</a>.
 */
public class SequenceCompiler {

  /** the Java compiler */
  private final JavaCompiler compiler;

  /** the options to the compiler */
  private final List<String> options;

  /** The diagnostics collector for this compiler */
  private final DiagnosticCollector<JavaFileObject> diagnostics;

  /** The {@code ClassLoader} for this compiler */
  private final SequenceClassLoader classLoader;

  /** The {@code FileManager} for this compiler */
  private SequenceJavaFileManager fileManager;

  /**
   * Creates a {@link SequenceCompiler} with the given {@code ClassLoader}, options list, and {@code DiagnosticsCollector}.
   *
   * @param classLoader  the class loader for this compiler
   * @param options  the compiler options
   * @param diagnostics  the diagnostics collector
   */
  public SequenceCompiler(
      SequenceClassLoader classLoader,
      List<String> options,
      final DiagnosticCollector<JavaFileObject> diagnostics) {
    this.classLoader = classLoader;
    this.compiler = ToolProvider.getSystemJavaCompiler();
    this.diagnostics = diagnostics;
    this.options = new ArrayList<>(options);

    if (this.compiler == null) {
      throw new IllegalStateException(
          "Cannot find the Java compiler. " + "Check that classpath includes tools.jar");
    }

    JavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
    this.fileManager = new SequenceJavaFileManager(standardFileManager, classLoader);
  }

  /**
   * Compiles the given class and returns the {@code Class<?>} object for the class.
   *
   * @param packageName  the package of the class
   * @param classname  the (unqualified) name of the class
   * @param classSource  the source text of the class
   * @param <T>  the type of the class (use a wildcard if you aren't sure)
   * @return the {@code Class<T>} object for the class
   * @throws SequenceCompilerException  if the compilation fails or the class cannot be loaded
   */
  public <T> Class<T> compile(
      final String packageName, final String classname, final String classSource)
      throws SequenceCompilerException {

    String classFileName = classname + CompileUtil.JAVA_EXTENSION;
    List<JavaFileObject> sources = new ArrayList<>();
    JavaFileObject source = new SequenceJavaFileObject(classFileName, classSource);
    sources.add(source);
    fileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName, classFileName, source);

    JavaCompiler.CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, options, null, sources);
    Boolean succeeded = task.call();
    if (succeeded == null || !succeeded) {
      throw new SequenceCompilerException("Compilation failed.", classSource, diagnostics);
    }

    Class<T> compiledClass;
    try {
      compiledClass = loadClass(packageName, classname);
    } catch (ClassNotFoundException e) {
      throw new SequenceCompilerException(
          "Could not load compiled class.", e, classSource, diagnostics);
    }

    return compiledClass;
  }

  /**
   * Loads the {@code Class<T>} object for the named class.
   *
   * @param packageName  the package of the class
   * @param classname  the name of the class
   * @param <T>  the class type
   * @return the {@code Class<T>} object with the class name
   * @throws ClassNotFoundException if the class cannot be loaded
   */
  @SuppressWarnings("unchecked")
  private <T> Class<T> loadClass(String packageName, String classname)
      throws ClassNotFoundException {
    String qualifiedName = packageName + "." + classname;
    if (packageName.isEmpty()) {
      qualifiedName = classname;
    }
    return (Class<T>) classLoader.loadClass(qualifiedName);
  }
}
