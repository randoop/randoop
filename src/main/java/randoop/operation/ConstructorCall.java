package randoop.operation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.ConstructorReflectionCode;
import randoop.util.ReflectionExecutor;
import randoop.util.Util;

/**
 * Represents a call to a constructor. It holds a reflective {@link java.lang.reflect.Constructor}
 * object.
 *
 * <p>As an {@link Operation}, a call to constructor <i>c</i> with <i>n</i> arguments is represented
 * as <i>c</i> : [<i>t1,...,tn</i>] &rarr; <i>c</i>, where the output type <i>c</i> is also the name
 * of the class.
 */
public final class ConstructorCall extends CallableOperation {

  /** The constructor that is called by this ConstructorCall. */
  private final Constructor<?> constructor;

  // Cached values (for improved performance). Their values
  // are computed upon the first invocation of the respective
  // getter method.
  private int hashCodeCached = 0;
  private boolean hashCodeComputed = false;

  /**
   * Creates object corresponding to the given reflection constructor.
   *
   * @param constructor reflective object for a constructor
   */
  public ConstructorCall(Constructor<?> constructor) {
    if (constructor == null) {
      throw new IllegalArgumentException("constructor should not be null.");
    }
    this.constructor = constructor;
    this.constructor.setAccessible(true);
  }

  /**
   * Returns what this calls.
   *
   * @return what this calls
   */
  public Constructor<?> getConstructor() {
    return this.constructor;
  }

  // `isMessage()` is not overridden: it returns false, because a constructor call has no receiver
  // argument and is not a method-call-like operation.

  @Override
  public boolean isConstructorCall() {
    return true;
  }

  @Override
  public Constructor<?> getReflectionObject() {
    return constructor;
  }

  @Override
  public String getName() {
    return "<init>";
  }

  /** Returns a string representation of this. */
  @Override
  public String toString() {
    StringJoiner b = new StringJoiner(", ", constructor.getName() + "(", ")");
    Class<?>[] types = constructor.getParameterTypes();
    for (Class<?> c : types) {
      b.add(c.getName());
    }
    return b.toString();
  }

