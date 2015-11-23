package randoop.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import randoop.experiments.RandomWalkGenerator;
import randoop.operation.Operation;
import randoop.reflection.OperationExtractor;

import junit.framework.TestCase;

import junit.framework.TestCase;

public class NaiveGeneratorTest extends TestCase {

  public void test() throws IOException {

    List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.add(ArrayList.class);
    classes.add(Object.class);
    
    List<Operation> statements = OperationExtractor.getOperations(classes, null);

    RandomWalkGenerator gen = new RandomWalkGenerator(statements, Long.MAX_VALUE, 1000000, null, null, null, null);

    gen.explore();
  }
}
