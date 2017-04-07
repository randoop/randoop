package randoop.condition;

/**
 * Created by bjkeller on 4/7/17.
 */
class ParentWithException {
  /** @throws RuntimeException if arg is null */
  void m(Object arg) {
    if (arg == null) {
      throw new RuntimeException("arg is null");
    }
  }

  void m2(Object arg) throws Throwable {
    if (arg == null) {
      throw new Throwable("arg");
    }
  }
}


class ChildWithException extends ParentWithException {
  /** @throws NullPointerException if arg is null */
  void m(Object arg) {
    if (arg == null) {
      throw new NullPointerException("arg is null");
    }
  }

  @Override
  void m2(Object arg) {
    if (arg == null) {
      throw new NullPointerException("arg");
    }
  }
}