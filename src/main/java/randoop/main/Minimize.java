package randoop.main;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
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
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import plume.Option;
import plume.OptionGroup;
import plume.Options;
import plume.TimeLimitProcess;

/**
 * This program minimizes a failing JUnit test suite. Its three command-line arguments are:
 *
 * <ol>
 *   <li>the Java file whose failing tests will be minimized
 *   <li>optional classpath containing dependencies needed to compile and run the Java file
 *   <li>optional timeout limit, in seconds, allowed for the whole test suite to run. The default is
 *       30 seconds.
 * </ol>
 *
 * <p>In a method that contains a failing assertion, the program will iterate through the method's
 * list of statements, from last to first. For each statement, possible replacement statements are
 * considered, from most minimized to least minimized. Removing the statement is the most a
 * statement can be minimized. Leaving the statement unchanged is the least that the statement can
 * be minimized.
 *
 * <p>If a replacement causes the output test suite to fail differently than the original test
 * suite, the algorithm tries a different replacement. If no replacement allows the output test
 * suite to fail in the same way as the original test suite, the algorithm adds back the original
 * version of the current statement and continues.
 */
public class Minimize extends CommandHandler {
  @OptionGroup(value = "Test case minimization options")
  /** The Java file whose failing tests will be minimized. */
  @Option("File containing the JUnit test suite to be minimized")
  public static String suitepath;

  /**
   * Classpath that includes dependencies needed to compile and run the JUnit test suite being
   * minimized.
   */
  @Option("Classpath to compile and run the JUnit test suite")
  public static String suiteclasspath;

  /** The maximum number of seconds allowed for the entire test suite to run. */
  @Option("Timeout, in seconds, for the whole test suite")
  public static int testsuitetimeout = 30;

  /** Output verbose output to standard output if true. */
  @Option("Verbose, flag for verbose output")
  public static boolean verboseminimizer = false;

  public Minimize() {
    super(
        "minimize",
        "Minimize a failing JUnit test suite.",
        "minimize",
        "",
        "Minimize a failing JUnit test suite.",
        null,
        "Path to Java file whose failing tests will be minimized, classpath to compile and run the Java file, maximum time (in seconds) allowed for a single unit test case to run before it times out.",
        "A minimized JUnit test suite (as one Java file) named \"InputFileMinimized.java\" if \"InputFile.java\" were the name of the input file.",
        "java -ea -cp bin:randoop-all-3.1.1.jar randoop.main.Main minimize --suitepath=~/RandoopTests/src/ErrorTestLang.java --suiteclasspath=~/RandoopTests/commons-lang3-3.5.jar --testsuitetimeout=30",
        new Options(Minimize.class));
  }

  private static final String PATH_SEPARATOR = System.getProperty("path.separator");
  private static final String SYSTEM_CLASS_PATH = System.getProperty("java.class.path");

  // The suffix to postpend onto the name of the minimized file and class.
  private static final String SUFFIX = "Minimized";

  /**
   * Check that the required parameters have been specified by the command-line options and then
   * call the mainMinimize method.
   *
   * @param args parameters, specified by keyword, for the input file, the classpath, the timeout
   *     value and the verbose flag
   * @return true if the command was handled successfully
   * @throws RandoopTextuiException thrown if incorrect arguments are passed
   */
  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {
    try {
      String[] nonargs = foptions.parse(args);
      if (nonargs.length > 0) {
        throw new Options.ArgException("Unrecognized arguments: " + Arrays.toString(nonargs));
      }
    } catch (Options.ArgException ae) {
      usage("while parsing command-line arguments: %s", ae.getMessage());
    }

    if (Minimize.suitepath == null) {
      System.out.println("You must specify an input file path.");
      System.out.println("Use the --suitepath option.");
      System.exit(1);
    }

    // Check that the input file is a Java file.
    if (!FilenameUtils.getExtension(Minimize.suitepath).equals("java")) {
      System.out.println("The input file must be a Java file.");
      System.exit(1);
    }

    if (Minimize.testsuitetimeout <= 0) {
      System.out.println("You must specify a positive timeout value.");
      System.exit(1);
    }

    // Call the main minimize method.
    return mainMinimize(suitepath, suiteclasspath, testsuitetimeout, verboseminimizer);
  }

