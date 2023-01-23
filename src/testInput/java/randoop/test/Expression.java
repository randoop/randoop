package randoop.test;

/**
 * Expression class using {@link OperatorEnum} enum intended to be used with tests in Randoop.
 *
 * @see OperatorEnum
 */
public class Expression {
  private OperatorEnum operator;
  private double leftOperand;
  private double rightOperand;

  public Expression(OperatorEnum op, double l, double r) {
    operator = op;
    leftOperand = l;
    rightOperand = r;
  }

  public double leftOperand() {
    return leftOperand;
  }

  public double rightOperand() {
    return rightOperand;
  }

  public double eval() {
    return operator.eval(leftOperand, rightOperand);
  }

  public String toString() {
    return "" + leftOperand + " " + operator + " " + rightOperand;
  }
}
