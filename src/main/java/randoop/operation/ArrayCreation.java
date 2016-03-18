package randoop.operation;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ConcreteArrayType;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;
import randoop.types.TypeNames;

/**
 * ArrayCreation is an {@link Operation} representing the construction of a
 * one-dimensional array with a given element type and length. The The
 * ArrayCreation operation requires a list of elements in an initializer. For
 * instance, <code>new int[2]</code> is the {@code ArrayCreation} in the
 * initialization<br>
 * <code>int[] x = new int[2] { 3, 7 };</code><br>
 * with the initializer list as inputs.
 * <p>
 * In terms of the notation used for the {@link Operation} class, a creation of
 * an array of elements of type <i>e</i> with length <i>n</i> has a signature [
 * <i>e,...,e</i>] &rarr; <i>t</i>, where [<i>e,...,e</i>] is a list of length
 * <i>n</i>, and <i>t</i> is the array type.
 * <p>
 * ArrayCreation objects are immutable.
 */
public final class ArrayCreation extends ConcreteOperation implements Operation {

  /** ID for parsing purposes (see StatementKinds.parse method) */
  public static final String ID = "array";

  // State variables.
  private final int length;
  private final ConcreteType elementType;

  /**
   * Creates an object representing the construction of an array that holds
   * values of the element type and has the given length.
   *
   * @param length
   *          number of objects allowed in the array
   * @param arrayType  the type of array this operation creates
   * @param inputTypes  the input types to create the array
   */
  public ArrayCreation(int length, ConcreteArrayType arrayType, ConcreteTypeTuple inputTypes) {
    super(arrayType, inputTypes, arrayType);

    assert length < 0 : "array length may not be negative";

    this.elementType = arrayType.getElementType();
    this.length = length;
  }

  /**
   * @return the type of the elements held in the created array
   */
  public ConcreteType getElementType() {
    return this.elementType;
  }

  /**
   * Returns the length of created array.
   *
   * @return length of array created by this object
   */
  public int getLength() {
    return this.length;
  }


  /**
   * {@inheritDoc}
   *
   * @return {@link NormalExecution} object containing constructed array.
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    if (statementInput.length > length) {
      String msg = "Too many arguments:" + statementInput.length + " capacity:" + length;
      throw new IllegalArgumentException(msg);
    }
    long startTime = System.currentTimeMillis();
    assert statementInput.length == this.length;
    Object theArray = Array.newInstance(this.elementType.getRuntimeClass(), this.length);
    for (int i = 0; i < statementInput.length; i++) Array.set(theArray, i, statementInput[i]);
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
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    if (inputVars.size() > length) {
      String msg = "Too many arguments:" + inputVars.size() + " capacity:" + length;
      throw new IllegalArgumentException(msg);
    }

    String arrayTypeName = this.elementType.getName();

    b.append("new ").append(arrayTypeName).append("[] { ");
    for (int i = 0; i < inputVars.size(); i++) {
      if (i > 0) b.append(", ");

      String param = inputVars.get(i).getName();

      // In the short output format, statements like "int x = 3" are not added
      // to a sequence; instead, the value (e.g. "3") is inserted directly
      // as arguments to method calls.
      Statement statementCreatingVar = inputVars.get(i).getDeclaringStatement();
      if (statementCreatingVar.isPrimitiveInitialization()
          && !statementCreatingVar.isNullInitialization()) {
        String shortForm = statementCreatingVar.getShortForm();
        if (shortForm != null) {
          param = shortForm;
        }
      }
      b.append(param);
    }
    b.append(" }");
  }

  @Override
  public int hashCode() {
    return Objects.hash(elementType, length);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ArrayCreation)) return false;
    if (this == o) return true;
    ArrayCreation otherArrayDecl = (ArrayCreation) o;
    if (!this.elementType.equals(otherArrayDecl.elementType)) return false;
    if (this.length != otherArrayDecl.length) return false;
    return true;
  }

  /**
   * {@inheritDoc} Creates string of the form TYPE[NUMELEMS] where TYPE is the
   * type of the array, and NUMELEMS is the number of elements.
   *
   * Example: int[3]
   *
   * @return string descriptor for array creation.
   */
  @Override
  public String toParseableString() {
    return elementType.getName() + "[" + Integer.toString(length) + "]";
  }

  /**
   * Parses an array declaration in a string descriptor in the form generated by
   * {@link ArrayCreation#toParseableString()}.
   *
   * @see OperationParser#parse(String)
   *
   * @param str
   *          the string to be parsed for the {@code ArrayCreation}.
   * @return the {@code ArrayCreation} object for the string.
   * @throws OperationParseException
   *           if string does not have expected form.
   */
  public static Operation parse(String str) throws OperationParseException {
    int openBr = str.indexOf('[');
    int closeBr = str.indexOf(']');
    String elementTypeStr = str.substring(0, openBr);
    String lengthStr = str.substring(openBr + 1, closeBr);

    Class<?> elementType;
    try {
      elementType = TypeNames.getTypeForName(elementTypeStr);
    } catch (ClassNotFoundException e) {
      throw new OperationParseException("Type not found for array element type " + elementTypeStr);
    }

    int length = Integer.parseInt(lengthStr);
//    return new ArrayCreation(elementType, length);
    return null;
  }

}
