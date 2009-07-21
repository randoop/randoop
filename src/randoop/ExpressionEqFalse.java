package randoop;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

import randoop.util.PrimitiveTypes;

/**
 * An observation recording the fact that an ObjectContract
 * evaluated to false.
 */
public class ExpressionEqFalse implements ContractViolation {

    // The expression whose runtime value this observation records.
    // We store the class, not the expression itself, to ensure
    // that serialization always work (we can easily serialize a class name)
    // without the expression classes having to implement serialization.
    // And since expressions are supposed to be stateless, we lose nothing
    // by storing the class instead of the object.
    public Class<? extends ObjectContract> objcontract;

    // The variables over which the expression applies.
    public List<Variable> vars;

    // The runtime value of the expression.
    public Object value;
    
    public String toString() {
      StringBuilder b = new StringBuilder();
      b.append("ContractViolation:eq-false-");
      b.append(objcontract.getName());
      b.append(vars);
      return b.toString();
    }

    /**
     * @param expression The expression. Cannot be not null.
     * @param vars The variables for the expression. Must all belong to the same sequence.
     * @param value The value for the expression. Must be a primitive value or string.
     */
    public ExpressionEqFalse(Class<? extends ObjectContract> expression,
        List<Variable> vars, Object value) {
      if (expression == null)
        throw new IllegalArgumentException("expression cannot be null.");
      if (value != null && !PrimitiveTypes.isBoxedPrimitiveTypeOrString(value.getClass()))
        throw new IllegalArgumentException("value is not a primitive or string : " +
            value.getClass());
      this.objcontract = expression;
      this.vars = new ArrayList<Variable>(vars);
      this.value = value;
    }


    private Object writeReplace() throws ObjectStreamException {
      throw new RuntimeException("Not implemented.");
    }

    public String toCodeStringPreStatement() {
      return "";
    }

    public String toCodeStringPostStatement() {
      StringBuilder b = new StringBuilder();
      b.append(Globals.lineSep);
      String comment = ExpressionUtils.localizeExpressionComment(objcontract, vars);
      b.append("// Checks the contract: ");
      b.append(" " + comment + Globals.lineSep);
      b.append("assertTrue(");
      b.append("\"Contract failed: " + comment + "\", ");
      String codeStr = null;
      try {
        codeStr = objcontract.newInstance().toCodeString();
      } catch (Exception e) {
        throw new Error(e);
      }

      if (codeStr == null) {
        // Print the expression the defatul way.
        b.append("(Boolean)");
        b.append(ExpressionUtils.toCodeString(objcontract, vars));
      } else {
        b.append(ExpressionUtils.localizeExpressionCode(objcontract, vars));
      }

      // Close assert.
      b.append(");");

      return b.toString();
    }

    public String toStringName() {
      return objcontract.getSimpleName();
    }

    public String toStringVars() {
      return vars.toString();
    }

 
}
