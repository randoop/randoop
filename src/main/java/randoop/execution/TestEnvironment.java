package randoop.execution;

import static randoop.execution.RunCommand.CommandException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import randoop.main.GenInputsAbstract;

/** Provides the environment for running JUnit tests. */
public class TestEnvironment {

  /** The process timeout in milliseconds. Defaults to 15 minutes. */
  private long timeout = 15 * 60 * 1000;

  /** The classpath for the tests. */
  private final String testClasspath;

  /** A map from javaagent jar path to argument string. */
  private final LinkedHashMap<Path, String> agentMap = new LinkedHashMap<>();

  /** The path for the replacecall agent. */
  private Path replaceCallAgentPath;

  /** The argument string for the replacecall agent. */
  private String replaceCallAgentArgs;

  /**
   * Creates a test environment with the given classpath and an empty agent map.
   *
   * @param testClasspath the class path for running the tests
   */
  public TestEnvironment(String testClasspath) {
    this.testClasspath = testClasspath;
  }

  /**
   * Adds the path for a javaagent jar file to the agent map. These agents will be included in the
   * command in the order they are added.
   *
   * @param agentPath the path to the Javaagent jar file
   * @param agentArgumentString the argument string for the agent
   */
  public void addAgent(Path agentPath, String agentArgumentString) {
    agentMap.put(agentPath, agentArgumentString);
  }

  /**
   * Sets the path and arguments for the replace call agent. This agent has to be placed on the boot
   * classpath.
   *
   * @param agentPath the path for the replacecall agent
   * @param agentArgs the arguments for running the agent
   */
  public void setReplaceCallAgent(Path agentPath, String agentArgs) {
    replaceCallAgentPath = agentPath;
    replaceCallAgentArgs = agentArgs;
  }

  /**
   * Set the test execution timeout.
   *
   * @param timeout the time in milliseconds that a test is allowed to run before being terminated
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * Runs the named JUnit test class in this environment.
   *
   * @param testClassName the fully-qualified JUnit test class name
   * @param workingDirectory the working directory for executing the test
   * @return the {@link RunCommand.Status} object for the execution of the test class
   * @throws CommandException if there is an error running the test command
   */
  public RunCommand.Status runTest(String testClassName, Path workingDirectory)
      throws CommandException {
    List<String> command = commandPrefix();
    command.add(testClassName);
    return RunCommand.run(command, workingDirectory, timeout);
  }

  /**
   * Constructs the command to run JUnit tests in this environment, minus the name of the test
   * class. Adding the test class name is sufficient to build a runnable command.
   *
   * @return the base command to run JUnit tests in this environment, without a test class name
   */
  private List<String> commandPrefix() {
    List<String> command = new ArrayList<>();
    command.add("java");
    command.add("-ea");
    command.add("-Xmx" + GenInputsAbstract.jvm_max_memory);
    command.add("-XX:+HeapDumpOnOutOfMemoryError");

    if (replaceCallAgentPath != null) {
      command.add("-Xbootclasspath/a:" + replaceCallAgentPath);
      command.add(getJavaagentOption(replaceCallAgentPath, replaceCallAgentArgs));
    }

    for (Map.Entry<Path, String> entry : agentMap.entrySet()) {
      String args = entry.getValue();
      command.add(getJavaagentOption(entry.getKey(), args));
    }

    command.add("-classpath");
    command.add("." + java.io.File.pathSeparator + testClasspath);
    command.add("org.junit.runner.JUnitCore");

    return command;
  }

  private String getJavaagentOption(Path agentPath, String args) {
    String agent = "-javaagent:" + agentPath;
    if (args != null && !args.isEmpty()) {
      agent = agent + "=" + args;
    }
    return agent;
  }
}
