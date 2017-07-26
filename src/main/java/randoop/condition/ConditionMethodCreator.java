package randoop.condition;

import java.lang.reflect.Method;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import randoop.Globals;
import randoop.compile.SequenceCompiler;
import randoop.compile.SequenceCompilerException;
import randoop.reflection.RawSignature;

/** Defines the factory method for creating condition methods. */
public class ConditionMethodCreator {

  /**
   * Creates the {@code java.lang.reflect.Method} to test the condition in the condition code.
   *
   * <p>Generates the Java source for a class with the method and then
   *
   * @param signature the signature string for the condition method
   * @param parameterDeclaration the parameter declaration string, includes parentheses
   * @param conditionSource the source code for the condition
   * @param compiler the compiler used to compile the condition class
   * @return the {@code Method} object for the condition method of the created class
   */
  public static Method create(
      RawSignature signature,
      String parameterDeclaration,
      String conditionSource,
      SequenceCompiler compiler) {

    String packageName = signature.getPackageName();
    String classname = signature.getClassname();
    String classText =
        createConditionClassSource(
            signature.getName(), conditionSource, parameterDeclaration, packageName, classname);
    System.out.println("compiling\n" + classText);
    Class<?> conditionClass;
    try {
      conditionClass =
          compiler.compile(signature.getPackageName(), signature.getClassname(), classText);
    } catch (SequenceCompilerException e) {
      String msg = getCompilerErrorMessage(e.getDiagnostics().getDiagnostics(), classText);
      throw new RandoopConditionError(msg, e);
    }

    try {
      return conditionClass.getDeclaredMethod(signature.getName(), signature.getParameterTypes());
    } catch (NoSuchMethodException e) {
      throw new RandoopConditionError("Failed to load condition method", e);
    }
  }

  /**
   * Gets the compilation error message for the condition class from the compiler diagnostics
   * object.
   *
   * @param diagnostics the compiler diagnostics object
   * @param classText the source code of the condition class
   * @return the compiler error message string
   */
  private static String getCompilerErrorMessage(
      List<Diagnostic<? extends JavaFileObject>> diagnostics, String classText) {
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
    msg.append(String.format("%nClass Declaration:%n%s", classText));
    return msg.toString();
  }

  /**
   * Create the source code for the condition class.
   *
   * @param conditionText the condition source code
   * @param parameterDeclarations the signature string for the condition method
   * @param packageName the package of the condition class
   * @param conditionClassName the name of the condition class
   * @return the Java source code for the condition class
   */
  private static String createConditionClassSource(
      String methodName,
      String conditionText,
      String parameterDeclarations,
      String packageName,
      String conditionClassName) {
    String classSource = "";
    if (packageName != null && !packageName.isEmpty()) {
      classSource = "package " + packageName + ";" + Globals.lineSep;
    }
    return classSource
        + Globals.lineSep
        + "public class "
        + conditionClassName
        + " {"
        + Globals.lineSep
        + "  public static boolean "
        + methodName
        + parameterDeclarations
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
