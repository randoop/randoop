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
class MethodDef {

  /** fully-qualified class name */
  private final String classname;

  /** simple method name */
  private final String name;

  /** The parameter types */
  private final Type[] paramTypes;

  /**
   * Creates a {@code MethodDef}.
   *
   * @param classname the fully-qualified classname
   * @param name the method name
   * @param argTypes the parameter types for the method
   */
  MethodDef(String classname, String name, Type[] argTypes) {
    this.classname = classname;
    this.name = name;
    this.paramTypes = argTypes;
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
    }
    Type[] argTypes = new Type[args.length];
    for (int i = 0; i < args.length; i++) {
      argTypes[i] = BCELUtil.classname_to_type(args[i].trim());
    }

    return new MethodDef(classname, methodName, argTypes);
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
    if (paramTypes.length > 0) {
      return Objects.hash(classname, name, Arrays.hashCode(paramTypes));
    } else {
      return Objects.hash(classname, name);
    }
  }

  @Override
  public String toString() {
    return String.format("%s.%s(%s)", classname, name, UtilMDE.join(paramTypes, ", "));
  }

  String getClassname() {
    return classname;
  }

  String getName() {
    return name;
  }

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
    Class<?> methodClass = Class.forName(classname);
    Class<?> args[] = new Class[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      args[i] = BCELUtil.type_to_class(paramTypes[i]);
    }
    Method method;
    try {
      method = methodClass.getDeclaredMethod(name, args);
      method.setAccessible(true);
      return method;
    } catch (NoSuchMethodException e) {
      // ignore -- look for inherited method
    }
    method = methodClass.getMethod(name, args);
    method.setAccessible(true);
    return method;
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
   *     parameter type removed
   */
  MethodDef removeFirstParameter() {
    Type[] types = new Type[paramTypes.length - 1];
    System.arraycopy(paramTypes, 1, types, 0, paramTypes.length - 1);
    return new MethodDef(this.classname, this.getName(), types);
  }
}
