package randoop.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Test;
import randoop.reflection.TypeNames;

public class CoverageInstrumentationTest {

  @Test
  public void test() {

    // get class for A
    Class<?> ac = null;
    try {
      ac = TypeNames.getTypeForName("instrument.testcase.AE");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    // get instrument field from A
    Field used = null;
    try {
      used = ac.getDeclaredField("randoop_classUsedFlag");
      used.setAccessible(true);
    } catch (NoSuchFieldException e2) {
      fail("didn't find field " + e2);
    } catch (SecurityException e2) {
      fail("can't access field " + e2);
    }

    // get instrument check method for A
    Method check = null;
    try {
      check = ac.getMethod("randoop_checkAndReset", new Class<?>[0]);
      check.setAccessible(true);
    } catch (NoSuchMethodException e1) {
      fail("no such method: " + e1);
    } catch (SecurityException e1) {
      fail("security exception " + e1);
    }

    // get class B
    Class<?> bc = null;
    try {
      bc = TypeNames.getTypeForName("instrument.testcase.BE");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    // Let's check instrumentation is working correctly, B has method
    Method bcheck;
    try {
      bcheck = bc.getMethod("randoop_checkAndReset", new Class<?>[0]);
      assertNotNull(bcheck);
    } catch (NoSuchMethodException e1) {
      // passes
    } catch (SecurityException e1) {
      fail("security exception " + e1);
    }

    // get class A
    try {
      TypeNames.getTypeForName("instrument.testcase.CE");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    // Let's check instrumentation is working correctly, C doesn't have method
    Method ccheck;
    try {
      ccheck = bc.getMethod("randoop_checkAndReset", new Class<?>[0]);
      assertNotNull(ccheck);
    } catch (NoSuchMethodException e1) {
      // passes
    } catch (SecurityException e1) {
      fail("security exception " + e1);
    }

    // be sure that instrumentation field in A is false
    boolean lastUsedValue = true;

    try {
      lastUsedValue = (boolean) used.get(null);
      assertFalse(lastUsedValue);
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }

    // ask instrumentation method for field value
    try {
      assertEquals(
          "flag should not have changed",
          lastUsedValue,
          (boolean) check.invoke(null, new Object[0]));
    } catch (IllegalAccessException e) {
      fail("illegal access " + e);
    } catch (IllegalArgumentException e) {
      fail("illegal argument " + e);
    } catch (InvocationTargetException e) {
      fail("invocation target " + e);
    }

    try {
      lastUsedValue = (boolean) used.get(null);
      assertFalse(lastUsedValue);
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }

    try {
      assertFalse((boolean) check.invoke(null, new Object[0]));
    } catch (IllegalAccessException e) {
      fail("illegal access " + e);
    } catch (IllegalArgumentException e) {
      fail("illegal argument " + e);
    } catch (InvocationTargetException e) {
      fail("invocation target " + e);
    }

    try {
      lastUsedValue = (boolean) used.get(null);
      assertFalse(lastUsedValue);
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }

    // Make an AE(BE) constructor to check direct manipulation of flag
    Constructor<?> acon = null;
    try {
      acon = ac.getDeclaredConstructor(bc);
      acon.setAccessible(true);
    } catch (NoSuchMethodException e3) {
      fail("cannot find A(B) " + e3);
    } catch (SecurityException e3) {
      fail("cannot access A(B) " + e3);
    }

    Constructor<?> bcon = null;
    try {
      bcon = bc.getConstructor(int.class);
      bcon.setAccessible(true);
    } catch (NoSuchMethodException e) {
      fail("can't find BE(int) " + e);
    } catch (SecurityException e) {
      fail("security exception for BE(int) " + e);
    }

    try {
      assertEquals("field should not have changed", lastUsedValue, (boolean) used.get(null));
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }

    Object[] args = new Object[1];
    args[0] = Integer.valueOf(1);
    Object bobj = null;
    try {
      bobj = bcon.newInstance(args);
    } catch (InstantiationException e) {
      fail("failed to instantiate " + e);
    } catch (IllegalAccessException e) {
      fail("bad access " + e);
    } catch (IllegalArgumentException e) {
      fail("bad argument " + e);
    } catch (InvocationTargetException e) {
      fail("bad invocation target " + e);
    }

    // should be true since B constructor uses A constructor
    try {
      lastUsedValue = (boolean) used.get(null);
      assertTrue(lastUsedValue);
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }

    try {
      assertTrue((boolean) check.invoke(null, new Object[0]));
    } catch (IllegalAccessException e) {
      fail("illegal access " + e);
    } catch (IllegalArgumentException e) {
      fail("illegal argument " + e);
    } catch (InvocationTargetException e) {
      fail("invocation target " + e);
    }

    try {
      lastUsedValue = (boolean) used.get(null);
      assertFalse(lastUsedValue);
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }

    try {
      acon.newInstance(bobj);
    } catch (InstantiationException e) {
      fail("failed to instantiate " + e);
    } catch (IllegalAccessException e) {
      fail("bad access " + e);
    } catch (IllegalArgumentException e) {
      fail("bad argument " + e);
    } catch (InvocationTargetException e) {
      fail("bad invocation target " + e);
    }

    try {
      assertTrue((boolean) used.get(null));
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }

    try {
      assertTrue((boolean) check.invoke(null, new Object[0]));
    } catch (IllegalAccessException e) {
      fail("illegal access " + e);
    } catch (IllegalArgumentException e) {
      fail("illegal argument " + e);
    } catch (InvocationTargetException e) {
      fail("invocation target " + e);
    }

    try {
      lastUsedValue = (boolean) used.get(null);
      assertFalse(lastUsedValue);
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }

    Method jump = null;
    try {
      jump = bc.getDeclaredMethod("jumpValue", new Class<?>[0]);
    } catch (NoSuchMethodException e) {
      fail("cannot find method" + e);
    } catch (SecurityException e) {
      fail("cannot access method" + e);
    }

    try {
      assertEquals("field should not have changed", lastUsedValue, (boolean) used.get(null));
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }

    try {
      jump.invoke(bobj, new Object[0]);
    } catch (IllegalAccessException e) {
      fail("cannot access " + e);
    } catch (IllegalArgumentException e) {
      fail("bad argument" + e);
    } catch (InvocationTargetException e) {
      fail("bad invocation " + e);
    }

    try {
      assertTrue((boolean) used.get(null));
    } catch (IllegalArgumentException e2) {
      fail("bad field access" + e2);
    } catch (IllegalAccessException e2) {
      fail("can't access field " + e2);
    }
  }
}
