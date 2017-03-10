package randoop.output;

import static randoop.output.NameGenerator.numDigits;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.VoidType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import randoop.sequence.ExecutableSequence;

/** Creates Java source as {@code String} for a suite of JUnit4 tests. */
public class JUnitCreator {

  private final String packageName;
  private final String testMethodPrefix;
  private final Set<String> testClassNames;

  /**
   * classMethodCounts maps test class names to the number of methods in each class. This is used to
   * generate lists of method names for a class, since current convention is that a test method is
   * named "test"+i for some integer i.
   */
  private Map<String, Integer> classMethodCounts;

  /** The Java text for BeforeAll method of generated test class. */
  private List<String> beforeAllText = null;

  /** The Java text for AfterAll method of generated test class. */
  private List<String> afterAllText = null;

  /** The Java text for BeforeEach method of generated test class. */
  private List<String> beforeEachText = null;

  /** The Java text for AfterEach method of generated test class. */
  private List<String> afterEachText = null;

  /** The JUnit annotation for the BeforeAll option */
  private static final String BEFORE_ALL = "BeforeClass";

  /** The JUnit annotation for the AfterAll option */
  private static final String AFTER_ALL = "AfterClass";

  /** The JUnit annotation for the BeforeEach option */
  private static final String BEFORE_EACH = "Before";

  /** The JUnit annotation for the AfterEach option */
  private static final String AFTER_EACH = "After";

  /** The method name for the BeforeAll option */
  private static final String BEFORE_ALL_METHOD = "setupAll";

  /** The method name for the AfterAll option */
  private static final String AFTER_ALL_METHOD = "teardownAll";

  /** The method name for the BeforeEach option */
  private static final String BEFORE_EACH_METHOD = "setup";

  /** The method name for the AfterEach option */
  private static final String AFTER_EACH_METHOD = "teardown";

  public static JUnitCreator getTestCreator(
      String junit_package_name,
      String testMethodPrefix,
      List<String> beforeAllText,
      List<String> afterAllText,
      List<String> beforeEachText,
      List<String> afterEachText) {
    JUnitCreator junitCreator = new JUnitCreator(junit_package_name, testMethodPrefix);
    if (beforeAllText != null) {
      junitCreator.addBeforeAll(beforeAllText);
    }
    if (afterAllText != null) {
      junitCreator.addAfterAll(afterAllText);
    }
    if (beforeEachText != null) {
      junitCreator.addBeforeEach(beforeEachText);
    }
    if (afterEachText != null) {
      junitCreator.addAfterEach(afterEachText);
    }
    return junitCreator;
  }

  private JUnitCreator(String packageName, String testMethodPrefix) {
    this.packageName = packageName;
    this.testMethodPrefix = testMethodPrefix;
    this.testClassNames = new TreeSet<>();
    this.classMethodCounts = new LinkedHashMap<>();
  }

  /**
   * Add text for BeforeClass-annotated method in each generated test class.
   *
   * @param text the (Java) text for method
   */
  private void addBeforeAll(List<String> text) {
    this.beforeAllText = text;
  }

  /**
   * Add text for AfterClass-annotated method in each generated text class.
   *
   * @param text the (Java) text for method
   */
  private void addAfterAll(List<String> text) {
    this.afterAllText = text;
  }

  /**
   * Add text for Before-annotated method in each generated test class.
   *
   * @param text the (Java) text for method
   */
  private void addBeforeEach(List<String> text) {
    this.beforeEachText = text;
  }

  /**
   * Add text for After-annotated method in each generated test class.
   *
   * @param text the (Java) text for method
   */
  private void addAfterEach(List<String> text) {
    this.afterEachText = text;
  }

