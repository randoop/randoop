package randoop.operation;

import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.TypeNames;

/**
 * ArrayCreation is an {@link Operation} representing the construction of a one-dimensional
 * array such as "int[] x = new int[2] { 3, 7 };"
 * 
 * In terms of the notation used for {@link Operation}, an array creation of an array of elements 
 * of type e and length n has a signature [e,...,e] -> t where [e,...,e] is a list of length n, 
 * and t is the array type.
 * 
 * Objects are immutable.
 */
public final class ArrayCreation extends AbstractOperation implements Operation, Serializable {

  private static final long serialVersionUID = 20100429; 

  /** ID for parsing purposes (see StatementKinds.parse method) */
  public static final String ID = "array";

  // State variables.
  private final int length;
  private final Class<?> elementType;

  // Cached values (for improved performance). Their values
  // are computed upon the first invocation of the respective
  // getter method.
  private List<Class<?>> inputTypesCached;
  private Class<?> outputTypeCached;    
  private int hashCodeCached;
  private boolean hashCodeComputed= false;

  /**
   * ArrayCreation creates an object representing the construction of an array that holds
   * values of the element type and has the given length.
   * 
   * @param elementType type of objects in the array
   * @param length number of objects allowed in the array
   */
  public ArrayCreation(Class<?> elementType, int length) {

    // Check legality of arguments.
    if (elementType == null) throw new IllegalArgumentException("elementType cannot be null.");
    if (length < 0) throw new IllegalArgumentException("arity cannot be less than zero: " + length);

    // Set state variables.
    this.elementType = elementType;
    this.length = length;
    this.outputTypeCached = Array.newInstance(elementType, 0).getClass();
  }

  /*
   * writeReplace is a Serialization method that creates a serializable version of an object.
   */
  private Object writeReplace() throws ObjectStreamException {
    return new SerializableArrayCreation(elementType, length);
  }

  /**
   * Returns the class of type of elements held in this ArrayDeclarationInfo
   */
  public Class<?> getElementType() {
    return this.elementType;
  }
  
  /**
   * Returns the length of elements held in this ArrayDeclarationInfo
   * */
  public int getLength() {
    return this.length;
  }

  /**
   * {@inheritDoc}
   * @return list of element types matching length of created array.
   */
  @Override
  public List<Class<?>> getInputTypes() {
    if (inputTypesCached == null) {
      this.inputTypesCached = new ArrayList<Class<?>>(length);
      for (int i = 0 ; i < length ; i++)
        inputTypesCached.add(elementType);
      inputTypesCached = Collections.unmodifiableList(inputTypesCached);
    }
    return Collections.unmodifiableList(this.inputTypesCached);
  }

  /**
   * {@inheritDoc}
   * @return {@link NormalExecution} object containing constructed array.
   */
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    if (statementInput.length > length)
      throw new IllegalArgumentException("Too many arguments:"
          + statementInput.length + " capacity:" + length);
    long startTime = System.currentTimeMillis();
    assert statementInput.length == this.length;
    Object theArray = Array.newInstance(this.elementType, this.length);
    for (int i = 0; i < statementInput.length; i++)
      Array.set(theArray, i, statementInput[i]);
    long totalTime = System.currentTimeMillis() - startTime;
    return new NormalExecution(theArray, totalTime);
  }

  @Override
  public String toString() {
    return toParseableString();
  }

  public String toStringShort() {
    return toString();
  }

  public String toStringVerbose() {
    return toString();
  }

  /**
   * {@inheritDoc}
   * @return type of created array.
   */
  @Override
  public Class<?> getOutputType() {
    return outputTypeCached;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    if (inputVars.size() > length)
      throw new IllegalArgumentException("Too many arguments:"
          + inputVars.size() + " capacity:" + length);
    
    //TODO fix naming in statement
    String declaringClass = this.elementType.getCanonicalName();
   // String var = Variable.classToVariableName(this.elementType) + "_array" + newVar.index;
      
    b.append("new " + declaringClass + "[] { ");
    for (int i = 0; i < inputVars.size(); i++) {
      if (i > 0)
        b.append(", ");
      
      // In the short output format, statements like "int x = 3" are not added to a sequence; instead,
      // the value (e.g. "3") is inserted directly added as arguments to method calls.
      Statement statementCreatingVar = inputVars.get(i).getDeclaringStatement(); 
      if (!GenInputsAbstract.long_format && 
          statementCreatingVar.isPrimitiveInitialization() &&
          !statementCreatingVar.isNullInitialization()) {
        String shortForm = statementCreatingVar.getShortForm();
        if (shortForm != null) {
          b.append(shortForm);
        }
      } else {
        b.append(inputVars.get(i).getName());
      }
    }
    b.append(" }");
  }

  @Override
  public int hashCode() {
    if (!hashCodeComputed) {
      hashCodeComputed = true;
      hashCodeCached = this.elementType.hashCode();
      hashCodeCached += this.length * 17;
    }
    return hashCodeCached;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ArrayCreation))
      return false;
    if (this == o)
      return true;
    ArrayCreation otherArrayDecl = (ArrayCreation) o;
    if (!this.elementType.equals(otherArrayDecl.elementType))
      return false;
    if (this.length != otherArrayDecl.length)
      return false;
    return true;
  }

  /**
   * {@inheritDoc}
   * Creates string of the form
   *   TYPE[NUMELEMS]
   * where TYPE is the type of the array, and NUMELEMS is the number of elements.
   * 
   * Example:
   *   int[3]
   *   
   * @return string descriptor for array creation.
   */
  @Override
  public String toParseableString() {
    return elementType.getName() + "[" + Integer.toString(length) + "]";
  }

  /**
   * parse recognizes an array declaration in a string descriptor in the form generated
   * by {@link ArrayCreation#toParseableString()}.
   * 
   * @see OperationParser#parse(String)
   * @throws OperationParseException 
   */
  public static Operation parse(String str) throws OperationParseException {
    int openBr = str.indexOf('[');
    int closeBr = str.indexOf(']');
    String elementTypeStr = str.substring(0, openBr);
    String lengthStr = str.substring(openBr + 1, closeBr);
    try {
    Class<?> elementType = TypeNames.recognizeType(elementTypeStr);
    int length = Integer.parseInt(lengthStr);
    return new ArrayCreation(elementType, length);
    } catch (ClassNotFoundException e) {
      throw new OperationParseException("Type not found for array element type " + elementTypeStr);
    }
  }

  @Override
  public Class<?> getDeclaringClass() {
    return getOutputType();
  }
  
}
