package randoop.test;

import java.util.ArrayList;

import junit.framework.TestCase;
import randoop.DummyVisitor;
import randoop.ExecutableSequence;
import randoop.ConstructorCall;
import randoop.Sequence;
import randoop.Variable;
import randoop.util.ReflectionExecutor;
import randoop.util.TimeoutExceededException;

public class NonterminatingInputTest extends TestCase {

  public void test() throws SecurityException, NoSuchMethodException {

    Sequence s = new Sequence();
    s = s.extend(ConstructorCall.getRConstructor(Looper.class.getConstructor()), new ArrayList<Variable>());
    int oldTimeout = ReflectionExecutor.timeout;
    ReflectionExecutor.timeout = 500;
    ExecutableSequence es = new ExecutableSequence(s);
    es.execute(new DummyVisitor());
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
