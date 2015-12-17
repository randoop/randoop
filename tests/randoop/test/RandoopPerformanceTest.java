package randoop.test;

import java.io.InputStream;
import java.util.List;

import randoop.main.ClassReader;
import randoop.main.GenInputsAbstract;
import randoop.operation.Operation;
import randoop.reflection.OperationExtractor;
import randoop.sequence.ForwardGenerator;

public class RandoopPerformanceTest extends AbstractPerformanceTest {

  @Override
  void execute() {
    String resourcename = "resources/java.util.classlist.java1.6.txt";

    InputStream classStream =
      ForwardExplorerPerformanceTest.class.getResourceAsStream(resourcename);

    List<Operation> model =
      OperationExtractor.getOperations(ClassReader.getClassesForStream(classStream, resourcename),null);
    assertFalse("model should not be empty", model.isEmpty());
    System.out.println("done creating model.");
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    GenInputsAbstract.debug_checks = false;
    ForwardGenerator explorer = new ForwardGenerator(model, Long.MAX_VALUE, 100000, 100000, null, null, null);
    explorer.explore();
  }

  @Override
  int expectedTimeMillis() {
    return 10000;
  }

}
