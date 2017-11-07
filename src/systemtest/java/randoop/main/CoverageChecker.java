package randoop.main;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

  /** The classes whose methods must be covered. */
  private final Set<String> classnames;

  /** The methods that must not be covered. */
  private final HashSet<String> excludedMethods;

  /** The methods whose coverage should be ignored. */
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
   * Create a coverage checker using the classnames from the option set. All other parts of the
   * options are ignored.
   *
   * @param options the options
   */
  CoverageChecker(RandoopOptions options) {
    this(options.getClassnames());
  }

  /**
   * Create a coverage checker using the classnames from the option set, and the given method
   * exclusions.
   *
   * @param options the options
   */
  CoverageChecker(RandoopOptions options, String... methodSpecs) {
    this(options.getClassnames());
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

  /**
   * Add method names to be excluded, ignored, or included (included has no effect).
   *
   * <p>Each string consists of a signature, a space, and one of the words "exclude", "ignore", or
   * "include". For example: "java2.util2.ArrayList.readObject(java.io.ObjectInputStream) exclude"
   *
   * <p>This format is intended to make it easy to sort the arguments.
   */
  void methods(String... methodSpecs) {
    for (String s : methodSpecs) {
      int spacepos = s.lastIndexOf(" ");
      if (spacepos == -1) {
        // Not BugInRandoopException because that isn't available here.
        throw new Error("Bad method spec: " + s);
      }
      String methodName = s.substring(0, spacepos);
      String action = s.substring(spacepos + 1);
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
          // Not BugInRandoopException because that isn't available here.
          throw new Error("Unrecognized action " + action + " in method spec: " + s);
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

    Set<String> missingMethods = new TreeSet<>();
    Set<String> shouldBeMissingMethods = new TreeSet<>();

    for (String classname : classnames) {
      String canonicalClassname = classname.replace('$', '.');

      Set<String> coveredMethods = new HashSet<>();
      coveredMethods.addAll(getCoveredMethodsForClass(regressionStatus, canonicalClassname));
      coveredMethods.addAll(getCoveredMethodsForClass(errorStatus, canonicalClassname));

      Class<?> c;
      try {
        c = Class.forName(classname);
      } catch (ClassNotFoundException e) {
        fail("Could not load input class" + classname + ": " + e.getMessage());
        throw new Error("unreachable");
      }

      boolean firstLine = true;
      // Deterministic order is needed because of println within the loop.
      for (Method m : getDeclaredMethods(c)) {
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
    String msg = failureMessage.toString();
    if (!msg.isEmpty()) {
      fail(msg);
    }
  }

  /**
   * Returns the covered methods from the given class, as extracted from the {@link
   * MethodCoverageMap} of the given {@link TestRunStatus}.
   *
   * @param testRunStatus the {@link TestRunStatus}
   * @param classname the name of the class
   * @param methods the set to which method names are added
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

  /**
   * Like {@link Class#getDeclaredMethods()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose declared methods to return
   * @return the class's declared methods
   */
  public static Method[] getDeclaredMethods(Class<?> c) {
    Method[] result = c.getDeclaredMethods();
    Arrays.sort(result, methodComparator);
    return result;
  }

  static ClassComparator classComparator = new ClassComparator();

  /** Compares Class objects by name. */
  private static class ClassComparator implements Comparator<Class<?>> {

    @Override
    public int compare(Class<?> c1, Class<?> c2) {
      return c1.getName().compareTo(c2.getName());
    }
  }

  static MethodComparator methodComparator = new MethodComparator();

  /**
   * Compares Method objcets by signature: compares name, number of parameters, and parameter type
   * names.
   */
  private static class MethodComparator implements Comparator<Method> {

    @Override
    public int compare(Method m1, Method m2) {
      int result = classComparator.compare(m1.getDeclaringClass(), m2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      result = m1.getName().compareTo(m2.getName());
      if (result != 0) {
        return result;
      }
      result = m1.getParameterTypes().length - m2.getParameterTypes().length;
      if (result != 0) {
        return result;
      }
      for (int i = 0; i < m1.getParameterTypes().length; i++) {
        result = classComparator.compare(m1.getParameterTypes()[i], m2.getParameterTypes()[i]);
        if (result != 0) {
          return result;
        }
      }
      return result;
    }
  }
}
