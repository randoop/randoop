package randoop;

import randoop.util.PrimitiveTypes;

/**
 * An check recording the value of a primitive value obtained
 * during execution, (e.g. <code>var3 == 1</code> where <code>var3</code>
 * is an integer-valued variable in a Randoop test). 
 *
 *<p>
 *
 * Obviously, this is not a property that must hold of all objects in a test.
 * Randoop creates an instance of this contract when, during execution of
 * a sequence, it determines that the above property holds. The property
 * thus represents a <i>regression</i> as it captures the behavior of the
 * code when it is executed.
 */
public final class PrimValue implements ObjectContract {

  private static final long serialVersionUID = -3862776185520906143L;

  /**
   * Specifies how the contract is to be printed. <code>EQUALSEQUALS</code>
   * results in 
   */
  public enum PrintMode { EQUALSEQUALS, EQUALSMETHOD }


  // The runtime value of the primitive value.
  // Is a primitive or String (checked during construction).
  public final Object value;
  
  public final PrintMode printMode;
  
  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof PrimValue)) {
      return false;
    }
    PrimValue other = (PrimValue)o;
    return value.equals(other.value);
  }

  @Override
  public int hashCode() {
    int h = 7;
    h = h * 31 + value.hashCode();
    return h;
  }

  /**
   * @param value The value for the expression. Must be a primitive value or string.
   */
  public PrimValue(Object value, PrintMode printMode) {
    if (value == null) {
      throw new IllegalArgumentException("value cannot be null");
    }
    if (!PrimitiveTypes.isBoxedPrimitiveTypeOrString(value.getClass()))
      throw new IllegalArgumentException("value is not a primitive or string : " +
          value.getClass());
    this.value = value;
    this.printMode = printMode;
  }

  public String toCodeStringPreStatement() {
    return "";
  }

  @Override
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 1;
    return value.equals(objects[0]);
  }

  @Override
  public int getArity() {
    return 1;
  }

  @Override
  public String toString() {
    return "randoop.PrimValue, value=" + value;
  }
 
  @Override
  public String get_observer_str() {
    return "PrimValue";
  }

  @Override
  public String toCodeString() {
  
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Regression assertion (captures the current behavior of the code)" + Globals.lineSep);
    b.append("assertTrue(");

    // ValueExpression represents the value of a variable.
    // We special-case printing for this type of expression,
    // to improve readability.
    if (printMode.equals(PrintMode.EQUALSMETHOD)) {
      // First add a message
      b.append ("\"'\" + " + "x0" + " + \"' != '\" + "
          + PrimitiveTypes.toCodeString(value) + "+ \"'\", ");
      b.append("x0");
      b.append(".equals(");
      b.append(PrimitiveTypes.toCodeString(value));
      b.append(")");
      } else {
        assert printMode.equals(PrintMode.EQUALSEQUALS);
        b.append("x0 == " + PrimitiveTypes.toCodeString(value));
      }

    // Close assert.
    b.append(");");

    return b.toString();
  }

  @Override
  public String toCommentString() {
    return null;
  }

  @Override
  public boolean evalExceptionMeansFailure() {
    return true;
  }

 
}
