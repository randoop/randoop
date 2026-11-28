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
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;

/**
 * Tests for {@link OperationExtractor} to ensure it is collecting the right types and operations.
 * Tests separately for a concrete class and a generic class.
 */
public class OperationExtractorTest {

  @Test
  public void concreteClassTest() throws RandoopSpecificationError {
    Class<?> c;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.ConcreteClass");
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      fail("didn't find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    Collection<TypedOperation> operations = OperationExtractor.operations(classType);
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
  public void genericClassTest() throws RandoopSpecificationError {
    Class<?> c;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.GenericClass");
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      fail("didn't find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertTrue(classType.isGeneric());

    assertFalse(classType.getTypeParameters().isEmpty());
    Substitution substitution =
        new Substitution(classType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    classType = classType.substitute(substitution);
    final Collection<TypedOperation> operations = OperationExtractor.operations(classType);
    assertEquals(21, operations.size());
  }

  @Test
  public void memberOfGenericTest() throws RandoopSpecificationError {
    String classname = "randoop.reflection.GenericTreeWithInnerNode";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertTrue(classType.isGeneric());
    assertFalse(classType.getTypeParameters().isEmpty());

    Substitution substitution =
        new Substitution(classType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    classType = classType.substitute(substitution);

    final Collection<TypedOperation> operations = OperationExtractor.operations(classType);
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
  public void memberExtendingEnclosingTest() throws RandoopSpecificationError {
    String classname = "randoop.reflection.GenericWithInnerSub$Inner";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertFalse(classType.isGeneric());
    assertTrue(classType.getTypeParameters().isEmpty());
    assertFalse(classType.isParameterized());
    final Collection<TypedOperation> operations = OperationExtractor.operations(classType);
    assertEquals(3, operations.size());
  }

  @Test
  public void partialInstantiationTest() throws RandoopSpecificationError {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager mgr =
        new ReflectionManager(
            new AccessibilityPredicate.PackageAccessibilityPredicate(
                this.getClass().getPackage().getName()));

    String classname = "randoop.reflection.PartialBindingInput";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertFalse(classType.isGeneric());
    assertFalse(classType.isParameterized());
    assertTrue(classType.getTypeParameters().isEmpty());
    final OperationExtractor extractor =
        new OperationExtractor(classType, new DefaultReflectionPredicate(), IS_PUBLIC);
    mgr.apply(extractor, classType.getRuntimeClass());
    operations.addAll(extractor.getOperations());
    assertEquals(4, operations.size());
  }

  @Test
  public void inaccessibleArgumentTest() throws RandoopSpecificationError {
    String classname = "randoop.reflection.accessibilitytest.InaccessibleArgumentInput";
    Class<?> c;
    try {
      c = TypeNames.getTypeForName(classname);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      fail("did not find class: " + e);
      throw new Error("Unreachable");
    }

    final Collection<TypedOperation> operations = OperationExtractor.operations(c);
    assertEquals(3, operations.size());
    for (TypedOperation operation : operations) {
      assertThat(
          "should be wildcard or variable",
          operation.getName(),
          anyOf(
              is(
                  equalTo(
                      "randoop.reflection.accessibilitytest.InaccessibleArgumentInput.mTypeVariable")),
              is(
                  equalTo(
                      "randoop.reflection.accessibilitytest.InaccessibleArgumentInput.mWildcard")),
              is(equalTo("java.lang.Object.getClass"))));
    }
  }

  /**
   * Returns the output type of each method that {@link OperationExtractor} returns for the given
   * class.
   *
   * @param c a class
   * @return a map from the simple name of each method of {@code c} to its output type
   */
  private static Map<String, Type> methodOutputTypes(Class<?> c) throws RandoopSpecificationError {
    Map<String, Type> result = new HashMap<>();
    for (TypedOperation operation : OperationExtractor.operations(c)) {
      if (operation.isMethodCall()) {
        String name = operation.getName();
        result.put(name.substring(name.lastIndexOf('.') + 1), operation.getOutputType());
      }
    }
    return result;
  }

  /**
   * A class that is not generic and extends a member class of an instantiation of a generic class
   * inherits operations that are instantiated by the type arguments of that enclosing type.
   */
  @Test
  public void memberSubclassTest() throws RandoopSpecificationError {
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();

    // InnerSubclass extends MemberSubclassOuter<Integer>.Inner.
    ClassOrInterfaceType genericInner =
        ClassOrInterfaceType.forClass(MemberSubclassOuter.Inner.class);
    Substitution substitution =
        ClassOrInterfaceType.forClass(MemberSubclassOuter.InnerSubclass.class)
            .getInstantiatingSubstitution(genericInner);
    assertNotNull(substitution);
    assertEquals(integerType, substitution.get(genericInner.getTypeParameters().get(0)));
    assertEquals(
        integerType, methodOutputTypes(MemberSubclassOuter.InnerSubclass.class).get("getT"));

    // StringInnerSubclass extends MemberSubclassOuter<Integer>.StringInner, which extends
    // GenericInner<String>.
    ClassOrInterfaceType genericGenericInner =
        ClassOrInterfaceType.forClass(MemberSubclassOuter.GenericInner.class);
    substitution =
        ClassOrInterfaceType.forClass(MemberSubclassOuter.StringInnerSubclass.class)
            .getInstantiatingSubstitution(genericGenericInner);
    assertNotNull(substitution);
    assertEquals(integerType, substitution.get(genericGenericInner.getTypeParameters().get(0)));
    assertEquals(
        JavaTypes.STRING_TYPE, substitution.get(genericGenericInner.getTypeParameters().get(1)));
    Map<String, Type> outputTypes =
        methodOutputTypes(MemberSubclassOuter.StringInnerSubclass.class);
    assertEquals(integerType, outputTypes.get("getT"));
    assertEquals(JavaTypes.STRING_TYPE, outputTypes.get("getI"));
  }
}
