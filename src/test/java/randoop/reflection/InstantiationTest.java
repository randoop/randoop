package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.main.ClassNameErrorHandler;
import randoop.main.RandoopClassNameError;
import randoop.main.ThrowClassNameError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.intersectiontypes.AccessibleInterval;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.JavaTypes;
import randoop.types.ParameterBound;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.types.TypeVariable;
import randoop.util.Randomness;

/** Tests instantiation of type parameters by OperationModel. */
public class InstantiationTest {

  @SuppressWarnings("signature:argument") // string concatenation
  @Test
  public void testGenericBounds() {
    Set<@ClassGetName String> classnames = new LinkedHashSet<>();
    String packageName = "randoop.reflection";
    classnames.add(packageName + "." + "GenericBounds");
    classnames.add(packageName + "." + "TW");
    classnames.add(packageName + "." + "SW");
    classnames.add(packageName + "." + "UW");
    classnames.add(packageName + "." + "VW");
    classnames.add(packageName + "." + "WW");
    classnames.add(packageName + "." + "XW");
    classnames.add(packageName + "." + "YW");
    classnames.add(packageName + "." + "RML");

    String classname = packageName + "." + "GenericBounds";
    Set<String> methodNames = new LinkedHashSet<>();
    methodNames.add(classname + "." + "m00");
    methodNames.add(classname + "." + "m01");
    methodNames.add(classname + "." + "m02");
    methodNames.add(classname + "." + "m03");
    methodNames.add(classname + "." + "m04");
    methodNames.add(classname + "." + "m05");
    methodNames.add(classname + "." + "m06");
    methodNames.add(classname + "." + "m07");
    methodNames.add(classname + "." + "m08");
    //    methodNames.add("m09");
    System.out.println("Note: test for m09 is disabled until have constraint propagation working");
    methodNames.add(classname + "." + "m10");
    methodNames.add(classname + "." + "m11");
    methodNames.add(classname + "." + "m12");
    methodNames.add("randoop.reflection.RML.getZ");
    methodNames.add("randoop.reflection.RML.setZ");
    methodNames.add("java.lang.Object.getClass");

    OperationModel model = createModel(classnames, packageName);

    Set<TypedOperation> classOperations = new LinkedHashSet<>();
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    getOperations(model, classOperations, inputTypes);

    int expectedClassCount = classnames.size() + 1;
    assertEquals(expectedClassCount, model.getClassTypes().size());

    int methodCount = 0;
    for (TypedOperation operation : classOperations) {
      if (operation.isMethodCall()) {
        assertTrue(
            "method name " + operation.getName() + " should be in expected list",
            methodNames.contains(operation.getName()));
        methodCount++;
      }
    }
    assertEquals(methodNames.size(), methodCount);
  }

  /*
   * This test fails if {@code D_BST} is removed since model always chooses {@code String} for
   * parameter to {@code BST} and without {@code D_BST} there is no class that implements {@code
   * C_BST<String>}.
   *
   * <p>It should be possible for it to pass with {@code B_BST}.
   */
  /*
  @Test
  public void testOperationInstantiation() {
    Set<String> classnames = new LinkedHashSet<>();
    String packageName = "randoop.reflection";
    classnames.add(packageName + "." + "BST");
    classnames.add(packageName + "." + "B_BST");
    classnames.add(packageName + "." + "D_BST");

    OperationModel model = createModel(classnames, packageName);

    int expectedClassCount = classnames.size() + 1;
    assertEquals(expectedClassCount, model.getClassTypes().size());

    Set<TypedOperation> classOperations = new LinkedHashSet<>();
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    getOperations(model, classOperations, inputTypes);

    int methodCount = 0;
    for (TypedOperation operation : classOperations) {
      if (operation.isMethodCall()) {
        methodCount++;
        if (!operation.getName().equals("m")) {
          fail("should only have method m, got " + operation.getName());
        }
      }
    }
    // XXX this should be 1, but running on Travis-CI using 1.8.0_101 misses the method,
    //    while running on my machine with 1.8.0_102 finds it.
    assertThat("expect one method", methodCount, isOneOf(0, 1));
  }
  */
  /*
  @Test
  public void testRecursiveInstantiation() {
    Set<String> classnames = new LinkedHashSet<>();
    String packageName = "randoop.reflection";
    classnames.add(packageName + "." + "BMB");
    classnames.add(packageName + "." + "AI");
    classnames.add(packageName + "." + "AT");

    OperationModel model = createModel(classnames, packageName);

    int expectedClassCount = classnames.size() + 1;
    assertEquals(expectedClassCount, model.getClassTypes().size());

    for (ClassOrInterfaceType type : model.getClassTypes()) {
      assertThat(
          "class name one of BMB, AI, AT, or Object",
          type.getSimpleName(),
          isOneOf("BMB", "AI", "AT", "Object"));
    }
  }
  */
  /*
  @Test
  public void testSortedSet() {
    Set<String> classnames = new LinkedHashSet<>();
    String packageName = "randoop.reflection";
    classnames.add(packageName + "." + "OrderedSet");
    classnames.add(packageName + "." + "SetUtility");
    OperationModel model = createModel(classnames, packageName);
    int expectedClassCount = classnames.size() + 1;
    assertEquals(expectedClassCount, model.getClassTypes().size());

    Set<TypedOperation> classOperations = new LinkedHashSet<>();
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    try {
      addTypes(Type.forName("randoop.reflection.StringComparator"), inputTypes);
    } catch (ClassNotFoundException  | NoClassDefFoundError e) {
      fail("cannot build type for comparator");
    }
    Set<String> nullOKNames = new HashSet<>();
    nullOKNames.add("forEach");
    nullOKNames.add("removeIf");
    getOperations(model, classOperations, inputTypes, nullOKNames);

    int count = 0;
    for (TypedOperation operation : classOperations) {
      assertFalse("should not be generic " + operation, operation.getInputTypes().isGeneric());
      assertFalse("should not have wildcards" + operation, operation.getInputTypes().hasWildcardArgument());
      count++;
    }
    assertEquals(24, count);

  }
  */

  /** Based on a case from imglib2. */
  @Test
  public void testIntersectionType() {
    Set<@ClassGetName String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.reflection.intersectiontypes.ExtendedBase");
    Package pkg = randoop.reflection.intersectiontypes.ExtendedBase.class.getPackage();
    assertNotNull(pkg);
    OperationModel model = createModel(classnames, pkg.getName());

    Set<TypedOperation> classOperations = new LinkedHashSet<>();
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    addTypes(ClassOrInterfaceType.forClass(AccessibleInterval.class), inputTypes);
    Set<String> nullOKNames = new HashSet<>();
    getOperations(model, classOperations, inputTypes, nullOKNames);

    assertFalse(classOperations.isEmpty());
    for (TypedOperation operation : classOperations) {
      assertFalse("should not be generic " + operation, operation.getInputTypes().isGeneric(false));
      assertFalse(
          "should not have wildcards" + operation, operation.getInputTypes().hasWildcardArgument());
    }
  }

