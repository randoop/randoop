package randoop.generation.types;

import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import randoop.types.JavaTypes;
import randoop.types.ReferenceType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SetDomainTest {
  @Test
  public void setDomainTest() {
    Set<ReferenceType> set = new HashSet<>();
    set.add(JavaTypes.STRING_TYPE);
    set.add(JavaTypes.INT_TYPE.toBoxedPrimitive());
    set.add(JavaTypes.DOUBLE_TYPE.toBoxedPrimitive());

    TypeDomain setDomain = SetDomain.createDomain(set);

    assertFalse("should have elements", setDomain.isEmpty());
    assertThat("should have three elements", ((SetDomain) setDomain).size(), is(equalTo(3)));

    assertEquals("set should be same as init set", set, ((SetDomain) setDomain).getTypes());

    TypeDomain restrictedDomain = setDomain.restrictDown(JavaTypes.OBJECT_TYPE);
    assertThat("should be unchanged", restrictedDomain, is(equalTo(setDomain)));

    restrictedDomain = setDomain.restrictUp(JavaTypes.NULL_TYPE);
    assertThat("should be unchanged", restrictedDomain, is(equalTo(setDomain)));

    TypeDomain empty = EmptyDomain.createDomain();

    restrictedDomain = setDomain.restrictDown(JavaTypes.NULL_TYPE);
    assertThat("should be empty domain", restrictedDomain, is(equalTo(empty)));
    restrictedDomain = setDomain.restrictUp(JavaTypes.OBJECT_TYPE);
    assertThat("should be empty domain", restrictedDomain, is(equalTo(empty)));

    ReferenceType boundType = ReferenceType.forClass(Number.class);
    restrictedDomain = setDomain.restrictDown(boundType);
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should have two elements", ((SetDomain) restrictedDomain).size(), is(equalTo(2)));
    Set<ReferenceType> types = ((SetDomain) restrictedDomain).getTypes();
    for (ReferenceType type : types) {
      assertTrue("should be subtype of Number " + type, type.isSubtypeOf(boundType));
    }

    boundType = JavaTypes.STRING_TYPE;
    restrictedDomain = setDomain.restrictUp(boundType);
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should have one element", ((SetDomain) restrictedDomain).size(), is(equalTo(1)));
    types = ((SetDomain) restrictedDomain).getTypes();
    for (ReferenceType type : types) {
      assertTrue("String should be subtype of " + type, boundType.isSubtypeOf(type));
    }

    restrictedDomain = setDomain.restrictDown(empty);
    assertTrue("should be empty", restrictedDomain.isEmpty());
    restrictedDomain = setDomain.restrictUp(empty);
    assertTrue("should be empty", restrictedDomain.isEmpty());

    // TODO check restriction by different types of domains

    Iterator<ReferenceType> it = setDomain.iterator();
    assertTrue("should have next", it.hasNext());
    // TODO check iterator completely

    //fail("incomplete");
  }

  @Test
  public void restrictBySetDomain() {
    ReferenceType a1000 = ReferenceType.forClass(A1000.class);
    ReferenceType a0100 = ReferenceType.forClass(A0100.class);
    ReferenceType a0010 = ReferenceType.forClass(A0010.class);
    ReferenceType a0001 = ReferenceType.forClass(A0001.class);
    ReferenceType a1100 = ReferenceType.forClass(A1100.class);
    ReferenceType a0110 = ReferenceType.forClass(A0110.class);
    ReferenceType a0011 = ReferenceType.forClass(A0011.class);
    ReferenceType a1110 = ReferenceType.forClass(A1110.class);
    ReferenceType a1101 = ReferenceType.forClass(A1101.class);
    ReferenceType a0111 = ReferenceType.forClass(A0111.class);

    TypeDomain restrictedDomain;
    TypeDomain expected;

    TypeDomain domain1 = SetDomain.createDomain(DomainTestUtilities.makeSet(a1000, a0100, a0010));
    TypeDomain domain2 = SetDomain.createDomain(DomainTestUtilities.makeSet(a1100, a0110));
    TypeDomain domain3 = SetDomain.createDomain(DomainTestUtilities.makeSet(a0110));

    restrictedDomain = domain1.restrictDown(domain2);
    assertTrue("empty", restrictedDomain.isEmpty());
    restrictedDomain = domain2.restrictDown(domain1);
    expected = domain2;
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should be " + expected, restrictedDomain, is(equalTo(expected)));

    restrictedDomain = domain1.restrictUp(domain2);
    expected = domain1;
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should be " + expected, restrictedDomain, is(equalTo(expected)));
    restrictedDomain = domain1.restrictDown(domain2);
    assertTrue("empty", restrictedDomain.isEmpty());

    restrictedDomain = domain1.restrictUp(domain3);
    expected = SetDomain.createDomain(DomainTestUtilities.makeSet(a0100, a0010));
    assertFalse("not empty", restrictedDomain.isEmpty());
    assertThat("should be " + expected, restrictedDomain, is(equalTo(expected)));
  }
}
