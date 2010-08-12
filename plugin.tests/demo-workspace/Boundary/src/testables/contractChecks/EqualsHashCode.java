package testables.contractChecks;

public class EqualsHashCode {
  private int fA;
  private int fB;

  public EqualsHashCode(int a, int b) {
    fA = a;
    fB = b;
  }

  @Override
  public int hashCode() {
    return fA + fB;
  }

  @Override
  public boolean equals(Object o) {
    return o != null;
  }
  
}
