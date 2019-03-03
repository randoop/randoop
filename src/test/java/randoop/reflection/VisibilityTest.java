package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import randoop.ExecutionOutcome;
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
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.RandoopTypeException;
import randoop.types.Type;
import randoop.types.TypeTuple;

public class VisibilityTest {

  // TODO need to add test case for when o.getPackage()==null for objects passed to predicate

  /*
   * package private class
   * package visibility
   * same package
   */
  @Test
  public void testStandardPackagePrivateVisibility() throws ClassNotFoundException {

    Class<?> c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);

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

    VisibilityPredicate visibility =
        new VisibilityPredicate.PackageVisibilityPredicate("randoop.reflection.visibilitytest");

    assertTrue("class should be visible", visibility.isVisible(c));

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    Set<TypedOperation> actual = getConcreteOperations(c, reflectionPredicate, visibility);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size()
            + 1;
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
  public void testPublicOnlyPackagePrivateVisibility() throws ClassNotFoundException {
    Class<?> c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);

    List<Constructor<?>> expectedConstructors = new ArrayList<>();
    for (Constructor<?> co : c.getDeclaredConstructors()) {
      int mods = co.getModifiers() & Modifier.constructorModifiers();
      if (isPubliclyVisible(mods)) {
        expectedConstructors.add(co);
      }
    }
    assertFalse("should have nonempty expected constructor set", expectedConstructors.isEmpty());

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

    assertFalse("should have nonempty expected enum set", expectedEnums.isEmpty());

