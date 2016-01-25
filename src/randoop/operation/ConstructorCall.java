package randoop.operation;

import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
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
import randoop.types.TypeNames;
import randoop.util.ConstructorReflectionCode;
import randoop.util.ReflectionExecutor;
import randoop.util.Util;

/**
 * ConstructorCall is an {@link Operation} that represents a call to a 
 * constructor, and holds a reference to a reflective 
 * {@link java.lang.reflect.Constructor} object.  
 * 
 * As an {@link Operation}, a call to constructor <i>c</i> with <i>n</i> 
 * arguments is represented as 
 * <i>c</i> : [<i>t1,...,tn</i>] &rarr; <i>c</i>, 
 * where the output type <i>c</i> is also the name of the class. 
 */
public final class ConstructorCall extends AbstractOperation implements Operation, Serializable {

  private static final long serialVersionUID = 20100429; 

  /** 
   * ID for parsing purposes. 
   * @see OperationParser#getId(Operation)
   */
  public static final String ID = "cons";

  private final Constructor<?> constructor;

  // Cached values (for improved performance). Their values
  // are computed upon the first invocation of the respective
  // getter method.
  private List<Class<?>> inputTypesCached;
  private Class<?> outputType;
  private int hashCodeCached = 0;
  private boolean hashCodeComputed = false;

  /**
   * Converts this object to a form that can be serialized.
   * 
   * @return serializable form of this object
   * @see SerializableConstructorCall
   */
  private Object writeReplace() throws ObjectStreamException {
    return new SerializableConstructorCall(constructor);
  }

  /** 
   * Creates object corresponding to the given reflection constructor.
   * @param constructor reflective object for a constructor.
   */
  public ConstructorCall(Constructor<?> constructor) {
    if (constructor == null)
      throw new IllegalArgumentException("constructor should not be null.");
    this.constructor = constructor;
    this.outputType = constructor.getDeclaringClass();
    // TODO move this earlier in the process: check first that all
    // methods to be used can be made accessible.
    // XXX this should not be here but I get infinite loop when comment out
    this.constructor.setAccessible(true);
  }

  /**
   * Return the reflective constructor corresponding to this ConstructorCall.
   * 
   * @return {@link Constructor} object called by this constructor call.
   */
  public Constructor<?> getConstructor() {
    return this.constructor;
  }

  /**
   * Creates the {@code ConstructorCall} corresponding to the given reflection 
   * constructor.
   * 
   * @param constructor  the {@link Constructor} object for calls
   * @return a new {@code ConstructorCall} object for the given {@code Constructor} instance.
   */
  public static ConstructorCall createConstructorCall(Constructor<?> constructor) {
    return new ConstructorCall(constructor);
  }

  /**
   * Returns concise string representation of this ConstructorCall.
   */
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(constructor.getName());
    b.append("(");
    Class<?>[] types = constructor.getParameterTypes();
    if (types.length > 0) {
      b.append(types[0].getName());
      for (int i = 1; i < types.length; i++) {
        b.append(", " + types[i].getName());
      }
    }
    b.append(")");
    return b.toString();
  }

  /**
   * Adds code for a constructor call to the given {@link StringBuilder}.
   * 
   * @param inputVars  a list of variables representing the actual arguments 
   *                   for the constructor call.
   * @param b  the StringBuilder to which the output is appended.
   * @see Operation#appendCode(List, StringBuilder)
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    assert inputVars.size() == this.getInputTypes().size();

    Class<?> declaringClass = constructor.getDeclaringClass();
    boolean isNonStaticMember = !Modifier.isStatic(declaringClass.getModifiers()) 
        && declaringClass.isMemberClass();
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
      
      String param = inputVars.get(i).getName();
      
      // In the short output format, statements that assign to a primitive
      // or string literal, like "int x = 3" are not added to a sequence;
      // instead, the value (e.g. "3") is inserted directly added as
      // arguments to method calls.
      Statement statementCreatingVar = inputVars.get(i).getDeclaringStatement(); 
      if (!GenInputsAbstract.long_format) {
        String shortForm = statementCreatingVar.getShortForm();
        if (shortForm != null) {
          param = shortForm;
        }
      }
      b.append(param);
    }
    b.append(")");
    
  }

  /**
   * Tests whether the parameter is a call to the same constructor.
   * 
   * @param o  an object
   * @return true if o is a ConstructorCall referring to same constructor object; false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof ConstructorCall) {
      if (this == o)
        return true;
    
      ConstructorCall other = (ConstructorCall) o;
      return this.constructor.equals(other.constructor);
    }
    return false;
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
   * {@inheritDoc}
   * Performs call to the constructor given the objects as actual parameters, 
   * and the output stream for any output.
   * 
   * @param statementInput  is an array of values corresponding to signature of the constructor.
   * @param out  is a stream for any output.
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
   * {@inheritDoc}
   * @return list of parameter types for constructor.
   */
  @Override
  public List<Class<?>> getInputTypes() {
    if (inputTypesCached == null) {
      inputTypesCached = new ArrayList<Class<?>>(Arrays.asList(constructor.getParameterTypes()));
    }
    return inputTypesCached;
  }

  /**
   * {@inheritDoc}
   * @return type of the object created (i.e., class for constructor).
   */
  @Override
  public Class<?> getOutputType() {
    return outputType;
  }

  /**
   * {@inheritDoc}
   * Generates a string representation of the constructor signature.
   *
   * Examples:
   * <pre><code>
   *  java.util.ArrayList.&lt;init&gt;()
   *  java.util.ArrayList.&lt;init&gt;(java.util.Collection)
   * </code></pre>
   * @see ConstructorSignatures#getSignatureString(Constructor)
   *
   * @return signature string for constructor.
   */
  @Override
  public String toParseableString() {
    return ConstructorSignatures.getSignatureString(constructor);
  }

  /**
   * Parse a constructor call in a string with the format generated by
   * {@link ConstructorCall#toParseableString()} and returns the corresponding
   * {@link ConstructorCall} object.
   * @see OperationParser#parse(String)
   * 
   * @param s a string descriptor of a constructor call.
   * @return {@link ConstructorCall} object corresponding to the given signature.
   * @throws OperationParseException if no constructor found for signature. 
   */
  public static Operation parse(String s) throws OperationParseException {
    return ConstructorCall.createConstructorCall(ConstructorSignatures.getConstructorForSignatureString(s));
  }

  /**
   * {@inheritDoc}
   * @return class object representing declaring class for the constructor.
   */
  @Override
  public Class<?> getDeclaringClass() {
    return constructor.getDeclaringClass();
  }

  /**
   * {@inheritDoc}
   * @return true, because this is a {@link ConstructorCall}.
   */
  @Override
  public boolean isConstructorCall() {
    return true;
  }

  /**
   * {@inheritDoc}
   * Determines whether enclosed {@link Constructor} satisfies the given 
   * predicate.
   * 
   * @return true only if the constructor in this object satisfies the 
   * {@link ReflectionPredicate#test(Constructor)} implemented by predicate.
   */
  @Override
  public boolean satisfies(ReflectionPredicate predicate) {
    return predicate.test(constructor);
  }
}
