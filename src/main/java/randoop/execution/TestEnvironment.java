package randoop.execution;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Provides the environment for running JUnit tests. */
public class TestEnvironment {

  /** The process timeout. Set initially to 15 minutes. */
  private long timeout = 900000; //15 minutes

  /** The classpath for the tests */
  private final String testClasspath;

  /** The map from javaagent jar path to argument string */
  private final Map<Path, String> agentMap;

  /**
   * Creates a test environment with the given classpath and an empty agent map.
   *
   * @param testClasspath the class path for running the tests
   */
  public TestEnvironment(String testClasspath) {
    this.testClasspath = testClasspath;
    this.agentMap = new HashMap<>();
  }

  /**
   * Adds a javaagent to the agent map. These agents will be included in the command.
   *
   * @param agentPath the path to the Javaagent jar file
   * @param agentArgumentString the argument string for the agent
   */
  public void addAgent(Path agentPath, String agentArgumentString) {
    agentMap.put(agentPath, agentArgumentString);
  }

  /**
   * Set the test execution timeout.
   *
   * @param timeout the time in microseconds that a test is allowed to run before being terminated.
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * Runs the named JUnit test class in this environment.
   *
   * @param testClassName the fully-qualified JUnit test class
   */
  public RunStatus runTest(String testClassName, File workingDirectory) {

    List<String> command = buildCommandPrefix();
    command.add(testClassName);

    try {
      return RunEnvironment.run(command, workingDirectory, timeout);
    } catch (ProcessException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Constructs the command to run JUnit tests in this environment minus the name of the test class.
   * Adding the test class name is sufficient to build a runnable command.
   *
   * @return the command to run JUnit tests in this environment
   */
  private List<String> buildCommandPrefix() {
    List<String> command = new ArrayList<>();
    command.add("java");

    for (Map.Entry<Path, String> entry : agentMap.entrySet()) {
      if (entry.getKey() != null) {
        String agentPath = entry.getKey().toString();
        String agent = "-javaagent:" + agentPath;
        String args = entry.getValue();
        if (args != null) {
          agent = agent + "=" + args;
        }
        command.add(agent);
      }
    }

    command.add("-ea");
    command.add("-classpath");
    command.add(testClasspath.toString());
    command.add("org.junit.runner.JUnitCore");

    return command;
  }
}
