package randoop.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import plume.EntryReader;
import randoop.generation.ForwardGenerator;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.ModelCollections;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.TypedOperationManager;
import randoop.types.ClassOrInterfaceType;

public class RandoopPerformanceTest extends AbstractPerformanceTest {

  @Override
  void execute() {
    String resourcename = "java.util.classlist.java1.6.txt";

    List<Class<?>> classes = new ArrayList<>();
    try (EntryReader er = new EntryReader(ForwardExplorerPerformanceTest.class.getResourceAsStream(resourcename))) {
      for (String entry : er) {
        classes.add(Class.forName(entry));
      }
    } catch (IOException e) {
      fail("exception while reading class names");
    } catch (ClassNotFoundException e) {
      fail("couldn't load class");
    }

    List<TypedOperation> model = getConcreteOperations(classes);
    assertFalse("model should not be empty", model.isEmpty());
    System.out.println("done creating model.");
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    GenInputsAbstract.debug_checks = false;
    ForwardGenerator explorer =
        new ForwardGenerator(model, new LinkedHashSet<TypedOperation>(), Long.MAX_VALUE, 100000, 100000, null, null, null);
    explorer.explore();
  }

  @Override
  int expectedTimeMillis() {
    return 10000;
  }


  private static List<TypedOperation> getConcreteOperations(List<Class<?>> classes) {
    final List<TypedOperation> model = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ClassOrInterfaceType declaringType, TypedOperation operation) {
        model.add(operation);
      }
    });
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());
    mgr.add(new OperationExtractor(operationManager, new DefaultReflectionPredicate()));
    for (Class<?> c: classes) {
      mgr.apply(c);
    }
    return model;
  }
}
