package randoop.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.JavaNames;

/**
 * Manages method coverage information for JaCoCo agent exec-file output from running a set of
 * Randoop generated tests. Includes all methods of classes covered by the test suite, and counter
 * information for each method.
 */
class MethodCoverageMap {

  /** The multi-map from classname to names of covered methods. */
  private Map<String, Set<String>> classMap;

  /** The map from method name to coverage counter information. */
  private Map<String, ICounter> counterMap;

  /** Creates an empty coverage map. */
  private MethodCoverageMap() {
    this.classMap = new LinkedHashMap<>();
    this.counterMap = new HashMap<>();
  }

  /**
   * Creates a coverage map from the the given JaCoCo exec file using class information from class
   * files in the given directory. Only includes methods with non-zero coverage.
   *
   * @param execFile the output of the JaCoCo javaagent
   * @param classesDirectory the root directory for the class files
   * @return the method coverage map for
   * @throws IOException if unable to load the exec file
   */
  static MethodCoverageMap collectCoverage(String execFile, Path classesDirectory)
      throws IOException {
    MethodCoverageMap coverageMap = new MethodCoverageMap();
    FileInputStream in = new FileInputStream(execFile);
    ExecFileLoader fileLoader = new ExecFileLoader();
    fileLoader.load(in);
    in.close();

    ExecutionDataStore dataStore = fileLoader.getExecutionDataStore();
    CoverageBuilder coverageBuilder = new CoverageBuilder();
    Analyzer analyzer = new Analyzer(dataStore, coverageBuilder);

    analyzer.analyzeAll(classesDirectory.toFile());
    JavaNames names = new JavaNames();
    for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {
      if (classCoverage.getMethodCounter().getCoveredCount() > 0) {
        String className = getClassName(names, classCoverage);
        for (IMethodCoverage methodCoverage : classCoverage.getMethods()) {
          String methodName = getMethodName(names, classCoverage, methodCoverage);
          ICounter methodCounter = methodCoverage.getMethodCounter();
          if (methodCounter.getCoveredCount() > 0) {
            coverageMap.put(className, methodName, methodCounter);
          }
        }
      }
    }
    return coverageMap;
  }

  /**
   * Return the set of names for covered methods of the class.
   *
   * @param classname the class name
   * @return the set of method names for covered methods
   */
  Set<String> getMethods(String classname) {
    Set<String> set = classMap.get(classname);
    if (set != null) {
      return set;
    }
    return Collections.emptySet();
  }

  private void put(String className, String methodName, ICounter methodCounter) {
    Set<String> set = classMap.get(className);
    if (set == null) {
      set = new LinkedHashSet<>();
    }
    set.add(methodName);
    classMap.put(className, set);
    this.counterMap.put(methodName, methodCounter);
  }

  /**
   * Constructs the string name of a method using JaCoCo classes.
   *
   * @param names the {@code JavaNames} object
   * @param classCoverage the {@code IClassCoverage} object
   * @param methodCoverage the {@code MethodCoverage} object
   * @return the method name
   */
  private static String getMethodName(
      JavaNames names, IClassCoverage classCoverage, IMethodCoverage methodCoverage) {
    return names.getQualifiedMethodName(
        classCoverage.getName(),
        methodCoverage.getName(),
        methodCoverage.getDesc(),
        methodCoverage.getSignature());
  }

  /**
   * Constructs the {@code String} name of a class using JaCoCo classes.
   *
   * @param names the {@code JavaNames} object
   * @param classCoverage the {@code IClassCoverage} object
   * @return the class name
   */
  private static String getClassName(JavaNames names, IClassCoverage classCoverage) {
    String classname =
        names.getClassName(
            classCoverage.getName(),
            classCoverage.getSignature(),
            classCoverage.getSuperName(),
            classCoverage.getInterfaceNames());
    String packageName = names.getPackageName(classCoverage.getPackageName());
    if (packageName.equals("default")) {
      return classname;
    } else {
      return packageName + "." + classname;
    }
  }
}
