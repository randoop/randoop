package randoop.plugin.internal.core.launching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.RandoopCoreUtil;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.TypeMnemonic;

public class RandoopArgumentCollector {
  private static final List<String> EMPTY_STRING_LIST = new ArrayList<String>();
  
  private String fName;
  private ArrayList<IType> fAvailableTypes;
  private List<IType> fSelectedTypes;
  private Map<IType, List<IMethod>> fSelectedMethodsByType;
  private int fRandomSeed;
  private int fMaxTestSize;
  private boolean fUseThreads;
  private int fThreadTimeout;
  private boolean fUseNull;
  private double fNullRatio;
  private int fInputLimit;
  private int fTimeLimit;
  private IJavaProject fJavaProject;
  private IPath fOutputDirectory;
  private String fJUnitPackageName;
  private String fJUnitClassName;
  private String fTestKinds;
  private int fMaxTestsWritten;
  private int fMaxTestsPerFile;

  
  public RandoopArgumentCollector(ILaunchConfiguration config, IWorkspaceRoot root) throws JavaModelException {
    fName = config.getName();
    Assert.isNotNull(fName, "Configuration name not given"); //$NON-NLS-1$
    
    String projectName = getProjectName(config);
    fJavaProject = RandoopCoreUtil.getProjectFromName(projectName);
    Assert.isNotNull(fJavaProject, "Java project not specified"); //$NON-NLS-1$

    fSelectedTypes = new ArrayList<IType>();
    fAvailableTypes = new ArrayList<IType>();
    List<?> availableTypes = getAvailableTypes(config);
    List<?> selectedTypes = getCheckedTypes(config);
    for (Object o : availableTypes) {
      Assert.isTrue(o instanceof String, "Non-String arguments stored in class-input list"); //$NON-NLS-1$
      String mnemonic = (String) o;
      
      TypeMnemonic typeMnemonic = new TypeMnemonic(mnemonic, root);
      IType type = typeMnemonic.getType();
      
      Assert.isTrue(fJavaProject.equals(type.getJavaProject()), "One of the selected class inputs is not associated with the selected project"); //$NON-NLS-1$

      fAvailableTypes.add(type);
      if (selectedTypes.contains(o)) {
        fSelectedTypes.add(type);
      }
    }
    
    fSelectedMethodsByType = new HashMap<IType, List<IMethod>>();
    for (IType type : fSelectedTypes) {
      List<IMethod> methodList = new ArrayList<IMethod>();
      List<?> selectedMethods = getCheckedMethods(config, new TypeMnemonic(type).toString());
      for (Object o : selectedMethods) {
        Assert.isTrue(o instanceof String, "Non-String arguments stored in method-input list"); //$NON-NLS-1$
        String mnemonic = (String) o;
        
        IMethod m = new MethodMnemonic(mnemonic).findMethod(type);
        Assert.isNotNull(m, "Stored method does not exist"); //$NON-NLS-1$
        Assert.isNotNull(m.exists(), "Stored method [" + m.getElementName() + "] does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.isTrue(fJavaProject.equals(m.getJavaProject()), "One of the selected method-input's declaring type is not associated with the selected project"); //$NON-NLS-1$
        
        methodList.add(m);
      }
      
      fSelectedMethodsByType.put(type, methodList);
    }
    
    Assert.isTrue(!fSelectedTypes.isEmpty() || !fSelectedMethodsByType.isEmpty(), "No class input or method input given"); //$NON-NLS-1$

    fRandomSeed = Integer.parseInt(getRandomSeed(config));
    fMaxTestSize = Integer.parseInt(getMaxTestSize(config));
    fUseThreads = getUseThreads(config);
    if (fUseThreads)
      fThreadTimeout = Integer.parseInt(getThreadTimeout(config));
    fUseNull = getUseNull(config);
    if (fUseNull)
      fNullRatio = Double.parseDouble(getNullRatio(config));
    fInputLimit = Integer.parseInt(getInputLimit(config));
    fTimeLimit = Integer.parseInt(getTimeLimit(config));

    String outputSourceFolderName = getOutputDirectoryName(config);
    fOutputDirectory = fJavaProject.getPath().append(outputSourceFolderName).makeAbsolute();
    Assert.isNotNull(fOutputDirectory, "Output directory not specified"); //$NON-NLS-1$

    fJUnitPackageName = getJUnitPackageName(config);
    Assert.isNotNull(fJUnitPackageName, "JUnit package name not given"); //$NON-NLS-1$

    fJUnitClassName = getJUnitClassName(config);
    Assert.isNotNull(fJUnitClassName, "JUnit class name not given"); //$NON-NLS-1$

    fTestKinds = getTestKinds(config);
    Assert.isNotNull(fTestKinds, "Test kinds not specified"); //$NON-NLS-1$

    fMaxTestsWritten = Integer.parseInt(getMaxTestsWritten(config));
    fMaxTestsPerFile = Integer.parseInt(getMaxTestsPerFile(config));
  }
  
  public IStatus checkForConflicts() {
    IJavaProject javaProject = getJavaProject();
    
    for (IType type : getSelectedTypes()) {
      String fqname = type.getFullyQualifiedName().replace('$', '.');
      
      try {
        if (!type.equals(javaProject.findType(fqname, (IProgressMonitor) null))) {
          return StatusFactory.createWarningStatus("One of the selected classes has a class with an identical fully-qualified name in the project's classpath that has priority and will be tested instead of the selected class.");
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    
    for (IType type : fSelectedMethodsByType.keySet()) {
      String fqname = type.getFullyQualifiedName().replace('$', '.');
      
      try {
        if (!type.equals(javaProject.findType(fqname, (IProgressMonitor) null))) {
          return StatusFactory.createErrorStatus("One of the selected method's declaring class has a class with an identical fully-qualified name in the project's classpath that has priority and will be tested instead of the selected method's declaring class.");
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    
    return StatusFactory.OK_STATUS;
  }

  public String getName() {
    return fName;
  }
  
  public IJavaProject getJavaProject() {
    return fJavaProject;
  }

  public List<IType> getSelectedTypes() {
    return fSelectedTypes;
  }

  public Map<IType, List<IMethod>> getSelectedMethodsByType() {
    return fSelectedMethodsByType;
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

  public int getInputLimit() {
    return fInputLimit;
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
        EMPTY_STRING_LIST);
  }
  
  public static List<String> getGrayedTypes(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_GRAYED_TYPES,
        EMPTY_STRING_LIST);
  }

  public static List<String> getCheckedTypes(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_CHECKED_TYPES,
        EMPTY_STRING_LIST);
  }

  public static List<String> getAvailableMethods(ILaunchConfiguration config, String typeMnemonic) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_AVAILABLE_METHODS_PREFIX + typeMnemonic,
        EMPTY_STRING_LIST);
  }
  
  public static List<String> getCheckedMethods(ILaunchConfiguration config, String typeMnemonic) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_CHECKED_METHODS_PREFIX + typeMnemonic,
        EMPTY_STRING_LIST);
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
        Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_THREADS));
  }

  public static String getThreadTimeout(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT,
        IRandoopLaunchConfigurationConstants.DEFAULT_THREAD_TIMEOUT);
  }

  public static boolean getUseNull(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_USE_NULL,
        Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_NULL));
  }

  public static String getNullRatio(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO,
        IRandoopLaunchConfigurationConstants.DEFAULT_NULL_RATIO);
  }

  public static String getInputLimit(ILaunchConfiguration config) {
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
        EMPTY_STRING_LIST);
  }

  public static void restoreGrayedTypes(ILaunchConfigurationWorkingCopy config) {
    setAttribute(config,
       IRandoopLaunchConfigurationConstants.ATTR_GRAYED_TYPES,
       EMPTY_STRING_LIST);
 }
  
  public static void restoreCheckedTypes(ILaunchConfigurationWorkingCopy config) {
     setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_CHECKED_TYPES,
        EMPTY_STRING_LIST);
  }
  
  public static void deleteAvailableMethods(ILaunchConfigurationWorkingCopy config, String typeMnemonic) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_AVAILABLE_METHODS_PREFIX + typeMnemonic,
        (String) null);
  }
  
  public static void deleteCheckedMethods(ILaunchConfigurationWorkingCopy config, String typeMnemonic) {
    setAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_CHECKED_METHODS_PREFIX + typeMnemonic,
        (String) null);
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

  public static void restoreInputLimit(ILaunchConfigurationWorkingCopy config) {
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
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_AVAILABLE_TYPES, availableTypes);
  }

  public static void setGrayedTypes(ILaunchConfigurationWorkingCopy config, List<String> grayedTypes) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_GRAYED_TYPES, grayedTypes);
  }
  
  public static void setCheckedTypes(ILaunchConfigurationWorkingCopy config, List<String> checkedTypes) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_CHECKED_TYPES, checkedTypes);
  }

  public static void setAvailableMethods(ILaunchConfigurationWorkingCopy config, String typeMnemonic, List<String> availableMethods) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_AVAILABLE_METHODS_PREFIX + typeMnemonic, availableMethods);
  }

  public static void setCheckedMethods(ILaunchConfigurationWorkingCopy config, String typeMnemonic, List<String> selectedMethods) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_CHECKED_METHODS_PREFIX + typeMnemonic, selectedMethods);
  }

  public static void setRandomSeed(ILaunchConfigurationWorkingCopy config, String seed) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_RANDOM_SEED, seed);
  }

  public static void setMaxTestSize(ILaunchConfigurationWorkingCopy config, String size) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TEST_SIZE, size);
  }

  public static void setUseThreads(ILaunchConfigurationWorkingCopy config, boolean useThreads) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS, useThreads);
  }

  public static void setThreadTimeout(ILaunchConfigurationWorkingCopy config, String threadTimeout) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT, threadTimeout);
  }

  public static void setUseNull(ILaunchConfigurationWorkingCopy config, boolean useNull) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_USE_NULL, useNull);
  }

  public static void setNullRatio(ILaunchConfigurationWorkingCopy config, String nullRatio) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO, nullRatio);
  }

  public static void setInputLimit(ILaunchConfigurationWorkingCopy config, String testInputs) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_TEST_INPUTS, testInputs);
  }

  public static void setTimeLimit(ILaunchConfigurationWorkingCopy config, String timeLimit) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_TIME_LIMIT, timeLimit);
  }

  public static void setProjectName(ILaunchConfigurationWorkingCopy config, String projectName) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
  }

  public static void setOutputDirectoryName(ILaunchConfigurationWorkingCopy config, String outputDirectory) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY_NAME, outputDirectory);
  }

  public static void setJUnitPackageName(ILaunchConfigurationWorkingCopy config, String junitPackageName) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME, junitPackageName);
  }

  public static void setJUnitClassName(ILaunchConfigurationWorkingCopy config, String junitClassName) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME, junitClassName);
  }

  public static void setTestKinds(ILaunchConfigurationWorkingCopy config, String testKinds) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_TEST_KINDS, testKinds);
  }

  public static void setMaxTestsWritten(ILaunchConfigurationWorkingCopy config, String maxTestsWritten) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_WRITTEN, maxTestsWritten);
  }

  public static void setMaxTestsPerFile(ILaunchConfigurationWorkingCopy config, String maxTestsPerFile) {
    setAttribute(config, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_PER_FILE, maxTestsPerFile);
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

  @SuppressWarnings("unchecked")
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
          && getSelectedMethodsByType().equals(other.getSelectedMethodsByType())
          && getRandomSeed() == other.getRandomSeed()
          && getMaxTestSize() == other.getMaxTestSize()
          && getUseThreads() == other.getUseThreads()
          && getThreadTimeout() == other.getThreadTimeout()
          && getUseNull() == other.getUseNull()
          && getNullRatio() == other.getNullRatio()
          && getInputLimit() == other.getInputLimit()
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
        + getSelectedMethodsByType().toString() + getRandomSeed() + getMaxTestSize()
        + getUseThreads() + getThreadTimeout() + getUseNull() + getNullRatio()
        + getInputLimit() + getTimeLimit() + getOutputDirectory()
        + getJUnitPackageName() + getJUnitClassName() + getTestKinds()
        + getMaxTestsWritten() + getMaxTestsPerFile()).hashCode();
  }

}
