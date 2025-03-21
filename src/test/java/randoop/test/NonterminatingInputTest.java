package randoop.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.operation.ConstructorCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.RandoopTypeException;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.ReflectionExecutor;

public class NonterminatingInputTest {

  @Test
  public void test() throws SecurityException, NoSuchMethodException {

    Sequence s = new Sequence();
    TypedOperation con = null;
    try {
      con = createConstructorCall(Looper.class.getConstructor());
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
    s = s.extend(con, new ArrayList<Variable>());
    int oldCallTimeoutMillis = ReflectionExecutor.call_timeout_millis;
    ReflectionExecutor.call_timeout_millis = 500;
    ExecutableSequence es = new ExecutableSequence(s);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    ReflectionExecutor.call_timeout_millis = oldCallTimeoutMillis;
    assertTrue(es.throwsException(TimeoutException.class));
  }

  static class Looper {
    public Looper() {
      while (true) {
        // loop.
      }
    }
  }

  private TypedOperation createConstructorCall(Constructor<?> con) throws RandoopTypeException {
    ConstructorCall op = new ConstructorCall(con);
    ClassOrInterfaceType declaringType = ClassOrInterfaceType.forClass(con.getDeclaringClass());
    List<Type> paramTypes = new ArrayList<>();
    for (Class<?> pc : con.getParameterTypes()) {
      paramTypes.add(Type.forClass(pc));
    }
    return new TypedClassOperation(op, declaringType, new TypeTuple(paramTypes), declaringType);
  }
}
