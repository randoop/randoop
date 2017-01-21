package randoop.main;

import plume.Options;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.*;
import plume.TimeLimitProcess;

/**
 * Minimizer minimizes failing unit tests. The program takes two command-line
 * arguments:
 * <ol>
 * <li>the complete path to the Java file that will be minimized, and
 * <li>the classpath containing dependencies needed to compile and run the Java
 * file.
 * </ol>
 */
public class Minimize extends CommandHandler {

  public static PrintStream out = System.out;

  public Minimize() {
    super(
        "minimize",
        "Minimize a failing JUnit test suite.",
        "minimize",
        "",
        "Minimize a failing JUnit test suite.",
        null,
        "Complete path to Java file to be minimized, complete classpath to compile and run the Java file, maximum time (in seconds) allowed for a single unit test case to run before it times out.",
        "A minimized JUnit test suite (as one Java file) named \"InputFileMinimized.java\" if \"InputFile.java\" were the name of the input file.",
        "java randoop.main.Main minimize \"C:\\RandoopTests\\src\\ErrorTestLang.java\" \"C:\\RandoopTests\\commons-lang3-3.5.jar;C:\\RandoopTests\\junit-4.12.jar;C:\\RandoopTests\\hamcrest-core-1.3.jar\" \"30\"",
        new Options(Minimize.class));
  }

  /**
   * Main entry point, minimize a Java unit test.
   *
   * @param args first parameter is the complete path to the Java file to be
   *             minimized and the second parameter is the complete classpath
   *             needed to compile and run the Java file
   * @throws IOException
   */
  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {
    if (args.length != 3) {
      System.out.println("Usage: [path to Java file] [classpath] [timeout limit]");
      return false;
    }

    // First argument is the complete path to the Java file.
    String filePath = args[0];

    // Second argument is the classpath that includes dependencies needed to
    // compile and run the Java file.
    String classpath = args[1];

    // Maximum number of seconds allowed for a unit test within the test
    // suite to run.
    int timeoutLimit = Integer.parseInt(args[2]);

    System.out.println("Reading and parsing: " + filePath);

    // Read and parse input Java file.
    CompilationUnit compUnit;
    try {
      FileInputStream inputStream = new FileInputStream(filePath);
      compUnit = JavaParser.parse(inputStream);
      inputStream.close();
    } catch (IOException e) {
      System.err.println("Error parsing Java file " + filePath);
      System.err.println(e);
      // This return statement is dead code, but Java's definite
      // assignment analysis
      // needs it to recognize that compUnit really gets assigned. :-(
      return false;
    }

    System.out.println("Obtaining expected output.");

    // Find the package name if it exists
    String packageName = "";
    try {
      packageName = compUnit.getPackage().getPackageName();
    } catch (NoSuchElementException e) {
      // No package declaration
    }

    // Run the test suite once to obtain expected output.
    String newFilePath = writeToFile(compUnit, filePath, "Minimized");
    Results res = compileAndRun(newFilePath, classpath, packageName, timeoutLimit);

    if (res == null) {
      // There was an error when compiling or running.
      System.err.println("Error when compiling or running file " + filePath + ". Aborting");
      return false;
    } else if (!res.compOut.errout.isEmpty()) {
      System.err.println("Error when compiling file " + filePath + ". Aborting");
      System.err.println(res.compOut.errout);
      return false;
    } else if (!res.runOut.errout.isEmpty()) {
      System.err.println("Error when running file " + filePath + ". Aborting");
      System.err.println(res.runOut.errout);
      return false;
    }

    // Keep track of the expected output.
    String expectedOutput = normalizeJUnitOutput(res.runOut.stdout);

    System.out.println("Minimizing: " + filePath);
    long start = System.currentTimeMillis();

    // Minimize the Java test suite and output to a new Java file.
    minimizeTestSuite(compUnit, filePath, classpath, expectedOutput, packageName, timeoutLimit);
    writeToFile(compUnit, filePath, "Minimized");

    long end = System.currentTimeMillis();
    System.out.println("Minimizing complete.");
    System.out.println("Total time: " + (end - start) / 1000 + " seconds.\n");

    return true;
  }

