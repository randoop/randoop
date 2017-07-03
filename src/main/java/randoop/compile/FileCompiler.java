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

/** Created by bjkeller on 6/30/17. */
public class FileCompiler {

  private final List<String> options;
  private final JavaCompiler compiler;

  public FileCompiler() {
    this(new ArrayList<String>());
  }

  public FileCompiler(List<String> options) {
    this.options = options;
    this.compiler = ToolProvider.getSystemJavaCompiler();
  }

  public boolean compile(List<File> testSourceFiles, Path destinationDir)
      throws FileCompilerException {
    options.add("-d");
    options.add(destinationDir.toString());

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
    Iterable<? extends JavaFileObject> filesToCompile =
        fileManager.getJavaFileObjectsFromFiles(testSourceFiles);

    JavaCompiler.CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, options, null, filesToCompile);

    Boolean succeeded = task.call();
    if (succeeded == null || !succeeded) {
      throw new FileCompilerException("Compilation failed.", testSourceFiles, diagnostics);
    }
    return true;
  }
}
