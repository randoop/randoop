package randoop.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;

/** Tests for ReflectionExecutor to verify concurrent execution of multiple tasks. */
public class ReflectionExecutorConcurrentTest {
  @Before
  public void setUp() {
    ReflectionExecutor.resetStatistics();
    ReflectionExecutor.usethreads = true;
    ReflectionExecutor.initializeExecutor(4);
  }

  @After
  public void tearDown() {
    ReflectionExecutor.shutdownExecutor();
  }

  /**
   * Test concurrent execution by submitting multiple tasks at the same time. Each task sleeps for
   * 300ms and then returns its index. With 8 tasks and a thread pool of 4 threads, the total time
   * should be roughly two batches (~600ms) rather than 8 * 300ms.
   */
  @Test
  public void testConcurrentTaskExecution() throws Exception {
    final int numTasks = 8;
    final long sleepMillis = 300;
    final CountDownLatch startLatch = new CountDownLatch(1);

    ExecutorService testExecutor = Executors.newFixedThreadPool(numTasks);
    List<Future<Integer>> futures = new ArrayList<>();

    for (int i = 0; i < numTasks; i++) {
      final int taskIndex = i;
      Future<Integer> future =
          testExecutor.submit(
              new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                  startLatch.await();
                  ReflectionCode code =
                      new ReflectionCode() {
                        private Object retval;
                        private Throwable exception;

                        @Override
                        public void runReflectionCodeRaw() {
                          try {
                            Thread.sleep(sleepMillis);
                          } catch (InterruptedException e) {

                          }
                          retval = taskIndex;
                        }

                        @Override
                        public Object getReturnValue() {
                          return retval;
                        }

                        @Override
                        public Throwable getExceptionThrown() {
                          return exception;
                        }
                      };
                  ExecutionOutcome outcome = ReflectionExecutor.executeReflectionCode(code);
                  if (outcome instanceof NormalExecution) {
                    NormalExecution normal = (NormalExecution) outcome;
                    return (Integer) normal.getRuntimeValue();
                  } else {
                    throw new IllegalStateException(
                        "Task " + taskIndex + " did not complete normally.");
                  }
                }
              });
      futures.add(future);
    }

    long startTime = System.currentTimeMillis();
    startLatch.countDown();

    for (int i = 0; i < numTasks; i++) {
      int result = futures.get(i).get();
      assertEquals("Task " + i + " should return its index", i, result);
    }

    long elapsedTime = System.currentTimeMillis() - startTime;
    assertTrue(
        "Total execution time (" + elapsedTime + "ms) should be less than 1000ms",
        elapsedTime < 1000);

    testExecutor.shutdownNow();
  }
}
