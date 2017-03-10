package randoop.compile;

import static java.lang.reflect.Modifier.PUBLIC;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import org.junit.Test;

public class SequenceCompilerTest {

  @Test
  public void compilableTest() {

    SequenceCompiler compiler = getSequenceCompiler();

    String simpleClass = createCompilableClass();

    Class<?> compiledClass = null;
    try {
      compiledClass = compiler.compile("", "Simple", simpleClass);
    } catch (SequenceCompilerException e) {
      System.out.print(e.getMessage());
      if (e.getCause() != null) System.out.print(": " + e.getCause().getMessage());
      System.out.println();
      printDiagnostics(System.out, e.getDiagnostics().getDiagnostics());
      fail("compilation failed");
    }

    try {
      Constructor<?> simpleCons = compiledClass.getConstructor();
      Object object = simpleCons.newInstance();
      Method zeroMethod = compiledClass.getMethod("zero");
      Object value = zeroMethod.invoke(object);
      assertThat("return value should be 0", (Integer) value, is(equalTo(0)));
    } catch (NoSuchMethodException e) {
      fail("could not load zero method: " + e.getMessage());
    } catch (IllegalAccessException e) {
      fail("access error: " + e.getMessage());
    } catch (InstantiationException e) {
      fail("cannot instantiate class: " + e.getMessage());
    } catch (InvocationTargetException e) {
      fail("failure during invocation: " + e.getMessage());
    }
  }

  private String createCompilableClass() {
    CompilationUnit cu = new CompilationUnit();
    ClassOrInterfaceDeclaration classDeclaration =
        new ClassOrInterfaceDeclaration(PUBLIC, false, "Simple");
    MethodDeclaration method =
        new MethodDeclaration(PUBLIC, new PrimitiveType(PrimitiveType.Primitive.Int), "zero");
    ReturnStmt statement = new ReturnStmt(new IntegerLiteralExpr("0"));
    BlockStmt body = new BlockStmt();
    List<Statement> statements = new ArrayList<>();
    statements.add(statement);
    body.setStmts(statements);
    method.setBody(body);

    List<BodyDeclaration> bodyDeclarations = new ArrayList<>();
    bodyDeclarations.add(method);
    classDeclaration.setMembers(bodyDeclarations);
    List<TypeDeclaration> types = new ArrayList<>();
    types.add(classDeclaration);
    cu.setTypes(types);
    return cu.toString();
  }

  @Test
  public void uncompilableTest() {
    SequenceCompiler compiler = getSequenceCompiler();
    String classSource = createUncompilableClass();
    try {
      compiler.compile("", "SimplyBad", classSource);
      fail("should not compile");
    } catch (SequenceCompilerException e) {
      if (e.getCause() != null) System.out.print(": " + e.getCause().getMessage());
      System.out.println();
      for (Diagnostic<? extends JavaFileObject> diagnostic : e.getDiagnostics().getDiagnostics()) {
        if (diagnostic != null) {
          if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
            String sourceName = diagnostic.getSource().toUri().toString();
            assertThat("name should be class name", sourceName, is(equalTo("SimplyBad.java")));
            assertThat(
                "line number",
                diagnostic.getLineNumber(),
                anyOf(is(equalTo(8L)), is(equalTo(12L))));
          } else {
            fail("compilation failure was not error, got " + diagnostic.getKind());
          }
        }
      }
    }
  }

  private String createUncompilableClass() {

    CompilationUnit cu = new CompilationUnit();
    ClassOrInterfaceDeclaration classDeclaration =
        new ClassOrInterfaceDeclaration(PUBLIC, false, "SimplyBad");

    MethodDeclaration method =
        new MethodDeclaration(
            Modifier.PUBLIC, new PrimitiveType(PrimitiveType.Primitive.Int), "zero");
    Statement statement = new ReturnStmt(new IntegerLiteralExpr("0"));
    BlockStmt body = new BlockStmt();
    List<BodyDeclaration> bodyDeclarations = new ArrayList<>();
    List<Statement> statements = new ArrayList<>();
    statements.add(statement);
    body.setStmts(statements);
    method.setBody(body);
    bodyDeclarations.add(method);

    method =
        new MethodDeclaration(
            Modifier.PUBLIC, new PrimitiveType(PrimitiveType.Primitive.Int), "one");
    List<VariableDeclarator> variableList = new ArrayList<>();
    variableList.add(
        new VariableDeclarator(new VariableDeclaratorId("i"), new StringLiteralExpr("one")));
    VariableDeclarationExpr expression =
        new VariableDeclarationExpr(new PrimitiveType(PrimitiveType.Primitive.Int), variableList);
    statement = new ExpressionStmt(expression);
    body = new BlockStmt();
    statements = new ArrayList<>();
    statements.add(statement);
    body.setStmts(statements);
    method.setBody(body);
    bodyDeclarations.add(method);

    method =
        new MethodDeclaration(
            Modifier.PUBLIC, new PrimitiveType(PrimitiveType.Primitive.Int), "two");
    statement = new ReturnStmt(new StringLiteralExpr("one"));
    body = new BlockStmt();
    statements = new ArrayList<>();
    statements.add(statement);
    body.setStmts(statements);
    method.setBody(body);

    bodyDeclarations.add(method);
    classDeclaration.setMembers(bodyDeclarations);
    List<TypeDeclaration> types = new ArrayList<>();
    types.add(classDeclaration);
    cu.setTypes(types);
    return cu.toString();
  }

  private SequenceCompiler getSequenceCompiler() {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    SequenceClassLoader classLoader = new SequenceClassLoader(getClass().getClassLoader());
    List<String> options = new ArrayList<>();
    options.add("-Xmaxerrs");
    options.add("1000");
    return new SequenceCompiler(classLoader, options, diagnostics);
  }

  private void printDiagnostics(
      PrintStream err, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    for (Diagnostic<? extends JavaFileObject> diag : diagnostics) {
      if (diag != null) {
        if (diag.getSource() != null) {
          String sourceName = diag.getSource().toUri().toString();
          if (diag.getLineNumber() >= 0) {
            err.printf(
                "Error on %d of %s%n%s%n", diag.getLineNumber(), sourceName, diag.getMessage(null));
          } else {
            err.printf("%s%n", diag.getMessage(null));
          }
        } else {
          err.printf("%s%n", diag.getMessage(null));
        }
      }
    }
  }
}
