package randoop.operation;

import java.io.PrintStream;
import java.util.List;

import randoop.BugInRandoopException;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.field.AccessibleField;
import randoop.field.FieldParser;
import randoop.field.FinalInstanceField;
import randoop.field.StaticFinalField;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ConcreteType;

/**
 * FieldSetter is an adapter for a {@link AccessibleField} as a
 * {@link Operation} that acts like a setter for the field.
 *
 * @see AccessibleField
 */
public class FieldSet extends ConcreteOperation implements Operation {

  public static String ID = "setter";

  private AccessibleField field;

  /**
   * Creates a setter {@link Operation} object for a field of a class. Throws an
   * exception if the field is static final.
   *
   * @param field
   *          the field object to be set by setter statements.
   * @throws IllegalArgumentException
   *           if field is static final.
   */
  public FieldSet(AccessibleField field) {
    super(field.getDeclaringType(), field.getSetTypes(), ConcreteType.forClass(void.class));
    if (field instanceof StaticFinalField) {
      throw new IllegalArgumentException("Field may not be static final for FieldSetter");
    }
    if (field instanceof FinalInstanceField) {
      throw new IllegalArgumentException("Field may not be final for FieldSetter");
    }
    this.field = field;
  }

  /**
   * Sets the value of the field given the inputs. Should the action raise an
   * exception, those are captured and returned as an {@link ExecutionOutcome}.
   * Exceptions should only be {@link NullPointerException}, which happens when
   * input is null but field is an instance field.
   * {@link AccessibleField#getValue(Object)} suppresses exceptions that occur
   * because the field is not valid or accessible (specifically
   * {@link IllegalArgumentException} and {@link IllegalAccessException}).
   *
   * @param statementInput
   *          the inputs for statement.
   * @param out
   *          the stream for printing output (unused).
   * @return outcome of access, either void normal execution or captured
   *         exception.
   * @throws BugInRandoopException
   *           if field access throws bug exception.
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    assert statementInput.length == getInputTypes().size()
        : "expected " + getInputTypes().size() + " got " + statementInput.length;

    Object instance = null;
    Object input = statementInput[0];
    if (statementInput.length == 2) {
      instance = statementInput[0];
      input = statementInput[1];
    }

    try {
      field.setValue(instance, input);
    } catch (BugInRandoopException e) {
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
   * @param inputVars
   *          the list of input variables. Last element is value to assign. If
   *          an instance field, first is instance, second is value.
   * @param b
   *          the StringBuilder to which code is issued.
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    assert inputVars.size() == 1 || inputVars.size() == 2;

    b.append(field.toCode(inputVars));
    b.append(" = ");

    // variable/value to be assigned is either only or second entry in list
    int index = inputVars.size() - 1;

    // TODO this is duplicate code from RMethod - should factor out behavior
    String rhs = inputVars.get(index).getName();
    Statement statementCreatingVar = inputVars.get(index).getDeclaringStatement();

    String shortForm = statementCreatingVar.getShortForm();
    if (shortForm != null) {
      rhs = shortForm;
    }

    b.append(rhs);
  }

  /**
   * Returns the string descriptor for field that can be parsed by
   * {@link FieldParser}.
   *
   * @return the parseable string descriptor for this setter.
   */
  @Override
  public String toParseableString() {
    return "<set>(" + field.toParseableString() + ")";
  }

  @Override
  public String toString() {
    return toParseableString();
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

  /**
   * Parses a description of a field setter in the given string. A setter
   * description has the form "&lt;set&gt;( field-descriptor )" where
   * "&lt;set&gt;" is literally what is expected.
   *
   * @param descr
   *          string containing descriptor of field setter.
   * @return {@code FieldSetter} object corresponding to setter descriptor.
   * @throws OperationParseException
   *           if descr does not have expected form.
   * @see FieldParser#parse(String)
   */
  public static FieldSet parse(String descr) throws OperationParseException {
    int parPos = descr.indexOf('(');
    String errorPrefix = "Error parsing " + descr + " as description for field getter statement: ";
    if (parPos < 0) {
      String msg = errorPrefix + " expecting parentheses.";
      throw new OperationParseException(msg);
    }
    String prefix = descr.substring(0, parPos);
    if (!prefix.equals("<set>")) {
      String msg = errorPrefix + " expecting <set>( <field-descriptor> ).";
      throw new OperationParseException(msg);
    }
    int lastParPos = descr.lastIndexOf(')');
    if (lastParPos < 0) {
      String msg = errorPrefix + " no closing parentheses found.";
      throw new OperationParseException(msg);
    }
    String fieldDescriptor = descr.substring(parPos + 1, lastParPos);
    AccessibleField pf = (new FieldParser()).parse(fieldDescriptor);
    return new FieldSet(pf);
  }

  @Override
  public boolean isStatic() {
    return field.isStatic();
  }

  /**
   * A FieldSetter is a method call because it acts like a setter.
   */
  @Override
  public boolean isMessage() {
    return true;
  }

  /**
   * Determines whether enclosed {@link java.lang.reflect.Field Field} satisfies
   * the given predicate.
   *
   * @param predicate
   *          the {@link ReflectionPredicate} to be checked.
   * @return true only if the field used in this setter satisfies
   *         predicate.canUse.
   */
  @Override
  public boolean satisfies(ReflectionPredicate predicate) {
    return field.satisfies(predicate);
  }
}
