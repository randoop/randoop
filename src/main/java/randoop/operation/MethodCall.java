package randoop.operation;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.MethodReflectionCode;
import randoop.util.ReflectionExecutor;

/**
 * MethodCall is a {@link Operation} that represents a call to a method. It is
 * a wrapper for a reflective Method object, and caches values of computed
 * reflective calls.
 * <p>
 * An an {@link Operation}, a call to a non-static method<br>
 *   <code>T mname (T1,...,Tn)</code><br>
 * of class C can be represented formally as an operation
 * <i>mname</i>: [<i>C, T1,...,Tn</i>] &rarr; <i>T</i>.
 * If this method is static, then we could write the operation as
 * <i>C.mname</i>: [<i>T1,...,Tn</i>] &rarr; <i>T</i>
 * (a class instance not being needed as an input).
 * <p>
 * The execution of a {@code MethodCall} executes the enclosed {@link Method}
 * given values for the inputs.
 * <p>
 * (Class previously called RMethod.)
 */
public final class MethodCall extends CallableOperation {

  /**
   * ID for parsing purposes
   *
   * @see OperationParser#getId(TypedOperation)
   */
  public static final String ID = "method";

  private final Method method;
  private final boolean isStatic;

  /**
   * getMethod returns Method object of this MethodCall.
   *
   * @return {@link Method} object called by this {@link MethodCall}
   */
  public Method getMethod() {
    return this.method;
  }

  /**
   * MethodCall creates an object corresponding to the given reflective method.
   *
   * @param method  the reflective method object
   */
  public MethodCall(Method method) {
    if (method == null) {
      throw new IllegalArgumentException("method should not be null.");
    }

    this.method = method;
    this.method.setAccessible(true);
    this.isStatic = Modifier.isStatic(method.getModifiers() & Modifier.methodModifiers());
  }

  /**
   * toString outputs a text representation of the method call.
   *
   * @return string representation of the enclosed method
   */
  @Override
  public String toString() {
    return method.toString();
  }

