package randoop.test;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.plumelib.util.EntryReader;
import randoop.generation.ForwardGenerator;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.reflection.OperationExtractor;
import randoop.types.ClassOrInterfaceType;

public class RandoopPerformanceTest extends AbstractPerformanceTest {

  @Override
  void execute() {
    String resourcename = "java.util.classlist.java1.6.txt";

    List<ClassOrInterfaceType> classTypes = new ArrayList<>();
    try (EntryReader er =
        new EntryReader(ForwardExplorerPerformanceTest.class.getResourceAsStream(resourcename))) {
      for (String entryLine : er) {
        @SuppressWarnings("signature:assignment.type.incompatible") // need run-time check
        @ClassGetName String entry = entryLine;
        Class<?> clazz = Class.forName(entry);
        ClassOrInterfaceType type = ClassOrInterfaceType.forClass(clazz);
        classTypes.add(type);
      }
    } catch (IOException e) {
      throw new AssertionError("exception while reading class names", e);
    } catch (ClassNotFoundException e) {
      throw new AssertionError("couldn't load class", e);
    }

    List<TypedOperation> model = OperationExtractor.operations(classTypes);
    assertFalse(model.isEmpty());
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
}
