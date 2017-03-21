package randoop.main;

import java.nio.file.Path;

/** Manages the environment for an individual system test method. */
class TestEnvironment {

  /** The current working directory. */
  final Path workingDir;

  /** The source directory */
  final Path sourceDir;

  /** The class directory */
  final Path classDir;

  /** The JaCoCo output directory */
  final Path jacocoDir;

  /** The parent environment */
  private final SystemTestEnvironment systemTestEnvironment;

  /** The classpath to run the tests in this environment */
  final String testClassPath;

  /** the path to the java agent. null by default. */
  private Path javaAgentPath;

  /**
   * Creates a test environment for a specific system test method.
   *
   * @param systemTestEnvironment the working environment for the system test
   * @param workingDir the working directory for the test method
   * @param sourceDir the source directory for Randoop generated tests
   * @param classDir the directory for compiled Randoop generated tests
   * @param jacocoDir the directory for output of JaCoCo when running Randoop generated tests
   */
  TestEnvironment(
      SystemTestEnvironment systemTestEnvironment,
      Path workingDir,
      Path sourceDir,
      Path classDir,
      Path jacocoDir) {
    this.systemTestEnvironment = systemTestEnvironment;
    this.workingDir = workingDir;
    this.sourceDir = sourceDir;
    this.classDir = classDir;
    this.jacocoDir = jacocoDir;
    this.testClassPath = systemTestEnvironment.classpath + ":" + classDir.toString();
    this.javaAgentPath = null;
  }

  /**
   * Returns the classpath for the system tests.
   *
   * @return the classpath for the system tests
   */
  String getSystemTestClasspath() {
    return systemTestEnvironment.classpath;
  }

  /**
   * Returns the path to the JaCoCo agent in the build directory.
   *
   * @return the path to the {@code jacocoagent.jar} file
   */
  Path getJacocoAgentPath() {
    return systemTestEnvironment.jacocoAgentPath;
  }

  /**
   * Returns the root directory for test input classes.
   *
   * @return the path for the test input class root directory
   */
  Path getTestInputClassDir() {
    return systemTestEnvironment.testInputClassDir;
  }

  void addJavaAgent(Path javaAgentPath) {
    this.javaAgentPath = javaAgentPath;
  }

  Path getJavaAgentPath() {
    return javaAgentPath;
  }
}
