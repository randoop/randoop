package randoop.reflection;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.util.Collection;
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
    Class<?> c;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.ConcreteClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    Collection<TypedOperation> operations =
        OperationExtractor.operations(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    assertEquals(c.getName(), classType.getBinaryName());

    assertEquals(14, operations.size());

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
    assertEquals(1, genericOpCount);
    assertEquals(1, wildcardOpCount);
  }

  @Test
  public void genericClassTest() {
    Class<?> c;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.GenericClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertTrue(classType.isGeneric());

    assertFalse(classType.getTypeParameters().isEmpty());
    Substitution substitution =
        new Substitution(classType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    classType = classType.substitute(substitution);
    final Collection<TypedOperation> operations =
        OperationExtractor.operations(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    assertEquals(21, operations.size());
  }

  @Test
  public void memberOfGenericTest() {
    String classname = "randoop.reflection.GenericTreeWithInnerNode";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertTrue(classType.isGeneric());
    assertFalse(classType.getTypeParameters().isEmpty());

    Substitution substitution =
        new Substitution(classType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    classType = classType.substitute(substitution);

    final Collection<TypedOperation> operations =
        OperationExtractor.operations(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    assertEquals(4, operations.size());

    ClassOrInterfaceType memberType = null;
    for (TypedOperation operation : operations) {
      if (!operation.getOutputType().equals(classType)
          && !operation.getOutputType().equals(JavaTypes.VOID_TYPE)) {
        memberType = (ClassOrInterfaceType) operation.getOutputType();
      }
    }
    assertNotNull(memberType);
    assertEquals(
        "randoop.reflection.GenericTreeWithInnerNode<java.lang.String>$Node",
        memberType.getBinaryName());
    assertFalse(memberType.isGeneric());
    assertTrue(memberType.isParameterized());
  }

  @Test
  public void memberExtendingEnclosingTest() {
    String classname = "randoop.reflection.GenericWithInnerSub$Inner";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertFalse(classType.isGeneric());
    assertFalse(classType.getTypeParameters().size() > 0);
    assertFalse(classType.isParameterized());
    final Collection<TypedOperation> operations =
        OperationExtractor.operations(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    assertEquals(3, operations.size());
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
    assertFalse(classType.isGeneric());
    assertFalse(classType.isParameterized());
    assertFalse(classType.getTypeParameters().size() > 0);
    final OperationExtractor extractor =
        new OperationExtractor(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    mgr.apply(extractor, classType.getRuntimeClass());
    operations.addAll(extractor.getOperations());
    assertEquals(4, operations.size());
  }

  @Test
  public void inaccessibleArgumentTest() {
    VisibilityPredicate visibility = IS_PUBLIC;
    String classname = "randoop.reflection.visibilitytest.InaccessibleArgumentInput";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }

    final Collection<TypedOperation> operations =
        OperationExtractor.operations(c, new DefaultReflectionPredicate(), visibility);
    assertEquals(3, operations.size());
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
