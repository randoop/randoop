package randoop.operation;

import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
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

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.util.CollectionsExt;
import randoop.util.MethodReflectionCode;
import randoop.util.PrimitiveTypes;
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
public final class MethodCall extends AbstractOperation implements Operation, Serializable {

  private static final long serialVersionUID = -7616184807726929835L;

  /** 
   * ID for parsing purposes
   * @see OperationParser#getId(Operation)
   */
  public static final String ID = "method";

  private final Method method;

  // Cached values (for improved performance). Their values
  // are computed upon the first invocation of the respective
  // getter method.
  private List<Class<?>> inputTypesCached;
  private Class<?> outputTypeCached;
  private boolean hashCodeComputed = false;
  private int hashCodeCached = 0;
  private boolean isVoidComputed = false;
  private boolean isVoidCached = false;
  private boolean isStaticComputed = false;
  private boolean isStaticCached = false;

  /**
   * Converts this object to a form that can be serialized.
   * 
   * @return serializable form of this object
   * @see SerializableMethodCall
   */
  private Object writeReplace() throws ObjectStreamException {
    return new SerializableMethodCall(this.method);
  }

  /**
   * getMethod returns Method object of this MethodCall.
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
    if (method == null)
      throw new IllegalArgumentException("method should not be null.");

    this.method = method;
    // TODO move this earlier in the process: check first that all
    // methods to be used can be made accessible.
    // XXX this should not be here but I get infinite loop when comment out
    this.method.setAccessible(true);
  }

  /**
   * Creates {@code MethodCall} object for the given reflective method.
   * 
   * @param  method the {@link Method} object
   * @return constructed {@link MethodCall}
   */
  public static MethodCall createMethodCall(Method method) {
    return new MethodCall(method);
  }

  /**
   * toString outputs a parseable text representation of the method call.
   * @return string representation constructed by {@link MethodCall#toParseableString()}
   */
  @Override
  public String toString() {
    return toParseableString();
  }

  /**
   * {@inheritDoc}
   * Issues the code that corresponds to calling the method with the provided 
   * {@link Variable} objects as arguments.
   * @param inputVars is the list of actual arguments to be printed.
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder sb) {
    
    String receiverString = isStatic() ? null : inputVars.get(0).getName();
    appendReceiverOrClassForStatics(receiverString, sb);

    sb.append(".");
    sb.append(getTypeArguments());
    sb.append(getMethod().getName() + "(");

    int startIndex = (isStatic() ? 0 : 1);
    for (int i = startIndex ; i < inputVars.size() ; i++) {
      if (i > startIndex)
        sb.append(", ");

      // CASTING.
      if (PrimitiveTypes.isPrimitive(getInputTypes().get(i)) && GenInputsAbstract.long_format) {
        // Cast if input type is a primitive, because Randoop uses
        // boxed primitives.  (Is that necessary with autoboxing?)
        sb.append("(" + getInputTypes().get(i).getName() + ")");
      } else if (!inputVars.get(i).getType().equals(getInputTypes().get(i))) {
        // Cast if the variable and input types are not identical.
        sb.append("(" + getInputTypes().get(i).getCanonicalName() + ")");
      }

      // In the short output format, statements like "int x = 3" are not added 
      // to a sequence; instead, the value (e.g. "3") is inserted directly added 
      // as arguments to method calls.
      Statement statementCreatingVar = inputVars.get(i).getDeclaringStatement();
      String shortForm = statementCreatingVar.getShortForm();
      if (!GenInputsAbstract.long_format && shortForm != null) {
        sb.append(shortForm);
      } else {
        sb.append(inputVars.get(i).getName());
      }
    }
    sb.append(")");
  }
  
  // XXX this is a pretty bogus workaround for a bug in javac (type inference
  // fails sometimes)
  // It is bogus because what we produce here may be different from correct
  // inferred type.
  private String getTypeArguments() {
    TypeVariable<Method>[] typeParameters = method.getTypeParameters();
    if (typeParameters.length == 0)
      return "";
    StringBuilder b = new StringBuilder();
    Class<?>[] params = new Class<?>[typeParameters.length];
    b.append("<");
    for (int i = 0; i < typeParameters.length; i++) {
      if (i > 0)
        b.append(",");
      Type firstBound = typeParameters[i].getBounds().length == 0 ? Object.class
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
    if (t instanceof Class<?>)
      return (Class<?>) t;
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
      throw new UnsupportedOperationException(
          "erasure of arrays not implemented " + t);
    if (t instanceof WildcardType)
      throw new UnsupportedOperationException(
          "erasure of wildcards not implemented " + t);
    throw new IllegalStateException("unexpected type " + t);
  }

  private void appendReceiverOrClassForStatics(String receiverString,
      StringBuilder b) {
    if (isStatic()) {
      String s2 = this.method.getDeclaringClass().getName().replace('$',
      '.'); // TODO combine this with last if clause
      b.append(s2);
    } else {
      Class<?> expectedType = getInputTypes().get(0);
      String canonicalName = expectedType.getCanonicalName();
      boolean mustCast = canonicalName != null
      && PrimitiveTypes
      .isBoxedPrimitiveTypeOrString(expectedType)
      && !expectedType.equals(String.class);
      if (mustCast) {
        // this is a little paranoid but we need to cast primitives in
        // order to get them boxed.
        b.append("((" + canonicalName + ")" + receiverString + ")");
      } else {
        b.append(receiverString);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MethodCall))
      return false;
    if (this == o)
      return true;
    MethodCall other = (MethodCall) o;
    return this.method.equals(other.method);
  }

  @Override
  public int hashCode() {
    if (!hashCodeComputed) {
      hashCodeComputed = true;
      hashCodeCached = this.method.hashCode();
    }
    return hashCodeCached;
  }

  public long calls_time = 0;
  public int calls_num = 0;

  /**
   * {@inheritDoc}
   * @return {@link NormalExecution} with return value if execution normal, 
   *         otherwise {@link ExceptionalExecution} if an exception thrown.
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {

    assert statementInput.length == getInputTypes().size();

    Object receiver = null;
    int paramsLength = getInputTypes().size();
    int paramsStartIndex = 0;
    if (!isStatic()) {
      receiver = statementInput[0];
      paramsLength--;
      paramsStartIndex = 1;
    }

    Object[] params = new Object[paramsLength];
    for (int i = 0; i < params.length; i++) {
      params[i] = statementInput[i + paramsStartIndex];
    }

    MethodReflectionCode code = new MethodReflectionCode(this.method,
        receiver, params);

    calls_num++;
    long startTime = System.nanoTime();
    Throwable thrown = ReflectionExecutor.executeReflectionCode(code, out);
    calls_time += System.nanoTime() - startTime;

    if (thrown == null) {
      return new NormalExecution(code.getReturnVariable(), 0);
    } else {
      return new ExceptionalExecution(thrown, 0);
    }
  }

  /**
   * {@inheritDoc}
   * If the method is non-static the first element of the list is the
   * type of the class to which the method belongs.
   * 
   * @return list of argument types for this method.
   */
  @Override
  public List<Class<?>> getInputTypes() {
    if (inputTypesCached == null) {
      Class<?>[] methodParameterTypes = method.getParameterTypes();
      inputTypesCached = new ArrayList<Class<?>>(
          methodParameterTypes.length + (isStatic() ? 0 : 1));
      if (!isStatic())
        inputTypesCached.add(method.getDeclaringClass());
      for (int i = 0; i < methodParameterTypes.length; i++) {
        inputTypesCached.add(methodParameterTypes[i]);
      }
    }
    return inputTypesCached;
  }

