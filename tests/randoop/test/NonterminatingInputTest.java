package randoop.test;

import java.util.ArrayList;

import randoop.DummyVisitor;
import randoop.operation.ConstructorCall;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.util.ReflectionExecutor;
import randoop.util.TimeoutExceededException;

import junit.framework.TestCase;

public class NonterminatingInputTest extends TestCase {

  public void test() throws SecurityException, NoSuchMethodException {

    Sequence s = new Sequence();
    ConstructorCall con = ConstructorCall.createConstructorCall(Looper.class.getConstructor());
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
}
