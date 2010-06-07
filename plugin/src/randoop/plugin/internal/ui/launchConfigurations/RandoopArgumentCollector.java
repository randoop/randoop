package randoop.plugin.internal.ui.launchConfigurations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.ui.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.ui.RandoopLaunchConfigurationUtil;

public class RandoopArgumentCollector {
  private List<IType> fCheckedTypes;
  private List<IMethod> fCheckedMethods;
  private int fRandomSeed;
  private int fMaxTestSize;
  private boolean fUseThreads;
  private int fThreadTimeout;
  private boolean fUseNull;
  private double fNullRatio;
  private int fJUnitTestInputs;
  private int fTimeLimit;
  private IPath fOutputDirectory;
  private String fJUnitPackageName;
  private String fJUnitClassName;
  private String fTestKinds;
  private int fMaxTestsWritten;
  private int fMaxTestsPerFile;

  public RandoopArgumentCollector(ILaunchConfiguration config) {
    fCheckedTypes = new ArrayList<IType>();
    fCheckedMethods = new ArrayList<IMethod>();

    List<?> checkedElements = getCheckedJavaElements(config);
    for (Object id : checkedElements) {
      if (id instanceof String) {
        IJavaElement element = JavaCore.create((String) id);
        if (element instanceof IType) {
          fCheckedTypes.add((IType) element);
        } else if (element instanceof IMethod) {
          fCheckedMethods.add((IMethod) element);
        }
      }
    }

    fRandomSeed = Integer.parseInt(getRandomSeed(config));
    fMaxTestSize = Integer.parseInt(getMaxTestSize(config));
    fUseThreads = getUseThreads(config);
    fThreadTimeout = Integer.parseInt(getThreadTimeout(config));
    fUseNull = getUseNull(config);
    fNullRatio = Double.parseDouble(getNullRatio(config));
    fJUnitTestInputs = Integer.parseInt(getJUnitTestInputs(config));
    fTimeLimit = Integer.parseInt(getTimeLimit(config));

    String outputSourceFolderHandlerId = getOutputDirectoryHandlerId(config);
    IPackageFragmentRoot outputDir = RandoopLaunchConfigurationUtil
        .getPackageFragmentRoot(outputSourceFolderHandlerId);
    if (outputDir != null) {
      fOutputDirectory = outputDir.getPath().makeAbsolute();
    }

    fJUnitPackageName = getJUnitPackageName(config);
    fJUnitClassName = getJUnitClassName(config);
    fTestKinds = getTestKinds(config);
    fMaxTestsWritten = Integer.parseInt(getMaxTestsWritten(config));
    fMaxTestsPerFile = Integer.parseInt(getMaxTestsPerFile(config));
  }

  public List<IType> getCheckedTypes() {
    return fCheckedTypes;
  }

  public List<IMethod> getCheckedMethods() {
    return fCheckedMethods;
  };

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

  public static List<String> getAllJavaTypes(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_ALL_JAVA_TYPES,
        IConstants.EMPTY_STRING_LIST);
  }

  public static List<String> getCheckedJavaElements(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_CHECKED_JAVA_ELEMENTS,
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
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS,
        Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS));
  }

  public static String getThreadTimeout(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT,
        IRandoopLaunchConfigurationConstants.DEFAULT_THREAD_TIMEOUT);
  }

  public static boolean getUseNull(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_USE_NULL, Boolean
            .parseBoolean(IRandoopLaunchConfigurationConstants.ATTR_USE_NULL));
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

  public static String getOutputDirectoryHandlerId(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY,
        IRandoopLaunchConfigurationConstants.DEFAULT_OUTPUT_DIRECTORY);
  }

  public static String getJUnitPackageName(ILaunchConfiguration config) {
    return getAttribute(config,
        IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME,
        IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_PACKAGE_NAME);
  }

  public static String getJUnitClassName(ILaunchConfiguration config) {
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
}
