package randoop.main;

import static com.github.javaparser.utils.PositionUtils.sortByBeginPosition;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.CloneVisitor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.plumelib.options.Option;
import org.plumelib.options.OptionGroup;
import org.plumelib.options.Options;
import randoop.Globals;
import randoop.output.ClassRenamingVisitor;
import randoop.output.ClassTypeNameSimplifyVisitor;
import randoop.output.ClassTypeVisitor;
import randoop.output.FieldAccessTypeNameSimplifyVisitor;
import randoop.output.MethodTypeNameSimplifyVisitor;
import randoop.output.PrimitiveAndWrappedTypeVarNameCollector;

/**
 * This program minimizes a failing JUnit test suite. Its three command-line arguments are:
 *
 * <ol>
 *   <li>the Java file whose failing tests will be minimized
 *   <li>an optional classpath containing dependencies needed to compile and run the Java file
 *   <li>an optional timeout limit, in seconds, allowed for the whole test suite to run. The default
 *       is 30 seconds.
 * </ol>
 *
 * <p>The minimizer will only attempt to minimize methods that are annotated with the @Test
 * annotation. In a method that contains a failing assertion, the minimizer will iterate through the
 * statements of the method, from last to first. For each statement, it tries possible replacement
 * statements, from most minimized to least minimized. Removing the statement is the most a
 * statement can be minimized. Leaving the statement unchanged is the least that the statement can
 * be minimized.
 *
 * <p>If a replacement causes the output test suite to fail differently than the original test
 * suite, the algorithm tries a different replacement. If no replacement allows the output test
 * suite to fail in the same way as the original test suite, the algorithm adds back the original
 * version of the current statement and continues.
 */
public class Minimize extends CommandHandler {

  /** The Java file whose failing tests will be minimized. */
  @SuppressWarnings("WeakerAccess")
  @OptionGroup(value = "Test case minimization")
  @Option("File containing the JUnit test suite to be minimized")
  public static String suitepath;

  /**
   * Classpath that includes dependencies needed to compile and run the JUnit test suite being
   * minimized.
   */
  @SuppressWarnings("WeakerAccess")
  @Option("Classpath to compile and run the JUnit test suite")
  public static String suiteclasspath;

  /** The maximum number of seconds allowed for the entire minimization process. */
  @SuppressWarnings("WeakerAccess")
  @Option("Timeout, in seconds, for the whole minimization process")
  public static int minimizetimeout = 600;

  /** The maximum number of seconds allowed for the entire test suite to run. */
  @SuppressWarnings("WeakerAccess")
  @Option("Timeout, in seconds, for the whole test suite")
  public static int testsuitetimeout = 30;

  /** Produce verbose diagnostics to standard output if true. */
  @SuppressWarnings("WeakerAccess")
  @Option("Verbose, flag for verbose output")
  public static boolean verboseminimizer = false;

  /** An instance of a Java parser. */
  private static final JavaParser javaParser = new JavaParser();

  /** Create the handler for Randoop's {@code minimize} command. */
  Minimize() {
    super(
        "minimize",
        "Minimize a failing JUnit test suite.",
        "minimize",
        "",
        "Minimize a failing JUnit test suite.",
        null,
        "Path to Java file whose failing tests will be minimized, classpath to compile and run the Java file, maximum time (in seconds) allowed for a single unit test case to run before it times out.",
        "A minimized JUnit test suite (as one Java file) named \"InputFileMinimized.java\" if \"InputFile.java\" were the name of the input file.",
        "java randoop.main.Main minimize --suitepath=~/RandoopTests/src/ErrorTestLang.java --suiteclasspath=~/RandoopTests/commons-lang3-3.5.jar --testsuitetimeout=30",
        new Options(Minimize.class));
  }

  /** Path separator as defined by the system, used to separate elements of the classpath. */
  private static final String PATH_SEPARATOR = System.getProperty("path.separator");

  /** The suffix to postpend onto the name of the minimized file and class. */
  private static final String SUFFIX = "Minimized";

  /**
   * Given a .java filename for non-minimized tests, returns the simple name of the class containing
   * the minimized tests.
   *
   * @param file the .java filename for non-minimized tests
   * @return the simple class name for the minimized tests
   */
  public static String minimizedClassName(Path file) {
    return FilenameUtils.removeExtension(file.getFileName().toString()) + SUFFIX;
  }

