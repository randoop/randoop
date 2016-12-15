package randoop.generation.types;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import randoop.types.JavaTypes;
import randoop.types.ReferenceType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UpSumDomainTest {
  @Test
  public void domainCreationTest() {
    Map<ReferenceType, TypeDomain> sumMap = new LinkedHashMap<>();
    TypeDomain empty = UpSumDomain.createDomain(sumMap);
    assertTrue("should be empty", empty.isEmpty());

    sumMap = new LinkedHashMap<>();
    sumMap.put(JavaTypes.STRING_TYPE, EmptyDomain.createDomain());
    sumMap.put(JavaTypes.INT_TYPE.toBoxedPrimitive(), EmptyDomain.createDomain());
    TypeDomain upDomain = UpSumDomain.createDomain(sumMap);
    assertTrue("should be empty", upDomain.isEmpty());

    TypeDomain trivial = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, JavaTypes.OBJECT_TYPE);
    sumMap = new LinkedHashMap<>();
    sumMap.put(JavaTypes.NULL_TYPE, trivial);
    upDomain = UpSumDomain.createDomain(sumMap);
    assertTrue("should be simplified to an interval domain", upDomain instanceof IntervalDomain);
    assertFalse("should not be empty", upDomain.isEmpty());

    sumMap = new LinkedHashMap<>();
    sumMap.put(JavaTypes.STRING_TYPE, trivial.restrictUp(JavaTypes.STRING_TYPE));
    ReferenceType integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    sumMap.put(integerType, trivial.restrictUp(integerType));
    upDomain = UpSumDomain.createDomain(sumMap);
    assertFalse("should be nonempty", upDomain.isEmpty());
    assertTrue("should be UpSumDomain", upDomain instanceof UpSumDomain);
  }

  @Test
  public void restrictByTypeTest() {
    ReferenceType a1000 = ReferenceType.forClass(A1000.class);
    ReferenceType a0100 = ReferenceType.forClass(A0100.class);
    ReferenceType a0010 = ReferenceType.forClass(A0010.class);
    ReferenceType a0001 = ReferenceType.forClass(A0001.class);
    ReferenceType a1100 = ReferenceType.forClass(A1100.class);
    ReferenceType a1010 = ReferenceType.forClass(A1010.class);
    ReferenceType a1001 = ReferenceType.forClass(A1001.class);
    ReferenceType a0101 = ReferenceType.forClass(A0101.class);
    ReferenceType a0110 = ReferenceType.forClass(A0110.class);
    ReferenceType a0011 = ReferenceType.forClass(A0011.class);
    ReferenceType a1110 = ReferenceType.forClass(A1110.class);
    ReferenceType a1101 = ReferenceType.forClass(A1101.class);
    ReferenceType a1011 = ReferenceType.forClass(A1011.class);
    ReferenceType a0111 = ReferenceType.forClass(A0111.class);
    TypeDomain trivial = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, JavaTypes.OBJECT_TYPE);

    TypeDomain restrictedDomain;

    Map<ReferenceType, TypeDomain> sumMap = new LinkedHashMap<>();
    sumMap.put(a1110, trivial.restrictUp(a1110));
    sumMap.put(a1101, trivial.restrictUp(a1011));
    TypeDomain upDomain = UpSumDomain.createDomain(sumMap);

    restrictedDomain = upDomain.restrictUp(a0111);
    System.out.println(restrictedDomain);
    assertFalse("should not be empty", restrictedDomain.isEmpty());

    assertTrue("should have supertype", restrictedDomain.hasSupertypeOf(a1000));
    assertTrue("should have supertype", restrictedDomain.hasSupertypeOf(a1001));
    assertTrue("should have supertype", restrictedDomain.hasSupertypeOf(a1100));
    assertTrue("should have supertype", restrictedDomain.hasSupertypeOf(a1010));
    assertTrue("should have supertype", restrictedDomain.hasSupertypeOf(a1001));
    assertTrue("should have supertype", restrictedDomain.hasSupertypeOf(a0110));
    assertTrue("should have supertype", restrictedDomain.hasSupertypeOf(a0101));
    assertTrue("should have supertype", restrictedDomain.hasSupertypeOf(a0011));
    assertFalse("should not have subtype", restrictedDomain.hasSubtypeOf(a1100));
    assertFalse("should not have subtype", restrictedDomain.hasSubtypeOf(a0101));
    assertFalse("should not have subtype", restrictedDomain.hasSubtypeOf(a1001));
    assertFalse("should not have subtype", restrictedDomain.hasSubtypeOf(a0001));

    //    fail("incomplete");
  }
}
