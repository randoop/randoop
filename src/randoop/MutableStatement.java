/**
 * 
 */
package randoop;

import java.util.ArrayList;
import java.util.List;

/**
 * A statement that is part of a mutable sequence.
 */
public class MutableStatement {

  public final Operation operation;

  public final List<MutableVariable> inputs;

  public final MutableVariable result;
  
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(result.toString());
    b.append(" = ");
    b.append(operation.toString());
    b.append(" ");
    b.append(inputs);
    return b.toString();
  }

  /**
   * Create a new statement of type statement that takes as input the
   * given values.
   */
  public MutableStatement(Operation statement, List<MutableVariable> inputVariables, MutableVariable result) {
    this.operation = statement;
    this.inputs = new ArrayList<MutableVariable>(inputVariables);
    this.result = result;
  }
}