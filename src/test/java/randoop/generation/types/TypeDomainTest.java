package randoop.generation.types;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;

import randoop.types.JavaTypes;
import randoop.types.ReferenceType;

import static org.junit.Assert.assertTrue;

/**
 * Created by bjkeller on 11/18/16.
 */
public class TypeDomainTest {

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
  }
}
