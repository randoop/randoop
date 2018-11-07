package randoop.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import randoop.main.ClassNameErrorHandler;
import randoop.main.ThrowClassNameError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.intersectiontypes.AccessibleInterval;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.JavaTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;

/** Tests instantiation of type parameters by OperationModel. */
public class InstantiationTest {

  @Test
  public void testGenericBounds() {
    Set<String> classnames = new LinkedHashSet<>();
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
    assertThat(
        "expect "
            + expectedClassCount
            + " classes: GenericBounds, SW, TW, UW, VW, WW, XW, YW, RML and Object",
        model.getClassTypes().size(),
        is(equalTo(expectedClassCount)));

    int methodCount = 0;
    for (TypedOperation operation : classOperations) {
      if (operation.isMethodCall()) {
        assertTrue(
            "method name " + operation.getName() + " should be in expected list",
            methodNames.contains(operation.getName()));
        methodCount++;
      }
    }
    assertThat(
        "expect " + methodNames.size() + " methods", methodCount, is(equalTo(methodNames.size())));
  }

  /**
   * This test fails if {@code D_BST} is removed since model always chooses {@code String} for
   * parameter to {@code BST} and without {@code D_BST} there is no class that implements {@code
   * C_BST<String>}.
   *
   * <p>it should be possible for it to pass with {@code B_BST}.
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
    assertThat(
        "expect " + expectedClassCount + " classes",
        model.getClassTypes().size(),
        is(equalTo(expectedClassCount)));

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
    // XXX this should be 1, but running on travis using 1.8.0_101 misses the method,
    //    while running on my machine with 1.8.0_102 finds it
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
    assertThat(
        "expect " + expectedClassCount + " classes",
        model.getClassTypes().size(),
        is(equalTo(expectedClassCount)));

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
    assertThat(
            "expect " + expectedClassCount + " classes",
            model.getClassTypes().size(),
            is(equalTo(expectedClassCount)));

    Set<TypedOperation> classOperations = new LinkedHashSet<>();
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.STRING_TYPE, inputTypes);
    try {
      addTypes(Type.forName("randoop.reflection.StringComparator"), inputTypes);
    } catch (ClassNotFoundException e) {
      fail("cannot build type for comparator");
    }
    Set<String> nullOKNames = new HashSet<>();
    nullOKNames.add("forEach");
    nullOKNames.add("removeIf");
    getOperations(model, classOperations, inputTypes, nullOKNames);

    int count = 0;
    for (TypedOperation operation : classOperations) {
      assertFalse("should not be generic " + operation, operation.getInputTypes().isGeneric());
      assertFalse("should not have wildcards" + operation, operation.getInputTypes().hasWildcard());
      count++;
    }
    assertThat("should have X operations", count, is(equalTo(24)));

  }
  */

  /** Based on a case from imglib2. */
  @Test
  public void testIntersectionType() {
    Set<String> classnames = new LinkedHashSet<>();
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

    assertTrue("should be some operations", classOperations.size() > 0);
    for (TypedOperation operation : classOperations) {
      assertFalse("should not be generic " + operation, operation.getInputTypes().isGeneric());
      assertFalse("should not have wildcards" + operation, operation.getInputTypes().hasWildcard());
    }
  }

  /** Based on a case from Apache Commons Collections. */
  @Test
  public void testCaptureConvInstantiation() {
    Set<String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.reflection.CaptureInstantiationCase");
    OperationModel model = createModel(classnames, "randoop.reflection");
    Set<TypedOperation> classOperations = new LinkedHashSet<>();
    Set<Type> inputTypes = new LinkedHashSet<>();
    addTypes(JavaTypes.INT_TYPE.toBoxedPrimitive(), inputTypes);
    addTypes(ClassOrInterfaceType.forClass(AnIterable.class), inputTypes);
    Substitution<ReferenceType> subst;
    GenericClassType predicateType =
        GenericClassType.forClass(CaptureInstantiationCase.LocalPredicate.class);
    subst = Substitution.forArgs(predicateType.getTypeParameters(), JavaTypes.SERIALIZABLE_TYPE);
    addTypes(predicateType.apply(subst), inputTypes);
    GenericClassType onePredicateType =
        GenericClassType.forClass(CaptureInstantiationCase.OnePredicate.class);
    subst = Substitution.forArgs(onePredicateType.getTypeParameters(), JavaTypes.SERIALIZABLE_TYPE);
    InstantiatedType oneSerializablePredicateType = onePredicateType.apply(subst);
    addTypes(oneSerializablePredicateType, inputTypes);
    subst =
        Substitution.forArgs(
            JDKTypes.TREE_SET_TYPE.getTypeParameters(),
            (ReferenceType) oneSerializablePredicateType);
    addTypes(JDKTypes.TREE_SET_TYPE.apply(subst), inputTypes);
    subst =
        Substitution.forArgs(
            predicateType.getTypeParameters(), (ReferenceType) oneSerializablePredicateType);
    addTypes(predicateType.apply(subst), inputTypes);

    Set<String> nullOKNames = new HashSet<>();
    getOperations(model, classOperations, inputTypes, nullOKNames);
    assertTrue("should be some operations", classOperations.size() > 0);
    for (TypedOperation operation : classOperations) {
      if (operation.getName().equals("filter")) {
        assertFalse("filter operation should be instantiated ", operation.isGeneric());
      }
      if (operation.getName().equals("oneOf")) {
        assertFalse("oneOf operation should be instantiated", operation.isGeneric());
      }
    }
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
    Substitution<ReferenceType> substitution = Substitution.forArgs(JDKTypes.TREE_SET_TYPE.getTypeParameters(), (ReferenceType)JavaTypes.STRING_TYPE);
    addTypes(JDKTypes.TREE_SET_TYPE.apply(substitution), inputTypes);

    Set<String> nullOKNames = new HashSet<>();
    getOperations(model, classOperations, inputTypes, nullOKNames);
    assertTrue("should be some operations", classOperations.size() > 0);

  }
  */

  private OperationModel createModel(Set<String> names, String packageName) {
    VisibilityPredicate visibility =
        new VisibilityPredicate.PackageVisibilityPredicate(packageName);
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<String> coveredClassnames = new LinkedHashSet<>();
    Set<String> methodSignatures = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new ThrowClassNameError();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model =
          OperationModel.createModel(
              visibility,
              reflectionPredicate,
              names,
              coveredClassnames,
              methodSignatures,
              errorHandler,
              literalsFileList);
    } catch (SignatureParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
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
            assertNotNull(
                "instantiation of method " + operation + " should not be null", classOperation);
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
        typeSet.add(classType);
        typeSet.addAll(classType.getSuperTypes());
      }
    }
  }
}
