package randoop.compile;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
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
import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.junit.Test;

public class SequenceCompilerTest {

  /** The "public" and "static" modifiers. */
  private final NodeList<Modifier> PUBLIC = new NodeList<>(Modifier.publicModifier());

  @Test
  public void compilableTest() throws ClassNotFoundException {

    SequenceCompiler compiler = getSequenceCompiler();

    String simpleClass = createCompilableClass();

    Class<?> compiledClass = null;
    try {
      compiledClass = compiler.compileAndLoad(null, "Simple", simpleClass);
    } catch (SequenceCompilerException e) {
      System.out.print(e.getMessage());
      if (e.getCause() != null) System.out.print(": " + e.getCause().getMessage());
      System.out.println();
      printDiagnostics(System.out, e.getDiagnostics().getDiagnostics());
      throw new AssertionError("compilation failed", e);
    }

    try {
      Constructor<?> simpleCons = compiledClass.getConstructor();
      Object object = simpleCons.newInstance();
      Method zeroMethod = compiledClass.getMethod("zero");
      Object value = zeroMethod.invoke(object);
      assertEquals(0, (int) (Integer) value);
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
    CompilationUnit compilationUnit = new CompilationUnit();
    ClassOrInterfaceDeclaration classDeclaration =
        new ClassOrInterfaceDeclaration(PUBLIC, false, "Simple");
    MethodDeclaration method =
        new MethodDeclaration(PUBLIC, new PrimitiveType(PrimitiveType.Primitive.INT), "zero");
    ReturnStmt statement = new ReturnStmt(new IntegerLiteralExpr("0"));
    BlockStmt body = new BlockStmt();
    NodeList<Statement> statements = new NodeList<>(statement);
    body.setStatements(statements);
    method.setBody(body);

    NodeList<BodyDeclaration<?>> bodyDeclarations = new NodeList<>(method);
    classDeclaration.setMembers(bodyDeclarations);
    NodeList<TypeDeclaration<?>> types = new NodeList<>(classDeclaration);
    compilationUnit.setTypes(types);
    return compilationUnit.toString();
  }

  @Test
  public void uncompilableTest() {
    SequenceCompiler compiler = getSequenceCompiler();
    String classSource = createUncompilableClass();
    try {
      compiler.compileAndLoad(null, "SimplyBad", classSource);
      fail("should not compile");
    } catch (SequenceCompilerException e) {
      if (e.getCause() != null) System.out.print(": " + e.getCause().getMessage());
      System.out.println();
      for (Diagnostic<? extends JavaFileObject> diagnostic : e.getDiagnostics().getDiagnostics()) {
        if (diagnostic != null) {
          if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
            String sourceName = diagnostic.getSource().toUri().toString();
            assertEquals("SimplyBad.java", sourceName);
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

    CompilationUnit compilationUnit = new CompilationUnit();
    ClassOrInterfaceDeclaration classDeclaration =
        new ClassOrInterfaceDeclaration(PUBLIC, false, "SimplyBad");

    MethodDeclaration method =
        new MethodDeclaration(PUBLIC, new PrimitiveType(PrimitiveType.Primitive.INT), "zero");
    Statement statement = new ReturnStmt(new IntegerLiteralExpr("0"));
    BlockStmt body = new BlockStmt();
    NodeList<BodyDeclaration<?>> bodyDeclarations = new NodeList<>();
    NodeList<Statement> statements = new NodeList<>(statement);
    body.setStatements(statements);
    method.setBody(body);
    bodyDeclarations.add(method);

    method = new MethodDeclaration(PUBLIC, new PrimitiveType(PrimitiveType.Primitive.INT), "one");
    NodeList<VariableDeclarator> variableList =
        new NodeList<>(
            new VariableDeclarator(
                new PrimitiveType(PrimitiveType.Primitive.INT), "i", new StringLiteralExpr("one")));
    VariableDeclarationExpr expression = new VariableDeclarationExpr(variableList);
    statement = new ExpressionStmt(expression);
    body = new BlockStmt();
    statements = new NodeList<>(statement);
    body.setStatements(statements);
    method.setBody(body);
    bodyDeclarations.add(method);

    method = new MethodDeclaration(PUBLIC, new PrimitiveType(PrimitiveType.Primitive.INT), "two");
    statement = new ReturnStmt(new StringLiteralExpr("one"));
    body = new BlockStmt();
    statements = new NodeList<>(statement);
    body.setStatements(statements);
    method.setBody(body);

    bodyDeclarations.add(method);
    classDeclaration.setMembers(bodyDeclarations);
    NodeList<TypeDeclaration<?>> types = new NodeList<>(classDeclaration);
    compilationUnit.setTypes(types);
    return compilationUnit.toString();
  }

  private SequenceCompiler getSequenceCompiler() {
    List<String> compilerOptions = new ArrayList<>();
    // These are javac compilerOptions
    compilerOptions.add("-Xmaxerrs");
    compilerOptions.add("1000");
    return new SequenceCompiler(compilerOptions);
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
