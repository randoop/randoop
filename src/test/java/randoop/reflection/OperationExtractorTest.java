package randoop.reflection;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ParameterizedType;
import randoop.types.TypeNames;
import randoop.util.MultiMap;

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
    final Set<ClassOrInterfaceType> classTypes = new LinkedHashSet<>();
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    final MultiMap<ParameterizedType, TypedOperation> genericClassTypes = new MultiMap<>();

    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());

    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.ConcreteClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
    }
    assert c != null;
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    classTypes.add(classType);
    mgr.apply(new OperationExtractor(classType, operations, new DefaultReflectionPredicate()), c);

    assertThat("should only be one class", classTypes.size(), is(equalTo(1)) );
    assertThat("name should be", classTypes.iterator().next().getName(), is(equalTo(c.getName())));
    assertTrue("there are no generic types", genericClassTypes.isEmpty());

    assertThat("class has 9 concrete operations", operations.size(), is(equalTo(11)));

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

    mgr.apply(new OperationExtractor(classType, operations, new DefaultReflectionPredicate()), c);

    assertTrue("should be a generic type", classType.isGeneric());
    assertThat("there should be 20 operations", operations.size(), is(equalTo(20)));
  }
}
