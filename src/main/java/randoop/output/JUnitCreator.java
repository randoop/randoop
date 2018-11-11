package randoop.output;

import static randoop.output.NameGenerator.numDigits;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.TokenMgrError;
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
import java.util.Objects;
import java.util.Set;
import randoop.Globals;
import randoop.main.RandoopBug;
import randoop.sequence.ExecutableSequence;

/** Creates Java source as {@code String} for a suite of JUnit4 tests. */
public class JUnitCreator {

  private final String packageName;

  /**
   * classMethodCounts maps test class names to the number of methods in each class. This is used to
   * generate lists of method names for a class, since current convention is that a test method is
   * named "test"+i for some integer i.
   */
  private Map<String, Integer> classMethodCounts;

  /** The Java text for BeforeAll method of generated test class. */
  private BlockStmt beforeAllBody = null;

  /** The Java text for AfterAll method of generated test class. */
  private BlockStmt afterAllBody = null;

  /** The Java text for BeforeEach method of generated test class. */
  private BlockStmt beforeEachBody = null;

  /** The Java text for AfterEach method of generated test class. */
  private BlockStmt afterEachBody = null;

  /** The JUnit annotation for the BeforeAll option. */
  private static final String BEFORE_ALL = "BeforeClass";

  /** The JUnit annotation for the AfterAll option. */
  private static final String AFTER_ALL = "AfterClass";

  /** The JUnit annotation for the BeforeEach option. */
  private static final String BEFORE_EACH = "Before";

  /** The JUnit annotation for the AfterEach option. */
  private static final String AFTER_EACH = "After";

  /** The method name for the BeforeAll option. */
  private static final String BEFORE_ALL_METHOD = "setupAll";

  /** The method name for the AfterAll option. */
  private static final String AFTER_ALL_METHOD = "teardownAll";

  /** The method name for the BeforeEach option. */
  private static final String BEFORE_EACH_METHOD = "setup";

  /** The method name for the AfterEach option. */
  private static final String AFTER_EACH_METHOD = "teardown";

  public static JUnitCreator getTestCreator(
      String junit_package_name,
      BlockStmt beforeAllBody,
      BlockStmt afterAllBody,
      BlockStmt beforeEachBody,
      BlockStmt afterEachBody) {
    assert !Objects.equals(junit_package_name, "");
    JUnitCreator junitCreator = new JUnitCreator(junit_package_name);
    if (beforeAllBody != null) {
      junitCreator.addBeforeAll(beforeAllBody);
    }
    if (afterAllBody != null) {
      junitCreator.addAfterAll(afterAllBody);
    }
    if (beforeEachBody != null) {
      junitCreator.addBeforeEach(beforeEachBody);
    }
    if (afterEachBody != null) {
      junitCreator.addAfterEach(afterEachBody);
    }
    return junitCreator;
  }

  private JUnitCreator(String packageName) {
    assert !Objects.equals(packageName, "");
    this.packageName = packageName;
    this.classMethodCounts = new LinkedHashMap<>();
  }

  /**
   * Add text for BeforeClass-annotated method in each generated test class.
   *
   * @param body the (Java) text for method
   */
  private void addBeforeAll(BlockStmt body) {
    this.beforeAllBody = body;
  }

  /**
   * Add text for AfterClass-annotated method in each generated text class.
   *
   * @param body the (Java) text for method
   */
  private void addAfterAll(BlockStmt body) {
    this.afterAllBody = body;
  }

  /**
   * Add text for Before-annotated method in each generated test class.
   *
   * @param body the (Java) text for method
   */
  private void addBeforeEach(BlockStmt body) {
    this.beforeEachBody = body;
  }

  /**
   * Add text for After-annotated method in each generated test class.
   *
   * @param text the (Java) text for method
   */
  private void addAfterEach(BlockStmt text) {
    this.afterEachBody = text;
  }

