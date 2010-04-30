package randoop.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import randoop.Testable;




/**
 * This class is used for running randoop's system tests.
 * It is added to the list of classes to explore, and 
 * provides methods that return Classes, Methods and
 * Constructors (several methods in randoop require such
 * objects as parameters).
 * 
 * See build.xml for running the system tests.
 *
 */
public class Seeds {

  private static Class<?> A;

  private static Constructor<?> ACons1;

  private static Constructor<?> ACons2;

  private static Method a1;

  private static Method a2;

  private static Method a3;

  private static Class<?> B;

  private static Constructor<?> BCons1;

  private static Constructor<?> BCons2;

  private static Method b1;

  private static Method b2;

  private static Method b3;

  static {
    try {
      A = Class.forName("randoop.test.A");
      B = Class.forName("randoop.test.B");

      ACons1 = A.getConstructor();
      ACons2 = A.getConstructor(B);
      a1 = A.getMethod("a1");
      a2 = A.getMethod("a2", A);
      a3 = A.getMethod("a3", A, B);

      BCons1 = B.getConstructor();
      BCons2 = B.getConstructor(B);
      b1 = B.getMethod("b1");
      b2 = B.getMethod("b2", A);
      b3 = B.getMethod("b3", B, A);

    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
      System.exit(1);
    }
  }

  @Testable
  public static Class<?> getA() {
    return A;
  }

  @Testable
  public static Class<?> getB() {
    return B;
  }

  @Testable
  public static Constructor<?> getACons1() {
    return ACons1;
  }

  @Testable
  public static Constructor<?> getACons2() {
    return ACons2;
  }

  @Testable
  public static Constructor<?> getBCons1() {
    return BCons1;
  }

  @Testable
  public static Constructor<?> getBCons2() {
    return BCons2;
  }

  @Testable
  public static Method getA1() {
    return a1;
  }

  @Testable
  public static Method getA2() {
    return a2;
  }

  @Testable
  public static Method getA3() {
    return a3;
  }

  @Testable
  public static Method getB1() {
    return b1;
  }

  @Testable
  public static Method getB2() {
    return b2;
  }

  @Testable
  public static Method getB3() {
    return b3;
  }

}
