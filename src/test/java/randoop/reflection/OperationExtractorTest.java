package randoop.reflection;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GeneralType;
import randoop.types.ParameterizedType;
import randoop.types.TypeNames;
import randoop.util.MultiMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link OperationExtractor} and {@link TypedOperationManager} to ensure they are
 * collecting the right types and operations.
 * Tests separately for a concrete class and a generic class.
 */
public class OperationExtractorTest {

  @Test
  public void concreteClassTest() {
    final Set<ClassOrInterfaceType> classTypes = new LinkedHashSet<>();
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    final MultiMap<ParameterizedType, TypedOperation> genericClassTypes = new MultiMap<>();
    final Set<TypedOperation> genericOperations = new LinkedHashSet<>();

    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteClassType(ClassOrInterfaceType type) {
        classTypes.add(type);
      }

      @Override
      public void addOperationToGenericType(ParameterizedType declaringType, TypedOperation operation) {
        genericClassTypes.add(declaringType, operation);
      }

      @Override
      public void addGenericOperation(ClassOrInterfaceType declaringType, TypedOperation operation) {
        genericOperations.add(operation);
      }

      @Override
      public void addConcreteOperation(ClassOrInterfaceType declaringType, TypedOperation operation) {
        operations.add(operation);
      }
    });

    OperationExtractor extractor = new OperationExtractor(operationManager, new DefaultReflectionPredicate());
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());
    mgr.add(extractor);

    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.ConcreteClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
    }
    assert c != null;

    mgr.apply(c);

    assertThat("should only be one class", classTypes.size(), is(equalTo(1)) );
    assertThat("name should be", classTypes.iterator().next().getName(), is(equalTo(c.getName())));
    assertTrue("there are no generic types", genericClassTypes.isEmpty());

    assertThat("class has 9 concrete operations", operations.size(), is(equalTo(11)));
    assertThat("class has 1 generic operation", genericOperations.size(), is(equalTo(1)));

  }

  @Test
  public void genericClassTest() {
    final Set<GeneralType> concreteTypes = new LinkedHashSet<>();
    final Set<ParameterizedType> genericTypes = new LinkedHashSet<>();
    final MultiMap<ClassOrInterfaceType,TypedOperation> genericsMap = new MultiMap<>();

    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteClassType(ClassOrInterfaceType type) {
        concreteTypes.add(type);
      }

      @Override
      public void addGenericClassType(ParameterizedType type) {
        genericTypes.add(type);
      }

      @Override
      public void addGenericOperation(ClassOrInterfaceType declaringType, TypedOperation operation) {
        genericsMap.add(declaringType, operation);
      }

    });
    OperationExtractor extractor = new OperationExtractor(operationManager, new DefaultReflectionPredicate());
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());
    mgr.add(extractor);

    Class<?> c = null;
    try {
      c = TypeNames.getTypeForName("randoop.reflection.GenericClass");
    } catch (ClassNotFoundException e) {
      fail("didn't find class: " + e);
    }
    assert c != null;

    mgr.apply(c);

    assertTrue("should be no concrete types", concreteTypes.isEmpty());
    assertTrue("should be a generic type", ! genericTypes.isEmpty());
    assertThat("should be one generic type", genericTypes.size(), is(equalTo(1)));

    for (ClassOrInterfaceType key : genericsMap.keySet()) {
      assertThat("there should be 20 operations", genericsMap.getValues(key).size(), is(equalTo(20)));
    }


  }
}
