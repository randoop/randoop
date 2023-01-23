package randoop.test;

/**
 * Operator enum based on examples from Java Language Specification intended to be used with tests
 * in Randoop.
 *
 * @see Expression
 */
public enum OperatorEnum {
  PLUS {
    @Override
    public double eval(double l, double r) {
      return l + r;
    }

    public String toString() {
      return "+";
    }
  },
  MINUS {
    @Override
    public double eval(double l, double r) {
      return l - r;
    }

    public String toString() {
      return "-";
    }
  },
  MULT {
    @Override
    public double eval(double l, double r) {
      return l * r;
    }

    public String toString() {
      return "*";
    }
  },
  DIV {
    @Override
    public double eval(double l, double r) {
      return l / r;
    }

    public String toString() {
      return "/";
    }
  };

  public abstract double eval(double l, double r);
}
