package randoop.instrument;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import plume.BCELUtil;
import plume.UtilMDE;

/** Class that defines a method (by its fully-qualified name and argument types) */
class MethodDef {

  /** fully-qualified class name */
  private final String classname;

  /** simple method name */
  private final String name;

  /** The argument types */
  private final Type[] argTypes;

  MethodDef(String classname, String name, Type[] argTypes) {
    this.classname = classname;
    this.name = name;
    this.argTypes = argTypes;
  }

  public static MethodDef of(Method method) {
    Class<?>[] paramTypes = method.getParameterTypes();
    Type[] argTypes = new Type[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      argTypes[i] = Type.getType(paramTypes[i]);
    }
    return new MethodDef(method.getDeclaringClass().getCanonicalName(), method.getName(), argTypes);
  }

  static MethodDef of(InvokeInstruction invocation, ConstantPoolGen pgen) {
    return new MethodDef(
        invocation.getClassName(pgen),
        invocation.getMethodName(pgen),
        invocation.getArgumentTypes(pgen));
  }

  /**
   * @param fullMethodName fully-qualified name of method
   * @param args fully-qualified names of argument types
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
        && Arrays.equals(this.argTypes, md.argTypes);
  }

  @Override
  public int hashCode() {
    if (argTypes.length > 0) {
      return Objects.hash(classname, name, Arrays.hashCode(argTypes));
    } else {
      return Objects.hash(classname, name);
    }
  }

  @Override
  public String toString() {
    return String.format("%s.%s(%s)", classname, name, UtilMDE.join(argTypes, ", "));
  }

  String getClassname() {
    return classname;
  }

  String getName() {
    return name;
  }

  Type[] getArgTypes() {
    return argTypes;
  }

  Method toMethod() throws ClassNotFoundException, NoSuchMethodException {
    Class<?> methodClass = Class.forName(classname);
    Class<?> args[] = new Class[argTypes.length];
    for (int i = 0; i < argTypes.length; i++) {
      args[i] = BCELUtil.type_to_class(argTypes[i]);
    }
    Method method = methodClass.getDeclaredMethod(name, args);
    method.setAccessible(true);
    return method;
  }

  boolean exists() {
    try {
      return toMethod() != null;
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      return false;
    }
  }
}
