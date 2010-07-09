package randoop.plugin.internal.core.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.ui.launching.RandoopLaunchConfigurationUtil;
import randoop.plugin.internal.ui.options.MethodMnemonics;

public class RandoopArgumentCollector {
  private String fName;
  private List<IType> fSelectedTypes;
  private List<IMethod> fSelectedMethods;
  private int fRandomSeed;
  private int fMaxTestSize;
  private boolean fUseThreads;
  private int fThreadTimeout;
  private boolean fUseNull;
  private double fNullRatio;
  private int fJUnitTestInputs;
  private int fTimeLimit;
  private IJavaProject fJavaProject;
  private IPath fOutputDirectory;
  private String fJUnitPackageName;
  private String fJUnitClassName;
  private String fTestKinds;
  private int fMaxTestsWritten;
  private int fMaxTestsPerFile;

  public RandoopArgumentCollector(ILaunchConfiguration config) {
    fName = config.getName();

    String projectName = getProjectName(config);
    fJavaProject = RandoopLaunchConfigurationUtil.getProjectFromName(projectName);

    fSelectedTypes = new ArrayList<IType>();
    List<?> selectedTypes = getSelectedTypes(config);
    for (Object o : selectedTypes) {
      Assert.isTrue(o instanceof String, "Non-String arguments stored in List"); //$NON-NLS-1$
      String fqname = (String) o;
      
      IProgressMonitor pm = new NullProgressMonitor();
      try {
        IType type = fJavaProject.findType(fqname, pm);

        fSelectedTypes.add(type);
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    
    fSelectedMethods = new ArrayList<IMethod>();
    List<?> selectedMethods = getSelectedMethods(config);
    for (Object o : selectedMethods) {
      Assert.isTrue(o instanceof String, "Non-String arguments stored in List"); //$NON-NLS-1$
      String mnemonic = (String) o;
      
      IMethod m = MethodMnemonics.getMethod(fJavaProject, mnemonic);
      Assert.isNotNull(m, "Stored method does not exist");
      Assert.isNotNull(m.exists(), "Stored method [" + m.getElementName()
          + "] does not exist");
      
      fSelectedMethods.add(m);
    }
    
    fRandomSeed = Integer.parseInt(getRandomSeed(config));
    fMaxTestSize = Integer.parseInt(getMaxTestSize(config));
    fUseThreads = getUseThreads(config);
    if (fUseThreads)
      fThreadTimeout = Integer.parseInt(getThreadTimeout(config));
    fUseNull = getUseNull(config);
    if (fUseNull)
      fNullRatio = Double.parseDouble(getNullRatio(config));
    fJUnitTestInputs = Integer.parseInt(getJUnitTestInputs(config));
    fTimeLimit = Integer.parseInt(getTimeLimit(config));
    
    String outputSourceFolderName = getOutputDirectoryName(config);
    IPackageFragmentRoot outputDir = RandoopLaunchConfigurationUtil.getPackageFragmentRoot(fJavaProject, outputSourceFolderName);
    if (outputDir != null) {
      fOutputDirectory = outputDir.getPath().makeRelative();
    }

    fJUnitPackageName = getJUnitPackageName(config);
    fJUnitClassName =  getJUnitClassName(config);
    
    fTestKinds = getTestKinds(config);
    fMaxTestsWritten = Integer.parseInt(getMaxTestsWritten(config));
    fMaxTestsPerFile = Integer.parseInt(getMaxTestsPerFile(config));
  }

  public Object getName() {
    return fName;
  }

  public List<IType> getSelectedTypes() {
    return fSelectedTypes;
  }

  public List<IMethod> getSelectedMethods() {
    return fSelectedMethods;
  }

  public int getRandomSeed() {
    return fRandomSeed;
  }

  public int getMaxTestSize() {
    return fMaxTestSize;
  }

  public boolean getUseThreads() {
    return fUseThreads;
  }

  public int getThreadTimeout() {
    return fThreadTimeout;
  }

  public boolean getUseNull() {
    return fUseNull;
  }

  public double getNullRatio() {
    return fNullRatio;
  }

  public int getJUnitTestInputs() {
    return fJUnitTestInputs;
  }

  public int getTimeLimit() {
    return fTimeLimit;
  }

  public IPath getOutputDirectory() {
    return fOutputDirectory;
  }

  public String getJUnitPackageName() {
    return fJUnitPackageName;
  }

  public String getJUnitClassName() {
    return fJUnitClassName;
  }

  public String getTestKinds() {
    return fTestKinds;
  }

  public int getMaxTestsWritten() {
    return fMaxTestsWritten;
  }

  public int getMaxTestsPerFile() {
    return fMaxTestsPerFile;
  }

  public static int getPort(ILaunchConfiguration config) {
    return getAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_PORT,
        IConstants.INVALID_PORT);
  }

  public static List<String> getAvailableTypes(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_AVAILABLE_TYPES,
        IConstants.EMPTY_STRING_LIST);
  }

