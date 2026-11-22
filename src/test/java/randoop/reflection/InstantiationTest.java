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
import randoop.types.ParameterizedType;
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

  /** Based on issue #1889: a type parameter bounded by its declaring type applied to a wildcard. */
  @Test
  public void testWildcardBoundInstantiation() {
    Set<@ClassGetName String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.reflection.WildcardBoundBuilder");
    classnames.add("randoop.reflection.ConcreteWildcardBoundBuilder");
    OperationModel model = createModel(classnames, "randoop.reflection");

    Set<TypedOperation> classOperations = new LinkedHashSet<>();
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    getOperations(model, classOperations, inputTypes);

    boolean foundCreate = false;
    for (TypedOperation operation : classOperations) {
      if (operation.getName().endsWith(".create")
          && ((TypedClassOperation) operation).getDeclaringType().getRuntimeClass()
              == WildcardBoundBuilder.class) {
        foundCreate = true;
      }
    }
    assertTrue("should instantiate WildcardBoundBuilder.create", foundCreate);
  }

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
   * Tests that a type variable that occurs only in wildcard bounds can be instantiated by a type
   * argument of an input type, even if that type argument is not itself an input type.
   */
  @Test
  public void testInferredTypeArgumentInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    Set<Type> inputTypes = new LinkedHashSet<>();
    // The input types include List<Integer>, but not Integer.
    addTypes(
        GenericClassType.forClass(List.class).instantiate(JavaTypes.INT_TYPE.toBoxedPrimitive()),
        inputTypes);
    assertFalse(inputTypes.contains(JavaTypes.INT_TYPE.toBoxedPrimitive()));
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    // <T> void copy(List<? super T>, List<? extends T>)
    TypedClassOperation copy =
        TypedOperation.forMethod(Collections.class.getMethod("copy", List.class, List.class));
    for (int seed = 0; seed < 10; seed++) {
      Randomness.setSeed(seed);
      TypedClassOperation copyInstance = instantiator.instantiate(copy);
      assertNotNull("instantiation failed for " + copy, copyInstance);
      assertFalse(copyInstance.isGeneric(/* ignoreWildcards= */ true));
    }
    Randomness.setSeed(0);
  }

  /**
   * Tests the instantiation of a type variable that is instantiated by matching a parameterized
   * parameter type, and whose bound mentions two type variables that remain to be instantiated: the
   * instantiation satisfies that bound, whatever the random choices.
   */
  @Test
  public void testMatchedVariableBoundInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    InstantiatedType mapType =
        JDKTypes.HASH_MAP_TYPE.instantiate(
            JavaTypes.STRING_TYPE, JavaTypes.INT_TYPE.toBoxedPrimitive());
    InstantiatedType listOfMaps = JDKTypes.ARRAY_LIST_TYPE.instantiate(mapType);
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    addTypes(listOfMaps, inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    // <K, V, M extends Map<K, V>> Map<K, V> someMap(Collection<M>)
    TypedClassOperation someMap =
        TypedOperation.forMethod(RelatedBoundsCase.class.getMethod("someMap", Collection.class));

    for (int seed = 0; seed < 20; seed++) {
      Randomness.setSeed(seed);
      TypedClassOperation instance = instantiator.instantiate(someMap);
      assertNotNull("instantiation failed for " + someMap, instance);
      // M := HashMap<String, Integer> satisfies M extends Map<K, V> only if K := String and
      // V := Integer.
      assertEquals(JDKTypes.COLLECTION_TYPE.instantiate(mapType), instance.getInputTypes().get(0));
      assertEquals(
          JDKTypes.MAP_TYPE.instantiate(
              JavaTypes.STRING_TYPE, JavaTypes.INT_TYPE.toBoxedPrimitive()),
          instance.getOutputType());
    }
    Randomness.setSeed(0);
  }

  /**
   * Tests the instantiation of a type variable that occurs only in the bound of a type variable
   * that is instantiated by matching a parameterized parameter type, when the only suitable type is
   * a type argument of a supertype of the matched type, and is not an input type.
   */
  @Test
  public void testBoundArgumentInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    ClassOrInterfaceType nodeType = ClassOrInterfaceType.forClass(RelatedBoundsCase.Node.class);
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JDKTypes.ARRAY_LIST_TYPE.instantiate(nodeType), inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    // <K, V extends Map<K, V>> K selfMapKey(List<V>)
    TypedClassOperation selfMapKey =
        TypedOperation.forMethod(RelatedBoundsCase.class.getMethod("selfMapKey", List.class));

    for (int seed = 0; seed < 20; seed++) {
      Randomness.setSeed(seed);
      TypedClassOperation instance = instantiator.instantiate(selfMapKey);
      assertNotNull("instantiation failed for " + selfMapKey, instance);
      // V := Node satisfies V extends Map<K, V> only if K := Key.
      assertEquals(JDKTypes.LIST_TYPE.instantiate(nodeType), instance.getInputTypes().get(0));
      assertEquals(
          ClassOrInterfaceType.forClass(RelatedBoundsCase.Key.class), instance.getOutputType());
    }
    Randomness.setSeed(0);
  }

  /**
   * Tests the instantiation of a type variable whose upper bound is a type variable that must be
   * instantiated by a proper supertype of the type of the first type variable.
   */
  @Test
  public void testSupertypeBoundInstantiation()
      throws NoSuchMethodException, RandoopSpecificationError {
    ClassOrInterfaceType derivedType =
        ClassOrInterfaceType.forClass(RelatedBoundsCase.Derived.class);
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    addTypes(JDKTypes.ARRAY_LIST_TYPE.instantiate(derivedType), inputTypes);
    TypeInstantiator instantiator = new TypeInstantiator(inputTypes);
    // <T extends Comparable<T>, V extends T> T comparableSupertype(List<V>)
    TypedClassOperation comparableSupertype =
        TypedOperation.forMethod(
            RelatedBoundsCase.class.getMethod("comparableSupertype", List.class));

    for (int seed = 0; seed < 20; seed++) {
      Randomness.setSeed(seed);
      TypedClassOperation instance = instantiator.instantiate(comparableSupertype);
      assertNotNull("instantiation failed for " + comparableSupertype, instance);
      // Derived is not Comparable<Derived>, so T := Base.
      assertEquals(JDKTypes.LIST_TYPE.instantiate(derivedType), instance.getInputTypes().get(0));
      assertEquals(
          ClassOrInterfaceType.forClass(RelatedBoundsCase.Base.class), instance.getOutputType());
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
    // The capture variable for the wildcard is instantiated, too.
    assertEquals(
        GenericClassType.forClass(Collection.class).instantiate(JavaTypes.STRING_TYPE),
        instance.getInputTypes().get(0));
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
}
