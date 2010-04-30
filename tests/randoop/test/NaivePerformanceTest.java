package randoop.test;

import java.io.InputStream;
import java.util.List;

import randoop.Globals;
import randoop.NaiveRandomGenerator;
import randoop.StatementKind;
import randoop.main.GenInputsAbstract;
import randoop.util.Reflection;

public class NaivePerformanceTest extends AbstractPerformanceTest {

  @Override
  void execute() {
    InputStream classStream =
      ForwardExplorerPerformanceTest.class.getResourceAsStream("resources/java.util.classlist.java1.6.txt");

    List<StatementKind> m =
      Reflection.getStatements(Reflection.loadClassesFromStream(classStream),null);
    System.out.println("done creating model.");
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    Globals.nochecks = true;
    NaiveRandomGenerator explorer = new NaiveRandomGenerator(m, null, Long.MAX_VALUE, 100000, null);
    explorer.explore();
  }

  @Override
  int expectedTimeMillis() {
    return 16000;
  }

}
