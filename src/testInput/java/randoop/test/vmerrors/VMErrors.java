package randoop.test.vmerrors;

public class VMErrors {

  public void throwStackOverflow() {
    throwStackOverflow();
  }

  @Override
  public String toString() {
    return "VMErrors instance";
  }
}
