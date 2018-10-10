package randoop.main;

import java.nio.file.Path;

/** Manages the environment for an individual system test method. */
class SystemTestEnvironment {

  /** The current working directory. */
  final Path workingDir;

  /** The source directory. */
  final Path sourceDir;

  /** The class directory. */
  final Path classDir;

  /** The JaCoCo output directory. */
  final Path jacocoDir;

  /** The classpath to run the tests in this environment. */
  final String testClassPath;

  /** The bootclass path for running the tests in this environment. */
  private final String bootclasspath;

  /** The input classpath for running the tests in this environment. */
  private final String classpath;

  /** The path of the JaCoCo agent. */
  private final Path jacocoAgentPath;

  /** The path of the testinput class files. */
  private final Path testInputClassDir;

  /** the path to the java agent. null by default. */
  private Path javaAgentPath;

  /** the argument string for the java agent during generation. null by default */
  private String javaAgentArgumentString;

  /** the argument string for the java agent during test runs. null by default */
  private String javaAgentTestArgumentString;

  /**
   * Creates a test environment for a specific system test method.
   *
   * @param bootclasspath the bootclasspath for this system test
   * @param classpath the classpath for this system test
   * @param jacocoAgentPath the path for the JaCoCo agent
   * @param testInputClassDir the path for the input class directory
   * @param workingDir the working directory for the test method
   * @param sourceDir the source directory for Randoop generated tests
   * @param classDir the directory for compiled Randoop generated tests
   * @param jacocoDir the directory for output of JaCoCo when running Randoop generated tests
   */
  SystemTestEnvironment(
      String bootclasspath,
      String classpath,
      Path jacocoAgentPath,
      Path testInputClassDir,
      Path workingDir,
      Path sourceDir,
      Path classDir,
      Path jacocoDir) {
    this.bootclasspath = bootclasspath;
    this.classpath = classpath;
    this.jacocoAgentPath = jacocoAgentPath;
    this.testInputClassDir = testInputClassDir;
    this.workingDir = workingDir;
    this.sourceDir = sourceDir;
    this.classDir = classDir;
    this.jacocoDir = jacocoDir;
    this.testClassPath = classpath + java.io.File.pathSeparator + classDir.toString();
    this.javaAgentPath = null;
    this.javaAgentArgumentString = null;
    this.javaAgentTestArgumentString = null;
  }

  /**
   * Returns the classpath for the system tests.
   *
   * @return the classpath for the system tests
   */
  String getSystemTestClasspath() {
    return classpath;
  }

  /**
   * Returns the path to the JaCoCo agent in the build directory.
   *
   * @return the path to the {@code jacocoagent.jar} file
   */
  Path getJacocoAgentPath() {
    return jacocoAgentPath;
  }

  /**
   * Returns the root directory for test input classes.
   *
   * @return the path for the test input class root directory
   */
  Path getTestInputClassDir() {
    return testInputClassDir;
  }

  void addJavaAgent(Path javaAgentPath) {
    this.javaAgentPath = javaAgentPath;
  }

  void addJavaAgent(Path javaAgentPath, String argumentString) {
    this.addJavaAgent(javaAgentPath, argumentString, argumentString);
  }

  void addJavaAgent(Path javaAgentPath, String genArgumentString, String testArgumentString) {
    this.javaAgentPath = javaAgentPath;
    this.javaAgentArgumentString = genArgumentString;
    this.javaAgentTestArgumentString = testArgumentString;
  }

  Path getJavaAgentPath() {
    return javaAgentPath;
  }

  String getJavaAgentArgumentString() {
    return javaAgentArgumentString;
  }

  String getJavaAgentTestArgumentString() {
    return javaAgentTestArgumentString;
  }

  public String getBootClassPath() {
    return bootclasspath;
  }
}
