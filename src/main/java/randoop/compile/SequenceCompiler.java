package randoop.compile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.checkerframework.checker.calledmethods.qual.EnsuresCalledMethods;
import org.checkerframework.checker.mustcall.qual.MustCall;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.checker.signature.qual.BinaryName;
import org.checkerframework.checker.signature.qual.BinaryNameWithoutPackage;
import org.checkerframework.checker.signature.qual.DotSeparatedIdentifiers;
import org.plumelib.reflection.ReflectionPlume;
import randoop.Globals;
import randoop.main.RandoopBug;
import randoop.main.RandoopUsageError;

/**
 * Compiles a Java class given as a {@code String}.
 *
 * <p>A simplified version of the {@code javaxtools.compiler.CharSequenceCompiler} from <a
 * href="http://web.archive.org/web/20170202133304/https://www.ibm.com/developerworks/library/j-jcomp/index.html">Create
 * dynamic applications with javax.tools</a>.
 */
@MustCall("close") public class SequenceCompiler implements Closeable {

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
  private final @Owning JavaFileManager fileManager;

  /** Creates a {@link SequenceCompiler}. */
  public SequenceCompiler() {
    this(new ArrayList<String>(0));
  }

  /**
   * Creates a {@link SequenceCompiler}.
   *
   * @param compilerOptions the compiler options
   */
  public SequenceCompiler(List<String> compilerOptions) {
    this.compilerOptions = new ArrayList<>(compilerOptions.size() + 3);
    this.compilerOptions.addAll(compilerOptions);
    this.compilerOptions.add("-XDuseUnsharedTable");
    this.compilerOptions.add("-d");
    this.compilerOptions.add(".");
    this.compiler = ToolProvider.getSystemJavaCompiler();

    if (this.compiler == null) {
      throw new RandoopUsageError(
          "Cannot find the Java compiler. Check that classpath includes tools.jar."
              + Globals.lineSep
              + "Classpath:"
              + Globals.lineSep
              + ReflectionPlume.classpathToString());
    }

    this.fileManager = compiler.getStandardFileManager(null, null, null);
  }

  /** Releases any system resources associated with this. */
  @EnsuresCalledMethods(value = "fileManager", methods = "close")
  @Override
  public void close() throws IOException {
    fileManager.close();
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

    // Compilation can create multiple .class files; this only deletes the main one.
    Path dir = Paths.get((packageName == null) ? "." : packageName.replace(".", "/"));
    try {
      Files.delete(dir.resolve(classname + ".class"));
    } catch (NoSuchFileException e) {
      // Nothing to do, but I wonder why the file doesn't exist.
    } catch (IOException e) {
      System.out.printf(
          "Unable to delete %s: %s%n", dir.resolve(classname + ".class").toAbsolutePath(), e);
    }

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
    String classFileName = classname + ".java";
    List<JavaFileObject> sources = new ArrayList<>(1);
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
      final @BinaryNameWithoutPackage String classname,
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
   * @param className the binary name of the class defined in the file
   * @return the loaded Class object
   */
  private static Class<?> loadClassFile(File directory, @BinaryName String className) {
    try (URLClassLoader cl = new URLClassLoader(new URL[] {directory.toURI().toURL()})) {
      Class<?> cls = cl.loadClass(className);
      return cls;
    } catch (ClassNotFoundException | NoClassDefFoundError | IOException e) {
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
      @DotSeparatedIdentifiers String packageName, @BinaryNameWithoutPackage String classname) {
    @SuppressWarnings("signature:assignment") // string concatenation
    @BinaryName String result = (packageName == null ? "" : (packageName + ".")) + classname;
    return result;
  }
}
