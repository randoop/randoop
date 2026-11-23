package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.types.ExampleClassesForTests.A;
import static randoop.types.ExampleClassesForTests.B;
import static randoop.types.ExampleClassesForTests.C;
import static randoop.types.ExampleClassesForTests.ClassWithGenericInnerClass;
import static randoop.types.ExampleClassesForTests.ClassWithInnerClass;
import static randoop.types.ExampleClassesForTests.D;
import static randoop.types.ExampleClassesForTests.E;
import static randoop.types.ExampleClassesForTests.F;
import static randoop.types.ExampleClassesForTests.G;
import static randoop.types.ExampleClassesForTests.GenericWithInnerClass;
import static randoop.types.ExampleClassesForTests.H;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import randoop.types.test.ParameterInput;

public class ParameterizedTypeTest {

  @Test
  public void testAssignability() {
    Type strALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(NonParameterizedType.forClass(String.class));
    Type intALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(NonParameterizedType.forClass(Integer.class));
    Type objALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(NonParameterizedType.forClass(Object.class));
    Type rawALType = NonParameterizedType.forClass(ArrayList.class);

    assertTrue(strALType.isAssignableFrom(strALType));
    assertFalse(strALType.isAssignableFrom(intALType));
    assertTrue(strALType.isAssignableFrom(rawALType));
    assertFalse(objALType.isAssignableFrom(intALType));

    Type intType = NonParameterizedType.forClass(Integer.class);
    Type intCompType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(NonParameterizedType.forClass(Integer.class));
    assertTrue(intCompType.isAssignableFrom(intType));
    assertFalse(intType.isAssignableFrom(intCompType));

    Type strCompType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(NonParameterizedType.forClass(String.class));
    assertTrue(intCompType.isAssignableFrom(intCompType));
    assertFalse(intCompType.isAssignableFrom(strCompType));

    Type intArrayType = ArrayType.ofComponentType(NonParameterizedType.forClass(Integer.class));
    assertFalse(intCompType.isAssignableFrom(intArrayType));

    // class A<T> implements Comparable<A<T>> {}
    InstantiatedType intAType =
        GenericClassType.forClass(A.class)
            .instantiate(NonParameterizedType.forClass(Integer.class));
    Type intACompType = GenericClassType.forClass(Comparable.class).instantiate(intAType);
    assertTrue(intACompType.isAssignableFrom(intAType));

    // class B extends A<String> {}
    InstantiatedType strAType =
        GenericClassType.forClass(A.class).instantiate(NonParameterizedType.forClass(String.class));
    Type bType = NonParameterizedType.forClass(B.class);
    Type strACompType = GenericClassType.forClass(Comparable.class).instantiate(strAType);
    assertTrue(strAType.isAssignableFrom(bType));
    assertTrue(strACompType.isAssignableFrom(bType));

    // class C extends A<Integer> {}
    Type cType = NonParameterizedType.forClass(C.class);
    assertFalse(strAType.isAssignableFrom(cType));
    assertFalse(strCompType.isAssignableFrom(cType));

    // class H<T> extends G<T> implements Comparable<H<T>> {}
    InstantiatedType strHType =
        GenericClassType.forClass(H.class).instantiate(NonParameterizedType.forClass(String.class));
    Type strHCompType = GenericClassType.forClass(Comparable.class).instantiate(strHType);
    Type strGType =
        GenericClassType.forClass(G.class).instantiate(NonParameterizedType.forClass(String.class));
    assertTrue(strGType.isAssignableFrom(strHType));
    assertTrue(strHCompType.isAssignableFrom(strHType));

    // class D<S,T> extends A<T> {}
    Type strIntDType =
        GenericClassType.forClass(D.class)
            .instantiate(
                NonParameterizedType.forClass(String.class),
                NonParameterizedType.forClass(Integer.class));
    assertTrue(intAType.isAssignableFrom(strIntDType));
    assertFalse(strAType.isAssignableFrom(strIntDType));

    // class E<S,T> {}
    Type strIntEType =
        GenericClassType.forClass(E.class)
            .instantiate(
                NonParameterizedType.forClass(String.class),
                NonParameterizedType.forClass(Integer.class));
    // class F<T,S> extends E<S,T> {}
    Type intStrFType =
        GenericClassType.forClass(F.class)
            .instantiate(
                NonParameterizedType.forClass(Integer.class),
                NonParameterizedType.forClass(String.class));
    assertTrue(strIntEType.isAssignableFrom(intStrFType));
    Type strIntFType =
        GenericClassType.forClass(F.class)
            .instantiate(
                NonParameterizedType.forClass(String.class),
                NonParameterizedType.forClass(Integer.class));
    assertFalse(strIntEType.isAssignableFrom(strIntFType));
  }

