package randoop.types.generics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;

import org.junit.Test;

import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeBound;
import randoop.types.GenericClassType;
import randoop.types.GenericType;
import randoop.types.ConcreteSimpleType;
import randoop.types.RandoopTypeException;
import randoop.types.Substitution;
import randoop.types.TypeBound;

public class GenericTypesTest {

  @Test
  public void testVariableParameters() {
    Class<?> c1 = Variable1.class;
    GenericType a1 = null;
    try {
      a1 = GenericType.forClass(c1);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("has one bound", 1, a1.getBounds().size());
    assertEquals(
        "the bound is Object",
        new ConcreteTypeBound(new ConcreteSimpleType(Object.class)),
        a1.getBounds().get(0));
    GenericType a1Type = null;
    try {
      a1Type = new GenericClassType(c1);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("objects built fromClass and constructed are same", a1Type, a1);

    TypeBound b1 = a1.getBounds().get(0);
    Substitution subst =
        Substitution.forArgs(new ArrayList<TypeVariable<?>>());
    try {
      assertTrue(
          "String satisfies bound", b1.isSatisfiedBy(new ConcreteSimpleType(String.class), subst));
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    Class<?> c2 = Variable2.class;
    GenericType a2Type = null;
    try {
      a2Type = new GenericClassType(c2);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    GenericType a2 = null;
    try {
      a2 = GenericType.forClass(c2);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("has two bounds", 2, a2.getBounds().size());
    for (TypeBound o : a2.getBounds()) {
      assertEquals(
          "both bounds are Object", new ConcreteTypeBound(new ConcreteSimpleType(Object.class)), o);
    }
    assertEquals("objects built fromClass and constructed are same", a2Type, a2);
  }

  @Test
  public void testConcreteBounds() {
    Substitution emptySubst =
        Substitution.forArgs(new ArrayList<TypeVariable<?>>());

    Class<?> c1 = Class1.class;
    GenericType a1 = null;
    try {
      a1 = GenericType.forClass(c1);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("has one bound", 1, a1.getBounds().size());
    assertEquals(
        "the bound is Number",
        new ConcreteTypeBound(new ConcreteSimpleType(Number.class)),
        a1.getBounds().get(0));
    GenericType a1Type = null;
    try {
      a1Type = new GenericClassType(c1);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("built and constructed object same", a1Type, a1);

    TypeBound b1 = a1.getBounds().get(0);
    try {
      assertTrue(
              "Integer satisfies bound Number",
              b1.isSatisfiedBy(new ConcreteSimpleType(Integer.class), emptySubst));
      assertFalse(
              "String does not satisfy bound Number",
              b1.isSatisfiedBy(new ConcreteSimpleType(String.class), emptySubst));
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    Class<?> c2 = Class2.class;
    GenericType a2 = null;
    try {
      a2 = GenericType.forClass(c2);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("has one bound", 1, a2.getBounds().size());
    try {
      assertEquals(
          "the bound is Comparable<Integer>",
          new ConcreteTypeBound(
              ConcreteType.forClass(Comparable.class, new ConcreteSimpleType(Integer.class))),
          a2.getBounds().get(0));
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    GenericType a2Type = null;
    try {
      a2Type = new GenericClassType(c2);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("objects built fromClass and constructed are same", a2Type, a2);

    TypeBound b2 = a1.getBounds().get(0);
    try {
      assertTrue(
              "Integer satisfies bound Comparable<Integer>",
              b2.isSatisfiedBy(new ConcreteSimpleType(Integer.class), emptySubst));
      assertFalse(
              "String does not satisfy bound Comparable<Integer>",
              b2.isSatisfiedBy(new ConcreteSimpleType(String.class), emptySubst));
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
  }

  @Test
  public void testParameterizedBounds() {
    //being lazy, rather than building substitution, use instantiate

    Class<?> c1 = Parameterized1.class;
    GenericType a1 = null;
    try {
      a1 = GenericType.forClass(c1);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("has one bound", 1, a1.getBounds().size());

    ConcreteType pt = null;
    try {
      pt = ConcreteType.forClass(Variable1Ext.class);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    try {
      ConcreteType it = a1.instantiate(pt);
      assertTrue("Parameterized type bound satisfied, object instantiated", it != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten an exception");
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    ConcreteType pt2 = null;
    try {
      pt2 = ConcreteType.forClass(Integer.class);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    try {
      @SuppressWarnings("unused")
      ConcreteType ft = a1.instantiate(pt2);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "type argument does not match parameter bound",
          e.getMessage());
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    Class<?> c2 = IntersectionBounds.class;
    GenericType a2 = null;
    try {
      a2 = GenericType.forClass(c2);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("has one bound", 1, a2.getBounds().size());

    ConcreteType pt3 = null;
    try {
      pt3 = ConcreteType.forClass(Variable1Ext2.class);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    try {
      ConcreteType it2 = a2.instantiate(pt3);
      assertTrue("Intersection bound satisfied", it2 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten an exception");
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    try {
      @SuppressWarnings("unused")
      ConcreteType ft2 = a2.instantiate(pt);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "type argument does not match parameter bound",
          e.getMessage());
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    Class<?> c3 = MutuallyRecursive1.class;
    GenericType a3 = null;
    try {
      a3 = GenericType.forClass(c3);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    assertEquals("has two bounds", 2, a3.getBounds().size());

    ConcreteType pt4 = null;
    try {
      pt4 = ConcreteType.forClass(Variable1Ext3.class);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    ConcreteType pt5 = null;
    try {
      pt5 = ConcreteType.forClass(Variable1Ext4.class);
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    try {
      ConcreteType it3 = a3.instantiate(pt4, pt5);
      assertTrue("should have instantiated OK", it3 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten exception");
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    try {
      ConcreteType it4 = a3.instantiate(pt5, pt4);
      assertTrue("should have instantiated OK", it4 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten exception");
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    try {
      @SuppressWarnings("unused")
      ConcreteType ft3 = a3.instantiate(pt, pt5);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "type argument does not match parameter bound",
          e.getMessage());
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    try {
      @SuppressWarnings("unused")
      ConcreteType ft4 = a3.instantiate(pt);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "number of parameters and arguments must agree",
          e.getMessage());
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
  }
}
