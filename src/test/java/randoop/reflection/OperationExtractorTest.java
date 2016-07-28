package randoop.reflection;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ConcreteTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeNames;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link OperationExtractor}  to ensure it is
 * collecting the right types and operations.
 * Tests separately for a concrete class and a generic class.
 */
public class OperationExtractorTest {

  @Test
  public void concreteClassTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();

    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());

    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.ConcreteClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
    }
    assert c != null;
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    mgr.apply(
        new OperationExtractor(
            classType, operations, new DefaultReflectionPredicate(), new OperationModel()),
        c);
    assertThat("name should be", classType.getName(), is(equalTo(c.getName())));

    assertThat("class has 12 operations", operations.size(), is(equalTo(12)));

    int genericOpCount = 0;
    int wildcardOpCount = 0;
    for (TypedOperation operation : operations) {
      if (operation.isGeneric()) {
        genericOpCount++;
      }
      if (operation.hasWildcardTypes()) {
        wildcardOpCount++;
      }
    }
    assertThat("class has one generic operation", genericOpCount, is(equalTo(1)));
    assertThat("class has no operations with wildcards", wildcardOpCount, is(equalTo(0)));
  }

  @Test
  public void genericClassTest() {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());

    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.GenericClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
    }
    assert c != null;
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    assertTrue("should be a generic type", classType.isGeneric());

    assertTrue("should have type parameters", classType.getTypeParameters().size() > 0);
    Substitution<ReferenceType> substitution =
        Substitution.forArgs(
            classType.getTypeParameters(), (ReferenceType) ConcreteTypes.STRING_TYPE);
    classType = classType.apply(substitution);

    mgr.apply(
        new OperationExtractor(
            classType, operations, new DefaultReflectionPredicate(), new OperationModel()),
        c);

    assertThat("there should be 20 operations", operations.size(), is(equalTo(20)));
  }
}
