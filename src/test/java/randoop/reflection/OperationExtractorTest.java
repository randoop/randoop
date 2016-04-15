package randoop.reflection;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.operation.ConcreteOperation;
import randoop.operation.GenericOperation;
import randoop.types.ConcreteType;
import randoop.types.GenericClassType;
import randoop.types.GenericType;
import randoop.types.TypeNames;
import randoop.util.MultiMap;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.*;

/**
 * Tests for {@link OperationExtractor} and {@link TypedOperationManager} to ensure they are
 * collecting the right types and operations.
 * Tests separately for a concrete class and a generic class.
 */
public class OperationExtractorTest {

  @Test
  public void concreteClassTest() {
    final Set<ConcreteType> classTypes = new LinkedHashSet<>();
    final Set<ConcreteOperation> operations = new LinkedHashSet<>();
    final MultiMap<GenericType,GenericOperation> genericClassTypes = new MultiMap<>();
    final Set<GenericOperation> genericOperations = new LinkedHashSet<>();

    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteClassType(ConcreteType type) {
        classTypes.add(type);
      }

      @Override
      public void addGenericOperation(GenericClassType declaringType, GenericOperation operation) {
        genericClassTypes.add(declaringType, operation);
      }

      @Override
      public void addGenericOperation(ConcreteType declaringType, GenericOperation operation) {
        genericOperations.add(operation);
      }

      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
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
    final Set<ConcreteType> concreteTypes = new LinkedHashSet<>();
    final Set<GenericType> genericTypes = new LinkedHashSet<>();
    final MultiMap<GenericType,GenericOperation> genericsMap = new MultiMap<>();

    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteClassType(ConcreteType type) {
        concreteTypes.add(type);
      }

      @Override
      public void addGenericClassType(GenericClassType type) {
        genericTypes.add(type);
      }

      @Override
      public void addGenericOperation(GenericClassType declaringType, GenericOperation operation) {
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

    for (GenericType key : genericsMap.keySet()) {
      assertThat("there should be 20 operations", genericsMap.getValues(key).size(), is(equalTo(20)));
    }


  }
}