  /**
   * Minimize the input test file.
   *
   * <p>Given an input Java file, minimization produces an output file that is as small as possible
   * (as few lines of code as possible) and that fails the same way:
   *
   * <ol>
   *   <li>Same failing assertions as in the original input test suite.
   *   <li>Same stacktrace produced by failing assertions.
   * </ol>
   *
   * <p>The original input Java file will be compiled and run once. The "expected output" is a map
   * from test method name to failure stack trace. A method is included in the map only if the
   * method contains a failing assertion. Thus, the expected output of a test suite with no failing
   * tests will be empty.
   *
   * @param filePath the path to the Java file that is being minimized
   * @param classPath classpath used to compile and run the Java file
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @param verboseOutput whether to produce verbose output
   * @return true if minimization produced a (possibly unchanged) file that fails the same way as
   *     the original file
   */
  public static boolean mainMinimize(
      String filePath, String classPath, int timeoutLimit, boolean verboseOutput) {
    System.out.println("Reading and parsing: " + filePath);

    // Read and parse input Java file.
    CompilationUnit compUnit;
    try (FileInputStream inputStream = new FileInputStream(filePath)) {
      compUnit = JavaParser.parse(inputStream);
    } catch (ParseException e) {
      System.err.println("Error parsing Java file: " + filePath);
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      System.err.println("Error reading Java file: " + filePath);
      e.printStackTrace();
      return false;
    }

    System.out.println("Getting expected output.");

    // Find the package name of the input file if it has one.
    String packageName = null;
    try {
      PackageDeclaration classPackage = compUnit.getPackage();
      if (classPackage != null) {
        packageName = classPackage.getPackageName();
      }
    } catch (NoSuchElementException e) {
      // No package declaration.
    }

    File originalFile = new File(filePath);

    // Create a new file; the file and the class within will have "Minimized" postpended.
    String minimizedFileName =
        new StringBuilder(filePath).insert(filePath.lastIndexOf('.'), SUFFIX).toString();
    File minimizedFile = new File(minimizedFileName);
    // Rename the overall class to [original class name][suffix].
    String origClassName = FilenameUtils.removeExtension(originalFile.getName());
    new ClassRenamer().visit(compUnit, new String[] {origClassName, SUFFIX});
    // Write the compilation unit to the minimized file.
    writeToFile(compUnit, minimizedFile);

    // Compile the Java file and check that the exit value is 0.
    if (compileJavaFile(minimizedFile, classPath, packageName, timeoutLimit) != 0) {
      System.err.println("Error when compiling file " + filePath + ". Aborting.");
      return false;
    }

    String runResult = runJavaFile(minimizedFile, classPath, packageName, timeoutLimit);

    // expectedOutput is a map from method name to failure stack trace
    // with line numbers removed.
    Map<String, String> expectedOutput = normalizeJUnitOutput(runResult);

    System.out.println("Minimizing: " + filePath);

    // Minimize the Java test suite.
    minimizeTestSuite(
        compUnit,
        packageName,
        minimizedFile,
        classPath,
        expectedOutput,
        timeoutLimit,
        verboseOutput);

    // Cleanup: simplify variable type names and sort the import statements.
    compUnit =
        simplifyVariableTypeNames(
            compUnit, packageName, minimizedFile, classPath, expectedOutput, timeoutLimit);

    writeToFile(compUnit, minimizedFile);

    System.out.println("Minimizing complete.\n");
    System.out.println("Original file length: " + getFileLength(originalFile) + " lines.");
    System.out.println("Minimized file length: " + getFileLength(minimizedFile) + " lines.");

    return true;
  }

