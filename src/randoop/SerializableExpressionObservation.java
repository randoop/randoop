package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import randoop.util.Reflection;

public class SerializableExpressionObservation implements Serializable {

  private static final long serialVersionUID = -1700706972870774096L;
  private String expression;
  private List<Variable> vars;
  private Object value;

  public SerializableExpressionObservation(
      Class<? extends Expression> expression, List<Variable> vars,
      Object value) {
    this.expression = expression.getName();
    this.vars = vars;
    this.value = value;
  }

  // XXX Should copy the fields, not just assign them.
  @SuppressWarnings("unchecked")
  private Object readResolve() throws ObjectStreamException {
    Class<? extends Expression> exprClass =
      (Class<? extends Expression>) Reflection.classForName(this.expression);
    return new ExpressionEqValue(exprClass, vars, value);
  }
}
