package ps1;

public class Main {

  public static void main(String[] args) {
    RatPoly previous = new RatPoly();
    for (int i = 0; i < 10; i++) {
      RatPoly poly =
          new RatPoly(i, i + 1)
              .sub(RatPoly.parse("x"))
              .mul(previous)
              .add(new RatPoly(1, 0))
              .div(previous);
      poly.unparse();
      previous = poly;
    }

    str_test("hello there");
    str_test("hello:line");
  }

  public static String str_test(String str) {
    return str;
  }
}
