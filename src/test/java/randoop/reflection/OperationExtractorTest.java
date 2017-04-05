package randoop.reflection;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());

    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.ConcreteClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
    }
    assert c != null;
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    mgr.apply(
        new OperationExtractor(classType, operations, new DefaultReflectionPredicate(), visibility),
        c);
    assertThat("name should be", classType.getName(), is(equalTo(c.getName())));

    assertThat("class has 12 operations", operations.size(), is(equalTo(12)));

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
    assertThat("class has one generic operation", genericOpCount, is(equalTo(1)));
    assertThat("class has no operations with wildcards", wildcardOpCount, is(equalTo(0)));
  }

  @Test
  public void genericClassTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());

    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.GenericClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
    }
    assert c != null;
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertTrue("should be a generic type", classType.isGeneric());

    assertTrue("should have type parameters", classType.getTypeParameters().size() > 0);
    Substitution<ReferenceType> substitution =
        Substitution.forArgs(classType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    classType = classType.apply(substitution);
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    mgr.apply(
        new OperationExtractor(classType, operations, new DefaultReflectionPredicate(), visibility),
        c);

    assertThat("there should be 20 operations", operations.size(), is(equalTo(20)));
  }

  @Test
  public void memberOfGenericTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());

    String classname = "randoop.reflection.GenericTreeWithInnerNode";
    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
    }
    assert c != null;
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertTrue("should be a generic type", classType.isGeneric());
    assertTrue("should have type parameters", classType.getTypeParameters().size() > 0);

    Substitution<ReferenceType> substitution =
        Substitution.forArgs(classType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    classType = classType.apply(substitution);
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    mgr.apply(
        new OperationExtractor(classType, operations, new DefaultReflectionPredicate(), visibility),
        c);
    assertThat("should be three operations", operations.size(), is(equalTo(3)));

    ClassOrInterfaceType memberType = null;
    for (TypedOperation operation : operations) {
      if (!operation.getOutputType().equals(classType)
          && !operation.getOutputType().equals(JavaTypes.VOID_TYPE)) {
        memberType = (ClassOrInterfaceType) operation.getOutputType();
      }
    }
    assert memberType != null : "member type should not be null";
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
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());

    String classname = "randoop.reflection.GenericWithInnerSub$Inner";
    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
    }
    assert c != null;
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertFalse("static member should not be a generic type", classType.isGeneric());
    assertFalse("should not have type parameters", classType.getTypeParameters().size() > 0);
    assertFalse("static member is not parameterized", classType.isParameterized());
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    mgr.apply(
        new OperationExtractor(classType, operations, new DefaultReflectionPredicate(), visibility),
        classType.getRuntimeClass());

    assertThat("should be two operations", operations.size(), is(equalTo(2)));
  }

  @Test
  public void partialInstantiationTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager mgr =
        new ReflectionManager(
            new PackageVisibilityPredicate(this.getClass().getPackage().getName()));

    String classname = "randoop.reflection.PartialBindingInput";
    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
    }
    assert c != null;
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertFalse("class type should not be generic", classType.isGeneric());
    assertFalse("class type is not parameterized", classType.isParameterized());
    assertFalse("should not have type parameters", classType.getTypeParameters().size() > 0);
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    mgr.apply(
        new OperationExtractor(classType, operations, new DefaultReflectionPredicate(), visibility),
        classType.getRuntimeClass());
    assertThat("should be 3 operations", operations.size(), is(equalTo(3)));
  }

  @Test
  public void inaccessibleArgumentTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ReflectionManager mgr = new ReflectionManager(visibility);
    String classname = "randoop.reflection.visibilitytest.InaccessibleArgumentInput";
    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
    }
    assert c != null;
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    mgr.apply(
        new OperationExtractor(classType, operations, new DefaultReflectionPredicate(), visibility),
        classType.getRuntimeClass());
    assertTrue("should be two usable operations", operations.size() == 2);
    for (TypedOperation operation : operations) {
      assertThat(
          "should be wildcard or variable",
          operation.getName(),
          anyOf(is(equalTo("mTypeVariable")), is(equalTo("mWildcard"))));
    }
  }
}
