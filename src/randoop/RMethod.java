package randoop;

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

import randoop.main.GenInputsAbstract;
import randoop.util.CollectionsExt;
import randoop.util.MethodReflectionCode;
import randoop.util.PrimitiveTypes;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;

/**
 * Represents a method call.
 *
 * The "R" stands for "Randoop", to underline the distinction from
 * java.lang.reflect.Method.
 */
public final class RMethod implements StatementKind, Serializable {

  private static final long serialVersionUID = -7616184807726929835L;

  /** ID for parsing purposes (see StatementKinds.parse method) */
  public static final String ID = "method";

  // State variable.
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

  /** Version that doesn't include a Method **/
  private Object writeReplace() throws ObjectStreamException {
    return new SerializableRMethod(method);
  }

  /**
   * Returns Method object represented by this MethodCallInfo
   */
  public Method getMethod() {
    return this.method;
  }

  /**
   * Creates the Rmethod corresponding to the given reflection method.
   */
  private RMethod(Method method) {
    if (method == null)
      throw new IllegalArgumentException("method should not be null.");

    this.method = method;
    // TODO move this earlier in the process: check first that all
    // methods to be used can be made accessible.
    // XXX this should not be here but I get infinite loop when comment out
    this.method.setAccessible(true);
  }

  /**
   * Returns the statement corresponding to the given constructor.
   */
  public static RMethod getRMethod(Method method) {
    return new RMethod(method);
  }

  @Override
  public String toString() {
    return toParseableString();
  }

  public void appendCode(Variable newVar, List<Variable> inputVars, StringBuilder b) {
     if (!isVoid()) {
      b.append(Reflection.getCompilableName(this.method.getReturnType()));
      String cast = "";
      b.append(" " + newVar.getName() + " = " + cast);
    }
    String receiverString = isStatic() ? null : inputVars.get(0).getName();
    appendReceiverOrClassForStatics(receiverString, b);

    b.append(".");
    b.append(getTypeArguments());
    b.append(this.method.getName() + "(");

    int startIndex = (isStatic() ? 0 : 1);
    for (int i = startIndex ; i < inputVars.size() ; i++) {
      if (i > startIndex)
        b.append(", ");

      // CASTING.
      // We cast whenever the variable and input types are not identical.
      // We also cast if input type is a primitive, because Randoop uses
      // boxed primitives, and need to convert back to primitive.
      if (PrimitiveTypes.isPrimitive(getInputTypes().get(i)) && GenInputsAbstract.long_format) {
        b.append("(" + getInputTypes().get(i).getName() + ")");
      } else if (!inputVars.get(i).getType().equals(getInputTypes().get(i))) {
        b.append("(" + getInputTypes().get(i).getCanonicalName() + ")");
      }

      // In the short output format, statements like "int x = 3" are not added to a sequence; instead,
      // the value (e.g. "3") is inserted directly added as arguments to method calls.
      StatementKind statementCreatingVar = inputVars.get(i).getDeclaringStatement(); 
      if (!GenInputsAbstract.long_format
          && ExecutableSequence.canUseShortFormat(statementCreatingVar)) {
        Object val = ((PrimitiveOrStringOrNullDecl) statementCreatingVar).getValue();
        b.append(PrimitiveTypes.toCodeString(val));
      } else {
        b.append(inputVars.get(i).getName());
      }
    }

    b.append(");" + Globals.lineSep);
  }
  
  // XXX this is a pretty bogus workaround for a bug in javac (type inference
  // fails sometimes)
  // It is bogus because what we produce here may be different from correct
  // infered type.
  public String getTypeArguments() {
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
    if (!(o instanceof RMethod))
      return false;
    if (this == o)
      return true;
    RMethod other = (RMethod) o;
    if (!this.method.equals(other.method))
      return false;
    return true;
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
   * Returns the input types of this method.
   */
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
   * Returns the return type of this method.
   */
  public Class<?> getOutputType() {
    if (outputTypeCached == null) {
      outputTypeCached = method.getReturnType();
    }
    return outputTypeCached;
  }

  public boolean isVoid() {
    if (!isVoidComputed) {
      isVoidComputed = true;
      isVoidCached = void.class.equals(this.method.getReturnType());
    }
    return isVoidCached;
  }

  /**
   * Returns true if method represented by this MethodCallInfo is a static
   * method.
   */
  public boolean isStatic() {
    if (!isStaticComputed) {
      isStaticComputed = true;
      isStaticCached = Modifier.isStatic(this.method.getModifiers());
    }
    return this.isStaticCached;
  }

  /**
   * A string representing this method. The string is of the form:
   *
   * METHOD
   *
   * Where METHOD is a string representation of the method signature. Examples:
   *
   * java.util.ArrayList.get(int)
   * java.util.ArrayList.add(int,java.lang.Object)
   */
  public String toParseableString() {
    return Reflection.getSignature(method);
  }

  public static StatementKind parse(String s) {
    return RMethod.getRMethod(Reflection.getMethodForSignature(s));
  }


}
