package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import randoop.reflection.OperationExtractor;
import randoop.test.Coin;
import randoop.test.OperatorEnum;
import randoop.test.PlayingCard;
import randoop.test.SimpleEnum;
import randoop.util.Reflection;

/**
 * EnumReflectionTest consists of tests of {@link Reflection#getStatements}
 * to verify what is collected from enums and classes using enums. In particular,
 * want to collect enum constants, methods of enum (esp. if abstract), enums that are
 * are inner types.
 *
 */
public class EnumReflectionTest {

  /**
   * simpleEnum tests that for a simple enum (constants and no explicit
   * methods) that we just get the constant valuse and no methods.
   * Uses randoop.test.SimpleEnum, which is a basic enum
   * with four values: ONE, TWO, THREE, and FOUR
   */
  @Test
  public void simpleEnum() {
    ArrayList<Class<?>> classes = new ArrayList<>();
    Class<?> se = SimpleEnum.class;
    classes.add(se);

    @SuppressWarnings("unchecked")
    List<Enum<?>> include = asList(se.getEnumConstants());
    @SuppressWarnings("unchecked")
    List<Method> exclude = Arrays.asList(se.getMethods());
    List<Operation> actual = OperationExtractor.getOperations(classes, null);

    assertEquals("number of statements", include.size(), actual.size());

    for (Enum<?> e : include) {
      assertTrue(
          "enum constant " + e.name() + " should occur", actual.contains(new EnumConstant(e)));
    }
    for (Method m : exclude) {
      assertFalse(
          "method " + m.toGenericString() + " should not occur in simple enum",
          actual.contains(new MethodCall(m)));
    }
  }

  private List<Enum<?>> asList(Object[] enumConstants) {
    List<Enum<?>> list = new ArrayList<>();
    for (Object obj : enumConstants) {
      if (obj instanceof Enum) {
        list.add((Enum<?>) obj);
      }
    }
    return list;
  }

  /**
   * innerEnum tests that Reflection.getStatements is collecting simple
   * enum constants from a class. Uses randoop.test.PlayingCard, which has
   * two public enums, one private enum, and one enum with package access.
   *
   */
  @SuppressWarnings("unchecked")
  @Test
  public void innerEnum() {
    ArrayList<Class<?>> classes = new ArrayList<>();
    Class<?> pc = PlayingCard.class;
    classes.add(pc);

    List<Enum<?>> include = new ArrayList<>();
    List<Enum<?>> exclude = new ArrayList<>();
    for (Class<?> c : pc.getDeclaredClasses()) {
      int mods = c.getModifiers();
      if (c.isEnum()) {
        if (Modifier.isPublic(mods)) {
          include.addAll(asList(c.getEnumConstants()));
        } else {
          exclude.addAll(asList(c.getEnumConstants()));
        }
      }
    }

    List<Operation> actual = OperationExtractor.getOperations(classes, null);
    assertEquals("number of statements", include.size() + 5, actual.size());

    for (Enum<?> e : include) {
      assertTrue(
          "enum constant " + e.name() + " should occur", actual.contains(new EnumConstant(e)));
    }
    for (Enum<?> e : exclude) {
      assertFalse(
          "enum constant " + e.name() + " should not occur", actual.contains(new EnumConstant(e)));
    }
  }

  /**
   * valueEnum tests Reflection.getStatements for an enum with a field.
   * Uses randoop.test.Coin, which has a private int field, a private constructor,
   * and one public accessor method. Expect that should return the constants and accessor.
   * Note that compiler ensures constructor access is at least package level.
   */
  @Test
  public void valueEnum() {
    ArrayList<Class<?>> classes = new ArrayList<>();
    Class<?> coin = Coin.class;
    classes.add(coin);

    List<Operation> actual = OperationExtractor.getOperations(classes, null);

    int count = 0;
    for (Object obj : coin.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      assertTrue(
          "enum constant " + e.name() + " should occur", actual.contains(new EnumConstant(e)));
      count++;
    }

    for (Constructor<?> con : coin.getDeclaredConstructors()) {
      assertFalse(
          "enum constructor " + con.getName() + "should not occur",
          actual.contains(new ConstructorCall(con)));
    }

    for (Method m : coin.getMethods()) {
      if (m.getName().equals("value")) {
        assertTrue(
            "enum method " + m.toGenericString() + " should occur",
            actual.contains(new MethodCall(m)));
        count++;
      } else {
        assertFalse(
            "enum method " + m.toGenericString() + " should not occur",
            actual.contains(new MethodCall(m)));
      }
    }
    assertEquals("number of statements", count, actual.size());
  }

  /**
   * abstractMethodEnum tests Reflection.getStatements for an enum with an abstract method
   * and overridden Object methods.
   * Uses randoop.test.Operator that has four constants, one abstract method eval and each
   * constant implements eval and toString.
   */
  @Test
  public void abstractMethodEnum() {
    ArrayList<Class<?>> classes = new ArrayList<>();
    Class<?> op = OperatorEnum.class;
    classes.add(op);

    List<Operation> actual = OperationExtractor.getOperations(classes, null);

    Set<String> overrides = new TreeSet<String>();
    int count = 0;
    for (Object obj : op.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      assertTrue(
          "enum constant " + e.name() + " should occur", actual.contains(new EnumConstant(e)));
      count++;
      for (Method m : e.getClass().getDeclaredMethods()) {
        overrides.add(m.getName());
      }
    }

    for (Method m : op.getMethods()) {
      if (overrides.contains(m.getName())) {
        assertTrue(
            "enum method " + m.toGenericString() + " should occur",
            actual.contains(new MethodCall(m)));
        count++;
      } else {
        assertFalse(
            "enum method " + m.toGenericString() + " should not occur",
            actual.contains(new MethodCall(m)));
      }
    }

    assertEquals("number of statements", count, actual.size());
  }
}
