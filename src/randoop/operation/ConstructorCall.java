package randoop.operation;

import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.TypeNames;
import randoop.util.ConstructorReflectionCode;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;
import randoop.util.Util;

/**
 * ConstructorCall represents a call to a constructor, and holds a reference to
 * a reflective {@link java.lang.reflect.Constructor} object.  
 * As an {@link Operation}, the inputs are parameters to the constructor
 * and outputs include the new object.
 */
public final class ConstructorCall extends AbstractOperation implements Operation, Serializable {

  private static final long serialVersionUID = 20100429; 

  /** ID for parsing purposes (see StatementKinds.parse method) */
  public static final String ID = "cons";

  private final Constructor<?> constructor;

  /**
   * A list with as many sublists as the formal parameters of this constructor.
   * The <em>i</em>th set indicates all the possible argument types for the
   * <em>i</im>th formal parameter, for overloads of this constructor with the
   * same number of formal parameters.  At a call site, if the declared
   * type of an actual argument is not uniquely determined, then the actual
   * should be casted at the call site.
   */ 
  public List<Set<Class<?>>> overloads;

  // Cached values (for improved performance). Their values
  // are computed upon the first invocation of the respective
  // getter method.
  private List<Class<?>> inputTypesCached;
  private Class<?> outputTypeCached;
  private int hashCodeCached = 0;
  private boolean hashCodeComputed = false;

  private Object writeReplace() throws ObjectStreamException {
    return new SerializableConstructorCall(constructor);
  }

  /** 
   * Creates the ConstructorCall corresponding to the given reflection constructor.
   * @param constructor reflective object for a constructor.
   */
  public ConstructorCall(Constructor<?> constructor) {
    if (constructor == null)
      throw new IllegalArgumentException("constructor should not be null.");
    this.constructor = constructor;
    // TODO move this earlier in the process: check first that all
    // methods to be used can be made accessible.
    // XXX this should not be here but I get infinite loop when comment out
    this.constructor.setAccessible(true);
  }

  /**
   * getConstructor returns the reflection constructor corresponding to this ConstructorCall.
   * @return {@link Constructor<?>} object called by this constructor call.
   */
  public Constructor<?> getConstructor() {
    return this.constructor;
  }

  /**
   * Creates the ConstructorCall corresponding to the given reflection constructor.
   * @return a new ConstructorCall object for the given {@link Constructor<?>} instance.
   */
  public static ConstructorCall getRConstructor(Constructor<?> constructor) {
    return new ConstructorCall(constructor);
  }

  /** 
   * Reset/clear the overloads field. 
   */
  public void resetOverloads() {
    overloads = new ArrayList<Set<Class<?>>>();
    // For Java 8: for (int i=0; i<constructor.getParameterCount(); i++) {
    for (int i=0; i<constructor.getParameterTypes().length; i++) {
      overloads.add(new HashSet<Class<?>>());
    }
    addToOverloads(constructor);
  }

  public void addToOverloads(Constructor<?> c) {
    Class<?>[] ptypes = c.getParameterTypes();
    assert ptypes.length == overloads.size();
    for (int i=0; i<overloads.size(); i++) {
      overloads.get(i).add(ptypes[i]);
    }
  }

  /**
   * Returns concise string representation of this ConstructorCallInfo
   */
  @Override
  public String toString() {
    return toParseableString();
  }

  /**
   * appendCode adds code for a constructor call to the given {@link StringBuilder}.
   * 
   * @param inputVars a list of variables representing the actual arguments for the constructor call.
   * @param b the StringBuilder to which the output is appended.
   * @see Operation#appendCode(List, StringBuilder)
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
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
    String declaringClassStr = TypeNames.getCompilableName(declaringClass);

    b.append((isNonStaticMember ? inputVars.get(0) + "." : "")
        + "new "
        + (isNonStaticMember ? declaringClass.getSimpleName() : declaringClassStr)
        + "(");
    for (int i = (isNonStaticMember ? 1 : 0) ; i < inputVars.size() ; i++) {
      if (i > (isNonStaticMember ? 1 : 0))
        b.append(", ");

      // We cast whenever the variable and input types are not identical.
      if (!inputVars.get(i).getType().equals(getInputTypes().get(i)))
        b.append("(" + getInputTypes().get(i).getCanonicalName() + ")");
      
      // In the short output format, statements that assign to a primitive
      // or string literal, like "int x = 3" are not added to a sequence;
      // instead, the value (e.g. "3") is inserted directly added as
      // arguments to method calls.
      Statement statementCreatingVar = inputVars.get(i).getDeclaringStatement(); 
      String shortForm = statementCreatingVar.getShortForm();
      if (!GenInputsAbstract.long_format && shortForm != null) {
        b.append(shortForm);
      } else {
        b.append(inputVars.get(i).getName());
      }
    }
    b.append(")");
    
  }

  /**
   * equals tests whether the parameter is a call to the same constructor.
   * 
   * @param o an object
   * @return true if o is a ConstructorCall referring to same constructor object; false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (this == o)
      return true;
    if (!(o instanceof ConstructorCall))
      return false;
    ConstructorCall other = (ConstructorCall) o;
    return this.constructor.equals(other.constructor);
  }

  /**
   * hashCode returns the hashCode for the constructor called by this object.
   */
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

  /**
   * execute performs call to the constructor given the objects as actual parameters, 
   * and the output stream for any output.
   * 
   * @param statementInput is an array of values corresponding to signature of the constructor.
   * @param out is a stream for any output.
   * @see Operation#execute(Object[], PrintStream)
   */
  @Override
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
  @Override
  public List<Class<?>> getInputTypes() {
    if (inputTypesCached == null) {
      inputTypesCached = new ArrayList<Class<?>>(Arrays.asList(constructor.getParameterTypes()));
    }
    return inputTypesCached;
  }

  /**
   * Returns the return type of this constructor.
   */
  @Override
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
   * java.util.ArrayList.&lt;init&gt;()
   * java.util.ArrayList.&lt;init&gt;(java.util.Collection)
   *
   */
  public String toParseableString() {
    return Reflection.getSignature(constructor);
  }

  public static Operation parse(String s) {
    return ConstructorCall.getRConstructor(Reflection.getConstructorForSignature(s));
  }

  /**
   * getDeclaringClass returns the class that declared the enclosed constructor.
   * @return class object representing declaring class for the constructor.
   */
  @Override
  public Class<?> getDeclaringClass() {
    return constructor.getDeclaringClass();
  }

  /**
   * isConstructorCall is a predicate to indicate that this {@link Operation} is 
   * a {@link ConstructorCall} as opposed to another form of operation.
   * @see Operation#isConstructorCall()
   */
  @Override
  public boolean isConstructorCall() {
    return true;
  }

}
