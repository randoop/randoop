package misc.elephantbrain;

public class ElephantBrainTest {

  public static GrandParent getFamilyMember(int i) {
    int mod = i % 2;
    GrandParent result = null;

    switch (mod) {
      case 0:
        return new ChildA();
      case 1:
        return new ChildB();
    }

    return new Parent();
  }

  public static void performTest(Parent p) {
    String s = p.toString();

    switch (s) {
      case "ChildA":
        testA();
        break;
      case "ChildB":
        testB();
        break;
      default:
        testP();
    }
  }

  private static void testA() {}

  private static void testB() {}

  private static void testP() {}
}