    List<Field> expectedFields = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      if (isPubliclyVisible(mods)) {
        expectedFields.add(f);
      }
    }

    assertFalse("should have nonempty expected field set", expectedFields.isEmpty());

    List<Method> expectedMethods = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers() & Modifier.methodModifiers();
      if (!m.isBridge() && !m.isSynthetic() && isPubliclyVisible(mods)) {
        expectedMethods.add(m);
      }
    }

    assertFalse("should have nonempty expected set", expectedMethods.isEmpty());

    VisibilityPredicate visibility = IS_PUBLIC;

    assertFalse("class should not be visible", visibility.isVisible(c));

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    Set<TypedOperation> actual = getConcreteOperations(c, reflectionPredicate, visibility);

    assertEquals("Expect operations count to be methods plus constructor", 0, actual.size());

    for (Enum<?> e : expectedEnums) {
      assertFalse(
          "enum " + e.name() + " should not occur", actual.contains(createEnumOperation(e)));
    }

    for (Field f : expectedFields) {
      assertFalse(
          "field " + f.toGenericString() + " should occur",
          actual.containsAll(getOperations(f, declaringType)));
    }

    try {
      for (Method m : expectedMethods) {
        assertFalse(
            "method " + m.getName() + " should occur",
            actual.contains(createMethodCall(m, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("Type error: " + e.getMessage());
    }

    try {
      for (Constructor<?> co : expectedConstructors) {
        assertFalse(
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
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);

    List<Constructor<?>> expectedConstructors = new ArrayList<>();
    for (Constructor<?> co : c.getDeclaredConstructors()) {
      int mods = co.getModifiers() & Modifier.constructorModifiers();
      if (isPackageVisible(mods)) {
        expectedConstructors.add(co);
      }
    }

    assertFalse("should have nonempty expected constructor set", expectedConstructors.isEmpty());

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

    assertFalse("should have nonempty expected enum set", expectedEnums.isEmpty());

    List<Field> expectedFields = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      if (isPackageVisible(mods)) {
        expectedFields.add(f);
      }
    }

    assertFalse("should have nonempty expected field set", expectedFields.isEmpty());

    List<Method> expectedMethods = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers() & Modifier.methodModifiers();
      if (!m.isBridge() && !m.isSynthetic() && isPackageVisible(mods)) {
        expectedMethods.add(m);
      }
    }

    assertFalse("should have nonempty expected  method set", expectedMethods.isEmpty());

    VisibilityPredicate visibility =
        new VisibilityPredicate.PackageVisibilityPredicate("randoop.reflection.visibilitytest");

    assertTrue("class should be visible", visibility.isVisible(c));

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    Set<TypedOperation> actual = getConcreteOperations(c, reflectionPredicate, visibility);

    for (Enum<?> e : expectedEnums) {
      assertTrue(
          "enum value " + e.name() + " should occur", actual.contains(createEnumOperation(e)));
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

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size()
            + 1;
    assertEquals(
        "Expect operations count to be methods plus constructor", expectedCount, actual.size());
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
    assertFalse("should have nonempty expected constructor set", expectedConstructors.isEmpty());

    ClassOrInterfaceType declaringType = new NonParameterizedType(c);

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

    assertFalse("should have nonempty expected enum set", expectedEnums.isEmpty());

    List<Field> expectedFields = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      if (isPubliclyVisible(mods)) {
        expectedFields.add(f);
      }
    }

    assertFalse("should have nonempty expected field set", expectedFields.isEmpty());

    List<Method> expectedMethods = new ArrayList<>();
    for (Method m : c.getDeclaredMethods()) {
      int mods = m.getModifiers();
      if (!m.isBridge() && !m.isSynthetic() && Modifier.isPublic(mods)) {
        expectedMethods.add(m);
      }
    }

    assertFalse("should have nonempty expected set", expectedMethods.isEmpty());

    VisibilityPredicate visibility = IS_PUBLIC;

    assertTrue("class should be visible", visibility.isVisible(c));

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();

    assertTrue("class should be OK by reflection predicate", reflectionPredicate.test(c));

    Set<TypedOperation> actual = getConcreteOperations(c, reflectionPredicate, visibility);

    int expectedCount =
        expectedMethods.size()
            + 2 * expectedFields.size()
            + expectedEnums.size()
            + expectedConstructors.size()
            + 1;
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
  public void checkFieldAccessibility()
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          InvocationTargetException {
    Class<?> c = Class.forName("randoop.reflection.visibilitytest.PackagePrivateClass");
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);

    Constructor<?> con = null;
    try {
      con = c.getConstructor(int.class);
    } catch (NoSuchMethodException e) {
      fail("can't find constructor " + e);
    } catch (SecurityException e) {
      fail("can't access constructor " + e);
    }

    con.setAccessible(true);

    Object o = con.newInstance(10);

    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      if (isPackageVisible(mods)) {
        List<TypedOperation> ops = getOperations(f, declaringType);
        for (TypedOperation op : ops) {
          @SuppressWarnings("UnusedVariable")
          ExecutionOutcome result;
          if (op.getInputTypes().size() == 2) {
            Object[] input = new Object[] {o, 10};
            result = op.execute(input);
          } else {
            Object[] input = new Object[] {o};
            result = op.execute(input);
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
   * getOperations maps a field into possible operations. Looks at modifiers to decide which kind of
   * field wrapper to create and then builds list with getter and setter.
   *
   * @param f reflective Field object
   * @return a list of getter/setter statements for the field
   */
  private List<TypedOperation> getOperations(Field f, ClassOrInterfaceType declaringType) {
    List<TypedOperation> statements = new ArrayList<>();
    Type fieldType = Type.forType(f.getGenericType());
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
              JavaTypes.VOID_TYPE));
    }
    return statements;
  }

  private Set<TypedOperation> getConcreteOperations(
      Class<?> c,
      ReflectionPredicate reflectionPredicate,
      VisibilityPredicate visibilityPredicate) {
    ReflectionManager typeManager = new ReflectionManager(visibilityPredicate);
    Set<ClassOrInterfaceType> classTypes = new LinkedHashSet<>();
    typeManager.apply(new DeclarationExtractor(classTypes, reflectionPredicate), c);
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager opManager = new ReflectionManager(visibilityPredicate);
    for (ClassOrInterfaceType type : classTypes) {
      final OperationExtractor extractor =
          new OperationExtractor(type, reflectionPredicate, visibilityPredicate);
      opManager.apply(extractor, type.getRuntimeClass());
      operations.addAll(extractor.getOperations());
    }
    return operations;
  }

  private TypedOperation createEnumOperation(Enum<?> e) {
    CallableOperation eOp = new EnumConstant(e);
    ClassOrInterfaceType enumType = new NonParameterizedType(e.getDeclaringClass());
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