  public CompilationUnit createTestClass(
      String testClassName, String testMethodPrefix, List<ExecutableSequence> sequences) {
    this.classMethodCounts.put(testClassName, sequences.size());

    CompilationUnit compilationUnit = new CompilationUnit();
    if (packageName != null) {
      compilationUnit.setPackage(new PackageDeclaration(new NameExpr(packageName)));
    }

    List<ImportDeclaration> imports = new ArrayList<>();
    if (afterEachBody != null) {
      imports.add(new ImportDeclaration(new NameExpr("org.junit.After"), false, false));
    }
    if (afterAllBody != null) {
      imports.add(new ImportDeclaration(new NameExpr("org.junit.AfterClass"), false, false));
    }
    if (beforeEachBody != null) {
      imports.add(new ImportDeclaration(new NameExpr("org.junit.Before"), false, false));
    }
    if (beforeAllBody != null) {
      imports.add(new ImportDeclaration(new NameExpr("org.junit.BeforeClass"), false, false));
    }
    imports.add(new ImportDeclaration(new NameExpr("org.junit.FixMethodOrder"), false, false));
    imports.add(new ImportDeclaration(new NameExpr("org.junit.Test"), false, false));
    imports.add(
        new ImportDeclaration(new NameExpr("org.junit.runners.MethodSorters"), false, false));
    compilationUnit.setImports(imports);

    // class declaration
    ClassOrInterfaceDeclaration classDeclaration =
        new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, testClassName);
    List<AnnotationExpr> annotations = new ArrayList<>();
    annotations.add(
        new SingleMemberAnnotationExpr(
            new NameExpr("FixMethodOrder"), new NameExpr("MethodSorters.NAME_ASCENDING")));
    classDeclaration.setAnnotations(annotations);

    List<BodyDeclaration> bodyDeclarations = new ArrayList<>();
    // add debug field
    VariableDeclarator debugVariable = new VariableDeclarator(new VariableDeclaratorId("debug"));
    debugVariable.setInit(new BooleanLiteralExpr(false));
    FieldDeclaration debugField =
        new FieldDeclaration(
            Modifier.PUBLIC | Modifier.STATIC,
            new PrimitiveType(PrimitiveType.Primitive.Boolean),
            debugVariable);
    bodyDeclarations.add(debugField);

    if (beforeAllBody != null) {
      MethodDeclaration fixture =
          createFixture(
              BEFORE_ALL, Modifier.PUBLIC | Modifier.STATIC, BEFORE_ALL_METHOD, beforeAllBody);
      if (fixture != null) {
        bodyDeclarations.add(fixture);
      }
    }
    if (afterAllBody != null) {
      MethodDeclaration fixture =
          createFixture(
              AFTER_ALL, Modifier.PUBLIC | Modifier.STATIC, AFTER_ALL_METHOD, afterAllBody);
      if (fixture != null) {
        bodyDeclarations.add(fixture);
      }
    }
    if (beforeEachBody != null) {
      MethodDeclaration fixture =
          createFixture(BEFORE_EACH, Modifier.PUBLIC, BEFORE_EACH_METHOD, beforeEachBody);
      if (fixture != null) {
        bodyDeclarations.add(fixture);
      }
    }
    if (afterEachBody != null) {
      MethodDeclaration fixture =
          createFixture(AFTER_EACH, Modifier.PUBLIC, AFTER_EACH_METHOD, afterEachBody);
      if (fixture != null) {
        bodyDeclarations.add(fixture);
      }
    }

