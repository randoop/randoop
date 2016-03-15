package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.field.FinalInstanceField;
import randoop.field.InstanceField;
import randoop.field.StaticField;
import randoop.field.StaticFinalField;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGet;
import randoop.operation.FieldSet;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.reflection.visibilitytest.PublicClass;

public class VisibilityTest {

  /*
   * package private class
   * package visibility
   * same package
   */
  @Test
  public void testStandardPackagePrivateVisibility() {
    List<Class<?>> classes = new ArrayList<>();
    Class<?> c = null;
    try {
      c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
      classes.add(c);
    } catch (ClassNotFoundException e) {
      fail("can't access class-under-test: PackagePrivateClass");
    }

    List<Constructor<?>> expectedConstructors = new ArrayList<>();
    for (Constructor<?> co : c.getDeclaredConstructors()) {
      int mods = co.getModifiers() & Modifier.constructorModifiers();
      if (isPackageVisible(mods)) {
        expectedConstructors.add(co);
      }
    }
    if (expectedConstructors.isEmpty()) {
      fail("should have nonempty expected constructor set");
    }

    List<Enum<?>> expectedEnums = new ArrayList<>();
    for (Class<?> ic : c.getDeclaredClasses()) {
      int mods = ic.getModifiers() & Modifier.classModifiers();
      if (ic.isEnum() && isPackageVisible(mods)) {
        for (Object o : ic.getEnumConstants()) {
          Enum<?> e = (Enum<?>) o;
          expectedEnums.add(e);
        }
      }
    }
    if (expectedEnums.isEmpty()) {
      fail("should have nonempty expected enum set");
    }

    List<Field> expectedFields = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      if (isPackageVisible(mods)) {
        expectedFields.add(f);
      }
    }

    if (expectedFields.isEmpty()) {
      fail("should have nonempty expected field set");
    }

