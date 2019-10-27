package randoop.instrument;

import java.lang.instrument.IllegalClassFormatException;
import java.util.Arrays;
import java.util.Objects;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import org.plumelib.bcelutil.BcelUtil;
import org.plumelib.util.UtilPlume;

/**
 * Defines a method in a way that can be used to substitute method calls using BCEL. A method is
 * represented by its fully-qualified name and parameter types as BCEL {@code Type}. Once a
 * MethodSignature is created it is never modified.
 *
 * <p>Note: this is similar to the Randoop {@code randoop.reflection.RawSignature} class, but uses
 * BCEL {@code Type} instead of {@code java.lang.reflect.Class} for the parameter types.
 */
public class MethodSignature implements Comparable<MethodSignature> {

  /** The fully-qualified class name. */
  private final String classname;

  /** The method name. */
  private final String name;

  /** The parameter types. */
  private final Type[] paramTypes;

  /**
   * Cached {@link org.apache.bcel.classfile.Method} object for this {@link MethodSignature}. Is set
   * by {@link #toMethod}.
   */
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
   * Creates a {@link MethodSignature} object for a {@code org.apache.bcel.classfile.Method} object.
   *
   * @param classname the class containing the method
   * @param method the Method object
   * @return the {@link MethodSignature} representation of the Method object
   */
  public static MethodSignature of(String classname, org.apache.bcel.classfile.Method method) {
    return new MethodSignature(classname, method.getName(), method.getArgumentTypes());
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
   * @return the {@link MethodSignature} for the method represented by the string
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
   * name, and then the fully-qualified parameter types in parentheses. Note that a signature does
   * not include a return type.
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
  public int compareTo(MethodSignature m) {
    int result = this.classname.compareTo(m.classname);
    if (result == 0) {
      result = this.name.compareTo(m.name);
      if (result == 0) {
        // shorter array is considered 'less than'
        if (this.paramTypes.length < m.paramTypes.length) {
          return -1;
        } else if (this.paramTypes.length > m.paramTypes.length) {
          return 1;
        }
        for (int i = 0; i < this.paramTypes.length; i++) {
          result = this.paramTypes[i].getSignature().compareTo(m.paramTypes[i].getSignature());
          if (result != 0) {
            return result;
          }
        }
      }
    }
    return result;
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
   * Returns the {@code java.lang.reflect.Method} object for this {@link MethodSignature}.
   *
   * <p>Tries to locate a class file whose name is contained in {@code this.classname}. If found, it
   * then searches that class file for a method whose name matches {@code this.name} and whose
   * argument types match {@code this.paramTypes}. If it finds a matching method it returns the
   * corresponding {@code org.apache.bcel.classfile.Method} object for this {@link MethodSignature}.
   * If the class exists, but the method is not found, it checks to see if there is a superclass and
   * repeats the search process.
   *
   * @return the Method object for this {@link MethodSignature}
   * @throws ClassNotFoundException if the containing class of this {@link MethodSignature} is not
   *     found on the classpath
   * @throws NoSuchMethodException if the containing class of this {@link MethodSignature} does not
   *     have the represented method as a member
   * @throws IllegalClassFormatException if the containing class of this {@link MethodSignature}
   *     exists, but cannot be loaded
   */
  Method toMethod()
      throws ClassNotFoundException, NoSuchMethodException, IllegalClassFormatException {
    if (method != null) {
      return method;
    }

    String currentClassname = classname;
    while (true) {
      // Check that the class exists
      JavaClass currentClass;
      try {
        currentClass = ReplacementFileReader.getJavaClassFromClassname(currentClassname);
      } catch (Throwable e) {
        throw new IllegalClassFormatException("Unable to read: " + currentClassname);
      }
      if (currentClass == null) {
        throw new ClassNotFoundException("Class " + currentClassname + " not found");
      }

      for (Method m : currentClass.getMethods()) {
        if (m.getName().equals(this.name) && Arrays.equals(m.getArgumentTypes(), this.paramTypes)) {
          // we have a match
          this.method = m;
          return m;
        }
      }

      // Method not found; perhaps inherited from superclass.
      // Cannot use "currentClass = currentClass.getSuperClass()" because the superclass might
      // not have been loaded into BCEL yet.
      if (currentClass.getSuperclassNameIndex() == 0) {
        // The current class is Object; the search completed without finding a matching method.
        throw new NoSuchMethodException("Method " + this.name + " not found");
      }
      currentClassname = currentClass.getSuperclassName();
    }
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
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalClassFormatException e) {
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
