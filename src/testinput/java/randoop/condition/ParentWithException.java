package randoop.condition;

/**
 * These are inputs for testing behavior on conditions
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
  void m2(Object arg) throws Throwable {
    super.m2(arg);
    if (arg == null) {
      throw new NullPointerException("arg");
    }
  }
}