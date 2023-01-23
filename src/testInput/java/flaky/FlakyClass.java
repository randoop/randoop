package flaky;

public class FlakyClass {
  public int flakyDefaultHashCode() {
    return super.hashCode();
  }  
  
  public int getTwo() {
    return 2;
  }
  
  public int getThree() {
    return 3;
  }
  
  public int multiply(int a, int b) {
    return a * b;
  }
}
