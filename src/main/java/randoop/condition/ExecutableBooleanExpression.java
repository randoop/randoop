package randoop.condition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.plumelib.util.UtilPlume;
import randoop.Globals;
import randoop.compile.SequenceCompiler;
import randoop.compile.SequenceCompilerException;
import randoop.contract.ObjectContract;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.output.NameGenerator;
import randoop.reflection.RawSignature;

/**
 * A {@code ExecutableBooleanExpression} represents a boolean Java expression, and allows evaluation
 * on argument values.
 *
 * <p>This is the executable form of {@link
 * randoop.condition.specification.AbstractBooleanExpression}.
 *
 * @see SpecificationTranslator
 */
public class ExecutableBooleanExpression {

  /** The name generator to use to generate class names. */
  private static final NameGenerator classNameGenerator =
      new NameGenerator("RandoopExpressionClass");

  /**
   * The {@code java.lang.reflect.Method} to test this expression. The method is static (it does not
   * take a receiver argument).
   */
  private final Method expressionMethod;

  /** The comment describing this expression. */
  private final String comment;

  /**
   * The Java source code in {@link ObjectContract} format. See {@link #getContractSource()} for
   * format details.
   */
  private final String contractSource;

  /**
   * Creates a {@link ExecutableBooleanExpression} that calls the method to evaluate the expression.
   *
   * @param expressionMethod the reflection {@code Method} for the expression
   * @param comment a comment describing this expression
   * @param contractSource the source code for this expression (see {@link #getContractSource()} for
   *     format details)
   */
  ExecutableBooleanExpression(Method expressionMethod, String comment, String contractSource) {
    this.expressionMethod = expressionMethod;
    this.comment = comment;
    this.contractSource = contractSource;
  }

  /**
   * Creates a {@link ExecutableBooleanExpression} for evaluating an expression (see {@link
   * randoop.condition.specification.Guard}) of a specification.
   *
   * @param signature the signature for the expression method to be created. The class name of the
   *     expression method signature is ignored and a new name is generated using {@link
   *     #classNameGenerator}.
   * @param declarations the parameter declaration string for the expression method to be created,
   *     including parameter names and wrapped in parentheses
   * @param expressionSource the source code for a Java expression to be used as the body of the
   *     expression method
   * @param contractSource a Java expression that is the source code for the expression, in the
   *     format of {@link #getContractSource()}. The same as {@code expressionSource}, except that
   *     it uses dummy variable names x0, x1, instead of formal parameter names.
   * @param comment the comment describing the expression
   * @param compiler the compiler to used to compile the expression method
   */
  ExecutableBooleanExpression(
      RawSignature signature,
      String declarations,
      String expressionSource,
      String contractSource,
      String comment,
      SequenceCompiler compiler) {
    this(
        createMethod(signature, declarations, expressionSource, compiler), comment, contractSource);
  }

  /**
   * Returns the {@link ExecutableBooleanExpression} that checks the expression with the given
   * argument values as the pre-state.
   *
   * <p>Since pre-state is not yet implemented, this method just returns this object.
   *
   * @param args the pre-state values to the arguments
   * @return the {@link ExecutableBooleanExpression} with the pre-state set
   */
  ExecutableBooleanExpression addPrestate(Object[] args) {
    return this;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ExecutableBooleanExpression)) {
      return false;
    }
    ExecutableBooleanExpression other = (ExecutableBooleanExpression) object;
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
    if (comment == null || comment.isEmpty()) {
      return String.format(
          "ExecutableBooleanExpression{contractSource=%s, expressionMethod=%s}",
          contractSource, expressionMethod);
    } else {
      return String.format(
          "ExecutableBooleanExpression{contractSource=%s, comment=%s, expressionMethod=%s}",
          contractSource, comment, expressionMethod);
    }
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
      throw new RandoopSpecificationError("Failure executing expression method", e);
    } catch (InvocationTargetException e) {
      // Evaluation of the expression threw an exception.
      String messageDetails =
          String.format(
              "  contractSource = %s%n  comment = %s%n  cause = %s",
              contractSource, comment, e.getCause());
      if (GenInputsAbstract.ignore_condition_exception) {
        System.out.printf(
            "Failure executing expression method; fix the specification.%n" + messageDetails);
        return false;
      } else {
        throw new RandoopSpecificationError(
            String.format(
                    "Failure executing expression method.%n"
                        + "Fix the specification or pass --ignore-condition-exception=true .%n")
                + messageDetails);
      }
    }
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
   *     method signature is ignored and a new name is generated using {@link #classNameGenerator}.
   * @param parameterDeclaration the parameter declaration string, including parameter names and
   *     wrapped in parentheses
   * @param expressionSource a Java expression that is the source code for the expression, in the
   *     format of {@link ExecutableBooleanExpression#getContractSource()}.
   * @param compiler the compiler to use to compile the expression class
   * @return the {@code Method} object for {@code contractSource}
   */
  // package-private to enable test code to call it
  static Method createMethod(
      RawSignature signature,
      String parameterDeclaration,
      String expressionSource,
      SequenceCompiler compiler) {
    String packageName = signature.getPackageName();
    String classname = classNameGenerator.next(); // ignore the class name in the signature
    String classText =
        createConditionClassSource(
            signature.getName(), expressionSource, parameterDeclaration, packageName, classname);

    try {
      compiler.compile(packageName, classname, classText);
    } catch (SequenceCompilerException e) {
      String msg = getCompilerErrorMessage(e.getDiagnostics().getDiagnostics(), classText);
      throw new RandoopSpecificationError(msg, e);
    }

    Class<?> expressionClass;
    try {
      expressionClass = compiler.loadClass(packageName, classname);
    } catch (ClassNotFoundException e) {
      throw new RandoopBug("Failed to load expression class", e);
    }

    try {
      return expressionClass.getDeclaredMethod(signature.getName(), signature.getParameterTypes());
    } catch (NoSuchMethodException e) {
      throw new RandoopBug("Condition class does not contain expression method", e);
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
    if (packageName != null) {
      packageDeclaration = "package " + packageName + ";" + Globals.lineSep + Globals.lineSep;
    }
    return UtilPlume.join(
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
    StringBuilder msg = new StringBuilder("Condition method did not compile:");
    msg.append(Globals.lineSep);
    for (Diagnostic<? extends JavaFileObject> diag : diagnostics) {
      if (diag != null) {
        String diagMessage = diag.getMessage(null);
        if (diagMessage.contains("unreported exception")) {
          diagMessage =
              String.format(
                  "expression threw exception %s",
                  diagMessage.substring(0, diagMessage.indexOf(';')));
        }
        msg.append(
            String.format(
                "%d:%d: %s%n", diag.getLineNumber(), diag.getColumnNumber(), diagMessage));
      }
    }
    msg.append(String.format("%nClass Declaration:%n%s", classText));
    return msg.toString();
  }
}
