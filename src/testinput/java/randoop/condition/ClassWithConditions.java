package randoop.condition;

public class ClassWithConditions {
  private final int value;

  /**
   * @param value a non-negative integer
   */
  public ClassWithConditions(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public Range category(int value) {
    if (value > 4*this.value) {
      throw new IllegalArgumentException("should be less than 4*getValue()");
    }
    if (value < this.value) {
      return Range.ONE;
    } else if (value < 2*this.value) {
      return Range.TWO;
    } else if (value < 3*this.value){
      return Range.TWO; //should return THREE
    } else {
      return Range.FOUR;
    }
  }

  public int getThirdValue() {
    return 3*value - 1;
  }

  public enum Range {
    ONE,
    TWO,
    THREE,
    FOUR
  }
}
