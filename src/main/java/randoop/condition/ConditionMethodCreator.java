package randoop.condition;

import java.lang.reflect.Method;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import plume.UtilMDE;
import randoop.BugInRandoopException;
import randoop.Globals;
import randoop.compile.SequenceCompiler;
import randoop.compile.SequenceCompilerException;
import randoop.output.NameGenerator;
import randoop.reflection.RawSignature;

/** Defines the factory method {@link #create} for creating condition methods. */
public class ConditionMethodCreator {

  /** The basename for the condition class name. It is used for compiling the method. */
  private static final String CONDITION_CLASS_BASENAME = "RandoopConditionClass";

  /** The name generator to use to generate class names. */
  private static final NameGenerator nameGenerator = new NameGenerator(CONDITION_CLASS_BASENAME);

  /**
   * Creates the {@code java.lang.reflect.Method} to test the condition in the condition code.
   *
   * <p>Generates the Java source for a class with the method, compiles the class, and returns the
   * condition method.
   *
   * @param signature the signature for the condition method. The class name of the condition method
   *     signature is ignored and a new name is generated using {@link #nameGenerator}.
   * @param parameterDeclaration the parameter declaration string, including parameter names and
   *     wrapped in parentheses
   * @param conditionSource a Java expression that is the source code for the condition
   * @param compiler the compiler to use to compile the condition class
   * @return the {@code Method} object for the condition method of the created class
   */
  public static Method create(
      RawSignature signature,
      String parameterDeclaration,
      String conditionSource,
      SequenceCompiler compiler) {
    String packageName = signature.getPackageName();
    String classname = nameGenerator.next(); // ignore the class name in the signature
    String classText =
        createConditionClassSource(
            signature.getName(), conditionSource, parameterDeclaration, packageName, classname);

    try {
      compiler.compile(packageName, classname, classText);
    } catch (SequenceCompilerException e) {
      String msg = getCompilerErrorMessage(e.getDiagnostics().getDiagnostics(), classText);
      throw new RandoopConditionError(msg, e);
    }

    Class<?> conditionClass;
    try {
      conditionClass = compiler.loadClass(packageName, classname);
    } catch (ClassNotFoundException e) {
      throw new BugInRandoopException("Failed to load condition class", e);
    }

    try {
      return conditionClass.getDeclaredMethod(signature.getName(), signature.getParameterTypes());
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("Failed to load condition method", e);
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
   * @param methodName the name of the condition method
   * @param conditionText the condition source code -- a boolean Java expression
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
    String packageDeclaration = "";
    if (packageName != null && !packageName.isEmpty()) {
      packageDeclaration = "package " + packageName + ";" + Globals.lineSep + Globals.lineSep;
    }
    return UtilMDE.join(
        new String[] {
          packageDeclaration + "public class " + conditionClassName + " {",
          "  public static boolean " + methodName + parameterDeclarations + " throws Throwable {",
          "    return " + conditionText + ";",
          "  }",
          "}" + Globals.lineSep
        },
        Globals.lineSep);
  }

  /**
   * Creates the {@link RawSignature} for the precondition method.
   *
   * <p>if {@code insertReceiver} is true, the parameter types for the precondition method have the
   * receiver type first, followed by the parameter types. Otherwise, the condition method parameter
   * types are as given.
   *
   * <p>Note that these signatures may be used more than once for different condition methods, and
   * so {@link #create(RawSignature, String, String, SequenceCompiler)} replaces the classname to
   * ensure a unique name.
   *
   * @param packageName the package name for the condition class
   * @param receiverType the declaring class of the method or constructor, used as receiver type if
   *     {@code insertRetriever} is true
   * @param parameterTypes the parameter types for the original method
   * @param insertRetriever whether to insert the {@code receiverType} before the {@code
   *     parameterTypes} in the condition method parameter list
   * @return the constructed pre-condition method signature
   */
  static RawSignature getPreconditionSignature(
      String packageName,
      Class<?> receiverType,
      Class<?>[] parameterTypes,
      boolean insertRetriever) {
    int shift = (insertRetriever) ? 1 : 0;
    Class<?>[] conditionParameterTypes = new Class<?>[parameterTypes.length + shift];
    if (shift == 1) {
      conditionParameterTypes[0] = receiverType;
    }
    System.arraycopy(parameterTypes, 0, conditionParameterTypes, shift, parameterTypes.length);
    return new RawSignature(
        packageName, CONDITION_CLASS_BASENAME, "ClassNameWillBeReplaced", conditionParameterTypes);
  }

  /**
   * Creates the {@link RawSignature} for the post-condition method.
   *
   * <p>if {@code insertRetriever} is true, the parameter types for the post-condition method have
   * the receiver type first, followed by the parameter types. Otherwise, the condition method
   * parameter types are as given.
   *
   * <p>Note that these signatures may be used more than once for different condition methods, and
   * so {@link #create(RawSignature, String, String, SequenceCompiler)} replaces the classname to
   * ensure a unique name.
   *
   * @param packageName the package name for the condition class
   * @param receiverType the declaring class of the method or constructor, used as receiver type if
   *     {@code insertRetriever} is true
   * @param parameterTypes the parameter types for the original method or constructor
   * @param returnType the return type for the method, or the declaring class for a constructor
   * @param insertRetriever whether to insert the {@code receiverType} before the {@code
   *     parameterTypes} in the condition method parameter list
   * @return the constructed post-condition method signature
   */
  static RawSignature getPostconditionSignature(
      String packageName,
      Class<?> receiverType,
      Class<?>[] parameterTypes,
      Class<?> returnType,
      boolean insertRetriever) {
    int shift = (insertRetriever) ? 1 : 0;
    Class<?>[] conditionParameterTypes = new Class<?>[parameterTypes.length + shift + 1];
    if (shift == 1) {
      conditionParameterTypes[0] = receiverType;
    }
    conditionParameterTypes[conditionParameterTypes.length - 1] = returnType;
    System.arraycopy(parameterTypes, 0, conditionParameterTypes, shift, parameterTypes.length);
    return new RawSignature(
        packageName, CONDITION_CLASS_BASENAME, "ClassNameWillBeReplaced", conditionParameterTypes);
  }
}
