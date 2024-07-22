package sideeffectfree;

import org.checkerframework.dataflow.qual.Pure;

public class Square {
  private int a, b;

  private Square() {
    a = 2;
    b = 3;
  }

  @Pure
  public int getA() {
    return a;
  }

  public static Square getNewSquare() {
    return new Square();
  }

  @Pure
  public int getSum(Square other) {
    return other.a + other.b + a + b;
  }
}
