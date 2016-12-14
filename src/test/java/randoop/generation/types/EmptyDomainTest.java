package randoop.generation.types;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import randoop.types.JavaTypes;
import randoop.types.ReferenceType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmptyDomainTest {

  @Test
  public void emptyDomainTest() {
    TypeDomain emptySetDomain = SetDomain.createDomain(new HashSet<ReferenceType>());
    assertTrue("the empty domain should be empty ", emptySetDomain.isEmpty());

    TypeDomain emptyIntervalDomain =
        IntervalDomain.createDomain(JavaTypes.OBJECT_TYPE, JavaTypes.NULL_TYPE);
    assertTrue("the empty domain should be empty ", emptyIntervalDomain.isEmpty());

    TypeDomain emptyDSDomain = DownSumDomain.createDomain(new HashMap<ReferenceType, TypeDomain>());
    assertTrue("the empty domain should be empty ", emptyDSDomain.isEmpty());

    TypeDomain emptyUSDomain = UpSumDomain.createDomain(new HashMap<ReferenceType, TypeDomain>());
    assertTrue("the empty domain should be empty ", emptyUSDomain.isEmpty());

    EmptyDomain emptyDomain = EmptyDomain.createDomain();
    assertTrue("the empty domain should be empty ", emptyDomain.isEmpty());
    assertTrue(
        "created empty set should be canonical emptydomain", emptyDomain.equals(emptySetDomain));
    assertTrue(
        "created empty set should be canonical emptydomain",
        emptyDomain.equals(emptyIntervalDomain));
    assertTrue(
        "created empty set should be canonical emptydomain", emptyDomain.equals(emptyDSDomain));
    assertTrue(
        "created empty set should be canonical emptydomain", emptyDomain.equals(emptyUSDomain));

    TypeDomain restrictedEmptyDomain;
    restrictedEmptyDomain = emptyDomain.restrictDown(emptyDomain);
    assertTrue("the empty domain should be empty ", restrictedEmptyDomain.isEmpty());

    restrictedEmptyDomain = emptyDomain.restrictUp(emptyDomain);
    assertTrue("the empty domain should be empty ", restrictedEmptyDomain.isEmpty());

    TypeDomain trivialDomain =
        IntervalDomain.createDomain(JavaTypes.NULL_TYPE, JavaTypes.OBJECT_TYPE);
    restrictedEmptyDomain = emptyDomain.restrictDown(trivialDomain);
    assertTrue("the empty domain should be empty ", restrictedEmptyDomain.isEmpty());
    restrictedEmptyDomain = emptyDomain.restrictUp(trivialDomain);
    assertTrue("the empty domain should be empty ", restrictedEmptyDomain.isEmpty());

    Iterator<ReferenceType> it = emptyDomain.iterator();
    assertFalse("iterator should not have next", it.hasNext());
  }
}
