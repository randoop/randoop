package randoop.input.toradocu;

import net.Connection;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import plume.Pair;
import randoop.condition.Condition;
import randoop.condition.ConditionCollection;
import randoop.test.ExpectedExceptionGenerator;
import randoop.test.TestCheckGenerator;
import randoop.types.ClassOrInterfaceType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test whether Toradocu input is being read properly and can be used in evaluating conditions.
 */
public class DeserializationTest {

  @Test
  public void connectionReadTest() {

    ClassLoader loader = this.getClass().getClassLoader();
    assert loader != null : "ClassLoader should not be null";
    String path = loader.getResource("ConnectionConditions.json").getFile();
    List<File> jsonFiles = new ArrayList<>();
    jsonFiles.add(new File(path));
    ConditionCollection conditions =
        ToradocuConditionCollection.createToradocuConditions(jsonFiles);

    Class<?> declaringClass = getClass("net.Connection");
    assert declaringClass != null : "did not load net.Connection class";
    for (Method method : declaringClass.getDeclaredMethods()) {
      if (method.getName().equals("$jacocoInit")) {
        continue;
      }
      List<Condition> preconditions = conditions.getPreconditions(method);
      if (method.getName().equals("send") && method.getParameterTypes()[0].equals(int.class)) {
        assertThat("should be one precondition", preconditions.size(), is(equalTo(1)));
        Condition precondition = preconditions.get(0);
        Object[] args = new Object[] {null, 1};
        assertTrue("condition should confirm one is positive", precondition.check(args));
        args = new Object[] {null, -1};
        assertFalse("condition should confirm minus one is not positive", precondition.check(args));

      } else {
        assertThat(
            "should be one or no precondition",
            preconditions.size(),
            anyOf(equalTo(0), equalTo(1)));
      }

      Map<Condition, Pair<TestCheckGenerator, TestCheckGenerator>> throwsConditions =
          conditions.getThrowsConditions(method);
      assertTrue(
          "every method other than isOpen() should have throws, failed for " + method.getName(),
          method.getName().equals("isOpen") || !throwsConditions.isEmpty());
      if (method.getName().equals("open")) {
        assertThat("open() has one throws", throwsConditions.size(), is(equalTo(1)));
        for (Map.Entry<Condition, Pair<TestCheckGenerator, TestCheckGenerator>> entry :
            throwsConditions.entrySet()) {
          Condition throwsCondition = entry.getKey();
          Connection connection = new Connection();
          Object[] args = new Object[] {connection};
          assertFalse(
              "throws condition should confirm object is not open", throwsCondition.check(args));
          connection.open();
          assertTrue("throws condition should confirm object is open", throwsCondition.check(args));

          assertTrue(
              "should be ExpectedExceptionGenerator",
              entry.getValue().a instanceof ExpectedExceptionGenerator);
          assertThat(
              "thrown exception incorrect",
              ((ExpectedExceptionGenerator) entry.getValue().a).getExpected(),
              is(equalTo(ClassOrInterfaceType.forClass(IllegalStateException.class))));
          assertTrue("should be null", entry.getValue().b == null);
        }
      }
      if (method.getName().equals("send")) {
        if (method.getParameterTypes()[0].equals(String.class)) {
          assertThat(
              "send(String) has two throws-conditions", throwsConditions.size(), is(equalTo(2)));
          Iterator<Map.Entry<Condition, Pair<TestCheckGenerator, TestCheckGenerator>>> iterator =
              throwsConditions.entrySet().iterator();
          assert iterator.hasNext();
          Map.Entry<Condition, Pair<TestCheckGenerator, TestCheckGenerator>> entry =
              iterator.next();
          Condition throwsCondition = entry.getKey();
          Connection connection = new Connection();
          Object[] args = new Object[] {connection, null};
          assertTrue(
              "throws-condition should confirm argument is null", throwsCondition.check(args));
          args = new Object[] {connection, "blah"};
          assertFalse(
              "throws-condition should confirm argument is not null", throwsCondition.check(args));
          assertTrue(
              "should be ExpectedExceptionGenerator",
              entry.getValue().a instanceof ExpectedExceptionGenerator);
          assertThat(
              "thrown exception",
              ((ExpectedExceptionGenerator) entry.getValue().a).getExpected(),
              is(equalTo(ClassOrInterfaceType.forClass(NullPointerException.class))));

          assert iterator.hasNext();
          entry = iterator.next();
          throwsCondition = entry.getKey();
          args = new Object[] {connection, "dummy"};
          assertTrue("not open, so should be true", throwsCondition.check(args));
          connection.open();
          assertFalse("open, so should be false", throwsCondition.check(args));
          assertTrue(
              "should be ExpectedExceptionGenerator",
              entry.getValue().a instanceof ExpectedExceptionGenerator);
          assertThat(
              "thrown exception",
              ((ExpectedExceptionGenerator) entry.getValue().a).getExpected(),
              is(equalTo(ClassOrInterfaceType.forClass(IllegalStateException.class))));
        }
        if (method.getParameterTypes()[0].equals(int.class)) {
          assertThat("send(int) has one throws", throwsConditions.size(), is(equalTo(1)));
          Iterator<Map.Entry<Condition, Pair<TestCheckGenerator, TestCheckGenerator>>> iterator =
              throwsConditions.entrySet().iterator();
          assert iterator.hasNext();
          Map.Entry<Condition, Pair<TestCheckGenerator, TestCheckGenerator>> entry =
              iterator.next();
          Condition throwsCondition = entry.getKey();
          Connection connection = new Connection();
          Object[] args = new Object[] {connection, 1};
          assertTrue("not open, so should be true", throwsCondition.check(args));
          connection.open();
          assert connection.isOpen();
          assertFalse("open, so should be false", throwsCondition.check(args));
          assertTrue(
              "should be ExpectedExceptionGenerator",
              entry.getValue().a instanceof ExpectedExceptionGenerator);
          assertThat(
              "thrown exception",
              ((ExpectedExceptionGenerator) entry.getValue().a).getExpected(),
              is(equalTo(ClassOrInterfaceType.forClass(IllegalStateException.class))));
        }
      }
    }
  }

  @Test
  public void inheritedMethodsTest() {
    ClassLoader loader = this.getClass().getClassLoader();
    assert loader != null : "ClassLoader should not be null";
    List<File> jsonFiles = new ArrayList<>();
    jsonFiles.add(new File(loader.getResource("SubClassConditions.json").getFile()));
    ConditionCollection conditions =
        ToradocuConditionCollection.createToradocuConditions(jsonFiles);

    Class<?> declaringClass = getClass("pkg.SubClass");
    assert declaringClass != null : "did not load pkg.SubClass class";
    for (Method method : declaringClass.getMethods()) {
      if (method.getName().equals("$jacocoInit") || !method.getName().contains("methodWith")) {
        continue;
      }
      System.out.println("method: " + method.getName());
      List<Condition> preconditions = conditions.getPreconditions(method);
      for (Condition condition : preconditions) System.out.println("precondition: " + condition);
      assertThat("should be one precondition", preconditions.size(), is(equalTo(1)));
      Condition precondition = preconditions.get(0);
      Object[] args = new Object[] {null, 1};
      assertTrue("condition should confirm one is positive", precondition.check(args));
      args = new Object[] {null, -1};
      assertFalse("condition should confirm minus one is not positive", precondition.check(args));
    }
  }

  private Class<?> getClass(String classname) {
    try {
      return Class.forName(classname);
    } catch (ClassNotFoundException e) {
      fail("cannot open condition class");
    }
    return null;
  }
}
