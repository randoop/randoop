package randoop.test;

import randoop.Check;
import randoop.Execution;
import randoop.Variable;

/**
 * A custom check for class randoop.test.A.
 * This check represents the check:
 * 
 *    a.a1(a); Assert.assertEquals(a.i, V)
 *    
 * For some object a in a test sequence, and
 * some value V.
 *
 */
public class CustomCheck implements Check {

  private final Variable var;
  private final int ival;

  /**
   * Represents the custom check performed on the given variable (assumed to be
   * of type A) in the sequence, whose field i has value ai after the call to
   * method a1.
   */
  public CustomCheck(Variable variable, int ai) {
    this.var = variable;
    this.ival = ai;
  }

  @Override
  public boolean evaluate(Execution execution) {
    throw new RuntimeException("programmatic evaluation of a CustomCheck not supported");
  }

  @Override
  public String get_value() {
    throw new RuntimeException("programmatic evaluation of a CustomCheck not supported");
  }

  @Override
  public int get_stmt_no() {
    throw new RuntimeException("programmatic evaluation of a CustomCheck not supported");
  }

  @Override
  public String get_id() {
    throw new RuntimeException("programmatic evaluation of a CustomCheck not supported");
  }


  @Override
  public String toCodeStringPostStatement() {
    return String.format("%s.a1(%s); Assert.assertEquals(%s.i, %d);", var, var, var, ival);
  }

  @Override
  public String toCodeStringPreStatement() {
    return ""; // No pre-statement code.
  }

}
