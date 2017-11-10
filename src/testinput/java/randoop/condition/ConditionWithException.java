package randoop.condition;

/** Input class meant to test what happens when a condition throws an exception */
public class ConditionWithException {

  public boolean errorPredicate() {
    throw new Error("ConditionWithException.errorPredicate threw an error");
  }

  public boolean throwablePredicate() throws Throwable {
    throw new Throwable("ConditionWithException.throwablePredicate threw a Throwable");
  }

  public ConditionWithException() {}

  /**
   * pre-condition: receiver.errorPredicate()
   *
   * @return 1
   */
  public int getZero() {
    return 0;
  }

  /**
   * pre-condition: receiver.throwablePredicate()
   *
   * @return 0
   */
  public int getOne() {
    return 1;
  }
}