  /**
   * Check that the required parameters have been specified by the command-line options and then
   * call the mainMinimize method.
   *
   * @param args parameters, specified in command-line style, for the input file, the classpath, the
   *     timeout value, and the verbose flag
   * @return true if the command was handled successfully
   */
  @Override
  public boolean handle(String[] args) {
    try {
      String[] nonargs = foptions.parse(args);
      if (nonargs.length > 0) {
        throw new RandoopCommandError("Unrecognized arguments: " + Arrays.toString(nonargs));
      }
    } catch (Options.ArgException ae) {
      throw new RandoopCommandError(ae.getMessage());
    }

    if (Minimize.suitepath == null) {
      throw new RandoopCommandError("Use --suitepath to specify a file to be minimized.");
    }

    // Check that the input file is a Java file.
    if (!FilenameUtils.getExtension(Minimize.suitepath).equals("java")) {
      throw new RandoopCommandError("The input file must be a Java file: " + Minimize.suitepath);
    }

    if (Minimize.testsuitetimeout <= 0) {
      throw new RandoopCommandError(
          "Timout must be positive, was given as " + Minimize.testsuitetimeout + ".");
    }

    if (Minimize.minimizetimeout <= 0) {
      throw new RandoopCommandError(
          "Minimizer timout must be positive, was given as " + Minimize.minimizetimeout + ".");
    }

    // File object pointing to the file to be minimized.
    final Path originalFile = Paths.get(suitepath);

    ExecutorService executor = Executors.newFixedThreadPool(1);
    Future<Boolean> future =
        executor.submit(
            new Callable<Boolean>() {
              @Override
              public Boolean call() throws IOException {
                return mainMinimize(
                    originalFile, suiteclasspath, testsuitetimeout, verboseminimizer);
              }
            });

    executor.shutdown();

    boolean success = false;
    try {
      success = future.get(Minimize.minimizetimeout, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      System.err.println("Minimization process was interrupted.");
    } catch (ExecutionException e) {
      System.err.println("Minimizer exception: " + e.getCause());
    } catch (TimeoutException e) {
      future.cancel(true);
      System.err.println("Minimization process timed out.");
    }

    try {
      // Wait 5 more seconds to terminate processes.
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        // Force terminate the process.
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      System.err.println("Minimization process force terminated.");
    }

    return success;
  }

  /**
   * Minimize the input test file.
   *
   * <p>Given an input Java file, minimization produces a smaller file that fails in the same way as
   * the original, having the same failing assertions with the same stack trace.
   *
   * <ol>
   *   <li>Same failing assertions as in the original input test suite.
   *   <li>Same stacktrace produced by failing assertions.
   * </ol>
   *
   * <p>The original input Java file will be compiled and run once. The "expected output" derived
   * from the standard output from running the input file is a map from test method name to failure
   * stack trace. A method is included in the map only if the method contains a failing assertion.
   * Thus, the "expected output" of running a test suite with no failing tests will be an empty map.
   * The "expected output" will be used during subsequent runs of the modified test suite to
   * determine whether or not the test suite still fails in the same way.
   *
   * @param file the Java file that is being minimized
   * @param classPath classpath used to compile and run the Java file
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @param verboseOutput whether to produce verbose output
   * @return true if minimization produced a (possibly unchanged) file that fails the same way as
   *     the original file
   * @throws IOException if write to file fails
   */
  public static boolean mainMinimize(
      Path file, String classPath, int timeoutLimit, boolean verboseOutput) throws IOException {
    System.out.println("Minimizing: " + file);

    if (verboseOutput) {
      System.out.println("Reading and parsing file.");
    }

    // Read and parse input Java file.
    CompilationUnit compilationUnit;
    try (FileInputStream inputStream = new FileInputStream(file.toFile())) {
      ParseResult<CompilationUnit> parseCompilationUnit = javaParser.parse(inputStream);
      if (parseCompilationUnit.isSuccessful()) {
        compilationUnit = parseCompilationUnit.getResult().get();
      } else {
        System.err.println("Error parsing Java file: " + file);
        for (Problem problem : parseCompilationUnit.getProblems()) {
          System.out.println(problem);
        }
        return false;
      }
    } catch (IOException e) {
      System.err.println("Error reading Java file: " + file);
      e.printStackTrace();
      return false;
    }

    if (verboseOutput) {
      System.out.println("Getting expected output.");
    }

    // Find the package name of the input file if it has one.
    String packageName;
    try {
      Optional<PackageDeclaration> oClassPackage = compilationUnit.getPackageDeclaration();
      if (oClassPackage.isPresent()) {
        packageName = oClassPackage.get().getName().toString();
      } else {
        packageName = null;
      }
    } catch (NoSuchElementException e) {
      packageName = null;
      // No package declaration.
    }

    String oldClassName = FilenameUtils.removeExtension(file.getFileName().toString());
    String newClassName = oldClassName + SUFFIX;
    Path minimizedFile =
        ClassRenamingVisitor.copyAndRename(file, compilationUnit, oldClassName, newClassName);

    // Compile the original Java file (it has not been minimized yet).
    Outputs compilationOutput =
        compileJavaFile(minimizedFile, classPath, packageName, timeoutLimit);
    if (compilationOutput.isFailure()) {
      System.err.println("Error when compiling file " + file + ". Aborting.");
      System.err.println(compilationOutput.diagnostics());
      return false;
    }

    // expectedOutput is a map from method name to failure stack trace with
    // line numbers removed.
    String runResult = runJavaFile(minimizedFile, classPath, packageName, timeoutLimit);
    Map<String, String> expectedOutput = normalizeJUnitOutput(runResult);

    // Minimize the Java test suite.
    minimizeTestSuite(
        compilationUnit, packageName, minimizedFile, classPath, expectedOutput, timeoutLimit);

    // Cleanup: simplify type names and sort the import statements.
    compilationUnit =
        simplifyTypeNames(
            compilationUnit,
            packageName,
            minimizedFile,
            classPath,
            expectedOutput,
            timeoutLimit,
            verboseOutput);

    writeToFile(compilationUnit, minimizedFile);

    // Delete the .class file associated with the minimized Java file.
    cleanUp(minimizedFile, verboseOutput);

    System.out.println("Original file length: " + getFileLength(file) + " lines.");
    System.out.println("Minimized file length: " + getFileLength(minimizedFile) + " lines.");

    return true;
  }

  /**
   * Visit and minimize every JUnit test method within a compilation unit.
   *
   * @param compilationUnit the compilation unit to minimize; is modified by side effect
   * @param packageName the package that the Java file is in
   * @param file the Java file that is being minimized; is modified by side effect
   * @param classpath classpath used to compile and run the Java file
   * @param expectedOutput expected JUnit output when the Java file is compiled and run
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @throws IOException thrown if minimized method can't be written to file
   */
  private static void minimizeTestSuite(
      CompilationUnit compilationUnit,
      String packageName,
      Path file,
      String classpath,
      Map<String, String> expectedOutput,
      int timeoutLimit)
      throws IOException {
    System.out.println("Minimizing test suite.");

    int numberOfTestMethods = getNumberOfTestMethods(compilationUnit);
    int numberOfMinimizedTests = 0;

    for (TypeDeclaration<?> type : compilationUnit.getTypes()) {
      for (BodyDeclaration<?> member : type.getMembers()) {
        if (member instanceof MethodDeclaration) {
          MethodDeclaration method = (MethodDeclaration) member;

          // Minimize the method only if it is a JUnit test method.
          if (isTestMethod(method)) {
            minimizeMethod(
                method,
                compilationUnit,
                packageName,
                file,
                classpath,
                expectedOutput,
                timeoutLimit);
            printProgress(++numberOfMinimizedTests, numberOfTestMethods, method.getName());
          }
        }
      }
    }
  }

  /**
   * Check if the method is a JUnit test method.
   *
   * @param methodDeclaration the method declaration to check
   * @return true if the method is a JUnit test method
   */
  private static boolean isTestMethod(MethodDeclaration methodDeclaration) {
    // Iterate through the method's annotations and check for the test
    // annotation.
    for (AnnotationExpr annotationExpr : methodDeclaration.getAnnotations()) {
      if (annotationExpr.toString().equals("@Test")) {
        return true;
      }
    }

    return false;
  }

  /**
   * Minimize a method by minimizing each statement in turn.
   *
   * @param method the method to minimize; is modified by side effect
   * @param compilationUnit compilation unit for the Java file that we are minimizing; is modified
   *     by side effect
   * @param packageName the package that the Java file is in
   * @param file the Java file that is being minimized; is modified by side effect
   * @param classpath classpath needed to compile and run the Java file
   * @param expectedOutput expected output from running the JUnit test suite
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @throws IOException thrown if write to file fails
   */
  private static void minimizeMethod(
      MethodDeclaration method,
      CompilationUnit compilationUnit,
      String packageName,
      Path file,
      String classpath,
      Map<String, String> expectedOutput,
      int timeoutLimit)
      throws IOException {
    Optional<BlockStmt> oBlockStmt = method.getBody();
    if (!oBlockStmt.isPresent()) {
      return;
    }
    BlockStmt body = oBlockStmt.get();
    List<Statement> statements = body.getStatements();

    // Map from primitive variable name to the variable's value extracted
    // from a passing assertion.
    Map<String, String> primitiveValues = new HashMap<>();

    // Find all the names of the primitive and wrapped types.
    Set<String> primitiveAndWrappedTypes = new HashSet<>();
    new PrimitiveAndWrappedTypeVarNameCollector().visit(compilationUnit, primitiveAndWrappedTypes);

    // Iterate through the list of statements, from last to first.
    for (int i = statements.size() - 1; i >= 0; i--) {
      Statement currStmt = statements.get(i);
      List<Comment> orphanComments = new ArrayList<>(1);
      getOrphanCommentsBeforeThisChildNode(currStmt, orphanComments);
      Node parent = currStmt.getParentNode().get();

      // Remove the current statement. We will re-insert simplifications of it.
      statements.remove(i);

      // Obtain a list of possible replacements for the current statement.
      List<Statement> replacements = getStatementReplacements(currStmt, primitiveValues);
      boolean replacementFound = false;
      boolean replacementIsNull = false;

      for (Statement stmt : replacements) {
        // Add replacement statement to the method's body.
        // If stmt is null, don't add anything since null represents removal of the statement.
        if (stmt != null) {
          statements.add(i, stmt);
        }

        // Write, compile, and run the new Java file.
        writeToFile(compilationUnit, file);
        if (checkCorrectlyMinimized(file, classpath, packageName, expectedOutput, timeoutLimit)) {
          // No compilation or runtime issues, obtained output is the same as the expected output.
          // Use simplification of this statement and continue with next statement.
          replacementFound = true;
          replacementIsNull = (stmt == null);

          // Assertions are never simplified, only removed. If currStmt is an assertion, then stmt
          // is null.
          storeValueFromAssertion(currStmt, primitiveValues, primitiveAndWrappedTypes);
          break; // break replacement loop; continue statements loop.
        } else {
          // Issue encountered, remove the faulty replacement.
          if (stmt != null) {
            statements.remove(i);
          }
        }
      }

      if (!replacementFound) {
        // No correct simplification found. Add back the original statement to the list of
        // statements.
        statements.add(i, currStmt);
      } else if (replacementIsNull) {
        for (Comment oc : orphanComments) {
          parent.removeOrphanComment(oc);
        }
      }
    }
  }

  /**
   * If {@code currStmt} is a statement that is an assertTrue statement using an '==' operator,
   * store the value associated with the variable in the {@code primitiveValues} map.
   *
   * @param currStmt a statement
   * @param primitiveValues a map of variable names to variable values; modified if {@code currStmt}
   *     is a passing assertion, asserting a variable's value
   * @param primitiveAndWrappedTypeVars set containing the names of all primitive and wrapped type
   *     variables
   */
  private static void storeValueFromAssertion(
      Statement currStmt,
      Map<String, String> primitiveValues,
      Set<String> primitiveAndWrappedTypeVars) {
    // Check if the statement is an assertion regarding a value that can be
    // used in a simplification later on.
    if (currStmt instanceof ExpressionStmt) {
      Expression exp = ((ExpressionStmt) currStmt).getExpression();
      if (exp instanceof MethodCallExpr) {
        MethodCallExpr mCall = (MethodCallExpr) exp;
        // Check that the method call is an assertTrue statement.
        if (mCall.getName().toString().equals("assertTrue")) {
          List<Expression> mArgs = mCall.getArguments();
          // The condition expression from the assert statement.
          Expression mExp;
          if (mArgs.size() == 1) {
            mExp = mArgs.get(0);
          } else if (mArgs.size() == 1) {
            mExp = mArgs.get(1);
          } else {
            return;
          }

          // Check that the expression is a binary expression.
          if (mExp instanceof BinaryExpr) {
            BinaryExpr binaryExp = (BinaryExpr) mExp;
            // Check that the operator is an equality operator.
            if (binaryExp.getOperator().equals(BinaryExpr.Operator.EQUALS)) {
              // Retrieve and store the value associated with the variable in the assertion.
              Expression leftExpr = binaryExp.getLeft();
              Expression rightExpr = binaryExp.getRight();

              // Swap two expressions if left is a literal expression.
              if (leftExpr instanceof LiteralExpr) {
                Expression temp = leftExpr;
                leftExpr = rightExpr;
                rightExpr = temp;
              }

              // Check that the left is a variable name and the right is a literal.
              if (leftExpr instanceof NameExpr && rightExpr instanceof LiteralExpr) {
                NameExpr nameExpr = (NameExpr) leftExpr;
                // Check that the variable is a primitive or wrapped type.
                if (primitiveAndWrappedTypeVars.contains(nameExpr.getName().toString())) {
                  String var = binaryExp.getLeft().toString();
                  String val = binaryExp.getRight().toString();
                  primitiveValues.put(var, val);
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Return a list of statements that are a simplification of a given statement, in order from most
   * to least minimized. The possible minimizations are:
   *
   * <ul>
   *   <li>Remove a statement, represented by null.
   *   <li>Replace the right hand side expression with {@code 0}, {@code false}, or {@code null}.
   *   <li>Replace right hand side by a calculated value obtained from a passing assertion.
   *   <li>Remove the left hand side of a statement, retaining only the expression on the right.
   * </ul>
   *
   * <p>Assertions are never simplified, only removed completely.
   *
   * @param currStmt statement to simplify
   * @param primitiveValues map of primitive variable names to expressions representing their values
   * @return list of statements, where each is a possible simplification of {@code currStmt}
   */
  private static List<Statement> getStatementReplacements(
      Statement currStmt, Map<String, String> primitiveValues) {
    List<Statement> replacements = new ArrayList<>();

    // Null represents removal of the statement.
    replacements.add(null);

    if (currStmt instanceof ExpressionStmt) {
      Expression exp = ((ExpressionStmt) currStmt).getExpression();
      if (exp instanceof VariableDeclarationExpr) {
        VariableDeclarationExpr vdExpr = (VariableDeclarationExpr) exp;

        // Simplify right hand side to zero-equivalent value: 0, false,
        // or null.
        replacements.addAll(rhsAssignZeroValue(vdExpr));

        // Simplify right hand side to a value that was previously found
        // in a passing assertion.
        Statement rhsAssertValStmt = rhsAssignValueFromPassingAssertion(vdExpr, primitiveValues);
        if (rhsAssertValStmt != null) {
          replacements.add(rhsAssertValStmt);
        }

        // Simplify statement by removing the left hand side.
        Statement lhsRemovalStmt = removeLeftHandSideSimplification(vdExpr);
        if (lhsRemovalStmt != null) {
          replacements.add(lhsRemovalStmt);
        }
      }
    }
    return replacements;
  }

  /**
   * Return a list of variable declaration statements that could replace the right hand side by 0,
   * false, or null, whichever is type correct. Returns an empty list if there are multiple variable
   * declarations in a single statement, such as {@code int i, j, k; }.
   *
   * @param vdExpr variable declaration expression representing the current statement to simplify
   * @return a list of {@code Statement} objects representing the simplified variable declaration
   *     expression
   */
  private static List<Statement> rhsAssignZeroValue(VariableDeclarationExpr vdExpr) {
    List<Statement> resultList = new ArrayList<>();

    if (vdExpr.getVariables().size() != 1) {
      // Number of variables declared in this expression is not 1.
      return resultList;
    }

    Type type = vdExpr.getVariables().get(0).getType();
    if (type instanceof PrimitiveType) {
      // Replacement with zero value on the right hand side.
      resultList.add(rhsAssignWithValue(vdExpr, type, null));
    } else {
      // Replacement with null on the right hand side.
      resultList.add(rhsAssignWithValue(vdExpr, type, null));

      ReferenceType rType = (ReferenceType) type;
      if (rType instanceof ClassOrInterfaceType) {
        ClassOrInterfaceType classType = (ClassOrInterfaceType) rType;
        // Check if the type is a boxed primitive type.
        if (classType.isBoxedType()) {
          // Replacement with zero value on the right hand side.
          resultList.add(rhsAssignWithValue(vdExpr, classType.toUnboxedType(), null));
        }
      }
    }

    return resultList;
  }

  /**
   * Return a variable declaration statement that simplifies the right hand side by a calculated
   * value for primitive types. Returns null if there are multiple variable declarations in a single
   * statement, such as {@code int i, j, k; }.
   *
   * @param vdExpr variable declaration expression representing the current statement to simplify
   * @param primitiveValues a map of primitive variable names to expressions representing their
   *     values
   * @return a {@code Statement} object representing the simplified variable declaration expression
   *     if the type of the variable is a primitive and a value has been previously calculated.
   *     Otherwise, returns {@code null}. Also returns {@code null} if more than one variable is
   *     declared in the {@code VariableDeclarationExpr}.
   */
  private static Statement rhsAssignValueFromPassingAssertion(
      VariableDeclarationExpr vdExpr, Map<String, String> primitiveValues) {
    if (vdExpr.getVariables().size() != 1) {
      // Number of variables declared in this expression is not one.
      return null;
    }
    VariableDeclarator vDecl = vdExpr.getVariables().get(0);
    // Get the name of the variable being declared.
    String varName = vDecl.getName().toString();

    // Check if the map contains a value found from a passing assertion.
    if (primitiveValues.containsKey(varName)) {
      String value = primitiveValues.get(varName);
      return rhsAssignWithValue(vdExpr, vDecl.getType(), value);
    } else {
      // The map does not contain a value for this variable.
      return null;
    }
  }

  /**
   * Return a variable declaration statement that sets the right hand side of a variable declaration
   * to the value that is passed in.
   *
   * @param vdExpr variable declaration expression representing the current statement to simplify
   * @param exprType type of the variable declaration expression, should not be null
   * @param value value that will be assigned to the variable being declared. If the value is null,
   *     then the right hand side will have the zero value of the variable declaration's type.
   * @return a {@code Statement} object representing the simplified variable declaration expression.
   *     Returns {@code null} if more than one variable is declared in the {@code
   *     VariableDeclarationExpr}.
   */
  private static Statement rhsAssignWithValue(
      VariableDeclarationExpr vdExpr, Type exprType, String value) {
    if (vdExpr.getVariables().size() != 1) {
      // Number of variables declared in this expression is not 1.
      return null;
    }

    // Create the resulting expression, a copy of the original expression
    // which will be modified and returned.
    VariableDeclarationExpr resultExpr = vdExpr.clone();

    // The variable declaration.
    VariableDeclarator vd = resultExpr.getVariables().get(0);

    // Based on the declared variable type, set the right hand to the value
    // that was passed in.
    if (exprType instanceof PrimitiveType) {
      vd.setInitializer(getLiteralExpression(value, ((PrimitiveType) exprType).getType()));
    } else {
      // Set right hand side to the null expression.
      vd.setInitializer(new NullLiteralExpr());
    }

    // Return a new statement with the simplified expression.
    return new ExpressionStmt(resultExpr);
  }

  /**
   * Return a literal expression with the value that is passed in.
   *
   * @param value the value for the literal expression. If null, the value of the literal expression
   *     will be the zero value for the type that is passed in.
   * @param type the type of the expression, needs to be one of the eight primitive types
   * @return a literal expression containing the value that is passed in
   */
  private static LiteralExpr getLiteralExpression(String value, PrimitiveType.Primitive type) {
    switch (type) {
      case BOOLEAN:
        if (value == null) {
          return new BooleanLiteralExpr(Boolean.parseBoolean("false"));
        } else {
          return new BooleanLiteralExpr(Boolean.parseBoolean(value));
        }
      case CHAR:
      case BYTE:
      case SHORT:
      case INT:
        if (value == null) {
          return new IntegerLiteralExpr("0");
        } else {
          return new IntegerLiteralExpr(value);
        }
      case FLOAT:
        if (value == null) {
          return new DoubleLiteralExpr("0f");
        } else {
          return new DoubleLiteralExpr(value);
        }
      case DOUBLE:
        if (value == null) {
          return new DoubleLiteralExpr("0.0");
        } else {
          return new DoubleLiteralExpr(value);
        }
      case LONG:
        if (value == null) {
          return new LongLiteralExpr("0L");
        } else {
          return new LongLiteralExpr(value);
        }
      default:
        throw new IllegalArgumentException(
            "Type passed to get a literal expression was not a primitive type.");
    }
  }

  /**
   * Return a statement that contains only the right hand side of a given statement. Returns null if
   * there are multiple variable declarations in a single statement, such as {@code int i=1, j=2,
   * k=3; }, or if there are no initializers, as in {@code int i;}.
   *
   * @param vdExpr variable declaration expression that represents the statement to simplify
   * @return a {@code Statement} object that is equal to the right-hand-side of {@code vdExpr}.
   *     Returns {@code null} if more than one variable is declared in the {@code
   *     VariableDeclarationExpr}.
   */
  private static Statement removeLeftHandSideSimplification(VariableDeclarationExpr vdExpr) {
    if (vdExpr.getVariables().size() > 1) {
      // More than 1 variable declared in this expression.
      return null;
    }

    // Create the resulting expression, a copy of the original expression
    // which will be modified and returned.
    VariableDeclarationExpr resultExpr = vdExpr.clone();
    List<VariableDeclarator> vars = resultExpr.getVariables();
    VariableDeclarator vd = vars.get(0);

    // Return a new statement with only the right hand side.
    Optional<Expression> initializer = vd.getInitializer();
    if (initializer.isPresent()) {
      return new ExpressionStmt(initializer.get());
    } else {
      return null;
    }
  }

  /** Sorts a type by its simple name. */
  private static class ClassOrInterfaceTypeComparator implements Comparator<ClassOrInterfaceType> {
    @Override
    public int compare(ClassOrInterfaceType o1, ClassOrInterfaceType o2) {
      return o1.toString().compareTo(o2.toString());
    }
  }
  /** Sorts a type by its simple name. */
  private static ClassOrInterfaceTypeComparator classOrInterfaceTypeComparator =
      new ClassOrInterfaceTypeComparator();

  /**
   * Simplify the type names in a compilation unit. For example, {@code java.lang.String} should be
   * simplified to {@code String}. If two different types have the same simple type name, then the
   * lexicographically first one is simplified and the other is left unchanged.
   *
   * <p>Additionally, sort the import statements of the compilation unit.
   *
   * @param compilationUnit compilation unit containing an AST for a Java file, the compilation unit
   *     will be modified if a correct minimization of the method is found
   * @param packageName the package that the Java file is in
   * @param file the Java file to simplify; is modified by side effect
   * @param classpath classpath needed to compile and run the Java file
   * @param expectedOutput expected standard output from running the JUnit test suite
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @param verboseOutput whether or not to output information about minimization status
   * @return {@code CompilationUnit} with fully-qualified type names simplified to simple type names
   * @throws IOException thrown if write to file fails
   */
  private static CompilationUnit simplifyTypeNames(
      CompilationUnit compilationUnit,
      String packageName,
      Path file,
      String classpath,
      Map<String, String> expectedOutput,
      int timeoutLimit,
      boolean verboseOutput)
      throws IOException {
    if (verboseOutput) {
      System.out.println("Adding imports and simplifying type names.");
    }

    // Types that are used in variable declarations.  Contains only one type per simple name.
    Set<ClassOrInterfaceType> types = new TreeSet<>(classOrInterfaceTypeComparator);
    new ClassTypeVisitor().visit(compilationUnit, types);
    CompilationUnit result = compilationUnit;
    for (ClassOrInterfaceType type : types) {
      // Copy and modify the compilation unit.
      CompilationUnit compUnitWithSimpleTypeNames =
          (CompilationUnit) result.accept(new CloneVisitor(), null);

      // String representation of the fully-qualified type name.
      Optional<ClassOrInterfaceType> scope = type.getScope();

      String scopeString = (scope.isPresent() ? scope.get() + "." : "");
      // Check that the type is not in the java.lang package.
      if (!scopeString.equals("java.lang.")) {
        // Add an import statement for the type.
        addImport(compUnitWithSimpleTypeNames, scopeString + type.getName());
      }

      // Simplify class type names, method call names, and field names.
      // XXX this should be handled by a single visitor that uses the full set of types.
      new ClassTypeNameSimplifyVisitor().visit(compUnitWithSimpleTypeNames, type);
      new MethodTypeNameSimplifyVisitor().visit(compUnitWithSimpleTypeNames, type);
      new FieldAccessTypeNameSimplifyVisitor().visit(compUnitWithSimpleTypeNames, type);

      // Check that the simplification is correct.
      writeToFile(compUnitWithSimpleTypeNames, file);
      if (checkCorrectlyMinimized(file, classpath, packageName, expectedOutput, timeoutLimit)) {
        result = compUnitWithSimpleTypeNames;
      }
    }

    sortImports(result);

    return result;
  }

  /**
   * Check if a Java file has been correctly minimized. The file should not have compilation errors
   * or run-time errors. The file should fail in the same way as the original file.
   *
   * @param file the file being checked
   * @param classpath classpath needed to compile/run the Java file
   * @param packageName the package that the Java file is in
   * @param expectedOutput expected output of running the JUnit test suite
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @return true if there are no compilation and no run-time errors and the output is equal to the
   *     expected output
   */
  private static boolean checkCorrectlyMinimized(
      Path file,
      String classpath,
      String packageName,
      Map<String, String> expectedOutput,
      int timeoutLimit) {

    Outputs compilationOutput = compileJavaFile(file, classpath, packageName, timeoutLimit);
    if (compilationOutput.isFailure()) {
      return false;
    }

    // Run the Java file and get the standard output.
    String runResult = runJavaFile(file, classpath, packageName, timeoutLimit);

    // Compare the standard output with the expected output.
    return expectedOutput.equals(normalizeJUnitOutput(runResult));
  }

  /**
   * Compile a Java file and return the compilation exit value.
   *
   * @param file the file to be compiled and executed
   * @param classpath dependencies and complete classpath to compile and run the Java program
   * @param packageName the package that the Java file is in
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @return the result of compilation (includes status and output)
   */
  private static Outputs compileJavaFile(
      Path file, String classpath, String packageName, int timeoutLimit) {
    // Obtain directory to carry out compilation and execution step.
    Path executionDir = getExecutionDirectory(file, packageName);

    // Command to compile the input Java file.
    String command = "javac -classpath .";
    if (classpath != null) {
      // Add specified classpath to command.
      command += PATH_SEPARATOR + classpath;
    }

    command += " " + file.toAbsolutePath().toString();

    // Compile specified Java file.
    return runProcess(command, executionDir, timeoutLimit);
  }

  /**
   * Run a Java file and return the standard output.
   *
   * @param file the file to be compiled and executed
   * @param userClassPath dependencies and complete classpath to compile and run the Java program
   * @param packageName the package that the Java file is in
   * @param timeoutLimit number of seconds allowed for the Java program to run
   * @return standard output from running the Java file
   */
  private static String runJavaFile(
      Path file, String userClassPath, String packageName, int timeoutLimit) {
    // Obtain directory to carry out compilation and execution step.
    Path executionDir = getExecutionDirectory(file, packageName);

    // Directory path for the classpath.
    String dirPath = ".";
    if (file.getParent() != null) {
      dirPath += PATH_SEPARATOR + file.getParent();
    }

    // Classpath for running the Java file.
    String classpath = dirPath;
    if (userClassPath != null) {
      classpath += PATH_SEPARATOR + userClassPath;
    }

    // Fully-qualified classname.
    String fqClassName = FilenameUtils.getBaseName(file.toString());
    if (packageName != null) {
      fqClassName = packageName + "." + fqClassName;
    }

    String command = "java -classpath " + classpath + " org.junit.runner.JUnitCore " + fqClassName;

    // Run the specified Java file and return the standard output.
    return runProcess(command, executionDir, timeoutLimit).stdout;
  }

  /**
   * Get directory to execute command in, given file path and package name. Returns a {@code Path}
   * pointing to the directory that the Java file should be executed in.
   *
   * <p>For the simplest case where the Java file is nested in a single package layer, i.e.
   * MyJavaFile.java is in the package mypackage, the folder structure would be
   * src/mypackage/MyJavaFile.java. Here, we need to execute the Java file in the src/ directory. We
   * go up 2 layers from the directory of the Java file to get to the parent directory of the
   * directory for the package. For the general case where MyJavaFile.java is nested within multiple
   * layers of packages, we count the number of separators, i.e. ".", and add 2 to get the number of
   * layers to go up from the Java file's directory.
   *
   * @param file the Java file to be executed
   * @param packageName package name of input Java file
   * @return the directory to execute the commands in, or null if packageName is null
   */
  private static Path getExecutionDirectory(Path file, String packageName) {
    if (packageName == null) {
      return null;
    }

    // Determine how many layers above we should be executing the process
    // in. Add 2 which is for the case where the file is located in a single
    // layer of packaging.
    int foldersAbove = StringUtils.countMatches(packageName, ".") + 2;
    for (int i = 0; i < foldersAbove; i++) {
      file = file.getParent();
    }

    // Return the directory.
    return file;
  }

  /**
   * Run a command given as a String and return the output and error results in an Outputs object.
   *
   * @param command the input command to be run
   * @param executionDir the directory where the process commands should be executed
   * @param timeoutLimit number of seconds allowed for the command to run
   * @return an {@code Outputs} object containing the standard and error output
   */
  public static Outputs runProcess(String command, Path executionDir, int timeoutLimit) {
    if (executionDir != null && executionDir.toString().isEmpty()) {
      // Execute command in the default directory.
      executionDir = null;
    }

    String[] args = command.split(" ");
    CommandLine cmdLine = new CommandLine(args[0]); // constructor requires executable name
    cmdLine.addArguments(Arrays.copyOfRange(args, 1, args.length));

    DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    DefaultExecutor executor = new DefaultExecutor();
    if (executionDir != null) {
      executor.setWorkingDirectory(executionDir.toFile());
    }

    ExecuteWatchdog watchdog = new ExecuteWatchdog(timeoutLimit * 1000);
    executor.setWatchdog(watchdog);

    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outStream, errStream);
    executor.setStreamHandler(streamHandler);

    try {
      executor.execute(cmdLine, resultHandler);
    } catch (IOException e) {
      return Outputs.failure(cmdLine, "Exception starting process");
    }

    int exitValue = -1;
    try {
      resultHandler.waitFor();
      exitValue = resultHandler.getExitValue();
    } catch (InterruptedException e) {
      if (!watchdog.killedProcess()) {
        return Outputs.failure(cmdLine, "Process was interrupted while waiting.");
      }
    }
    boolean timedOut = executor.isFailure(exitValue) && watchdog.killedProcess();

    String stdOutputString;
    String errOutputString;

    try {
      stdOutputString = outStream.toString();
    } catch (RuntimeException e) {
      return Outputs.failure(cmdLine, "Exception getting process standard output");
    }

    try {
      errOutputString = errStream.toString();
    } catch (RuntimeException e) {
      return Outputs.failure(cmdLine, "Exception getting process error output");
    }

    if (timedOut) {
      return Outputs.failure(cmdLine, "Process timed out after " + timeoutLimit + " seconds.");
    }

    // Collect and return the results from the standard output and error
    // output.
    return new Outputs(cmdLine, exitValue, stdOutputString, errOutputString);
  }

  /**
   * Normalize the standard output obtained from running a JUnit test suite. By normalizing the
   * {@code String} representation of the output, we remove any extraneous information such as line
   * numbers. The resulting output is a map from method name to the method's failure stack trace.
   *
   * @param input the {@code String} produced from running a JUnit test suite
   * @return a map from method name to the method's failure stack trace. The stack trace will not
   *     contain any line numbers.
   */
  private static Map<String, String> normalizeJUnitOutput(String input) {
    BufferedReader bufReader = new BufferedReader(new StringReader(input));

    String methodName = null;
    Map<String, String> resultMap = new HashMap<>();

    StringBuilder result = new StringBuilder();
    // JUnit output starts with index 1 for first failure.
    int index = 1;

    try {
      for (String line; (line = bufReader.readLine()) != null; ) {
        String indexStr = index + ") ";
        // Check if the current line is the start of a failure stack
        // trace for a method.
        if (line.startsWith(indexStr)) {
          // If a previous failure stack trace is being read, add the
          // existing method name and stack trace to the map.
          if (methodName != null) {
            resultMap.put(methodName, result.toString());

            // Reset the string builder.
            result.setLength(0);
          }
          // Set the method name to the current line.
          methodName = line;
          index += 1;
        } else if (line.isEmpty()) {
          // Reached an empty line which marks the end of the JUnit
          // output.
          resultMap.put(methodName, result.toString());
          break;
        } else if (methodName != null) {
          // Look for a left-parentheses which marks the position
          // where a line number will appear.
          int lParenIndex = line.indexOf('(');
          if (lParenIndex >= 0) {
            // Remove the substring containing the line number.
            line = line.substring(0, lParenIndex);
          }
          result.append(line).append(Globals.lineSep);
        }
      }
      bufReader.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return resultMap;
  }

  /**
   * Write a compilation unit to a Java file.
   *
   * @param compilationUnit the compilation unit to write to file
   * @param file file to write to
   * @throws IOException thrown if write to file fails
   */
  public static void writeToFile(CompilationUnit compilationUnit, Path file) throws IOException {
    // Write the compilation unit to the file.
    try (BufferedWriter bw = Files.newBufferedWriter(file, UTF_8)) {
      bw.write(compilationUnit.toString());
    }
  }

  /**
   * Add an import to the list of {@code ImportDeclaration}s of the compilation unit. This method
   * adds the import statement if it has not been yet included in the list of current imports of the
   * compilation unit.
   *
   * @param compilationUnit compilation unit to add import to
   * @param importName the name of the import
   */
  private static void addImport(CompilationUnit compilationUnit, String importName) {
    String importStr = "import " + importName + ";";

    ParseResult<ImportDeclaration> parseImportDeclaration = javaParser.parseImport(importStr);
    if (!parseImportDeclaration.isSuccessful()) {
      throw new RandoopBug("Error parsing import: " + importName);
    }
    ImportDeclaration importDeclaration = parseImportDeclaration.getResult().get();

    NodeList<ImportDeclaration> importDeclarations = compilationUnit.getImports();
    for (ImportDeclaration im : importDeclarations) {
      String currImportStr = im.toString().trim();

      // Check if the compilation unit already includes the import
      // exactly.
      if (importStr.equals(currImportStr)) {
        return;
      }
      // Check if the compilation unit already includes the import as a
      // wildcard. Get index of last separator.
      int lastSeparator = importName.lastIndexOf('.');
      if (lastSeparator >= 0) {
        // Create a string representing a wildcard import.
        String wildcardName = importName.substring(0, lastSeparator);
        String wildcardImportStr = "import " + wildcardName + ".*" + ";";
        // If a wildcard import exists, don't need to add a redundant
        // import.
        if (wildcardImportStr.equals(currImportStr)) {
          return;
        }
      }
    }

    // Add the import to the compilation unit's list of imports.
    importDeclarations.add(importDeclaration);
    compilationUnit.setImports(importDeclarations);
  }

  /** Sorts ImportDeclaration objects by their name. */
  private static class ImportDeclarationComparator implements Comparator<ImportDeclaration> {
    @Override
    public int compare(ImportDeclaration o1, ImportDeclaration o2) {
      return o1.getName().toString().compareTo(o2.getName().toString());
    }
  }
  /** Sorts ImportDeclaration objects by their name. */
  private static ImportDeclarationComparator importDeclarationComparator =
      new ImportDeclarationComparator();

  /**
   * Sort a compilation unit's imports by name.
   *
   * @param compilationUnit the compilation unit whose imports will be sorted by name
   */
  private static void sortImports(CompilationUnit compilationUnit) {
    NodeList<ImportDeclaration> imports = compilationUnit.getImports();

    Collections.sort(imports, importDeclarationComparator);

    compilationUnit.setImports(imports);
  }

  /**
   * Contains the command line, exit status, standard output, and standard error from running a
   * process.
   */
  public static class Outputs {
    /** The command that was run. */
    public final String command;
    /** Exit value from running a process. 0 is success, other values are failure. */
    public final int exitValue;
    /** The standard output. */
    public final String stdout;
    /** The error output. */
    public final String errout;

    /**
     * Create an Outputs object.
     *
     * @param command the command that was run
     * @param exitValue exit value of process
     * @param stdout standard output
     * @param errout error output
     */
    Outputs(String command, int exitValue, String stdout, String errout) {
      this.command = command;
      this.exitValue = exitValue;
      this.stdout = stdout;
      this.errout = errout;
    }

    /**
     * Create an Outputs object.
     *
     * @param command the command that was run
     * @param exitValue exit value of process
     * @param stdout standard output
     * @param errout error output
     */
    Outputs(CommandLine command, int exitValue, String stdout, String errout) {
      this(command.toString(), exitValue, stdout, errout);
    }

    /**
     * Create an Outputs object representing a failed execution.
     *
     * @param command the command that was run
     * @param errout error output
     * @return an Outputs object representing a failed execution
     */
    static Outputs failure(CommandLine command, String errout) {
      return new Outputs(command.toString(), 1, "", errout);
    }

    /**
     * Return true if the command succeeded.
     *
     * @return true if the command succeeded
     */
    public boolean isSuccess() {
      return exitValue == 0;
    }

    /**
     * Return true if the command failed.
     *
     * @return true if the command failed
     */
    public boolean isFailure() {
      return !isSuccess();
    }

    /**
     * Verbose toString().
     *
     * @return a verbose multi-line string representation of this object, for dbugging
     */
    public String diagnostics() {
      return String.join(
          Globals.lineSep,
          "command: " + command,
          "exit status: " + exitValue + "  " + (isSuccess() ? "(success)" : "(failure)"),
          "standard output: ",
          stdout,
          "error output: ",
          errout);
    }
  }

  /**
   * Calculate the length of a file, by number of lines.
   *
   * @param file the file to compute the length of
   * @return the number of lines in the file. Returns -1 if an exception occurs while reading the
   *     file
   * @throws IOException thrown if error reading file
   */
  private static int getFileLength(Path file) throws IOException {
    int lines = 0;

    try (BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
      // Read and count the number of lines in the file.
      while (reader.readLine() != null) {
        lines++;
      }
    }

    return lines;
  }

  /**
   * Deletes the .class file associated with the outputFile.
   *
   * @param outputFile the source file for the class file to be removed
   * @param verboseOutput whether to print information about minimization status
   */
  private static void cleanUp(Path outputFile, boolean verboseOutput) {
    System.out.println("Minimizing complete.");

    String outputClassFileStr =
        FilenameUtils.removeExtension(outputFile.toAbsolutePath().toString()).concat(".class");
    Path outputClassFile = Paths.get(outputClassFileStr);
    try {
      boolean success = Files.deleteIfExists(outputClassFile);

      if (verboseOutput && success) {
        System.out.println("Minimizer cleanup: Removed .class file.");
      }
    } catch (IOException e) {
      System.err.println("IOException when cleaning up .class file.");
    }
  }

  /**
   * Return the number of JUnit test methods in a compilation unit.
   *
   * @param compilationUnit the compilation unit to count the number of unit test methods
   * @return the number of unit test methods in compilationUnit
   */
  private static int getNumberOfTestMethods(CompilationUnit compilationUnit) {
    int numberOfTestMethods = 0;
    for (TypeDeclaration<?> type : compilationUnit.getTypes()) {
      for (BodyDeclaration<?> member : type.getMembers()) {
        if (member instanceof MethodDeclaration) {
          MethodDeclaration method = (MethodDeclaration) member;
          if (isTestMethod(method)) {
            numberOfTestMethods += 1;
          }
        }
      }
    }
    return numberOfTestMethods;
  }

  /**
   * Output the minimizer's current progress.
   *
   * @param currentTestIndex the number of tests that have been minimized so far
   * @param totalTests the total number of tests in the input test suite
   * @param testName the current test method being minimized
   */
  private static void printProgress(int currentTestIndex, int totalTests, SimpleName testName) {
    System.out.println(
        currentTestIndex + "/" + totalTests + " tests minimized, Minimized method: " + testName);
  }

  /**
   * This is stolen from JavaParser's PrettyPrintVisitor.printOrphanCommentsBeforeThisChildNode,
   * with light modifications.
   *
   * @param node the node whose orphan comments to collect
   * @param result the list to add orphan comments to. Is side-effected by this method. The
   *     implementation uses this to minimize the diffs against upstream.
   */
  @SuppressWarnings({
    "JdkObsolete", // for LinkedList
    "ReferenceEquality"
  })
  private static void getOrphanCommentsBeforeThisChildNode(final Node node, List<Comment> result) {
    if (node instanceof Comment) {
      return;
    }

    Node parent = node.getParentNode().orElse(null);
    if (parent == null) {
      return;
    }
    List<Node> everything = new LinkedList<>(parent.getChildNodes());
    sortByBeginPosition(everything);
    int positionOfTheChild = -1;
    for (int i = 0; i < everything.size(); i++) {
      if (everything.get(i) == node) positionOfTheChild = i;
    }
    if (positionOfTheChild == -1) {
      throw new AssertionError("I am not a child of my parent.");
    }
    int positionOfPreviousChild = -1;
    for (int i = positionOfTheChild - 1; i >= 0 && positionOfPreviousChild == -1; i--) {
      if (!(everything.get(i) instanceof Comment)) positionOfPreviousChild = i;
    }
    for (int i = positionOfPreviousChild + 1; i < positionOfTheChild; i++) {
      Node nodeToPrint = everything.get(i);
      if (!(nodeToPrint instanceof Comment))
        throw new RuntimeException(
            "Expected comment, instead "
                + nodeToPrint.getClass()
                + ". Position of previous child: "
                + positionOfPreviousChild
                + ", position of child "
                + positionOfTheChild);
      result.add((Comment) nodeToPrint);
    }
  }
}
