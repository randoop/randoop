package randoop.compile;

import org.junit.Test;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import randoop.output.ClassSourceBuilder;
import randoop.output.MethodSourceBuilder;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

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
    ClassSourceBuilder classBuilder = new ClassSourceBuilder("Simple", "");
    MethodSourceBuilder methodBuilder =
        new MethodSourceBuilder(
            "public", "int", "zero", new ArrayList<String>(), new ArrayList<String>());
    methodBuilder.addBodyText("return 0;");
    classBuilder.addMember(methodBuilder.toString());
    return classBuilder.toString();
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
                anyOf(is(equalTo(7L)), is(equalTo(11L))));
          } else {
            fail("compilation failure was not error, got " + diagnostic.getKind());
          }
        }
      }
    }
  }

  private String createUncompilableClass() {
    ClassSourceBuilder classBuilder = new ClassSourceBuilder("SimplyBad", "");
    MethodSourceBuilder methodBuilder;
    methodBuilder =
        new MethodSourceBuilder(
            "public", "int", "zero", new ArrayList<String>(), new ArrayList<String>());
    methodBuilder.addBodyText("return 0;");
    classBuilder.addMember(methodBuilder.toString());
    methodBuilder =
        new MethodSourceBuilder(
            "public", "int", "one", new ArrayList<String>(), new ArrayList<String>());
    methodBuilder.addBodyText("int i = \"one\";");
    classBuilder.addMember(methodBuilder.toString());
    methodBuilder =
        new MethodSourceBuilder(
            "public", "int", "two", new ArrayList<String>(), new ArrayList<String>());
    methodBuilder.addBodyText("return \"two\";");
    classBuilder.addMember(methodBuilder.toString());
    return classBuilder.toString();
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
