package randoop.reflection;

import org.junit.Test;
import static org.junit.Assert.fail;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.operation.ConcreteOperation;
import randoop.operation.GenericOperation;
import randoop.types.ConcreteType;
import randoop.types.GenericType;
import randoop.types.TypeNames;
import randoop.util.MultiMap;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.*;

/**
 * Created by bjkeller on 3/24/16.
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
      public void addGenericOperation(GenericType declaringType, GenericOperation operation) {
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

    OperationExtractor extractor = new OperationExtractor(operationManager);
    ReflectionManager mgr = new ReflectionManager(new DefaultReflectionPredicate());
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
  }
}
