package randoop.test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.compile.SequenceClassLoader;
import randoop.compile.SequenceCompiler;
import randoop.main.GenTests;
import randoop.output.JUnitCreator;
import randoop.output.NameGenerator;
import randoop.sequence.ExecutableSequence;

/** {@code TestPredicate} that checks whether the given {@link ExecutableSequence} is compilable. */
public class CompilableTestPredicate implements Predicate<ExecutableSequence> {
  /** The compiler for sequence code */
  private final SequenceCompiler compiler;

  /**
   * The {@link randoop.output.JUnitCreator} to generate a class from a {@link
   * randoop.sequence.ExecutableSequence}
   */
  private final JUnitCreator junitCreator;

  /** The name generator for temporary class names */
  private final NameGenerator nameGenerator;

  /** The {@link GenTests} instance that created this predicate */
  private final GenTests genTests;

  /**
   * Creates a predicate using the given {@link JUnitCreator} to construct the test class for each
   * sequence.
   *
   * @param junitCreator the {@link JUnitCreator} for this Randoop run
   * @param genTests the {@link GenTests} instance to report compilation failures
   */
  public CompilableTestPredicate(JUnitCreator junitCreator, GenTests genTests) {
    SequenceClassLoader sequenceClassLoader = new SequenceClassLoader(getClass().getClassLoader());
    List<String> options = new ArrayList<>();
    // only need to know an error exists:
    options.add("-Xmaxerrs");
    options.add("1");
    // no class generation:
    options.add("-implicit:none");
    // no annotation processing: (note that -proc:only does not produce correct results)
    options.add("-proc:none");
    // no debugging information:
    options.add("-g:none");
    // no warnings:
    options.add("-Xlint:none");
    this.compiler = new SequenceCompiler(sequenceClassLoader, options);
    this.junitCreator = junitCreator;
    this.nameGenerator = new NameGenerator("RandoopTemporarySeqTest");
    this.genTests = genTests;
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
    String packageName = pkg == null ? null : pkg.getPackageName();
    boolean result = testSource(testClassName, source, packageName);
    if (!result && genTests != null) {
      // get result from last line of sequence
      ExecutionOutcome sequenceResult = sequence.getResult(sequence.size() - 1);
      if (sequenceResult instanceof ExceptionalExecution) {
        if (((ExceptionalExecution) sequenceResult).getException()
            instanceof randoop.util.TimeoutExceededException) {
          // Do not count TimeoutExceeded as a CompileFailure.
          return result;
        }
      }
      genTests.countSequenceCompileFailure();
    }
    return result;
  }

  /**
   * Test the source text directly. This is here to allow the mechanics of the predicate to be
   * tested directly. Otherwise, we have to create a broken {@link ExecutableSequence}, which may
   * not always be possible.
   *
   * @param testClassName the name of the test class
   * @param source the source text for the class
   * @param packageName the package name for the test, null if no package
   * @return true if the code compiles (without error), false otherwise
   */
  boolean testSource(String testClassName, CompilationUnit source, String packageName) {
    String sourceText = source.toString();
    return compiler.isCompilable(packageName, testClassName, sourceText);
  }
}
