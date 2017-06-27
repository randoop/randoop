package randoop.instrument;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import plume.BCELUtil;
import plume.UtilMDE;

/**
 * Defines a method in a way that can be used to substitute method calls using BCEL. A method is
 * represented by its fully-qualified name and parameter types as BCEL {@code Type}.
 */
public class MethodDef {

  /** fully-qualified class name */
  private final String classname;

  /** simple method name */
  private final String name;

  /** The parameter types */
  private final Type[] paramTypes;

  /** Cached {@link java.lang.reflect.Method} object for this {@link MethodDef} */
  private Method method;

  /**
   * Creates a {@code MethodDef}.
   *
   * @param classname the fully-qualified classname
   * @param name the method name
   * @param argTypes the parameter types for the method
   */
  private MethodDef(String classname, String name, Type[] argTypes) {
    this.classname = classname;
    this.name = name;
    this.paramTypes = argTypes;
    this.method = null;
  }

  /**
   * Creates a {@link MethodDef} object for a {@code java.lang.reflect.Method} object.
   *
   * @param method the reflection method object
   * @return the {@link MethodDef} representation of the reflection object
   */
  public static MethodDef of(Method method) {
    Class<?>[] paramTypes = method.getParameterTypes();
    Type[] argTypes = new Type[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      argTypes[i] = Type.getType(paramTypes[i]);
    }
    return new MethodDef(method.getDeclaringClass().getCanonicalName(), method.getName(), argTypes);
  }

  /**
   * Creates a {@link MethodDef} object for the method called by a BCEL {@code InvokeInstruction}.
   *
   * @param invocation the BCEL {@code InvokeInstruction} of the method
   * @param pgen the constant pool where the instruction occurs
   * @return the {@link MethodDef} for the method invoked by the given instruction
   */
  static MethodDef of(InvokeInstruction invocation, ConstantPoolGen pgen) {
    return new MethodDef(
        invocation.getClassName(pgen),
        invocation.getMethodName(pgen),
        invocation.getArgumentTypes(pgen));
  }

  /**
   * Creates a {@link MethodDef} object from string representations of its method name and types.
   *
   * @param fullMethodName fully-qualified name of method
   * @param args fully-qualified names of parameter types
   */
  static MethodDef of(String fullMethodName, String[] args) {
    String methodName = fullMethodName;
    String classname = "";
    int dotPos = fullMethodName.lastIndexOf('.');
    if (dotPos > 0) {
      methodName = fullMethodName.substring(dotPos + 1);
      classname = fullMethodName.substring(0, dotPos);
    } else {
      throw new IllegalArgumentException("Fully-qualified method name expected");
    }
    Type[] argTypes = new Type[args.length];
    for (int i = 0; i < args.length; i++) {
      argTypes[i] = BCELUtil.classname_to_type(args[i].trim());
    }

    return new MethodDef(classname, methodName, argTypes);
  }

