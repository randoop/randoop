package randoop.condition;

import java.lang.reflect.Method;
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
      throw new RandoopConditionError("Condition method did not compile", e);
    }
    Method conditionMethod;
    Method[] methods = conditionClass.getDeclaredMethods();
    assert methods.length == 1
        : "should only be one method in condition class, found " + methods.length;
    return methods[0];
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
        + " {"
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
