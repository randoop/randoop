package randoop;

import java.io.Serializable;

/**
 * A contract-violating observation recording a NullPointerException exception
 * during execution.
 */
public class StatementThrowsNPE implements ContractViolation, Serializable {

 private static final long serialVersionUID = 1L;

 private static final StatementThrowsNPE theInstance = new StatementThrowsNPE();

  public String toString() {
    return "// throws null pointer exception";
  }

  private StatementThrowsNPE() {
    // Empty body.
  }

  public static StatementThrowsNPE getInstance() {
    return theInstance;
  }

  /** The 'value' of this exception is always NPE **/
  public String get_value() {
    return "NPE";
  }

  /**
   * The "try" half of the try-catch wrapper.
   */
  public String toCodeStringPreStatement() {
    StringBuilder b = new StringBuilder();
    b.append("// Checks that no NullPointerException is thrown." + Globals.lineSep);
    b.append("try {" + Globals.lineSep + "  ");
    return b.toString();
  }

  /**
   * The "catch" half of the try-catch wrapper.
   */
  public String toCodeStringPostStatement() {
    StringBuilder b = new StringBuilder();
    b.append("} catch (NullPointerException e) {" + Globals.lineSep);
    b.append("  fail(\"Statement throw NullPointerException in the absence of null.\");" + Globals.lineSep);
    b.append("}" + Globals.lineSep);
    return b.toString();
  }

  public String toStringName() {
    return "npe";
  }

  public String toStringVars() {
    return "[]";
  }
}
