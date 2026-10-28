package randoop.main;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/**
 * Checks that every coverage-goals file in {@code src/systemTest/resources/test-covgoals/} is
 * well-formed and free of duplicate, redundant, and unneeded goals, on every JDK version that CI
 * tests. This test does not run Randoop.
 */
public class CoverageGoalsTest {

  /** The JDK versions that CI tests. Keep this in sync with {@code .azure/jobs.m4}. */
  private static final int[] CI_JDK_VERSIONS = {8, 11, 17, 21, 25, 26};

  /**
   * Processes each coverage-goals file for each JDK version that CI tests.
   *
   * @throws IOException if a coverage-goals file cannot be read
   * @throws URISyntaxException if the coverage-goals directory has a malformed URL
   */
  @Test
  public void testCovGoalsFiles() throws IOException, URISyntaxException {
    ClassLoader cloader = MethodHandles.lookup().lookupClass().getClassLoader();
    URL dirUrl = cloader.getResource("test-covgoals");
    if (dirUrl == null) {
      throw new Error("Resource not found on classpath: test-covgoals");
    }
    List<Path> covGoalsFiles;
    try (Stream<Path> files = Files.list(Paths.get(dirUrl.toURI()))) {
      covGoalsFiles =
          files
              .filter(p -> p.toString().endsWith(".covgoals"))
              .sorted()
              .collect(Collectors.toList());
    }
    assertFalse("No .covgoals files in " + dirUrl, covGoalsFiles.isEmpty());

    List<String> failures = new ArrayList<>();
    for (Path covGoalsFile : covGoalsFiles) {
      List<String> covGoals = Files.readAllLines(covGoalsFile, UTF_8);
      for (int jdkVersion : CI_JDK_VERSIONS) {
        try {
          new CoverageChecker(Collections.emptySet(), 0).methods(covGoals, jdkVersion);
        } catch (Error e) {
          failures.add(
              String.format(
                  "%s on JDK %d: %s", covGoalsFile.getFileName(), jdkVersion, e.getMessage()));
        }
      }
    }
    if (!failures.isEmpty()) {
      fail(
          String.format("Bad coverage goals:%n  %s", String.join(String.format("%n  "), failures)));
    }
  }
}
