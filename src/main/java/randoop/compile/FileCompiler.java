package randoop.compile;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/** Compiler for Java classes files. */
public class FileCompiler {

  /** The options for running this compiler. */
  private final List<String> options;

  /** The compiler object */
  private final JavaCompiler compiler;

  /** Creates a {@link FileCompiler} with no options. */
  public FileCompiler() {
    this(new ArrayList<String>());
  }

  /**
   * Creates a {@link FileCompiler} with the given options.
   *
   * <p>See documentation for <a
   * href="https://docs.oracle.com/javase/7/docs/api/javax/tools/JavaCompiler.html">JavaCompiler</a>
   * for details on the compiler options.
   *
   * @param options the command-line arguments for the {@code JavaCompiler}
   */
  public FileCompiler(List<String> options) {
    this.options = options;
    this.compiler = ToolProvider.getSystemJavaCompiler();
  }

  /**
   * Compile the given source files, writing resulting class files to the destination directory.
   *
   * @param sourceFiles the list of {@code File} objects for the Java source files
   * @param destinationDir the {@code Path} of the destination directory for class files
   * @return {@code true} if compilation succeeds and the method returns normally
   * @throws FileCompilerException if the compilation fails
   */
  public boolean compile(List<File> sourceFiles, Path destinationDir) throws FileCompilerException {
    // Set the destination directory for the compiler
    options.add("-d");
    options.add(destinationDir.toString());

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
    Iterable<? extends JavaFileObject> filesToCompile =
        fileManager.getJavaFileObjectsFromFiles(sourceFiles);

    JavaCompiler.CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, options, null, filesToCompile);

    Boolean succeeded = task.call();
    if (succeeded == null || !succeeded) {
      throw new FileCompilerException("Compilation failed.", sourceFiles, diagnostics);
    }
    return true;
  }
}