  /**
   * Visit and minimize every method within a Java file.
   *
   * @param cu             the compilation unit through which we visit each method
   * @param filePath       the complete file path to the Java program that is being
   *                       processed
   * @param classpath      classpath used to compile and run the Java file
   * @param expectedOutput expected JUnit output when the Java file is compiled and run
   * @param packageName    the name of the package that the Java file is in
   * @param timeoutLimit   maximum number of seconds allowed for any one unit test case
   *                       to run
   * @modifies cu, the compilation unit will be modified if a correct
   * minimization of a method is found
   */
  private static void minimizeTestSuite(
      CompilationUnit cu,
      String filePath,
      String classpath,
      String expectedOutput,
      String packageName,
      int timeoutLimit) {
    int index = 1;
    for (TypeDeclaration<?> type : cu.getTypes()) {
      for (BodyDeclaration<?> member : type.getMembers()) {
        if (member instanceof MethodDeclaration) {
          MethodDeclaration method = (MethodDeclaration) member;
          // Minimize method and then simplify variable type names
          minimizeMethod(
              method, cu, filePath, classpath, expectedOutput, packageName, timeoutLimit);
          simplifyVariableTypeNames(
              method, cu, filePath, classpath, expectedOutput, packageName, timeoutLimit);
          System.out.println("Minimized method " + (index++) + ".");
        }
      }
    }
  }

  /**
   * Minimize a method.
   *
   * @param method         current method that we are minimizing within the compilation
   *                       unit
   * @param compUnit       compilation unit that contains the AST for the Java file that
   *                       we are minimizing
   * @param filePath       complete path to the Java file that we are minimizing
   * @param classpath      classpath needed to compile and run the Java file
   * @param expectedOutput expected standard output from running the JUnit test suite
   * @param packageName    the name of the package that the Java file is in
   * @param timeoutLimit   the maximum number of seconds allowed for any one unit test
   *                       case to run
   * @modifies method, the given method will be modified if a correct
   * minimization of the method is found
   * @modifies compUnit, the compilation unit will be modified if a correct
   * minimization of the method is found
   */
  private static void minimizeMethod(
      MethodDeclaration method,
      CompilationUnit compUnit,
      String filePath,
      String classpath,
      String expectedOutput,
      String packageName,
      int timeoutLimit) {
    // Obtain a list of all the statements within the method.
    List<Statement> statements = method.getBody().getStmts();

    // Map from variable name to an expression that the variable is equal
    // to. These expressions are found in assertions.
    Map<String, String> primitiveValues = new HashMap<String, String>();

    // Iterate through the list of statements, from last to first
    for (int i = statements.size() - 1; i >= 0; i--) {
      Statement currStmt = statements.get(i);

      // Try removing the current statement.
      // If this changes the test's behavior, we will re-insert it.
      statements.remove(i);

      // Obtain a list of possible replacements for the current
      // statement.
      List<Statement> replacements = getReplacements(currStmt, primitiveValues);
      boolean replacementFound = false;
      for (Statement stmt : replacements) {
        // Add replacement statement to the method's body.
        if (stmt != null) {
          statements.add(i, stmt);
        }

        // Write, compile, and run the new Java file.
        String newFilePath = writeToFile(compUnit, filePath, "Minimized");
        if (checkCorrectlyMinimized(
            newFilePath, classpath, expectedOutput, packageName, timeoutLimit)) {
          // No compilation or runtime issues and the obtained
          // output is the same as the expected; safe to
          // use simplified statement and continue.
          replacementFound = true;

          if (currStmt instanceof ExpressionStmt) {
            Expression exp = ((ExpressionStmt) currStmt).getExpression();
            if (exp instanceof MethodCallExpr) {
              MethodCallExpr mCall = (MethodCallExpr) exp;
              if (mCall.getName().equals("assertTrue")) {
                // Is an assertTrue statement.
                List<Expression> mArgs = mCall.getArgs();
                if (mArgs.size() == 1) {
                  // Retrieve and store the expression
                  // associated with the variable in the
                  // assertion.
                  Expression mExp = mArgs.get(0);
                  List<Node> children = mExp.getChildrenNodes();
                  String var = children.get(0).toString();
                  String val = children.get(1).toString();
                  primitiveValues.put(var, val);
                }
              }
            }
          }
          break;
        }
        // Issue encountered, remove the faulty statement.
        if (stmt != null) {
          statements.remove(i);
        }
      }
      if (!replacementFound) {
        // No other correct simplifications found, add back the
        // original statement.
        statements.add(i, currStmt);
      }
    }
  }