  /**
   * {@inheritDoc}
   * Issues the code that corresponds to calling the method with the provided
   * {@link Variable} objects as arguments.
   * @param inputVars is the list of actual arguments to be printed
   */
  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder sb) {

    String receiverString = isStatic() ? null : inputVars.get(0).getName();
    if (isStatic()) {
      sb.append(declaringType.getName().replace('$', '.'));
    } else {
      Type expectedType = inputTypes.get(0);
      if (expectedType.isPrimitive()) { // explicit cast when want primitive boxed as receiver
        sb.append("((")
            .append(expectedType.getName())
            .append(")")
            .append(receiverString)
            .append(")");
      } else {
        sb.append(receiverString);
      }
    }

    sb.append(".");
    sb.append(getMethod().getName()).append("(");

    int startIndex = (isStatic() ? 0 : 1);
    for (int i = startIndex; i < inputVars.size(); i++) {
      if (i > startIndex) {
        sb.append(", ");
      }

      // CASTING.
      if (!inputVars.get(i).getType().equals(inputTypes.get(i))) {
        // Cast if the variable and input types are not identical.
        sb.append("(").append(inputTypes.get(i).getName()).append(")");
      }

      String param = inputVars.get(i).getName();

      // In the short output format, statements like "int x = 3" are not added
      // to a sequence; instead, the value (e.g. "3") is inserted directly added
      // as arguments to method calls.
      Statement statementCreatingVar = inputVars.get(i).getDeclaringStatement();

      String shortForm = statementCreatingVar.getShortForm();
      if (shortForm != null) {
        param = shortForm;
      }

      sb.append(param);
    }
    sb.append(")");
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MethodCall)) return false;
    if (this == o) return true;
    MethodCall other = (MethodCall) o;
    return this.method.equals(other.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(method);
  }

  /**
   * {@inheritDoc}
   * @return {@link NormalExecution} with return value if execution normal,
   *         otherwise {@link ExceptionalExecution} if an exception thrown.
   */
  @Override
  public ExecutionOutcome execute(Object[] input, PrintStream out) {

    Object receiver = null;
    int paramsLength = input.length;
    int paramsStartIndex = 0;
    if (!isStatic()) {
      receiver = input[0];
      paramsLength--;
      paramsStartIndex = 1;
    }

    Object[] params = new Object[paramsLength];
    for (int i = 0; i < params.length; i++) {
      params[i] = input[i + paramsStartIndex];
    }

    MethodReflectionCode code = new MethodReflectionCode(this.method, receiver, params);

    Throwable thrown = ReflectionExecutor.executeReflectionCode(code, out);

    if (thrown == null) {
      return new NormalExecution(code.getReturnVariable(), 0);
    } else {
      return new ExceptionalExecution(thrown, 0);
    }
  }

  /**
   * {@inheritDoc}
   * @return true if this method is static, and false otherwise
   */
  @Override
  public boolean isStatic() {
    return isStatic;
  }

  /**
   * {@inheritDoc}
   * The descriptor for a method is a string representing the method signature.
   *
   * Examples:
   *  java.util.ArrayList.get(int)
   *  java.util.ArrayList.add(int,java.lang.Object)
   */
  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    StringBuilder sb = new StringBuilder();
    sb.append(method.getDeclaringClass().getName()).append(".");
    sb.append(method.getName()).append("(");
    Class<?>[] params = method.getParameterTypes();
    TypeArguments.getTypeArgumentString(sb, params);
    sb.append(")");
    return sb.toString();
  }

  /**
   * Parses a method call in a string descriptor and returns a
   * {@link MethodCall} object. Should satisfy
   * <code>parse(op.toParsableString()).equals(op)</code> for Operation op.
   *
   * @see OperationParser#parse(String)
   *
   * @param signature  a string descriptor
   * @return the method call operation for the given string descriptor
   * @throws OperationParseException
   *           if s does not match expected descriptor.
   */
  public static TypedClassOperation parse(String signature) throws OperationParseException {
    if (signature == null) {
      throw new IllegalArgumentException("signature may not be null");
    }

    int openParPos = signature.indexOf('(');
    int closeParPos = signature.indexOf(')');

    String prefix = signature.substring(0, openParPos);
    int lastDotPos = prefix.lastIndexOf('.');

    assert lastDotPos >= 0;
    String classname = prefix.substring(0, lastDotPos);
    String opname = prefix.substring(lastDotPos + 1);
    String arguments = signature.substring(openParPos + 1, closeParPos);

    String methodString = classname + "." + opname + arguments;
    Type classType;
    try {
      classType = Type.forName(classname);
    } catch (ClassNotFoundException e) {
      String msg = "Class for method " + methodString + " not found: " + e;
      throw new OperationParseException(msg);
    }

    System.out.println("Looking for: " + opname + " args: " + arguments);

    Class<?>[] typeArguments = TypeArguments.getTypeArgumentsForString(arguments);
    Method m = null;
    String msg = "Method " + methodString + " not found: ";
    try {
      m = classType.getRuntimeClass().getDeclaredMethod(opname, typeArguments);
    } catch (NoSuchMethodException e) {
      msg += e;
    }
    if (m == null) {
      try {
        m = classType.getRuntimeClass().getMethod(opname, typeArguments);
      } catch (NoSuchMethodException e) {
        throw new OperationParseException(msg);
      }
    }

    return TypedClassOperation.forMethod(m);
  }

  /**
   * {@inheritDoc}
   * @return true always, since this is a method call
   */
  @Override
  public boolean isMessage() {
    return true;
  }

  /**
   * {@inheritDoc}
   * @return true always, since this is a method call
   */
  @Override
  public boolean isMethodCall() {
    return true;
  }

  public String getName() {
    return method.getName();
  }

  /**
   * {@inheritDoc}
   * Determines whether enclosed {@link Method} satisfies the given predicate.
   *
   * @param predicate the {@link ReflectionPredicate} to be checked.
   * @return true only if the method in this object satisfies the canUse(Method) of predicate
   */
  @Override
  public boolean satisfies(ReflectionPredicate predicate) {
    return predicate.test(method);
  }
}
