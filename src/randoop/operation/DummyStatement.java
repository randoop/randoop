package randoop.operation;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Variable;

/**
 * DummyStatement is an {@link Operation} representing a noop.
 * As an operation a DummyStatement with name noop is represented as noop : [] -> void, 
 * an operation with no input types and void output. It does nothing when executed.
 */
public class DummyStatement extends AbstractOperation implements Operation, Serializable {

  private static final long serialVersionUID = -3153094271647824398L;

  /** ID for parsing purposes (see StatementKinds.parse method) */
  public static final String ID = "dummy";

  private final String name;

  public DummyStatement() {
    this("DummyStatement");
  }


  public DummyStatement(String name) {
    if (name == null) throw new IllegalArgumentException("name cannot be null.");
    this.name = name;
  }

  public List<Class<?>> getInputTypes() {
    return new ArrayList<Class<?>>();
  }

  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    return new NormalExecution(null, 0);
  }

  @Override
  public String toString() {
    return name;
  }

  public String toStringShort() {
    return toString();
  }

  public String toStringVerbose() {
    return toString();
  }

  @Override
  public Class<?> getOutputType() {
    return void.class;
  }

  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    b.append("//DummyStatement");
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DummyStatement)) return false;
    DummyStatement other = (DummyStatement)o;
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * {@inheritDoc}
   * Generates a string is of the form:
   * 
   * (NAME)
   * 
   * Where NAME is the name of the dummy statement.
   * 
   * Example:
   *   (foobar)
   */
  @Override
  public String toParseableString() {
    return "(" + name + ")";
  }

  /**
   * parse recognizes a string representing this dummy statement as produced by 
   * {@link DummyStatement#toParseableString()}. 
   * 
   */
  public static Operation parse(String description) {
    assert description.charAt(0) == '(';
    assert description.charAt(description.length() - 1) == ')';
    return new DummyStatement(description.substring(1, description.length() - 1));
  }


  @Override
  public Class<?> getDeclaringClass() {
    return void.class;
  }
}