  @Test
  public void testNames() {
    Type strALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(NonParameterizedType.forClass(String.class));
    assertEquals("java.util.ArrayList<java.lang.String>", strALType.getBinaryName());
  }

  @Test
  public void testCompareTo() {
    Type strALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(NonParameterizedType.forClass(String.class));
    Type intALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(NonParameterizedType.forClass(Integer.class));
    Type rawALType = NonParameterizedType.forClass(ArrayList.class);
    assertNotEquals(0, strALType.compareTo(intALType));
    assertNotEquals(0, strALType.compareTo(rawALType));
    assertEquals(
        Integer.signum(strALType.compareTo(intALType)),
        -Integer.signum(intALType.compareTo(strALType)));
    assertEquals(
        0,
        strALType.compareTo(
            GenericClassType.forClass(ArrayList.class)
                .instantiate(NonParameterizedType.forClass(String.class))));

    Set<Type> types = new TreeSet<>(Arrays.asList(strALType, intALType, rawALType));
    assertEquals(3, types.size());
  }

  @Test
  public void testInnerClass() {
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();

    // GenericWithInnerClass.StaticInnerClass stc;
    ClassOrInterfaceType staticInnerType =
        (ClassOrInterfaceType) Type.forClass(GenericWithInnerClass.StaticInnerClass.class);
    assertTrue(staticInnerType.isReferenceType());
    assertFalse(staticInnerType.isParameterized());
    assertFalse(staticInnerType.isGeneric());
    assertFalse(staticInnerType.isPrimitive());
    assertFalse(staticInnerType.isRawtype());
    assertEquals(
        "randoop.types.ExampleClassesForTests$GenericWithInnerClass$StaticInnerClass",
        staticInnerType.getBinaryName());
    assertEquals(
        (List<TypeVariable>) new ArrayList<TypeVariable>(), staticInnerType.getTypeParameters());
    assertEquals(
        (List<TypeArgument>) new ArrayList<TypeArgument>(), staticInnerType.getTypeArguments());

    // ClassWithGenericInnerClass.GenericNestedClass<Integer> gnc2;
    ClassOrInterfaceType genericNestedTypeOfClass =
        (ClassOrInterfaceType) Type.forClass(ClassWithGenericInnerClass.GenericNestedClass.class);
    assertTrue(genericNestedTypeOfClass.isGeneric());
    assertFalse(genericNestedTypeOfClass.isParameterized());
    assertEquals(
        "randoop.types.ExampleClassesForTests$ClassWithGenericInnerClass$GenericNestedClass<T>",
        genericNestedTypeOfClass.getBinaryName());
    assertEquals(1, genericNestedTypeOfClass.getTypeParameters().size());
    Substitution substitution =
        new Substitution(genericNestedTypeOfClass.getTypeParameters(), integerType);
    ClassOrInterfaceType instantiatedGenericNestedClass =
        genericNestedTypeOfClass.substitute(substitution);
    assertEquals(
        "randoop.types.ExampleClassesForTests$ClassWithGenericInnerClass$GenericNestedClass<java.lang.Integer>",
        instantiatedGenericNestedClass.getBinaryName());
    substitution =
        new Substitution(
            genericNestedTypeOfClass.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    ClassOrInterfaceType instantiatedGenericNestedClass2 =
        genericNestedTypeOfClass.substitute(substitution);
    assertEquals(instantiatedGenericNestedClass, instantiatedGenericNestedClass);
    assertNotEquals(instantiatedGenericNestedClass, instantiatedGenericNestedClass2);
    assertTrue(instantiatedGenericNestedClass.isInstantiationOf(genericNestedTypeOfClass));
    assertTrue(instantiatedGenericNestedClass2.isInstantiationOf(genericNestedTypeOfClass));
    assertFalse(instantiatedGenericNestedClass.isInstantiationOf(instantiatedGenericNestedClass2));

    // GenericWithInnerClass<Integer>.InnerClass ic;
    ClassOrInterfaceType innerType =
        (ClassOrInterfaceType) Type.forClass(GenericWithInnerClass.InnerClass.class);
    assertFalse(innerType.isParameterized());
    assertTrue(innerType.isGeneric());
    assertEquals(
        "randoop.types.ExampleClassesForTests$GenericWithInnerClass<T>$InnerClass",
        innerType.getBinaryName());
    assertEquals(1, innerType.getTypeParameters().size());
    substitution = new Substitution(innerType.getTypeParameters(), integerType);
    ClassOrInterfaceType instantiatedInnerType = innerType.substitute(substitution);
    assertEquals(
        "randoop.types.ExampleClassesForTests$GenericWithInnerClass<java.lang.Integer>$InnerClass",
        instantiatedInnerType.getBinaryName());
    substitution =
        new Substitution(innerType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    ClassOrInterfaceType instantiatedInnerType2 = innerType.substitute(substitution);
    assertEquals(instantiatedInnerType, instantiatedInnerType);
    assertTrue(instantiatedInnerType.isInstantiationOf(innerType));
    assertTrue(instantiatedInnerType2.isInstantiationOf(innerType));

    // GenericWithInnerClass<String>.GenericNestedClass<Integer> gnc;
    ClassOrInterfaceType genericNestedType =
        (ClassOrInterfaceType) Type.forClass(GenericWithInnerClass.GenericNestedClass.class);
    assertFalse(genericNestedType.isParameterized());
    assertTrue(genericNestedType.isGeneric());
    assertEquals(
        "randoop.types.ExampleClassesForTests$GenericWithInnerClass<T>$GenericNestedClass<S>",
        genericNestedType.getBinaryName());
    assertEquals(2, genericNestedType.getTypeParameters().size());
    substitution =
        new Substitution(genericNestedType.getTypeParameters(), JavaTypes.STRING_TYPE, integerType);
    ClassOrInterfaceType instantiatedGenericNestedType = genericNestedType.substitute(substitution);
    assertEquals(
        "randoop.types.ExampleClassesForTests$GenericWithInnerClass<java.lang.String>$GenericNestedClass<java.lang.Integer>",
        instantiatedGenericNestedType.getBinaryName());
    assertEquals(
        "GenericWithInnerClass<java.lang.String>$GenericNestedClass<java.lang.Integer>",
        instantiatedGenericNestedType.getUnqualifiedBinaryName());
    assertEquals(
        "randoop.types.ExampleClassesForTests.GenericWithInnerClass.GenericNestedClass",
        instantiatedGenericNestedType.getCanonicalName());
    assertEquals(
        "randoop.types.ExampleClassesForTests$GenericWithInnerClass<java.lang.String>$GenericNestedClass<java.lang.Integer>",
        instantiatedGenericNestedType.getBinaryName());
    substitution =
        new Substitution(genericNestedType.getTypeParameters(), integerType, JavaTypes.STRING_TYPE);
    ClassOrInterfaceType instantiatedGenericNestedType2 =
        genericNestedType.substitute(substitution);
    assertEquals(instantiatedGenericNestedType, instantiatedGenericNestedType);
    assertNotEquals(instantiatedGenericNestedType, instantiatedGenericNestedType2);
    assertTrue(instantiatedGenericNestedType.isInstantiationOf(genericNestedType));
    assertTrue(instantiatedGenericNestedType2.isInstantiationOf(genericNestedType));

    ClassOrInterfaceType nonparamInnerClass =
        ClassOrInterfaceType.forClass(ClassWithInnerClass.InnerClass.class);
    ClassOrInterfaceType otherNonparamInnerClass =
        ClassOrInterfaceType.forClass(ClassWithInnerClass.OtherInnerClass.class);
    assertFalse(nonparamInnerClass.isParameterized());
    assertFalse(nonparamInnerClass.isGeneric());
    assertEquals(0, nonparamInnerClass.getTypeParameters().size());
    assertEquals(
        "ExampleClassesForTests$ClassWithInnerClass$InnerClass",
        nonparamInnerClass.getUnqualifiedBinaryName());
    assertEquals(
        "randoop.types.ExampleClassesForTests.ClassWithInnerClass.InnerClass",
        nonparamInnerClass.getCanonicalName());
    assertEquals(nonparamInnerClass, nonparamInnerClass);
    assertNotEquals(nonparamInnerClass, otherNonparamInnerClass);
    assertFalse(nonparamInnerClass.isInstantiationOf(otherNonparamInnerClass));
    assertTrue(nonparamInnerClass.isInstantiationOf(nonparamInnerClass));
  }

  /**
   * Test what happens when get type parameters for cases {@code Iterable<String>} {@code
   * Iterable<T>} {@code Iterable<? extends T>} {@code Iterable<? super T>} {@code Iterable<Cap of ?
   * extends T>} {@code Iterable<Cap of ? super T>} {@code Iterable<? extends Comparable<T>}
   */
  @Test
  public void testTypeParameters() {
    Class<?> c = ParameterInput.class;
    Method m;
    try {
      m =
          c.getMethod(
              "m",
              Iterable.class,
              Iterable.class,
              Iterable.class,
              Iterable.class,
              Iterable.class,
              Iterable.class);
    } catch (NoSuchMethodException e) {
      fail("failed to load method ParameterInput.m()");
      throw new Error("Unreachable");
    }
    for (java.lang.reflect.Type type : m.getGenericParameterTypes()) {
      ParameterizedType itType = InstantiatedType.forType(type);
      if (!itType.isGeneric()) {
        assertTrue(
            "non-generic should not have type parameters: " + itType,
            itType.getTypeParameters().isEmpty());
      } else {
        assertFalse(
            "generic should have type parameters: " + itType, itType.getTypeParameters().isEmpty());
        if (itType.hasWildcard()) {
          // the capture variable should have same type parameter as the original type
          ClassOrInterfaceType capType = itType.applyCaptureConversion();
          for (TypeVariable variable : capType.getTypeParameters()) {
            if (variable instanceof CaptureTypeVariable) {
              assertEquals(capType.getTypeParameters(), variable.getTypeParameters());
            } else if (variable instanceof ExplicitTypeVariable) {
              assertTrue(itType.getTypeParameters().contains(variable));
            } else {
              fail("capture type should only have either capture or explicit variable");
            }
          }
        }
      }
    }
  }

  /** A field whose type is a list of lists of integers. */
  @SuppressWarnings("unused")
  private static List<List<Integer>> listOfIntegerLists;

  /** A field whose type is a list of lists of strings. */
  @SuppressWarnings("unused")
  private static List<List<String>> listOfStringLists;

  /** A field whose type is a collection of numbers. */
  @SuppressWarnings("unused")
  private static Collection<? extends Number> numberCollection;

  /** A field whose type is a list of collections of numbers. */
  @SuppressWarnings("unused")
  private static List<? extends Collection<? extends Number>> listOfNumberCollections;

  /** A field whose type is a collection of supertypes of integers. */
  @SuppressWarnings("unused")
  private static Collection<? super Integer> integerSuperCollection;

  /** A field whose type is a list of unknown elements. */
  @SuppressWarnings("unused")
  private static List<?> unknownList;

  /** A field whose type is a collection of unknown elements. */
  @SuppressWarnings("unused")
  private static Collection<?> unknownCollection;

  /**
   * Returns the type of the given field of this class.
   *
   * @param fieldName the name of a field declared in this class
   * @return the type of the field
   */
  private static Type fieldType(String fieldName) {
    try {
      return Type.forType(ParameterizedTypeTest.class.getDeclaredField(fieldName).getGenericType());
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }

  /**
   * Tests that subtyping applies type argument containment to a supertype of another class: {@code
   * List<Integer>} is a subtype of {@code Collection<? extends Number>}.
   */
  @Test
  public void testContainmentInMatchingSupertype() {
    InstantiatedType listOfInteger =
        GenericClassType.forClass(List.class)
            .instantiate(ClassOrInterfaceType.forClass(Integer.class));
    InstantiatedType listOfString =
        GenericClassType.forClass(List.class).instantiate(JavaTypes.STRING_TYPE);
    Type collectionOfNumbers = fieldType("numberCollection");
    assertTrue(listOfInteger.isSubtypeOfOrEqualTo(collectionOfNumbers));
    assertFalse(listOfString.isSubtypeOfOrEqualTo(collectionOfNumbers));

    Type listOfNumberCollections = fieldType("listOfNumberCollections");
    assertTrue(fieldType("listOfIntegerLists").isSubtypeOfOrEqualTo(listOfNumberCollections));
    assertFalse(fieldType("listOfStringLists").isSubtypeOfOrEqualTo(listOfNumberCollections));
  }

  /**
   * Tests containment of wildcards with different kinds of bounds, in a supertype of another class:
   * {@code ? super Integer} does not contain {@code ?}, but {@code ?} contains {@code ? super
   * Integer}. So {@code List<?>} is not a subtype of {@code Collection<? super Integer>}, but
   * {@code List<Integer>} is.
   */
  @Test
  public void testMixedWildcardContainment() {
    InstantiatedType listOfInteger =
        GenericClassType.forClass(List.class)
            .instantiate(ClassOrInterfaceType.forClass(Integer.class));
    Type integerSuperCollection = fieldType("integerSuperCollection");
    assertTrue(listOfInteger.isSubtypeOfOrEqualTo(integerSuperCollection));
    assertFalse(fieldType("unknownList").isSubtypeOfOrEqualTo(integerSuperCollection));
    assertTrue(integerSuperCollection.isSubtypeOfOrEqualTo(fieldType("unknownCollection")));
  }

  /**
   * A generic class with a non-generic member class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic", "UnusedTypeParameter"})
  private static class OuterWithPlain<T> {
    /** A non-generic member class. */
    class Plain {}
  }

  /**
   * Tests that a parameterized type is not a subtype of a non-generic member class of a
   * parameterized type, as in {@code Outer<String>.Plain}, which is parameterized but is not an
   * {@link InstantiatedType}.
   */
  @Test
  public void testSubtypeOfNonGenericMemberOfParameterizedType() {
    ClassOrInterfaceType plain = ClassOrInterfaceType.forClass(OuterWithPlain.Plain.class);
    TypeVariable t = GenericClassType.forClass(OuterWithPlain.class).getTypeParameters().get(0);
    ClassOrInterfaceType plainOfString =
        plain.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(plainOfString.isParameterized());
    InstantiatedType arrayListOfString =
        GenericClassType.forClass(ArrayList.class).instantiate(JavaTypes.STRING_TYPE);
    assertFalse(arrayListOfString.isSubtypeOfOrEqualTo(plainOfString));
  }
}
