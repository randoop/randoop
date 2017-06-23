package randoop.main;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Manages the TestEnvironment for a system test run. */
class SystemTestEnvironment {
  /** The default directory name for test source files */
  private static final String SOURCE_DIR_NAME = "java";

  /** The default directory name for test source files */
  private static final String CLASS_DIR_NAME = "class";

  /** The default directory name for test jacocoagent files */
  private static final String JACOCO_DIR_NAME = "jacoco";

  /** The classpath for the systemtest */
  final String classpath;

  /** The root for the system test working directories */
  private final Path systemTestWorkingDir;

  /** The path for the JaCoCo javaagent */
  final Path jacocoAgentPath;

  /** The path for the root directory for test input classes. */
  final Path testInputClassDir;

  /** The path for the mapcall agent jar */
  final Path mapcallAgentPath;

  /** The path for the exercised-class agent jar */
  final Path exercisedClassAgentPath;

  /**
   * Initializes a {@link SystemTestEnvironment} with the given classpath, working directory, input
   * class directory, and JaCoCo agent path.
   *
   * @param classpath the class path for this system test run
   * @param workingDir the working directory for this system test run
   * @param testInputClassDir the input class directory
   * @param jacocoAgentPath the path for the jacocoagent.jar file
   */
  private SystemTestEnvironment(
      String classpath,
      Path workingDir,
      Path testInputClassDir,
      Path jacocoAgentPath,
      Path mapcallAgentPath,
      Path exercisedClassAgentPath) {
    this.classpath = classpath;
    this.systemTestWorkingDir = workingDir;
    this.testInputClassDir = testInputClassDir;
    this.jacocoAgentPath = jacocoAgentPath;
    this.mapcallAgentPath = mapcallAgentPath;
    this.exercisedClassAgentPath = exercisedClassAgentPath;
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
  static SystemTestEnvironment createSystemTestEnvironment(String classpath, Path buildDir) {
    Path workingDir = buildDir.resolve("working-directories");
    Path testInputClassDir =
        buildDir.resolve("classes/java/testInput"); //XXX breaks when Gradle changes
    Path jacocoAgentPath = buildDir.resolve("jacocoagent/jacocoagent.jar");
    Path libsPath = buildDir.resolve("libs");
    Path mapcallAgentPath = null;
    Path exercisedClassAgentPath = null;
    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(libsPath)) {
      for (Path entry : dirStream) {
        if (entry.getFileName().toString().startsWith("exercised-class")) {
          exercisedClassAgentPath = entry;
        }
        if (entry.getFileName().toString().startsWith("mapcall")) {
          mapcallAgentPath = entry;
        }
      }
    } catch (IOException e) {
      fail("unable to get build directory contents");
    }
    return new SystemTestEnvironment(
        classpath,
        workingDir,
        testInputClassDir,
        jacocoAgentPath,
        mapcallAgentPath,
        exercisedClassAgentPath);
  }

  /**
   * Creates the {@link TestEnvironment} for a test using the given directory name. Creates a
   * subdirectory in the {@link #systemTestWorkingDir} that contains the subdirectories for source,
   * class and JaCoCo files using the directory names {@link #SOURCE_DIR_NAME}, {@link
   * #CLASS_DIR_NAME}, and {@link #JACOCO_DIR_NAME}.
   *
   * <p>Will fail calling test if an {@code IOException} is thrown
   *
   * @param dirname the name of the directory to create
   * @return the {@link TestEnvironment} with the directory as the working directory
   */
  TestEnvironment createTestEnvironment(String dirname) {
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
    return new TestEnvironment(this, testDir, sourceDir, classDir, jacocoDir);
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
