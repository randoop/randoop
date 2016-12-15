package randoop.generation.types;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import randoop.types.JavaTypes;
import randoop.types.ReferenceType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by bjkeller on 12/14/16.
 */
public class DownSumDomainTest {

  @Test
  public void domainCreationTest() {
    Map<ReferenceType, TypeDomain> emptyMap = new HashMap<>();
    TypeDomain empty = DownSumDomain.createDomain(emptyMap);
    assertTrue("empty", empty.isEmpty());

    Map<ReferenceType, TypeDomain> sumMap = new LinkedHashMap<>();
    TypeDomain trivial = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, JavaTypes.OBJECT_TYPE);
    sumMap.put(JavaTypes.OBJECT_TYPE, trivial);
    TypeDomain downDomain = DownSumDomain.createDomain(sumMap);
    assertFalse("not empty", downDomain.isEmpty());
    assertTrue("should be simplified", downDomain instanceof IntervalDomain);

    ReferenceType numberType = ReferenceType.forClass(Number.class);
    sumMap = new LinkedHashMap<>();
    sumMap.put(numberType, EmptyDomain.createDomain());
    sumMap.put(JavaTypes.STRING_TYPE, EmptyDomain.createDomain());
    downDomain = DownSumDomain.createDomain(sumMap);
    assertTrue("should be empty", downDomain.isEmpty());

    sumMap = new LinkedHashMap<>();
    sumMap.put(numberType, trivial.restrictDown(numberType));
    sumMap.put(JavaTypes.STRING_TYPE, trivial.restrictDown(JavaTypes.STRING_TYPE));
    downDomain = DownSumDomain.createDomain(sumMap);
    assertFalse("not empty", downDomain.isEmpty());
    assertTrue("should be actual DownSumDomain", downDomain instanceof DownSumDomain);
  }

  @Test
  public void restrictByTypeTest() {

    ReferenceType a1000 = ReferenceType.forClass(A1000.class);
    ReferenceType a0100 = ReferenceType.forClass(A0100.class);
    ReferenceType a0010 = ReferenceType.forClass(A0010.class);
    ReferenceType a0001 = ReferenceType.forClass(A0001.class);
    ReferenceType a1100 = ReferenceType.forClass(A1100.class);
    ReferenceType a1001 = ReferenceType.forClass(A1001.class);
    ReferenceType a0101 = ReferenceType.forClass(A0101.class);
    ReferenceType a0011 = ReferenceType.forClass(A0011.class);
    ReferenceType a1110 = ReferenceType.forClass(A1110.class);
    ReferenceType a1101 = ReferenceType.forClass(A1101.class);
    ReferenceType a0111 = ReferenceType.forClass(A0111.class);
    TypeDomain trivial = IntervalDomain.createDomain(JavaTypes.NULL_TYPE, JavaTypes.OBJECT_TYPE);

    TypeDomain restrictedDomain;

    Map<ReferenceType, TypeDomain> sumMap = new LinkedHashMap<>();
    sumMap.put(a1000, trivial.restrictDown(a1000));
    sumMap.put(a0100, trivial.restrictDown(a0100));
    sumMap.put(a0010, trivial.restrictDown(a0010));
    TypeDomain downDomain = DownSumDomain.createDomain(sumMap);

    restrictedDomain = downDomain.restrictDown(a0001);
    assertFalse("should not be empty", restrictedDomain.isEmpty());
    assertTrue("should have upper bound", restrictedDomain.hasSupertypeOf(a1001));
    assertTrue("should have upper bound", restrictedDomain.hasSupertypeOf(a0101));
    assertTrue("should have upper bound", restrictedDomain.hasSupertypeOf(a0011));
    assertFalse("should not have upper bound", restrictedDomain.hasSupertypeOf(a0001));

    assertFalse("should not have lower bound", restrictedDomain.hasSubtypeOf(a1100));
    assertTrue("should have lower bound", restrictedDomain.hasSubtypeOf(a1001));

    restrictedDomain = downDomain.restrictUp(a0001);
    assertTrue("should be empty", restrictedDomain.isEmpty());
  }
}