  /**
   * Adds code for calling this to the given {@link StringBuilder}.
   *
   * @param inputVars the list of actual arguments to for the call
   * @param sb the StringBuilder to which the output is appended
   * @see TypedClassOperation#appendCode(List, StringBuilder)
   */
  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder sb) {
    assert declaringType instanceof ClassOrInterfaceType : "constructor must be member of class";

    ClassOrInterfaceType declaringClassType = (ClassOrInterfaceType) declaringType;

    boolean isMemberClass = declaringClassType.isMemberClass();
    assert Util.implies(isMemberClass, !inputVars.isEmpty());

    // If a class is a non-static member class, the
    // runtime signature of the constructor has an additional argument
    // (as the first argument) corresponding to the owning object. When printing
    // it out as source code, we need to treat it as a special case: instead
    // of printing "new Foo(x,y,z)" we have to print "x.new Foo(y,z)".
    sb.append(isMemberClass ? inputVars.get(0) + "." : "")
        .append("new ")
        .append(
            isMemberClass ? declaringClassType.getSimpleName() : declaringClassType.getFqName());

    StringJoiner arguments = new StringJoiner(", ", "(", ")");
    for (int i = (isMemberClass ? 1 : 0); i < inputVars.size(); i++) {
      // We cast whenever the variable and input types are not identical.
      String cast;
      if (inputVars.get(i).getType().equals(inputTypes.get(i))) {
        cast = "";
      } else {
        // Cast if the argument and formal parameter types are not identical.
        cast = "(" + inputTypes.get(i).getFqName() + ")";
      }
      String param = getArgumentString(inputVars.get(i));
      arguments.add(cast + param);
    }
    sb.append(arguments.toString());
  }

  /**
   * Returns true if the argument is a call to the same constructor.
   *
   * @param o an object
   * @return true if o is a ConstructorCall referring to same constructor object; false otherwise
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConstructorCall)) {
      return false;
    }
    ConstructorCall other = (ConstructorCall) o;
    return this.constructor.equals(other.constructor);
  }

  @Override
  public int hashCode() {
    if (!hashCodeComputed) {
      hashCodeComputed = true;
      hashCodeCached = this.constructor.hashCode();
    }
    return hashCodeCached;
  }

  @Override
  public ExecutionOutcome execute(Object[] input) {

    // if this is a constructor from a non-static inner class, then first argument must
    // be a superclass object that is non-null.  If null, then code should throw NPE, but
    // reflection class will happily create the object. So, we have to add the correct behavior.
    if (input.length > 0 && input[0] == null) {
      Class<?> declaringClass = this.constructor.getDeclaringClass();
      int mods = declaringClass.getModifiers() & Modifier.classModifiers();
      if (declaringClass.isMemberClass() && !Modifier.isStatic(mods)) {
        String message =
            "reflection call to " + constructor.getName() + " with null for superclass argument";
        return new ExceptionalExecution(new NullPointerException(message), 0);
      }
    }
    ConstructorReflectionCode code = new ConstructorReflectionCode(this.constructor, input);

    return ReflectionExecutor.executeReflectionCode(code);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns a string representation of the signature.
   *
   * <p>Examples:
   *
   * <pre>{@code
   * java.util.ArrayList.<init>()
   * java.util.ArrayList.<init>(java.util.Collection)
   * }</pre>
   *
   * @return the signature of this
   * @see #parse(String)
   */
  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    StringBuilder sb = new StringBuilder();
    sb.append(constructor.getName());
    sb.append('.');
    sb.append("<init>");
    sb.append("(");
    Class<?>[] params = constructor.getParameterTypes();
    TypeArguments.getTypeArgumentString(sb, params);
    sb.append(')');
    return sb.toString();
  }

  /**
   * Parse a constructor call in a string with the format generated by {@link
   * ConstructorCall#toParsableString(Type, TypeTuple, Type)} and returns the corresponding {@link
   * ConstructorCall} object.
   *
   * @param signature a string descriptor
   * @return the call operation for the given string descriptor
   * @throws OperationParseException if no constructor found for signature
   * @see OperationParser#parse(String)
   */
  @SuppressWarnings("signature") // parsing
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
    assert opname.equals("<init>") : "expected init, saw " + opname;
    String arguments = signature.substring(openParPos + 1, closeParPos);

    Type classType;
    try {
      classType = Type.getTypeforFullyQualifiedName(classname);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      String msg =
          "Class " + classname + " is not on classpath while parsing \"" + signature + "\"";
      throw new OperationParseException(msg);
    }

    Class<?>[] typeArguments;
    try {
      typeArguments = TypeArguments.getTypeArgumentsForString(arguments);
    } catch (OperationParseException e) {
      throw new OperationParseException(e.getMessage() + " while parsing \"" + signature + "\"", e);
    }
    Constructor<?> con;
    try {
      con = classType.getRuntimeClass().getDeclaredConstructor(typeArguments);
    } catch (NoSuchMethodException e) {
      String msg =
          "Constructor"
              + " with parameters "
              + Arrays.toString(typeArguments)
              + " does not exist in "
              + classType
              + ": "
              + e;
      throw new OperationParseException(msg);
    }

    return TypedClassOperation.forConstructor(con);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Determines whether this satisfies the given predicate.
   *
   * @param reflectionPredicate the {@link ReflectionPredicate} to be checked
   * @return true if the constructor in this object satisfies the {@link
   *     ReflectionPredicate#test(Constructor)} implemented by predicate
   */
  @Override
  public boolean satisfies(ReflectionPredicate reflectionPredicate) {
    return reflectionPredicate.test(constructor);
  }
}