  public static List<String> getSelectedTypes(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_SELECTED_TYPES,
        IConstants.EMPTY_STRING_LIST);
  }
  
  public static List<String> getSelectedMethods(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_SELECTED_METHODS,
        IConstants.EMPTY_STRING_LIST);
  }

  public static String getRandomSeed(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_RANDOM_SEED,
        IRandoopLaunchConfigurationConstants.DEFAULT_RANDOM_SEED);
  }

  public static String getMaxTestSize(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TEST_SIZE,
        IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TEST_SIZE);
  }

  public static boolean getUseThreads(ILaunchConfiguration config) {
    return getAttribute(
        config,
        IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS,
        Boolean
            .parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_THREADS));
  }

  public static String getThreadTimeout(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT,
        IRandoopLaunchConfigurationConstants.DEFAULT_THREAD_TIMEOUT);
  }

  public static boolean getUseNull(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_USE_NULL, Boolean
            .parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_NULL));
  }

  public static String getNullRatio(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO,
        IRandoopLaunchConfigurationConstants.DEFAULT_NULL_RATIO);
  }

  public static String getJUnitTestInputs(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_TEST_INPUTS,
        IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_TEST_INPUTS);
  }

  public static String getTimeLimit(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_TIME_LIMIT,
        IRandoopLaunchConfigurationConstants.DEFAULT_TIME_LIMIT);
  }

  public static String getProjectName(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_PROJECT_NAME,
        IRandoopLaunchConfigurationConstants.DEFAULT_PROJECT);
  }
  
  public static String getOutputDirectoryName(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY_NAME,
        IRandoopLaunchConfigurationConstants.DEFAULT_OUTPUT_DIRECTORY_NAME);
  }

  public static String getJUnitPackageName(
      ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME,
        IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_PACKAGE_NAME);
  }

  public static String getJUnitClassName(
      ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME,
        IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_CLASS_NAME);
  }
  
  public static String getTestKinds(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_TEST_KINDS,
        IRandoopLaunchConfigurationConstants.DEFAULT_TEST_KINDS);
  }

  public static String getMaxTestsWritten(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_WRITTEN,
        IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN);
  }

  public static String getMaxTestsPerFile(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_PER_FILE,
        IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE);
  }

  /*
   * Methods to restore default values
   */
  public static void restorePort(ILaunchConfigurationWorkingCopy config) {
    String nullStr = null;
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_PORT, nullStr);
  }
  
  public static void restoreAvailableTypes(ILaunchConfigurationWorkingCopy config) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_AVAILABLE_TYPES,
        IConstants.EMPTY_STRING_LIST);
  }

  public static void restoreSelectedTypes(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_SELECTED_TYPES,
        IConstants.EMPTY_STRING_LIST);
  }
  
  public static void restoreSelectedMethods(ILaunchConfigurationWorkingCopy config) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_SELECTED_METHODS,
        IConstants.EMPTY_STRING_LIST);
  }

  public static void restoreRandomSeed(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_RANDOM_SEED,
        IRandoopLaunchConfigurationConstants.DEFAULT_RANDOM_SEED);
  }

  public static void restoreMaxTestSize(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TEST_SIZE,
        IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TEST_SIZE);
  }

  public static void restoreUseThreads(ILaunchConfigurationWorkingCopy config) {
     setAttribute(
        config,
        IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS,
        Boolean
            .parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_THREADS));
  }

  public static void restoreThreadTimeout(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT,
        IRandoopLaunchConfigurationConstants.DEFAULT_THREAD_TIMEOUT);
  }

  public static void restoreUseNull(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_USE_NULL, Boolean
            .parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_NULL));
  }

  public static void restoreNullRatio(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO,
        IRandoopLaunchConfigurationConstants.DEFAULT_NULL_RATIO);
  }

  public static void restoreJUnitTestInputs(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_TEST_INPUTS,
        IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_TEST_INPUTS);
  }

  public static void restoreTimeLimit(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_TIME_LIMIT,
        IRandoopLaunchConfigurationConstants.DEFAULT_TIME_LIMIT);
  }

  public static void restoreProjectName(ILaunchConfigurationWorkingCopy config) {
    setAttribute(config,
       IRandoopLaunchConfigurationConstants.ATTR_PROJECT_NAME,
       IRandoopLaunchConfigurationConstants.DEFAULT_PROJECT);
 }
  
  public static void restoreOutputDirectoryName(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY_NAME,
        IRandoopLaunchConfigurationConstants.DEFAULT_OUTPUT_DIRECTORY_NAME);
  }

  public static void restoreJUnitPackageName(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME,
        IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_PACKAGE_NAME);
  }

  public static void restoreJUnitClassName(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME,
        IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_CLASS_NAME);
  }

  public static void restoreTestKinds(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_TEST_KINDS,
        IRandoopLaunchConfigurationConstants.DEFAULT_TEST_KINDS);
  }

  public static void restoreMaxTestsWritten(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_WRITTEN,
        IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN);
  }

  public static void restoreMaxTestsPerFile(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_PER_FILE,
        IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE);
  }


  /*
   * Methods to set ILaunchConfigurationWorkingCopy attributes 
   */
  public static void setPort(ILaunchConfigurationWorkingCopy config, int port) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_PORT, port);
  }
  
  public static void setAvailableTypes(ILaunchConfigurationWorkingCopy config, List<String> availableTypes) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_AVAILABLE_TYPES,
        availableTypes);
  }

  public static void setSelectedTypes(ILaunchConfigurationWorkingCopy config, List<String> selectedTypes) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_SELECTED_TYPES,
        selectedTypes);
  }
  
  public static void setSelectedMethods(ILaunchConfigurationWorkingCopy config, List<String> selectedMethods) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_SELECTED_METHODS,
        selectedMethods);
  }

  public static void setRandomSeed(ILaunchConfigurationWorkingCopy config, String seed) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_RANDOM_SEED,
        seed);
  }

  public static void setMaxTestSize(ILaunchConfigurationWorkingCopy config, String size) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TEST_SIZE, size);
  }

  public static void setUseThreads(ILaunchConfigurationWorkingCopy config, boolean useThreads) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS,
        useThreads);
  }

  public static void setThreadTimeout(ILaunchConfigurationWorkingCopy config, String threadTimeout) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT, threadTimeout);
  }

  public static void setUseNull(ILaunchConfigurationWorkingCopy config, boolean useNull) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_USE_NULL,
        useNull);
  }

  public static void setNullRatio(ILaunchConfigurationWorkingCopy config, String nullRatio) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO,
        nullRatio);
  }

  public static void setJUnitTestInputs(ILaunchConfigurationWorkingCopy config, String testInputs) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_TEST_INPUTS, testInputs);
  }

  public static void setTimeLimit(ILaunchConfigurationWorkingCopy config, String timeLimit) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_TIME_LIMIT,
        timeLimit);
  }
  
  public static void setProjectName(ILaunchConfigurationWorkingCopy config, String projectName) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
  }
  
  public static void setOutputDirectoryName(ILaunchConfigurationWorkingCopy config, String outputDirectory) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY_NAME,
        outputDirectory);
  }

  public static void setJUnitPackageName(ILaunchConfigurationWorkingCopy config, String junitPackageName) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME,
        junitPackageName);
  }

  public static void setJUnitClassName(ILaunchConfigurationWorkingCopy config, String junitClassName) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME,
        junitClassName);
  }

  public static void setTestKinds(ILaunchConfigurationWorkingCopy config, String testKinds) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_TEST_KINDS,
        testKinds);
  }

  public static void setMaxTestsWritten(ILaunchConfigurationWorkingCopy config, String maxTestsWritten) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_WRITTEN,
        maxTestsWritten);
  }

  public static void setMaxTestsPerFile(ILaunchConfigurationWorkingCopy config, String maxTestsPerFile) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_PER_FILE,
        maxTestsPerFile);
  }

  private static int getAttribute(ILaunchConfiguration config,
      String attributeName, int defaultValue) {
    try {
      return config.getAttribute(attributeName, defaultValue);
    } catch (CoreException ce) {
      return defaultValue;
    }
  }
  
  private static String getAttribute(ILaunchConfiguration config,
      String attributeName, String defaultValue) {
    try {
      return config.getAttribute(attributeName, defaultValue);
    } catch (CoreException ce) {
      return defaultValue;
    }
  }

  private static boolean getAttribute(ILaunchConfiguration config,
      String attributeName, boolean defaultValue) {
    try {
      return config.getAttribute(attributeName, defaultValue);
    } catch (CoreException ce) {
      return defaultValue;
    }
  }

  private static List<String> getAttribute(ILaunchConfiguration config,
      String attributeName, List<String> defaultValue) {
    try {
      return (List<String>) config.getAttribute(attributeName, defaultValue);
    } catch (CoreException ce) {
      return defaultValue;
    }
  }

  private static void setAttribute(ILaunchConfigurationWorkingCopy config,
      String attributeName, int value) {
    config.setAttribute(attributeName, value);
  }
  
  private static void setAttribute(ILaunchConfigurationWorkingCopy config,
      String attributeName, String value) {
    config.setAttribute(attributeName, value);
  }

  private static void setAttribute(ILaunchConfigurationWorkingCopy config,
      String attributeName, boolean value) {
    config.setAttribute(attributeName, value);
  }

  private static void setAttribute(ILaunchConfigurationWorkingCopy config,
      String attributeName, List<String> value) {
    config.setAttribute(attributeName, value);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RandoopArgumentCollector) {
      RandoopArgumentCollector other = (RandoopArgumentCollector) obj;

      return getName().equals(other.getName())
          && getSelectedTypes().equals(other.getSelectedTypes())
          && getSelectedMethods().equals(other.getSelectedMethods())
          && getRandomSeed() == other.getRandomSeed()
          && getMaxTestSize() == other.getMaxTestSize()
          && getUseThreads() == other.getUseThreads()
          && getThreadTimeout() == other.getThreadTimeout()
          && getUseNull() == other.getUseNull()
          && getNullRatio() == other.getNullRatio()
          && getJUnitTestInputs() == other.getJUnitTestInputs()
          && getTimeLimit() == other.getTimeLimit()
          && getOutputDirectory().equals(other.getOutputDirectory())
          && getJUnitPackageName().equals(other.getJUnitPackageName())
          && getJUnitClassName().equals(other.getJUnitClassName())
          && getTestKinds().equals(other.getTestKinds())
          && getMaxTestsWritten() == other.getMaxTestsWritten()
          && getMaxTestsPerFile() == other.getMaxTestsPerFile();
    }

    return super.equals(obj);
  }
  
  @Override
  public int hashCode() {
    return (getName() + getSelectedTypes().toString()
        + getSelectedMethods().toString() + getRandomSeed() + getMaxTestSize()
        + getUseThreads() + getThreadTimeout() + getUseNull() + getNullRatio()
        + getJUnitTestInputs() + getTimeLimit() + getOutputDirectory()
        + getJUnitPackageName() + getJUnitClassName() + getTestKinds()
        + getMaxTestsWritten() + getMaxTestsPerFile()).hashCode();
  }

}