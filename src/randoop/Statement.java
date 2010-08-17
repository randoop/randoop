package randoop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import randoop.Sequence.RelativeNegativeIndex;

/**
 * The elements of a Sequence. Each of these contains two pieces of information:
 * (1) A statement kind, and (2) inputs to the statement.
 */
public final class Statement implements Serializable {

  private static final long serialVersionUID = -6876369784900176443L;

  // The kind of statement (method call, constructor call,
  // primitive values declaration, etc.).
  public final StatementKind statement;

  // The list of values used as input to the statement.
  //
  // NOTE that the inputs to a statement are not a list
  // of Variables, but a list of RelativeNegativeIndex objects.
  // See that class for an explanation.
  public final List<RelativeNegativeIndex> inputs;

  /**
   * Create a new statement of type statement that takes as input the given
   * values.
   */
  public Statement(StatementKind statement,
      List<RelativeNegativeIndex> inputVariables) {
    this.statement = statement;
    this.inputs = Collections
    .unmodifiableList(new ArrayList<RelativeNegativeIndex>(
        inputVariables));
  }

  /**
   * True iff this statement is a void method call.
   */
  public boolean isVoidMethodCall() {
    return statement.getOutputType().equals(void.class);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Statement))
      return false;
    if (this == o)
      return true;
    Statement other = (Statement) o;
    if (!statement.equals(other.statement))
      return false;
    if (inputs.size() != other.inputs.size())
      return false;
    for (int i = 0; i < inputs.size(); i++) {
      if (inputs.get(i) != other.inputs.get(i))
        return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int retval = 5;
    retval += 7 ^ statement.hashCode();
    for (int i = 0; i < inputs.size(); i++) {
      retval += 13 ^ inputs.get(i).index;
    }
    return retval;
  }

}
