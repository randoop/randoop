package randoop.generation.types;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.ReferenceType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class IntervalDomainTest {
  @Test
  public void restrictDownByTypeTest() {
    NonParameterizedType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType numberType = ReferenceType.forClass(Number.class);
    TypeDomain trivial = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, JavaTypes.OBJECT_TYPE);
    TypeDomain intDownDomain = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, integerType);

    assertFalse("is not empty", trivial.isEmpty());
    assertFalse("is not empty", intDownDomain.isEmpty());

    // [Nulltype, Object] down Integer
    TypeDomain restrictedDomain = trivial.restrictDown(integerType);
    assertThat(
        "trivial domain restricted by Integer is [Null,Integer]",
        restrictedDomain,
        is(equalTo(intDownDomain)));

    // [Nulltype, Integer] down Object
    restrictedDomain = intDownDomain.restrictDown(JavaTypes.OBJECT_TYPE);
    assertThat("no restriction", restrictedDomain, is(equalTo(intDownDomain)));

    Set<ReferenceType> bounds = new HashSet<>();
    bounds.add(JavaTypes.STRING_TYPE);
    bounds.add(integerType);
    TypeDomain intStringDownDomain = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, bounds);

    // [Nulltype, Integer] down String
    restrictedDomain = intDownDomain.restrictDown(JavaTypes.STRING_TYPE);
    assertFalse("should not be empty", restrictedDomain.isEmpty());
    assertThat("is intersection domain", restrictedDomain, is(equalTo(intStringDownDomain)));

    ReferenceType integerComparableType = DomainTestUtilities.makeComparableType(integerType);
    bounds = new HashSet<>();
    bounds.add(integerComparableType);
    bounds.add(numberType);
    TypeDomain intCompDownDomain = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, bounds);

    // [Nulltype, Integer] down Comparable<Integer>
    restrictedDomain = intDownDomain.restrictDown(integerComparableType);
    assertFalse("should not be empty", restrictedDomain.isEmpty());
    assertThat("should have no restriction", restrictedDomain, is(equalTo(intDownDomain)));
    TypeDomain numberDownDomain = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, numberType);

    // [Nulltype, Number] down Comparable<Integer>
    restrictedDomain = numberDownDomain.restrictDown(integerComparableType);
    assertFalse("should not be empty", restrictedDomain.isEmpty());
    assertThat("restricted number domain", restrictedDomain, is(equalTo(intCompDownDomain)));

    TypeDomain intToNumberDomain = IntervalDomain.createDomain(integerType, numberType);

    // [Integer, Number] down String
    restrictedDomain = intToNumberDomain.restrictDown(JavaTypes.STRING_TYPE);
    assertTrue("should be empty", restrictedDomain.isEmpty());

    //fail("incomplete");
  }

  @Test
  public void restrictUpByTypeTest() {
    NonParameterizedType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType numberType = ReferenceType.forClass(Number.class);
    TypeDomain trivial = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, JavaTypes.OBJECT_TYPE);
    TypeDomain intUpDomain =
        IntervalDomain.createDomain(JavaTypes.INT_TYPE.toBoxedPrimitive(), JavaTypes.OBJECT_TYPE);
    assertFalse("is not empty", intUpDomain.isEmpty());

    TypeDomain restrictedDomain;

    Set<ReferenceType> bounds = new HashSet<>();
    bounds.add(JavaTypes.STRING_TYPE);
    bounds.add(integerType);

    // [Nulltype, Object] up Integer
    restrictedDomain = trivial.restrictUp(integerType);
    assertThat(
        "trivial domain restricted by Integer is [Integer,Object]",
        restrictedDomain,
        is(equalTo(intUpDomain)));

    // [Integer, Object] up Nulltype
    restrictedDomain = intUpDomain.restrictUp(JavaTypes.NULL_TYPE);
    assertThat("no restriction", restrictedDomain, is(equalTo(intUpDomain)));

    TypeDomain intStringUpDomain = IntervalDomain.createDomain(bounds, JavaTypes.OBJECT_TYPE);

    // [Integer, Object] up String
    restrictedDomain = intUpDomain.restrictUp(JavaTypes.STRING_TYPE);
    assertFalse("should be empty", restrictedDomain.isEmpty());
    assertThat("is intersection domain", restrictedDomain, is(equalTo(intStringUpDomain)));

    ReferenceType integerComparableType = DomainTestUtilities.makeComparableType(integerType);
    bounds = new HashSet<>();
    bounds.add(integerComparableType);
    bounds.add(numberType);
    TypeDomain numberUpDomain = IntervalDomain.createDomain(numberType, JavaTypes.OBJECT_TYPE);
    TypeDomain intCompUpDomain = IntervalDomain.createDomain(bounds, JavaTypes.OBJECT_TYPE);

    // [Integer, Object] up Comparable<Integer>
    restrictedDomain = numberUpDomain.restrictUp(integerComparableType);
    assertFalse("should not be empty", restrictedDomain.isEmpty());
    assertThat("restricted number domain", restrictedDomain, is(equalTo(intCompUpDomain)));

    TypeDomain intToNumberDomain = IntervalDomain.createDomain(integerType, numberType);

    // [Integer, Number] up String
    restrictedDomain = intToNumberDomain.restrictUp(JavaTypes.STRING_TYPE);
    assertTrue("should be empty", restrictedDomain.isEmpty());

    //fail("incomplete");
  }

  @Test
  public void restrictByInterval() {
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    ReferenceType numberType = ReferenceType.forClass(Number.class);
    ReferenceType comparableIntegerType = DomainTestUtilities.makeComparableType(integerType);

    TypeDomain integerDown = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, integerType);
    TypeDomain numberDown = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, numberType);
    TypeDomain intToCompInt = IntervalDomain.createDomain(integerType, comparableIntegerType);

    TypeDomain restrictedDomain;
    TypeDomain expected;

    // [Null, Number] down [Integer, Comparable<Integer>]
    restrictedDomain = numberDown.restrictDown(intToCompInt);
    expected =
        IntervalDomain.createDomain(
            integerType, DomainTestUtilities.makeSet(numberType, comparableIntegerType));
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should be " + expected, restrictedDomain, is(equalTo(expected)));
    restrictedDomain = restrictedDomain.restrictDown(integerDown);
    expected = IntervalDomain.createDomain(integerType, integerType);
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should be " + expected, restrictedDomain, is(equalTo(expected)));
  }

  @Test
  public void restrictInHierarchy() {
    ReferenceType a1000 = ReferenceType.forClass(A1000.class);
    ReferenceType a0100 = ReferenceType.forClass(A0100.class);
    ReferenceType a1100 = ReferenceType.forClass(A1100.class);
    ReferenceType a1110 = ReferenceType.forClass(A1110.class);
    ReferenceType a1101 = ReferenceType.forClass(A1101.class);
    ReferenceType a0111 = ReferenceType.forClass(A0111.class);

    TypeDomain restrictedDomain;
    TypeDomain expected;

    TypeDomain a1110ToA1000 = IntervalDomain.createDomain(a1110, a1000);
    TypeDomain a1101ToA0100 = IntervalDomain.createDomain(a1101, a0100);

    restrictedDomain = a1110ToA1000.restrictDown(a1101ToA0100);
    expected =
        IntervalDomain.createDomain(
            DomainTestUtilities.makeSet(a1110, a1101), DomainTestUtilities.makeSet(a1000, a0100));
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should be " + expected, restrictedDomain, is(equalTo(expected)));
    expected = restrictedDomain;
    restrictedDomain = a1110ToA1000.restrictUp(a1101ToA0100);
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should be " + expected, restrictedDomain, is(equalTo(expected)));
    restrictedDomain = a1101ToA0100.restrictUp(a1110ToA1000);
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should be " + expected, restrictedDomain, is(equalTo(expected)));

    TypeDomain a1000Down = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, a1000);
    TypeDomain a0111Up = IntervalDomain.createDomain(a0111, JavaTypes.OBJECT_TYPE);
    restrictedDomain = a1000Down.restrictDown(a0111Up);
    assertTrue("should be empty", restrictedDomain.isEmpty());
    restrictedDomain = a1000Down.restrictUp(a0111Up);
    assertTrue("should be empty", restrictedDomain.isEmpty());

    TypeDomain a1100Up = IntervalDomain.createDomain(a1100, JavaTypes.OBJECT_TYPE);
    TypeDomain a1110Up = IntervalDomain.createDomain(a1110, JavaTypes.OBJECT_TYPE);
    restrictedDomain = a1100Up.restrictDown(a1110Up);
    expected = a1100Up;
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should be " + expected, restrictedDomain, is(equalTo(expected)));
  }
}
