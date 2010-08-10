package randoop.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import randoop.StatementKind;
import randoop.experiments.RandomWalkGenerator;
import randoop.util.Reflection;

public class NaiveGeneratorTest extends TestCase {

  public void test() throws IOException {

    List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.add(ArrayList.class);
    classes.add(Object.class);
    
    List<StatementKind> statements = Reflection.getStatements(classes, null);

    RandomWalkGenerator gen = new RandomWalkGenerator(statements, Long.MAX_VALUE, 1000000, null, null, null, null);

    gen.explore();
  }
}
