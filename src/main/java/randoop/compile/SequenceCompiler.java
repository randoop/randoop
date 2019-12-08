package randoop.compile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.checkerframework.checker.signature.qual.BinaryName;
import org.checkerframework.checker.signature.qual.BinaryNameInUnnamedPackage;
import org.checkerframework.checker.signature.qual.DotSeparatedIdentifiers;
import randoop.Globals;
import randoop.main.RandoopBug;

/**
 * Compiles a Java class given as a {@code String}.
 *
 * <p>A simplified version of the {@code javaxtools.compiler.CharSequenceCompiler} from <a
 * href="https://www.ibm.com/developerworks/library/j-jcomp/index.html">Create dynamic applications
 * with javax.tools</a>.
 */
public class SequenceCompiler {

  /**
   * If non-null, do verbose output for compilation failures where the Java source code contains the
   * string.
   */
  private static final String debugCompilationFailure = null;

  /** The options to the compiler. */
  private final List<String> compilerOptions;

  /** the Java compiler */
  private final JavaCompiler compiler;

  /** The {@code FileManager} for this compiler. */
  private final JavaFileManager fileManager;

  /** Creates a {@link SequenceCompiler}. */
  public SequenceCompiler() {
    this(new ArrayList<String>());
  }

  /**
   * Creates a {@link SequenceCompiler}.
   *
   * @param compilerOptions the compiler options
   */
  public SequenceCompiler(List<String> compilerOptions) {
    this.compilerOptions = new ArrayList<>(compilerOptions);
    this.compilerOptions.add("-XDuseUnsharedTable");
    this.compilerOptions.add("-d");
    this.compilerOptions.add(".");
    this.compiler = ToolProvider.getSystemJavaCompiler();

    if (this.compiler == null) {
      throw new IllegalStateException(
          "Cannot find the Java compiler. Check that classpath includes tools.jar");
    }

    this.fileManager = compiler.getStandardFileManager(null, null, null);
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
    boolean result = compile(packageName, classname, javaSource, diagnostics);
    if (!result
        && debugCompilationFailure != null
        && javaSource.contains(debugCompilationFailure)) {
      StringJoiner sj = new StringJoiner(Globals.lineSep);
      sj.add("isCompilable => false");
      for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
        sj.add(d.toString());
      }
      sj.add(javaSource);
      System.out.println(sj.toString());
    }
    return result;
  }

  /**
   * Compiles the given class. If this method returns normally, compilation was successful.
   *
   * @param packageName the package of the class, null if default package
   * @param classname the simple name of the class
   * @param javaSource the source text of the class
   * @throws SequenceCompilerException if the compilation fails
   */
  private void compile(final String packageName, final String classname, final String javaSource)
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
  @SuppressWarnings("UnusedVariable") // TODO: remove packageName formal parameter
  private boolean compile(
      final String packageName,
      final String classname,
      final String javaSource,
      DiagnosticCollector<JavaFileObject> diagnostics) {
    String classFileName = classname + CompileUtil.JAVA_EXTENSION;
    List<JavaFileObject> sources = new ArrayList<>();
    JavaFileObject source = new SequenceJavaFileObject(classFileName, javaSource);
    sources.add(source);
    JavaCompiler.CompilationTask task =
        compiler.getTask(
            null, fileManager, diagnostics, new ArrayList<String>(compilerOptions), null, sources);
    Boolean succeeded = task.call();
    return (succeeded != null && succeeded);
  }

  /**
   * Compiles the given class, leads it, and returns the Class object. If this method returns
   * normally, compilation was successful.
   *
   * @param packageName the package of the class, null if default package
   * @param classname the simple name of the class
   * @param javaSource the source text of the class
   * @throws SequenceCompilerException if the compilation fails
   * @return the loaded Class object
   */
  public Class<?> compileAndLoad(
      final @DotSeparatedIdentifiers String packageName,
      final @BinaryNameInUnnamedPackage String classname,
      final String javaSource)
      throws SequenceCompilerException {
    compile(packageName, classname, javaSource);
    String fqName = fullyQualifiedName(packageName, classname);
    File dir = new File("").getAbsoluteFile();
    return loadClassFile(dir, fqName);
  }

  /**
   * Given a .class file, returns the corresponding Class object.
   *
   * @param directory the directory containing the .class file (possibly in a package-named
   *     subdirectory)
   * @param className the fully-qualified name of the class defined in the file
   * @return the loaded Class object
   */
  private static Class<?> loadClassFile(File directory, @BinaryName String className) {
    try {
      ClassLoader cl = new URLClassLoader(new URL[] {directory.toURI().toURL()});
      Class<?> cls = cl.loadClass(className);
      return cls;
    } catch (MalformedURLException | ClassNotFoundException e) {
      throw new RandoopBug(e);
    }
  }

  /**
   * Constructs a fully-qualified class name from the given package and unqualified class name.
   *
   * @param packageName the package of the class, null if default package
   * @param classname the name of the class, without the package
   * @return the fully-qualified class name constructed from the arguments
   */
  @BinaryName String fullyQualifiedName(
      @DotSeparatedIdentifiers String packageName, @BinaryNameInUnnamedPackage String classname) {
    @SuppressWarnings("signature:assignment.type.incompatible") // string concatenation
    @BinaryName String result = (packageName == null ? "" : (packageName + ".")) + classname;
    return result;
  }
}
