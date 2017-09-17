package randoop.condition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import plume.UtilMDE;
import randoop.BugInRandoopException;
import randoop.Globals;
import randoop.compile.SequenceCompiler;
import randoop.compile.SequenceCompilerException;
import randoop.contract.ObjectContract;
import randoop.output.NameGenerator;
import randoop.reflection.RawSignature;
import randoop.util.Log;

/**
 * A {@code BooleanExpression} is an expression constructed from the Java source for a Boolean
 * expression, and allows evaluation on argument values.
 *
 * <p>Corresponds to {@link randoop.condition.specification.Guard} in {@link
 * randoop.condition.specification.Precondition}, {@link
 * randoop.condition.specification.Postcondition} or {@link
 * randoop.condition.specification.ThrowsCondition}; and to {@link
 * randoop.condition.specification.Property} in {@link
 * randoop.condition.specification.Postcondition}.
 *
 * @see SpecificationTranslator
 */
public class BooleanExpression {

  /** The basename for the expression class name. It is used for compiling the method. */
  private static final String EXPRESSION_CLASS_NAME = "RandoopExpressionClass";

  /** The name generator to use to generate class names. */
  private static final NameGenerator nameGenerator = new NameGenerator(EXPRESSION_CLASS_NAME);

  /** The {@code java.lang.reflect.Method} to test this expression */
  private final Method expressionMethod;

  /** The comment describing this expression */
  private final String comment;

  /**
   * The Java source code in {@link ObjectContract} format. See {@link #getContractSource()} for
   * format details.
   */
  private final String contractSource;

  /**
   * Creates a {@link BooleanExpression} that calls the method to evaluate the expression.
   *
   * @param expressionMethod the reflection {@code Method} for the expression
   * @param comment a comment describing this expression
   * @param contractSource the source code for this expression (see {@link #getContractSource()} for
   *     format details)
   */
  BooleanExpression(Method expressionMethod, String comment, String contractSource) {
    this.expressionMethod = expressionMethod;
    this.comment = comment;
    this.contractSource = contractSource;
  }

  /**
   * Creates a {@link BooleanExpression} for evaluating an expression (see {@link
   * randoop.condition.specification.Guard}) of a specification.
   *
   * @param signature the signature for the expression method to be created. The class name of the
   *     expression method signature is ignored and a new name is generated using {@link
   *     #nameGenerator}.
   * @param declarations the parameter declaration string for the expression method to be created,
   *     including parameter names and wrapped in parentheses
   * @param expressionSource the source code for a Java expression to be used as the body of the
   *     expression method
   * @param contractSource a Java expression that is the source code for the expression, in the
   *     format of {@link BooleanExpression#getContractSource()}
   * @param comment the comment describing the expression
   * @param compiler the compiler to used to compile the expression method
   * @return the {@link BooleanExpression} that evaluates the given expression source on parameters
   *     described by the declaration string
   */
  static BooleanExpression createBooleanExpression(
      RawSignature signature,
      String declarations,
      String expressionSource,
      String contractSource,
      String comment,
      SequenceCompiler compiler) {
    Method expressionMethod = createMethod(signature, declarations, expressionSource, compiler);
    return new BooleanExpression(expressionMethod, comment, contractSource);
  }

