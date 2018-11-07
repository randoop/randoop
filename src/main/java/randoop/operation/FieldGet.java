package randoop.operation;

import java.io.PrintStream;
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
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * FieldGetter is an adapter that creates a {@link Operation} from a {@link AccessibleField} and
 * behaves like a getter for the field.
 *
 * @see AccessibleField
 */
public class FieldGet extends CallableOperation {

  private AccessibleField field;

  /**
   * FieldGetter sets the public field for the getter statement.
   *
   * @param field the {@link AccessibleField} object from which to get values
   */
  public FieldGet(AccessibleField field) {
    this.field = field;
  }

  /**
   * Performs computation of getting value of field or capturing thrown exceptions. Exceptions
   * should only be NullPointerException, which happens when input is null but field is an instance
   * field. {@link AccessibleField#getValue(Object)} suppresses exceptions that occur because field
   * is not valid or accessible.
   *
   * @param statementInput the inputs for statement
   * @param out the stream for printing output (unused)
   * @return outcome of access
   * @throws RandoopBug if field access throws bug exception
   * @throws SequenceExecutionException if field access has a type exception
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {

    // either 0 or 1 inputs. If none use null, otherwise give object.
    Object input = statementInput.length == 0 ? null : statementInput[0];

    try {

      Object value = field.getValue(input);
      return new NormalExecution(value, 0);

    } catch (RandoopBug | SequenceExecutionException e) {
      throw e;
    } catch (Throwable thrown) {
      return new ExceptionalExecution(thrown, 0);
    }
  }

  /**
   * Adds the text for an initialization of a variable from a field to the StringBuilder.
   *
   * @param inputVars the list of variables to be used (ignored)
   * @param b the StringBuilder that strings are appended to
   */
  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder b) {
    b.append(field.toCode(declaringType, inputVars));
  }

  /** Returns string descriptor for field that can be parsed by PublicFieldParser. */
  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    return declaringType.getName() + ".<get>(" + field.getName() + ")";
  }

  @Override
  public String toString() {
    return field.toString();
  }

  @Override
  public String getName() {
    return "<get>(" + field.getName() + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FieldGet) {
      FieldGet s = (FieldGet) obj;
      return field.equals(s.field);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return field.hashCode();
  }

  /**
   * Parses a getter for a field from a string. A getter description has the form "{@code <get>(
   * field-descriptor )}" where "{@code <get>}" is literal ("{@code <}" and "{@code >}" included),
   * and field-descriptor is as recognized by
   *
   * @param descr the string containing descriptor of getter for a field
   * @return the getter operation for the given string descriptor
   * @throws OperationParseException if any error in descriptor string
   */
  @SuppressWarnings("signature") // parsing
  public static TypedOperation parse(String descr) throws OperationParseException {
    String errorPrefix = "Error parsing " + descr + " as description for field getter statement: ";

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
    assert opname.equals("<get>") : "expecting <get>, saw " + opname;

    if (closeParPos < 0) {
      String msg = errorPrefix + " no closing parentheses found.";
      throw new OperationParseException(msg);
    }
    String fieldname = descr.substring(openParPos + 1, closeParPos);

    AccessibleField accessibleField = FieldParser.parse(descr, classname, fieldname);
    ClassOrInterfaceType classType = accessibleField.getDeclaringType();
    Type fieldType = Type.forType(accessibleField.getRawField().getGenericType());

    List<Type> getInputTypeList = new ArrayList<>();
    if (!accessibleField.isStatic()) {
      getInputTypeList.add(classType);
    }
    return new TypedClassOperation(
        new FieldGet(accessibleField), classType, new TypeTuple(getInputTypeList), fieldType);
  }

  @Override
  public boolean isStatic() {
    return field.isStatic();
  }

  @Override
  public boolean isConstantField() {
    return field.isStatic() && field.isFinal();
  }

  /**
   * {@inheritDoc}
   *
   * @return true, always
   */
  @Override
  public boolean isMessage() {
    return true;
  }

  /**
   * Determines whether enclosed {@link java.lang.reflect.Field} satisfies the given predicate.
   *
   * @param reflectionPredicate the {@link ReflectionPredicate} to be checked
   * @return true only if the field used in this getter satisfies predicate.canUse
   */
  @Override
  public boolean satisfies(ReflectionPredicate reflectionPredicate) {
    return field.satisfies(reflectionPredicate);
  }
}