  /**
   * {@inheritDoc}
   * @return return type of this method.
   */
  @Override
  public Class<?> getOutputType() {
    if (outputTypeCached == null) {
      outputTypeCached = method.getReturnType();
    }
    return outputTypeCached;
  }

  /**
   * isVoid is a predicate to indicate whether this method has a void return types.
   * 
   * @return true if this method has a void return type, false otherwise.
   */
  public boolean isVoid() {
    if (!isVoidComputed) {
      isVoidComputed = true;
      isVoidCached = void.class.equals(this.method.getReturnType());
    }
    return isVoidCached;
  }

  /**
   * {@inheritDoc}
   * @return true if this method is static, and false, otherwise.
   */
  @Override
  public boolean isStatic() {
    if (!isStaticComputed) {
      isStaticComputed = true;
      isStaticCached = Modifier.isStatic(this.method.getModifiers());
    }
    return this.isStaticCached;
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
  public String toParseableString() {
    return MethodSignatures.getSignatureString(this.method);
  }

  /**
   * Parses a method call in a string descriptor and returns a {@link MethodCall}
   * object. Should satisfy <code>parse(op.toParseableString()).equals(op)</code>
   * for Operation op.
   * @see OperationParser#parse(String)
   * 
   * @param s  a string descriptor
   * @return the {@link MethodCall} object described by the string.
   * @throws OperationParseException if s does not match expected descriptor.
   */
  public static Operation parse(String s) throws OperationParseException {
    return MethodCall.createMethodCall(MethodSignatures.getMethodForSignatureString(s));
  }

  /**
   * {@inheritDoc}
   * @return the class in which this method is declared.
   */
  @Override
  public Class<?> getDeclaringClass() {
    return method.getDeclaringClass();
  }

  /**
   * callsMethodIn determines whether the current method object
   * calls one of the methods in the list.
   * @param list method objects to compare against.
   * @return true if method called by this object is in the given list.
   */
  public boolean callsMethodIn(List<Method> list) {
    return list != null && list.contains(method);
  }

  /**
   * callsMethod determines whether the method that this object calls is 
   * method given in the parameter.
   * @param m method to test against.
   * @return true, if m corresponds to the method in this object, false, otherwise.
   */
  public boolean callsMethod(Method m) {
    return method.equals(m);
  }

  /**
   * {@inheritDoc}
   * @return true always, since this is a method call. 
   */
  @Override
  public boolean isMessage() {
    return true;
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
    return predicate.canUse(method);
  }
  
}
