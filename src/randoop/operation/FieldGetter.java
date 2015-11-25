package randoop.operation;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

import randoop.BugInRandoopException;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Variable;

/**
 * FieldGetter is an adapter that creates a {@link Operation} from
 * a {@link PublicField} and behaves like a getter for the field.
 * 
 * @see PublicField
 *
 */
public class FieldGetter extends AbstractOperation implements Operation,Serializable {

  private static final long serialVersionUID = 3966201727170073093L;
  
  public static String ID = "getter";
  
  private PublicField field;

  /**
   * FieldGetter sets the public field for the getter statement.
   * 
   * @param field - PublicField object from which to get values.
   */
  public FieldGetter(PublicField field) {
    this.field = field;
  }

  /**
   * getInputTypes returns the types required to access the field.
   * @return singleton list if field is instance field, empty if static
   */
  @Override
  public List<Class<?>> getInputTypes() {
    return field.getAccessTypes();
  }

  /**
   * getOutputType returns the type of the field
   */
  @Override
  public Class<?> getOutputType() {
    return field.getType();
  }

  /**
   * execute performs computation – getting value of field or capturing thrown exceptions.
   * Exceptions should only be NullPointerException, which happens when input is null but 
   * field is an instance field. {@link PublicField#getValue(Object)} suppresses exceptions
   * that occur because field is not valid or accessible.
   * 
   * @param statementInput - inputs for statement.
   * @param out - stream for printing output (unused).
   * @return outcome of access.
   * @throws BugInRandoopException if field access throws bug exception.
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    assert statementInput.length == getInputTypes().size();
    
    //either 0 or 1 inputs. If none use null, otherwise give object.
    Object input = statementInput.length == 0 ? null : statementInput[0];
    
    try {
      
      Object value = field.getValue(input);
      return new NormalExecution(value,0);
      
    } catch (BugInRandoopException e) {
      throw e;
    } catch (Throwable thrown) {
      return new ExceptionalExecution(thrown,0);
    }
    
  }

  /**
   * appendCode adds the text for an initialization of a variable from a field to 
   * the StringBuilder.
   * @param inputVars - list of variables to be used (ignored).
   * @param b - StringBuilder that strings are appended to.
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    b.append(field.toCode(inputVars));
  }

  /**
   * toParseableString returns string descriptor for field that can be parsed by
   * PublicFieldParser.
   */
  @Override
  public String toParseableString() {
    return "<get>(" + field.toParseableString() + ")";
  }

  @Override
  public String toString() {
    return toParseableString();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FieldGetter) {
      FieldGetter s = (FieldGetter)obj;
      return field.equals(s.field);
    }
    return false;
  }
  
  @Override
  public int hashCode() { return field.hashCode(); }

  /**
   * Parses a getter for a field in a string.
   * A getter description has the form "&lt;get&gt;( field-descriptor )"
   * where &lt;get&gt;" is literal ("&lt;" and "&gt;" included), and field-descriptor
   * is as recognized by {@link PublicFieldParser#parse(String)}.
   * @see PublicFieldParser#parse(String)
   * 
   * @param descr  the string containing descriptor of getter for a field.
   * @return the getter object in string.
   * @throws OperationParseException if any error in descriptor string
   */
  public static FieldGetter parse(String descr) throws OperationParseException {
    int parPos = descr.indexOf('(');
    String errorPrefix = "Error parsing " + descr + " as description for field getter statement: ";
    if (parPos < 0) {
      String msg = errorPrefix + " expecting parentheses.";
      throw new OperationParseException(msg);
    }
    String prefix = descr.substring(0, parPos);
    if (!prefix.equals("<get>")) {
      String msg = errorPrefix + " expecting <get>( <field-descriptor> ).";
      throw new OperationParseException(msg);
    }
    int lastParPos = descr.lastIndexOf(')');
    if (lastParPos < 0) {
      String msg = errorPrefix + " no closing parentheses found.";
      throw new OperationParseException(msg);
    }
    String fieldDescriptor = descr.substring(parPos + 1, lastParPos);
    PublicField pf = (new PublicFieldParser()).parse(fieldDescriptor);
    return new FieldGetter(pf);
  }

  @Override
  public boolean isStatic() {
    return field.isStatic();
  }
 
  /**
   * {@inheritDoc}
   * @return true, always.
   */
  @Override
  public boolean isMessage() {
    return true;
  }
  
  @Override
  public Class<?> getDeclaringClass() {
    return field.getDeclaringClass();
  }
  
  /**
   * Determines whether enclosed {@link java.lang.reflect.Field} satisfies the 
   * given predicate.
   * 
   * @param predicate the {@link ReflectionPredicate} to be checked.
   * @return true only if the field used in this getter satisfies predicate.canUse.
   */
  @Override
  public boolean satisfies(ReflectionPredicate predicate) {
    return field.satisfies(predicate);
  }
}
