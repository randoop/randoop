package testables.contractChecks;

public class SymmetryOfEquality {
  private int fA;

  public SymmetryOfEquality(int a) {
    fA = a;
  }

  @Override
  public boolean equals(Object o) {
    if (o != null && o instanceof SymmetryOfEquality) {
      return fA < ((SymmetryOfEquality) o).fA;
    }

    return false;
  }

}
