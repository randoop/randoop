package randoop.main;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Manages the SystemTestEnvironment for a system test run. */
class SystemTestEnvironmentManager {
  /** The default directory name for test source files. */
  private static final String SOURCE_DIR_NAME = "java";

  /** The default directory name for test source files. */
  private static final String CLASS_DIR_NAME = "class";

  /** The default directory name for test jacocoagent files. */
  private static final String JACOCO_DIR_NAME = "jacoco";

  /** The classpath for the systemtest. */
  final String classpath;

  /** The root for the system test working directories. */
  private final Path systemTestWorkingDir;

  /** The path for the JaCoCo javaagent. */
  private final Path jacocoAgentPath;

  /** The path for the root directory for test input classes. */
  private final Path testInputClassDir;

  /** The path for the replacecall agent jar. */
  final Path replacecallAgentPath;

  /** The path for the covered-class agent jar. */
  final Path coveredClassAgentPath;

  /**
   * Initializes a {@link SystemTestEnvironmentManager} with the given classpath, working directory,
   * input class directory, and JaCoCo agent path.
   *
   * @param classpath the class path for this system test run
   * @param workingDir the working directory for this system test run
   * @param testInputClassDir the input class directory
   * @param jacocoAgentPath the path for the jacocoagent.jar file
   */
  private SystemTestEnvironmentManager(
      String classpath,
      Path workingDir,
      Path testInputClassDir,
      Path jacocoAgentPath,
      Path replacecallAgentPath,
      Path coveredClassAgentPath) {
    this.classpath = classpath;
    this.systemTestWorkingDir = workingDir;
    this.testInputClassDir = testInputClassDir;
    this.jacocoAgentPath = jacocoAgentPath;
    this.replacecallAgentPath = replacecallAgentPath;
    this.coveredClassAgentPath = coveredClassAgentPath;
  }

  /**
   * Creates the system test environment using the given classpath and build directory. Assumes
   * working directories, test input class files, and the jacoco agent jar file are all located
   * within the build directory.
   *
   * @param classpath the system test classpath
   * @param buildDir the build directory
   * @return the system test environment with
   */
  static SystemTestEnvironmentManager createSystemTestEnvironmentManager(
      String classpath, Path buildDir) {
    Path workingDir = buildDir.resolve("working-directories");
    Path testInputClassDir =
        buildDir.resolve("classes/java/testInput"); // XXX breaks when Gradle changes
    Path jacocoAgentPath = buildDir.resolve("jacocoagent/jacocoagent.jar");

    Path randoopJarPath = getPathFromProperty("jar.randoop");
    Path replacecallAgentPath = getPathFromProperty("jar.replacecall.agent");
    Path coveredClassAgentPath = getPathFromProperty("jar.covered.class.agent");

    assert randoopJarPath != null;

    return new SystemTestEnvironmentManager(
        classpath + java.io.File.pathSeparator + randoopJarPath,
        workingDir,
        testInputClassDir,
        jacocoAgentPath,
        replacecallAgentPath,
        coveredClassAgentPath);
  }

  /**
   * Gets the {@code Path} for the system property representing a path.
   *
   * @param pathProperty the name of the system property for a path
   * @return the path named by the system property
   */
  private static Path getPathFromProperty(String pathProperty) {
    String pathString = System.getProperty(pathProperty);
    if (pathString != null && !pathString.isEmpty()) {
      return Paths.get(pathString);
    }
    return null;
  }

  /**
   * Creates the {@link SystemTestEnvironment} for a test run in the given directory name (usually a
   * temporary directory). Creates a subdirectory in the {@link #systemTestWorkingDir} that contains
   * the subdirectories for source, class and JaCoCo files using the directory names {@link
   * #SOURCE_DIR_NAME}, {@link #CLASS_DIR_NAME}, and {@link #JACOCO_DIR_NAME}.
   *
   * <p>Will fail the calling test if an {@code IOException} is thrown
   *
   * @param dirname the name of the directory to create
   * @return the {@link SystemTestEnvironment} with the directory as the working directory
   */
  SystemTestEnvironment createTestEnvironment(String dirname) {
    return createTestEnvironment(dirname, this.classpath, null);
  }

  /**
   * Creates the {@link SystemTestEnvironment} for a test run in the given directory name (usually a
   * temporary directory) and using the given classpath. Creates a subdirectory in the {@link
   * #systemTestWorkingDir} that contains the subdirectories for source, class and JaCoCo files
   * using the directory names {@link #SOURCE_DIR_NAME}, {@link #CLASS_DIR_NAME}, and {@link
   * #JACOCO_DIR_NAME}.
   *
   * <p>Will fail calling test if an {@code IOException} is thrown
   *
   * @param dirname the name of the working directory to create
   * @param classpath the classpath to use for the test
   * @param bootclasspath the bootclasspath to use for the test, null if none
   * @return the {@link SystemTestEnvironment} with the directory as the working directory and using
   *     the given classpath
   */
  SystemTestEnvironment createTestEnvironment(
      String dirname, String classpath, String bootclasspath) {
    Path testDir = null;
    Path sourceDir = null;
    Path classDir = null;
    Path jacocoDir = null;
    try {
      testDir = createSubDirectory(systemTestWorkingDir, dirname);
      sourceDir = createSubDirectory(testDir, SOURCE_DIR_NAME);
      classDir = createSubDirectory(testDir, CLASS_DIR_NAME);
      jacocoDir = createSubDirectory(testDir, JACOCO_DIR_NAME);
    } catch (IOException e) {
      fail("failed to create working directory for test: " + e);
    }
    return new SystemTestEnvironment(
        bootclasspath,
        classpath,
        this.jacocoAgentPath,
        this.testInputClassDir,
        testDir,
        sourceDir,
        classDir,
        jacocoDir);
  }

  /**
   * Creates a directory in the given parent directory with the subdirectory name.
   *
   * @param parentDir the parent directory
   * @param subdirName the subdirectory name
   * @return the path of the created subdirectory
   */
  private Path createSubDirectory(Path parentDir, String subdirName) throws IOException {
    Path subDir = parentDir.resolve(subdirName);
    if (!Files.exists(subDir)) {
      Files.createDirectory(subDir);
    }
    return subDir;
  }
}
