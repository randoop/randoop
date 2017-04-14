package randoop.condition;

/**
 * Input class meant to test what happens when a condition throws an exception
 */
public class ConditionWithException {

  public boolean errorPredicate() {
    throw new Error("this happened");
  }

  public boolean throwablePredicate() throws Throwable {
    throw new Throwable("this happened");
  }

  public ConditionWithException() {}

  /**
   * pre-condition: receiver.errorPredicate()
   *
   * @return 1
   */
  public int getZero() { return 0; }

  /**
   * pre-condition: receiver.throwablePredicate()
   *
   * @return 0
   */
  public int getOne() { return 1; }
}
