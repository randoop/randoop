package randoop.test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import randoop.compile.SequenceClassLoader;
import randoop.compile.SequenceCompiler;
import randoop.compile.SequenceCompilerException;
import randoop.output.JUnitCreator;
import randoop.output.NameGenerator;
import randoop.sequence.ExecutableSequence;
import randoop.util.predicate.DefaultPredicate;

/** {@code TestPredicate} that checks whether the given {@link ExecutableSequence} is compilable. */
public class CompilableTestPredicate extends DefaultPredicate<ExecutableSequence> {
  /** The compiler for sequence code */
  private final SequenceCompiler compiler;

  /**
   * The {@link randoop.output.JUnitCreator} to generate a class from a {@link
   * randoop.sequence.ExecutableSequence}
   */
  private final JUnitCreator junitCreator;

  /** The name generator for temporary class names */
  private final NameGenerator nameGenerator;

  /**
   * Creates a predicate using the given {@link JUnitCreator} to construct the test class for each
   * sequence.
   *
   * @param junitCreator the {@link JUnitCreator} for this Randoop run
   */
  public CompilableTestPredicate(JUnitCreator junitCreator) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    SequenceClassLoader sequenceClassLoader = new SequenceClassLoader(getClass().getClassLoader());
    List<String> options = new ArrayList<>();
    options.add("-Xmaxerrs");
    options.add("1000");
    this.compiler = new SequenceCompiler(sequenceClassLoader, options, diagnostics);
    this.junitCreator = junitCreator;
    this.nameGenerator = new NameGenerator("RandoopTemporarySeqTest");
  }

  /**
   * {@inheritDoc}
   *
   * <p>Indicate whether the given sequence is compilable.
   *
   * @return true if the sequence can be compiled, false otherwise
   */
  @Override
  public boolean test(ExecutableSequence sequence) {
    String testClassName = nameGenerator.next();
    String methodNamePrefix = "test";
    List<ExecutableSequence> sequences = new ArrayList<>();
    sequences.add(sequence);
    CompilationUnit source =
        junitCreator.createTestClass(testClassName, methodNamePrefix, sequences);
    PackageDeclaration pkg = source.getPackage();
    String packageName = "";
    if (pkg != null) {
      packageName = pkg.getPackageName();
    }

    try {
      compiler.compile(packageName, testClassName, source.toString());
    } catch (SequenceCompilerException e) {
      for (Diagnostic<? extends JavaFileObject> diagnostic : e.getDiagnostics().getDiagnostics()) {
        if (diagnostic != null) {
          if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
