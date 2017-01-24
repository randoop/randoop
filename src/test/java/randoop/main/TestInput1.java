package randoop.main;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInput1 {
  public static boolean debug = false;

  @Test
  public void test1() throws Throwable {
    if (debug) {
      System.out.format("%n%s%n", "ErrorTest0.test10");
    }
    org.apache.commons.lang3.math.Fraction fraction0 =
        org.apache.commons.lang3.math.Fraction.ONE_QUARTER;
    org.apache.commons.lang3.math.Fraction fraction1 =
        org.apache.commons.lang3.math.Fraction.TWO_QUARTERS;
    long long2 = fraction1.longValue();
    String str3 = fraction1.toProperString();
    org.apache.commons.lang3.math.Fraction fraction4 = fraction0.divideBy(fraction1);
    org.apache.commons.lang3.mutable.MutableByte mutableByte5 =
        new org.apache.commons.lang3.mutable.MutableByte((Number) fraction1);
    // byte byte6 = mutableByte5.getAndDecrement();

    // Checks the contract: compareTo-equals on fraction1 and fraction4
    org.junit.Assert.assertTrue(
        "Contract failed: compareTo-equals on fraction1 and fraction4",
        (fraction1.compareTo(fraction4) == 0) == fraction1.equals(fraction4));
  }
}
