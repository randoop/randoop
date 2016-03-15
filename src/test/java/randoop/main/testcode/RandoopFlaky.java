package randoop.main.testcode;

public class RandoopFlaky {
  private static int f = 0;

  public int check1(int i) {
    if (f != 0) {
      throw new RuntimeException();
    }
    return 1;
  }

  public int check2(int i) {
    if (f != 0) {
      throw new RuntimeException();
    }
    return 2;
  }

  public int check3(int i) {
    if (f != 0) {
      throw new RuntimeException();
    }
    return 3;
  }

  public int check4(int i) {
    if (f != 0) {
      throw new RuntimeException();
    }
    return 4;
  }

  public int check5(int i) {
    if (f != 0) {
      throw new RuntimeException();
    }
    return 5;
  }

  public int check6(int i) {
    if (f != 0) {
      throw new RuntimeException();
    }
    return 6;
  }

  public int check7(int i) {
    if (f != 0) {
      throw new RuntimeException();
    }
    return 7;
  }

  public int check8(int i) {
    if (f != 0) {
      throw new RuntimeException();
    }
    return 8;
  }

  public int check9(int i) {
    if (f != 0) {
      throw new RuntimeException();
    }
    return 9;
  }

  public int modify(int i) {
    f++;
    return f;
  }
}