  /**
   * Return a list of statements that are a simplification of a given
   * statement.
   *
   * @param currStmt        statement to simplify
   * @param primitiveValues map of primitive variable names to expressions representing
   *                        their values
   * @return non-null list of statements, where each is a possible
   * simplification of currStmt
   */
  private static List<Statement> getReplacements(
      Statement currStmt, Map<String, String> primitiveValues) {
    List<Statement> replacements = new ArrayList<Statement>();

    // Null represents the empty statement.
    replacements.add(null);

    if (currStmt instanceof ExpressionStmt) {
      Expression exp = ((ExpressionStmt) currStmt).getExpression();
      if (exp instanceof VariableDeclarationExpr) {
        // Create and return a list of possible replacement statements.
        VariableDeclarationExpr vdExpr = (VariableDeclarationExpr) exp;

        replacements.add(rightHandSideSimplificationStatement(vdExpr, primitiveValues));
        replacements.add(removeLeftHandSideSimplifiation(vdExpr));
      }
    }
    return replacements;
  }

  /**
   * Return a variable declaration statement that simplifies the right hand
   * side to a calculated constant for primitive types and 0/false/null for
   * all other types.
   *
   * @param vdExpr          variable declaration expression representing the current
   *                        statement to simplify
   * @param primitiveValues a map of primitive variable names to expressions representing
   *                        their values
   * @return a Statement object representing the simplified variable
   * declaration expression
   */
  private static Statement rightHandSideSimplificationStatement(
      VariableDeclarationExpr vdExpr, Map<String, String> primitiveValues) {
    // Clone vdExpr so that the original statement is not modified. If all
    // partial removals/substitutions fail, the original statement will be
    // reinserted into the method body. exprCopy is used to create a new
    // Statement object where the right hand side has been simplified.
    VariableDeclarationExpr exprCopy = (VariableDeclarationExpr) vdExpr.clone();

    // Obtain a reference to the variable declaration.
    List<VariableDeclarator> vars = exprCopy.getVariables();
    VariableDeclarator vd = vars.get(0);

    // Based on the declared variable type, set the right hand
    // side to either a previously calculated value or zero or null.
    Type<?> type = exprCopy.getElementType();
    String varName = vd.getId().getName();
    if (type instanceof PrimitiveType) {
      if (primitiveValues.containsKey(varName)) {
        // Set right hand side to a calculated value.
        String val = primitiveValues.get(varName);
        switch (type.toString()) {
          case "boolean":
            vd.setInit(new BooleanLiteralExpr(Boolean.parseBoolean(val)));
            break;
          case "byte":
            vd.setInit(new IntegerLiteralExpr(val));
            break;
          case "char":
            vd.setInit(new CharLiteralExpr(val));
            break;
          case "short":
            vd.setInit(new IntegerLiteralExpr(val));
            break;
          case "int":
            vd.setInit(new IntegerLiteralExpr(val));
            break;
          case "long":
            vd.setInit(new LongLiteralExpr(val));
            break;
          case "float":
            vd.setInit(new DoubleLiteralExpr(val));
            break;
          case "double":
            vd.setInit(new DoubleLiteralExpr(val));
            break;
        }
      } else {
        // Set right hand side to a zero value.
        vd.setInit(new IntegerLiteralExpr("0"));
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
   * Return a statement that contains only the right hand side of a given
   * statement.
   *
   * @param vdExpr variable declaration expression that represents the statement
   *               to simplify
   * @return a Statement object that is equal to vdExpr without the assignment
   * to the declared variable
   */
  private static Statement removeLeftHandSideSimplifiation(VariableDeclarationExpr vdExpr) {
    // Clone vdExpr so that the original statement is not modified. If all
    // partial removals/substitutions fail, the original statement will be
    // reinserted into the method body.
    VariableDeclarationExpr exprCopy = (VariableDeclarationExpr) vdExpr.clone();
    List<VariableDeclarator> vars = exprCopy.getVariables();
    VariableDeclarator vd = vars.get(0);

    // Create a new statement with only the right hand side.
    ExpressionStmt newStmt = new ExpressionStmt(vd.getInit());
    exprCopy.setParentNode(newStmt);
    return newStmt;
  }

  /**
   * Simplify the variable type names in a method. For example,
   * java.lang.String should be simplified to String.
   *
   * @param method         a method within the Java file
   * @param compUnit       compilation unit containing an AST for a Java file
   * @param classpath      classpath needed to compile and run the Java file
   * @param expectedOutput expected standard output from running the JUnit test suite
   * @param packageName    the name of the package that the Java file is in
   * @param timeoutLimit   the maximum number of seconds allowed for any one unit test
   *                       case to run
   * @modifies method, the given method will be modified if a correct
   * minimization of the method is found
   * @modifies compUnit, the compilation unit will be modified if a correct
   * minimization of the method is found
   */
  private static void simplifyVariableTypeNames(
      MethodDeclaration method,
      CompilationUnit compUnit,
      String filePath,
      String classpath,
      String expectedOutput,
      String packageName,
      int timeoutLimit) {
    List<Statement> statements = method.getBody().getStmts();

    // Map from fully-qualified type name to simple type name
    Map<String, String> typeNameMap = new HashMap<String, String>();
    // Set of fully qualified type names that are used in variable
    // declarations
    Set<String> fullyQualifiedNames = new HashSet<String>();

    for (int i = 0; i < statements.size(); i++) {
      Statement currStmt = statements.get(i);
      if (currStmt instanceof ExpressionStmt) {
        Expression exp = ((ExpressionStmt) currStmt).getExpression();
        if (exp instanceof VariableDeclarationExpr) {
          VariableDeclarationExpr vdExpr = (VariableDeclarationExpr) exp;
          String fullyQualifiedTypeName = vdExpr.getElementType().toString();
          fullyQualifiedNames.addAll(getFullyQualifiedTypeNames(fullyQualifiedTypeName));
        }
      }
    }

    // Iterate through the set of fully qualified names and fill the map
    // with mappings from fully qualified names to simple type names. Also
    // add in necessary import statements.
    for (String typeName : fullyQualifiedNames) {
      if (typeName.contains(".")) {
        compUnit.addImport(typeName);
        typeNameMap.put(typeName, getSimpleTypeName(typeName));
      }
    }

    // Iterate through all the statements and replace any String occurrences
    // of any fully qualified type name within fullyQualifiedNames with the
    // simple type name that it maps to.
    for (int i = 0; i < statements.size(); i++) {
      Statement currStmt = statements.get(i);
      String newStmt = currStmt.toString();

      for (String type : typeNameMap.keySet()) {
        if (newStmt.contains(type)) {
          newStmt = newStmt.replace(type, typeNameMap.get(type));
        }
      }
      // Remove the existing statement at i and insert the new statement.
      statements.remove(i);
      statements.add(i, JavaParser.parseStatement(newStmt));

      String newFilePath = writeToFile(compUnit, filePath, "Minimized");
      if (!checkCorrectlyMinimized(
          newFilePath, classpath, expectedOutput, packageName, timeoutLimit)) {
        // If an issue occurs, remove the new statement and add back the
        // original.
        statements.remove(i);
        statements.add(i, currStmt);
      }
    }
  }

  /**
   * Get the set of all the names of the different types that appear in a
   * fully qualified type name. For example the fully qualified type name:
   * Map<String, Pair<Integer, Double>> should return the set: {Map, String,
   * Pair, Integer, Double}.
   *
   * @param typeName fully qualified type name
   * @return set of the different type names
   */
  public static Set<String> getFullyQualifiedTypeNames(String typeName) {
    if (typeName == null) {
      return null;
    }

    // Remove all whitespace.
    typeName = typeName.replaceAll("\\s+", "");

    Set<String> typeNameSet = new HashSet<String>();
    StringBuilder sb = new StringBuilder();

    // Iterate through the entire String.
    for (int i = 0; i < typeName.length(); i++) {
      char c = typeName.charAt(i);
      // If the current character is a comma or angle bracket, add the
      // String generated from the String Builder to the type name set and
      // reset the String Builder.
      if (c == ',' || c == '<' || c == '>') {
        typeNameSet.add(sb.toString());
        sb.setLength(0);
      } else {
        sb.append(c);
      }
    }
    // Add the final String from the String Builder.
    typeNameSet.add(sb.toString());
    // Remove the empty String that gets generated.
    typeNameSet.remove("");

    return typeNameSet;
  }

  /**
   * Get the simple type name of a fully qualified type name. For example,
   * java.lang.String should be simplified to String.
   *
   * @param typeName fully qualified type name
   * @return simple type name of typeName
   */
  public static String getSimpleTypeName(String typeName) {
    int indexOflastSeparator = typeName.lastIndexOf('.');
    return indexOflastSeparator > 0
        ? typeName.substring(indexOflastSeparator + 1, typeName.length())
        : typeName;
  }

  /**
   * Check if a Java file has been correctly minimized.
   *
   * @param filePath       the complete path to the Java file
   * @param classpath      classpath needed to compile/run Java file
   * @param expectedOutput expected output of running JUnit test suite
   * @param packageName    the name of the package that the Java file is in
   * @param timeoutLimit   the maximum number of seconds allowed for any one unit test
   *                       case to run
   * @return true if there are no compilation and no runtime errors and the
   * output is equal to the expected output
   */
  private static boolean checkCorrectlyMinimized(
      String filePath,
      String classpath,
      String expectedOutput,
      String packageName,
      int timeoutLimit) {
    Results res = compileAndRun(filePath, classpath, packageName, timeoutLimit);

    return res != null
        && res.compOut.errout.isEmpty()
        && res.runOut.errout.isEmpty()
        && expectedOutput.equals(normalizeJUnitOutput(res.runOut.stdout));
  }

  /**
   * Compile and run a given Java file and return the compilation and run
   * output.
   *
   * @param filePath     the complete path to the Java file to be compiled and executed
   * @param classpath    dependencies and complete classpath to compile and run the
   *                     Java program
   * @param packageName  the name of the package that the Java file is in
   * @param timeoutLimit the maximum number of seconds allowed for any one unit test
   *                     case to run
   * @return a Results object containing the compilation output and run output
   */
  private static Results compileAndRun(
      String filePath, String classpath, String packageName, int timeoutLimit) {
    try {
      // Obtain directory path from file path.
      String dirPath = Paths.get(filePath).getParent().toString();

      // Obtain directory to carry out compilation and execution step
      String executionDir = getExecutionDirectory(filePath, packageName);

      // Compile specified Java file.
      Outputs cRes =
          runProcess("javac -classpath " + classpath + " " + filePath, executionDir, timeoutLimit);

      // Check compilation results for compilation error.
      if (cRes == null || !cRes.errout.isEmpty()) {
        return null;
      }

      String pathSeparator = System.getProperty("path.separator");

      // Add the package name if it exists
      String className = getClassName(filePath);
      if (!packageName.isEmpty()) {
        className = packageName + "." + className;
      }

      // Run the specified Java file.
      Outputs rRes =
          runProcess(
              "java -classpath "
                  + dirPath
                  + pathSeparator
                  + classpath
                  + " org.junit.runner.JUnitCore "
                  + className,
              executionDir,
              timeoutLimit);

      // Check execution results for runtime error.
      if (rRes == null) {
        return null;
      }

      return new Results(cRes, rRes);
    } catch (Exception e) {
      // Error running process.
      System.err.println("Error running process:");
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Get directory to execute command in given file path and package name
   *
   * @param filePath    the complete file path to the input Java file
   * @param packageName package name of input Java file
   * @return String of the directory to execute the commands in
   */
  private static String getExecutionDirectory(String filePath, String packageName) {
    if (packageName.isEmpty()) {
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
   * Run a command given as a String and return the output and error results
   * in an Outputs object.
   *
   * @param command      the input command to be run
   * @param executionDir the directory where the process commands should be executed
   * @param timeoutLimit the maximum number of seconds allowed for the process to run
   * @return an Outputs object containing the standard and error output
   * @throws Exception if the input command is unable to be run
   */
  private static Outputs runProcess(String command, String executionDir, int timeoutLimit)
      throws Exception {
    Process process;

    if (executionDir == null) {
      // Execution directory is null, execute command in default directory
      process = Runtime.getRuntime().exec(command);
    } else {
      // input Java file is in a package, execute in the root directory
      process = Runtime.getRuntime().exec(command, null, new File(executionDir));
    }
    final TimeLimitProcess tp = new TimeLimitProcess(process, timeoutLimit * 1000);

    Callable<String> stdCallable =
        new Callable<String>() {
          @Override
          public String call() throws Exception {
            try {
              return IOUtils.toString(tp.getInputStream());
            } catch (IOException e) {
              // Error
              return null;
            }
          }
        };

    Callable<String> errCallable =
        new Callable<String>() {
          @Override
          public String call() throws Exception {
            try {
              return IOUtils.toString(tp.getErrorStream());
            } catch (IOException e) {
              // Error
              return null;
            }
          }
        };

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
    Future<String> stdOutput = fixedThreadPool.submit(stdCallable);
    Future<String> errOutput = fixedThreadPool.submit(errCallable);
    fixedThreadPool.shutdown();

    tp.waitFor();
    if (tp.timed_out()) {
      return null;
    }

    // Wait for process to complete.
    /*
     * if (!process.waitFor(timeoutLimit, TimeUnit.SECONDS)) { // Process
     * timed out. System.out.println("Destroy"); process.destroy(); return
     * null; }
     */

    // Run and collect the results from the standard output and error output
    return new Outputs(stdOutput.get(), errOutput.get());
  }

  /**
   * Normalize the standard output obtained from running a JUnit test suite.
   *
   * @param input the String produced from running a JUnit test suite
   * @return a String which contains several String pairs of output from the
   * JUnit test suite. Each pair's first element is the index of the
   * failing test along with the name of the method containing the
   * failing assertion. Each pair's second element is the output from
   * the failing assertion. For example: 1)
   * test10(ErrorTestLangMinimized) java.lang.AssertionError: Contract
   * failed: compareTo-equals on fraction1 and fraction4
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
   * Write a compilation unit to a Java file, renaming according to the
   * suffix.
   *
   * @param compUnit the compilation unit to write to file
   * @param filePath the origin of the Java file that was processed
   * @param suffix   the suffix to append to the name of the new Java file
   * @return string representing the complete path to the newly written Java
   * file. Returns null if error occurred in writing to the new file
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
   * Given a complete file path to a Java file, return a String representing
   * the class name of the file.
   *
   * @param filePath complete path to a Java program
   * @return string representing the class name
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
   * Renames the overall class. Visits every class/interface declaration, of
   * which we expect there to be one in the input test suite. The overall
   * class will be renamed to class name + suffix.
   */
  private static class ClassRenamer extends VoidVisitorAdapter<Object> {
    /**
     * @param arg a String array where the first element is the class name
     *            and the second element is the suffix that we will append
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

  /**
   * Contains two Strings which represent standard output and error output.
   */
  private static class Outputs {
    /**
     * String representing the standard output
     */
    private String stdout;
    /**
     * String representing the error output
     */
    private String errout;

    /**
     * Create an Outputs object
     *
     * @param stdout standard output
     * @param errout error output
     */
    public Outputs(String stdout, String errout) {
      this.stdout = stdout;
      this.errout = errout;
    }
  }

  /**
   * Contains two Outputs for compilation and execution.
   */
  private static class Results {
    /**
     * The standard and error output of compiling a Java file.
     */
    private Outputs compOut;
    /**
     * The standard and error output of running a Java file.
     */
    private Outputs runOut;

    /**
     * Create a Results object
     *
     * @param compOut compilation results
     * @param runOut  runtime results
     */
    public Results(Outputs compOut, Outputs runOut) {
      this.compOut = compOut;
      this.runOut = runOut;
    }
  }
}
