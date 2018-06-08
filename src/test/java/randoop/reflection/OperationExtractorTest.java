package randoop.reflection;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;

/**
 * Tests for {@link OperationExtractor} to ensure it is collecting the right types and operations.
 * Tests separately for a concrete class and a generic class.
 */
public class OperationExtractorTest {

  @Test
  public void concreteClassTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();

    ReflectionManager mgr = new ReflectionManager(IS_PUBLIC);

    Class<?> c;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.ConcreteClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    final OperationExtractor extractor =
        new OperationExtractor(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    mgr.apply(extractor, c);
    operations.addAll(extractor.getOperations());
    assertThat("name should be", classType.getName(), is(equalTo(c.getName())));

    int expectedCount = 14;
    assertThat(
        "class has " + expectedCount + " operations",
        operations.size(),
        is(equalTo(expectedCount)));

    int genericOpCount = 0;
    int wildcardOpCount = 0;
    for (TypedOperation operation : operations) {
      if (operation.isGeneric()) {
        genericOpCount++;
      }
      if (operation.hasWildcardTypes()) {
        wildcardOpCount++;
      }
    }
    assertThat("class has one generic operation", 1, is(equalTo(genericOpCount)));
    assertThat(
        "class has no operations with wildcards other than getClass",
        1,
        is(equalTo(wildcardOpCount)));
  }

  @Test
  public void genericClassTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager mgr = new ReflectionManager(IS_PUBLIC);

    Class<?> c;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.GenericClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertTrue("should be a generic type", classType.isGeneric());

    assertTrue("should have type parameters", classType.getTypeParameters().size() > 0);
    Substitution<ReferenceType> substitution =
        Substitution.forArgs(classType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    classType = classType.apply(substitution);
    final OperationExtractor extractor =
        new OperationExtractor(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    mgr.apply(extractor, c);
    operations.addAll(extractor.getOperations());
    int expectedCount = 21;
    assertThat(
        "there should be " + expectedCount + " operations",
        expectedCount,
        is(equalTo(operations.size())));
  }

  @Test
  public void memberOfGenericTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager mgr = new ReflectionManager(IS_PUBLIC);

    String classname = "randoop.reflection.GenericTreeWithInnerNode";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertTrue("should be a generic type", classType.isGeneric());
    assertTrue("should have type parameters", classType.getTypeParameters().size() > 0);

    Substitution<ReferenceType> substitution =
        Substitution.forArgs(classType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    classType = classType.apply(substitution);
    final OperationExtractor extractor =
        new OperationExtractor(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    mgr.apply(extractor, c);
    operations.addAll(extractor.getOperations());
    int expectedCount = 4;
    assertThat("should be " + expectedCount + " operations", expectedCount, is(equalTo(4)));

    ClassOrInterfaceType memberType = null;
    for (TypedOperation operation : operations) {
      if (!operation.getOutputType().equals(classType)
          && !operation.getOutputType().equals(JavaTypes.VOID_TYPE)) {
        memberType = (ClassOrInterfaceType) operation.getOutputType();
      }
    }
    assertNotNull(memberType);
    assertThat(
        "member type name",
        memberType.getName(),
        is(equalTo("randoop.reflection.GenericTreeWithInnerNode<java.lang.String>.Node")));
    assertFalse("is generic", memberType.isGeneric());
    assertTrue("is parameterized", memberType.isParameterized());
  }

  @Test
  public void memberExtendingEnclosingTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager mgr = new ReflectionManager(IS_PUBLIC);

    String classname = "randoop.reflection.GenericWithInnerSub$Inner";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertFalse("static member should not be a generic type", classType.isGeneric());
    assertFalse("should not have type parameters", classType.getTypeParameters().size() > 0);
    assertFalse("static member is not parameterized", classType.isParameterized());
    final OperationExtractor extractor =
        new OperationExtractor(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    mgr.apply(extractor, classType.getRuntimeClass());
    operations.addAll(extractor.getOperations());
    int expectedCount = 3;
    assertThat(
        "should be " + expectedCount + " operations",
        expectedCount,
        is(equalTo(operations.size())));
  }

  @Test
  public void partialInstantiationTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager mgr =
        new ReflectionManager(
            new VisibilityPredicate.PackageVisibilityPredicate(
                this.getClass().getPackage().getName()));

    String classname = "randoop.reflection.PartialBindingInput";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertFalse("class type should not be generic", classType.isGeneric());
    assertFalse("class type is not parameterized", classType.isParameterized());
    assertFalse("should not have type parameters", classType.getTypeParameters().size() > 0);
    final OperationExtractor extractor =
        new OperationExtractor(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    mgr.apply(extractor, classType.getRuntimeClass());
    operations.addAll(extractor.getOperations());
    int expectedCount = 4;
    assertThat(
        "should be " + expectedCount + " operations",
        expectedCount,
        is(equalTo(operations.size())));
  }

  @Test
  public void inaccessibleArgumentTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    VisibilityPredicate visibility = IS_PUBLIC;
    ReflectionManager mgr = new ReflectionManager(visibility);
    String classname = "randoop.reflection.visibilitytest.InaccessibleArgumentInput";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    final OperationExtractor extractor =
        new OperationExtractor(classType, new DefaultReflectionPredicate(), visibility);
    mgr.apply(extractor, classType.getRuntimeClass());
    operations.addAll(extractor.getOperations());
    assertTrue("should be three usable operations", operations.size() == 3);
    for (TypedOperation operation : operations) {
      assertThat(
          "should be wildcard or variable",
          operation.getName(),
          anyOf(
              is(
                  equalTo(
                      "randoop.reflection.visibilitytest.InaccessibleArgumentInput.mTypeVariable")),
              is(equalTo("randoop.reflection.visibilitytest.InaccessibleArgumentInput.mWildcard")),
              is(equalTo("java.lang.Object.getClass"))));
    }
  }
}
