package randoop.instrument;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import org.plumelib.bcelutil.BcelUtil;
import org.plumelib.signature.Signatures;
import org.plumelib.util.UtilPlume;

/**
 * Defines a method in a way that can be used to substitute method calls using BCEL. A method is
 * represented by its fully-qualified name and parameter types as BCEL {@code Type}.
 *
 * <p>Note: this is similar to the Randoop {@code randoop.reflection.RawSignature} class, but uses
 * BCEL {@code Type} instead of {@code java.lang.reflect.Class} for the parameter types.
 */
public class MethodSignature {

  /** fully-qualified class name */
  private final String classname;

  /** simple method name */
  private final String name;

  /** The parameter types */
  private final Type[] paramTypes;

  /** Cached {@link java.lang.reflect.Method} object for this {@link MethodSignature} */
  private Method method;

  /**
   * Creates a {@code MethodSignature}.
   *
   * @param classname the fully-qualified classname
   * @param name the method name
   * @param argTypes the parameter types for the method
   */
  private MethodSignature(String classname, String name, Type[] argTypes) {
    this.classname = classname;
    this.name = name;
    this.paramTypes = argTypes;
    this.method = null;
  }

  /**
   * Creates a {@link MethodSignature} object for a {@code java.lang.reflect.Method} object.
   *
   * @param method the reflection method object
   * @return the {@link MethodSignature} representation of the reflection object
   */
  public static MethodSignature of(Method method) {
    Class<?>[] paramTypes = method.getParameterTypes();
    Type[] argTypes = new Type[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      argTypes[i] = Type.getType(paramTypes[i]);
    }
    return new MethodSignature(
        method.getDeclaringClass().getCanonicalName(), method.getName(), argTypes);
  }

  /**
   * Creates a {@link MethodSignature} object for the method called by a BCEL {@code
   * InvokeInstruction}.
   *
   * @param invocation the BCEL {@code InvokeInstruction} of the method
   * @param pgen the constant pool where the instruction occurs
   * @return the {@link MethodSignature} for the method invoked by the given instruction
   */
  static MethodSignature of(InvokeInstruction invocation, ConstantPoolGen pgen) {
    return new MethodSignature(
        invocation.getClassName(pgen),
        invocation.getMethodName(pgen),
        invocation.getArgumentTypes(pgen));
  }

  /**
   * Creates a {@link MethodSignature} object from string representations of its method name and
   * types.
   *
   * @param fullMethodName fully-qualified name of method
   * @param params fully-qualified names of parameter types
   */
  static MethodSignature of(String fullMethodName, String[] params) {
    int dotPos = fullMethodName.lastIndexOf('.');
    if (dotPos < 1) {
      throw new IllegalArgumentException(
          "Fully-qualified method name expected, no period found: " + fullMethodName);
    }
    String classname = fullMethodName.substring(0, dotPos);
    String methodName = fullMethodName.substring(dotPos + 1);
    Type[] paramTypes = new Type[params.length];
    for (int i = 0; i < params.length; i++) {
      paramTypes[i] = BcelUtil.classnameToType(params[i].trim());
    }

    return new MethodSignature(classname, methodName, paramTypes);
  }

