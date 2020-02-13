package randoop.test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import randoop.compile.SequenceCompiler;
import randoop.main.GenTests;
import randoop.output.JUnitCreator;
import randoop.output.NameGenerator;
import randoop.sequence.ExecutableSequence;
import randoop.util.Log;

/**
 * {@code TestPredicate} that returns true if the given {@link ExecutableSequence} is compilable.
 */
public class CompilableTestPredicate implements Predicate<ExecutableSequence> {
  /** The compiler for sequence code. */
  private final SequenceCompiler compiler;

  /**
   * The {@link randoop.output.JUnitCreator} to generate a class from a {@link
   * randoop.sequence.ExecutableSequence}
   */
  private final JUnitCreator junitCreator;

  /** The name generator for temporary class names. */
  private final NameGenerator classNameGenerator;

  /** The name generator for test method names. */
  private final NameGenerator methodNameGenerator;

  /** The {@link GenTests} instance that created this predicate. */
  private final GenTests genTests;

  /**
   * Creates a predicate using the given {@link JUnitCreator} to construct the test class for each
   * sequence.
   *
   * @param junitCreator the {@link JUnitCreator} for this Randoop run
   * @param genTests the {@link GenTests} instance to report compilation failures
   */
  public CompilableTestPredicate(JUnitCreator junitCreator, GenTests genTests) {
    List<String> compilerOptions = new ArrayList<>();
    // only need to know an error exists:
    compilerOptions.add("-Xmaxerrs");
    compilerOptions.add("1");
    // no class generation:
    compilerOptions.add("-implicit:none");
    // no annotation processing: (note that -proc:only does not produce correct results)
    compilerOptions.add("-proc:none");
    // no debugging information:
    compilerOptions.add("-g:none");
    // no warnings:
    compilerOptions.add("-Xlint:none");
    this.compiler = new SequenceCompiler(compilerOptions);
    this.junitCreator = junitCreator;
    this.classNameGenerator = new NameGenerator("RandoopTemporarySeqTest");
    this.methodNameGenerator = new NameGenerator("theSequence");
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
  public boolean test(ExecutableSequence eseq) {
    String testClassName = classNameGenerator.next();
    List<ExecutableSequence> sequences = Collections.singletonList(eseq);
    CompilationUnit source =
        junitCreator.createTestClass(testClassName, methodNameGenerator, sequences);
    Optional<PackageDeclaration> oPkg = source.getPackageDeclaration();
    String packageName = oPkg.isPresent() ? oPkg.get().getName().toString() : null;
    boolean result = testSource(testClassName, source, packageName);
    if (!result) {
      genTests.incrementSequenceCompileFailureCount();
      Log.logPrintf(
          "%nCompilableTestPredicate => false for%n%nsequence =%n%s%nsource =%n%s%n", eseq, source);
    }
    return result;
  }

  /**
   * Return true if the given source code compiles without error. This is here to allow the
   * mechanics of the predicate to be tested directly. Otherwise, we have to create a broken {@link
   * ExecutableSequence}, which may not always be possible.
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