  /** Based on a case from Apache Commons Collections. */
  @Test
  public void testCaptureConvInstantiation() {
    Set<@ClassGetName String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.reflection.CaptureInstantiationCase");
    OperationModel model = createModel(classnames, "randoop.reflection");
    Set<TypedOperation> classOperations = new LinkedHashSet<>();
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    addTypes(ClassOrInterfaceType.forClass(AnIterable.class), inputTypes);
    Substitution subst;
    GenericClassType predicateType =
        GenericClassType.forClass(CaptureInstantiationCase.LocalPredicate.class);
    subst = new Substitution(predicateType.getTypeParameters(), JavaTypes.SERIALIZABLE_TYPE);
    addTypes(predicateType.substitute(subst), inputTypes);
    GenericClassType onePredicateType =
        GenericClassType.forClass(CaptureInstantiationCase.OnePredicate.class);
    subst = new Substitution(onePredicateType.getTypeParameters(), JavaTypes.SERIALIZABLE_TYPE);
    InstantiatedType oneSerializablePredicateType = onePredicateType.substitute(subst);
    addTypes(oneSerializablePredicateType, inputTypes);
    subst =
        new Substitution(
            JDKTypes.TREE_SET_TYPE.getTypeParameters(),
            (ReferenceType) oneSerializablePredicateType);
    addTypes(JDKTypes.TREE_SET_TYPE.substitute(subst), inputTypes);
    subst =
        new Substitution(
            predicateType.getTypeParameters(), (ReferenceType) oneSerializablePredicateType);
    addTypes(predicateType.substitute(subst), inputTypes);

    Set<String> nullOKNames = new HashSet<>();
    getOperations(model, classOperations, inputTypes, nullOKNames);
    assertFalse(classOperations.isEmpty());
    for (TypedOperation operation : classOperations) {
      if (operation.getName().equals("filter")) {
        assertFalse(operation.isGeneric());
      }
      if (operation.getName().equals("oneOf")) {
        assertFalse(operation.isGeneric());
      }
    }
  }

  /**
   * Tests the instantiation of type variables whose bounds mention other type variables of the same
   * method: each instantiation succeeds and satisfies the bounds, whatever the random choices.
   */
  @Test
  public void testRelatedBoundsInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation widen =
        TypedOperation.forMethod(RelatedBoundsCase.class.getMethod("widen", Object.class));
    TypedClassOperation chain =
        TypedOperation.forMethod(
            RelatedBoundsCase.class.getMethod("chain", Object.class, Object.class));

