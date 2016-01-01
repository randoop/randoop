package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.reflection.visibilitytest.PublicClass;

public class VisibilityTest {

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

    List<Method> include = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers();
      if (! m.isBridge() && ! m.isSynthetic() && (Modifier.isPublic(mods) || ! Modifier.isPrivate(mods))) {
        include.add(m);
      }
    }
    
    if (include.isEmpty()) {
      fail("should have nonempty expected set");
    }
    
    Package testPackage = Package.getPackage("randoop.reflection.visibilitytest");
    VisibilityPredicate visibility = new PackageVisibilityPredicate(testPackage);
    
    assertTrue("class should be visible", visibility.isVisible(c));
    
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);
    
    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));
    
    boolean publicMembersOnly = false;
    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate, publicMembersOnly );
    
    assertEquals("Expect operations count to be methods plus constructor", include.size() + 1, actual.size());
    
    for (Method m : include) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

  }

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

    List<Method> include = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers();
      if (! m.isBridge() && ! m.isSynthetic() && (Modifier.isPublic(mods))) {
        include.add(m);
      }
    }
    
    if (include.isEmpty()) {
      fail("should have nonempty expected set");
    }
    
    Package testPackage = Package.getPackage("randoop.reflection.visibilitytest");
    VisibilityPredicate visibility = new PackageVisibilityPredicate(testPackage);
    
    assertTrue("class should be visible", visibility.isVisible(c));
    
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);
    
    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));
    
    boolean publicMembersOnly = true;
    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate, publicMembersOnly );
    
    assertEquals("Expect operations count to be methods plus constructor", include.size() + 1, actual.size());
    
    for (Method m : include) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

  }
  
  @Test
  public void testStandardPackageVisibility() {
    List<Class<?>> classes = new ArrayList<>();
    Class<?> c = null;
    try {
      c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
      classes.add(c);
    } catch (ClassNotFoundException e) {
      fail("can't access class-under-test: PackagePrivateClass");
    }

    List<Method> include = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers();
      if (! m.isBridge() && ! m.isSynthetic() && (Modifier.isPublic(mods))) {
        include.add(m);
      }
    }
    
    if (include.isEmpty()) {
      fail("should have nonempty expected set");
    }
    
    Package testPackage = Package.getPackage("randoop.reflection");
    VisibilityPredicate visibility = new PackageVisibilityPredicate(testPackage);
    
    assertTrue("class should be visible", visibility.isVisible(c));
    
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);
    
    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));
    
    boolean publicMembersOnly = false;
    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate, publicMembersOnly );
    
    assertEquals("Expect operations count to be methods plus constructor", include.size() + 1, actual.size());
    
    for (Method m : include) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

  }

  @Test
  public void testPublicOnlyPublicVisibility() {
    List<Class<?>> classes = new ArrayList<>();
    Class<?> c = null;
    try {
      c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
      classes.add(c);
    } catch (ClassNotFoundException e) {
      fail("can't access class-under-test: PackagePrivateClass");
    }

    List<Method> include = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers();
      if (! m.isBridge() && ! m.isSynthetic() && (Modifier.isPublic(mods))) {
        include.add(m);
      }
    }
    
    if (include.isEmpty()) {
      fail("should have nonempty expected set");
    }
    
    Package testPackage = Package.getPackage("randoop.reflection");
    VisibilityPredicate visibility = new PackageVisibilityPredicate(testPackage);
    
    assertTrue("class should be visible", visibility.isVisible(c));
    
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);
    
    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));
    
    boolean publicMembersOnly = true;
    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate, publicMembersOnly );
    
    assertEquals("Expect operations count to be methods plus constructor", include.size() + 1, actual.size());
    
    for (Method m : include) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

  }
  
  @Test
  public void testStandardVisibility() {
    List<Class<?>> classes = new ArrayList<>();
    Class<?> c = PublicClass.class;
    classes.add(c);

    List<Method> include = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers();
      if (! m.isBridge() && ! m.isSynthetic() && (Modifier.isPublic(mods) || ! Modifier.isPrivate(mods))) {
        include.add(m);
      }
    }
    
    if (include.isEmpty()) {
      fail("should have nonempty expected set");
    }
    
    Package testPackage = Package.getPackage("randoop.reflection.visibilitytest");
    VisibilityPredicate visibility = new PackageVisibilityPredicate(testPackage);
    
    assertTrue("class should be visible", visibility.isVisible(c));
    
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);
    
    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));
    
    boolean publicMembersOnly = false;
    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate, publicMembersOnly );
    
    assertEquals("Expect operations count to be methods plus constructor", include.size() + 1, actual.size());
    
    for (Method m : include) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

  }

  @Test
  public void testPublicOnlyVisibility() {
    List<Class<?>> classes = new ArrayList<>();
    Class<?> c = PublicClass.class;
    classes.add(c);

    List<Method> include = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers();
      if (! m.isBridge() && ! m.isSynthetic() && (Modifier.isPublic(mods))) {
        include.add(m);
      }
    }
    
    if (include.isEmpty()) {
      fail("should have nonempty expected set");
    }
    
    Package testPackage = Package.getPackage("randoop.reflection");
    VisibilityPredicate visibility = new PackageVisibilityPredicate(testPackage);
    
    assertTrue("class should be visible", visibility.isVisible(c));
    
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(visibility);
    
    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));
    
    boolean publicMembersOnly = true;
    List<Operation> actual = OperationExtractor.getOperations(classes, reflectionPredicate, publicMembersOnly );
    
    assertEquals("Expect operations count to be methods plus constructor", include.size() + 1, actual.size());
    
    for (Method m : include) {
      assertTrue("method " + m.getName() + " should occur", actual.contains(new MethodCall(m)));
    }

  }
}
