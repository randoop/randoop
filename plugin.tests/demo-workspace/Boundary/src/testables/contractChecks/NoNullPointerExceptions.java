package testables.contractChecks;

public class NoNullPointerExceptions {

  public static int throwsAnNPE() {
    throw new NullPointerException();
  }
}