  /**
   * Reads a signature string and builds the corresponding {@link MethodDef}.
   *
   * <p>The signature string must start with the fully-qualified classname, followed by the method
   * name, and then the fully-qualified parameter types in parenthesis.
   *
   * @param signature the method signature string, all types must be fully-qualified
   * @return the {@link MethodDef} for the method represented by the signature string
   * @throws IllegalArgumentException if {@code signature} is not formatted correctly
   */
  static MethodDef of(String signature) {
    int parenPos = signature.indexOf('(');
    if (parenPos < 1) {
      throw new IllegalArgumentException("Method signature expected");
    }
    String name = signature.substring(0, parenPos);
    int lastParenPos = signature.lastIndexOf(')');
    if (lastParenPos < parenPos + 1) {
      throw new IllegalArgumentException("Method signature expected");
    }
    String argString = signature.substring(parenPos + 1, lastParenPos);
    String[] arguments = new String[0];
    if (!argString.isEmpty()) {
      arguments = argString.split("\\s*,\\s*");
    }
    return MethodDef.of(name, arguments);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MethodDef)) {
      return false;
    }
    MethodDef md = (MethodDef) obj;
    return this.classname.equals(md.classname)
        && this.name.equals(md.name)
        && Arrays.equals(this.paramTypes, md.paramTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(classname, name, Arrays.hashCode(paramTypes));
  }

  /**
   * Returns the fully-qualified signature string for this {@link MethodDef}.
   *
   * @return the fully-qualified signature string for this {@link MethodDef}
   */
  @Override
  public String toString() {
    return String.format("%s.%s(%s)", classname, name, UtilMDE.join(paramTypes, ", "));
  }

  /**
   * Returns the fully-qualified class name of this {@link MethodDef}.
   *
   * @return the fully-qualified class name of this {@link MethodDef}
   */
  String getClassname() {
    return classname;
  }

  /**
   * Returns the method name for this {@link MethodDef}.
   *
   * @return the method name of this {@link MethodDef}
   */
  String getName() {
    return name;
  }

  /**
   * Returns the parameter types (as BCEL {@code Type} references) for this {@link MethodDef}.
   *
   * @return the parameter types for this {@link MethodDef}
   */
  Type[] getParameterTypes() {
    return paramTypes;
  }

  /**
   * Uses reflection to get the {@code java.lang.reflect.Method} object for this {@link MethodDef}.
   *
   * @return the reflection object for this {@link MethodDef}
   * @throws ClassNotFoundException if the containing class of this {@link MethodDef} is not found
   *     on the classpath
   * @throws NoSuchMethodException if the containing class of this {@link MethodDef} does not have
   *     the represented method as a member
   */
  Method toMethod() throws ClassNotFoundException, NoSuchMethodException {
    if (method != null) {
      return method;
    }

    Class<?> methodClass = Class.forName(classname);
    Class<?> args[] = new Class[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      try {
        args[i] = typeToClass(paramTypes[i]);
      } catch (ClassNotFoundException e) {
        throw new ArgumentClassNotFoundException(e.getMessage());
      }
    }

    // First check it the method is declared in the class
    try {
      method = methodClass.getDeclaredMethod(name, args);
      method.setAccessible(true);
      return method;
    } catch (NoSuchMethodException e) {
      // ignore -- look for inherited method
    }

    // If it is not declared in class, check if it is inherited
    method = methodClass.getMethod(name, args);
    return method;
  }

  /**
   * Converts the BCEL type to a {@code java.lang.Class} object. (This method replicates the {@code
   * BCELUtils.type_to_class()} method, but does not repackage the exception.)
   *
   * @param type the type object
   * @return the {@code Class<?>} object for the type
   * @throws ClassNotFoundException if no {@code Class<?>} was found for the type
   */
  private Class<?> typeToClass(Type type) throws ClassNotFoundException {
    Class<?> c = null;
    String name = UtilMDE.fieldDescriptorToClassGetName(type.getSignature());
    c = UtilMDE.classForName(name);
    return c;
  }

  /**
   * Indicates whether the method represented by this {@link MethodDef} is found on the classpath.
   * (Specifically, whether the containing class can be loaded, and contains the represented
   * method.)
   *
   * @return true if the the represented method exists on the classpath, false otherwise.
   */
  boolean exists() {
    try {
      return toMethod() != null;
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      return false;
    }
  }

  /**
   * Returns the {@link MethodDef} formed by substituting the given classname for the classname of
   * this {@link MethodDef}.
   *
   * @param classname the substitute classname
   * @return a new {@link MethodDef} with {@code classname} as the class name, and the signature of
   *     this
   */
  MethodDef substituteClassname(String classname) {
    return new MethodDef(classname, this.getName(), this.getParameterTypes());
  }

  /**
   * Returns the {@link MethodDef} formed by removing the first parameter of this {@link MethodDef}.
   *
   * @return a new {@link MethodDef} identical to this one except the signature has the first
   *     parameter removed
   */
  MethodDef removeFirstParameter() {
    Type[] types = new Type[paramTypes.length - 1];
    System.arraycopy(paramTypes, 1, types, 0, paramTypes.length - 1);
    return new MethodDef(this.classname, this.getName(), types);
  }
}
