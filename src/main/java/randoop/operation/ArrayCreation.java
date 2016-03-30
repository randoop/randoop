package randoop.operation;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.reflection.TypedOperationManager;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ConcreteArrayType;
import randoop.types.ConcreteType;
import randoop.types.GeneralType;
import randoop.types.GeneralTypeTuple;
import randoop.types.GenericTypeTuple;

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
public final class ArrayCreation extends CallableOperation {

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
   */
  ArrayCreation(ConcreteArrayType arrayType, int length) {
    assert length < 0 : "array length may not be negative";

    this.elementType = arrayType.getElementType();
    this.length = length;
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
    return elementType.getName() + "[" + length + "]";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void appendCode(GeneralType declaringType, GeneralTypeTuple inputTypes, GeneralType outputType, List<Variable> inputVars, StringBuilder b) {
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
    return this.elementType.equals(otherArrayDecl.elementType)
            && this.length == otherArrayDecl.length;
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
  public String toParseableString(GeneralType declaringType, GeneralTypeTuple inputTypes, GeneralType outputType) {
    return elementType.getName() + "[" + Integer.toString(length) + "]";
  }

  /**
   * Parses an array declaration in a string descriptor in the form generated by
   * {@link ArrayCreation#toParseableString(GeneralType,GeneralTypeTuple,GeneralType)}.
   *
   * @see OperationParser#parse(String, randoop.reflection.TypedOperationManager)
   *
   * @param str
   *          the string to be parsed for the {@code ArrayCreation}.
   * @param manager
   *          the {@link TypedOperationManager} to collect operations
   * @throws OperationParseException
   *           if string does not have expected form.
   */
  public static void parse(String str, TypedOperationManager manager) throws OperationParseException {
    int openBr = str.indexOf('[');
    int closeBr = str.indexOf(']');
    String elementTypeName = str.substring(0, openBr);
    String lengthStr = str.substring(openBr + 1, closeBr);

    int length = Integer.parseInt(lengthStr);

    GeneralType elementType;
    try {
      elementType = GeneralType.forName(elementTypeName);
    } catch (ClassNotFoundException e) {
      throw new OperationParseException("Type not found for array element type " + elementTypeName);
    }

    if (elementType.isGeneric()) {
      throw new OperationParseException("Array element type may not be generic " + elementTypeName);
    }

    List<GeneralType> paramTypes = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      paramTypes.add(elementType);
    }
    GenericTypeTuple inputTypes = new GenericTypeTuple(paramTypes);
    ConcreteArrayType arrayType = new ConcreteArrayType((ConcreteType) elementType);
    manager.createTypedOperation(new ArrayCreation(arrayType, length), arrayType, inputTypes, arrayType);
  }
}
