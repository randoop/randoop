package randoop.compile;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import randoop.sequence.Sequence;

/**
 * Compiles a {@link Sequence}
 */
public class SequenceCompiler {

  private final JavaCompiler compiler;
  private final List<String> options;
  private Writer writer;
  private JavaFileManager fileManager;

  public SequenceCompiler(ClassLoader classLoader, List<String> options) {
    this.compiler = ToolProvider.getSystemJavaCompiler();
    if (this.compiler == null) {
      throw new IllegalStateException(
          "Cannot find the Java compiler. " + "Check that classpath includes tools.jar");
    }
    DiagnosticListener<? super JavaFileObject> diagnostics = null; // here to make this compile
    JavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
    fileManager = new SequenceJavaFileManager(standardFileManager, classLoader);
    this.options = new ArrayList<String>(options);
  }

  public <T> Class<T> compile(
      final String classname,
      final Sequence sequence,
      final DiagnosticCollector<JavaFileObject> diagnostics)
      throws SequenceCompilerException {

    Iterable<? extends JavaFileObject> sources = new ArrayList<>();
    JavaCompiler.CompilationTask task =
        compiler.getTask(writer, fileManager, diagnostics, options, null, sources);
    Boolean succeeded = task.call();
    if (succeeded == null || !succeeded) {
      throw new SequenceCompilerException("Compilation failed.", sequence, diagnostics);
    }

    return null;
  }
}
