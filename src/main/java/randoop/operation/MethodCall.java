package randoop.operation;

import java.io.PrintStream;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.TypedOperationManager;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.GeneralType;
import randoop.types.GeneralTypeTuple;
import randoop.types.GenericTypeTuple;
import randoop.types.PrimitiveTypes;
import randoop.util.CollectionsExt;
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
   * @see OperationParser#getId(ConcreteOperation)
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
   * @param method  the reflective method object.
   */
  public MethodCall(Method method) {
    if (method == null) throw new IllegalArgumentException("method should not be null.");

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
   * @param inputVars is the list of actual arguments to be printed.
   */
  @Override
  public void appendCode(GeneralType declaringType, GeneralTypeTuple inputTypes, GeneralType outputType, List<Variable> inputVars, StringBuilder sb) {

    String receiverString = isStatic() ? null : inputVars.get(0).getName();
    appendReceiverOrClassForStatics(declaringType, inputTypes, receiverString, sb);

    sb.append(".");
    sb.append(getTypeArguments());
    sb.append(getMethod().getName()).append("(");

    int startIndex = (isStatic() ? 0 : 1);
    for (int i = startIndex; i < inputVars.size(); i++) {
      if (i > startIndex) sb.append(", ");

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

  // XXX this is a pretty bogus workaround for a bug in javac (type inference
  // fails sometimes)
  // It is bogus because what we produce here may be different from correct
  // inferred type.
  private String getTypeArguments() {
    TypeVariable<Method>[] typeParameters = method.getTypeParameters();
    if (typeParameters.length == 0) return "";
    StringBuilder b = new StringBuilder();
    Class<?>[] params = new Class<?>[typeParameters.length];
    b.append("<");
    for (int i = 0; i < typeParameters.length; i++) {
      if (i > 0) b.append(",");
      Type firstBound =
          typeParameters[i].getBounds().length == 0
              ? Object.class
              : typeParameters[i].getBounds()[0];
      params[i] = getErasure(firstBound);
      b.append(getErasure(firstBound).getCanonicalName());
    }
    b.append(">");
    // if all are object, then don't bother
    if (CollectionsExt.findAll(Arrays.asList(params), Object.class).size() == params.length)
      return "";
    return b.toString();
  }

  private static Class<?> getErasure(Type t) {
    if (t instanceof Class<?>) return (Class<?>) t;
    if (t instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) t;
      return getErasure(pt.getRawType());
    }
    if (t instanceof TypeVariable<?>) {
      TypeVariable<?> tv = (TypeVariable<?>) t;
      Type[] bounds = tv.getBounds();
      Type firstBound = bounds.length == 0 ? Object.class : bounds[0];
      return getErasure(firstBound);
    }
    if (t instanceof GenericArrayType)
      throw new UnsupportedOperationException("erasure of arrays not implemented " + t);
    if (t instanceof WildcardType)
      throw new UnsupportedOperationException("erasure of wildcards not implemented " + t);
    throw new IllegalStateException("unexpected type " + t);
  }

  private void appendReceiverOrClassForStatics(GeneralType declaringType, GeneralTypeTuple inputTypes, String receiverString, StringBuilder b) {
    if (isStatic()) {
      b.append(declaringType.getName().replace('$', '.'));
    } else {
      Class<?> expectedType = inputTypes.get(0).getRuntimeClass();
      String typeName = expectedType.getName();
      boolean mustCast =
          typeName != null
              && PrimitiveTypes.isBoxedPrimitiveTypeOrString(expectedType)
              && !expectedType.equals(String.class);
      if (mustCast) {
        // this is a little paranoid but we need to cast primitives in
        // order to get them boxed.
        b.append("((").append(typeName).append(")").append(receiverString).append(")");
      } else {
        b.append(receiverString);
      }
    }
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
   * @return true if this method is static, and false otherwise.
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
  public String toParseableString(GeneralType declaringType, GeneralTypeTuple inputTypes, GeneralType outputType) {
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
   * <code>parse(op.toParseableString()).equals(op)</code> for Operation op.
   *
   * @see OperationParser#parse(String, randoop.reflection.TypedOperationManager)
   *
   * @param signature  a string descriptor
   * @param manager  the {@link TypedOperationManager} for collecting operations
   * @throws OperationParseException
   *           if s does not match expected descriptor.
   */
  public static void parse(String signature, TypedOperationManager manager) throws OperationParseException {
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
    GeneralType classType;
    try {
      classType = GeneralType.forName(classname);
    } catch (ClassNotFoundException e) {
      String msg = "Class for method " + methodString + " not found: " + e;
      throw new OperationParseException(msg);
    }

    System.out.println("Looking for: " + opname + " args: " + arguments);

    Class<?>[] typeArguments = TypeArguments.getTypeArgumentsForString(arguments);
    Method m = null;
    try {
      m = classType.getRuntimeClass().getDeclaredMethod(opname, typeArguments);
    } catch (NoSuchMethodException e) {

    }
    if (m == null) {
      try {
        m = classType.getRuntimeClass().getMethod(opname, typeArguments);
      } catch (NoSuchMethodException e) {
        String msg = "Method " + methodString + " not found: " + e;
        throw new OperationParseException(msg);
      }
    }

    MethodCall op = new MethodCall(m);
    List<GeneralType> paramTypes = new ArrayList<>();
    if (!Modifier.isStatic(m.getModifiers() & Modifier.methodModifiers())) {
      paramTypes.add(classType);
    }
    for (Class<?> c : typeArguments) {
      paramTypes.add(manager.getClassType(c));
    }
    GeneralType outputType = GeneralType.forType(m.getGenericReturnType());
    manager.createTypedOperation(op, classType, new GenericTypeTuple(paramTypes), outputType);

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
  public boolean isMethodCall() { return true; }

  public String getName() {
    return method.getName();
  }

  /**
   * {@inheritDoc}
   * Determines whether enclosed {@link Method} satisfies the given predicate.
   *
   * @param predicate the {@link ReflectionPredicate} to be checked.
   * @return true only if the method in this object satisfies the canUse(Method) of predicate.
   */
  @Override
  public boolean satisfies(ReflectionPredicate predicate) {
    return predicate.test(method);
  }


}
