package randoop.test;

/**
 * Coin enum based on examples from Java Language Specification intended to be used with tests in
 * Randoop.
 *
 * @see CoinPurse
 */
public enum Coin {
  PENNY(1),
  NICKEL(5),
  DIME(10),
  QUARTER(25),
  HALFDOLLAR(50),
  DOLLAR(100);

  private final int value;

  Coin(int value) {
    this.value = value;
  }

  public int value() {
    return value;
  }
}
