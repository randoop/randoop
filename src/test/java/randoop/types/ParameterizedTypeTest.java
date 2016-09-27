package randoop.types;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import randoop.test.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParameterizedTypeTest {

  @Test
  public void testAssignability() {
    Type strALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(new NonParameterizedType(String.class));
    Type intALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(new NonParameterizedType(Integer.class));
    Type objALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(new NonParameterizedType(Object.class));
    Type rawALType = new NonParameterizedType(ArrayList.class);

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

    Type intType = new NonParameterizedType(Integer.class);
    Type intCompType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(new NonParameterizedType(Integer.class));
    assertTrue("Integer assignable to Comparable<Integer>", intCompType.isAssignableFrom(intType));
    assertFalse(
        "Comparable<Integer> not assignable to Integer", intType.isAssignableFrom(intCompType));

    Type strCompType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(new NonParameterizedType(String.class));
    assertTrue(
        "Comparable<Integer> assignable to Comparable<Integer>",
        intCompType.isAssignableFrom(intCompType));
    assertFalse(
        "Comparable<Integer> is not assignable from Comparable<String>",
        intCompType.isAssignableFrom(strCompType));

    Type intArrayType = ArrayType.ofElementType(new NonParameterizedType(Integer.class));
    assertFalse(
        "Comparable<Integer> not assignable from Integer[]",
        intCompType.isAssignableFrom(intArrayType));

    // class A<T> implements Comparable<T> {}
    Type intAType =
        GenericClassType.forClass(A.class).instantiate(new NonParameterizedType(Integer.class));
    assertTrue(
        "A<Integer> assignable to Comparable<Integer>", intCompType.isAssignableFrom(intAType));

    // class B extends A<String> {}
    Type strAType =
        GenericClassType.forClass(A.class).instantiate(new NonParameterizedType(String.class));
    Type bType = new NonParameterizedType(B.class);
    assertTrue("B assignable to A<String>", strAType.isAssignableFrom(bType));
    assertTrue("B assignable to Comparable<String>", strCompType.isAssignableFrom(bType));

    // class C extends A<Integer> {}
    Type cType = new NonParameterizedType(C.class);
    assertFalse("C not assignable to A<String>", strAType.isAssignableFrom(cType));
    assertFalse("C not assignable to Comparable<String>", strCompType.isAssignableFrom(cType));

    // class H<T> extends G<T> implements Comparable<T> {}
    Type strHType =
        GenericClassType.forClass(H.class).instantiate(new NonParameterizedType(String.class));
    Type strGType =
        GenericClassType.forClass(G.class).instantiate(new NonParameterizedType(String.class));
    assertTrue("H<String> assignable to G<String>", strGType.isAssignableFrom(strHType));
    assertTrue(
        "H<String> assignable to Comparable<String>", strCompType.isAssignableFrom(strHType));

    // class D<S,T> extends A<T> {}
    Type strIntDType =
        GenericClassType.forClass(D.class)
            .instantiate(
                new NonParameterizedType(String.class), new NonParameterizedType(Integer.class));
    assertTrue(
        "D<String,Integer> assignable to A<Integer>", intAType.isAssignableFrom(strIntDType));
    assertFalse(
        "D<String,Integer> not assignable to A<String>", strAType.isAssignableFrom(strIntDType));

    // class E<S,T> {}
    Type strIntEType =
        GenericClassType.forClass(E.class)
            .instantiate(
                new NonParameterizedType(String.class), new NonParameterizedType(Integer.class));
    // class F<T,S> extends E<S,T> {}
    Type intStrFType =
        GenericClassType.forClass(F.class)
            .instantiate(
                new NonParameterizedType(Integer.class), new NonParameterizedType(String.class));
    assertTrue(
        "F<Integer,String> assignable to E<String,Integer>",
        strIntEType.isAssignableFrom(intStrFType));
    Type strIntFType =
        GenericClassType.forClass(F.class)
            .instantiate(
                new NonParameterizedType(String.class), new NonParameterizedType(Integer.class));
    assertFalse(
        "F<String,Integer> not assignable to E<String,Integer>",
        strIntEType.isAssignableFrom(strIntFType));
  }

  @Test
  public void testNames() {
    Type strALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(new NonParameterizedType(String.class));
    assertEquals(
        "parameterized type name ", "java.util.ArrayList<java.lang.String>", strALType.getName());
  }

  @Test
  public void testInnerClass() {
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();

    //GenericWithInnerClass.StaticInnerClass stc;
    ClassOrInterfaceType staticInnerType =
        (ClassOrInterfaceType) Type.forClass(GenericWithInnerClass.StaticInnerClass.class);
    assertTrue("is reference type", staticInnerType.isReferenceType());
    assertFalse("is not parameterized", staticInnerType.isParameterized());
    assertFalse("is not generic", staticInnerType.isGeneric());
    assertFalse("is not primitive", staticInnerType.isPrimitive());
    assertFalse("is not rawtype", staticInnerType.isRawtype());
    assertThat(
        "name of static nested class has no type arguments",
        staticInnerType.getName(),
        is(equalTo("randoop.types.GenericWithInnerClass.StaticInnerClass")));
    assertThat(
        "static member of generic has no type parameters",
        staticInnerType.getTypeParameters(),
        is(equalTo((List<TypeVariable>) new ArrayList<TypeVariable>())));
    assertThat(
        "static member class of generic has no type arguments",
        staticInnerType.getTypeArguments(),
        is(equalTo((List<TypeArgument>) new ArrayList<TypeArgument>())));

    //ClassWithGenericInnerClass.GenericNestedClass<Integer> gnc2;
    ClassOrInterfaceType genericNestedTypeOfClass =
        (ClassOrInterfaceType) Type.forClass(ClassWithGenericInnerClass.GenericNestedClass.class);
    assertTrue("is generic", genericNestedTypeOfClass.isGeneric());
    assertFalse("is not parameterized", genericNestedTypeOfClass.isParameterized());
    assertThat(
        "name of generic inner class has type arguments",
        genericNestedTypeOfClass.getName(),
        is(equalTo("randoop.types.ClassWithGenericInnerClass.GenericNestedClass<T>")));
    assertThat(
        "generic member class has type parameters",
        genericNestedTypeOfClass.getTypeParameters().size(),
        is(equalTo(1)));
    Substitution<ReferenceType> substitution =
        Substitution.forArgs(genericNestedTypeOfClass.getTypeParameters(), integerType);
    ClassOrInterfaceType instantiatedGenericNestedClass =
        genericNestedTypeOfClass.apply(substitution);
    assertThat(
        "name of instantiated generic member class",
        instantiatedGenericNestedClass.getName(),
        is(
            equalTo(
                "randoop.types.ClassWithGenericInnerClass.GenericNestedClass<java.lang.Integer>")));
    substitution =
        Substitution.forArgs(
            genericNestedTypeOfClass.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    ClassOrInterfaceType instantiatedGenericNestedClass2 =
        genericNestedTypeOfClass.apply(substitution);
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

    //GenericWithInnerClass<Integer>.InnerClass ic;
    ClassOrInterfaceType innerType =
        (ClassOrInterfaceType) Type.forClass(GenericWithInnerClass.InnerClass.class);
    assertFalse("is parameterized", innerType.isParameterized());
    assertTrue("is generic", innerType.isGeneric());
    assertThat(
        "name of inner class of generic should have type arguments",
        innerType.getName(),
        is(equalTo("randoop.types.GenericWithInnerClass<T>.InnerClass")));
    assertThat(
        "member of generic has type parameters",
        innerType.getTypeParameters().size(),
        is(equalTo(1)));
    substitution = Substitution.forArgs(innerType.getTypeParameters(), integerType);
    ClassOrInterfaceType instantiatedInnerType = innerType.apply(substitution);
    assertThat(
        "name of instantiated member class",
        instantiatedInnerType.getName(),
        is(equalTo("randoop.types.GenericWithInnerClass<java.lang.Integer>.InnerClass")));
    substitution =
        Substitution.forArgs(innerType.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    ClassOrInterfaceType instantiatedInnerType2 = innerType.apply(substitution);
    assertTrue("equality should be reflexive", instantiatedInnerType.equals(instantiatedInnerType));
    assertFalse(
        "different instantiations not equal", instantiatedInnerType.equals(instantiatedInnerType2));
    assertTrue(
        "instantiation should instantiate generic type",
        instantiatedInnerType.isInstantiationOf(innerType));
    assertTrue(
        "instantiation should instantiate generic type",
        instantiatedInnerType2.isInstantiationOf(innerType));
    assertFalse(
        "instantiation should not instantiate instantiation",
        instantiatedInnerType.isInstantiationOf(instantiatedInnerType2));

    //GenericWithInnerClass<String>.GenericNestedClass<Integer> gnc;
    ClassOrInterfaceType genericNestedType =
        (ClassOrInterfaceType) Type.forClass(GenericWithInnerClass.GenericNestedClass.class);
    assertFalse("is not parameterized", genericNestedType.isParameterized());
    assertTrue("is generic", genericNestedType.isGeneric());
    /*
    assertThat(
        "name of generic class with inner class should have type parameters",
        genericNestedType.getName(),
        is(equalTo("randoop.types.GenericWithInnerClass<T>.GenericNestedClass<S>")));
        */
    assertThat(
        "generic member of generic class has type parameters",
        genericNestedType.getTypeParameters().size(),
        is(equalTo(2)));
    substitution =
        Substitution.forArgs(
            genericNestedType.getTypeParameters(), JavaTypes.STRING_TYPE, integerType);
    ClassOrInterfaceType instantiatedGenericNestedType = genericNestedType.apply(substitution);
    assertThat(
        "unqual name",
        instantiatedGenericNestedType.getUnqualifiedName(),
        is(equalTo("GenericNestedClass<java.lang.Integer>")));
    assertThat(
        "canonical name",
        instantiatedGenericNestedType.getCanonicalName(),
        is(equalTo("randoop.types.GenericWithInnerClass.GenericNestedClass")));
    assertThat(
        "name of instantiated generic member of generic class",
        instantiatedGenericNestedType.getName(),
        is(
            equalTo(
                "randoop.types.GenericWithInnerClass<java.lang.String>.GenericNestedClass<java.lang.Integer>")));
    substitution =
        Substitution.forArgs(
            genericNestedType.getTypeParameters(), integerType, JavaTypes.STRING_TYPE);
    ClassOrInterfaceType instantiatedGenericNestedType2 = genericNestedType.apply(substitution);
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
    assertFalse(
        "instantiation should not instantiate instantiation",
        instantiatedInnerType.isInstantiationOf(instantiatedInnerType2));

    ClassOrInterfaceType nonparamInnerClass =
        ClassOrInterfaceType.forClass(ClassWithInnerClass.InnerClass.class);
    ClassOrInterfaceType otherNonparamInnerClass =
        ClassOrInterfaceType.forClass(ClassWithInnerClass.OtherInnerClass.class);
    assertFalse("not parameterized", nonparamInnerClass.isParameterized());
    assertFalse("not generic", nonparamInnerClass.isGeneric());
    assertThat(
        "should not have type parameters",
        nonparamInnerClass.getTypeParameters().size(),
        is(equalTo(0)));
    assertThat(
        "unqualified name",
        nonparamInnerClass.getUnqualifiedName(),
        is(equalTo("ClassWithInnerClass.InnerClass")));
    assertThat(
        "canonical name",
        nonparamInnerClass.getCanonicalName(),
        is(equalTo("randoop.types.ClassWithInnerClass.InnerClass")));
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
}
