package randoop;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

import randoop.util.Reflection;

/**
 * FieldGetter is an adapter that creates a {@link Operation} from
 * a {@link PublicField} and behaves like a getter for the field.
 * 
 * @see PublicField
 * 
 * @author bjkeller
 *
 */
public class FieldGetter implements Operation,Serializable {

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
   * @param newVar - variable to be initialized.
   * @param inputVars - list of variables to be used (ignored).
   * @param b - StringBuilder that strings are appended to.
   */
  @Override
  public void appendCode(Variable newVar, List<Variable> inputVars, StringBuilder b) {
    b.append(Reflection.getCompilableName(field.getType()));
    b.append(" ");
    b.append(newVar.getName());
    b.append(" = ");
    b.append(field.toCode(inputVars));
    b.append(";");
    b.append(Globals.lineSep);
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
   * parse recognizes a getter for a field in a string.
   * A getter description has the form "<get>( field-descriptor )"
   * where "<get>" is literal ("<" and ">" included, and field-descriptor
   * is as recognized by {@link PublicFieldParser#parse(String)}.
   * 
   * @param descr - string containing descriptor of getter for a field.
   * @return - getter object in string.
   * @throws StatementKindParseException if any error in descriptor string
   * @see PublicFieldParser#parse(String)
   */
  public static FieldGetter parse(String descr) throws StatementKindParseException {
    int parPos = descr.indexOf('(');
    String errorPrefix = "Error parsing " + descr + " as description for field getter statement: ";
    if (parPos < 0) {
      String msg = errorPrefix + " expecting parentheses.";
      throw new StatementKindParseException(msg);
    }
    String prefix = descr.substring(0, parPos);
    if (!prefix.equals("<get>")) {
      String msg = errorPrefix + " expecting <get>( <field-descriptor> ).";
      throw new StatementKindParseException(msg);
    }
    int lastParPos = descr.lastIndexOf(')');
    if (lastParPos < 0) {
      String msg = errorPrefix + " no closing parentheses found.";
      throw new StatementKindParseException(msg);
    }
    String fieldDescriptor = descr.substring(parPos + 1, lastParPos);
    PublicField pf = (new PublicFieldParser()).parse(fieldDescriptor);
    return new FieldGetter(pf);
  }
}