  /**
   * Reads a signature string and builds the corresponding {@link MethodSignature}.
   *
   * <p>The signature string must start with the fully-qualified classname, followed by the method
   * name, and then the fully-qualified parameter types in parentheses.
   *
   * @param signature the method signature string, all types must be fully-qualified
   * @return the {@link MethodSignature} for the method represented by the signature string
   * @throws IllegalArgumentException if {@code signature} is not formatted correctly
   */
  static MethodSignature of(String signature) {
    int parenPos = signature.indexOf('(');
    if (parenPos < 1) {
      throw new IllegalArgumentException(
          "Method signature expected, did not find beginning parenthesis: " + signature);
    }
    String fullMethodName = signature.substring(0, parenPos);
    int lastParenPos = signature.lastIndexOf(')');
    if (lastParenPos < parenPos + 1) {
      throw new IllegalArgumentException(
          "Method signature expected, mismatched parenthesis: " + signature);
    }
    String paramString = signature.substring(parenPos + 1, lastParenPos);
    String[] parameters = paramString.isEmpty() ? new String[0] : paramString.split("\\s*,\\s*");
    return MethodSignature.of(fullMethodName, parameters);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MethodSignature)) {
      return false;
    }
    MethodSignature md = (MethodSignature) obj;
    return this.classname.equals(md.classname)
        && this.name.equals(md.name)
        && Arrays.equals(this.paramTypes, md.paramTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(classname, name, Arrays.hashCode(paramTypes));
  }

  /**
   * Returns the fully-qualified signature string for this {@link MethodSignature}.
   *
   * @return the fully-qualified signature string for this {@link MethodSignature}
   */
  @Override
  public String toString() {
    return String.format("%s.%s(%s)", classname, name, UtilPlume.join(paramTypes, ", "));
  }

  /**
   * Returns the fully-qualified class name of this {@link MethodSignature}.
   *
   * @return the fully-qualified class name of this {@link MethodSignature}
   */
  String getClassname() {
    return classname;
  }

  /**
   * Returns the simple method name for this {@link MethodSignature}.
   *
   * @return the simple method name of this {@link MethodSignature}
   */
  String getName() {
    return name;
  }

  /**
   * Returns the parameter types (as BCEL {@code Type} references) for this {@link MethodSignature}.
   *
   * @return the parameter types for this {@link MethodSignature}
   */
  Type[] getParameterTypes() {
    return paramTypes;
  }

  /**
   * Uses reflection to get the {@code java.lang.reflect.Method} object for this {@link
   * MethodSignature}.
   *
   * @return the reflection object for this {@link MethodSignature}
   * @throws ClassNotFoundException if the containing class of this {@link MethodSignature} is not
   *     found on the classpath
   * @throws NoSuchMethodException if the containing class of this {@link MethodSignature} does not
   *     have the represented method as a member
   */
  Method toMethod() throws ClassNotFoundException, NoSuchMethodException {
    if (method != null) {
      return method;
    }

    Class<?> methodClass = Class.forName(classname);
    Class<?> params[] = new Class[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      params[i] = typeToClass(paramTypes[i]);
    }

    // Note that Method.getMethod only returns public methods, so call Method.getDeclaredMethod
    // first

    // First check if the method is declared in the class
    try {
      method = methodClass.getDeclaredMethod(name, params);
      return method;
    } catch (NoSuchMethodException e) {
      // ignore exception -- look for inherited method
    }

    // If it is not declared in class, check if it is inherited
    method = methodClass.getMethod(name, params);
    return method;
  }

  /**
   * Converts the BCEL type to a {@code java.lang.Class} object.
   *
   * <p>This method replicates the {@code BcelUtils.typeToClass()} method, but does not repackage
   * the exception.
   *
   * @param type the type object
   * @return the {@code Class<?>} object for the type
   * @throws ClassNotFoundException if no {@code Class<?>} was found for the type
   */
  private Class<?> typeToClass(Type type) throws ClassNotFoundException {
    String name = Signatures.fieldDescriptorToClassGetName(type.getSignature());
    return BcelUtil.classForName(name);
  }

  /**
   * Indicates whether the method represented by this {@link MethodSignature} is found on the
   * classpath. (Specifically, whether the containing class can be loaded, and contains the
   * represented method.)
   *
   * @return true if the the represented method exists on the classpath, false otherwise
   */
  boolean exists() {
    try {
      return toMethod() != null;
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      return false;
    }
  }

  /**
   * Returns the {@link MethodSignature} formed by substituting the given classname for the
   * classname of this {@link MethodSignature}.
   *
   * @param classname the substitute classname
   * @return a new {@link MethodSignature} with {@code classname} as the class name, and the
   *     signature of this
   */
  MethodSignature substituteClassname(String classname) {
    return new MethodSignature(classname, this.getName(), this.getParameterTypes());
  }

  /**
   * Returns the {@link MethodSignature} formed by removing the first parameter of this {@link
   * MethodSignature}.
   *
   * @return a new {@link MethodSignature} identical to this one except the signature has the first
   *     parameter removed
   */
  MethodSignature removeFirstParameter() {
    Type[] types = new Type[paramTypes.length - 1];
    System.arraycopy(paramTypes, 1, types, 0, paramTypes.length - 1);
    return new MethodSignature(this.classname, this.getName(), types);
  }
}
