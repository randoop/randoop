package randoop.test;

import static org.junit.Assert.assertFalse;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.plumelib.util.EntryReader;
import randoop.generation.ForwardGenerator;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.ReflectionManager;
import randoop.reflection.VisibilityPredicate;
import randoop.types.ClassOrInterfaceType;

public class RandoopPerformanceTest extends AbstractPerformanceTest {

  @Override
  void execute() {
    String resourcename = "java.util.classlist.java1.6.txt";

    List<Class<?>> classes = new ArrayList<>();
    try (EntryReader er =
        new EntryReader(ForwardExplorerPerformanceTest.class.getResourceAsStream(resourcename))) {
      for (String entryLine : er) {
        @SuppressWarnings("signature:assignment.type.incompatible") // need run-time check
        @ClassGetName String entry = entryLine;
        classes.add(Class.forName(entry));
      }
    } catch (IOException e) {
      throw new AssertionError("exception while reading class names", e);
    } catch (ClassNotFoundException e) {
      throw new AssertionError("couldn't load class", e);
    }

    List<TypedOperation> model = getConcreteOperations(classes);
    assertFalse("model should not be empty", model.isEmpty());
    System.out.println("done creating model.");
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    GenInputsAbstract.debug_checks = false;
    ForwardGenerator explorer =
        new ForwardGenerator(
            model,
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(0, 100000, 100000, 100000),
            null,
            null,
            null);
    explorer.createAndClassifySequences();
  }

  @Override
  int expectedTimeMillis() {
    return 10000;
  }

  private static List<TypedOperation> getConcreteOperations(List<Class<?>> classes) {
    final List<TypedOperation> model = new ArrayList<>();
    VisibilityPredicate visibility = IS_PUBLIC;
    ReflectionManager mgr = new ReflectionManager(visibility);
    for (Class<?> c : classes) {
      ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
      final OperationExtractor extractor =
          new OperationExtractor(classType, new DefaultReflectionPredicate(), visibility);
      mgr.apply(extractor, c);
      model.addAll(extractor.getOperations());
    }
    return model;
  }
}
