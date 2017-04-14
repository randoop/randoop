package randoop.condition;

import java.lang.reflect.Method;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import randoop.Globals;
import randoop.compile.SequenceCompiler;
import randoop.compile.SequenceCompilerException;
import randoop.output.NameGenerator;

/** Factory for condition methods. */
public class ConditionMethodCreator {
  private static final String CONDITION_METHOD_NAME = "test";
  private static final NameGenerator nameGenerator = new NameGenerator("RandoopConditionClass");

  public static Method create(
      String packageName, String signature, String conditionText, SequenceCompiler compiler) {
    String conditionClassName = nameGenerator.next();
    String classText = createClass(conditionText, signature, packageName, conditionClassName);
    Class<?> conditionClass;
    try {
      conditionClass = compiler.compile(packageName, conditionClassName, classText);
    } catch (SequenceCompilerException e) {
      String msg = getMessage(e.getDiagnostics().getDiagnostics());
      throw new RandoopConditionError(msg, e);
    }
    Method[] methods = conditionClass.getDeclaredMethods();
    for (Method method : methods) {
      if (method.getName().equals(CONDITION_METHOD_NAME)) {
        return method;
      }
    }
    assert false : "didn't manage to create condition method";
    return null;
  }

  private static String getMessage(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    StringBuilder msg = new StringBuilder("Condition method did not compile: ");
    for (Diagnostic<? extends JavaFileObject> diag : diagnostics) {
      if (diag != null) {
        String diagMessage = diag.getMessage(null);
        if (diagMessage.contains("unreported exception")) {
          msg.append(
              String.format(
                  "condition threw exception %s",
                  diagMessage.substring(0, diagMessage.indexOf(';'))));
        } else {
          msg.append(diagMessage);
        }
      }
    }
    return msg.toString();
  }

  private static String createClass(
      String conditionText, String signature, String packageName, String conditionClassName) {
    String classText = "";
    if (packageName != null && !packageName.isEmpty()) {
      classText = "package " + packageName + ";" + Globals.lineSep;
    }
    return classText
        + Globals.lineSep
        + "public class "
        + conditionClassName
        + " {"
        + Globals.lineSep
        + "  public static boolean "
        + CONDITION_METHOD_NAME
        + signature
        + " throws Throwable {"
        + Globals.lineSep
        + "    return "
        + conditionText
        + ";"
        + Globals.lineSep
        + "  }"
        + Globals.lineSep
        + "}"
        + Globals.lineSep;
  }
}
