package randoop.main;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.emory.mathcs.backport.java.util.Collections;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.io.IOUtils;
import plume.Option;
import plume.OptionGroup;
import plume.Options;
import plume.TimeLimitProcess;

/**
 * This program minimizes a failing JUnit test suite. Its three command-line arguments are:
 *
 * <p>
 *
 * <ol>
 *   <li>the Java file whose failing tests will be minimized
 *   <li>optional classpath containing dependencies needed to compile and run the Java file
 *   <li>optional timeout limit, in seconds, allowed for the whole test suite to be run. The default
 *       is 30 seconds.
 * </ol>
 *
 * <p>In a method that contains a failing assertion, the program will iterate through the method's
 * list of statements, from last to first. For each statement, possible replacement statements are
 * considered, from most minimized to least minimized. Removing the statement is the most a
 * statement can be minimized. Leaving the statement unchanged is the least that the statement can
 * be minimized.
 *
 * <p>
 *
 * <p>The algorithm first tries to remove the statement. If this causes the output test suite to
 * fail differently than the original test suite, we will clone the current statement and then
 * modify it to represent the different possible replacements. If none of these minimizations allow
 * the output test suite to fail in the same way as the original test suite, we add back the
 * original version of the current statement and continue.
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

  private static final String pathSeparator = System.getProperty("path.separator");
  private static final String systemClassPath = System.getProperty("java.class.path");

  /**
   * Check that the required parameters have been specified by the command-line options and then
   * call the mainMinimize method.
   *
   * @param args first parameter is the path to the Java file to be minimized. The second parameter
   *     is the classpath needed to compile and run the Java file. The third parameter is the
   *     timeout time, in seconds, for the whole test suite.
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
    } else if (Minimize.testsuitetimeout <= 0) {
      System.out.println("You must specify a positive timeout value.");
      System.exit(1);
    }

    // Call the main minimize method
    return mainMinimize(suitepath, suiteclasspath, testsuitetimeout);
  }

  /**
   * Minimize the input test file.
   *
   * <p>
   *
   * <p>Given an input file, minimization produces an output file that is as small as possible (as
   * few lines of code as possible) and that fails the same way:
   *
   * <p>
   *
   * <ol>
   *   <li>Same failing assertions as in the original input test suite.
   *   <li>Same stacktrace produced by failing assertions.
   * </ol>
   *
   * <p>
   *
   * <p>The original input Java file will be compiled and run once. The "expected output" is a map
   * from method name to failure stack trace. A method is included in the map only if the method
   * contains a failing assertion. Thus, the expected output of a test suite with no failing tests
   * will be empty.
   *
   * @param filePath the path to the Java file that is being minimized
   * @param classPath classpath used to compile and run the Java file
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   * @return true if minimization produced a (possibly unchanged) file that fails the same way as
   *     the original file
   */
  public static boolean mainMinimize(String filePath, String classPath, int timeoutLimit) {
    System.out.println("Reading and parsing: " + filePath);

    // Read and parse input Java file.
    CompilationUnit compUnit;
    try (FileInputStream inputStream = new FileInputStream(filePath)) {
      compUnit = JavaParser.parse(inputStream);
    } catch (ParseException e) {
      System.err.println("Error parsing Java file: " + filePath);
      System.err.println(e);
      return false;
    } catch (IOException e) {
      System.err.println("Error reading Java file: " + filePath);
      System.err.println(e);
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

    // Write to a new file with 'Minimized' appended to the name.
    String newFilePath = writeToFile(compUnit, filePath, "Minimized");

    // Compile the Java file and check that the exit value is zero.
    if (compileJavaFile(newFilePath, classPath, packageName, timeoutLimit) != 0) {
      System.err.println("Error when compiling file " + filePath + ". Aborting.");
      return false;
    }
    // Run the Java file.
    String runResult = runJavaFile(newFilePath, classPath, packageName, timeoutLimit);

    // Keep track of the expected output, a map from method name to failure stack trace.
    Map<String, String> expectedOutput = normalizeJUnitOutput(runResult);

    System.out.println("Minimizing: " + filePath);

    // Minimize the Java test suite, simplify variable type names, sort the import statements, and write to a new file.
    minimizeTestSuite(compUnit, packageName, filePath, classPath, expectedOutput, timeoutLimit);
    compUnit =
        simplifyVariableTypeNames(
            compUnit, packageName, filePath, classPath, expectedOutput, timeoutLimit);
    sortImports(compUnit);

    writeToFile(compUnit, filePath, "Minimized");

    System.out.println("Minimizing complete.\n");

    // Output original and minimized file lengths.
    System.out.println("Original file length: " + getFileLength(filePath) + " lines.");
    System.out.println("Minimized file length: " + getFileLength(newFilePath) + " lines.");

    return true;
  }

  /**
   * Visit and minimize every method within a compilation unit.
   *
   * @param cu the compilation unit to minimize, will be modified by side effect
   * @param packageName the name of the package that the Java file is in
   * @param filePath the path to the Java file that is being minimized
   * @param classpath classpath used to compile and run the Java file
   * @param expectedOutput expected JUnit output when the Java file is compiled and run
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   */
  private static void minimizeTestSuite(
      CompilationUnit cu,
      String packageName,
      String filePath,
      String classpath,
      Map<String, String> expectedOutput,
      int timeoutLimit) {
    for (TypeDeclaration type : cu.getTypes()) {
      for (BodyDeclaration member : type.getMembers()) {
        if (member instanceof MethodDeclaration) {
          MethodDeclaration method = (MethodDeclaration) member;

          // Get method's annotations.
          List<AnnotationExpr> annotationExprs = method.getAnnotations();
          if (annotationExprs.toString().contains("@Test")) {
            // Minimize the method only if it is a JUnit test method.
            minimizeMethod(
                method, cu, packageName, filePath, classpath, expectedOutput, timeoutLimit);
            System.out.println("Minimized method " + method.getName());
          }
        }
      }
    }
  }

  /**
   * Minimize a method by minimizing each statement in turn.
   *
   * @param method the method to minimize, will be modified by side effect minimization of the
   *     method is found
   * @param compUnit compilation unit for the Java file that we are minimizing; will be modified by
   *     side effect
   * @param packageName the name of the package that the Java file is in
   * @param filePath path to the Java file that we are minimizing
   * @param classpath classpath needed to compile and run the Java file
   * @param expectedOutput expected output from running the JUnit test suite
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   */
  private static void minimizeMethod(
      MethodDeclaration method,
      CompilationUnit compUnit,
      String packageName,
      String filePath,
      String classpath,
      Map<String, String> expectedOutput,
      int timeoutLimit) {
    List<Statement> statements = method.getBody().getStmts();

    // Map from primitive variable name to the variable's value which is
    // found in a passing assertion.
    Map<String, String> primitiveValues = new HashMap<String, String>();

    // Iterate through the list of statements, from last to first
    for (int i = statements.size() - 1; i >= 0; i--) {
      Statement currStmt = statements.get(i);

      // Remove the current statement. We will re-insert simplifications of it.
      statements.remove(i);

      // Obtain a list of possible replacements for the current statement.
      List<Statement> replacements = getStatementReplacements(currStmt, primitiveValues);
      boolean replacementFound = false;
      for (Statement stmt : replacements) {
        // Add replacement statement to the method's body. If stmt is null, don't add anything since null represents removal of the statement.
        if (stmt != null) {
          statements.add(i, stmt);
        }

        // Write, compile, and run the new Java file with the new suffix "Minimized".
        String newFilePath = writeToFile(compUnit, filePath, "Minimized");
        if (checkCorrectlyMinimized(
            newFilePath, classpath, packageName, expectedOutput, timeoutLimit)) {
          // No compilation or runtime issues, obtained output is the same as the expected output.
          // Use simplification of this statement and continue with next statement.
          replacementFound = true;

          // Assertions are never simplified; if this is an assertion, it's the original statement.
          storeValueFromAssertion(currStmt, primitiveValues);
          break;
        } else {
          // Issue encountered, remove the faulty statement.
          if (stmt != null) {
            statements.remove(i);
          }
        }
      }
      if (!replacementFound) {
        // No other correct simplifications found, add back the
        // original statement to the list of statements.
        statements.add(i, currStmt);
      }
    }
  }

  /**
   * Return a list of statements that are a simplification of a given statement, in order from most
   * to least minimized. The possible minimizations are:
   *
   * <p>
   *
   * <ul>
   *   <li>Remove a statement, represented by null
   *   <li>Replace the right hand side expression with {@code 0}, {@code false}, or {@code null}
   *   <li>Replace right hand side by a calculated value obtained from a passing assertion
   *   <li>Remove the left hand side of a statement, retaining only the expression on the right
   * </ul>
   *
   * <p>Assertions are never simplified.
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

        // Simplify right hand side to zero equivalent value, 0, false,
        // or null.
        Statement rhsZeroValStmt = rhsAssignZeroValue(vdExpr);
        if (rhsZeroValStmt != null) {
          replacements.add(rhsZeroValStmt);
        }

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
   * If {@code currStmt} is a statement that is an assert true statement, store the value associated
   * with the variable in the {@code primitiveValues} map. The assertion must be a passing assertion
   * and must be using an '==' operator.
   *
   * @param currStmt a statement
   * @param primitiveValues a map of variable names to variable values, modified if {@code currStmt}
   *     is a passing assertion, asserting a variable's value.
   */
  private static void storeValueFromAssertion(
      Statement currStmt, Map<String, String> primitiveValues) {
    // Check if the statement is an assertion regarding a value that can be
    // used in a simplification later on.
    if (currStmt instanceof ExpressionStmt) {
      Expression exp = ((ExpressionStmt) currStmt).getExpression();
      if (exp instanceof MethodCallExpr) {
        MethodCallExpr mCall = (MethodCallExpr) exp;
        // Check if the method call is an assertTrue statement
        if (mCall.getName().equals("assertTrue")) {
          // Is an assertTrue statement.
          List<Expression> mArgs = mCall.getArgs();
          if (mArgs.size() == 1) {
            // Retrieve and store the value associated with the
            // variable in the assertion.
            Expression mExp = mArgs.get(0);
            if (mExp.toString().contains("==")) {
              List<Node> children = mExp.getChildrenNodes();
              String var = children.get(0).toString();
              String val = children.get(1).toString();
              primitiveValues.put(var, val);
            }
          }
        }
      }
    }
  }

  /**
   * Return a variable declaration statement that replaces the right hand side by 0, false, or null,
   * whichever is type correct. The input variable declaration statement is assumed to declare only
   * one variable, for instance, {@code int i = expr;}. Returns null if there are multiple variable
   * declarations in a single statement in the form {@code int i, j, k;}.
   *
   * @param vdExpr variable declaration expression representing the current statement to simplify
   * @return a {@code Statement} object representing the simplified variable declaration expression
   *     if the type of the variable is a primitive and a value has been previously calculated.
   *     Otherwise, {@code null} is returned. Also returns {@code null} if more than one variable is
   *     declared in the {@code VariableDeclarationExpr}.
   */
  private static Statement rhsAssignZeroValue(VariableDeclarationExpr vdExpr) {
    Type type = vdExpr.getType();
    if (type instanceof PrimitiveType) {
      switch (((PrimitiveType) type).getType()) {
        case Boolean:
          return rhsAssignWithValue(vdExpr, "false");
        case Char:
          return rhsAssignWithValue(vdExpr, "");
        default:
          return rhsAssignWithValue(vdExpr, "0");
      }
    }
    // Replacement with null on the right hand side.
    return rhsAssignWithValue(vdExpr, null);
  }

  /**
   * Return a variable declaration statement that simplifies the right hand side by a calculated
   * value for primitive types. The variable declaration statement is assumed to declare only one
   * variable, for instance, {@code int i;}. Returns null if there are multiple variable
   * declarations in a single statement in the form {@code int i, j, k;}.
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
      return rhsAssignWithValue(vdExpr, value);
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
   * @param value value that will be assigned to the variable being declared
   * @return a {@code Statement} object representing the simplified variable declaration expression
   *     if the type of the variable is a primitive and a value has been previously calculated.
   *     Returns {@code null} if more than one variable is declared in the {@code
   *     VariableDeclarationExpr}.
   */
  private static Statement rhsAssignWithValue(VariableDeclarationExpr vdExpr, String value) {
    if (vdExpr.getVars().size() != 1) {
      // Number of variables declared in this expression is not one.
      return null;
    }

    // Copy the variable declaration expression.
    VariableDeclarationExpr exprCopy = (VariableDeclarationExpr) vdExpr.clone();

    // Obtain a reference to the variable declaration.
    List<VariableDeclarator> vars = exprCopy.getVars();
    VariableDeclarator vd = vars.get(0);

    // Based on the declared variable type, set the right hand to the value
    // that was passed in.
    Type type = exprCopy.getType();
    if (type instanceof PrimitiveType) {
      switch (((PrimitiveType) type).getType()) {
        case Boolean:
          vd.setInit(new BooleanLiteralExpr(Boolean.parseBoolean(value)));
          break;
        case Byte:
          vd.setInit(new IntegerLiteralExpr(value));
          break;
        case Char:
          vd.setInit(new CharLiteralExpr(value));
          break;
        case Short:
          vd.setInit(new IntegerLiteralExpr(value));
          break;
        case Int:
          vd.setInit(new IntegerLiteralExpr(value));
          break;
        case Long:
          vd.setInit(new LongLiteralExpr(value));
          break;
        case Float:
          vd.setInit(new DoubleLiteralExpr(value));
          break;
        case Double:
          vd.setInit(new DoubleLiteralExpr(value));
          break;
      }
    } else {
      // Set right hand side to null.
      vd.setInit(new NullLiteralExpr());
    }
    // Create a new statement with the simplified expression.
    ExpressionStmt newStmt = new ExpressionStmt(exprCopy);
    exprCopy.setParentNode(newStmt);
    return newStmt;
  }

  /**
   * Return a statement that contains only the right hand side of a given statement. The variable
   * declaration statement is assumed to declare only one variable, for instance, {@code int i;}.
   * Multiple variable declarations in a single statement in the form {@code int i, j, k;} are not
   * valid.
   *
   * @param vdExpr variable declaration expression that represents the statement to simplify
   * @return a {@code Statement} object that is equal to {@code vdExpr} without the assignment to
   *     the declared variable. Returns {@code null} if more than one variable is declared in the
   *     {@code VariableDeclarationExpr}.
   */
  private static Statement removeLeftHandSideSimplification(VariableDeclarationExpr vdExpr) {
    // Copy the variable declaration expression.
    VariableDeclarationExpr exprCopy = (VariableDeclarationExpr) vdExpr.clone();
    List<VariableDeclarator> vars = exprCopy.getVars();
    if (vars.size() > 1) {
      // More than one variable declared in this expression.
      return null;
    }
    VariableDeclarator vd = vars.get(0);

    // Create a new statement with only the right hand side.
    ExpressionStmt newStmt = new ExpressionStmt(vd.getInit());
    exprCopy.setParentNode(newStmt);
    return newStmt;
  }

  /**
   * Simplify the variable type names in a compilation unit. For example, {@code java.lang.String}
   * should be simplified to {@code String}.
   *
   * @param compUnit compilation unit containing an AST for a Java file, the compilation unit will
   *     be modified if a correct minimization of the method is found
   * @param packageName the name of the package that the Java file is in
   * @param filePath absolute file path to the input Java file
   * @param classpath classpath needed to compile and run the Java file
   * @param expectedOutput expected standard output from running the JUnit test suite
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   * @return {@code CompilationUnit} with fully-qualified type names simplified to simple type names
   */
  private static CompilationUnit simplifyVariableTypeNames(
      CompilationUnit compUnit,
      String packageName,
      String filePath,
      String classpath,
      Map<String, String> expectedOutput,
      int timeoutLimit) {
    // Map from fully-qualified type name to simple type name.
    Map<String, String> typeNameMap = new HashMap<String, String>();
    // Set of fully qualified type names that are used in variable
    // declarations
    Set<Type> fullyQualifiedNames = new HashSet<Type>();
    // Collect all of the type names in the compilation unit.
    new TypeVisitor().visit(compUnit, fullyQualifiedNames);

    // Iterate through the set of fully qualified names and fill the map
    // with mappings from fully qualified names to simple type names. Also
    // add in necessary import statements.
    for (Type type : fullyQualifiedNames) {
      String typeName = type.toString();

      // If the type is a generic type, handle the main type, the other
      // elements are included in the list and are processed separately.
      int leftAngleBracketIndex = typeName.indexOf('<');
      if (leftAngleBracketIndex >= 0) {
        typeName = typeName.substring(0, leftAngleBracketIndex);
      }
      // Add an import statement to the compilation unit.
      addImport(compUnit, typeName);
      // Add to the map, the fully-qualified type name to the simple type name.
      typeNameMap.put(typeName, getSimpleTypeName(typeName));
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
      String newFilePath = writeToFile(result, filePath, "Minimized");
      if (checkCorrectlyMinimized(
          newFilePath, classpath, packageName, expectedOutput, timeoutLimit)) {
        result = compUnitWithSimpleTypeNames;
      }
    }
    return result;
  }

  /**
   * Get the simple type name of a fully qualified type name. For example, {@code java.lang.String}
   * should be simplified to {@code String}.
   *
   * @param typeName fully qualified type name
   * @return simple type name of {@code typeName}
   */
  public static String getSimpleTypeName(String typeName) {
    int indexOflastSeparator = typeName.lastIndexOf('.');
    return indexOflastSeparator > 0
        ? typeName.substring(indexOflastSeparator + 1, typeName.length())
        : typeName;
  }

  /**
   * Check if a Java file has been correctly minimized. The file should not have compilation errors
   * or runtime errors. The file should fail in the same way as the original file.
   *
   * @param filePath the absolute path to the Java file
   * @param classpath classpath needed to compile/run Java file
   * @param packageName the name of the package that the Java file is in
   * @param expectedOutput expected output of running JUnit test suite
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   * @return true if there are no compilation and no runtime errors and the output is equal to the
   *     expected output
   */
  private static boolean checkCorrectlyMinimized(
      String filePath,
      String classpath,
      String packageName,
      Map<String, String> expectedOutput,
      int timeoutLimit) {
    // Check that the exit value from compiling the Java file is zero.
    if (compileJavaFile(filePath, classpath, packageName, timeoutLimit) != 0) {
      return false;
    }

    // Run the Java file and get the standard output.
    String runResult = runJavaFile(filePath, classpath, packageName, timeoutLimit);

    // Compare the standard output with the expected output.
    return expectedOutput.equals(normalizeJUnitOutput(runResult));
  }

  /**
   * Compile a Java file and return the compilation exit value.
   *
   * @param filePath the absolute path to the Java file to be compiled and executed
   * @param classpath dependencies and complete classpath to compile and run the Java program
   * @param packageName the name of the package that the Java file is in
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   * @return exit value of compiling the Java file
   */
  private static int compileJavaFile(
      String filePath, String classpath, String packageName, int timeoutLimit) {
    // Obtain directory to carry out compilation and execution step.
    String executionDir = getExecutionDirectory(filePath, packageName);

    // Command to compile the input Java file
    String command = "javac -classpath " + systemClassPath;
    // Add current directory to class path
    command += pathSeparator + ".";
    if (classpath != null) {
      // Add specified classpath to command
      command += pathSeparator + classpath;
    }
    command += " " + filePath;

    // Compile specified Java file.
    return runProcess(command, executionDir, timeoutLimit).exitValue;
  }

  /**
   * Run a Java file and return the standard output.
   *
   * @param filePath the absolute path to the Java file to be compiled and executed
   * @param classpath dependencies and complete classpath to compile and run the Java program
   * @param packageName the name of the package that the Java file is in
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   * @return the standard output from running the Java file
   */
  private static String runJavaFile(
      String filePath, String classpath, String packageName, int timeoutLimit) {
    // Obtain directory to carry out compilation and execution step.
    String executionDir = getExecutionDirectory(filePath, packageName);

    // Obtain directory path from file path.
    Path fPath = Paths.get(filePath).getParent();
    // Directory path for the classpath.
    String dirPath = null;
    if (fPath != null) {
      dirPath = fPath.toString();
    }

    // Add the package name to the class name if it exists.
    String className = getClassName(filePath);
    if (packageName != null) {
      className = packageName + "." + className;
    }

    // Command to run the Java file.
    String command = "java -classpath " + systemClassPath;
    // Add current directory to class path.
    command += pathSeparator + ".";
    if (dirPath != null) {
      // Use directory as defined by the file path.
      command += pathSeparator + dirPath;
    }
    if (classpath != null) {
      // Add specified classpath to command.
      command += pathSeparator + classpath;
    }
    // Add JUnitCore command.
    command += " org.junit.runner.JUnitCore " + className;

    // Run the specified Java file.
    return runProcess(command, executionDir, timeoutLimit).stdout;
  }

  /**
   * Get directory to execute command in given file path and package name
   *
   * @param filePath the absolute file path to the input Java file
   * @param packageName package name of input Java file
   * @return String of the directory to execute the commands in. Null if packageName is null.
   */
  private static String getExecutionDirectory(String filePath, String packageName) {
    if (packageName == null) {
      return null;
    }

    String packageAsDirectory = packageName.replace(".", System.getProperty("file.separator"));
    int index = filePath.lastIndexOf(packageAsDirectory);
    if (index < 0) {
      return null;
    }
    return filePath.substring(0, index);
  }

  /**
   * Run a command given as a String and return the output and error results in an Outputs object.
   *
   * @param command the input command to be run
   * @param executionDir the directory where the process commands should be executed
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   * @return an {@code Outputs} object containing the standard and error output
   */
  private static Outputs runProcess(String command, String executionDir, int timeoutLimit) {
    Process process;

    if (executionDir == null || executionDir.isEmpty()) {
      // Execution directory is null, execute command in default
      // directory.
      try {
        process = Runtime.getRuntime().exec(command);
      } catch (IOException e) {
        return new Outputs("", "I/O error occurred when running process.", 1);
      }
    } else {
      // Input Java file is in a package, execute in the root directory.
      try {
        process = Runtime.getRuntime().exec(command, null, new File(executionDir));
      } catch (IOException e) {
        return new Outputs("", "I/O error occurred when running process.", 1);
      }
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
      return new Outputs("", "Process timed out after " + timeoutLimit * 1000 + " seconds.", 1);
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

    // Collect and return the results from the standard output and error output.
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
    String line;

    String methodName = null;
    Map<String, String> resultMap = new HashMap<String, String>();

    StringBuilder result = new StringBuilder();
    try {
      // JUnit output starts with index 1 for first failure
      int index = 1;

      // Read through the trace, line by line.
      while ((line = bufReader.readLine()) != null) {
        String indexStr = index + ") ";
        // Check if the current line is the start of a failure stack trace for a method.
        if (line.startsWith(indexStr)) {
          // If a previous failure stack trace is being read, add the existing method name
          // and stack trace to the map.
          if (methodName != null) {
            resultMap.put(methodName, result.toString());

            // Reset the string builder.
            result.setLength(0);
          }
          // Set the method name to the current line.
          methodName = line;
          index += 1;
        } else if (line.isEmpty()) {
          // Reached an empty line which marks the end of the JUnit output.
          resultMap.put(methodName, result.toString());
          break;
        } else if (methodName != null) {
          // Look for a left-parentheses which marks the position where a line number will appear.
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
   * Write a compilation unit to a Java file. The file that is written to is the file path obtained
   * by appending the suffix to the end of the file's name, before the file type suffix.
   *
   * @param compUnit the compilation unit to write to file
   * @param filePath the absolute path to the input Java file
   * @param suffix the suffix to append to the name of the new Java file
   * @return {@code String} representing the absolute path to the newly written Java file. Returns
   *     {@code null} if error occurred in writing to the new file
   */
  private static String writeToFile(CompilationUnit compUnit, String filePath, String suffix) {
    // Rename the overall class to [old class name][suffix].
    new ClassRenamer().visit(compUnit, new String[] {getClassName(filePath), suffix});

    // Create a new string to represent the new file name.
    String newFileName =
        new StringBuilder(filePath).insert(filePath.lastIndexOf('.'), suffix).toString();

    // Write the compilation unit to the new file.
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(newFileName))) {
      bw.write(compUnit.toString());
    } catch (IOException e) {
      System.err.println("Error writing to file: " + newFileName);
      e.printStackTrace();
      System.exit(1);
    }

    return newFileName;
  }

  /**
   * Given the absolute file path to a Java file, return the simple class name of the file.
   *
   * @param filePath absolute path to a Java file, the file path should not be null.
   * @return the simple class name
   */
  public static String getClassName(String filePath) {
    if (filePath == null) {
      System.err.println("File path should not be null.");
      System.exit(1);
    }

    // Get the name of the file without full path.
    String fileName = Paths.get(filePath).getFileName().toString();
    // Remove .java extension from file name.
    return fileName.substring(0, fileName.indexOf('.'));
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
  private static class TypeVisitor extends VoidVisitorAdapter<Object> {
    /**
     * If the class or interface type is in a package that's not visible by default, add the type to
     * the set of types that is passed in as an argument. For instance, suppose that the type {@code
     * org.apache.commons.lang3.MutablePair} appears in the program. This type will be added to the
     * set of types. This is used for type name simplifications to simplify {@code
     * org.apache.commons.lang3.MutablePair} into {@code MutablePair} after adding the import
     * statement {@code import org.apache.commons.lang3.MutablePair;}.
     *
     * @param arg a set of {@code Type} objects, will be modified if the class or interface type is
     *     a non-visible type by default
     */
    @SuppressWarnings("unchecked")
    @Override
    public void visit(ClassOrInterfaceType n, Object arg) {
      Set<Type> params = (Set<Type>) arg;
      // Add the type to the set if it's not a visible type be default.
      if (n.toString().contains(".")) {
        params.add(n);
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

      // Check if the compilation unit already includes the import.
      if (importStr.equals(currImportStr)) {
        return;
      }
      // Get index of last separator.
      int lastSeparator = importName.lastIndexOf('.');
      if (lastSeparator >= 0) {
        // Create a string representing a wildcard import.
        String wildcardName = importName.substring(0, lastSeparator);
        String wildcardImportStr = "import " + wildcardName + ".*" + ";";
        // If a wildcard import exists, don't need to add a redundant import.
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
   * @param filepath absolute file path to the input file
   * @return the number of lines in the file. Negative one is returned if an exception occurs from
   *     finding or reading the file
   */
  private static int getFileLength(String filepath) {
    int lines = 0;
    try {
      // Read and count the number of lines in the file.
      BufferedReader reader = new BufferedReader(new FileReader(filepath));
      while (reader.readLine() != null) {
        lines++;
      }
      reader.close();
    } catch (FileNotFoundException e) {
      System.err.println(
          "File length not calculated, file not found exception for file " + filepath);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("File length not calculated, file read exception for file " + filepath);
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