    List<Method> expectedMethods = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers() & Modifier.methodModifiers();
      if (!m.isBridge() && !m.isSynthetic() && isPackageVisible(mods)) {
        expectedMethods.add(m);
      }
    }

    if (expectedMethods.isEmpty()) {
      fail("should have nonempty expected method set");
    }

    Package testPackage = Package.getPackage("randoop.reflection.visibilitytest");
    VisibilityPredicate visibility = new PackageVisibilityPredicate(testPackage);

    assertTrue("class should be visible", visibility.isVisible(c));

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size();
    assertEquals(
        "Expect operations count to be methods plus constructor", expectedCount, actual.size());

    for (Enum<?> e : expectedEnums) {
      assertTrue("enum " + e.name() + " should occur", actual.contains(new EnumConstant(e)));
    }

    for (Field f : expectedFields) {
      assertTrue(
          "field " + f.toGenericString() + " should occur", actual.containsAll(getOperations(f)));
    }

    for (Method m : expectedMethods) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

    for (Constructor<?> co : expectedConstructors) {
      assertTrue(
          "constructor " + co.getName() + " should occur",
          actual.contains(new ConstructorCall(co)));
    }
  }

  /*
   * package private class
   * public visibility
   */
  @Test
  public void testPublicOnlyPackagePrivateVisibility() {
    List<Class<?>> classes = new ArrayList<>();
    Class<?> c = null;
    try {
      c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
      classes.add(c);
    } catch (ClassNotFoundException e) {
      fail("can't access class-under-test: PackagePrivateClass");
    }

    List<Constructor<?>> expectedConstructors = new ArrayList<>();
    for (Constructor<?> co : c.getDeclaredConstructors()) {
      int mods = co.getModifiers() & Modifier.constructorModifiers();
      if (isPubliclyVisible(mods)) {
        expectedConstructors.add(co);
      }
    }
    if (expectedConstructors.isEmpty()) {
      fail("should have nonempty expected constructor set");
    }

    List<Enum<?>> expectedEnums = new ArrayList<>();
    for (Class<?> ic : c.getDeclaredClasses()) {
      int mods = ic.getModifiers() & Modifier.classModifiers();
      if (ic.isEnum() && isPubliclyVisible(mods)) {
        for (Object o : ic.getEnumConstants()) {
          Enum<?> e = (Enum<?>) o;
          expectedEnums.add(e);
        }
      }
    }

    if (expectedEnums.isEmpty()) {
      fail("should have nonempty expected enum set");
    }

    List<Field> expectedFields = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      if (isPubliclyVisible(mods)) {
        expectedFields.add(f);
      }
    }

    if (expectedFields.isEmpty()) {
      fail("should have nonempty expected field set");
    }

    List<Method> expectedMethods = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers() & Modifier.methodModifiers();
      if (!m.isBridge() && !m.isSynthetic() && (isPubliclyVisible(mods))) {
        expectedMethods.add(m);
      }
    }

    if (expectedMethods.isEmpty()) {
      fail("should have nonempty expected set");
    }

    VisibilityPredicate visibility = new PublicVisibilityPredicate();

    assertTrue("class should be visible", visibility.isVisible(c));

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size();
    assertEquals(
        "Expect operations count to be methods plus constructor", expectedCount, actual.size());

    for (Enum<?> e : expectedEnums) {
      assertTrue("enum " + e.name() + " should occur", actual.contains(new EnumConstant(e)));
    }

    for (Field f : expectedFields) {
      assertTrue(
          "field " + f.toGenericString() + " should occur", actual.containsAll(getOperations(f)));
    }

    for (Method m : expectedMethods) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

    for (Constructor<?> co : expectedConstructors) {
      assertTrue(
          "constructor " + co.getName() + " should occur",
          actual.contains(new ConstructorCall(co)));
    }
  }

  /*
   * public class
   * package visibility
   * same package
   */
  @Test
  public void testStandardVisibility() {
    List<Class<?>> classes = new ArrayList<>();
    Class<?> c = PublicClass.class;
    classes.add(c);

    List<Constructor<?>> expectedConstructors = new ArrayList<>();
    for (Constructor<?> co : c.getDeclaredConstructors()) {
      int mods = co.getModifiers() & Modifier.constructorModifiers();
      if (isPackageVisible(mods)) {
        expectedConstructors.add(co);
      }
    }
    if (expectedConstructors.isEmpty()) {
      fail("should have nonempty expected constructor set");
    }

    List<Enum<?>> expectedEnums = new ArrayList<>();
    for (Class<?> ic : c.getDeclaredClasses()) {
      int mods = ic.getModifiers() & Modifier.classModifiers();
      if (ic.isEnum() && isPackageVisible(mods)) {
        for (Object o : ic.getEnumConstants()) {
          Enum<?> e = (Enum<?>) o;
          expectedEnums.add(e);
        }
      }
    }

    if (expectedEnums.isEmpty()) {
      fail("should have nonempty expected enum set");
    }

    List<Field> expectedFields = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      if (isPackageVisible(mods)) {
        expectedFields.add(f);
      }
    }

    if (expectedFields.isEmpty()) {
      fail("should have nonempty expected field set");
    }

    List<Method> expectedMethods = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers() & Modifier.methodModifiers();
      if (!m.isBridge() && !m.isSynthetic() && isPackageVisible(mods)) {
        expectedMethods.add(m);
      }
    }

    if (expectedMethods.isEmpty()) {
      fail("should have nonempty expected  method set");
    }

    Package testPackage = Package.getPackage("randoop.reflection.visibilitytest");
    VisibilityPredicate visibility = new PackageVisibilityPredicate(testPackage);

    assertTrue("class should be visible", visibility.isVisible(c));

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size();
    assertEquals(
        "Expect operations count to be methods plus constructor", expectedCount, actual.size());

    for (Enum<?> e : expectedEnums) {
      assertTrue("enum " + e.name() + " should occur", actual.contains(new EnumConstant(e)));
    }

    for (Field f : expectedFields) {
      assertTrue(
          "field " + f.toGenericString() + " should occur", actual.containsAll(getOperations(f)));
    }

    for (Method m : expectedMethods) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

    for (Constructor<?> co : expectedConstructors) {
      assertTrue(
          "constructor " + co.getName() + " should occur",
          actual.contains(new ConstructorCall(co)));
    }
  }

  /*
   * public class
   * public accessibility
   */
  @Test
  public void testPublicOnlyVisibility() {
    List<Class<?>> classes = new ArrayList<>();
    Class<?> c = PublicClass.class;
    classes.add(c);

    List<Constructor<?>> expectedConstructors = new ArrayList<>();
    for (Constructor<?> co : c.getDeclaredConstructors()) {
      int mods = co.getModifiers() & Modifier.constructorModifiers();
      if (isPubliclyVisible(mods)) {
        expectedConstructors.add(co);
      }
    }
    if (expectedConstructors.isEmpty()) {
      fail("should have nonempty expected constructor set");
    }

    List<Enum<?>> expectedEnums = new ArrayList<>();
    for (Class<?> ic : c.getDeclaredClasses()) {
      int mods = ic.getModifiers() & Modifier.classModifiers();
      if (ic.isEnum() && isPubliclyVisible(mods)) {
        for (Object o : ic.getEnumConstants()) {
          Enum<?> e = (Enum<?>) o;
          expectedEnums.add(e);
        }
      }
    }

    if (expectedEnums.isEmpty()) {
      fail("should have nonempty expected enum set");
    }

    List<Field> expectedFields = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      if (isPubliclyVisible(mods)) {
        expectedFields.add(f);
      }
    }

    if (expectedFields.isEmpty()) {
      fail("should have nonempty expected field set");
    }

    List<Method> expectedMethods = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers();
      if (!m.isBridge() && !m.isSynthetic() && (Modifier.isPublic(mods))) {
        expectedMethods.add(m);
      }
    }

    if (expectedMethods.isEmpty()) {
      fail("should have nonempty expected set");
    }

    VisibilityPredicate visibility = new PublicVisibilityPredicate();

    assertTrue("class should be visible", visibility.isVisible(c));

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size();
    assertEquals(
        "Expect operations count to be methods plus constructor", expectedCount, actual.size());

    for (Enum<?> e : expectedEnums) {
      assertTrue("enum " + e.name() + " should occur", actual.contains(new EnumConstant(e)));
    }

    for (Field f : expectedFields) {
      assertTrue(
          "field " + f.toGenericString() + " should occur", actual.containsAll(getOperations(f)));
    }

    for (Method m : expectedMethods) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

    for (Constructor<?> co : expectedConstructors) {
      assertTrue(
          "constructor " + co.getName() + " should occur",
          actual.contains(new ConstructorCall(co)));
    }
  }

  @Test
  public void checkFieldAccessibility() {
    List<Class<?>> classes = new ArrayList<>();
    Class<?> c = null;
    try {
      c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
      classes.add(c);
    } catch (ClassNotFoundException e) {
      fail("can't access class-under-test: PackagePrivateClass");
    }

    Constructor<?> con = null;
    try {
      con = c.getConstructor(int.class);
    } catch (NoSuchMethodException e) {
      fail("can't find constructor " + e);
    } catch (SecurityException e) {
      fail("can't access constructor " + e);
    }

    Object o = null;
    try {
      o = con.newInstance(10);
    } catch (InstantiationException e) {
      fail("constructor failed");
    } catch (IllegalAccessException e) {
      fail("cannot access constructor");
    } catch (IllegalArgumentException e) {
      fail("bad argument to constructor");
    } catch (InvocationTargetException e) {
      fail("constructor threw exception");
    }

    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      if (isPackageVisible(mods)) {
        Collection<Operation> ops = getOperations(f);
        for (Operation op : ops) {
          ExecutionOutcome result;
          if (op.getInputTypes().size() == 2) {
            Object[] input = new Object[] {o, 10};
            try {
              result = op.execute(input, null);
              assertTrue("result should be normal execution", (result instanceof NormalExecution));
            } catch (Throwable t) {
              fail("should not throw exception: " + t);
            }

          } else {
            Object[] input = new Object[] {o};
            try {
              result = op.execute(input, null);
              assertTrue("result should be normal execution", (result instanceof NormalExecution));
            } catch (Throwable t) {
              fail("should not throw exception: " + t);
            }
          }
        }
      }
    }
  }

  private boolean isPubliclyVisible(int mods) {
    return Modifier.isPublic(mods);
  }

  private boolean isPackageVisible(int mods) {
    return Modifier.isPublic(mods) || !Modifier.isPrivate(mods);
  }

  private Collection<Operation> getOperations(Field f) {
    List<Operation> statements = new ArrayList<>();
    int mods = f.getModifiers();
    if (Modifier.isStatic(mods)) {
      if (Modifier.isFinal(mods)) {
        statements.add(new FieldGet(new StaticFinalField(f)));
      } else {
        statements.add(new FieldGet(new StaticField(f)));
        statements.add(new FieldSet(new StaticField(f)));
      }
    } else {
      if (Modifier.isFinal(mods)) {
        statements.add(new FieldGet(new FinalInstanceField(f)));
      } else {
        statements.add(new FieldGet(new InstanceField(f)));
        statements.add(new FieldSet(new InstanceField(f)));
      }
    }
    return statements;
  }
}
