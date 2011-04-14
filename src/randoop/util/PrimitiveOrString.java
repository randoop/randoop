package randoop.util;

/**
 * Holds a primitive or string.
 * The string can be null.
 */
public class PrimitiveOrString {

  private Object thePrimitiveOrString;

  @Override
  public String toString() {
    return (thePrimitiveOrString == null ? "null" : thePrimitiveOrString.toString());
  }

  /**
   * The given object must not be null and must
   * be a primitive or string.
   */
  public PrimitiveOrString(Object o) {

    if (o == null)
      throw new IllegalArgumentException("o cannot be null.");
    if (!PrimitiveTypes.isBoxedOrPrimitiveOrStringType(o.getClass()))
      throw new IllegalArgumentException("o must be a primitive or string.");
    this.thePrimitiveOrString = o;  
  }

  public PrimitiveOrString(boolean b) {
    this.thePrimitiveOrString = b;
  }

  public PrimitiveOrString(char c) {
    this.thePrimitiveOrString = c;
  }

  public PrimitiveOrString(byte b) {
    this.thePrimitiveOrString = b;
  }

  public PrimitiveOrString(short s) {
    this.thePrimitiveOrString = s;
  }

  public PrimitiveOrString(int i) {
    this.thePrimitiveOrString = i;
  }

  public PrimitiveOrString(long l) {
    this.thePrimitiveOrString = l;
  }

  public PrimitiveOrString(float f) {
    this.thePrimitiveOrString = f;
  }

  public PrimitiveOrString(double d) {
    this.thePrimitiveOrString = d;
  }

  public PrimitiveOrString(String s) {
    this.thePrimitiveOrString = s;
  }
}
