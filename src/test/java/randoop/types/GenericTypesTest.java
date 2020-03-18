package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.types.ExampleClassesForTests.BaseStream;
import static randoop.types.ExampleClassesForTests.Stream;
import static randoop.types.GenericsExamples.Class1;
import static randoop.types.GenericsExamples.Class2;
import static randoop.types.GenericsExamples.IntersectionBounds;
import static randoop.types.GenericsExamples.MutuallyRecursive1;
import static randoop.types.GenericsExamples.Parameterized1;
import static randoop.types.GenericsExamples.Variable1;
import static randoop.types.GenericsExamples.Variable1Ext;
import static randoop.types.GenericsExamples.Variable1Ext2;
import static randoop.types.GenericsExamples.Variable1Ext3;
import static randoop.types.GenericsExamples.Variable1Ext4;
import static randoop.types.GenericsExamples.Variable2;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import randoop.types.test.ComplexSubclass;
import randoop.types.test.Superclass;

public class GenericTypesTest {

  @Test
  public void testVariableParameters() {
    Class<?> c1 = Variable1.class;
    GenericClassType a1 = GenericClassType.forClass(c1);
    assertEquals(1, a1.getTypeParameters().size());
    assertEquals(
        new EagerReferenceBound(NonParameterizedType.forClass(Object.class)),
        a1.getTypeParameters().get(0).getUpperTypeBound());

    ParameterBound b1 = a1.getTypeParameters().get(0).getUpperTypeBound();
    Substitution subst =
        new Substitution(a1.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    assertTrue(b1.isUpperBound(JavaTypes.STRING_TYPE, subst));

    Class<?> c2 = Variable2.class;
    GenericClassType a2 = GenericClassType.forClass(c2);
    assertEquals(2, a2.getTypeParameters().size());
    for (TypeVariable o : a2.getTypeParameters()) {
      assertEquals(
          new EagerReferenceBound(NonParameterizedType.forClass(Object.class)),
          o.getUpperTypeBound());
    }
  }

  @Test
  public void testConcreteBounds() {

    Class<?> c1 = Class1.class;
    GenericClassType a1 = GenericClassType.forClass(c1);
    assertEquals(1, a1.getTypeParameters().size());
    assertEquals(
        new EagerReferenceBound(NonParameterizedType.forClass(Number.class)),
        a1.getTypeParameters().get(0).getUpperTypeBound());

    Substitution substitution;
    ParameterBound b1 = a1.getTypeParameters().get(0).getUpperTypeBound();
    ReferenceType candidateType = NonParameterizedType.forClass(Integer.class);
    substitution = new Substitution(a1.getTypeParameters(), candidateType);
    assertTrue(b1.isUpperBound(candidateType, substitution));
    candidateType = JavaTypes.STRING_TYPE;
    substitution = new Substitution(a1.getTypeParameters(), candidateType);
    assertFalse(b1.isUpperBound(candidateType, substitution));

    Class<?> c2 = Class2.class;
    GenericClassType a2 = GenericClassType.forClass(c2);
    ParameterBound b2 = a2.getTypeParameters().get(0).getUpperTypeBound();

    assertEquals(1, a2.getTypeParameters().size());
    candidateType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(NonParameterizedType.forClass(Integer.class));
    substitution = new Substitution(a2.getTypeParameters(), candidateType);
    assertTrue(b2.isUpperBound(candidateType, substitution));

    candidateType = NonParameterizedType.forClass(Integer.class);
    substitution = new Substitution(a2.getTypeParameters(), candidateType);
    assertTrue(b2.isUpperBound(candidateType, substitution));

    candidateType = NonParameterizedType.forClass(String.class);
    substitution = new Substitution(a2.getTypeParameters(), candidateType);
    assertFalse(b2.isUpperBound(NonParameterizedType.forClass(String.class), substitution));
  }

  @Test
  public void testParameterizedBounds() throws IllegalArgumentException {
    // being lazy, rather than building substitution, use instantiate

    Class<?> c1 = Parameterized1.class;
    GenericClassType a1 = GenericClassType.forClass(c1);
    assertEquals(1, a1.getTypeParameters().size());

    try {
      Type it = a1.instantiate(ReferenceType.forClass(Variable1Ext.class));
      assertNotNull(it);
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
          "type argument java.lang.Integer does not match parameter bound randoop.types.GenericsExamples$Variable1<T>",
          e.getMessage());
    }

    Class<?> c2 = IntersectionBounds.class;
    GenericClassType a2 = GenericClassType.forClass(c2);
    assertEquals(1, a2.getTypeParameters().size());
    ReferenceType pt3 = ReferenceType.forClass(Variable1Ext2.class);
    try {
      Type it2 = a2.instantiate(pt3);
      assertNotNull(it2);
    } catch (IllegalArgumentException e) {
      fail("should not have gotten an exception: " + e.getMessage());
    }

    try {
      @SuppressWarnings("unused")
      Type ft2 = a2.instantiate(ReferenceType.forClass(Variable1Ext.class));
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "type argument randoop.types.GenericsExamples$Variable1Ext does not match parameter bound "
              + "randoop.types.GenericsExamples$Variable1<T> & java.lang.Comparable<T>",
          e.getMessage());
    }