  /**
   * Visit and minimize every JUnit test method within a compilation unit.
   *
   * @param cu the compilation unit to minimize; is modified by side effect
   * @param packageName the package that the Java file is in
   * @param file the Java file that is being minimized; is modified by side effect
   * @param classpath classpath used to compile and run the Java file
   * @param expectedOutput expected JUnit output when the Java file is compiled and run
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @param verboseOutput prints out information about minimization status if true
   */
  private static void minimizeTestSuite(
      CompilationUnit cu,
      String packageName,
      File file,
      String classpath,
      Map<String, String> expectedOutput,
      int timeoutLimit,
      boolean verboseOutput) {
    for (TypeDeclaration type : cu.getTypes()) {
      for (BodyDeclaration member : type.getMembers()) {
        if (member instanceof MethodDeclaration) {
          MethodDeclaration method = (MethodDeclaration) member;

          // Minimize the method only if it is a JUnit test method.
          if (isTestMethod(method)) {
            minimizeMethod(method, cu, packageName, file, classpath, expectedOutput, timeoutLimit);

            if (verboseOutput) {
              System.out.println("Minimized method " + method.getName() + ".");
            }
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
    // Iterate through the method's annotations and check for the test annotation.
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
   * @param compUnit compilation unit for the Java file that we are minimizing; is modified by side
   *     effect
   * @param packageName the package that the Java file is in
   * @param file the Java file that is being minimized; is modified by side effect
   * @param classpath classpath needed to compile and run the Java file
   * @param expectedOutput expected output from running the JUnit test suite
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   */
  private static void minimizeMethod(
      MethodDeclaration method,
      CompilationUnit compUnit,
      String packageName,
      File file,
      String classpath,
      Map<String, String> expectedOutput,
      int timeoutLimit) {
    List<Statement> statements = method.getBody().getStmts();

    // Map from primitive variable name to the variable's value which is
    // found in a passing assertion.
    Map<String, String> primitiveValues = new HashMap<String, String>();

    // Find all the names of the primitive and wrapped type variables.
    Set<String> primitiveAndWrappedTypeVars = new HashSet<String>();
    new VarDeclVisitor().visit(compUnit, primitiveAndWrappedTypeVars);

    // Iterate through the list of statements, from last to first.
    for (int i = statements.size() - 1; i >= 0; i--) {
      Statement currStmt = statements.get(i);

      // Remove the current statement. We will re-insert simplifications of it.
      statements.remove(i);

      // Obtain a list of possible replacements for the current statement.
      List<Statement> replacements = getStatementReplacements(currStmt, primitiveValues);
      boolean replacementFound = false;
      for (Statement stmt : replacements) {
        // Add replacement statement to the method's body.
        // If stmt is null, don't add anything since null represents removal of the statement.
        if (stmt != null) {
          statements.add(i, stmt);
        }

        // Write, compile, and run the new Java file.
        writeToFile(compUnit, file);
        if (checkCorrectlyMinimized(file, classpath, packageName, expectedOutput, timeoutLimit)) {
          // No compilation or runtime issues, obtained output is the same as the expected output.
          // Use simplification of this statement and continue with next statement.
          replacementFound = true;

          // Assertions are never simplified, only removed.
          // If currStmt is an assertion, then stmt is null.
          storeValueFromAssertion(currStmt, primitiveValues, primitiveAndWrappedTypeVars);
          break; // break replacement loop; continue statements loop.
        } else {
          // Issue encountered, remove the faulty replacement.
          if (stmt != null) {
            statements.remove(i);
          }
        }
      }

      if (!replacementFound) {
        // No correct simplification found.
        // Add back the original statement to the list of statements.
        statements.add(i, currStmt);
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
        if (mCall.getName().equals("assertTrue")) {
          List<Expression> mArgs = mCall.getArgs();
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
            if (binaryExp.getOperator().equals(BinaryExpr.Operator.equals)) {
              // Retrieve and store the value associated with the
              // variable in the assertion.
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
                if (primitiveAndWrappedTypeVars.contains(nameExpr.getName())) {
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
    List<Statement> replacements = new ArrayList<Statement>();

    // Null represents removal of the statement.
    replacements.add(null);

    if (currStmt instanceof ExpressionStmt) {
      Expression exp = ((ExpressionStmt) currStmt).getExpression();
      if (exp instanceof VariableDeclarationExpr) {
        // Create and return a list of possible replacement statements.
        VariableDeclarationExpr vdExpr = (VariableDeclarationExpr) exp;

        // Simplify right hand side to zero-equivalent value: 0, false,
        // or null.
        List<Statement> rhsZeroValStmts = rhsAssignZeroValue(vdExpr);
        replacements.addAll(rhsZeroValStmts);

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
   * declarations in a single statement, such as {@code int i, j, k;}.
   *
   * @param vdExpr variable declaration expression representing the current statement to simplify
   * @return a list of {@code Statement} objects representing the simplified variable declaration
   *     expression if the type of the variable is a primitive and a value has been previously
   *     calculated.
   */
  private static List<Statement> rhsAssignZeroValue(VariableDeclarationExpr vdExpr) {
    List<Statement> resultList = new ArrayList<Statement>();

    if (vdExpr.getVars().size() != 1) {
      // Number of variables declared in this expression is not 1.
      return resultList;
    }

    Type type = vdExpr.getType();
    if (type instanceof PrimitiveType) {
      // Replacement with zero value on the right hand side.
      resultList.add(rhsAssignWithValue(vdExpr, type, null));
    } else {
      // Replacement with null on the right hand side.
      resultList.add(rhsAssignWithValue(vdExpr, type, null));

      ReferenceType rType = (ReferenceType) type;
      if (rType.getType() instanceof ClassOrInterfaceType) {
        ClassOrInterfaceType classType = (ClassOrInterfaceType) rType.getType();
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
   * statement, such as {@code int i, j, k;}.
   *
   * @param vdExpr variable declaration expression representing the current statement to simplify
   * @param primitiveValues a map of primitive variable names to expressions representing their
   *     values
   * @return a {@code Statement} object representing the simplified variable declaration expression
   *     if the type of the variable is a primitive and a value has been previously calculated.
   *     Otherwise, {@code null} is returned. Also returns {@code null} if more than one variable is
   *     declared in the {@code VariableDeclarationExpr}.
   */
  private static Statement rhsAssignValueFromPassingAssertion(
      VariableDeclarationExpr vdExpr, Map<String, String> primitiveValues) {
    if (vdExpr.getVars().size() != 1) {
      // Number of variables declared in this expression is not one.
      return null;
    }
    // Get the name of the variable being declared.
    String varName = vdExpr.getVars().get(0).getId().getName();

    // Check if the map contains a value found from a passing assertion.
    if (primitiveValues.containsKey(varName)) {
      String value = primitiveValues.get(varName);
      return rhsAssignWithValue(vdExpr, vdExpr.getType(), value);
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
   * @param exprType type of the variable declaration expression
   * @param value value that will be assigned to the variable being declared
   * @return a {@code Statement} object representing the simplified variable declaration expression
   *     if the type of the variable is a primitive and a value has been previously calculated.
   *     Returns {@code null} if more than one variable is declared in the {@code
   *     VariableDeclarationExpr}.
   */
  private static Statement rhsAssignWithValue(
      VariableDeclarationExpr vdExpr, Type exprType, String value) {
    if (vdExpr.getVars().size() != 1) {
      // Number of variables declared in this expression is not 1.
      return null;
    }

    // Create the resulting expression, a copy of the original expression
    // which will be modified and returned.
    VariableDeclarationExpr resultExpr = (VariableDeclarationExpr) vdExpr.clone();

    // The variable declaration.
    VariableDeclarator vd = resultExpr.getVars().get(0);

    // Based on the declared variable type, set the right hand to the value
    // that was passed in.
    if (exprType instanceof PrimitiveType) {
      switch (((PrimitiveType) exprType).getType()) {
        case Boolean:
          if (value == null) {
            vd.setInit(new BooleanLiteralExpr(Boolean.parseBoolean("false")));
          } else {
            vd.setInit(new BooleanLiteralExpr(Boolean.parseBoolean(value)));
          }
          break;
        case Char:
        case Byte:
        case Short:
        case Int:
          if (value == null) {
            vd.setInit(new IntegerLiteralExpr("0"));
          } else {
            vd.setInit(new IntegerLiteralExpr(value));
          }
          break;
        case Float:
          if (value == null) {
            vd.setInit(new DoubleLiteralExpr("0f"));
          } else {
            vd.setInit(new DoubleLiteralExpr(value));
          }
          break;
        case Double:
          if (value == null) {
            vd.setInit(new DoubleLiteralExpr("0.0"));
          } else {
            vd.setInit(new DoubleLiteralExpr(value));
          }
          break;
        case Long:
          if (value == null) {
            vd.setInit(new LongLiteralExpr("0L"));
          } else {
            vd.setInit(new LongLiteralExpr(value));
          }
          break;
      }
    } else {
      // Set right hand side to null.
      vd.setInit(new NullLiteralExpr());
    }

    // Return a new statement with the simplified expression.
    return new ExpressionStmt(resultExpr);
  }

  /**
   * Return a statement that contains only the right hand side of a given statement. Returns null if
   * there are multiple variable declarations in a single statement, such as {@code int i, j, k;}.
   *
   * @param vdExpr variable declaration expression that represents the statement to simplify
   * @return a {@code Statement} object that is equal to the right-hand-side of {@code vdExpr}.
   *     Returns {@code null} if more than one variable is declared in the {@code
   *     VariableDeclarationExpr}.
   */
  private static Statement removeLeftHandSideSimplification(VariableDeclarationExpr vdExpr) {
    if (vdExpr.getVars().size() > 1) {
      // More than 1 variable declared in this expression.
      return null;
    }

    // Create the resulting expression, a copy of the original expression
    // which will be modified and returned.
    VariableDeclarationExpr resultExpr = (VariableDeclarationExpr) vdExpr.clone();
    List<VariableDeclarator> vars = resultExpr.getVars();
    VariableDeclarator vd = vars.get(0);

    // Return a new statement with only the right hand side.
    return new ExpressionStmt(vd.getInit());
  }

  /**
   * Simplify the variable type names in a compilation unit. For example, {@code java.lang.String}
   * should be simplified to {@code String}. Additionally sort the import statements of the
   * compilation unit.
   *
   * @param compUnit compilation unit containing an AST for a Java file, the compilation unit will
   *     be modified if a correct minimization of the method is found
   * @param packageName the package that the Java file is in
   * @param file the Java file to simplify; is modified by side effect
   * @param classpath classpath needed to compile and run the Java file
   * @param expectedOutput expected standard output from running the JUnit test suite
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @return {@code CompilationUnit} with fully-qualified type names simplified to simple type names
   */
  private static CompilationUnit simplifyVariableTypeNames(
      CompilationUnit compUnit,
      String packageName,
      File file,
      String classpath,
      Map<String, String> expectedOutput,
      int timeoutLimit) {

    // Set of fully-qualified type names that are used in variable declarations.
    Set<ClassOrInterfaceType> fullyQualifiedNames = new HashSet<ClassOrInterfaceType>();
    new ClassTypeVisitor().visit(compUnit, fullyQualifiedNames);

    // Map from fully-qualified type name to simple type name.
    Map<String, String> typeNameMap = new HashMap<String, String>();
    for (ClassOrInterfaceType type : fullyQualifiedNames) {
      String typeName = type.getScope() + "." + type.getName();
      typeNameMap.put(typeName, type.getName());

      // Add an import statement for the type.
      addImport(compUnit, typeName);
    }

    CompilationUnit result = compUnit;
    List<ImportDeclaration> imports = result.getImports();
    for (String type : typeNameMap.keySet()) {
      String compUnitStr = result.toString();

      // Replace all instances of the fully-qualified type name with the
      // simple type name.
      if (compUnitStr.contains(type)) {
        compUnitStr = compUnitStr.replace(type, typeNameMap.get(type));
      }
      CompilationUnit compUnitWithSimpleTypeNames;
      try {
        // Parse the compilation unit and set the imports.
        compUnitWithSimpleTypeNames = JavaParser.parse(IOUtils.toInputStream(compUnitStr, "UTF-8"));
        compUnitWithSimpleTypeNames.setImports(imports);
      } catch (ParseException e) {
        System.err.println("Error parsing into a compilation unit.");
        continue;
      } catch (IOException e) {
        System.err.println("IOUtils error creating input stream from string.");
        continue;
      }

      // Check that the simplification is correct.
      writeToFile(result, file);
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
      File file,
      String classpath,
      String packageName,
      Map<String, String> expectedOutput,
      int timeoutLimit) {
    // Zero exit status means success.
    if (compileJavaFile(file, classpath, packageName, timeoutLimit) != 0) {
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
   * @return exit value of compiling the Java file
   */
  private static int compileJavaFile(
      File file, String classpath, String packageName, int timeoutLimit) {
    // Obtain directory to carry out compilation and execution step.
    File executionDir = getExecutionDirectory(file, packageName);

    // Command to compile the input Java file.
    String command = "javac -classpath " + SYSTEM_CLASS_PATH;
    // Add current directory to class path.
    command += PATH_SEPARATOR + ".";
    if (classpath != null) {
      // Add specified classpath to command.
      command += PATH_SEPARATOR + classpath;
    }

    command += " " + file.getAbsolutePath();

    // Compile specified Java file.
    return runProcess(command, executionDir, timeoutLimit).exitValue;
  }

  /**
   * Run a Java file and return the standard output.
   *
   * @param file the file to be compiled and executed
   * @param userClassPath dependencies and complete classpath to compile and run the Java program
   * @param packageName the package that the Java file is in
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @return standard output from running the Java file
   */
  private static String runJavaFile(
      File file, String userClassPath, String packageName, int timeoutLimit) {
    // Obtain directory to carry out compilation and execution step.
    File executionDir = getExecutionDirectory(file, packageName);

    // Directory path for the classpath.
    String dirPath = ".";
    if (file.getParentFile() != null) {
      dirPath += PATH_SEPARATOR + file.getParentFile().getPath();
    }

    // Classpath for running the Java file.
    String classpath = SYSTEM_CLASS_PATH + PATH_SEPARATOR + dirPath;
    if (userClassPath != null) {
      classpath += PATH_SEPARATOR + userClassPath;
    }

    // Fully-qualified classname.
    String fqClassName = FilenameUtils.removeExtension(file.getName());
    if (packageName != null) {
      fqClassName = packageName + "." + fqClassName;
    }

    String command = "java -classpath " + classpath + " org.junit.runner.JUnitCore " + fqClassName;

    // Run the specified Java file and return the standard output.
    return runProcess(command, executionDir, timeoutLimit).stdout;
  }

  /**
   * Get directory to execute command in, given file path and package name. Returns a {@code File}
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
   * @return the directory to execute the commands in. Null if packageName is null.
   */
  private static File getExecutionDirectory(File file, String packageName) {
    if (packageName == null) {
      return null;
    }

    // Determine how many layers above we should be executing the process in.
    // Add 2 which is for the case where the file is located in a single layer of packaging.
    int foldersAbove = StringUtils.countMatches(packageName, ".") + 2;
    for (int i = 0; i < foldersAbove; i++) {
      file = file.getParentFile();
    }

    // Return the directory.
    return file;
  }

  /**
   * Run a command given as a String and return the output and error results in an Outputs object.
   *
   * @param command the input command to be run
   * @param executionDir the directory where the process commands should be executed
   * @param timeoutLimit number of seconds allowed for the whole test suite to run
   * @return an {@code Outputs} object containing the standard and error output
   */
  private static Outputs runProcess(String command, File executionDir, int timeoutLimit) {
    Process process;

    if (executionDir != null && executionDir.toString().isEmpty()) {
      // Execute command in the default directory.
      executionDir = null;
    }

    try {
      process = Runtime.getRuntime().exec(command, null, executionDir);
    } catch (IOException e) {
      return new Outputs("", "I/O error occurred when running process.", 1);
    }

    final TimeLimitProcess timeLimitProcess = new TimeLimitProcess(process, timeoutLimit * 1000);

    Callable<String> stdCallable =
        new Callable<String>() {
          @Override
          public String call() throws Exception {
            try {
              return IOUtils.toString(timeLimitProcess.getInputStream(), Charset.defaultCharset());
            } catch (IOException e) {
              // Error reading from process's input stream.
              return "Error reading from process's input stream.";
            }
          }
        };

    Callable<String> errCallable =
        new Callable<String>() {
          @Override
          public String call() throws Exception {
            try {
              return IOUtils.toString(timeLimitProcess.getErrorStream(), Charset.defaultCharset());
            } catch (IOException e) {
              // Error reading from process's error stream.
              return "Error reading from process's error stream.";
            }
          }
        };

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
    Future<String> stdOutput = fixedThreadPool.submit(stdCallable);
    Future<String> errOutput = fixedThreadPool.submit(errCallable);
    fixedThreadPool.shutdown();

    // Wait for the TimeLimitProcess to finish.
    try {
      timeLimitProcess.waitFor();
    } catch (InterruptedException e) {
      return new Outputs("", "Process was interrupted while waiting.", 1);
    }
    if (timeLimitProcess.timed_out()) {
      timeLimitProcess.destroy();
      return new Outputs("", "Process timed out after " + timeoutLimit + " seconds.", 1);
    }

    String stdOutputString;
    String errOutputString;
    try {
      stdOutputString = stdOutput.get();
      errOutputString = errOutput.get();
    } catch (InterruptedException e) {
      return new Outputs("", "Process was interrupted while waiting.", 1);
    } catch (ExecutionException e) {
      return new Outputs("", "A computation in the process threw an exception.", 1);
    }

    // Collect and return the results from the standard output and error
    // output.
    return new Outputs(stdOutputString, errOutputString, timeLimitProcess.exitValue());
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
    Map<String, String> resultMap = new HashMap<String, String>();

    StringBuilder result = new StringBuilder();
    try {
      // JUnit output starts with index 1 for first failure.
      int index = 1;

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
          result.append(line + "\n");
        }
      }
      bufReader.close();
    } catch (IOException e) {
      // Error with readLine() or close().
      System.err.println("I/O error reading line from trace.");
      e.printStackTrace();
      System.exit(1);
      return null;
    }

    return resultMap;
  }

  /**
   * Write a compilation unit to a Java file.
   *
   * @param compUnit the compilation unit to write to file
   * @param file file to write to
   */
  private static void writeToFile(CompilationUnit compUnit, File file) {
    // Write the compilation unit to the file.
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
      bw.write(compUnit.toString());
    } catch (IOException e) {
      System.err.println("Error writing to file: " + file);
      e.printStackTrace();
      System.exit(1);
    }
  }

  /** Visit every class or interface type. */
  private static class ClassRenamer extends VoidVisitorAdapter<Object> {
    /**
     * Rename the overall class to class name + suffix.
     *
     * @param arg a String array where the first element is the class name and the second element is
     *     the suffix that we will append
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
      String[] params = (String[]) arg;
      String className = params[0];
      String suffix = params[1];
      if (className.equals(n.getName())) {
        n.setName(className + suffix);
      }
    }
  }

  /** Visit every class or interface type. */
  private static class ClassTypeVisitor extends VoidVisitorAdapter<Object> {
    /**
     * If the class or interface type is in a package that's not visible by default, add the type to
     * the set of types that is passed in as an argument. For instance, suppose that the type {@code
     * org.apache.commons.lang3.MutablePair} appears in the program. This type will be added to the
     * set of types. This is used for type name simplifications to simplify {@code
     * org.apache.commons.lang3.MutablePair} into {@code MutablePair} after adding the import
     * statement {@code import org.apache.commons.lang3.MutablePair;}.
     *
     * @param arg a set of {@code Type} objects; will be modified if the class or interface type is
     *     a non-visible type by default
     */
    @SuppressWarnings("unchecked")
    @Override
    public void visit(ClassOrInterfaceType n, Object arg) {
      Set<ClassOrInterfaceType> params = (Set<ClassOrInterfaceType>) arg;
      // Add the type to the set if it's not a visible type be default.
      if (n.toString().contains(".")) {
        params.add(n);
      }
    }
  }

  /** Visit every variable declaration. */
  private static class VarDeclVisitor extends VoidVisitorAdapter<Object> {
    /**
     * Visit every variable declaration.
     *
     * @param arg a set containing the names of all the primitive and wrapped type variables.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void visit(VariableDeclarationExpr n, Object arg) {
      Set<String> variables = (Set<String>) arg;

      ClassOrInterfaceType classType = null;
      if (n.getType() instanceof ReferenceType) {
        ReferenceType rType = (ReferenceType) n.getType();
        if (rType.getType() instanceof ClassOrInterfaceType) {
          classType = (ClassOrInterfaceType) rType.getType();
        }
      }

      // Check if the variable's type is a primitive or a wrapped type.
      if (n.getType() instanceof PrimitiveType || (classType != null && classType.isBoxedType())) {
        for (VariableDeclarator vd : n.getVars()) {
          variables.add(vd.getId().getName());
        }
      }
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
    ImportDeclaration importDeclaration;

    try {
      importDeclaration = JavaParser.parseImport(importStr);
    } catch (ParseException e) {
      // Unexpected error from parsing import.
      System.err.println("Error parsing import: " + importName);
      return;
    }

    List<ImportDeclaration> importDeclarations = compilationUnit.getImports();
    for (ImportDeclaration im : importDeclarations) {
      String currImportStr = im.toString().trim();

      // Check if the compilation unit already includes the import exactly.
      if (importStr.equals(currImportStr)) {
        return;
      }
      // Check if the compilation unit already includes the import as a wildcard.
      // Get index of last separator.
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

  /**
   * Sort a compilation unit's imports by name.
   *
   * @param compilationUnit the compilation unit whose imports will be sorted by name
   */
  private static void sortImports(CompilationUnit compilationUnit) {
    List<ImportDeclaration> imports = compilationUnit.getImports();

    Collections.sort(
        imports,
        new Comparator<ImportDeclaration>() {
          @Override
          public int compare(ImportDeclaration o1, ImportDeclaration o2) {
            return o1.getName().toString().compareTo(o2.getName().toString());
          }
        });

    compilationUnit.setImports(imports);
  }

  /** Contains the standard output, standard error, and exit status from running a process. */
  private static class Outputs {
    /** The standard output. */
    private String stdout;
    /** The error output. */
    private String errout;

    /** Exit value from running a process. */
    private int exitValue;

    /**
     * Create an Outputs object.
     *
     * @param stdout standard output
     * @param errout error output
     * @param exitValue exit value of process
     */
    private Outputs(String stdout, String errout, int exitValue) {
      this.stdout = stdout;
      this.errout = errout;
      this.exitValue = exitValue;
    }
  }

  /**
   * Calculate the length of a file, by number of lines.
   *
   * @param file the file to compute the length of.
   * @return the number of lines in the file. Returns -1 if an exception occurs while reading the
   *     file
   */
  private static int getFileLength(File file) {
    int lines = 0;
    try {
      // Read and count the number of lines in the file.
      BufferedReader reader = new BufferedReader(new FileReader(file));
      while (reader.readLine() != null) {
        lines++;
      }
      reader.close();
    } catch (FileNotFoundException e) {
      System.err.println("File length not calculated, file not found exception for file " + file);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("File length not calculated, file read exception for file " + file);
      System.exit(1);
    }
    return lines;
  }

  /**
   * Print out usage error and stack trace and then exit.
   *
   * @param format the string format
   * @param args the arguments
   */
  private void usage(String format, Object... args) {
    System.out.print("ERROR: ");
    System.out.printf(format, args);
    System.out.println();
    System.out.println(foptions.usage());
    System.exit(-1);
  }
}
