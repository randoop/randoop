/**
 * 
 */
package randoop;

import java.util.ArrayList;
import java.util.List;

/**
 * A statement that is part of a mutable sequence.
 */
public class MStatement {

  public final StatementKind statementKind;

  public final List<MVariable> inputs;

  public final MVariable result;
  
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(result.toString());
    b.append(" = ");
    b.append(statementKind.toString());
    b.append(" ");
    b.append(inputs);
    return b.toString();
  }

  /**
   * Create a new statement of type statement that takes as input the
   * given values.
   */
  public MStatement(StatementKind statement, List<MVariable> inputVariables, MVariable result) {
    this.statementKind = statement;
    this.inputs = new ArrayList<MVariable>(inputVariables);
    this.result = result;
  }
}