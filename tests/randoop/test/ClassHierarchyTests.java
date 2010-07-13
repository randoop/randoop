package randoop.test;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import randoop.util.ClassHierarchy;

public class ClassHierarchyTests extends TestCase {
  public void testClosure1() throws Exception {
    Set<Class<?>> closure = ClassHierarchy.superClassClosure(String.class);
    Set<Class<?>> expected = new LinkedHashSet<Class<?>>();
    expected.add(String.class);
    expected.add(Serializable.class);
    expected.add(Comparable.class);
    expected.add(CharSequence.class);
    expected.add(Object.class);
    assertEquals(expected, closure);
  }

  public void testClosure2() throws Exception {
    Set<Class<?>> closure = ClassHierarchy.superClassClosure(LinkedHashSet.class);
    Set<Class<?>> expected = new LinkedHashSet<Class<?>>();
    expected.add(java.util.LinkedHashSet.class);
    expected.add(java.util.HashSet.class);
    expected.add(java.util.AbstractSet.class);
    expected.add(java.util.AbstractCollection.class);
    expected.add(java.lang.Object.class);
    expected.add(java.util.Collection.class);
    expected.add(java.lang.Iterable.class);
    expected.add(java.util.Set.class);
    expected.add(java.lang.Cloneable.class);
    expected.add(java.io.Serializable.class);
    assertEquals(expected, closure);
  }

  public void test1() throws Exception {
    Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    classes.addAll(ClassHierarchy.superClassClosure(LinkedHashSet.class));
    ClassHierarchy h = new ClassHierarchy(classes);
    assertTrue(h.superClasses(java.util.Set.class).toString(), h.superClasses(java.util.Set.class).contains(Iterable.class));
    assertTrue(!h.superClasses(java.util.Set.class).contains(LinkedHashSet.class));
    assertTrue(h.subClasses(java.util.Set.class).contains(LinkedHashSet.class));
    assertTrue(!h.subClasses(java.util.Set.class).contains(Cloneable.class));
  }
}
