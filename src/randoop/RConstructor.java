package randoop;

import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import randoop.main.GenInputsAbstract;
import randoop.util.ConstructorReflectionCode;
import randoop.util.PrimitiveTypes;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;
import randoop.util.Util;

/**
 * Represents a constructor call. The inputs are parameters to the constructor
 * and outputs include the new object.
 *
 * The "R" stands for "Randoop", to underline the distinction from
 * java.lang.reflect.Constructor.
 *
 */
public final class RConstructor implements StatementKind, Serializable {

  private static final long serialVersionUID = 20100429; 

  /** ID for parsing purposes (see StatementKinds.parse method) */
  public static final String ID = "cons";

  // State variable.
  private final Constructor<?> constructor;

  // Cached values (for improved performance). Their values
  // are computed upon the first invocation of the respective
  // getter method.
  private List<Class<?>> inputTypesCached;
  private Class<?> outputTypeCached;
  private int hashCodeCached = 0;
  private boolean hashCodeComputed = false;

  private Object writeReplace() throws ObjectStreamException {
    return new SerializableRConstructor(constructor);
  }

  // Creates the RConstructor corresponding to the given reflection constructor.
  private RConstructor(Constructor<?> constructor) {
    if (constructor == null)
      throw new IllegalArgumentException("constructor should not be null.");
    this.constructor = constructor;
    // TODO move this earlier in the process: check first that all
    // methods to be used can be made accessible.
    // XXX this should not be here but I get infinite loop when comment out
    this.constructor.setAccessible(true);
  }

  /**
   * Returns the reflection constructor corresponding to this RConstructor.
   */
  public Constructor<?> getConstructor() {
    return this.constructor;
  }

  /**
   * Creates the RConstructor corresponding to the given reflection constructor.
   */
  public static RConstructor getRConstructor(Constructor<?> constructor) {
    return new RConstructor(constructor);
  }

  /**
   * Returns concise string representation of this ConstructorCallInfo
   */
  @Override
  public String toString() {
    return toParseableString();
  }

  // TODO: integrate with below method
  public void appendCode(Variable varName, List<Variable> inputVars, StringBuilder b) {
    assert inputVars.size() == this.getInputTypes().size();

    Class<?> declaringClass = constructor.getDeclaringClass();
    boolean isNonStaticMember = !Modifier.isStatic(declaringClass.getModifiers()) && declaringClass.isMemberClass();
    assert Util.implies(isNonStaticMember, inputVars.size() > 0);

    // Note on isNonStaticMember: if a class is a non-static member class, the
    // runtime signature of the constructor will have an additional argument
    // (as the first argument) corresponding to the owning object. When printing
    // it out as source code, we need to treat it as a special case: instead
    // of printing "new Foo(x,y.z)" we have to print "x.new Foo(y,z)".

    // TODO the last replace is ugly. There should be a method that does it.
    String declaringClassStr = Reflection.getCompilableName(declaringClass);

    b.append(declaringClassStr + " " + varName.getName() + " = "
        + (isNonStaticMember ? inputVars.get(0) + "." : "")
        + "new "
        + (isNonStaticMember ? declaringClass.getSimpleName() : declaringClassStr)
        + "(");
    for (int i = (isNonStaticMember ? 1 : 0) ; i < inputVars.size() ; i++) {
      if (i > (isNonStaticMember ? 1 : 0))
        b.append(", ");

      // We cast whenever the variable and input types are not identical.
      if (!inputVars.get(i).getType().equals(getInputTypes().get(i)))
        b.append("(" + getInputTypes().get(i).getCanonicalName() + ")");
      
      // In the short output format, statements like "int x = 3" are not added to a sequence; instead,
      // the value (e.g. "3") is inserted directly added as arguments to method calls.
      StatementKind statementCreatingVar = inputVars.get(i).getDeclaringStatement(); 
      if (!GenInputsAbstract.long_format
          && ExecutableSequence.canUseShortFormat(statementCreatingVar)) {
        b.append(PrimitiveTypes.toCodeString(((PrimitiveOrStringOrNullDecl) statementCreatingVar).getValue()));
      } else {
        b.append(inputVars.get(i).getName());
      }
    }
    b.append(");");
    b.append(Globals.lineSep);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (this == o)
      return true;
    if (!(o instanceof RConstructor))
      return false;
    RConstructor other = (RConstructor) o;
    if (!this.constructor.equals(other.constructor))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    if (!hashCodeComputed) {
      hashCodeComputed = true;
      hashCodeCached = this.constructor.hashCode();
    }
    return hashCodeCached;
  }

  public long calls_time = 0;
  public int calls_num = 0;

  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {

    assert statementInput.length == this.getInputTypes().size();

    ConstructorReflectionCode code = new ConstructorReflectionCode(
        this.constructor, statementInput);

    // long startTime = System.currentTimeMillis();
    Throwable thrown = ReflectionExecutor.executeReflectionCode(code, out);
    // long totalTime = System.currentTimeMillis() - startTime;

    if (thrown == null) {
      return new NormalExecution(code.getReturnVariable(), 0);
    } else {
      return new ExceptionalExecution(thrown, 0);
    }
  }

  /**
   * Returns the input types of this constructor.
   */
  public List<Class<?>> getInputTypes() {
    if (inputTypesCached == null) {
      inputTypesCached = new ArrayList<Class<?>>(Arrays.asList(constructor.getParameterTypes()));
    }
    return inputTypesCached;
  }

  /**
   * Returns the return type of this constructor.
   */
  public Class<?> getOutputType() {
    if (outputTypeCached == null) {
      outputTypeCached = constructor.getDeclaringClass();
    }
    return outputTypeCached;
  }

  /**
   * A string representing this constructor. The string is of the form:
   *
   * CONSTRUCTOR
   *
   * Where CONSTRUCTOR is a string representation of the constrctor signature. Examples:
   *
   * java.util.ArrayList.<init>()
   * java.util.ArrayList.<init>(java.util.Collection)
   *
   */
  public String toParseableString() {
    return Reflection.getSignature(constructor);
  }

  public static StatementKind parse(String s) {
    return RConstructor.getRConstructor(Reflection.getConstructorForSignature(s));
  }
}
