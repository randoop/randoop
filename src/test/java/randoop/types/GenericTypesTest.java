package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import randoop.types.test.ComplexSubclass;
import randoop.types.test.Superclass;

public class GenericTypesTest {

  @Test
  public void testVariableParameters() {
    Class<?> c1 = Variable1.class;
    GenericClassType a1;
    a1 = GenericClassType.forClass(c1);
    assertEquals("has one parameter", 1, a1.getTypeParameters().size());
    assertEquals(
        "the parameter has bound Object",
        new EagerReferenceBound(new NonParameterizedType(Object.class)),
        a1.getTypeParameters().get(0).getUpperTypeBound());

    ParameterBound b1 = a1.getTypeParameters().get(0).getUpperTypeBound();
    Substitution<ReferenceType> subst =
        Substitution.forArgs(a1.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    assertTrue("String satisfies bound", b1.isUpperBound(JavaTypes.STRING_TYPE, subst));

    Class<?> c2 = Variable2.class;
    GenericClassType a2;
    a2 = GenericClassType.forClass(c2);
    assertEquals("has two bounds", 2, a2.getTypeParameters().size());
    for (TypeVariable o : a2.getTypeParameters()) {
      assertEquals(
          "both bounds are Object",
          new EagerReferenceBound(new NonParameterizedType(Object.class)),
          o.getUpperTypeBound());
    }
  }

  @Test
  public void testConcreteBounds() {

    Class<?> c1 = Class1.class;
    GenericClassType a1;
    a1 = GenericClassType.forClass(c1);
    assertEquals("has one parameter", 1, a1.getTypeParameters().size());
    assertEquals(
        "the bound is Number",
        new EagerReferenceBound(new NonParameterizedType(Number.class)),
        a1.getTypeParameters().get(0).getUpperTypeBound());

    Substitution<ReferenceType> substitution;
    ParameterBound b1 = a1.getTypeParameters().get(0).getUpperTypeBound();
    ReferenceType candidateType = new NonParameterizedType(Integer.class);
    substitution = Substitution.forArgs(a1.getTypeParameters(), candidateType);
    assertTrue("Integer satisfies bound Number", b1.isUpperBound(candidateType, substitution));
    candidateType = JavaTypes.STRING_TYPE;
    substitution = Substitution.forArgs(a1.getTypeParameters(), candidateType);
    assertFalse(
        "String does not satisfy bound Number", b1.isUpperBound(candidateType, substitution));

    Class<?> c2 = Class2.class;
    GenericClassType a2 = GenericClassType.forClass(c2);
    ParameterBound b2 = a2.getTypeParameters().get(0).getUpperTypeBound();

    assertEquals("has one parameter", 1, a2.getTypeParameters().size());
    candidateType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(new NonParameterizedType(Integer.class));
    substitution = Substitution.forArgs(a2.getTypeParameters(), candidateType);
    assertTrue(
        "Comparable<Integer> satisfies bound Comparable<Integer>",
        b2.isUpperBound(candidateType, substitution));

    candidateType = new NonParameterizedType(Integer.class);
    substitution = Substitution.forArgs(a2.getTypeParameters(), candidateType);
    assertTrue(
        "Integer satisfies bound Comparable<Integer>",
        b2.isUpperBound(candidateType, substitution));

    candidateType = new NonParameterizedType(String.class);
    substitution = Substitution.forArgs(a2.getTypeParameters(), candidateType);
    assertFalse(
        "String does not satisfy bound Comparable<Integer>",
        b2.isUpperBound(new NonParameterizedType(String.class), substitution));
  }

  @Test
  public void testParameterizedBounds() {
    //being lazy, rather than building substitution, use instantiate

    Class<?> c1 = Parameterized1.class;
    GenericClassType a1 = GenericClassType.forClass(c1);
    assertEquals("has one parameter", 1, a1.getTypeParameters().size());

    try {
      Type it = a1.instantiate(ReferenceType.forClass(Variable1Ext.class));
      assertTrue("Parameterized type bound satisfied, object instantiated", it != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten the exception: " + e.getMessage());
    }

    ReferenceType pt2 = ReferenceType.forClass(Integer.class);

    try {
      @SuppressWarnings("unused")
      Type ft = a1.instantiate(pt2);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "type argument java.lang.Integer does not match parameter bound randoop.types.Variable1<T>",
          e.getMessage());
    }

    Class<?> c2 = IntersectionBounds.class;
    GenericClassType a2 = GenericClassType.forClass(c2);
    assertEquals("has one parameter", 1, a2.getTypeParameters().size());

    ReferenceType pt3 = ReferenceType.forClass(Variable1Ext2.class);
    try {
      Type it2 = a2.instantiate(pt3);
      assertTrue("Intersection bound satisfied", it2 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten an exception: " + e.getMessage());
    }

    try {
      @SuppressWarnings("unused")
      Type ft2 = a2.instantiate(ReferenceType.forClass(Variable1Ext.class));
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "type argument randoop.types.Variable1Ext does not match parameter bound "
              + "randoop.types.Variable1<T> & java.lang.Comparable<T>",
          e.getMessage());
    }

    Class<?> c3 = MutuallyRecursive1.class;
    GenericClassType a3 = GenericClassType.forClass(c3);
    assertEquals("has two parameter", 2, a3.getTypeParameters().size());

    ReferenceType pt4 = ReferenceType.forClass(Variable1Ext3.class);
    ReferenceType pt5 = ReferenceType.forClass(Variable1Ext4.class);

    try {
      Type it3 = a3.instantiate(pt4, pt5);
      assertTrue("should have instantiated OK", it3 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten exception");
    }

    try {
      Type it4 = a3.instantiate(pt5, pt4);
      assertTrue("should have instantiated OK", it4 != null);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten exception");
    }

    try {
      @SuppressWarnings("unused")
      Type ft3 = a3.instantiate(ReferenceType.forClass(Variable1Ext.class), pt5);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "illegal argument message matches",
          "type argument randoop.types.Variable1Ext does not match parameter bound randoop.types.Variable1<T>",
          e.getMessage());
    }

    try {
      @SuppressWarnings("unused")
      Type ft4 = a3.instantiate(ReferenceType.forClass(Variable1Ext.class));
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
    ParameterizedType iterableType =
        GenericClassType.forClass(Iterable.class).instantiate(JavaTypes.STRING_TYPE);
    ParameterizedType collectionType = JDKTypes.COLLECTION_TYPE.instantiate(JavaTypes.STRING_TYPE);
    assertTrue("collection is subtype of iterable", collectionType.isSubtypeOf(iterableType));
    assertFalse(
        "iterable is supertype of collection, not subtype",
        iterableType.isSubtypeOf(collectionType));
    ParameterizedType vectorType = JDKTypes.VECTOR_TYPE.instantiate(JavaTypes.STRING_TYPE);
    assertTrue("vector is subtype of iterable", vectorType.isSubtypeOf(iterableType));
    assertTrue("vector is subtype of collection", vectorType.isSubtypeOf(collectionType));
    assertFalse("supertype is not a subtype", iterableType.isSubtypeOf(vectorType));
    assertFalse("supertype is not a subtype", collectionType.isSubtypeOf(vectorType));
  }

  @Test
  public void parameterizedSupertypeTest() {
    // subclass extends parameterized Superclass<Set<T>>
    GenericClassType genericSubtype = GenericClassType.forClass(ComplexSubclass.class);
    InstantiatedType subtype = genericSubtype.instantiate(JavaTypes.STRING_TYPE);

    // make instantiated Superclass<Set<String>>
    GenericClassType genericSetType = GenericClassType.forClass(Set.class);
    InstantiatedType stringSetType = genericSetType.instantiate(JavaTypes.STRING_TYPE);
    GenericClassType genericSuperType = GenericClassType.forClass(Superclass.class);
    InstantiatedType stringSuperType = genericSuperType.instantiate(stringSetType);

    assertTrue(
        "ComplexSubclass<String> should be subtype of Superclass<Set<String>>",
        subtype.isSubtypeOf(stringSuperType));
    assertEquals("superclass", stringSuperType, subtype.getSuperclass());

    // try with example inspired by java.util.stream.Stream (which is Java 8)
    GenericClassType genericStreamType = GenericClassType.forClass(Stream.class);
    InstantiatedType stringStreamType = genericStreamType.instantiate(JavaTypes.STRING_TYPE);
    GenericClassType genericBaseStreamType = GenericClassType.forClass(BaseStream.class);
    InstantiatedType stringBaseStreamType =
        genericBaseStreamType.instantiate(JavaTypes.STRING_TYPE, stringStreamType);

    assertTrue("is subtype", stringStreamType.isSubtypeOf(stringBaseStreamType));
    assertEquals("superclass", null, stringStreamType.getSuperclass());
    assertTrue("interface", stringStreamType.getInterfaces().contains(stringBaseStreamType));
  }

  @Test
  public void wildcardAssignabilityTest() {
    // List<? extends Number> list;
    //ArrayList<? extends Number> arrayList = new ArrayList<>();
    //list = arrayList;

    ParameterBound bound = ParameterBound.forType(ReferenceType.forClass(Number.class));
    WildcardType wildcardType = new WildcardType(bound, true);
    TypeArgument argument = new WildcardArgument(wildcardType);
    List<TypeArgument> arguments = new ArrayList<>();
    arguments.add(argument);
    InstantiatedType list = new InstantiatedType(JDKTypes.LIST_TYPE, arguments);
    InstantiatedType arraylist = new InstantiatedType(JDKTypes.ARRAY_LIST_TYPE, arguments);

    assertTrue("? extends Number should contain itself", argument.contains(argument));

    List<TypeArgument> listTypeArguments = list.getTypeArguments();
    List<TypeArgument> arraylistTypeArguments = arraylist.getTypeArguments();
    assertEquals(
        "number of arguments should be same",
        listTypeArguments.size(),
        arraylistTypeArguments.size());
    for (int i = 0; i < listTypeArguments.size(); i++) {
      assertTrue(
          "list args should contain arraylist args",
          listTypeArguments.get(i).contains(arraylistTypeArguments.get(i)));
    }

    assertTrue(
        "List<? extends Number> is assignable from ArrayList<? extends Number>",
        list.isAssignableFrom(arraylist));
    assertFalse(
        "ArrayList<? extends Number> is not assignable from List<? extends Number>",
        arraylist.isAssignableFrom(list));
  }
}