  public CompilationUnit createTestClass(String testClassName, List<ExecutableSequence> sequences) {
    this.testClassNames.add(testClassName);
    this.classMethodCounts.put(testClassName, sequences.size());

    CompilationUnit compilationUnit = new CompilationUnit();
    if (packageName != null && !packageName.isEmpty()) {
      compilationUnit.setPackage(new PackageDeclaration(new NameExpr(packageName)));
    }

    List<ImportDeclaration> imports = new ArrayList<>();
    if (afterEachText != null && !afterEachText.isEmpty()) {
      imports.add(new ImportDeclaration(new NameExpr("org.junit.After"), false, false));
      //compilationUnit.addImport("org.junit.After");
    }
    if (afterAllText != null && !afterAllText.isEmpty()) {
      imports.add(new ImportDeclaration(new NameExpr("org.junit.AfterClass"), false, false));
      //compilationUnit.addImport("org.junit.AfterClass");
    }
    if (beforeEachText != null && !beforeEachText.isEmpty()) {
      imports.add(new ImportDeclaration(new NameExpr("org.junit.Before"), false, false));
      //compilationUnit.addImport("org.junit.Before");
    }
    if (beforeAllText != null && !beforeAllText.isEmpty()) {
      imports.add(new ImportDeclaration(new NameExpr("org.junit.BeforeClass"), false, false));
      //compilationUnit.addImport("org.junit.BeforeClass");
    }
    imports.add(new ImportDeclaration(new NameExpr("org.junit.FixMethodOrder"), false, false));
    //compilationUnit.addImport("org.junit.FixMethodOrder");
    imports.add(new ImportDeclaration(new NameExpr("org.junit.Test"), false, false));
    //compilationUnit.addImport("org.junit.Test");
    imports.add(
        new ImportDeclaration(new NameExpr("org.junit.runners.MethodSorters"), false, false));
    //compilationUnit.addImport("org.junit.runners.MethodSorters");
    compilationUnit.setImports(imports);

    // class declaration
    //ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass(testClassName);
    ClassOrInterfaceDeclaration classDeclaration =
        new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, testClassName);
    //classDeclaration.addModifier(PUBLIC);
    //classDeclaration.addSingleMemberAnnotation("FixMethodOrder", "MethodSorters.NAME_ASCENDING");
    List<AnnotationExpr> annotations = new ArrayList<>();
    annotations.add(
        new SingleMemberAnnotationExpr(
            new NameExpr("FixMethodOrder"), new NameExpr("MethodSorters.NAME_ASCENDING")));
    classDeclaration.setAnnotations(annotations);

    List<BodyDeclaration> bodyDeclarations = new ArrayList<>();
    // add debug field
    //FieldDeclaration debugField = classDeclaration.addField("boolean", "debug", PUBLIC, STATIC);

    VariableDeclarator debugVariable = new VariableDeclarator(new VariableDeclaratorId("debug"));
    debugVariable.setInit(new BooleanLiteralExpr(false));
    FieldDeclaration debugField =
        new FieldDeclaration(
            Modifier.PUBLIC | Modifier.STATIC,
            new PrimitiveType(PrimitiveType.Primitive.Boolean),
            debugVariable);
    bodyDeclarations.add(debugField);

    if (beforeAllText != null && !beforeAllText.isEmpty()) {
      MethodDeclaration fixture =
          createFixture(
              BEFORE_ALL, Modifier.PUBLIC | Modifier.STATIC, BEFORE_ALL_METHOD, beforeAllText);
      if (fixture != null) {
        bodyDeclarations.add(fixture);
        //classDeclaration.addMember(fixture);
      }
    }
    if (afterAllText != null && !afterAllText.isEmpty()) {
      MethodDeclaration fixture =
          createFixture(
              AFTER_ALL, Modifier.PUBLIC | Modifier.STATIC, AFTER_ALL_METHOD, afterAllText);
      if (fixture != null) {
        bodyDeclarations.add(fixture);
        //classDeclaration.addMember(fixture);
      }
    }
    if (beforeEachText != null && !beforeEachText.isEmpty()) {
      MethodDeclaration fixture =
          createFixture(BEFORE_EACH, Modifier.PUBLIC, BEFORE_EACH_METHOD, beforeEachText);
      if (fixture != null) {
        bodyDeclarations.add(fixture);
        //classDeclaration.addMember(fixture);
      }
    }
    if (afterEachText != null && !afterEachText.isEmpty()) {
      MethodDeclaration fixture =
          createFixture(AFTER_EACH, Modifier.PUBLIC, AFTER_EACH_METHOD, afterEachText);
      if (fixture != null) {
        bodyDeclarations.add(fixture);
        //classDeclaration.addMember(fixture);
      }
    }

