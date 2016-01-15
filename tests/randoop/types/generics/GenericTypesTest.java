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
import randoop.types.SimpleType;
import randoop.types.Substitution;
import randoop.types.TypeBound;

public class GenericTypesTest {

  @Test
  public void testVariableParameters() {
    Class<?> c1 = Variable1.class;
    GenericType a1 = GenericType.forClass(c1);
    assertEquals("has one bound", 1, a1.getBounds().size());
    assertEquals("the bound is Object", new ConcreteTypeBound(new SimpleType(Object.class)), a1.getBounds().get(0));
    GenericType a1Type = new GenericClassType(c1);
    assertEquals("objects built fromClass and constructed are same", a1Type, a1);
    
    TypeBound b1 = a1.getBounds().get(0);
    Substitution subst = Substitution.forArgs(new ArrayList<TypeVariable<?>>(), new ConcreteType[0]);
    assertTrue("String satisfies bound", b1.isSatisfiedBy(new SimpleType(String.class), subst ));
    
   
   
    Class<?> c2 = Variable2.class;
    GenericType a2Type = new GenericClassType(c2);
    GenericType a2 = GenericType.forClass(c2);
    assertEquals("has two bounds", 2, a2.getBounds().size());
    for (TypeBound o : a2.getBounds()) {
      assertEquals("both bounds are Object", new ConcreteTypeBound(new SimpleType(Object.class)), o);
    }
    assertEquals("objects built fromClass and constructed are same", a2Type, a2);

  }
  
  @Test
  public void testConcreteBounds() {
    Substitution emptySubst = Substitution.forArgs(new ArrayList<TypeVariable<?>>(), new ConcreteType[0]);
    
    Class<?> c1 = Class1.class;
    GenericType a1 = GenericType.forClass(c1);
    assertEquals("has one bound", 1, a1.getBounds().size());
    assertEquals("the bound is Number", new ConcreteTypeBound(new SimpleType(Number.class)), a1.getBounds().get(0));
    GenericType a1Type = new GenericClassType(c1);
    assertEquals("built and constructed object same", a1Type, a1);
    
    TypeBound b1 = a1.getBounds().get(0);
    assertTrue("Integer satisfies bound Number", b1.isSatisfiedBy(new SimpleType(Integer.class), emptySubst));
    assertFalse("String does not satisfy bound Number", b1.isSatisfiedBy(new SimpleType(String.class), emptySubst));
    
    Class<?> c2 = Class2.class;
    GenericType a2 = GenericType.forClass(c2);
    assertEquals("has one bound", 1, a2.getBounds().size());
    assertEquals("the bound is Comparable<Integer>", new ConcreteTypeBound(ConcreteType.forClass(Comparable.class, new SimpleType(Integer.class))), a2.getBounds().get(0));
    GenericType a2Type = new GenericClassType(c2);
    assertEquals("objects built fromClass and constructed are same", a2Type, a2);
    
    TypeBound b2 = a1.getBounds().get(0);
    assertTrue("Integer satisfies bound Comparable<Integer>", b2.isSatisfiedBy(new SimpleType(Integer.class), emptySubst));
    assertFalse("String does not satisfy bound Comparable<Integer>", b2.isSatisfiedBy(new SimpleType(String.class), emptySubst));
  }
  
  @Test
  public void testParameterizedBounds() {
    //being lazy, rather than building substitution, use instantiate
    
    Class<?> c1 = Parameterized1.class;
    GenericType a1 = GenericType.forClass(c1);
    assertEquals("has one bound", 1, a1.getBounds().size());

    ConcreteType pt = ConcreteType.forClass(Variable1Ext.class);
    try {
      ConcreteType it = a1.instantiate(pt);
      assertTrue("Parameterized type bound satisfied, object instantiated", it != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten an exception");
    }

    ConcreteType pt2 = ConcreteType.forClass(Integer.class);
    try {
      ConcreteType ft = a1.instantiate(pt2);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("illegal argument message matches", "type argument does not match parameter bound", e.getMessage());
    }

    Class<?> c2 = IntersectionBounds.class;
    GenericType a2 = GenericType.forClass(c2);
    assertEquals("has one bound", 1, a2.getBounds().size());

    ConcreteType pt3 = ConcreteType.forClass(Variable1Ext2.class);
    try {
      ConcreteType it2 = a2.instantiate(pt3);
      assertTrue("Intersection bound satisfied", it2 != null);
    } catch (IllegalArgumentException e){
      fail("should not have gotten an exception");
    }

    try {
      ConcreteType ft2 = a2.instantiate(pt);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("illegal argument message matches", "type argument does not match parameter bound", e.getMessage());
    }

    Class<?> c3 = MutuallyRecursive1.class;
    GenericType a3 = GenericType.forClass(c3);
    assertEquals("has two bounds", 2, a3.getBounds().size());

    ConcreteType pt4 = ConcreteType.forClass(Variable1Ext3.class);
    ConcreteType pt5 = ConcreteType.forClass(Variable1Ext4.class);
    try {
      ConcreteType it3 = a3.instantiate(pt4,pt5);
      assertTrue("should have instantiated OK", it3 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten exception");
    }

    try {
      ConcreteType it4 = a3.instantiate(pt5, pt4);
      assertTrue("should have instantiated OK", it4 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten exception");
    }

    try {
      ConcreteType ft3 = a3.instantiate(pt, pt5);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("illegal argument message matches", "type argument does not match parameter bound", e.getMessage());
    }
    
    try {
      ConcreteType ft4 = a3.instantiate(pt);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("illegal argument message matches", "number of parameters and arguments must agree", e.getMessage());
    }
  }

}
