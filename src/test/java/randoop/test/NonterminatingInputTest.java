package randoop.test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import randoop.DummyVisitor;
import randoop.operation.ConcreteOperation;
import randoop.operation.ConstructorCall;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;
import randoop.util.ReflectionExecutor;
import randoop.util.TimeoutExceededException;

import junit.framework.TestCase;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NonterminatingInputTest {

  @Test
  public void test() throws SecurityException, NoSuchMethodException {

    Sequence s = new Sequence();
    ConcreteOperation con = createConstructorCall(Looper.class.getConstructor());
    s = s.extend(con, new ArrayList<Variable>());
    int oldTimeout = ReflectionExecutor.timeout;
    ReflectionExecutor.timeout = 500;
    ExecutableSequence es = new ExecutableSequence(s);
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    ReflectionExecutor.timeout = oldTimeout;
    assertTrue(es.throwsException(TimeoutExceededException.class));
  }

  public static class Looper {
    public Looper() {
      while (true) {
        // loop.
      }
    }
  }

  private ConcreteOperation createConstructorCall(Constructor<?> con) {
    ConstructorCall op = new ConstructorCall(con);
    ConcreteType declaringType = ConcreteType.forClass(con.getDeclaringClass());
    List<ConcreteType> paramTypes = new ArrayList<>();
    for (Class<?> pc : con.getParameterTypes()) {
      paramTypes.add(ConcreteType.forClass(pc));
    }
    return new ConcreteOperation(op, declaringType, new ConcreteTypeTuple(paramTypes), declaringType);
  }
}
