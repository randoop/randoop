package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;

public class GenericTypesTest {

  @Test
  public void testVariableParameters() {
    Class<?> c1 = Variable1.class;
    GenericClassType a1;
    a1 = GenericClassType.forClass(c1);
    assertEquals("has one parameter", 1, a1.getTypeParameters().size());
    assertEquals(
        "the parameter has bound Object",
        new ClassOrInterfaceTypeBound(new SimpleClassOrInterfaceType(Object.class)),
        a1.getTypeParameters().get(0).getTypeBound());

    ParameterBound b1 = a1.getFormalTypeParameters().get(0).getTypeBound();
    Substitution<ReferenceType> subst =
        Substitution.forArgs(new ArrayList<AbstractTypeVariable>());
    assertTrue(
          "String satisfies bound", b1.isSatisfiedBy(new SimpleClassOrInterfaceType(String.class), subst));

    Class<?> c2 = Variable2.class;
    GenericClassType a2;
    a2 = GenericClassType.forClass(c2);
    assertEquals("has two bounds", 2, a2.getTypeParameters().size());
    for (AbstractTypeVariable o : a2.getTypeParameters()) {
      assertEquals(
          "both bounds are Object", new ClassOrInterfaceTypeBound(new SimpleClassOrInterfaceType(Object.class)), o.getTypeBound());
    }
  }

  @Test
  public void testConcreteBounds() {
    Substitution<ReferenceType> emptySubst =
        Substitution.forArgs(new ArrayList<AbstractTypeVariable>());

    Class<?> c1 = Class1.class;
    GenericClassType a1;
    a1 = GenericClassType.forClass(c1);
    assertEquals("has one bound", 1, a1.getTypeParameters().size());
    assertEquals(
        "the bound is Number",
        new ClassOrInterfaceTypeBound(new SimpleClassOrInterfaceType(Number.class)),
        a1.getTypeParameters().get(0).getTypeBound());

    ParameterBound b1 = a1.getFormalTypeParameters().get(0).getTypeBound();
    assertTrue(
              "Integer satisfies bound Number",
              b1.isSatisfiedBy(new SimpleClassOrInterfaceType(Integer.class), emptySubst));
    assertFalse(
              "String does not satisfy bound Number",
              b1.isSatisfiedBy(new SimpleClassOrInterfaceType(String.class), emptySubst));

    Class<?> c2 = Class2.class;
    GenericClassType a2 = GenericClassType.forClass(c2);

    assertEquals("has one bound", 1, a2.getTypeParameters().size());
    assertEquals(
          "the bound is Comparable<Integer>",
          new ClassOrInterfaceTypeBound(
              GenericClassType.forClass(Comparable.class).instantiate(new SimpleClassOrInterfaceType(Integer.class))),
          a2.getTypeParameters().get(0).getTypeBound());

    ParameterBound b2 = a1.getFormalTypeParameters().get(0).getTypeBound();
    assertTrue(
              "Integer satisfies bound Comparable<Integer>",
              b2.isSatisfiedBy(new SimpleClassOrInterfaceType(Integer.class), emptySubst));
    assertFalse(
              "String does not satisfy bound Comparable<Integer>",
              b2.isSatisfiedBy(new SimpleClassOrInterfaceType(String.class), emptySubst));
  }

  @Test
  public void testParameterizedBounds() {
    //being lazy, rather than building substitution, use instantiate

    Class<?> c1 = Parameterized1.class;
    GenericClassType a1 = GenericClassType.forClass(c1);
    assertEquals("has one parameter", 1, a1.getTypeParameters().size());

    try {
      GeneralType it = a1.instantiate(ReferenceType.forClass(Variable1Ext.class));
      assertTrue("Parameterized type bound satisfied, object instantiated", it != null);
    } catch (IllegalArgumentException e) {
    fail("should not have gotten the exception: " + e.getMessage());
  }

    ReferenceType pt2 = ReferenceType.forClass(Integer.class);

    try {
      @SuppressWarnings("unused")
      GeneralType ft = a1.instantiate(pt2);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "type argument does not match parameter bound",
          e.getMessage());
    }

    Class<?> c2 = IntersectionBounds.class;
    GenericClassType a2 = GenericClassType.forClass(c2);
    assertEquals("has one parameter", 1, a2.getTypeParameters().size());

    ReferenceType pt3 = ReferenceType.forClass(Variable1Ext2.class);
    try {
      GeneralType it2 = a2.instantiate(pt3);
      assertTrue("Intersection bound satisfied", it2 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten an exception: " + e.getMessage());
    }

    try {
      @SuppressWarnings("unused")
      GeneralType ft2 = a2.instantiate(ReferenceType.forClass(Variable1Ext.class));
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "type argument does not match parameter bound",
          e.getMessage());
    }

    Class<?> c3 = MutuallyRecursive1.class;
    GenericClassType a3 = GenericClassType.forClass(c3);
    assertEquals("has two parameter", 2, a3.getTypeParameters().size());

    ReferenceType pt4 = ReferenceType.forClass(Variable1Ext3.class);
    ReferenceType pt5 = ReferenceType.forClass(Variable1Ext4.class);

    try {
      GeneralType it3 = a3.instantiate(pt4, pt5);
      assertTrue("should have instantiated OK", it3 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten exception");
    }

    try {
      GeneralType it4 = a3.instantiate(pt5, pt4);
      assertTrue("should have instantiated OK", it4 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten exception");
    }

    try {
      @SuppressWarnings("unused")
      GeneralType ft3 = a3.instantiate(ReferenceType.forClass(Variable1Ext.class), pt5);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "type argument does not match parameter bound",
          e.getMessage());
    }

    try {
      @SuppressWarnings("unused")
      GeneralType ft4 = a3.instantiate(ReferenceType.forClass(Variable1Ext.class));
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "number of arguments and parameters must match",
          e.getMessage());
    }
  }

  @Test
  public void subtypeTransitivityTest() {
    ParameterizedType iterableType = GenericClassType.forClass(Iterable.class).instantiate(ConcreteTypes.STRING_TYPE);
    ParameterizedType collectionType = JDKTypes.COLLECTION_TYPE.instantiate(ConcreteTypes.STRING_TYPE);
    assertTrue("collection is subtype of iterable", collectionType.isSubtypeOf(iterableType));
    assertFalse("iterable is supertype of collection, not subtype", iterableType.isSubtypeOf(collectionType));
    ParameterizedType vectorType = JDKTypes.VECTOR_TYPE.instantiate(ConcreteTypes.STRING_TYPE);
    assertTrue("vector is subtype of iterable", vectorType.isSubtypeOf(iterableType) );
    assertTrue("vector is subtype of collection", vectorType.isSubtypeOf(collectionType));
    assertFalse("supertype is not a subtype", iterableType.isSubtypeOf(vectorType));
    assertFalse("supertype is not a subtype", collectionType.isSubtypeOf(vectorType));
  }
}
