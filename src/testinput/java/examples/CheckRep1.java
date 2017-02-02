package examples;

import randoop.*;

public class CheckRep1 {

  public CheckRep1() {}

  @CheckRep
  public void foo() {
    throw new RuntimeException();
  }
}
