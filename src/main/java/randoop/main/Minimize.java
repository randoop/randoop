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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
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
 * <ol>
 *   <li>the Java file whose failing tests will be minimized
 *   <li>optional classpath containing dependencies needed to compile and run the Java file
 *   <li>optional timeout limit, in seconds, allowed for the whole test suite to be run. The default
 *       is 30 seconds.
 * </ol>
 *
 * In a method that contains a failing assertion, the program will iterate through the method's list
 * of statements, from last to first. For each statement, possible replacement statements are
 * considered, from most minimized to least minimized. Removing the statement is the most a
 * statement can be minimized. Leaving the statement unchanged is the least that the statement can
 * be minimized. The algorithm first tries to remove the statement. If this causes the output test
 * suite to fail differently than the original test suite, we will clone the current statement and
 * then modify it to represent the different possible replacements. If none of these minimizations
 * allow the output test suite to fail in the same way as the original test suite, we add back the
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
  @Option("classpath to compile and run the JUnit test suite")
  public static String suiteclasspath;

  /**
   * The maximum number of seconds allowed for the entire test suite to be run. This is useful for
   * test cases that do not terminate when run. This timeout limit should be large enough such that
   * unit tests which do terminate have enough time to run until completion.
   */
  @Option("timeout, in seconds, for the whole test suite")
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
        "java -ea -cp bin:randoop-all-3.0.9.jar randoop.main.Main minimize --suitepath=~/RandoopTests/src/ErrorTestLang.java --suiteclasspath=~/RandoopTests/commons-lang3-3.5.jar --testsuitetimeout=30",
        new Options(Minimize.class));
  }

  /**
   * Check that the required parameters have been specified by the command-line options and then
   * call the mainMinimize method.
   *
   * @param args first parameter is the path to the Java file to be minimized. The second parameter
   *     is the classpath needed to compile and run the Java file. The third parameter is the
   *     timeout time, in seconds, for the whole test suite.
   * @return true if the command was handled successfully
   * @throws RandoopTextuiException thrown if unrecognized arguments are passed
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
      System.out.println("You must specify a positive, nonzero timeout value.");
      System.exit(1);
    }

    // Call the main minimize method
    return mainMinimize(suitepath, suiteclasspath, testsuitetimeout);
  }

  /**
   * Minimize the input test file.
   *
   * <p>Minimization is defined as processing an input file and producing an output file that is as
   * small as possible (as few lines of code as possible) which fails the same way:
   *
   * <ol>
   *   <li>Same failing assertions as in the original input test suite.
   *   <li>Same stacktrace produced by failing assertions.
   * </ol>
   *
   * <p>The original input Java file will be compiled and run once. Using the resulting output from
   * standard output and standard error, the "expected output" is defined as the collection of
   * standard output and standard error outputs produced from compiling and running the original
   * input Java file.
   *
   * @param filePath the path to the Java file that is being minimized
   * @param classPath classpath used to compile and run the Java file
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   * @return true if minimization succeeded
   */
  public static boolean mainMinimize(String filePath, String classPath, int timeoutLimit) {
    System.out.println("Reading and parsing: " + filePath);

    // Read and parse input Java file.
    CompilationUnit compUnit;
    try (FileInputStream inputStream = new FileInputStream(filePath)) {
      compUnit = JavaParser.parse(inputStream);
    } catch (ParseException e) {
      System.err.println("Error parsing Java file " + filePath);
      System.err.println(e);
      return false;
    } catch (IOException e) {
      System.err.println("Error reading Java file " + filePath);
      System.err.println(e);
      return false;
    }

    System.out.println("Obtaining expected output.");

    // Find the package name if it exists.
    String packageName = null;
    try {
      PackageDeclaration classPackage = compUnit.getPackage();
      if (classPackage != null) {
        packageName = classPackage.getPackageName();
      }
    } catch (NoSuchElementException e) {
      // No package declaration.
    }

    // Run the test suite once to obtain the expected output.
    String newFilePath = writeToFile(compUnit, filePath, "Minimized");
    Results res = compileAndRun(newFilePath, classPath, packageName, timeoutLimit);

    if (res.compOut.exitValue != 0) {
      System.err.println("Error when compiling file " + filePath + ". Aborting");
      System.err.println(res.compOut.errout);
      return false;
    } else if (res.runOut == null || !res.runOut.errout.isEmpty()) {
      System.err.println("Error when running file " + filePath + ". Aborting");
      System.err.println(res.runOut.errout);
      return false;
    }

    // Keep track of the expected output.
    String expectedOutput = normalizeJUnitOutput(res.runOut.stdout);

    System.out.println("Minimizing: " + filePath);

    // Minimize the Java test suite and output to a new Java file.
    minimizeTestSuite(compUnit, packageName, filePath, classPath, expectedOutput, timeoutLimit);
    compUnit =
        simplifyVariableTypeNames(
            compUnit, packageName, filePath, classPath, expectedOutput, timeoutLimit);
    writeToFile(compUnit, filePath, "Minimized");

    System.out.println("Minimizing complete.\n");

    // Output original and minimized file lengths.
    System.out.println("Original file length: " + getFileLength(filePath) + " lines");
    System.out.println("Minimized file length: " + getFileLength(newFilePath) + " lines");

    return true;
  }

  /**
   * Visit and minimize every method within a compilation unit.
   *
   * @param cu the compilation unit to minimize, the compilation unit will be modified if a method
   *     is minimized
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
      String expectedOutput,
      int timeoutLimit) {
    for (TypeDeclaration type : cu.getTypes()) {
      for (BodyDeclaration member : type.getMembers()) {
        if (member instanceof MethodDeclaration) {
          MethodDeclaration method = (MethodDeclaration) member;
          // Minimize method and then simplify variable type names
          minimizeMethod(
              method, cu, packageName, filePath, classpath, expectedOutput, timeoutLimit);
          System.out.println("Minimized method " + method.getName());
        }
      }
    }
  }

  /**
   * Minimize a method by minimizing each statement in turn.
   *
   * @param method the method that we are minimizing, the method will be modified if a correct
   *     minimization of the method is found
   * @param compUnit compilation unit that contains the AST for the Java file that we are
   *     minimizing, the compilation unit will be modified if a correct minimization of a method is
   *     found
   * @param packageName the name of the package that the Java file is in
   * @param filePath path to the Java file that we are minimizing
   * @param classpath classpath needed to compile and run the Java file
   * @param expectedOutput expected standard output from running the JUnit test suite
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   */
  private static void minimizeMethod(
      MethodDeclaration method,
      CompilationUnit compUnit,
      String packageName,
      String filePath,
      String classpath,
      String expectedOutput,
      int timeoutLimit) {
    List<Statement> statements = method.getBody().getStmts();

    // Map from variable name to the variable's value which is found in a passing assertion.
    Map<String, String> primitiveValues = new HashMap<String, String>();

    // Iterate through the list of statements, from last to first
    for (int i = statements.size() - 1; i >= 0; i--) {
      Statement currStmt = statements.get(i);

      // Try removing the current statement. If the test's behavior changes, we will re-insert it.
      statements.remove(i);

      // Obtain a list of possible replacements for the current statement.
      List<Statement> replacements = getStatementReplacements(currStmt, primitiveValues);
      boolean replacementFound = false;
      for (Statement stmt : replacements) {
        // Add replacement statement to the method's body. If stmt is null, we won't add
        // anything since null represents removal of the statement.
        if (stmt != null) {
          statements.add(i, stmt);
        }

        // Write, compile, and run the new Java file with the new suffix "Minimized".
        String newFilePath = writeToFile(compUnit, filePath, "Minimized");
        if (checkCorrectlyMinimized(
            newFilePath, classpath, packageName, expectedOutput, timeoutLimit)) {
          // No compilation or runtime issues, obtained output is the same as the expected output.
          // Use simplified statement and continue.
          replacementFound = true;
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
   * Return a list of statements that are a simplification of a given statement. In order from most
   * to least minimized. The possible minimizations are:
   *
   * <ul>
   *   <li>Remove a statement, represented by null
   *   <li>Replace the right hand side expression with {@code 0}, {@code false}, or {@code null}
   *   <li>Replace right hand side to a calculated value obtained from a passing assertion
   *   <li>Remove the left hand side of a statement, retaining only the expression on the right
   * </ul>
   *
   * @param currStmt statement to simplify
   * @param primitiveValues map of primitive variable names to expressions representing their values
   * @return non-null list of statements, where each is a possible simplification of {@code
   *     currStmt}
   */
  private static List<Statement> getStatementReplacements(
      Statement currStmt, Map<String, String> primitiveValues) {
    List<Statement> replacements = new ArrayList<Statement>();

    // Null represents a removal of the statement.
    replacements.add(null);

    if (currStmt instanceof ExpressionStmt) {
      Expression exp = ((ExpressionStmt) currStmt).getExpression();
      if (exp instanceof VariableDeclarationExpr) {
        // Create and return a list of possible replacement statements.
        VariableDeclarationExpr vdExpr = (VariableDeclarationExpr) exp;

        // Simplify right hand side to zero equivalent value, 0, false, or null.
        replacements.add(rhsAssignZeroValue(vdExpr));

        // Simplify right hand side to a value that was previously found in a passing assertion.
        Statement rs = rhsAssignValueFromPassingAssertion(vdExpr, primitiveValues);
        if (rs != null) {
          replacements.add(rs);
        }

        // Simplify statement by removing the left hand side.
        replacements.add(removeLeftHandSideSimplifiation(vdExpr));
      }
    }
    return replacements;
  }

  /**
   * If {@code currStmt} is a statement that is a passing assertion, store the value associated with
   * the variable in the {@code primitiveValues} map.
   *
   * @param currStmt a statement
   * @param primitiveValues a map of variable names to variable values, modified if {@code currStmt}
   *     is a passing assertion, asserting a variable's value.
   */
  private static void storeValueFromAssertion(
      Statement currStmt, Map<String, String> primitiveValues) {
    // Check if the statement is an assertion regarding a value that can be used
    // in a simplification later on.
    if (currStmt instanceof ExpressionStmt) {
      Expression exp = ((ExpressionStmt) currStmt).getExpression();
      if (exp instanceof MethodCallExpr) {
        MethodCallExpr mCall = (MethodCallExpr) exp;
        // Check if the method call is an assertTrue statement
        if (mCall.getName().equals("assertTrue")) {
          // Is an assertTrue statement.
          List<Expression> mArgs = mCall.getArgs();
          if (mArgs.size() == 1) {
            // Retrieve and store the value associated with the variable in the assertion.
            Expression mExp = mArgs.get(0);
            List<Node> children = mExp.getChildrenNodes();
            String var = children.get(0).toString();
            String val = children.get(1).toString();
            primitiveValues.put(var, val);
          }
        }
      }
    }
  }

  /**
   * Return a variable declaration statement that simplifies the right hand side to 0, false, or
   * null, whichever is type correct. The variable declaration statement is assumed to declare only
   * one variable, for instance, {@code int i;}. Multiple variable declarations in a single
   * statement in the form {@code int i, j, k;} are not valid.
   *
   * @param vdExpr variable declaration expression representing the current statement to simplify
   * @return a {@code Statement} object representing the simplified variable declaration expression.
   *     Returns {@code null} if more than one variable is declared in the {@code
   *     VariableDeclarationExpr}.
   */
  private static Statement rhsAssignZeroValue(VariableDeclarationExpr vdExpr) {
    // Copy the variable declaration expression.
    VariableDeclarationExpr exprCopy = (VariableDeclarationExpr) vdExpr.clone();

    // Obtain a reference to the variable declaration.
    List<VariableDeclarator> vars = exprCopy.getVars();
    if (vars.size() > 1) {
      // More than one variable declared in this expression.
      return null;
    }
    VariableDeclarator vd = vars.get(0);

    // Based on the declared variable type, assign the variable 0, false, or null.
    Type type = exprCopy.getType();
    if (type instanceof PrimitiveType) {
      switch (((PrimitiveType) type).getType()) {
        case Boolean:
          vd.setInit(new BooleanLiteralExpr(false));
          break;
        case Byte:
          vd.setInit(new IntegerLiteralExpr("0"));
          break;
        case Char:
          vd.setInit(new CharLiteralExpr(""));
          break;
        case Short:
          vd.setInit(new IntegerLiteralExpr("0"));
          break;
        case Int:
          vd.setInit(new IntegerLiteralExpr("0"));
          break;
        case Long:
          vd.setInit(new LongLiteralExpr("0"));
          break;
        case Float:
          vd.setInit(new DoubleLiteralExpr("0"));
          break;
        case Double:
          vd.setInit(new DoubleLiteralExpr("0"));
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
   * Return a variable declaration statement that simplifies the right hand side to a calculated
   * value for primitive types. The variable declaration statement is assumed to declare only one
   * variable, for instance, {@code int i;}. Multiple variable declarations in a single statement in
   * the form {@code int i, j, k;} are not valid.
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
    // Copy the variable declaration expression.
    VariableDeclarationExpr exprCopy = (VariableDeclarationExpr) vdExpr.clone();

    // Obtain a reference to the variable declaration.
    List<VariableDeclarator> vars = exprCopy.getVars();
    if (vars.size() > 1) {
      // More than one variable declared in this expression.
      return null;
    }
    VariableDeclarator vd = vars.get(0);

    // Based on the declared variable type, set the right hand a previously calculated value
    Type type = exprCopy.getType();
    String varName = vd.getId().getName();
    if (type instanceof PrimitiveType && primitiveValues.containsKey(varName)) {
      // Set right hand side to a calculated value.
      String val = primitiveValues.get(varName);
      switch (((PrimitiveType) type).getType()) {
        case Boolean:
          vd.setInit(new BooleanLiteralExpr(Boolean.parseBoolean(val)));
          break;
        case Byte:
          vd.setInit(new IntegerLiteralExpr(val));
          break;
        case Char:
          vd.setInit(new CharLiteralExpr(val));
          break;
        case Short:
          vd.setInit(new IntegerLiteralExpr(val));
          break;
        case Int:
          vd.setInit(new IntegerLiteralExpr(val));
          break;
        case Long:
          vd.setInit(new LongLiteralExpr(val));
          break;
        case Float:
          vd.setInit(new DoubleLiteralExpr(val));
          break;
        case Double:
          vd.setInit(new DoubleLiteralExpr(val));
          break;
      }

      // Create a new statement with the simplified expression.
      ExpressionStmt newStmt = new ExpressionStmt(exprCopy);
      exprCopy.setParentNode(newStmt);
      return newStmt;
    }
    // Variable is not a primitive type or no value has yet been found in a passing assertion.
    return null;
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
  private static Statement removeLeftHandSideSimplifiation(VariableDeclarationExpr vdExpr) {
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
   * @return {@Code CompilationUnit} with fully-qualified type names simplified to simple type names
   */
  private static CompilationUnit simplifyVariableTypeNames(
      CompilationUnit compUnit,
      String packageName,
      String filePath,
      String classpath,
      String expectedOutput,
      int timeoutLimit) {
    // Map from fully-qualified type name to simple type name.
    Map<String, String> typeNameMap = new HashMap<String, String>();
    // Set of fully qualified type names that are used in variable declarations
    Set<Type> fullyQualifiedNames = new HashSet<Type>();
    // Collect all of the type names in the compilation unit.
    new TypeVisitor().visit(compUnit, fullyQualifiedNames);

    // Iterate through the set of fully qualified names and fill the map
    // with mappings from fully qualified names to simple type names. Also
    // add in necessary import statements.
    for (Type type : fullyQualifiedNames) {
      String typeName = type.toString();
      addImport(compUnit, typeName);
      typeNameMap.put(typeName, getSimpleTypeName(typeName));
    }

    CompilationUnit result = compUnit;
    List<ImportDeclaration> imports = result.getImports();
    for (String type : typeNameMap.keySet()) {
      String compUnitStr = result.toString();

      // Replace all instances of the fully-qualified type name with the simple type name.
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
      String expectedOutput,
      int timeoutLimit) {
    Results res = compileAndRun(filePath, classpath, packageName, timeoutLimit);

    return res.compOut.exitValue == 0
        && res.runOut != null
        && res.runOut.errout.isEmpty()
        && expectedOutput.equals(normalizeJUnitOutput(res.runOut.stdout));
  }

  /**
   * Compile and run a given Java file and return the compilation and run output.
   *
   * @param filePath the absolute path to the Java file to be compiled and executed
   * @param classpath dependencies and complete classpath to compile and run the Java program
   * @param packageName the name of the package that the Java file is in
   * @param timeoutLimit number of seconds allowed for the whole test suite to be run
   * @return a Results object containing the compilation output and run output
   */
  private static Results compileAndRun(
      String filePath, String classpath, String packageName, int timeoutLimit) {
    String pathSeparator = System.getProperty("path.separator");
    String systemClassPath = System.getProperty("java.class.path");

    // Obtain directory to carry out compilation and execution step
    String executionDir = getExecutionDirectory(filePath, packageName);

    // Obtain directory path from file path.
    Path fPath = Paths.get(filePath).getParent();
    // Directory path for the classpath
    String dirPath = null;
    if (fPath != null) {
      dirPath = fPath.toString();
    }

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
    Outputs cRes = runProcess(command, executionDir, timeoutLimit);

    // Check compilation results for compilation error.
    if (cRes.exitValue != 0) {
      return new Results(cRes, null);
    }

    // Add the package name to the class name if it exists.
    String className = getClassName(filePath);
    if (packageName != null) {
      className = packageName + "." + className;
    }

    // Command to run the Java file.
    command = "java -classpath " + systemClassPath;
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
    Outputs rRes = runProcess(command, executionDir, timeoutLimit);

    return new Results(cRes, rRes);
  }

  /**
   * Get directory to execute command in given file path and package name
   *
   * @param filePath the absolute file path to the input Java file
   * @param packageName package name of input Java file
   * @return String of the directory to execute the commands in
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
      // Execution directory is null, execute command in default directory.
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
              // Error reading from process' input stream.
              return "Error reading from process' input stream.";
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
              // Error reading from process' error stream.
              return "Error reading from process' error stream.";
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
   * numbers. The relevant information that is retained include the indices of each failing test and
   * the respective method names.
   *
   * @param input the {@code String} produced from running a JUnit test suite
   * @return a {@code String} which contains several {@code String} pairs of output from the JUnit
   *     test suite. Each pair's first element is the index of the failing test along with the name
   *     of the method containing the failing assertion. Each pair's second element is the output
   *     from the failing assertion. For example: {@code 1) test10(ErrorTestLangMinimized)
   *     java.lang.AssertionError: Contract failed: compareTo-equals on fraction1 and fraction4}
   */
  private static String normalizeJUnitOutput(String input) {
    Scanner scn = new Scanner(input);

    // JUnit output starts with index 1 for first failure
    int index = 1;
    // String to represent the result that we return
    String res = "";
    // Continue until we finish processing the input String
    while (scn.hasNextLine()) {
      String line = scn.nextLine();
      // Look for a String of the form "i)" where i is the current failing
      // test index.
      if (line.startsWith(index + ") ")) {
        res += line + "\n" + scn.nextLine() + "\n";
        index++;
      }
    }
    scn.close();
    return res;
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
    // Rename the overall class to [old class name][suffix]
    new ClassRenamer().visit(compUnit, new String[] {getClassName(filePath), suffix});

    // Create a new string to represent the new file name
    String newFileName =
        new StringBuilder(filePath).insert(filePath.lastIndexOf('.'), suffix).toString();

    // Write the compilation unit to the new file
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
   * Given the absolute file path to a Java file, return a String representing the class name of the
   * file.
   *
   * @param filePath absolute path to a Java file
   * @return {@code String} representing the class name
   */
  public static String getClassName(String filePath) {
    if (filePath == null) {
      return null;
    }

    // Get the name of the file without full path
    String fileName = Paths.get(filePath).getFileName().toString();
    // Remove .java extension from file name
    return fileName.substring(0, fileName.indexOf('.'));
  }

  /**
   * Rename the overall class. Visit every class and interface declaration, of which we expect there
   * to be one in the input test suite. The overall class will be renamed to class name + suffix.
   */
  private static class ClassRenamer extends VoidVisitorAdapter<Object> {
    /**
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

  private static class TypeVisitor extends VoidVisitorAdapter<Object> {
    /** @param arg a set of {@code Type} objects */
    @SuppressWarnings("unchecked")
    @Override
    public void visit(ClassOrInterfaceType n, Object arg) {
      Set<Type> params = (Set<Type>) arg;
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
   * @param name the import name
   */
  private static void addImport(CompilationUnit compilationUnit, String name) {
    String i = "import " + name + ";";
    ImportDeclaration importDeclaration;

    try {
      importDeclaration = JavaParser.parseImport(i);
    } catch (ParseException e) {
      // Unexpected error from parsing import.
      System.err.println("Error parsing import");
      return;
    }

    List<ImportDeclaration> importDeclarations = compilationUnit.getImports();
    for (ImportDeclaration im : importDeclarations) {
      // Check if the compilation unit already includes the import.
      if (importDeclaration.toString().equals(im.toString())) {
        return;
      }
    }

    // Add the import to the compilation unit's list of imports.
    importDeclarations.add(importDeclaration);
    compilationUnit.setImports(importDeclarations);
  }

  /**
   * Contains two {@code String} objects and an exit status which represent the standard output and
   * error output and the resulting status from running a process.
   */
  private static class Outputs {
    /** String representing the standard output */
    private String stdout;
    /** String representing the error output */
    private String errout;

    /** Exit value from running a process */
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

  /** Contains two {@code Output} objects for compilation and execution. */
  private static class Results {
    /** The standard and error output of compiling a Java file. */
    private Outputs compOut;
    /** The standard and error output of running a Java file. */
    private Outputs runOut;

    /**
     * Create a Results object.
     *
     * @param compOut compilation results
     * @param runOut runtime results
     */
    private Results(Outputs compOut, Outputs runOut) {
      this.compOut = compOut;
      this.runOut = runOut;
    }
  }

  /**
   * Calculate the length of a file, by number of lines
   *
   * @param filepath absolute file path to the input file
   * @return the number of lines in the file. Negative one is returned if an exception occurs from
   *     finding or reading the file
   */
  private static int getFileLength(String filepath) {
    int lines = 0;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filepath));
      while (reader.readLine() != null) {
        lines++;
      }
      reader.close();
    } catch (FileNotFoundException e) {
      System.err.println("File length not calculated, file not found exception.");
      System.exit(1);
    } catch (IOException e) {
      System.err.println("File length not calculated, file read exception.");
      System.exit(1);
    }
    return lines;
  }

  /**
   * Print out usage error and stack trace and then exit
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
