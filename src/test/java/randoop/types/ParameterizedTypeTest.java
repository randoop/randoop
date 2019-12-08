package randoop.types;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import java.util.List;
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

    assertTrue(
        "ArrayList<String> can be assigned to itself", strALType.isAssignableFrom(strALType));
    assertFalse(
        "ArrayList<Integer> cannot be assigned to ArrayList<String>",
        strALType.isAssignableFrom(intALType));
    assertTrue(
        "ArrayList can be assigned to ArrayList<String>", strALType.isAssignableFrom(rawALType));
    assertFalse(
        "ArrayList<Integer> cannot be assigned to ArrayList<Number>",
        objALType.isAssignableFrom(intALType));

    Type intType = NonParameterizedType.forClass(Integer.class);
    Type intCompType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(NonParameterizedType.forClass(Integer.class));
    assertTrue("Integer assignable to Comparable<Integer>", intCompType.isAssignableFrom(intType));
    assertFalse(
        "Comparable<Integer> not assignable to Integer", intType.isAssignableFrom(intCompType));

    Type strCompType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(NonParameterizedType.forClass(String.class));
    assertTrue(
        "Comparable<Integer> assignable to Comparable<Integer>",
        intCompType.isAssignableFrom(intCompType));
    assertFalse(
        "Comparable<Integer> is not assignable from Comparable<String>",
        intCompType.isAssignableFrom(strCompType));

    Type intArrayType = ArrayType.ofComponentType(NonParameterizedType.forClass(Integer.class));
    assertFalse(
        "Comparable<Integer> not assignable from Integer[]",
        intCompType.isAssignableFrom(intArrayType));

    // class A<T> implements Comparable<A<T>> {}
    InstantiatedType intAType =
        GenericClassType.forClass(A.class)
            .instantiate(NonParameterizedType.forClass(Integer.class));
    Type intACompType = GenericClassType.forClass(Comparable.class).instantiate(intAType);
    assertTrue(
        "A<Integer> assignable to Comparable<A<Integer>>", intACompType.isAssignableFrom(intAType));

    // class B extends A<String> {}
    InstantiatedType strAType =
        GenericClassType.forClass(A.class).instantiate(NonParameterizedType.forClass(String.class));
    Type bType = NonParameterizedType.forClass(B.class);
    Type strACompType = GenericClassType.forClass(Comparable.class).instantiate(strAType);
    assertTrue("B assignable to A<String>", strAType.isAssignableFrom(bType));
    assertTrue("B assignable to Comparable<A<String>>", strACompType.isAssignableFrom(bType));

    // class C extends A<Integer> {}
    Type cType = NonParameterizedType.forClass(C.class);
    assertFalse("C not assignable to A<String>", strAType.isAssignableFrom(cType));
    assertFalse("C not assignable to Comparable<String>", strCompType.isAssignableFrom(cType));

    // class H<T> extends G<T> implements Comparable<H<T>> {}
    InstantiatedType strHType =
        GenericClassType.forClass(H.class).instantiate(NonParameterizedType.forClass(String.class));
    Type strHCompType = GenericClassType.forClass(Comparable.class).instantiate(strHType);
    Type strGType =
        GenericClassType.forClass(G.class).instantiate(NonParameterizedType.forClass(String.class));
    assertTrue("H<String> assignable to G<String>", strGType.isAssignableFrom(strHType));
    assertTrue(
        "H<String> assignable to Comparable<H<String>>", strHCompType.isAssignableFrom(strHType));

    // class D<S,T> extends A<T> {}
    Type strIntDType =
        GenericClassType.forClass(D.class)
            .instantiate(
                NonParameterizedType.forClass(String.class),
                NonParameterizedType.forClass(Integer.class));
    assertTrue(
        "D<String,Integer> assignable to A<Integer>", intAType.isAssignableFrom(strIntDType));
    assertFalse(
        "D<String,Integer> not assignable to A<String>", strAType.isAssignableFrom(strIntDType));

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
    assertTrue(
        "F<Integer,String> assignable to E<String,Integer>",
        strIntEType.isAssignableFrom(intStrFType));
    Type strIntFType =
        GenericClassType.forClass(F.class)
            .instantiate(
                NonParameterizedType.forClass(String.class),
                NonParameterizedType.forClass(Integer.class));
    assertFalse(
        "F<String,Integer> not assignable to E<String,Integer>",
        strIntEType.isAssignableFrom(strIntFType));
  }

  @Test
  public void testNames() {
    Type strALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(NonParameterizedType.forClass(String.class));
    assertEquals(
        "parameterized type name ", "java.util.ArrayList<java.lang.String>", strALType.getName());
  }

  @Test
  public void testInnerClass() {
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();

    // GenericWithInnerClass.StaticInnerClass stc;
    ClassOrInterfaceType staticInnerType =
        (ClassOrInterfaceType) Type.forClass(GenericWithInnerClass.StaticInnerClass.class);
    assertTrue("is reference type", staticInnerType.isReferenceType());
    assertFalse("is not parameterized", staticInnerType.isParameterized());
    assertFalse("is not generic", staticInnerType.isGeneric());
    assertFalse("is not primitive", staticInnerType.isPrimitive());
    assertFalse("is not rawtype", staticInnerType.isRawtype());
    assertEquals(
        "name of static nested class has no type arguments",
        "randoop.types.ExampleClassesForTests.GenericWithInnerClass.StaticInnerClass",
        staticInnerType.getName());
    assertEquals(
        "static member of generic has no type parameters",
        (List<TypeVariable>) new ArrayList<TypeVariable>(),
        staticInnerType.getTypeParameters());
    assertEquals(
        "static member class of generic has no type arguments",
        (List<TypeArgument>) new ArrayList<TypeArgument>(),
        staticInnerType.getTypeArguments());

    // ClassWithGenericInnerClass.GenericNestedClass<Integer> gnc2;
    ClassOrInterfaceType genericNestedTypeOfClass =
        (ClassOrInterfaceType) Type.forClass(ClassWithGenericInnerClass.GenericNestedClass.class);
    assertTrue("is generic", genericNestedTypeOfClass.isGeneric());
    assertFalse("is not parameterized", genericNestedTypeOfClass.isParameterized());
    assertEquals(
        "name of generic inner class has type arguments",
        "randoop.types.ExampleClassesForTests.ClassWithGenericInnerClass.GenericNestedClass<T>",
        genericNestedTypeOfClass.getName());
    assertEquals(
        "generic member class has type parameters",
        1,
        genericNestedTypeOfClass.getTypeParameters().size());
    Substitution substitution =
        new Substitution(genericNestedTypeOfClass.getTypeParameters(), integerType);
    ClassOrInterfaceType instantiatedGenericNestedClass =
        genericNestedTypeOfClass.substitute(substitution);
    assertThat(
        "name of instantiated generic member class",
        instantiatedGenericNestedClass.getName(),
        is(
            equalTo(
                "randoop.types.ExampleClassesForTests.ClassWithGenericInnerClass.GenericNestedClass<java.lang.Integer>")));
    substitution =
        new Substitution(
            genericNestedTypeOfClass.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    ClassOrInterfaceType instantiatedGenericNestedClass2 =
        genericNestedTypeOfClass.substitute(substitution);
    assertTrue(
        "equality should be reflexive",
        instantiatedGenericNestedClass.equals(instantiatedGenericNestedClass));
    assertFalse(
        "different instantiations not equal",
        instantiatedGenericNestedClass.equals(instantiatedGenericNestedClass2));
    assertTrue(
        "instantiation should instantiate generic type",
        instantiatedGenericNestedClass.isInstantiationOf(genericNestedTypeOfClass));
    assertTrue(
        "instantiation should instantiate generic type",
        instantiatedGenericNestedClass2.isInstantiationOf(genericNestedTypeOfClass));
    assertFalse(
        "instantiation should not instantiate instantiation",
        instantiatedGenericNestedClass.isInstantiationOf(instantiatedGenericNestedClass2));

    // GenericWithInnerClass<Integer>.InnerClass ic;
    ClassOrInterfaceType innerType =
        (ClassOrInterfaceType) Type.forClass(GenericWithInnerClass.InnerClass.class);
    assertFalse("is parameterized", innerType.isParameterized());
    assertTrue("is generic", innerType.isGeneric());
    assertEquals(
        "name of inner class of generic should have type arguments",
        "randoop.types.ExampleClassesForTests.GenericWithInnerClass<T>.InnerClass",
        innerType.getName());
    System.out.printf(
        "innerType=%s, type parameters=%s%n", innerType, innerType.getTypeParameters());
    assertEquals("member of generic type parameters", 1, innerType.getTypeParameters().size());
    substitution = new Substitution(innerType.getTypeParameters(), integerType);
    ClassOrInterfaceType instantiatedInnerType = innerType.substitute(substitution);
    assertEquals(
        "name of instantiated member class",
        "randoop.types.ExampleClassesForTests.GenericWithInnerClass<java.lang.Integer>.InnerClass",
        instantiatedInnerType.getName());
    substitution =
        new Substitution(innerType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    ClassOrInterfaceType instantiatedInnerType2 = innerType.substitute(substitution);
    assertTrue("equality should be reflexive", instantiatedInnerType.equals(instantiatedInnerType));
    assertTrue(
        "instantiation should instantiate generic type",
        instantiatedInnerType.isInstantiationOf(innerType));
    assertTrue(
        "instantiation should instantiate generic type",
        instantiatedInnerType2.isInstantiationOf(innerType));

    // GenericWithInnerClass<String>.GenericNestedClass<Integer> gnc;
    ClassOrInterfaceType genericNestedType =
        (ClassOrInterfaceType) Type.forClass(GenericWithInnerClass.GenericNestedClass.class);
    assertFalse("is not parameterized", genericNestedType.isParameterized());
    assertTrue("is generic", genericNestedType.isGeneric());
    /*
    assertEquals("name of generic class with inner class should have type parameters", "randoop.types.ExampleClassesForTests.GenericWithInnerClass<T>.GenericNestedClass<S>", genericNestedType.getName());
        */
    assertEquals(
        "generic member of generic class has type parameters",
        2,
        genericNestedType.getTypeParameters().size());
    substitution =
        new Substitution(genericNestedType.getTypeParameters(), JavaTypes.STRING_TYPE, integerType);
    ClassOrInterfaceType instantiatedGenericNestedType = genericNestedType.substitute(substitution);
    assertEquals(
        "unqual name",
        "GenericNestedClass<java.lang.Integer>",
        instantiatedGenericNestedType.getUnqualifiedName());
    assertEquals(
        "canonical name",
        "randoop.types.ExampleClassesForTests.GenericWithInnerClass.GenericNestedClass",
        instantiatedGenericNestedType.getCanonicalName());
    assertThat(
        "name of instantiated generic member of generic class",
        instantiatedGenericNestedType.getName(),
        is(
            equalTo(
                "randoop.types.ExampleClassesForTests.GenericWithInnerClass<java.lang.String>.GenericNestedClass<java.lang.Integer>")));
    substitution =
        new Substitution(genericNestedType.getTypeParameters(), integerType, JavaTypes.STRING_TYPE);
    ClassOrInterfaceType instantiatedGenericNestedType2 =
        genericNestedType.substitute(substitution);
    assertTrue(
        "equality should be reflexive",
        instantiatedGenericNestedType.equals(instantiatedGenericNestedType));
    assertFalse(
        "different instantiations not equal",
        instantiatedGenericNestedType.equals(instantiatedGenericNestedType2));
    assertTrue(
        "instantiation should instantiate generic type",
        instantiatedGenericNestedType.isInstantiationOf(genericNestedType));
    assertTrue(
        "instantiation should instantiate generic type",
        instantiatedGenericNestedType2.isInstantiationOf(genericNestedType));

    ClassOrInterfaceType nonparamInnerClass =
        ClassOrInterfaceType.forClass(ClassWithInnerClass.InnerClass.class);
    ClassOrInterfaceType otherNonparamInnerClass =
        ClassOrInterfaceType.forClass(ClassWithInnerClass.OtherInnerClass.class);
    assertFalse("not parameterized", nonparamInnerClass.isParameterized());
    assertFalse("not generic", nonparamInnerClass.isGeneric());
    assertEquals(
        "should not have type parameters", 0, nonparamInnerClass.getTypeParameters().size());
    assertEquals(
        "unqualified name",
        "ExampleClassesForTests.ClassWithInnerClass.InnerClass",
        nonparamInnerClass.getUnqualifiedName());
    assertEquals(
        "canonical name",
        "randoop.types.ExampleClassesForTests.ClassWithInnerClass.InnerClass",
        nonparamInnerClass.getCanonicalName());
    assertTrue("equality should be reflexive", nonparamInnerClass.equals(nonparamInnerClass));
    assertFalse(
        "different member classes not equal", nonparamInnerClass.equals(otherNonparamInnerClass));
    assertFalse(
        "different member classes don't instantiate",
        nonparamInnerClass.isInstantiationOf(otherNonparamInnerClass));
    assertTrue(
        "identical member classes instantiate",
        nonparamInnerClass.isInstantiationOf(nonparamInnerClass));
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
              assertEquals(
                  "capture variable should have same parameter as capture type",
                  capType.getTypeParameters(),
                  variable.getTypeParameters());
            } else if (variable instanceof ExplicitTypeVariable) {
              assertTrue(
                  "explicit variable should occur in wildcard type",
                  itType.getTypeParameters().contains(variable));
            } else {
              fail("capture type should only have either capture or explicit variable");
            }
          }
        }
      }
    }
  }
}
