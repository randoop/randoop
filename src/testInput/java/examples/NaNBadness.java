package examples;

/**
 * This is code from issue #120 reported by user Changmed modified slightly to force
 * creation of NaN value by computation.
 * Test is to ensure that does not result in flaky tests.
 */
public class NaNBadness {

  private static final int LONG_BYTE_ARRAY_LEN = 8;

  public static double getBadNaN() { return toDouble(toByteArray(-1)); }

  // long
  private static byte[] toByteArray(long value) {
    return new byte[]{(byte) (value >>> 56), (byte) (value >>> 48), (byte) (value >>> 40), (byte) (value >>> 32), (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
  }

  public static long toLong(byte[] b) {
    if (b == null || b.length != LONG_BYTE_ARRAY_LEN)
      throw new IllegalArgumentException("input must be non-null byte[8]");

    return ((long) (b[0] & 0xFF) << 56) + ((long) (b[1] & 0xFF) << 48) + ((long) (b[2] & 0xFF) << 40) + ((long) (b[3] & 0xFF) << 32) + ((long) (b[4] & 0xFF) << 24) + ((long) (b[5] & 0xFF) << 16) + ((long) (b[6] & 0xFF) << 8) + (long) (b[7] & 0xFF);
  }

  // double
  public static byte[] toByteArray(double value) {
    return toByteArray(Double.doubleToRawLongBits(value));
  }

  private static double toDouble(byte[] b) {
    if (b == null || b.length != LONG_BYTE_ARRAY_LEN)
      throw new IllegalArgumentException("input must be non-null byte[8]");

    return Double.longBitsToDouble(toLong(b));
  }

}
