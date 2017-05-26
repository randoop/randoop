package randoop.main;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Captures the status of a Randoop run, along with status from the compilation of the Randoop
 * generated tests.
 */
class RandoopRunStatus {

  /** The {@link ProcessStatus} for the Randoop run. */
  final ProcessStatus processStatus;

  /** The number of operators used in the Randoop run. */
  final int operatorCount;

  /** The number of generated regression tests. */
  final int regressionTestCount;

  /** The number of generated error-revealing tests. */
  final int errorTestCount;

  /**
   * Creates a {@link RandoopRunStatus} object with the given {@link ProcessStatus}, operator count,
   * and generated test counts.
   *
   * @param processStatus the status of Randoop execution
   * @param operatorCount the number of operators used in generation
   * @param regressionTestCount the number of generated regression tests
   * @param errorTestCount the number of generated error-revealing tests
   */
  private RandoopRunStatus(
      ProcessStatus processStatus, int operatorCount, int regressionTestCount, int errorTestCount) {
    this.processStatus = processStatus;
    this.operatorCount = operatorCount;
    this.regressionTestCount = regressionTestCount;
    this.errorTestCount = errorTestCount;
  }

  /**
   * Runs Randoop and compiles.
   *
   * @param testEnvironment the {@link TestEnvironment} for this run
   * @param options the command-line arguments to Randoop
   * @return the status information collected from generation and compilation
   */
  static RandoopRunStatus generateAndCompile(
      TestEnvironment testEnvironment, RandoopOptions options, boolean allowRandoopFailure) {

    List<String> command = new ArrayList<>();
    command.add("java");
    command.add("-ea");
    command.add("-classpath");
    command.add(testEnvironment.getSystemTestClasspath());
    if (testEnvironment.getJavaAgentPath() != null) {
      String agent = "-javaagent:" + testEnvironment.getJavaAgentPath();
      String args = testEnvironment.getJavaAgentArgumentString();
      if (args != null) {
        agent = agent + "=" + args;
      }
      command.add(agent);
    }
    command.add("randoop.main.Main");
    command.add("gentests");
    command.addAll(options.getOptions());
    ProcessStatus randoopExitStatus = ProcessStatus.runCommand(command);

    if (randoopExitStatus.exitStatus != 0) {
      if (allowRandoopFailure) {
        return getRandoopRunStatus(randoopExitStatus);
      } else {
        for (String line : randoopExitStatus.outputLines) {
          System.out.println(line);
        }
        fail("Randoop exited badly, exit value = " + randoopExitStatus.exitStatus);
      }
    }

    String packagePathString = options.getPackageName().replace('.', '/');
    String regressionBasename = options.getRegressionBasename();
    String errorBasename = options.getErrorBasename();

    // determine whether files are really there and have the right names
    Path srcDir = testEnvironment.sourceDir.resolve(packagePathString);
    List<File> testClassSourceFiles = getFiles(srcDir, "*.java", regressionBasename, errorBasename);

    // definitely cannot do anything useful if no generated test files
    // but not sure that this is the right way to deal with it
    // what if test is meant not to generate anything ?
    if (testClassSourceFiles.size() == 0) {
      for (String line : randoopExitStatus.outputLines) {
        System.err.println(line);
      }
      fail("No test class source files found");
    }

    Path classDir = testEnvironment.classDir;
    CompilationStatus compileStatus =
        CompilationStatus.compileTests(testClassSourceFiles, classDir.toString());
    if (!compileStatus.succeeded) {
      if (randoopExitStatus.exitStatus == 0) {
        for (String line : randoopExitStatus.outputLines) {
          System.err.println(line);
        }
      }
      compileStatus.printDiagnostics(System.err);

      fail("Compilation failed");
    }

    Path classFileDir = classDir.resolve(packagePathString);
    List<File> testClassClassFiles =
        getFiles(classFileDir, "*.class", regressionBasename, errorBasename);
    assertThat(
        "Number of compiled tests equals source tests",
        testClassClassFiles.size(),
        is(equalTo(testClassSourceFiles.size())));

    return getRandoopRunStatus(randoopExitStatus);
  }

  /**
   * Collects the list of {@code File} objects for the files in the given directory that have the
   * extension. If the directory contains any files that do not begin with one of the given
   * basenames, then fails the calling test.
   *
   * @param dirPath the directory path
   * @param extension the expected file extension
   * @param regressionBasename the basename for regression tests
   * @param errorBasename the basename for error-revealing tests
   * @return the list of tests with the extension in the directory
   */
  private static List<File> getFiles(
      Path dirPath, String extension, String regressionBasename, String errorBasename) {
    List<File> files = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, extension)) {
      for (Path entry : stream) {
        String filename = entry.getFileName().toString();
        assertThat(
            "Test class filename should start with basename",
            filename,
            is(anyOf(startsWith(regressionBasename), startsWith(errorBasename))));
        files.add(entry.toFile());
      }
    } catch (IOException e) {
      fail("Exception reading working directory " + e);
    }
    return files;
  }

  /**
   * Converts the {@link ProcessStatus} of a Randoop run to a {@link RandoopRunStatus} object.
   * Counts the numbers of operators collected, regression tests generated, and error-revealing
   * tests generated.
   *
   * @param ps the {@link ProcessStatus} from a Randoop run
   * @return the {@link RandoopRunStatus} for the given process status
   */
  private static RandoopRunStatus getRandoopRunStatus(ProcessStatus ps) {
    int operatorCount = 0;
    int regressionTestCount = 0;
    int errorTestCount = 0;

    for (String line : ps.outputLines) {
      if (line.contains("PUBLIC MEMBERS=") || line.contains("test count:")) {
        int count = Integer.valueOf(line.replaceFirst("\\D*(\\d*).*", "$1"));
        if (line.contains("PUBLIC MEMBERS=")) {
          operatorCount = count;
        } else if (line.contains("Regression")) {
          regressionTestCount = count;
        } else if (line.contains("Error")) {
          errorTestCount = count;
        }
      }
    }

    return new RandoopRunStatus(ps, operatorCount, regressionTestCount, errorTestCount);
  }
}
