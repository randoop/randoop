package testables.contracts;

public class NoNullPointerExceptions {

  public static int throwsAnNPE() {
    throw new NullPointerException();
  }
}
