package randoop.generation.types;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import randoop.types.GenericClassType;
import randoop.types.JavaTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class TypeSetTest {

  @Test
  public void emptySetTest() {
    TypeSet s1 = new TypeSet();
    TypeSet s2 = new TypeSet(new HashSet<Type>());

    assertThat("empty sets should be equal", s1, is(equalTo(s2)));
    assertThat("empty set should have zero size", s1.size(), is(equalTo(0)));
    assertTrue(
        "empty set should not have match for any type", s1.match(JavaTypes.OBJECT_TYPE).isEmpty());
  }

  @Test
  public void objectSetTest() {
    TypeSet empty = new TypeSet();
    TypeSet s1 = new TypeSet();
    s1.add(JavaTypes.OBJECT_TYPE);
    Set<Type> inputTypes = new HashSet<>();
    inputTypes.add(JavaTypes.OBJECT_TYPE);
    TypeSet s2 = new TypeSet(inputTypes);
    assertThat("object set should not be equal to empty set", s1, is(not(equalTo(empty))));
    assertThat(
        "object and empty set should have different hashcodes",
        s1.hashCode(),
        is(not(equalTo(empty.hashCode()))));
    assertThat("object sets should be equal", s1, is(equalTo(s2)));
    assertThat("object set should have size 1", s1.size(), is(equalTo(1)));
    assertTrue(
        "object set should have match for object",
        s1.match(JavaTypes.OBJECT_TYPE).contains(JavaTypes.OBJECT_TYPE)
            && s1.match(JavaTypes.OBJECT_TYPE).size() == 1);
    assertTrue("object set should contain object", s1.contains(JavaTypes.OBJECT_TYPE));
  }

  @Test
  public void integerSetTest() {
    Type integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    Substitution<ReferenceType> substitution =
        Substitution.forArgs(
            JavaTypes.COMPARABLE_TYPE.getTypeParameters(), (ReferenceType) integerType);
    Type integerComparableType = JavaTypes.COMPARABLE_TYPE.apply(substitution);
    TypeSet s = new TypeSet();
    s.add(integerType);
    List<Type> expected = new ArrayList<>();
    expected.add(integerType);
    expected.add(Type.forClass(Number.class));
    expected.add(JavaTypes.OBJECT_TYPE);
    expected.add(JavaTypes.SERIALIZABLE_TYPE);
    expected.add(integerComparableType);
    checkSetContents(s, expected);

    List<Type> expectedMatches = new ArrayList<>();
    expectedMatches.add(integerComparableType);
    checkMatches(s, JavaTypes.COMPARABLE_TYPE, expectedMatches);
  }

  @Test
  public void integerStringSetTest() {
    Type integerType = JavaTypes.INT_TYPE.toBoxedPrimitive();
    Substitution<ReferenceType> substitution =
        Substitution.forArgs(
            JavaTypes.COMPARABLE_TYPE.getTypeParameters(), (ReferenceType) integerType);
    Type integerComparableType = JavaTypes.COMPARABLE_TYPE.apply(substitution);
    substitution =
        Substitution.forArgs(
            JavaTypes.COMPARABLE_TYPE.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    Type stringComparableType = JavaTypes.COMPARABLE_TYPE.apply(substitution);
    TypeSet s = new TypeSet();
    s.add(integerType);
    s.add(JavaTypes.STRING_TYPE);

    List<Type> expected = new ArrayList<>();
    expected.add(integerType);
    expected.add(Type.forClass(Number.class));
    expected.add(JavaTypes.STRING_TYPE);
    expected.add(JavaTypes.OBJECT_TYPE);
    expected.add(JavaTypes.SERIALIZABLE_TYPE);
    expected.add(Type.forClass(CharSequence.class));
    expected.add(integerComparableType);
    expected.add(stringComparableType);
    checkSetContents(s, expected);

    List<Type> expectedMatches = new ArrayList<>();
    expectedMatches.add(integerComparableType);
    expectedMatches.add(stringComparableType);
    checkMatches(s, JavaTypes.COMPARABLE_TYPE, expectedMatches);
  }

  private void checkMatches(
      TypeSet s, GenericClassType comparableType, List<Type> expectedMatches) {
    List<Type> matches = s.match(JavaTypes.COMPARABLE_TYPE);
    for (Type type : expectedMatches) {
      assertThat("set match for " + comparableType, matches.contains(type));
    }
    assertThat(
        "set should have expected match count",
        matches.size(),
        is(equalTo(expectedMatches.size())));
  }

  private void checkSetContents(TypeSet s, List<Type> expected) {
    for (Type type : expected) {
      assertTrue("set should contain " + type, s.contains(type));
    }
    assertThat("set should only have expected types", s.size(), is(equalTo(expected.size())));
  }
}