    NameGenerator methodNameGen =
        new NameGenerator(testMethodPrefix, 1, numDigits(sequences.size()));
    for (ExecutableSequence s : sequences) {
      MethodDeclaration testMethod = createTestMethod(testClassName, methodNameGen.next(), s);
      if (testMethod != null) {
        bodyDeclarations.add(testMethod);
        //classDeclaration.addMember(testMethod);
      }
    }
    classDeclaration.setMembers(bodyDeclarations);
    List<TypeDeclaration> types = new ArrayList<>();
    types.add(classDeclaration);
    compilationUnit.setTypes(types);

    return compilationUnit;
  }

  /**
   * Creates a test method as a {@code String} for the sequence {@code testSequence}.
   *
   * @param className the name of the test class
   * @param methodName the name of the test method
   * @param testSequence the {@link ExecutableSequence} test sequence
   * @return the {@code String} for the test method
   */
  private MethodDeclaration createTestMethod(
      String className, String methodName, ExecutableSequence testSequence) {
    MethodDeclaration method = new MethodDeclaration(Modifier.PUBLIC, new VoidType(), methodName);
    List<AnnotationExpr> annotations = new ArrayList<>();
    annotations.add(new MarkerAnnotationExpr(new NameExpr("Test")));
    method.setAnnotations(annotations);
    //method.addMarkerAnnotation("Test");
    List<ReferenceType> throwsList = new ArrayList<>();
    throwsList.add(new ReferenceType(new ClassOrInterfaceType("Throwable")));
    method.setThrows(throwsList);
    //    method.addThrows(Throwable.class);

    BlockStmt body = new BlockStmt();
    List<Statement> statements = new ArrayList<>();
    FieldAccessExpr field = new FieldAccessExpr(new NameExpr("System"), "out");
    MethodCallExpr call = new MethodCallExpr(field, "format");

    //call.addArgument(new StringLiteralExpr("%n%s%n"));
    //call.addArgument(new StringLiteralExpr(className + "." + methodName));
    List<Expression> arguments = new ArrayList<>();
    arguments.add(new StringLiteralExpr("%n%s%n"));
    arguments.add(new StringLiteralExpr(className + "." + methodName));
    call.setArgs(arguments);
    //body.addStatement(ifDebug);
    statements.add(new IfStmt(new NameExpr("debug"), new ExpressionStmt(call), null));

    //TODO make sequence generate list of JavaParser statements
    String sequenceBlockString = "{ " + testSequence.toCodeString() + " }";
    try {
      BlockStmt sequenceBlock = JavaParser.parseBlock(sequenceBlockString);
      for (Statement statement : sequenceBlock.getStmts()) {
        //body.addStatement(statement);
        statements.add(statement);
      }
    } catch (ParseException e) {
      System.out.println("Error creating test method: " + e.getMessage());
      return null;
    }

    body.setStmts(statements);
    method.setBody(body);
    return method;
  }

  /**
   * Creates the declaration of a single test fixture.
   *
   * @param annotation the fixture annotation
   * @param modifiers the method modifiers for fixture declaration
   * @param methodName the name of the fixture method
   * @param bodyText the text of the fixture method, must be non-null and non-empty
   * @return the fixture method as a {@code String}
   */
  private MethodDeclaration createFixture(
      String annotation, int modifiers, String methodName, List<String> bodyText) {
    MethodDeclaration method = new MethodDeclaration(modifiers, new VoidType(), methodName);
    //method.addMarkerAnnotation(annotation);
    List<AnnotationExpr> annotations = new ArrayList<>();
    annotations.add(new MarkerAnnotationExpr(new NameExpr(annotation)));
    method.setAnnotations(annotations);
    String blockText = "{\n";
    for (String line : bodyText) {
      blockText += line + "\n";
    }
    blockText += "\n}";
    try {
      BlockStmt body = JavaParser.parseBlock(blockText);
      method.setBody(body);
    } catch (ParseException e) {
      System.out.println("Error creating test method: " + e.getMessage());
      return null;
    }

    return method;
  }

  /**
   * Creates the JUnit4 suite class for the tests in this object as a {@code String}.
   *
   * @param suiteClassName the name of the suite class created
   * @return the {@code String} with the declaration for the suite class
   */
  public String createSuiteClass(String suiteClassName) {
    CompilationUnit cu = new CompilationUnit();
    if (packageName != null && !packageName.isEmpty()) {
      cu.setPackage(new PackageDeclaration(new NameExpr(packageName)));
    }
    List<ImportDeclaration> imports = new ArrayList<>();
    imports.add(new ImportDeclaration(new NameExpr("org.junit.runner.RunWith"), false, false));
    imports.add(new ImportDeclaration(new NameExpr("org.junit.runners.Suite"), false, false));
    //cu.addImport("org.junit.runner.RunWith");
    //cu.addImport("org.junit.runners.Suite");

    cu.setImports(imports);

    ClassOrInterfaceDeclaration suiteClass =
        new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, suiteClassName);
    //suiteClass.addSingleMemberAnnotation("RunWith", "Suite.class");
    List<AnnotationExpr> annotations = new ArrayList<>();
    annotations.add(
        new SingleMemberAnnotationExpr(new NameExpr("RunWith"), new NameExpr("Suite.class")));
    String classList = "";
    Iterator<String> testIterator = testClassNames.iterator();
    if (testIterator.hasNext()) {
      String classString = testIterator.next() + ".class";
      while (testIterator.hasNext()) {
        classList += classString + ", ";
        classString = testIterator.next() + ".class";
      }
      classList += classString;
    }
    //    suiteClass.addSingleMemberAnnotation("Suite.SuiteClasses", "{ " + classList + " }");
    annotations.add(
        new SingleMemberAnnotationExpr(
            new NameExpr("Suite.SuiteClasses"), new NameExpr("{ " + classList + " }")));
    suiteClass.setAnnotations(annotations);
    List<TypeDeclaration> types = new ArrayList<>();
    types.add(suiteClass);
    cu.setTypes(types);
    return cu.toString();
  }

  /**
   * Create non-reflective test driver as a main class.
   *
   * @param driverName the name for the driver class
   * @return the test driver class as a {@code String}
   */
  public String createTestDriver(String driverName) {
    CompilationUnit cu = new CompilationUnit();
    if (packageName != null && !packageName.isEmpty()) {
      cu.setPackage(new PackageDeclaration(new NameExpr(packageName)));
    }
    //ClassOrInterfaceDeclaration driverClass = cu.addClass(driverName);
    MethodDeclaration mainMethod =
        new MethodDeclaration(Modifier.PUBLIC | Modifier.STATIC, new VoidType(), "main");
    List<Parameter> parameters = new ArrayList<>();
    Parameter parameter =
        new Parameter(new ClassOrInterfaceType("String"), new VariableDeclaratorId("args"));
    parameter.setVarArgs(true);
    parameters.add(parameter);
    mainMethod.setParameters(parameters);

    mainMethod.getParameters().get(0).setVarArgs(true);

    List<Statement> bodyStatements = new ArrayList<>();
    String failureVariableName = "hadFailure";
    VariableDeclarator variableDecl =
        new VariableDeclarator(
            new VariableDeclaratorId(failureVariableName), new BooleanLiteralExpr(false));
    List<VariableDeclarator> variableList = new ArrayList<>();
    variableList.add(variableDecl);
    VariableDeclarationExpr variableExpr =
        new VariableDeclarationExpr(
            new PrimitiveType(PrimitiveType.Primitive.Boolean), variableList);
    bodyStatements.add(new ExpressionStmt(variableExpr));

    NameGenerator instanceNameGen = new NameGenerator("t");
    for (String testClass : testClassNames) {
      if (beforeAllText != null) {
        bodyStatements.add(
            new ExpressionStmt(new MethodCallExpr(new NameExpr(testClass), BEFORE_ALL_METHOD)));
      }

      String testVariable = instanceNameGen.next();
      variableDecl =
          new VariableDeclarator(
              new VariableDeclaratorId(testVariable),
              new ObjectCreationExpr(null, new ClassOrInterfaceType(testClass), null));
      variableList = new ArrayList<>();
      variableList.add(variableDecl);
      variableExpr = new VariableDeclarationExpr(new ClassOrInterfaceType(testClass), variableList);
      bodyStatements.add(new ExpressionStmt(variableExpr));

      int classMethodCount = classMethodCounts.get(testClass);
      NameGenerator methodGen = new NameGenerator("test", 1, numDigits(classMethodCount));

      while (methodGen.nameCount() < classMethodCount) {
        if (beforeEachText != null) {
          bodyStatements.add(
              new ExpressionStmt(
                  new MethodCallExpr(new NameExpr(testVariable), BEFORE_EACH_METHOD)));
        }
        String methodName = methodGen.next();

        TryStmt tryStmt = new TryStmt();
        List<Statement> tryStatements = new ArrayList<>();
        tryStatements.add(
            new ExpressionStmt(new MethodCallExpr(new NameExpr(testVariable), methodName)));
        BlockStmt tryBlock = new BlockStmt();

        tryBlock.setStmts(tryStatements);
        tryStmt.setTryBlock(tryBlock);
        CatchClause catchClause = new CatchClause();
        catchClause.setParam(
            new Parameter(new ClassOrInterfaceType("Throwable"), new VariableDeclaratorId("e")));
        BlockStmt catchBlock = new BlockStmt();
        List<Statement> catchStatements = new ArrayList<>();
        catchStatements.add(
            new ExpressionStmt(
                new AssignExpr(
                    new NameExpr(failureVariableName),
                    new BooleanLiteralExpr(true),
                    AssignExpr.Operator.assign)));
        catchStatements.add(
            new ExpressionStmt(new MethodCallExpr(new NameExpr("e"), "printStackTrace")));

        catchBlock.setStmts(catchStatements);
        //catchClause.setBody(catchBlock);
        catchClause.setCatchBlock(catchBlock);
        List<CatchClause> catches = new ArrayList<>();
        catches.add(catchClause);
        tryStmt.setCatchs(catches);
        bodyStatements.add(tryStmt);

        if (afterEachText != null) {
          bodyStatements.add(
              new ExpressionStmt(
                  new MethodCallExpr(new NameExpr(testVariable), AFTER_EACH_METHOD)));
        }
      }

      if (afterAllText != null) {
        bodyStatements.add(
            new ExpressionStmt(new MethodCallExpr(new NameExpr(testClass), AFTER_ALL_METHOD)));
      }
    }

    BlockStmt exitCall = new BlockStmt();
    List<Expression> args = new ArrayList<>();
    args.add(new IntegerLiteralExpr("1"));
    List<Statement> exitStatement = new ArrayList<>();
    exitStatement.add(new ExpressionStmt(new MethodCallExpr(new NameExpr("System"), "exit", args)));
    exitCall.setStmts(exitStatement);
    bodyStatements.add(new IfStmt(new NameExpr(failureVariableName), exitCall, null));

    BlockStmt body = new BlockStmt();
    body.setStmts(bodyStatements);
    mainMethod.setBody(body);
    List<BodyDeclaration> bodyDeclarations = new ArrayList<>();
    //driverClass.addMember(mainMethod);
    bodyDeclarations.add(mainMethod);

    ClassOrInterfaceDeclaration driverClass =
        new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, driverName);
    driverClass.setMembers(bodyDeclarations);

    List<TypeDeclaration> types = new ArrayList<>();
    types.add(driverClass);
    cu.setTypes(types);
    return cu.toString();
  }
}
