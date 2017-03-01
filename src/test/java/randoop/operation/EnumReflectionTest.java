package randoop.operation;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.test.ClassWithInnerEnum;
import randoop.test.Coin;
import randoop.test.EnumAsPredicate;
import randoop.test.OperatorEnum;
import randoop.test.PlayingCard;
import randoop.test.SimpleEnum;
import randoop.types.ClassOrInterfaceType;
import randoop.types.InstantiatedType;
import randoop.types.NonParameterizedType;
import randoop.types.RandoopTypeException;
import randoop.types.Type;
import randoop.types.TypeTuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * EnumReflectionTest consists of tests of reflection classes
 * to verify what is collected from enums and classes using enums. In particular,
 * want to collect enum constants, methods of enum (esp. if abstract), enums that are
 * are inner types.
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
    Class<?> se = SimpleEnum.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(se);

    @SuppressWarnings("unchecked")
    List<Enum<?>> include = asList(se.getEnumConstants());
    @SuppressWarnings("unchecked")
    List<Method> exclude = Arrays.asList(se.getMethods());
    Set<TypedOperation> actual = getConcreteOperations(se);

    assertEquals("number of statements", include.size(), actual.size());

    for (Enum<?> e : include) {
      assertTrue(
          "enum constant " + e.name() + " should occur", actual.contains(createEnumOperation(e)));
    }
    for (Method m : exclude) {
      assertFalse(
          "method " + m.toGenericString() + " should not occur in simple enum",
          actual.contains(createMethodCall(m, declaringType)));
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
   */
  @SuppressWarnings("unchecked")
  @Test
  public void innerEnum() {
    Class<?> pc = PlayingCard.class;

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

    Set<TypedOperation> actual = getConcreteOperations(pc);
    assertEquals("number of statements", include.size() + 5, actual.size());

    for (Enum<?> e : include) {
      assertTrue(
          "enum constant " + e.name() + " should occur", actual.contains(createEnumOperation(e)));
    }
    for (Enum<?> e : exclude) {
      assertFalse(
          "enum constant " + e.name() + " should not occur",
          actual.contains(createEnumOperation(e)));
    }
  }

  @Test
  public void innerEnumWithMethodsTest() {
    Class<?> cwim = ClassWithInnerEnum.class;

    List<TypedOperation> include = new ArrayList<>();
    List<TypedOperation> exclude = new ArrayList<>();
    for (Class<?> c : cwim.getDeclaredClasses()) {
      if (c.isEnum()) {
        ClassOrInterfaceType enumType = ClassOrInterfaceType.forClass(c);
        for (Object obj : c.getEnumConstants()) {
          Enum<?> e = (Enum<?>) obj;
          include.add(createEnumOperation(e));
        }
        for (Method m : c.getDeclaredMethods()) {
          if (!m.getName().equals("$jacocoInit")) {
            if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
              include.add(createMethodCall(m, enumType));
            } else {
              exclude.add(createMethodCall(m, enumType));
            }
          }
        }
      }
    }

    // TODO test that declaring class of operations for inner enum is enum

    Set<TypedOperation> actual = getConcreteOperations(cwim);
    assertEquals("number of statements", 13, actual.size());

    for (TypedOperation op : include) {
      assertTrue("operation " + op + " should occur", actual.contains(op));
    }
    for (TypedOperation op : exclude) {
      assertFalse("operation " + op + " should not occur", actual.contains(op));
    }
  }

  @Test
  public void enumAsPredicateTest() {
    Class<?> c = EnumAsPredicate.class;
    assert c.isEnum() : "something wrong -- should be an enum";

    ClassOrInterfaceType enumType = ClassOrInterfaceType.forClass(c);
    List<ClassOrInterfaceType> interfaces = enumType.getInterfaces();
    assert interfaces.size() == 1 : "should only be one interface";
    InstantiatedType interfaceType = (InstantiatedType) interfaces.get(0);

    List<TypedOperation> include = new ArrayList<>();
    List<TypedOperation> exclude = new ArrayList<>();
    Map<String, Set<TypedClassOperation>> overrideMap = new LinkedHashMap<>();
    for (Object obj : c.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      include.add(createEnumOperation(e));
      for (Method m : e.getClass().getDeclaredMethods()) {
        Set<TypedClassOperation> opSet = overrideMap.get(m.getName());
        if (opSet == null) {
          opSet = new HashSet<>();
        }
        opSet.add(createMethodCall(m, enumType));
        overrideMap.put(m.getName(), opSet);
      }
    }
    for (Method m : c.getDeclaredMethods()) {
      if (!m.getName().equals("$jacocoInit")) {
        if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
          include.add(createMethodCall(m, enumType));
        } else {
          exclude.add(createMethodCall(m, enumType));
        }
      }
    }

    for (Method m : c.getMethods()) {
      Set<TypedClassOperation> opSet = overrideMap.get(m.getName());
      if (opSet != null) {
        TypedClassOperation actualEnumOp =
            createMethodCall(m, enumType).apply(interfaceType.getTypeSubstitution());
        include.add(actualEnumOp);
      }
    }

    Set<TypedOperation> actual = getConcreteOperations(c);
    // TODO this should be 5, except for odd business of getting test(Object) when getting declared methods of constant class
    assertEquals("number of operations", 5, actual.size());

    for (TypedOperation op : actual) {
      if (op.getName().equals("test")) {
        checkOutcome(op, new Object[] {EnumAsPredicate.ONE, new Integer(0)}, false);
        checkOutcome(op, new Object[] {EnumAsPredicate.TWO, new Integer(0)}, true);
      }
    }

    for (TypedOperation op : include) {
      assertTrue(String.format("operation %n%s%nshould occur", op), actual.contains(op));
    }
    for (TypedOperation op : exclude) {
      assertFalse(String.format("operation %n%s%n should not occur", op), actual.contains(op));
    }
  }

  private void checkOutcome(TypedOperation op, Object[] input, Object expected) {
    ExecutionOutcome outcome = op.execute(input, System.out);
    assertTrue(
        "should have normal execution, outcome: " + outcome, outcome instanceof NormalExecution);
    NormalExecution exec = (NormalExecution) outcome;
    assertEquals("should have return value for input", expected, exec.getRuntimeValue());
  }

  /**
   * valueEnum tests Reflection.getStatements for an enum with a field.
   * Uses randoop.test.Coin, which has a private int field, a private constructor,
   * and one public accessor method. Expect that should return the constants and accessor.
   * Note that compiler ensures constructor access is at least package level.
   */
  @Test
  public void valueEnum() {
    Class<?> coin = Coin.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(coin);

    Set<TypedOperation> actual = getConcreteOperations(coin);

    int count = 0;
    for (Object obj : coin.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      assertTrue(
          "enum constant " + e.name() + " should occur", actual.contains(createEnumOperation(e)));
      count++;
    }

    for (Constructor<?> con : coin.getDeclaredConstructors()) {
      try {
        assertFalse(
            "enum constructor " + con.getName() + "should not occur",
            actual.contains(createConstructorCall(con)));
      } catch (RandoopTypeException e) {
        fail("type error: " + e);
      }
    }

    for (Method m : coin.getMethods()) {
      TypedOperation mc = createMethodCall(m, declaringType);
      if (m.getName().equals("value")) {
        assertTrue("enum method " + m.toGenericString() + " should occur", actual.contains(mc));
        count++;
      } else {
        assertFalse(
            "enum method " + m.toGenericString() + " should not occur", actual.contains(mc));
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
    Class<?> op = OperatorEnum.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(op);

    Set<TypedOperation> actual = getConcreteOperations(op);
    Set<String> overrides = new TreeSet<>();
    int count = 0;
    for (Object obj : op.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      assertTrue(
          "enum constant " + e.name() + " should occur", actual.contains(createEnumOperation(e)));
      count++;
      for (Method m : e.getClass().getDeclaredMethods()) {
        overrides.add(m.getName());
      }
    }

    for (Method m : op.getMethods()) {
      TypedOperation mc = createMethodCall(m, declaringType);
      if (overrides.contains(m.getName())) {
        assertTrue("enum method " + mc + " should occur", actual.contains(mc));
        count++;
      } else {
        assertFalse("enum method " + mc + " should not occur", actual.contains(mc));
      }
    }

    assertEquals("number of operations", count, actual.size());
  }

  private Set<TypedOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(
        c, new DefaultReflectionPredicate(), new PublicVisibilityPredicate());
  }

  private Set<TypedOperation> getConcreteOperations(
      Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    OperationExtractor extractor =
        new OperationExtractor(classType, operations, predicate, visibilityPredicate);
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.apply(extractor, c);
    return operations;
  }

  private TypedClassOperation createEnumOperation(Enum<?> e) {
    CallableOperation eOp = new EnumConstant(e);
    ClassOrInterfaceType enumType = new NonParameterizedType(e.getDeclaringClass());
    return new TypedClassOperation(eOp, enumType, new TypeTuple(), enumType);
  }

  private TypedClassOperation createConstructorCall(Constructor<?> con)
      throws RandoopTypeException {
    ConstructorCall op = new ConstructorCall(con);
    ClassOrInterfaceType declaringType = ClassOrInterfaceType.forClass(con.getDeclaringClass());
    List<Type> paramTypes = new ArrayList<>();
    for (java.lang.reflect.Type pc : con.getGenericParameterTypes()) {
      paramTypes.add(Type.forType(declaringType.getParameterTable(), pc));
    }
    return new TypedClassOperation(op, declaringType, new TypeTuple(paramTypes), declaringType);
  }

  private TypedClassOperation createMethodCall(Method m, ClassOrInterfaceType declaringType) {
    MethodCall op = new MethodCall(m);
    List<Type> paramTypes = new ArrayList<>();
    paramTypes.add(declaringType);
    for (java.lang.reflect.Type t : m.getGenericParameterTypes()) {
      paramTypes.add(Type.forType(declaringType.getParameterTable(), t));
    }
    Type outputType = Type.forType(declaringType.getParameterTable(), m.getGenericReturnType());
    return new TypedClassOperation(op, declaringType, new TypeTuple(paramTypes), outputType);
  }
}
