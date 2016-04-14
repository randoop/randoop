package randoop.operation;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.ModelCollections;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.TypedOperationManager;
import randoop.reflection.VisibilityPredicate;
import randoop.test.AnIntegerPredicate;
import randoop.types.ConcreteType;

import static org.junit.Assert.assertEquals;

/**
 * Created by bjkeller on 4/14/16.
 */
public class ClassReflectionTest {

  @Test
  public void implementsParameterizedTypeTest() {
    Class<?> c = AnIntegerPredicate.class;
    Set<ConcreteOperation> actual = getConcreteOperations(c);

    // TODO be sure the types of the inherited method has the proper type arguments
    assertEquals("number of operations", 4, actual.size());
  }

  private Set<ConcreteOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(c, new DefaultReflectionPredicate(), new PublicVisibilityPredicate());
  }

  private Set<ConcreteOperation> getConcreteOperations(Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    final Set<ConcreteOperation> operations = new LinkedHashSet<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        operations.add(operation);
      }
    });
    OperationExtractor extractor = new OperationExtractor(operationManager, predicate);
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.add(extractor);
    manager.apply(c);
    return operations;
  }
}