  /**
   * Returns the {@link BooleanExpression} that checks the expression with the given argument values
   * as the pre-state.
   *
   * <p>Since pre-state is not yet implemented, this method just returns this object.
   *
   * @param args the pre-state values to the arguments
   * @return the {@link BooleanExpression} with the pre-state set
   */
  BooleanExpression addPrestate(Object[] args) {
    return this;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof BooleanExpression)) {
      return false;
    }
    BooleanExpression other = (BooleanExpression) object;
    return this.expressionMethod.equals(other.expressionMethod)
        && this.comment.equals(other.comment)
        && this.contractSource.equals(other.contractSource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(expressionMethod, comment, contractSource);
  }

  @Override
  public String toString() {
    return contractSource + " // " + comment;
  }

  /**
   * Indicate whether this expression is satisfied by the given values.
   *
   * @param values the values to check the expression against
   * @return true if this expression is satisfied by the values, false otherwise
   */
  public boolean check(Object[] values) {
    try {
      return (boolean) expressionMethod.invoke(null, values);
    } catch (IllegalAccessException e) {
      throw new RandoopConditionError("Failure executing expression method", e);
    } catch (InvocationTargetException e) {
      String message =
          "Failure executing expression method: "
              + expressionMethod
              + " (invoke threw "
              + e.getCause()
              + "). This indicates a bug in the expression method creation.";
      // TODO: throwing seems like better behavior than logging, but it breaks the tests.  Need to
      // investigate.
      // throw new RandoopConditionError(message);
      if (Log.isLoggingOn()) {
        Log.logLine(message);
      }
    }
    return false;
  }

  /**
   * Return the code comment for this expression.
   *
   * @return the code comment for this expression
   */
  public String getComment() {
    return comment;
  }

  /**
   * Return the Java source code for this expression. Arguments to the expression follow the {@link
   * randoop.contract.ObjectContract} convention where parameters (including the receiver, if any)
   * are represented by {@code x0}, ..., {@code xn} for some number {@code n}. If the operation has
   * a return value it will be {@code xn} (the last, extra variable).
   *
   * @return the Java representation of the expression as a {@code String}
   */
  public String getContractSource() {
    return contractSource;
  }

  /**
   * Creates a {@code java.lang.reflect.Method} to test the expression given by {@code
   * contractSource}.
   *
   * <p>Generates the Java source for a class with the method, compiles the class, and returns the
   * expression method.
   *
   * @param signature the signature for the expression method. The class name of the expression
   *     method signature is ignored and a new name is generated using {@link #nameGenerator}.
   * @param parameterDeclaration the parameter declaration string, including parameter names and
   *     wrapped in parentheses
   * @param expressionSource a Java expression that is the source code for the expression, in the
   *     format of {@link BooleanExpression#getContractSource()}.
   * @param compiler the compiler to use to compile the expression class
   * @return the {@code Method} object for {@code contractSource}
   */
  static Method createMethod(
      RawSignature signature,
      String parameterDeclaration,
      String expressionSource,
      SequenceCompiler compiler) {
    String packageName = signature.getPackageName();
    String classname = nameGenerator.next(); // ignore the class name in the signature
    String classText =
        createConditionClassSource(
            signature.getName(), expressionSource, parameterDeclaration, packageName, classname);

    try {
      compiler.compile(packageName, classname, classText);
    } catch (SequenceCompilerException e) {
      String msg = getCompilerErrorMessage(e.getDiagnostics().getDiagnostics(), classText);
      throw new RandoopConditionError(msg, e);
    }

    Class<?> expressionClass;
    try {
      expressionClass = compiler.loadClass(packageName, classname);
    } catch (ClassNotFoundException e) {
      throw new BugInRandoopException("Failed to load expression class", e);
    }

    try {
      return expressionClass.getDeclaredMethod(signature.getName(), signature.getParameterTypes());
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("Condition class does not contain expression method", e);
    }
  }

  /**
   * Create the source code for the expression class.
   *
   * @param methodName the name of the expression method
   * @param expressionText the expression source code -- a boolean Java expression
   * @param parameterDeclarations the signature string for the expression method
   * @param packageName the package of the expression class, or null for the default package
   * @param expressionClassName the name of the expression class
   * @return the Java source code for the expression class
   */
  private static String createConditionClassSource(
      String methodName,
      String expressionText,
      String parameterDeclarations,
      String packageName,
      String expressionClassName) {
    String packageDeclaration = "";
    if (packageName != null && !packageName.isEmpty()) {
      packageDeclaration = "package " + packageName + ";" + Globals.lineSep + Globals.lineSep;
    }
    return UtilMDE.join(
        new String[] {
          packageDeclaration + "public class " + expressionClassName + " {",
          "  public static boolean " + methodName + parameterDeclarations + " throws Throwable {",
          "    return " + expressionText + ";",
          "  }",
          "}" + Globals.lineSep
        },
        Globals.lineSep);
  }

  /**
   * Gets the compilation error message for the expression class from the compiler diagnostics
   * object.
   *
   * @param diagnostics the compiler diagnostics object
   * @param classText the source code of the expression class
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
                  "expression threw exception %s",
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
   * Creates a {@link RawSignature} for the expression method of the {@link BooleanExpression}.
   *
   * <p>Note that these signatures may be used more than once for different expression methods, and
   * so {@link #createMethod(RawSignature, String, String, SequenceCompiler)} replaces the classname
   * to ensure a unique name.
   *
   * @param packageName the package name for the expression class
   * @param receiverType the declaring class of the method or constructor, included first in
   *     parameter types if non-null
   * @param parameterTypes the parameter types for the original method or constructor
   * @param returnType the return type for the method, or the declaring class for a constructor,
   *     included last in parameter types if non-null
   * @return the constructed post-expression method signature
   */
  static RawSignature getRawSignature(
      String packageName, Class<?> receiverType, Class<?>[] parameterTypes, Class<?> returnType) {
    final int shift = (receiverType != null) ? 1 : 0;
    final int length = parameterTypes.length + shift + (returnType != null ? 1 : 0);
    Class<?>[] expressionParameterTypes = new Class<?>[length];
    if (receiverType != null) {
      expressionParameterTypes[0] = receiverType;
    }
    System.arraycopy(parameterTypes, 0, expressionParameterTypes, shift, parameterTypes.length);
    if (returnType != null) {
      expressionParameterTypes[expressionParameterTypes.length - 1] = returnType;
    }
    return new RawSignature(
        packageName, "ClassNameIsIrrelevant", "MethodNameIsIrrelevant", expressionParameterTypes);
  }
}