    for (int seed = 0; seed < 20; seed++) {
      Randomness.setSeed(seed);

      // <K, V extends K> K widen(V)
      TypedClassOperation widenInstance = instantiator.instantiate(widen);
      assertNotNull("instantiation failed for " + widen, widenInstance);
      assertFalse(widenInstance + " should not be generic", widenInstance.isGeneric());
      Type k = widenInstance.getOutputType();
      Type v = widenInstance.getInputTypes().get(0);
      assertTrue(v + " should be a subtype of " + k, v.isSubtypeOfOrEqualTo(k));

      // <A, B extends A, C extends B> A chain(B, C)
      TypedClassOperation chainInstance = instantiator.instantiate(chain);
      assertNotNull("instantiation failed for " + chain, chainInstance);
      assertFalse(chainInstance + " should not be generic", chainInstance.isGeneric());
      Type a = chainInstance.getOutputType();
      Type b = chainInstance.getInputTypes().get(0);
      Type c = chainInstance.getInputTypes().get(1);
      assertTrue(b + " should be a subtype of " + a, b.isSubtypeOfOrEqualTo(a));
      assertTrue(c + " should be a subtype of " + b, c.isSubtypeOfOrEqualTo(b));
    }
    Randomness.setSeed(0);
  }

  /**
   * Tests that the instantiation of {@code <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ?
   * extends V>)} respects the bounds of the capture variables that it instantiates from an input
   * type: the key type of the argument is a subtype of the key type of the result, even though many
   * candidate types for {@code K} are incompatible with it.
   */
  @Test
  public void testCaptureBoundsInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    addTypes(
        JDKTypes.HASH_MAP_TYPE.instantiate(
            JavaTypes.INT_TYPE.toBoxedPrimitive(), JavaTypes.STRING_TYPE),
        inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation unmodifiableMap =
        TypedOperation.forMethod(Collections.class.getMethod("unmodifiableMap", Map.class));

    for (int seed = 0; seed < 20; seed++) {
      Randomness.setSeed(seed);
      TypedClassOperation instance = instantiator.instantiate(unmodifiableMap);
      assertNotNull("instantiation failed for " + unmodifiableMap, instance);
      InstantiatedType argType = (InstantiatedType) instance.getInputTypes().get(0);
      InstantiatedType resultType = (InstantiatedType) instance.getOutputType();
      for (int i = 0; i < 2; i++) {
        Type argTypeArgument =
            ((ReferenceArgument) argType.getTypeArguments().get(i)).getReferenceType();
        Type resultTypeArgument =
            ((ReferenceArgument) resultType.getTypeArguments().get(i)).getReferenceType();
        assertTrue(
            argTypeArgument + " should be a subtype of " + resultTypeArgument,
            argTypeArgument.isSubtypeOfOrEqualTo(resultTypeArgument));
      }
    }
    Randomness.setSeed(0);
  }

  /**
   * Tests the instantiation of {@code <T extends Object & Comparable<? super T>> T max(Collection<?
   * extends T>)}, in which the upper bound of the capture variable of the wildcard is a type
   * variable whose bound mentions that type variable.
   */
  @Test
  public void testRecursivelyBoundedCaptureInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JDKTypes.ARRAY_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE), inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation max =
        TypedOperation.forMethod(Collections.class.getMethod("max", Collection.class));

    TypedClassOperation instance = instantiator.instantiate(max);
    assertNotNull("instantiation failed for " + max, instance);
    assertEquals(JavaTypes.STRING_TYPE, instance.getOutputType());
  }

  /**
   * Tests that a parameter type that would map a type variable to two different types does not
   * cause an exception.
   */
  @Test
  public void testConflictingInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(
        GenericClassType.forClass(java.util.Map.class)
            .instantiate(JavaTypes.STRING_TYPE, JavaTypes.INT_TYPE.toBoxedPrimitive()),
        inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    TypedClassOperation sameKeyAndValue =
        TypedOperation.forMethod(
            RelatedBoundsCase.class.getMethod("sameKeyAndValue", java.util.Map.class));
    for (int seed = 0; seed < 10; seed++) {
      Randomness.setSeed(seed);
      // Map<String, Integer> is the only Map in the input types, and it is not a Map<X, X>.
      assertNull(instantiator.instantiate(sameKeyAndValue));
    }
    Randomness.setSeed(0);
  }

  /* disabled until fix type parameter management
  @Test
  public void testLazyConversionInstantiation() {
    Set<String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.reflection.LazyConversionInstantiationCase");
    OperationModel model = createModel(classnames, "randoop.reflection");
    Set<TypedOperation> classOperations = new LinkedHashSet<>();
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    Substitution substitution = new Substitution(JDKTypes.TREE_SET_TYPE.getTypeParameters(), (ReferenceType)JavaTypes.STRING_TYPE);
    addTypes(JDKTypes.TREE_SET_TYPE.substitute(substitution), inputTypes);

    Set<String> nullOKNames = new HashSet<>();
    getOperations(model, classOperations, inputTypes, nullOKNames);
    assertFalse(classOperations.isEmpty());

  }
  */

  private OperationModel createModel(Set<@ClassGetName String> classnames, String packageName) {
    AccessibilityPredicate accessibility =
        new AccessibilityPredicate.PackageAccessibilityPredicate(packageName);
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<@ClassGetName String> coveredClassnames = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new ThrowClassNameError();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model =
          OperationModel.createModel(
              accessibility,
              reflectionPredicate,
              classnames,
              coveredClassnames,
              errorHandler,
              literalsFileList);
    } catch (SignatureParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
    } catch (RandoopClassNameError e) {
      fail("bad class name: " + e.getMessage());
    } catch (RandoopSpecificationError e) {
      fail("bad specification: " + e.getMessage());
    }
    assertNotNull(model);
    return model;
  }

  private void getOperations(
      OperationModel model, Set<TypedOperation> classOperations, Set<Type> inputTypes) {
    getOperations(model, classOperations, inputTypes, new HashSet<String>());
  }

  private void getOperations(
      OperationModel model,
      Set<TypedOperation> classOperations,
      Set<Type> inputTypes,
      Set<String> nullOKNames) {
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);

    Set<TypedClassOperation> genericConstructors = new LinkedHashSet<>();
    // prime the type set by including types from non-generic constructors
    for (TypedOperation operation : model.getOperations()) {
      if (operation.isConstructorCall()) {
        if (operation.isGeneric()) { //
          genericConstructors.add((TypedClassOperation) operation);
        } else {
          addTypes(operation, inputTypes);
        }
      }
    }

    // instantiate generic constructors
    for (TypedClassOperation operation : genericConstructors) {
      TypedClassOperation classOperation = instantiator.instantiate(operation);
      assertNotNull(
          "instantiation of constructor " + operation + " should not be null", classOperation);
      addTypes(classOperation, inputTypes);
    }

    for (TypedOperation operation : model.getOperations()) {
      if (operation.isGeneric()) {
        if (!nullOKNames.contains(operation.getName())) {
          TypedClassOperation classOperation =
              instantiator.instantiate((TypedClassOperation) operation);
          if (!operation.getName().equals("randoop.reflection.GenericBounds.m09")) {
            assertNotNull("instantiation failed for method " + operation, classOperation);
            addTypes(classOperation, inputTypes);
            if (classOperation.isMethodCall()) {
              classOperations.add(classOperation);
            }
          }
        }
      } else {
        if (operation.isMethodCall()) {
          classOperations.add(operation);
        }
      }
    }
  }

  private void addTypes(TypedOperation operation, Set<Type> typeSet) {
    Type outputType = operation.getOutputType();
    if (outputType.isClassOrInterfaceType()) {
      addTypes(outputType, typeSet);
    }
  }

  private void addTypes(Type type, Set<Type> typeSet) {
    if (type.isClassOrInterfaceType()) {
      ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
      if (!(classType.isGeneric() || classType.hasWildcard())) {
        typeSet.addAll(classType.getSupertypesInclusive());
      }
    }
  }

  /**
   * A generic class with a member class.
   *
   * @param <E> the type parameter
   */
  private static class Outer<E> {
    /** A member class with no type parameters of its own. */
    class Inner {}

    /** Another member class with no type parameters of its own, unrelated to {@link Inner}. */
    class Other {}

    /** A member class with no type parameters of its own, which has a member class. */
    class Mid {
      /** A member class two levels in, with no type parameters of its own. */
      class Deep {}
    }

    /**
     * A member class with a type parameter of its own.
     *
     * @param <U> a type parameter
     */
    class Holder<U> {}

    /**
     * A member class with a type parameter of its own, whose superclass is a member class.
     *
     * @param <U> a type parameter
     */
    class Child<U> extends Inner {}

    /**
     * A method whose parameter type is the generic form of a member class.
     *
     * @param inner an inner object
     */
    @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
    void take(Inner inner) {}
  }

  /**
   * A method whose parameter type is generic only through its enclosing type.
   *
   * @param <E> the type parameter of the enclosing type
   * @param inner an inner object
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <E> void innerParameter(Outer<E>.Inner inner) {}

  /**
   * In {@code <E> void innerParameter(Outer<E>.Inner inner)}, the parameter type is a member class
   * with no type parameters of its own, which is generic only through its enclosing type.
   */
  @Test
  public void testNonParameterizedMemberClassParameter() throws Exception {
    Method m = InstantiationTest.class.getDeclaredMethod("innerParameter", Outer.Inner.class);
    TypedClassOperation operation = TypedOperation.forMethod(m);
    ClassOrInterfaceType genericInner = ClassOrInterfaceType.forClass(Outer.Inner.class);
    assertTrue(genericInner.isGeneric());
    assertFalse(genericInner instanceof ParameterizedType);
    // Outer<String>.Inner.
    TypeVariable outerParameter = GenericClassType.forClass(Outer.class).getTypeParameters().get(0);
    ClassOrInterfaceType stringInner =
        genericInner.substitute(new Substitution(outerParameter, JavaTypes.STRING_TYPE));
    Set<Type> inputTypes = new LinkedHashSet<>();
    inputTypes.add(JavaTypes.OBJECT_TYPE);
    inputTypes.add(stringInner);
    TypedClassOperation instantiated = new TypeInstantiator(inputTypes).instantiate(operation);
    assertNotNull("no instantiation found", instantiated);
    assertEquals(stringInner, instantiated.getInputTypes().get(0));

    // The parameter types of `<E> void pair(Outer<E>.Inner inner, List<E> values)` share the
    // method's type parameter `E`, so `Outer<String>.Inner` is paired with `List<String>`, never
    // with `List<Integer>`.
    Method pairMethod =
        InstantiationTest.class.getDeclaredMethod("pair", Outer.Inner.class, List.class);
    TypedClassOperation pairOperation = TypedOperation.forMethod(pairMethod);
    Type innerParameterType = pairOperation.getInputTypes().get(0);
    Type listParameterType = pairOperation.getInputTypes().get(1);
    assertEquals(
        ((ClassOrInterfaceType) innerParameterType).getTypeParameters(),
        ((ClassOrInterfaceType) listParameterType).getTypeParameters());
    InstantiatedType integerList =
        JDKTypes.LIST_TYPE.instantiate(ReferenceType.forClass(Integer.class));
    InstantiatedType stringList = JDKTypes.LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);

    // The input type `Outer<String>.Inner` of the first parameter determines `E`.  TypeInstantiator
    // does not require an input type for the resulting non-generic parameter type `List<String>`.
    Set<Type> integerListInputTypes = new LinkedHashSet<>();
    integerListInputTypes.add(JavaTypes.OBJECT_TYPE);
    integerListInputTypes.add(stringInner);
    integerListInputTypes.add(integerList);
    TypedClassOperation integerListInstantiated =
        new TypeInstantiator(integerListInputTypes).instantiate(pairOperation);
    assertNotNull("no instantiation found", integerListInstantiated);
    assertEquals(stringInner, integerListInstantiated.getInputTypes().get(0));
    assertEquals(stringList, integerListInstantiated.getInputTypes().get(1));

    Set<Type> matchingInputTypes = new LinkedHashSet<>();
    matchingInputTypes.add(JavaTypes.OBJECT_TYPE);
    matchingInputTypes.add(stringInner);
    matchingInputTypes.add(stringList);
    TypedClassOperation pairInstantiated =
        new TypeInstantiator(matchingInputTypes).instantiate(pairOperation);
    assertNotNull("no instantiation found", pairInstantiated);
    assertEquals(stringInner, pairInstantiated.getInputTypes().get(0));
    assertEquals(stringList, pairInstantiated.getInputTypes().get(1));
  }

  /**
   * A method whose parameter types share a type parameter, one of them only through an enclosing
   * type.
   *
   * @param <E> the type parameter
   * @param inner an inner object
   * @param values a list
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <E> void pair(Outer<E>.Inner inner, List<E> values) {}

  /**
   * The parameter type of {@code void take(Inner inner)} in {@code Outer<E>} is {@code
   * Outer<E>.Inner}, where {@code E} is the type parameter of {@code Outer}, which is the generic
   * form of {@code Outer.Inner}.
   */
  @Test
  public void testGenericFormMemberClassParameter() throws Exception {
    Method take = Outer.class.getDeclaredMethod("take", Outer.Inner.class);
    java.lang.reflect.Type parameterType = take.getGenericParameterTypes()[0];
    assertTrue(parameterType instanceof java.lang.reflect.ParameterizedType);
    ClassOrInterfaceType takeParameterType = ClassOrInterfaceType.forType(parameterType);
    assertEquals(ClassOrInterfaceType.forClass(Outer.Inner.class), takeParameterType);
    assertTrue(takeParameterType.isGenericForm());
  }

  /**
   * Matching enclosing types does not make {@code Outer<String>.Inner} an instantiation of the
   * unrelated member class {@code Outer<E>.Other}.
   */
  @Test
  public void testUnrelatedMemberClassSubstitution() {
    ClassOrInterfaceType genericInner = ClassOrInterfaceType.forClass(Outer.Inner.class);
    TypeVariable outerParameter = GenericClassType.forClass(Outer.class).getTypeParameters().get(0);
    ClassOrInterfaceType stringInner =
        genericInner.substitute(new Substitution(outerParameter, JavaTypes.STRING_TYPE));
    ClassOrInterfaceType genericOther = ClassOrInterfaceType.forClass(Outer.Other.class);
    assertNotNull(stringInner.getInstantiatingSubstitution(genericInner));
    assertNull(stringInner.getInstantiatingSubstitution(genericOther));
  }

  /**
   * A method whose parameter type has type arguments only on an owner two levels out.
   *
   * @param deep a deep object
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static void deepParameter(Outer<String>.Mid.Deep deep) {}

  /**
   * The conversion of {@code Outer<String>.Mid.Deep}, where neither {@code Mid} nor {@code Deep}
   * declares type parameters, retains the type argument {@code String}.
   */
  @Test
  public void testTwoLevelMemberClassParameter() throws Exception {
    Method m = InstantiationTest.class.getDeclaredMethod("deepParameter", Outer.Mid.Deep.class);
    ClassOrInterfaceType deepType = ClassOrInterfaceType.forType(m.getGenericParameterTypes()[0]);
    TypeVariable outerParameter = GenericClassType.forClass(Outer.class).getTypeParameters().get(0);
    ClassOrInterfaceType expected =
        ClassOrInterfaceType.forClass(Outer.Mid.Deep.class)
            .substitute(new Substitution(outerParameter, JavaTypes.STRING_TYPE));
    assertEquals(expected, deepType);
    assertFalse(deepType.isGeneric());
    assertFalse(deepType.isGenericForm());
  }

  /**
   * {@code Outer<String>.Holder<Integer>} does not instantiate {@code Outer<Long>.Holder<V>},
   * because the enclosing types differ.
   */
  @Test
  public void testMemberClassWithFixedEnclosingTypeSubstitution() {
    TypeVariable outerParameter = GenericClassType.forClass(Outer.class).getTypeParameters().get(0);
    ClassOrInterfaceType genericHolder = ClassOrInterfaceType.forClass(Outer.Holder.class);
    List<TypeVariable> holderParameters = new ArrayList<>(genericHolder.getTypeParameters());
    holderParameters.remove(outerParameter);
    assertEquals(1, holderParameters.size());
    TypeVariable holderParameter = holderParameters.get(0);
    ClassOrInterfaceType stringIntegerHolder =
        genericHolder.substitute(
            new Substitution(outerParameter, JavaTypes.STRING_TYPE)
                .extend(
                    new Substitution(
                        holderParameter, ClassOrInterfaceType.forClass(Integer.class))));
    ClassOrInterfaceType longHolder =
        genericHolder.substitute(
            new Substitution(outerParameter, ClassOrInterfaceType.forClass(Long.class)));
    assertTrue(longHolder.isGeneric());
    assertNull(stringIntegerHolder.getInstantiatingSubstitution(longHolder));
  }

  /**
   * A method whose type parameter is bounded by an instantiation of a generic class.
   *
   * @param <T> the type parameter
   * @param outer an outer object
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Outer<String>> void boundedByOuter(T outer) {}

  /**
   * {@code Outer<String>.Inner} does not instantiate {@code T extends Outer<String>}, even though
   * its enclosing type does.
   */
  @Test
  public void testMemberClassDoesNotInstantiateTypeVariableOfEnclosingType() throws Exception {
    Method m = InstantiationTest.class.getDeclaredMethod("boundedByOuter", Outer.class);
    TypeVariable t = (TypeVariable) TypedOperation.forMethod(m).getInputTypes().get(0);
    TypeVariable outerParameter = GenericClassType.forClass(Outer.class).getTypeParameters().get(0);
    ClassOrInterfaceType stringInner =
        ClassOrInterfaceType.forClass(Outer.Inner.class)
            .substitute(new Substitution(outerParameter, JavaTypes.STRING_TYPE));
    Substitution substitution = stringInner.getInstantiatingSubstitution(t);
    assertTrue(
        "unexpected substitution " + substitution,
        substitution == null || substitution.get(t) == null);
  }

  /**
   * Returns {@code MemberTypeOuter<typeArgument>.Inner}.
   *
   * @param typeArgument the type argument of the enclosing type
   * @return the member type {@code MemberTypeOuter<typeArgument>.Inner}
   */
  private static ClassOrInterfaceType memberInnerType(ReferenceType typeArgument) {
    return ClassOrInterfaceType.forClass(MemberTypeOuter.Inner.class)
        .substitute(
            new Substitution(
                GenericClassType.forClass(MemberTypeOuter.class).getTypeParameters(),
                typeArgument));
  }

  /**
   * Tests that the instantiation of {@code <U extends Number> void
   * useBounded(MemberTypeOuter<U>.Inner)} respects the bound of {@code U}: it rejects {@code
   * MemberTypeOuter<String>.Inner} and accepts {@code MemberTypeOuter<Integer>.Inner}.
   */
  @Test
  public void testBoundedMemberTypeInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    TypedClassOperation useBounded =
        TypedOperation.forMethod(
            MemberTypeCase.class.getMethod("useBounded", MemberTypeOuter.Inner.class));

    ClassOrInterfaceType stringInner = memberInnerType(JavaTypes.STRING_TYPE);
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    inputTypes.addAll(stringInner.getSupertypesInclusive());
    TypedClassOperation instance = new TypeInstantiator(inputTypes).instantiate(useBounded);
    assertNull("instantiation should fail for " + useBounded, instance);

    ClassOrInterfaceType integerInner =
        memberInnerType((ReferenceType) JavaTypes.INT_TYPE.toBoxedPrimitive());
    inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    inputTypes.addAll(integerInner.getSupertypesInclusive());
    instance = new TypeInstantiator(inputTypes).instantiate(useBounded);
    assertNotNull("instantiation failed for " + useBounded, instance);
    assertEquals(integerInner, instance.getInputTypes().get(0));
  }

  /**
   * Tests that the instantiation of {@code <U> U get(MemberTypeOuter<U>.Inner)} instantiates the
   * return type consistently with the parameter type.
   */
  @Test
  public void testMemberTypeReturnInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    ClassOrInterfaceType stringInner = memberInnerType(JavaTypes.STRING_TYPE);
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    inputTypes.addAll(stringInner.getSupertypesInclusive());
    TypedClassOperation get =
        TypedOperation.forMethod(
            MemberTypeCase.class.getMethod("get", MemberTypeOuter.Inner.class));

    TypedClassOperation instance = new TypeInstantiator(inputTypes).instantiate(get);
    assertNotNull("instantiation failed for " + get, instance);
    assertEquals(stringInner, instance.getInputTypes().get(0));
    assertEquals(JavaTypes.STRING_TYPE, instance.getOutputType());
  }

  /**
   * Tests that the instantiation of {@code <U> U getLeaf(MemberTypeOuter<U>.Middle.Leaf)}, whose
   * parameter type has an owner type {@code MemberTypeOuter<U>.Middle} with no type arguments of
   * its own, instantiates the return type consistently with the parameter type.
   */
  @Test
  public void testTwoLevelMemberTypeReturnInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    TypedClassOperation getLeaf =
        TypedOperation.forMethod(
            MemberTypeCase.class.getMethod("getLeaf", MemberTypeOuter.Middle.Leaf.class));
    // The parameter type mentions the method's type parameter U, not the declaration's T.
    ClassOrInterfaceType parameterType = (ClassOrInterfaceType) getLeaf.getInputTypes().get(0);
    assertFalse(parameterType.isGenericForm());
    assertEquals(getLeaf.getOutputType(), parameterType.getTypeParameters().get(0));

    ClassOrInterfaceType stringLeaf =
        ClassOrInterfaceType.forClass(MemberTypeOuter.Middle.Leaf.class)
            .substitute(
                new Substitution(
                    GenericClassType.forClass(MemberTypeOuter.class).getTypeParameters(),
                    JavaTypes.STRING_TYPE));
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    inputTypes.addAll(stringLeaf.getSupertypesInclusive());
    for (int seed = 0; seed < 10; seed++) {
      Randomness.setSeed(seed);
      TypedClassOperation instance = new TypeInstantiator(inputTypes).instantiate(getLeaf);
      assertNotNull("instantiation failed for " + getLeaf, instance);
      assertEquals(stringLeaf, instance.getInputTypes().get(0));
      assertEquals(JavaTypes.STRING_TYPE, instance.getOutputType());
    }
    Randomness.setSeed(0);
  }

  /**
   * Tests that a member type instantiates a goal member type whose enclosing type is not generic
   * only if the enclosing types are the same: {@code MemberTypeOuter<String>.Inner2<Integer>}
   * instantiates {@code MemberTypeOuter<String>.Inner2<V>}, but {@code
   * MemberTypeOuter<Integer>.Inner2<Long>} does not.
   */
  @Test
  public void testMemberTypeWithNonGenericEnclosingGoalType() {
    // The type parameters of MemberTypeOuter<T>.Inner2<V> are T and V, in that order.
    ClassOrInterfaceType genericInner2 =
        ClassOrInterfaceType.forClass(MemberTypeOuter.Inner2.class);
    List<TypeVariable> parameters = genericInner2.getTypeParameters();
    TypeVariable t = parameters.get(0);
    TypeVariable v = parameters.get(1);
    ReferenceType integerType = (ReferenceType) JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType longType = (ReferenceType) JavaTypes.LONG_TYPE.toBoxedPrimitive();

    // MemberTypeOuter<String>.Inner2<V>, whose enclosing type is not generic.
    ClassOrInterfaceType goalType =
        genericInner2.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(goalType.isGeneric());

    ClassOrInterfaceType stringInteger =
        genericInner2.substitute(new Substitution(parameters, JavaTypes.STRING_TYPE, integerType));
    Substitution substitution = stringInteger.getInstantiatingSubstitution(goalType);
    assertNotNull(stringInteger + " should instantiate " + goalType, substitution);
    assertEquals(integerType, substitution.get(v));

    ClassOrInterfaceType integerLong =
        genericInner2.substitute(new Substitution(parameters, integerType, longType));
    assertNull(integerLong.getInstantiatingSubstitution(goalType));
  }

  /**
   * Tests that the instantiation of {@code <U> U
   * getInner2Leaf(MemberTypeOuter<U>.Inner2<Integer>.Leaf)}, whose parameter type has an owner type
   * {@code MemberTypeOuter<U>.Inner2<Integer>} with a type argument of its own, instantiates the
   * return type consistently with the parameter type.
   */
  @Test
  public void testParameterizedOwnerReturnInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    TypedClassOperation getInner2Leaf =
        TypedOperation.forMethod(
            MemberTypeCase.class.getMethod("getInner2Leaf", MemberTypeOuter.Inner2.Leaf.class));
    // The conversion of the parameter type retains the owner MemberTypeOuter<U> of Inner2<Integer>.
    ClassOrInterfaceType parameterType =
        (ClassOrInterfaceType) getInner2Leaf.getInputTypes().get(0);
    assertEquals(
        Collections.singletonList(getInner2Leaf.getOutputType()),
        parameterType.getTypeParameters());

    // The type parameters of MemberTypeOuter<T>.Inner2<V>.Leaf are T and V, in that order.
    ClassOrInterfaceType genericLeaf =
        ClassOrInterfaceType.forClass(MemberTypeOuter.Inner2.Leaf.class);
    ClassOrInterfaceType stringIntegerLeaf =
        genericLeaf.substitute(
            new Substitution(
                genericLeaf.getTypeParameters(),
                JavaTypes.STRING_TYPE,
                (ReferenceType) JavaTypes.INT_TYPE.toBoxedPrimitive()));
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    inputTypes.addAll(stringIntegerLeaf.getSupertypesInclusive());
    for (int seed = 0; seed < 10; seed++) {
      Randomness.setSeed(seed);
      TypedClassOperation instance = new TypeInstantiator(inputTypes).instantiate(getInner2Leaf);
      assertNotNull("instantiation failed for " + getInner2Leaf, instance);
      assertEquals(stringIntegerLeaf, instance.getInputTypes().get(0));
      assertEquals(JavaTypes.STRING_TYPE, instance.getOutputType());
    }
    Randomness.setSeed(0);
  }

  /** A class with a generic member class. */
  private static class OwnerA {
    /**
     * A generic member class.
     *
     * @param <T> a type parameter
     */
    class Base<T> {}
  }

  /** A class with a member class that extends a member class of another class. */
  private static class OwnerB {
    /** A member class that extends a member class of another class. */
    class Sub extends OwnerA.Base<String> {
      /**
       * Creates a Sub.
       *
       * @param a the enclosing instance of the superclass
       */
      Sub(OwnerA a) {
        a.super();
      }
    }
  }

  /**
   * {@code OwnerB.Sub}, which extends {@code OwnerA.Base<String>}, instantiates {@code
   * OwnerA.Base<T>}, though the enclosing classes differ.
   */
  @Test
  public void testMemberSubclassOfMemberClassOfAnotherClass() {
    ClassOrInterfaceType sub = ClassOrInterfaceType.forClass(OwnerB.Sub.class);
    ClassOrInterfaceType base = ClassOrInterfaceType.forClass(OwnerA.Base.class);
    Substitution substitution = sub.getInstantiatingSubstitution(base);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(base.getTypeParameters().get(0)));
  }

  /**
   * Returns {@code Outer<outerArgument>.Holder<holderArgument>}.
   *
   * @param outerArgument the type argument of the enclosing type
   * @param holderArgument the type argument of the member class
   * @return the member type {@code Outer<outerArgument>.Holder<holderArgument>}
   */
  private static ClassOrInterfaceType holderType(
      ReferenceType outerArgument, ReferenceType holderArgument) {
    ClassOrInterfaceType genericHolder = ClassOrInterfaceType.forClass(Outer.Holder.class);
    // The type parameters of Outer<E>.Holder<U> are E and U, in that order.
    return genericHolder.substitute(
        new Substitution(genericHolder.getTypeParameters(), outerArgument, holderArgument));
  }

  /**
   * A method whose parameter type mentions its type parameter in the enclosing type and in the type
   * argument.
   *
   * @param <T> the type parameter
   * @param holder a holder
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T> void sameArguments(Outer<T>.Holder<T> holder) {}

  /**
   * {@code Outer<String>.Holder<Integer>} does not instantiate {@code Outer<T>.Holder<T>}, because
   * it would bind {@code T} to two types; and the instantiation of {@code sameArguments} chooses
   * {@code Outer<String>.Holder<String>} instead.
   */
  @Test
  public void testMemberClassWithRepeatedTypeVariable() throws Exception {
    Method m = InstantiationTest.class.getDeclaredMethod("sameArguments", Outer.Holder.class);
    TypedClassOperation operation = TypedOperation.forMethod(m);
    ReferenceType goalType = (ReferenceType) operation.getInputTypes().get(0);
    TypeVariable t = goalType.getTypeParameters().get(0);
    ReferenceType integerType = (ReferenceType) JavaTypes.INT_TYPE.toBoxedPrimitive();
    ClassOrInterfaceType stringInteger = holderType(JavaTypes.STRING_TYPE, integerType);
    ClassOrInterfaceType stringString = holderType(JavaTypes.STRING_TYPE, JavaTypes.STRING_TYPE);

    assertNull(stringInteger.getInstantiatingSubstitution(goalType));
    Substitution substitution = stringString.getInstantiatingSubstitution(goalType);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(t));

    // Positive control:  the only member type input is Outer<String>.Holder<String>.
    Set<Type> onlyMatchingInputTypes = new LinkedHashSet<>();
    onlyMatchingInputTypes.add(JavaTypes.OBJECT_TYPE);
    onlyMatchingInputTypes.add(stringString);
    TypedClassOperation onlyMatchingInstance =
        new TypeInstantiator(onlyMatchingInputTypes).instantiate(operation);
    assertNotNull("instantiation failed for " + operation, onlyMatchingInstance);
    assertEquals(stringString, onlyMatchingInstance.getInputTypes().get(0));

    // The candidate input types exclude Outer<String>.Holder<Integer>, so instantiation never
    // chooses it, and it always succeeds.
    Set<Type> inputTypes = new LinkedHashSet<>();
    inputTypes.add(JavaTypes.OBJECT_TYPE);
    inputTypes.add(stringInteger);
    inputTypes.add(stringString);
    for (int seed = 0; seed < 10; seed++) {
      Randomness.setSeed(seed);
      TypedClassOperation instance = new TypeInstantiator(inputTypes).instantiate(operation);
      assertNotNull("instantiation failed for " + operation, instance);
      assertEquals(stringString, instance.getInputTypes().get(0));
    }
    Randomness.setSeed(0);
  }

  /**
   * A fixed type argument of a goal type must equal the corresponding type argument: {@code
   * Outer<String>.Holder<Integer>} does not instantiate {@code Outer<E>.Holder<Long>}, but {@code
   * Outer<String>.Holder<Long>} does.
   */
  @Test
  public void testMemberClassWithFixedTypeArgument() {
    ClassOrInterfaceType genericHolder = ClassOrInterfaceType.forClass(Outer.Holder.class);
    TypeVariable outerParameter = genericHolder.getTypeParameters().get(0);
    TypeVariable holderParameter = genericHolder.getTypeParameters().get(1);
    ReferenceType integerType = (ReferenceType) JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType longType = (ReferenceType) JavaTypes.LONG_TYPE.toBoxedPrimitive();
    // Outer<E>.Holder<Long>
    ClassOrInterfaceType goalType =
        genericHolder.substitute(new Substitution(holderParameter, longType));
    assertTrue(goalType.isGeneric());

    assertNull(
        holderType(JavaTypes.STRING_TYPE, integerType).getInstantiatingSubstitution(goalType));
    Substitution substitution =
        holderType(JavaTypes.STRING_TYPE, longType).getInstantiatingSubstitution(goalType);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(outerParameter));
  }

  /**
   * {@code Outer<String>.Holder<Integer>} is not a subtype of {@code Outer<Long>.Holder<Integer>},
   * because the enclosing types differ.
   */
  @Test
  public void testMemberTypeSubtypeRequiresSameEnclosingType() {
    ReferenceType integerType = (ReferenceType) JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType longType = (ReferenceType) JavaTypes.LONG_TYPE.toBoxedPrimitive();
    ClassOrInterfaceType stringInteger = holderType(JavaTypes.STRING_TYPE, integerType);
    assertTrue(stringInteger.isSubtypeOfOrEqualTo(holderType(JavaTypes.STRING_TYPE, integerType)));
    assertFalse(stringInteger.isSubtypeOfOrEqualTo(holderType(longType, integerType)));
  }

  /**
   * Returns {@code Outer<typeArgument>.Inner}.
   *
   * @param typeArgument the type argument of the enclosing type
   * @return the member type {@code Outer<typeArgument>.Inner}
   */
  private static ClassOrInterfaceType outerInnerType(ReferenceType typeArgument) {
    ClassOrInterfaceType genericInner = ClassOrInterfaceType.forClass(Outer.Inner.class);
    return genericInner.substitute(
        new Substitution(genericInner.getTypeParameters(), typeArgument));
  }

  /**
   * The superclass of {@code Outer<String>.Child<Integer>} is {@code Outer<String>.Inner}, not
   * {@code Outer<Long>.Inner}.
   */
  @Test
  public void testMemberSubclassOfMemberClass() {
    ClassOrInterfaceType genericChild = ClassOrInterfaceType.forClass(Outer.Child.class);
    // The type parameters of Outer<E>.Child<U> are E and U, in that order.
    ClassOrInterfaceType stringIntegerChild =
        genericChild.substitute(
            new Substitution(
                genericChild.getTypeParameters(),
                JavaTypes.STRING_TYPE,
                (ReferenceType) JavaTypes.INT_TYPE.toBoxedPrimitive()));
    ReferenceType longType = (ReferenceType) JavaTypes.LONG_TYPE.toBoxedPrimitive();
    assertTrue(stringIntegerChild.isSubtypeOfOrEqualTo(outerInnerType(JavaTypes.STRING_TYPE)));
    assertFalse(stringIntegerChild.isSubtypeOfOrEqualTo(outerInnerType(longType)));
  }

  /**
   * A bound that is a member type with no type parameters of its own, such as {@code
   * Outer<String>.Inner}, contains that type.
   */
  @Test
  public void testMemberTypeBound() {
    ClassOrInterfaceType stringInner = outerInnerType(JavaTypes.STRING_TYPE);
    ParameterBound bound = ParameterBound.forType(stringInner);
    assertTrue(bound.isLowerBound(stringInner, new Substitution()));
    assertTrue(bound.isUpperBound(stringInner, new Substitution()));
  }

  /**
   * A method whose parameter type is a member type whose enclosing type has a wildcard.
   *
   * @param inner an inner object
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static void wildcardOwner(Outer<?>.Inner inner) {}

  /**
   * Capture conversion of {@code Outer<?>.Inner} does not change that type, and its result has a
   * capture variable.
   */
  @Test
  public void testMemberTypeCaptureConversion() throws Exception {
    Method m = InstantiationTest.class.getDeclaredMethod("wildcardOwner", Outer.Inner.class);
    ClassOrInterfaceType wildcardInner =
        ClassOrInterfaceType.forType(m.getGenericParameterTypes()[0]);
    assertTrue(wildcardInner.hasWildcardArgument());
    ClassOrInterfaceType captured = wildcardInner.applyCaptureConversion();
    assertTrue(wildcardInner.hasWildcardArgument());
    assertFalse(captured.hasWildcardArgument());
    assertTrue(captured.hasCaptureVariable());
    assertTrue(new TypeTuple(Collections.<Type>singletonList(captured)).hasCaptureVariable());
    assertFalse(
        new TypeTuple(Collections.<Type>singletonList(outerInnerType(JavaTypes.STRING_TYPE)))
            .hasCaptureVariable());
  }

  /**
   * An interface whose type argument is a type that implements it.
   *
   * @param <T> the type parameter
   */
  private interface Marker<T> {}

  /** A class that implements {@link Marker} for itself. */
  private static class MarkerBase implements Marker<MarkerBase> {}

  /** A subclass of {@link MarkerBase}, which is a {@code Marker<MarkerBase>}. */
  private static class MarkerChild extends MarkerBase {}

  /**
   * A method whose type parameter has a bound that mentions it.
   *
   * @param <T> the type parameter
   * @param t a value
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Marker<T>> void marked(T t) {}

  /**
   * {@code MarkerChild} is within the bound {@code T} of {@code <T extends Marker<T>>}, because
   * {@code T} can be its supertype {@code MarkerBase}, though not {@code MarkerChild} itself.
   */
  @Test
  public void testSelfBoundedVariableBound() throws Exception {
    Method m = InstantiationTest.class.getDeclaredMethod("marked", Marker.class);
    TypeVariable t = (TypeVariable) TypedOperation.forMethod(m).getInputTypes().get(0);
    ParameterBound bound = ParameterBound.forType(t);
    assertTrue(
        bound.isUpperBound(ClassOrInterfaceType.forClass(MarkerBase.class), new Substitution()));
    assertTrue(
        bound.isUpperBound(ClassOrInterfaceType.forClass(MarkerChild.class), new Substitution()));
    assertFalse(bound.isUpperBound(JavaTypes.STRING_TYPE, new Substitution()));
  }

  /** A class with a member class that extends {@code Outer<String>.Inner}. */
  private static class OwnerC {
    /** A member class that extends a member class, of another class, with no type parameters. */
    class SubOfInner extends Outer<String>.Inner {
      /**
       * Creates a SubOfInner.
       *
       * @param outer the enclosing instance of the superclass
       */
      SubOfInner(Outer<String> outer) {
        outer.super();
      }
    }
  }

  /**
   * {@code OwnerC.SubOfInner}, which extends {@code Outer<String>.Inner}, instantiates {@code
   * Outer<E>.Inner} by binding {@code E} to {@code String}, though {@code Inner} declares no type
   * parameters and the enclosing classes differ.
   */
  @Test
  public void testMemberSubclassOfNonParameterizedMemberClass() {
    ClassOrInterfaceType sub = ClassOrInterfaceType.forClass(OwnerC.SubOfInner.class);
    ClassOrInterfaceType genericInner = ClassOrInterfaceType.forClass(Outer.Inner.class);
    TypeVariable outerParameter = genericInner.getTypeParameters().get(0);
    Substitution substitution = sub.getInstantiatingSubstitution(genericInner);
    assertNotNull(substitution);
    assertEquals(JavaTypes.STRING_TYPE, substitution.get(outerParameter));
  }

  /**
   * A generic class whose member class has a type parameter bounded by the enclosing class's type
   * parameter.
   *
   * @param <T> the type parameter
   */
  private static class BoundedOuter<T extends Number> {
    /**
     * A member class whose type parameter is bounded by that of the enclosing class.
     *
     * @param <U> the type parameter
     */
    class Inner<U extends T> {}
  }

  /**
   * A method whose parameter type is a member type with a wildcard type argument.
   *
   * @param inner an inner object
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static void boundedWildcardInner(BoundedOuter<Integer>.Inner<?> inner) {}

  /**
   * The capture variable of {@code BoundedOuter<Integer>.Inner<?>}, where the type parameter of
   * {@code Inner} is bounded by the type parameter of {@code BoundedOuter}, has upper bound {@code
   * Integer}.
   */
  @Test
  public void testMemberTypeCaptureBoundFromEnclosingType() throws Exception {
    Method m =
        InstantiationTest.class.getDeclaredMethod("boundedWildcardInner", BoundedOuter.Inner.class);
    InstantiatedType wildcardInner =
        (InstantiatedType) ClassOrInterfaceType.forType(m.getGenericParameterTypes()[0]);
    InstantiatedType captured = wildcardInner.applyCaptureConversion();
    TypeVariable capture =
        (TypeVariable) ((ReferenceArgument) captured.getTypeArguments().get(0)).getReferenceType();
    ParameterBound upperBound = capture.getUpperTypeBound();
    assertTrue(upperBound.toString(), upperBound.getTypeParameters().isEmpty());
    assertTrue(
        upperBound.toString(),
        upperBound.isUpperBound(ReferenceType.forClass(Integer.class), new Substitution()));
  }

  /**
   * Applying the substitution from {@link InstantiatedType#getTypeSubstitutionWithEnclosingTypes()}
   * to the generic class type yields the instantiated type, including its enclosing type.
   */
  @Test
  public void testTypeSubstitutionWithEnclosingTypes() {
    ReferenceType integerType = (ReferenceType) JavaTypes.INT_TYPE.toBoxedPrimitive();
    InstantiatedType stringInteger =
        (InstantiatedType) holderType(JavaTypes.STRING_TYPE, integerType);
    Substitution substitution = stringInteger.getTypeSubstitutionWithEnclosingTypes();
    assertNotNull(substitution);
    ClassOrInterfaceType genericHolder = ClassOrInterfaceType.forClass(Outer.Holder.class);
    assertEquals(stringInteger, genericHolder.substitute(substitution));
  }

  /**
   * A method whose type parameter is bounded by a member type that has no type parameters of its
   * own.
   *
   * @param <T> the type parameter
   * @param inner an inner object
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Outer<String>.Inner> void boundedByInner(T inner) {}

  /**
   * A method whose type parameter is bounded by a member type whose owner type mentions the type
   * parameter.
   *
   * @param <T> the type parameter
   * @param holder a holder
   */
  @SuppressWarnings({"UnusedMethod", "PMD.UnusedPrivateMethod"}) // used via reflection
  private static <T extends Outer<T>.Holder<Integer>> void boundedByRecursiveOwner(T holder) {}

  /**
   * Converting a type variable whose bound is a member type, such as {@code T extends
   * Outer<String>.Inner} or {@code T extends Outer<T>.Holder<Integer>}, succeeds.
   */
  @Test
  public void testMemberTypeBoundsFromReflection() throws Exception {
    Method inner = InstantiationTest.class.getDeclaredMethod("boundedByInner", Outer.Inner.class);
    TypeVariable t = (TypeVariable) TypedOperation.forMethod(inner).getInputTypes().get(0);
    ClassOrInterfaceType stringInner = outerInnerType(JavaTypes.STRING_TYPE);
    assertTrue(t.getUpperTypeBound().isUpperBound(stringInner, new Substitution()));

    Method recursive =
        InstantiationTest.class.getDeclaredMethod("boundedByRecursiveOwner", Outer.Holder.class);
    TypeVariable u = (TypeVariable) TypedOperation.forMethod(recursive).getInputTypes().get(0);
    assertTrue(u.getUpperTypeBound().getTypeParameters().contains(u));
  }
}
