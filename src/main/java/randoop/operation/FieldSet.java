package randoop.operation;

import java.util.ArrayList;
import java.util.List;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.field.AccessibleField;
import randoop.field.FieldParser;
import randoop.main.RandoopBug;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.SequenceExecutionException;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * FieldSetter is an adapter for a {@link AccessibleField} as a {@link Operation} that acts like a
 * setter for the field.
 *
 * @see AccessibleField
 */
public class FieldSet extends CallableOperation {

  private AccessibleField field;

  /**
   * Creates a setter {@link Operation} object for a field of a class. Throws an exception if the
   * field is static final.
   *
   * @param field the field object to be set by setter statements
   * @throws IllegalArgumentException if field is static final
   */
  public FieldSet(AccessibleField field) {
    if (field.isFinal()) {
      throw new IllegalArgumentException("Field may not be final for FieldSet");
    }
    this.field = field;
  }

  /**
   * Sets the value of the field given the inputs. Should the action raise an exception, those are
   * captured and returned as an {@link ExecutionOutcome}. Exceptions should only be {@link
   * NullPointerException}, which happens when input is null but field is an instance field. {@link
   * AccessibleField#getValue(Object)} suppresses exceptions that occur because the field is not
   * valid or accessible (specifically {@link IllegalArgumentException} and {@link
   * IllegalAccessException}).
   *
   * @param statementInput the inputs for statement
   * @return outcome of access, either void normal execution or captured exception
   * @throws RandoopBug if field access throws bug exception
   * @throws SequenceExecutionException if field access has type exception
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput) {

    Object instance = null;
    Object input = statementInput[0];
    if (statementInput.length == 2) {
      instance = statementInput[0];
      input = statementInput[1];
    }

    try {
      field.setValue(instance, input);
    } catch (RandoopBug | SequenceExecutionException e) {
      throw e;
    } catch (Throwable thrown) {
      return new ExceptionalExecution(thrown, 0);
    }

    return new NormalExecution(null, 0);
  }

  /**
   * Generates code for setting a field. Should look like
   *
   * <pre>
   * field = value;
   * </pre>
   *
   * or
   *
   * <pre>
   * field = variable;
   * </pre>
   *
   * @param inputVars the list of input variables. Last element is value to assign. If an instance
   *     field, first is instance, second is value.
   * @param b the StringBuilder to which code is issued
   */
  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder b) {

    b.append(field.toCode(declaringType, inputVars));
    b.append(" = ");

    // variable/value to be assigned is either only or second entry in list
    int index = inputVars.size() - 1;
    String rhs = getArgumentString(inputVars.get(index));
    b.append(rhs);
  }

  /**
   * Returns the string descriptor for field that can be parsed by.
   *
   * @return the parsable string descriptor for this setter
   */
  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    return declaringType.getName() + ".<set>(" + field.getName() + ")";
  }

  /**
   * Parses a description of a field setter in the given string. A setter description has the form
   * "{@code <set>( field-descriptor )}" where "{@code <set>}" is literally what is expected.
   *
   * @param descr string containing descriptor of field setter
   * @return the field setter for the given string descriptor
   * @throws OperationParseException if descr does not have expected form
   */
  @SuppressWarnings("signature") // parsing
  public static TypedOperation parse(String descr) throws OperationParseException {
    String errorPrefix = "Error parsing " + descr + " as description for field set statement: ";

    int openParPos = descr.indexOf('(');
    int closeParPos = descr.indexOf(')');

    if (openParPos < 0) {
      String msg = errorPrefix + " expecting parentheses.";
      throw new OperationParseException(msg);
    }
    String prefix = descr.substring(0, openParPos);
    int lastDotPos = prefix.lastIndexOf('.');
    assert lastDotPos > 0 : "should be a period after the classname: " + descr;

    String classname = prefix.substring(0, lastDotPos);
    String opname = prefix.substring(lastDotPos + 1);
    assert opname.equals("<set>") : "expecting <set>, saw " + opname;
    assert (closeParPos > 0) : "no closing parentheses found.";

    String fieldname = descr.substring(openParPos + 1, closeParPos);

    AccessibleField accessibleField = FieldParser.parse(descr, classname, fieldname);
    ClassOrInterfaceType classType = accessibleField.getDeclaringType();
    Type fieldType = Type.forType(accessibleField.getRawField().getGenericType());

    if (accessibleField.isFinal()) {
      throw new OperationParseException(
          "Cannot create setter for final field " + classname + "." + opname);
    }
    List<Type> setInputTypeList = new ArrayList<>();
    if (!accessibleField.isStatic()) {
      setInputTypeList.add(classType);
    }
    setInputTypeList.add(fieldType);
    return new TypedClassOperation(
        new FieldSet(accessibleField),
        classType,
        new TypeTuple(setInputTypeList),
        JavaTypes.VOID_TYPE);
  }

  @Override
  public String toString() {
    return field.toString();
  }

  @Override
  public String getName() {
    return "<set>(" + field.getName() + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FieldSet) {
      FieldSet s = (FieldSet) obj;
      return field.equals(s.field);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return field.hashCode();
  }

  @Override
  public boolean isStatic() {
    return field.isStatic();
  }

  /** A FieldSetter is a method call because it acts like a setter. */
  @Override
  public boolean isMessage() {
    return true;
  }

  /**
   * Determines whether enclosed {@link java.lang.reflect.Field Field} satisfies the given
   * predicate.
   *
   * @param reflectionPredicate the {@link ReflectionPredicate} to be checked
   * @return true only if the field used in this setter satisfies predicate.canUse
   */
  @Override
  public boolean satisfies(ReflectionPredicate reflectionPredicate) {
    return field.satisfies(reflectionPredicate);
  }
}
