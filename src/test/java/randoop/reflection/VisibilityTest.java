package randoop.reflection;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.field.AccessibleField;
import randoop.operation.CallableOperation;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGet;
import randoop.operation.FieldSet;
import randoop.operation.MethodCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.visibilitytest.PublicClass;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ConcreteTypes;
import randoop.types.Type;
import randoop.types.RandoopTypeException;
import randoop.types.SimpleClassOrInterfaceType;
import randoop.types.TypeTuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VisibilityTest {

  /*
   * package private class
   * package visibility
   * same package
   */
  @Test
  public void testStandardPackagePrivateVisibility() {

    Class<?> c = null;
    try {
      c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
    } catch (ClassNotFoundException e) {
      fail("can't access class-under-test: PackagePrivateClass");
    }
    ClassOrInterfaceType declaringType = new SimpleClassOrInterfaceType(c);

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

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    Set<TypedOperation> actual = getConcreteOperations(c, reflectionPredicate, visibility);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size();
    assertEquals(
        "Expect operations count to be methods plus constructor", expectedCount, actual.size());

    for (Enum<?> e : expectedEnums) {
      assertTrue("enum " + e.name() + " should occur", actual.contains(createEnumOperation(e)));
    }

    for (Field f : expectedFields) {
      assertTrue(
          "field " + f.toGenericString() + " should occur",
          actual.containsAll(getOperations(f, declaringType)));
    }

    try {
      for (Method m : expectedMethods) {
        assertTrue(
            "method " + m.getName() + " should occur",
            actual.contains(createMethodCall(m, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("Type error: " + e.getMessage());
    }

    try {
      for (Constructor<?> co : expectedConstructors) {
        assertTrue(
            "constructor " + co.getName() + " should occur",
            actual.contains(createConstructorCall(co)));
      }
    } catch (RandoopTypeException e) {
      fail("Type error: " + e.getMessage());
    }
  }

  /*
   * package private class
   * public visibility
   */
  @Test
  public void testPublicOnlyPackagePrivateVisibility() {
    Class<?> c = null;
    try {
      c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
    } catch (ClassNotFoundException e) {
      fail("can't access class-under-test: PackagePrivateClass");
    }
    ClassOrInterfaceType declaringType = new SimpleClassOrInterfaceType(c);

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

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    Set<TypedOperation> actual = getConcreteOperations(c, reflectionPredicate, visibility);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size();
    assertEquals(
        "Expect operations count to be methods plus constructor", expectedCount, actual.size());

    for (Enum<?> e : expectedEnums) {
      assertTrue("enum " + e.name() + " should occur", actual.contains(createEnumOperation(e)));
    }

    for (Field f : expectedFields) {
      assertTrue(
          "field " + f.toGenericString() + " should occur",
          actual.containsAll(getOperations(f, declaringType)));
    }

    try {
      for (Method m : expectedMethods) {
        assertTrue(
            "method " + m.getName() + " should occur",
            actual.contains(createMethodCall(m, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("Type error: " + e.getMessage());
    }

    try {
      for (Constructor<?> co : expectedConstructors) {
        assertTrue(
            "constructor " + co.getName() + " should occur",
            actual.contains(createConstructorCall(co)));
      }
    } catch (RandoopTypeException e) {
      fail("Type error: " + e.getMessage());
    }
  }

  /*
   * public class
   * package visibility
   * same package
   */
  @Test
  public void testStandardVisibility() {
    Class<?> c = PublicClass.class;
    ClassOrInterfaceType declaringType = new SimpleClassOrInterfaceType(c);

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

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    Set<TypedOperation> actual = getConcreteOperations(c, reflectionPredicate, visibility);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size();
    assertEquals(
        "Expect operations count to be methods plus constructor", expectedCount, actual.size());

    for (Enum<?> e : expectedEnums) {
      assertTrue("enum " + e.name() + " should occur", actual.contains(createEnumOperation(e)));
    }

    for (Field f : expectedFields) {
      assertTrue(
          "field " + f.toGenericString() + " should occur",
          actual.containsAll(getOperations(f, declaringType)));
    }

    try {
      for (Method m : expectedMethods) {
        assertTrue(
            "method " + m.getName() + " should occur",
            actual.contains(createMethodCall(m, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("Type error: " + e.getMessage());
    }

    try {
      for (Constructor<?> co : expectedConstructors) {
        assertTrue(
            "constructor " + co.getName() + " should occur",
            actual.contains(createConstructorCall(co)));
      }
    } catch (RandoopTypeException e) {
      fail("Type error: " + e.getMessage());
    }
  }

  /*
   * public class
   * public accessibility
   */
  @Test
  public void testPublicOnlyVisibility() {
    Class<?> c = PublicClass.class;

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
    ClassOrInterfaceType declaringType = new SimpleClassOrInterfaceType(c);

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

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    Set<TypedOperation> actual = getConcreteOperations(c, reflectionPredicate, visibility);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size();
    assertEquals(
        "Expect operations count to be methods plus constructor", expectedCount, actual.size());

    for (Enum<?> e : expectedEnums) {
      assertTrue("enum " + e.name() + " should occur", actual.contains(createEnumOperation(e)));
    }

    try {
      for (Field f : expectedFields) {
        assertTrue(
            "field " + f.toGenericString() + " should occur",
            actual.containsAll(getOperations(f, declaringType)));
      }

      for (Method m : expectedMethods) {
        assertTrue(
            "method " + m.getName() + " should occur",
            actual.contains(createMethodCall(m, declaringType)));
      }

      for (Constructor<?> co : expectedConstructors) {
        assertTrue(
            "constructor " + co.getName() + " should occur",
            actual.contains(createConstructorCall(co)));
      }
    } catch (RandoopTypeException e) {
      fail("Type error: " + e.getMessage());
    }
  }

  @Test
  public void checkFieldAccessibility() {
    Class<?> c = null;
    try {
      c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
    } catch (ClassNotFoundException e) {
      fail("can't access class-under-test: PackagePrivateClass");
    }
    ClassOrInterfaceType declaringType = new SimpleClassOrInterfaceType(c);

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
        List<TypedOperation> ops = getOperations(f, declaringType);
        for (TypedOperation op : ops) {
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

  /**
   * getOperations maps a field into possible operations.
   * Looks at modifiers to decide which kind of field wrapper
   * to create and then builds list with getter and setter.
   *
   * @param f reflective Field object
   * @return a list of getter/setter statements for the field
   */
  private List<TypedOperation> getOperations(Field f, ClassOrInterfaceType declaringType) {
    List<TypedOperation> statements = new ArrayList<>();
    Type fieldType;
    fieldType = Type.forType(f.getGenericType());
    AccessibleField field = new AccessibleField(f, declaringType);
    List<Type> getInputTypeList = new ArrayList<>();
    List<Type> setInputTypeList = new ArrayList<>();
    if (!field.isStatic()) {
      getInputTypeList.add(declaringType);
      setInputTypeList.add(declaringType);
    }

    statements.add(
        new TypedClassOperation(
            new FieldGet(field), declaringType, new TypeTuple(getInputTypeList), fieldType));

    if (!field.isFinal()) {
      setInputTypeList.add(fieldType);
      statements.add(
          new TypedClassOperation(
              new FieldSet(field),
              declaringType,
              new TypeTuple(setInputTypeList),
              ConcreteTypes.VOID_TYPE));
    }
    return statements;
  }

  private Set<TypedOperation> getConcreteOperations(
      Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    OperationExtractor extractor =
        new OperationExtractor(classType, operations, predicate, new OperationModel());
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.add(extractor);
    manager.apply(c);
    return operations;
  }

  private TypedOperation createEnumOperation(Enum<?> e) {
    CallableOperation eOp = new EnumConstant(e);
    ClassOrInterfaceType enumType = new SimpleClassOrInterfaceType(e.getDeclaringClass());
    return new TypedClassOperation(eOp, enumType, new TypeTuple(), enumType);
  }

  private TypedOperation createConstructorCall(Constructor<?> con) throws RandoopTypeException {
    ConstructorCall op = new ConstructorCall(con);
    ClassOrInterfaceType declaringType = ClassOrInterfaceType.forClass(con.getDeclaringClass());
    List<Type> paramTypes = new ArrayList<>();
    for (Class<?> pc : con.getParameterTypes()) {
      paramTypes.add(Type.forClass(pc));
    }
    return new TypedClassOperation(op, declaringType, new TypeTuple(paramTypes), declaringType);
  }

  private TypedOperation createMethodCall(Method m, ClassOrInterfaceType declaringType)
      throws RandoopTypeException {
    MethodCall op = new MethodCall(m);
    List<Type> paramTypes = new ArrayList<>();
    if (!Modifier.isStatic(m.getModifiers() & Modifier.methodModifiers())) {
      paramTypes.add(declaringType);
    }
    for (Class<?> pc : m.getParameterTypes()) {
      paramTypes.add(Type.forClass(pc));
    }
    Type outputType = Type.forClass(m.getReturnType());
    return new TypedClassOperation(op, declaringType, new TypeTuple(paramTypes), outputType);
  }
}
