package randoop.output;

import static com.github.javaparser.ast.Modifier.PUBLIC;
import static com.github.javaparser.ast.Modifier.STATIC;
import static randoop.output.NameGenerator.numDigits;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
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
import com.github.javaparser.ast.type.VoidType;
import java.util.ArrayList;
import java.util.EnumSet;
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
      compilationUnit.setPackage(new PackageDeclaration(NameExpr.name(packageName)));
    }

    if (afterEachText != null && !afterEachText.isEmpty()) {
      compilationUnit.addImport("org.junit.After");
    }
    if (afterAllText != null && !afterAllText.isEmpty()) {
      compilationUnit.addImport("org.junit.AfterClass");
    }
    if (beforeEachText != null && !beforeEachText.isEmpty()) {
      compilationUnit.addImport("org.junit.Before");
    }
    if (beforeAllText != null && !beforeAllText.isEmpty()) {
      compilationUnit.addImport("org.junit.BeforeClass");
    }
    compilationUnit.addImport("org.junit.FixMethodOrder");
    compilationUnit.addImport("org.junit.Test");
    compilationUnit.addImport("org.junit.runners.MethodSorters");

    // class declaration
    ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass(testClassName);
    classDeclaration.addModifier(PUBLIC);
    classDeclaration.addSingleMemberAnnotation("FixMethodOrder", "MethodSorters.NAME_ASCENDING");

    // add debug field
    FieldDeclaration debugField = classDeclaration.addField("boolean", "debug", PUBLIC, STATIC);
    debugField.getVariables().get(0).setInit(new BooleanLiteralExpr(false));

    if (beforeAllText != null && !beforeAllText.isEmpty()) {
      MethodDeclaration fixture =
          createFixture(BEFORE_ALL, EnumSet.of(PUBLIC, STATIC), BEFORE_ALL_METHOD, beforeAllText);
      if (fixture != null) {
        classDeclaration.addMember(fixture);
      }
    }
    if (afterAllText != null && !afterAllText.isEmpty()) {
      MethodDeclaration fixture =
          createFixture(AFTER_ALL, EnumSet.of(PUBLIC, STATIC), AFTER_ALL_METHOD, afterAllText);
      if (fixture != null) {
        classDeclaration.addMember(fixture);
      }
    }
    if (beforeEachText != null && !beforeEachText.isEmpty()) {
      MethodDeclaration fixture =
          createFixture(BEFORE_EACH, EnumSet.of(PUBLIC), BEFORE_EACH_METHOD, beforeEachText);
      if (fixture != null) {
        classDeclaration.addMember(fixture);
      }
    }
    if (afterEachText != null && !afterEachText.isEmpty()) {
      MethodDeclaration fixture =
          createFixture(AFTER_EACH, EnumSet.of(PUBLIC), AFTER_EACH_METHOD, afterEachText);
      if (fixture != null) {
        classDeclaration.addMember(fixture);
      }
    }

    NameGenerator methodNameGen =
        new NameGenerator(testMethodPrefix, 1, numDigits(sequences.size()));
    for (ExecutableSequence s : sequences) {
      MethodDeclaration testMethod = createTestMethod(testClassName, methodNameGen.next(), s);
      if (testMethod != null) {
        classDeclaration.addMember(testMethod);
      }
    }

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
    MethodDeclaration method =
        new MethodDeclaration(EnumSet.of(PUBLIC), new VoidType(), methodName);
    method.addMarkerAnnotation("Test");
    method.addThrows(Throwable.class);

    BlockStmt body = new BlockStmt();
    NameExpr clazz = new NameExpr("System");
    FieldAccessExpr field = new FieldAccessExpr(clazz, "out");
    MethodCallExpr call = new MethodCallExpr(field, "format");
    call.addArgument(new StringLiteralExpr("%n%s%n"));
    call.addArgument(new StringLiteralExpr(className + "." + methodName));
    IfStmt ifDebug = new IfStmt(NameExpr.name("debug"), new ExpressionStmt(call), null);
    body.addStatement(ifDebug);

    //TODO make sequence generate list of JavaParser statements
    String sequenceBlockString = "{ " + testSequence.toCodeString() + " }";
    try {
      BlockStmt sequenceBlock = JavaParser.parseBlock(sequenceBlockString);
      for (Statement statement : sequenceBlock.getStmts()) {
        body.addStatement(statement);
      }
    } catch (ParseProblemException e) {
      System.out.println("Error creating test method: " + e.getMessage());
      return null;
    }

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
      String annotation, EnumSet<Modifier> modifiers, String methodName, List<String> bodyText) {
    MethodDeclaration method = new MethodDeclaration(modifiers, new VoidType(), methodName);
    method.addMarkerAnnotation(annotation);
    String blockText = "{\n";
    for (String line : bodyText) {
      blockText += line + "\n";
    }
    blockText += "\n}";
    try {
      BlockStmt body = JavaParser.parseBlock(blockText);
      method.setBody(body);
    } catch (ParseProblemException e) {
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
      cu.setPackage(new PackageDeclaration(NameExpr.name(packageName)));
    }
    cu.addImport("org.junit.runner.RunWith");
    cu.addImport("org.junit.runners.Suite");

    ClassOrInterfaceDeclaration suiteClass = cu.addClass(suiteClassName);
    suiteClass.addSingleMemberAnnotation("RunWith", "Suite.class");
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
    suiteClass.addSingleMemberAnnotation("Suite.SuiteClasses", "{ " + classList + " }");
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
      cu.setPackage(new PackageDeclaration(NameExpr.name(packageName)));
    }
    ClassOrInterfaceDeclaration driverClass = cu.addClass(driverName);
    MethodDeclaration mainMethod =
        new MethodDeclaration(EnumSet.of(PUBLIC, STATIC), new VoidType(), "main");
    mainMethod.addParameter("String", "args");
    mainMethod.getParameters().get(0).setVarArgs(true);

    BlockStmt body = new BlockStmt();

    String failureVariableName = "hadFailure";
    VariableDeclarationExpr variableExpr =
        new VariableDeclarationExpr(
            new PrimitiveType(PrimitiveType.Primitive.Boolean), failureVariableName);
    variableExpr.getVariables().get(0).setInit(new BooleanLiteralExpr(false));
    body.addStatement(new ExpressionStmt(variableExpr));

    NameGenerator instanceNameGen = new NameGenerator("t");
    for (String testClass : testClassNames) {
      if (beforeAllText != null) {
        body.addStatement(
            new ExpressionStmt(new MethodCallExpr(NameExpr.name(testClass), BEFORE_ALL_METHOD)));
      }

      String testVariable = instanceNameGen.next();

      variableExpr = new VariableDeclarationExpr(new ClassOrInterfaceType(testClass), testVariable);
      variableExpr
          .getVariables()
          .get(0)
          .setInit(new ObjectCreationExpr(null, new ClassOrInterfaceType(testClass), null));
      body.addStatement(new ExpressionStmt(variableExpr));

      int classMethodCount = classMethodCounts.get(testClass);
      NameGenerator methodGen = new NameGenerator("test", 1, numDigits(classMethodCount));

      while (methodGen.nameCount() < classMethodCount) {
        if (beforeEachText != null) {
          body.addStatement(
              new ExpressionStmt(
                  new MethodCallExpr(NameExpr.name(testVariable), BEFORE_EACH_METHOD)));
        }
        String methodName = methodGen.next();

        TryStmt tryStmt = new TryStmt();
        BlockStmt tryBlock = new BlockStmt();
        tryBlock.addStatement(
            new ExpressionStmt(new MethodCallExpr(NameExpr.name(testVariable), methodName)));
        tryStmt.setTryBlock(tryBlock);
        CatchClause catchClause = new CatchClause();
        catchClause.setParam(
            new Parameter(
                new ClassOrInterfaceType("Throwable"), new VariableDeclarator("e").getId()));
        BlockStmt catchBlock = new BlockStmt();
        catchBlock.addStatement(
            new ExpressionStmt(
                new AssignExpr(
                    NameExpr.name(failureVariableName),
                    new BooleanLiteralExpr(true),
                    AssignExpr.Operator.assign)));
        catchBlock.addStatement(
            new ExpressionStmt(new MethodCallExpr(NameExpr.name("e"), "printStackTrace")));
        catchClause.setBody(catchBlock);
        List<CatchClause> catches = new ArrayList<>();
        catches.add(catchClause);
        tryStmt.setCatchs(catches);
        body.addStatement(tryStmt);

        if (afterEachText != null) {
          body.addStatement(
              new ExpressionStmt(
                  new MethodCallExpr(NameExpr.name(testVariable), AFTER_EACH_METHOD)));
        }
      }

      if (afterAllText != null) {
        body.addStatement(
            new ExpressionStmt(new MethodCallExpr(NameExpr.name(testClass), AFTER_ALL_METHOD)));
      }
    }

    BlockStmt exitCall = new BlockStmt();
    List<Expression> args = new ArrayList<>();
    args.add(new IntegerLiteralExpr("1"));
    exitCall.addStatement(
        new ExpressionStmt(new MethodCallExpr(NameExpr.name("System"), "exit", args)));
    body.addStatement(new IfStmt(NameExpr.name(failureVariableName), exitCall, null));

    mainMethod.setBody(body);
    driverClass.addMember(mainMethod);
    return cu.toString();
  }
}