    NameGenerator methodNameGen =
        new NameGenerator(testMethodPrefix, 1, numDigits(sequences.size()));
    for (ExecutableSequence eseq : sequences) {
      MethodDeclaration testMethod = createTestMethod(testClassName, methodNameGen.next(), eseq);
      if (testMethod != null) {
        bodyDeclarations.add(testMethod);
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

    List<ReferenceType> throwsList = new ArrayList<>();
    throwsList.add(new ReferenceType(new ClassOrInterfaceType("Throwable")));
    method.setThrows(throwsList);

    BlockStmt body = new BlockStmt();
    List<Statement> statements = new ArrayList<>();
    FieldAccessExpr field = new FieldAccessExpr(new NameExpr("System"), "out");
    MethodCallExpr call = new MethodCallExpr(field, "format");

    List<Expression> arguments = new ArrayList<>();
    arguments.add(new StringLiteralExpr("%n%s%n"));
    arguments.add(new StringLiteralExpr(className + "." + methodName));
    call.setArgs(arguments);
    statements.add(new IfStmt(new NameExpr("debug"), new ExpressionStmt(call), null));

    // TODO make sequence generate list of JavaParser statements
    String sequenceBlockString = "{ " + testSequence.toCodeString() + " }";
    try {
      BlockStmt sequenceBlock = JavaParser.parseBlock(sequenceBlockString);
      statements.addAll(sequenceBlock.getStmts());
    } catch (ParseException e) {
      System.out.println(
          "Parse error while creating test method " + className + "." + methodName + " for block ");
      System.out.println(sequenceBlockString);
      throw new RandoopBug("Parse error while creating test method", e);
    } catch (TokenMgrError e) {
      System.out.println(
          "Lexical error while creating test method " + className + "." + methodName);
      System.out.println("Exception: " + e.getMessage());
      System.out.println(sequenceBlockString);
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
   * @param body the {@code BlockStmt} for the fixture
   * @return the fixture method as a {@code String}
   */
  private MethodDeclaration createFixture(
      String annotation, int modifiers, String methodName, BlockStmt body) {
    MethodDeclaration method = new MethodDeclaration(modifiers, new VoidType(), methodName);
    List<AnnotationExpr> annotations = new ArrayList<>();
    annotations.add(new MarkerAnnotationExpr(new NameExpr(annotation)));
    method.setAnnotations(annotations);
    method.setBody(body);
    return method;
  }

  /**
   * Creates the JUnit4 suite class for the tests in this object as a {@code String}.
   *
   * @param suiteClassName the name of the suite class created
   * @param testClassNames the names of the test classes in the suite
   * @return the {@code String} with the declaration for the suite class
   */
  public String createTestSuite(String suiteClassName, Set<String> testClassNames) {
    CompilationUnit compilationUnit = new CompilationUnit();
    if (packageName != null) {
      compilationUnit.setPackage(new PackageDeclaration(new NameExpr(packageName)));
    }
    List<ImportDeclaration> imports = new ArrayList<>();
    imports.add(new ImportDeclaration(new NameExpr("org.junit.runner.RunWith"), false, false));
    imports.add(new ImportDeclaration(new NameExpr("org.junit.runners.Suite"), false, false));
    compilationUnit.setImports(imports);

    ClassOrInterfaceDeclaration suiteClass =
        new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, suiteClassName);
    List<AnnotationExpr> annotations = new ArrayList<>();
    annotations.add(
        new SingleMemberAnnotationExpr(new NameExpr("RunWith"), new NameExpr("Suite.class")));
    StringBuilder classList = new StringBuilder();
    Iterator<String> testIterator = testClassNames.iterator();
    if (testIterator.hasNext()) {
      String classCode = testIterator.next() + ".class";
      while (testIterator.hasNext()) {
        classList.append(classCode).append(", ");
        classCode = testIterator.next() + ".class";
      }
      classList.append(classCode);
    }
    annotations.add(
        new SingleMemberAnnotationExpr(
            new NameExpr("Suite.SuiteClasses"), new NameExpr("{ " + classList + " }")));
    suiteClass.setAnnotations(annotations);
    List<TypeDeclaration> types = new ArrayList<>();
    types.add(suiteClass);
    compilationUnit.setTypes(types);
    return compilationUnit.toString();
  }

  /**
   * Create non-reflective test driver as a main class.
   *
   * @param driverName the name for the driver class
   * @param testClassNames the names of the test classes in the suite
   * @return the test driver class as a {@code String}
   */
  public String createTestDriver(String driverName, Set<String> testClassNames) {
    CompilationUnit compilationUnit = new CompilationUnit();
    if (packageName != null) {
      compilationUnit.setPackage(new PackageDeclaration(new NameExpr(packageName)));
    }

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
      if (beforeAllBody != null) {
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
        if (beforeEachBody != null) {
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
        catchClause.setCatchBlock(catchBlock);
        List<CatchClause> catches = new ArrayList<>();
        catches.add(catchClause);
        tryStmt.setCatchs(catches);
        bodyStatements.add(tryStmt);

        if (afterEachBody != null) {
          bodyStatements.add(
              new ExpressionStmt(
                  new MethodCallExpr(new NameExpr(testVariable), AFTER_EACH_METHOD)));
        }
      }

      if (afterAllBody != null) {
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
    bodyDeclarations.add(mainMethod);

    ClassOrInterfaceDeclaration driverClass =
        new ClassOrInterfaceDeclaration(Modifier.PUBLIC, false, driverName);
    driverClass.setMembers(bodyDeclarations);

    List<TypeDeclaration> types = new ArrayList<>();
    types.add(driverClass);
    compilationUnit.setTypes(types);
    return compilationUnit.toString();
  }

  public static BlockStmt parseFixture(List<String> bodyText) throws ParseException {
    if (bodyText == null) {
      return null;
    }
    StringBuilder blockText = new StringBuilder();
    blockText.append("{").append(Globals.lineSep);
    for (String line : bodyText) {
      blockText.append(line).append(Globals.lineSep);
    }
    blockText.append(Globals.lineSep).append("}");
    return JavaParser.parseBlock(blockText.toString());
  }
}
