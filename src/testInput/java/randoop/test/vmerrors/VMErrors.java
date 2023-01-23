package randoop.test.vmerrors;

public class VMErrors {

  @SuppressWarnings("InfiniteRecursion")
  public void throwStackOverflow() {
    throwStackOverflow();
  }

  @Override
  public String toString() {
    return "VMErrors instance";
  }
}
