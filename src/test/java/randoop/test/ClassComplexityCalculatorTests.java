package randoop.test;

import java.io.FileWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import randoop.util.ClassComplexityCalculator;
import randoop.util.ClassHierarchy;

public class ClassComplexityCalculatorTests extends TestCase {
  public void test1() throws Exception {
    Set<Class<?>> classes = ClassHierarchy.superClassClosure(LinkedHashSet.class);
    ClassComplexityCalculator ccc = new ClassComplexityCalculator(classes);
    for (Class<?> c : classes) {
      assertEquals(1, ccc.classComplexity(c));
    }
  }

  public void test2() throws Exception {
    Set<Class<?>> classes = ClassHierarchy.superClassClosure(FileWriter.class);
    ClassComplexityCalculator ccc = new ClassComplexityCalculator(classes);
    assertEquals(FileWriter.class.getName(), 2, ccc.classComplexity(FileWriter.class));
  }

  public void test3() throws Exception {
    Set<Class<?>> x = new LinkedHashSet<Class<?>>();
    x.add(A.class);
    x.add(B.class);
    x.add(C.class);
    x.add(Integer.class);
    x.add(String.class);
    Set<Class<?>> classes = ClassHierarchy.superClassClosure(x);
    ClassComplexityCalculator ccc = new ClassComplexityCalculator(classes);
    assertEquals(A.class.getName(), 5, ccc.classComplexity(A.class));
    assertEquals(B.class.getName(), 4, ccc.classComplexity(B.class));
    assertEquals(C.class.getName(), 3, ccc.classComplexity(C.class));
  }

  public static class A {
    public A(B b) { //
    }
  }

  public static class B {
    public B(B b) {}

    public B(C c) {}
  }

  public static class C {
    public C(Integer i) {}
  }
}
