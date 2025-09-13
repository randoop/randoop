package randoop.main;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.report.JavaNames;
import org.plumelib.util.ClassDeterministic;
import org.plumelib.util.CollectionsPlume;
import org.plumelib.util.StringsPlume;

/** Checks coverage for a test, managing information needed to perform the coverage checks. */
class CoverageChecker {

  /** The classes whose methods must be covered. */
  private final Set<@ClassGetName String> classnames;

  /** The number of methods that must be covered. */
  private final int minMethodsToCover;

  /** The methods that must not be covered. */
  private final Set<String> excludedMethods = new HashSet<>();

  /** The methods whose coverage should be ignored. */
  private final Set<String> dontCareMethods = new HashSet<>();

  /** The major version number of the Java runtime. */
  public static final int javaVersion = getJavaVersion();

  // This is identical to bcel-util's BcelUtil.getJavaVersion.
  // Starting in Java 9, you can use `Runtime.version()`.
  /**
   * Extract the major version number from the "java.version" system property.
   *
   * @return the major version of the Java runtime
   */
  private static int getJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      // Up to Java 8, from a version string like "1.8.whatever", extract "8".
      version = version.substring(2, 3);
    } else {
      // Since Java 9, from a version string like "11.0.1", extract "11".
      int i = version.indexOf('.');
      if (i < 0) {
        // Some Linux dockerfiles return only the major version number for
        // the system property "java.version"; i.e., no ".<minor version>".
        // Return 'version' unchanged in this case.
      } else {
        version = version.substring(0, i);
      }
    }
    // Handle version strings like "18-ea".
    int i = version.indexOf('-');
    if (i > 0) {
      version = version.substring(0, i);
    }
    return Integer.parseInt(version);
  }

  /**
   * Create a coverage checker for the set of class names.
   *
   * @param classnames the class name set
   * @param minMethodsToCover the minimum number of methods that must be covered by this test
   */
  private CoverageChecker(Set<@ClassGetName String> classnames, int minMethodsToCover) {
    this.classnames = classnames;
    this.minMethodsToCover = minMethodsToCover;
  }

  /**
   * Create a coverage checker using the classnames from the option set. All other parts of the
   * options are ignored. Assumes all declared methods of the classes under test should be covered.
   *
   * @param options the test generation options
   * @param minMethodsToCover the minimum number of methods that must be covered by this test
   */
  CoverageChecker(RandoopOptions options, int minMethodsToCover) {
    this(options.getClassnames(), minMethodsToCover);
  }

  /**
   * Create a coverage checker using the classnames from the option set, and the method exclusions
   * in the given file
   *
   * @param options the test generation options
   * @param minMethodsToCover the minimum number of methods that must be covered by this test
   * @param methodSpecsFile which methods should be covered; see {@link #methods}
   */
  static CoverageChecker fromFile(
      RandoopOptions options, int minMethodsToCover, String methodSpecsFile) {
    // Load from classpath: src/systemTest/resources/test-methodspecs/<file>
    CoverageChecker result = new CoverageChecker(options, minMethodsToCover);
    String resource = "test-methodspecs/" + methodSpecsFile;
    Class<?> thisClass = MethodHandles.lookup().lookupClass();
    List<String> methodSpecs;
    try (InputStream in = thisClass.getClassLoader().getResourceAsStream(resource)) {
      if (in == null) {
        throw new Error("Resource not found on classpath: " + resource);
      }
      methodSpecs =
          new BufferedReader(new InputStreamReader(in, UTF_8)).lines().collect(Collectors.toList());
    } catch (IOException e) {
      throw new Error("Problem reading resource " + resource, e);
    }
    result.methods(methodSpecs.toArray(new String[0]));
    return result;
  }

  /**
   * Create a coverage checker using the classnames from the option set, and the given method
   * exclusions.
   *
   * @param options the test generation options
   * @param minMethodsToCover the minimum number of methods that must be covered by this test
   * @param methodSpecs which methods should be covered; see {@link #methods}
   */
  CoverageChecker(RandoopOptions options, int minMethodsToCover, String... methodSpecs) {
    this(options.getClassnames(), minMethodsToCover);
    methods(methodSpecs);
  }

  /**
   * Add a method name to the excluded method names in this checker.
   *
   * @param methodName the name to add
   */
  void exclude(String methodName) {
    excludedMethods.add(methodName);
  }

  /**
   * Add a method name to the ignored methods names in this checker.
   *
   * @param methodName the name to add
   */
  void ignore(String methodName) {
    dontCareMethods.add(methodName);
  }

  /** Matches digits at the end of a string. */
  private static final Pattern TRAILING_NUMBER_PATTERN = Pattern.compile("^(.*?)([0-9]+)$");

  /**
   * Add method names to be excluded, ignored, or included (included has no effect).
   *
   * <p>Each string consists of a signature, a space, and one of the words "exclude", "ignore", or
   * "include". For example: "java7.util7.ArrayList.readObject(java.io.ObjectInputStream) exclude"
   * "exclude{17,21,22+}" and "ignore{17,21,22+}" are similar, but only active if Java version = 17,
   * 21, or &ge; 22.
   *
   * <p>This format is intended to make it easy to sort the arguments.
   *
   * @param methodSpecs method specifications
   */
  void methods(String... methodSpecs) {
    methods(Arrays.asList(methodSpecs));
  }

  /**
   * Add method names to be excluded, ignored, or included (included has no effect).
   *
   * <p>Each string consists of a signature, a space, and one of the words "exclude", "ignore", or
   * "include". For example: "java7.util7.ArrayList.readObject(java.io.ObjectInputStream) exclude"
   * "exclude{17,21,22+}" and "ignore{17,21,22+}" are similar, but only active if Java version = 17,
   * 21, or &ge; 22.
   *
   * <p>This format is intended to make it easy to sort the arguments.
   *
   * @param methodSpecs method specifications
   */
  void methods(List<String> methodSpecs) {
    for (String s : methodSpecs) {
      int hashPos = s.indexOf('#');
      if (hashPos != -1) {
        s = s.substring(0, hashPos);
      }
      s = s.trim();
      if (s.isEmpty()) {
        continue;
      }
      int spacepos = s.lastIndexOf(' ');
      if (spacepos == -1) {
        throw new Error(
            "Bad method spec, lacks action at end "
                + "(exclude{,NN,NN+}, ignore{,NN,NN+}, or include): "
                + s);
      }
      String methodName = s.substring(0, spacepos);
      String action = s.substring(spacepos + 1);

      boolean plus;
      if (action.endsWith("+")) {
        action = action.substring(0, action.length() - 1);
        plus = true;
      } else {
        plus = false;
      }

      int actionJdk;
      Matcher m = TRAILING_NUMBER_PATTERN.matcher(action);
      if (m.matches()) {
        action = m.group(1);
        actionJdk = Integer.parseInt(m.group(2));
      } else {
        actionJdk = 0;
      }

      if (actionJdk == 0
          || (!plus && javaVersion == actionJdk)
          || (plus && javaVersion >= actionJdk)) {
        switch (action) {
          case "exclude":
            exclude(methodName);
            break;
          case "ignore":
            ignore(methodName);
            break;
          case "include":
            // nothing to do
            break;
          default:
            // Not RandoopBug because that isn't available here.
            throw new Error("Unrecognized action " + action + " in method spec: " + s);
        }
      }
    }
  }

  /**
   * Performs a coverage check for the given set of classes. Each declared method of a class that
   * does not satisfy {@link #isIgnoredMethod(String)} is checked for coverage. If the method occurs
   * in the excluded methods, then it must not be covered by any test. Otherwise, the method must be
   * covered by some test.
   *
   * @param regressionStatus the {@link TestRunStatus} from the regression tests
   * @param errorStatus the {@link TestRunStatus} from the error tests
   */
  void checkCoverage(TestRunStatus regressionStatus, TestRunStatus errorStatus) {

    int numCoveredMethods = 0;
    Set<String> missingMethods = new TreeSet<>();
    Set<String> shouldBeMissingMethods = new TreeSet<>();

    for (String classname : classnames) {
      String canonicalClassname = classname.replace('$', '.');

      Set<String> coveredMethods = new HashSet<>();
      coveredMethods.addAll(getCoveredMethodsForClass(regressionStatus, canonicalClassname));
      coveredMethods.addAll(getCoveredMethodsForClass(errorStatus, canonicalClassname));
      numCoveredMethods += coveredMethods.size();

      Class<?> c;
      try {
        c = Class.forName(classname);
      } catch (ClassNotFoundException | NoClassDefFoundError e) {
        fail("Could not load input class" + classname + ": " + e.getMessage());
        throw new Error("unreachable");
      }

      boolean firstLine = true;
      // Deterministic order is needed because of println within the loop.
      for (Method m : ClassDeterministic.getDeclaredMethods(c)) {
        String methodname = methodName(m);
        if (!isIgnoredMethod(methodname) && !dontCareMethods.contains(methodname)) {
          if (excludedMethods.contains(methodname)) {
            if (coveredMethods.contains(methodname)) {
              shouldBeMissingMethods.add(methodname);
            }
          } else {
            if (!coveredMethods.contains(methodname)) {
              missingMethods.add(methodname);
            }
          }
        } else {
          if (firstLine) {
            System.out.println();
            firstLine = false;
          }
          System.out.println("Ignoring " + methodname + " in coverage checks");
        }
      }
    }

    StringBuilder failureMessage = new StringBuilder();
    String totalCoveredMethodsMsg =
        String.format(
            "Covered %d methods, expected at least %d%n", numCoveredMethods, minMethodsToCover);
    if (numCoveredMethods < minMethodsToCover) {
      failureMessage.append(totalCoveredMethodsMsg);
    }
    if (!missingMethods.isEmpty()) {
      failureMessage.append(String.format("Expected methods not covered:%n"));
      for (String name : missingMethods) {
        failureMessage.append(String.format("  %s%n", name));
      }
    }
    if (!shouldBeMissingMethods.isEmpty()) {
      failureMessage.append(String.format("Excluded methods that are covered:%n"));
      for (String name : shouldBeMissingMethods) {
        failureMessage.append(String.format("  %s%n", name));
      }
    }
    if (regressionStatus == null) {
      System.out.printf("No regression tests.%n");
    } else {
      System.out.printf(
          "Ran %d tests, %d succeeded.%n",
          regressionStatus.testsRun, regressionStatus.testsSucceed);
    }
    String msg = failureMessage.toString();
    if (!msg.isEmpty()) {
      fail(msg);
    } else {
      System.out.println(totalCoveredMethodsMsg);
    }
  }

  /**
   * Returns the covered methods from the given class, as extracted from the {@link
   * MethodCoverageMap} of the given {@link TestRunStatus}.
   *
   * @param testRunStatus the {@link TestRunStatus}
   * @param classname the name of the class
   */
  private Set<String> getCoveredMethodsForClass(TestRunStatus testRunStatus, String classname) {
    if (testRunStatus != null) {
      Set<String> coveredMethods = testRunStatus.coverageMap.getMethods(classname);
      if (coveredMethods != null) {
        return coveredMethods;
      }
    }
    return Collections.emptySet();
  }

  /**
   * Constructs a method signature for a {@code java.lang.reflect.Method} object in a format that
   * matches the name construction in {@link MethodCoverageMap#getMethodName(JavaNames,
   * IClassCoverage, String, IMethodCoverage)}.
   *
   * @param m the {@code java.lang.reflect.Method} object
   * @return the method signature for the method object
   */
  private String methodName(Method m) {
    List<String> params = CollectionsPlume.mapList(Class::getCanonicalName, m.getParameterTypes());
    return m.getDeclaringClass().getCanonicalName()
        + "."
        + m.getName()
        + "("
        + StringsPlume.join(", ", params)
        + ")";
  }

  /**
   * Pattern for excluding method names from coverage checks. Excludes JaCoCo, Java private access
   * inner class methods, and hashCode().
   */
  private static final Pattern IGNORE_PATTERN =
      Pattern.compile("\\$jacocoInit|access\\$\\d+|(\\.hashCode\\(\\)$)");

  /**
   * Returns true if the given method name should be ignored during the coverage check.
   *
   * @param methodname the method name
   * @return true if the method should be ignored, false otherwise
   */
  private boolean isIgnoredMethod(String methodname) {
    Matcher matcher = IGNORE_PATTERN.matcher(methodname);
    return matcher.find();
  }
}
