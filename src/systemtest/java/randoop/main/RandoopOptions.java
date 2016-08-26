package randoop.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import plume.EntryReader;

/**
 * Manages the options for a run of Randoop within a system test method so that the information
 * can be used to setup test conditions.
 */
class RandoopOptions {

  /** The list of options used to call Randoop */
  private final List<String> options;

  /** The list of classnames for running Randoop */
  private final Set<String> classnames;

  /** The package name for Randoop generated test classes. */
  private String packageName;

  /** The basename for generated regression test classes */
  private String regressionBasename;

  /** The basename for generated error test classes */
  private String errorBasename;

  /**
   * Creates an empty set of options.
   */
  private RandoopOptions() {
    this.options = new ArrayList<>();
    this.classnames = new HashSet<>();
    this.packageName = "";
    this.regressionBasename = "";
    this.errorBasename = "";
  }

  /**
   * Creates an initial set of options based on the test environment consisting of the
   * output directory for generated tests, and log file location.
   *
   * @param testEnvironment  the test environment
   * @return a base set of Randoop options
   */
  static RandoopOptions createOptions(TestEnvironment testEnvironment) {
    RandoopOptions options = new RandoopOptions();
    options.setOption("junit-output-dir", testEnvironment.sourceDir.toString());
    options.setOption("log", testEnvironment.workingDir + "/randoop-log.txt");
    return options;
  }

  /**
   * Adds an option-value pair to this option set.
   *
   * @param option  the option name
   * @param value  the option value
   */
  void setOption(String option, String value) {
    options.add("--" + option + "=" + value);
  }

  /**
   * Adds a option-flag to this option set.
   *
   * @param option  the name of the option flag to be set
   */
  void setFlag(String option) {
    options.add("--" + option);
  }

  /**
   * Sets the package name for generated tests, and adds the option to this set.
   *
   * @param packageName  the package name
   */
  void setPackageName(String packageName) {
    if (packageName.length() > 0) {
      setOption("junit-package-name", packageName);
      this.packageName = packageName;
    }
  }

  /**
   * Sets the regression base name for generated tests, and adds the option to this set.
   *
   * @param regressionBasename  the regression basename
   */
  void setRegressionBasename(String regressionBasename) {
    if (regressionBasename.length() > 0) {
      setOption("regression-test-basename", regressionBasename);
      this.regressionBasename = regressionBasename;
    }
  }

  /**
   * Sets the error base name for generated tests, and adds the option to this set.
   *
   * @param errorBasename  the errorBasename
   */
  void setErrorBasename(String errorBasename) {
    if (errorBasename.length() > 0) {
      setOption("error-test-basename", errorBasename);
      this.errorBasename = errorBasename;
    }
  }

  /**
   * Adds a test class name to this option set.
   *
   * @param classname  the test class name
   */
  void addTestClass(String classname) {
    if (classname.length() > 0) {
      setOption("testclass", classname);
      classnames.add(classname);
    } else {
      throw new IllegalArgumentException("class name may not be empty string");
    }
  }

  /**
   * Returns the package name set for this option set.
   *
   * @return the package name, which may be null if not set
   */
  String getPackageName() {
    return packageName;
  }

  /**
   * Return the regression basename for this option set.
   *
   * @return the regression basename, which may be null if not set
   */
  String getRegressionBasename() {
    return regressionBasename;
  }

  /**
   * Return the error base name for this option set.
   *
   * @return the error base name, which may be null if not set
   */
  String getErrorBasename() {
    return errorBasename;
  }

  /**
   * Return this set of options as a list of strings.
   *
   * @return this option set as a list of {@code String}
   */
  List<String> getOptions() {
    return options;
  }

  /**
   * Adds a class list filename to this set of options.
   *
   * @param classListFilename the class list filename
   */
  void addClassList(String classListFilename) {
    if (classListFilename.length() > 0) {
      setOption("classlist", classListFilename);
      loadClassNames(classListFilename);
    } else {
      throw new IllegalArgumentException("class list name may not be empty string");
    }
  }

  /**
   * Reads the named class list file and adds the elements to the classnames in this options set.
   *
   * @param classListFilename  the class list filename
   */
  private void loadClassNames(String classListFilename) {
    try (EntryReader er = new EntryReader(classListFilename, "^#.*", null)) {
      for (String line : er) {
        String name = line.trim();
        if (!name.isEmpty()) {
          classnames.add(name);
        }
      }
    } catch (IOException e) {
      System.err.println("Failed to load class names: " + e.getMessage());
    }
  }

  /**
   * Returns the set of input class names in this options set.
   *
   * @return the set of names of input classes in this options set
   */
  Set<String> getClassnames() {
    return classnames;
  }
}
