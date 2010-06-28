package randoop.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import randoop.NaiveRandomGenerator;
import randoop.StatementKind;
import randoop.util.Reflection;

public class NaiveGeneratorTest extends TestCase {

  public void test() throws IOException {

    List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.add(ArrayList.class);
    classes.add(Object.class);
    
    List<StatementKind> statements = Reflection.getStatements(classes, null);

    NaiveRandomGenerator gen = new NaiveRandomGenerator(statements, null, Long.MAX_VALUE, 1000000, null, null);

    gen.explore();
  }
}