    Class<?> c3 = MutuallyRecursive1.class;
    GenericClassType a3 = GenericClassType.forClass(c3);
    assertEquals(2, a3.getTypeParameters().size());

    ReferenceType pt4 = ReferenceType.forClass(Variable1Ext3.class);
    ReferenceType pt5 = ReferenceType.forClass(Variable1Ext4.class);

    Type it3 = a3.instantiate(pt4, pt5);
    assertNotNull(it3);

    Type it4 = a3.instantiate(pt5, pt4);
    assertNotNull(it4);

    try {
      @SuppressWarnings("unused")
      Type ft3 = a3.instantiate(ReferenceType.forClass(Variable1Ext.class), pt5);
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "type argument randoop.types.GenericsExamples$Variable1Ext does not match parameter bound randoop.types.GenericsExamples$Variable1<T>",
          e.getMessage());
    }

    try {
      @SuppressWarnings("unused")
      Type ft4 = a3.instantiate(ReferenceType.forClass(Variable1Ext.class));
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("number of arguments and parameters must match", e.getMessage());
    }
  }

  // Type hierarchy:
  // IB2  IA   Object
  //   \    \ /
  //   IB    A
  //     \ /
  //      B
  //      |
  //      C

  static interface IA {}

  static interface IB2 {}

  static interface IB extends IB2 {}

  static class A implements IA {}

  static class B extends A implements IB {}

  static class C extends B {}

  @Test
  public void subtypeTransitivityTest() {
    NonParameterizedType timerType = NonParameterizedType.forClass(java.util.Timer.class);
    ParameterizedType iterableType =
        GenericClassType.forClass(Iterable.class).instantiate(JavaTypes.STRING_TYPE);
    ParameterizedType collectionType = JDKTypes.COLLECTION_TYPE.instantiate(JavaTypes.STRING_TYPE);
    ParameterizedType vectorType = JDKTypes.VECTOR_TYPE.instantiate(JavaTypes.STRING_TYPE);

    assertStrictSubtype(collectionType, iterableType);
    assertStrictSubtype(vectorType, iterableType);
    assertStrictSubtype(vectorType, collectionType);
    assertStrictSubtype(JavaTypes.STRING_TYPE, JavaTypes.OBJECT_TYPE);
    assertStrictSubtype(JavaTypes.STRING_TYPE, JavaTypes.SERIALIZABLE_TYPE);
    assertStrictSubtype(JavaTypes.SERIALIZABLE_TYPE, JavaTypes.OBJECT_TYPE);
    assertStrictSubtype(timerType, JavaTypes.OBJECT_TYPE);

    NonParameterizedType typeIA = NonParameterizedType.forClass(IA.class);
    NonParameterizedType typeIB = NonParameterizedType.forClass(IB.class);
    NonParameterizedType typeIB2 = NonParameterizedType.forClass(IB2.class);
    NonParameterizedType typeA = NonParameterizedType.forClass(A.class);
    NonParameterizedType typeB = NonParameterizedType.forClass(B.class);
    NonParameterizedType typeC = NonParameterizedType.forClass(C.class);
    assertStrictSubtype(typeA, JavaTypes.OBJECT_TYPE);
    assertStrictSubtype(typeA, typeIA);
    assertStrictSubtype(typeIB, JavaTypes.OBJECT_TYPE);
    assertStrictSubtype(typeIB, typeIB2);
    assertStrictSubtype(typeB, JavaTypes.OBJECT_TYPE);
    assertStrictSubtype(typeB, typeIA);
    assertStrictSubtype(typeB, typeA);
    assertStrictSubtype(typeB, typeIB2);
    assertStrictSubtype(typeB, typeIB);
    assertStrictSubtype(typeC, JavaTypes.OBJECT_TYPE);
    assertStrictSubtype(typeC, typeIA);
    assertStrictSubtype(typeC, typeA);
    assertStrictSubtype(typeC, typeIB2);
    assertStrictSubtype(typeC, typeIB);
    assertStrictSubtype(typeC, typeB);

    assertTrue(JavaTypes.OBJECT_TYPE.isSubtypeOf(JavaTypes.OBJECT_TYPE));
  }

  /** Assert that {@code subtype} is a strict subtype of {@code supertype}. */
  private static void assertStrictSubtype(Type subtype, Type supertype) {
    assertTrue(subtype.isSubtypeOf(supertype));
    assertFalse(supertype.isSubtypeOf(subtype));
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

    assertTrue(subtype.isSubtypeOf(stringSuperType));
    assertEquals(stringSuperType, subtype.getSuperclass());

    // try with example inspired by java.util.stream.Stream (which was introduced in Java 8)
    GenericClassType genericStreamType = GenericClassType.forClass(Stream.class);
    InstantiatedType stringStreamType = genericStreamType.instantiate(JavaTypes.STRING_TYPE);
    GenericClassType genericBaseStreamType = GenericClassType.forClass(BaseStream.class);
    InstantiatedType stringBaseStreamType =
        genericBaseStreamType.instantiate(JavaTypes.STRING_TYPE, stringStreamType);

    assertTrue(stringStreamType.isSubtypeOf(stringBaseStreamType));
    assertEquals(JavaTypes.OBJECT_TYPE, stringStreamType.getSuperclass());
    assertTrue(stringStreamType.getInterfaces().contains(stringBaseStreamType));
  }

  @Test
  public void wildcardAssignabilityTest() {
    // List<? extends Number> list;
    // ArrayList<? extends Number> arrayList = new ArrayList<>();
    // list = arrayList;

    ParameterBound bound = ParameterBound.forType(ReferenceType.forClass(Number.class));
    WildcardType wildcardType = new WildcardType(bound, true);
    TypeArgument argument = new WildcardArgument(wildcardType);
    List<TypeArgument> arguments = Collections.singletonList(argument);
    InstantiatedType list = new InstantiatedType(JDKTypes.LIST_TYPE, arguments);
    InstantiatedType arraylist = new InstantiatedType(JDKTypes.ARRAY_LIST_TYPE, arguments);

    assertTrue(argument.contains(argument));

    List<TypeArgument> listTypeArguments = list.getTypeArguments();
    List<TypeArgument> arraylistTypeArguments = arraylist.getTypeArguments();
    assertEquals(listTypeArguments.size(), arraylistTypeArguments.size());
    for (int i = 0; i < listTypeArguments.size(); i++) {
      assertTrue(listTypeArguments.get(i).contains(arraylistTypeArguments.get(i)));
    }

    assertTrue(list.isAssignableFrom(arraylist));
    assertFalse(arraylist.isAssignableFrom(list));
  }
}
