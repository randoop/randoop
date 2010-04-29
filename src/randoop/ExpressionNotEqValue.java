package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import randoop.util.PrimitiveTypes;
import randoop.util.Reflection;

/**
 * An observation recording the value of an expression evaluated
 * to during execution. In particular, this observation represents
 *
 *     expression(vars) != value
 *
 * where value is a primitive value or a String.
 */
public class ExpressionNotEqValue implements Observation, Serializable {

  private static final long serialVersionUID = 20100429; 

  // The expression whose runtime value this observation records.
  // We store the class, not the expression itself, to ensure
  // that serialization always work (we can easily serialize a class name)
  // without the expression classes having to implement serialization.
  // And since expressions are supposed to be stateless, we lose nothing
  // by storing the class instead of the object.  This is declared transient
  // because Class is not serializable itself.  We serialize it with its
  // name.
  public transient Class<? extends Expression> expression;

  // The variables over which the expression applies.
  public List<Variable> vars;

  // The runtime value of the expression.
  public Object value;

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("<ExpressionNotEqValue ");
    b.append(expression.getName());
    b.append(" " + vars.toString() + " ");
    b.append(", value=" + (value==null? "null" : value.toString()));
    return b.toString();
  }

  /**
   * @param expression The expression. Cannot be not null.
   * @param vars The variables for the expression. Must all belong to the same sequence.
   * @param value The value for the expression. Must be a primitive value or string.
   */
  public ExpressionNotEqValue(Class<? extends Expression> expression,
      List<Variable> vars, Object value) {

    if (expression == null)
      throw new IllegalArgumentException("expression cannot be null.");
    if (value != null && !PrimitiveTypes.isBoxedPrimitiveTypeOrString(value.getClass()))
      throw new IllegalArgumentException("value is not a primitive or string : " +
          value.getClass());
    this.expression = expression;
    this.vars = new ArrayList<Variable>(vars);
    this.value = value;

    // System.out.printf ("expressoineqvalue: %s", toString());
    // Throwable t = new Throwable(); t.printStackTrace();
  }

  /** Returns the value as a string **/
  public String get_value() {
    if (value == null)
      return "!null";
    else
      return "!" + value.toString();
  }

  /**
   * Write the object state.  Necessary because Class is not serializable
   * The name of the class is stored instead.
   */
   private void writeObject (java.io.ObjectOutputStream os) throws IOException {

     os.defaultWriteObject();
     os.writeObject (expression.getName());
   }

  /**
   * Read the object state.  Necessary because Class is not serializable
   * The expression is rebuilt from the name of the class.
   */
  private void readObject (java.io.ObjectInputStream is) throws IOException,
                                                      ClassNotFoundException {

    is.defaultReadObject();
    @SuppressWarnings("unchecked")
    Class<? extends Expression> expression_tmp = (Class<? extends Expression>)
      Reflection.classForName ((String) is.readObject());
    expression = expression_tmp;
  }

  public String toCodeStringPreStatement() {
    return "";
  }

  public String toCodeStringPostStatement() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Regression assertion (captures the current behavior of the code)" + Globals.lineSep);
    b.append("assertFalse(");

    // ValueExpression represents the value of a variable.
    // We special-case printing for this type of expression,
    // to improve readability.
    if (expression.equals(ValueExpression.class)) {
      assert vars.size() == 1;
      Variable var = vars.get(0);
      if (value == null) {
        b.append(var + " == null");
      } else if (var.getType().isPrimitive()) {
        b.append(var + " == " + PrimitiveTypes.toCodeString(value));
      } else {
        // First add a message
        b.append ("\"'\" + " + var + " + \"' != '\" + "
                  + PrimitiveTypes.toCodeString(value) + "+ \"'\", ");
        b.append(var);
        b.append(".equals(");
        b.append(PrimitiveTypes.toCodeString(value));
        b.append(")");
      }
    } else {

      // Print the expression the standard way.
      b.append(ExpressionUtils.toCodeString(expression, vars));
      if (value == null) {
        b.append(" == null);");
      } else {
        b.append(".equals(");
        b.append(PrimitiveTypes.toCodeString(value));
        b.append(")");
      }
    }

    // Close assert.
    b.append(");");

    return b.toString();
  }
}
