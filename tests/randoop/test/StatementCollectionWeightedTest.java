package randoop.test;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import randoop.StatementKind;
import randoop.util.Reflection;
import randoop.util.Util;

public class StatementCollectionWeightedTest extends TestCase {

  @SuppressWarnings("deprecation")
  public void test() throws SecurityException, NoSuchMethodException {

    if (true) return;

    List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.add(java.util.ArrayList.class);

    List<StatementKind> statements = 
      Reflection.getStatements(classes, null);

    StringBuilder weightedMethods = new StringBuilder();
    weightedMethods.append("java.util.ArrayList.add(java.lang.Object)" + Util.newLine);
    weightedMethods.append("10");

    InputStream stream = new StringBufferInputStream(weightedMethods.toString());

    fail();
    //statements.assignWeights(stream);

//  StatementKind addStatement = RMethod.getRMethod(java.util.ArrayList.class.getMethod("add", Object.class));
//  for (StatementKind s : statements) {
//  if (s.equals(addStatement)) {
//  assertEquals(10.0, statements.getWeight(s));
//  } else {
//  assertEquals(1.0, statements.getWeight(s));
//  }
//  }
  }
}
