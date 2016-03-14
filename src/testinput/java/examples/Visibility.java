package examples;

class NonVisible {
  public void randoopOmittedMethod() {};
}

public class Visibility {

  public NonVisible getNonVisible() {
    return new NonVisible();
  }

  public void takesNonVisible(NonVisible nonVisible) {}

  public void m() throws InnerInvisibleException {
    throw new InnerInvisibleException();
  }

  private static class InnerInvisibleException extends Exception {
    // empty body
  }
}
