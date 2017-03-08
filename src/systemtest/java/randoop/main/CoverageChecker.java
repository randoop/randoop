package randoop.main;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.report.JavaNames;
import plume.UtilMDE;

/** Checks coverage for a test, managing information needed to perform the coverage checks. */
class CoverageChecker {

  /** The classes to check for coverage */
  private final Set<String> classnames;

  /** The methods that must not be covered */
  private final HashSet<String> excludedMethods;

  /** The methods whose coverage should be ignored */
  private final HashSet<String> dontCareMethods;

  /**
   * Create a coverage checker for the set of class names.
   *
   * @param classnames the class name set
   */
  private CoverageChecker(Set<String> classnames) {
    this.classnames = classnames;
    this.excludedMethods = new HashSet<>();
    this.dontCareMethods = new HashSet<>();
  }

  /**
   * Create a coverage checker using the classnames from the option set.
   *
   * @param options the options
   */
  CoverageChecker(RandoopOptions options) {
    this(options.getClassnames());
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

  /**
   * Performs a coverage check for the given set of classes relative to the full set of tests. Each
   * declared method of a class that does not satisfy {@link #isIgnoredMethod(String)} is checked
   * for coverage. If the method occurs in the excluded methods, then it must not be covered by any
   * test. Otherwise, the method must be covered by some test.
   *
   * @param regressionStatus the {@link TestRunStatus} from the regression tests
   * @param errorStatus the {@link TestRunStatus} from the error tests
   */
  void checkCoverage(TestRunStatus regressionStatus, TestRunStatus errorStatus) {

    Set<String> missingMethods = new TreeSet<>();
    Set<String> shouldBeMissingMethods = new TreeSet<>();

    for (String classname : classnames) {
      Set<String> methods = new HashSet<>();

      String canonicalClassname = classname.replace('$', '.');
      getCoveredMethodsForClass(regressionStatus, canonicalClassname, methods);
      getCoveredMethodsForClass(errorStatus, canonicalClassname, methods);

      Class<?> c;
      try {
        c = Class.forName(classname);

        boolean firstLine = true;
        for (Method m : c.getDeclaredMethods()) {
          String methodname = methodName(m);
          if (!isIgnoredMethod(methodname) && !dontCareMethods.contains(methodname)) {
            if (excludedMethods.contains(methodname)) {
              if (methods.contains(methodname)) {
                shouldBeMissingMethods.add(methodname);
              }
            } else {
              if (!methods.contains(methodname)) {
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
      } catch (ClassNotFoundException e) {
        fail("Could not load input class" + classname + ": " + e.getMessage());
      }
    }

    if (!missingMethods.isEmpty()) {
      String msg = String.format("Expected methods not covered:%n");
      for (String name : missingMethods) {
        msg += String.format("  %s%n", name);
      }
      fail(msg);
    }
    if (!shouldBeMissingMethods.isEmpty()) {
      String msg = String.format("Excluded methods that are covered:%n");
      for (String name : shouldBeMissingMethods) {
        msg += String.format("  %s%n", name);
      }
      fail(msg);
    }
  }

  /**
   * Adds methods from the given class to the set if they are covered in the {@link
   * MethodCoverageMap} of the given {@link TestRunStatus}.
   *
   * @param testRunStatus the {@link TestRunStatus}
   * @param classname the name of the class
   * @param methods the set to which method names are added
   */
  private void getCoveredMethodsForClass(
      TestRunStatus testRunStatus, String classname, Set<String> methods) {
    if (testRunStatus != null) {
      Set<String> coveredMethods = testRunStatus.coverageMap.getMethods(classname);
      if (coveredMethods != null) {
        methods.addAll(coveredMethods);
      }
    }
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
    List<String> params = new ArrayList<>();
    for (Class<?> paramType : m.getParameterTypes()) {
      params.add(paramType.getCanonicalName());
    }
    return m.getDeclaringClass().getCanonicalName()
        + "."
        + m.getName()
        + "("
        + UtilMDE.join(params, ", ")
        + ")";
  }

  /**
   * Pattern for excluding method names from coverage checks. Excludes JaCoCo, and Java private
   * access inner class methods.
   */
  private static final Pattern IGNORE_PATTERN = Pattern.compile("\\$jacocoInit|access\\$\\d{3}+");

  /**
   * Indicates whether the given method name should be ignored during the coverage check.
   *
   * @param methodname the method name
   * @return true if the method should be ignored, false otherwise
   */
  private boolean isIgnoredMethod(String methodname) {
    Matcher matcher = IGNORE_PATTERN.matcher(methodname);
    return matcher.find();
  }
}
