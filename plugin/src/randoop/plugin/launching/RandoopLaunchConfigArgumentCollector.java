package randoop.plugin.launching;

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
import org.eclipse.swt.widgets.TreeItem;

public class RandoopLaunchConfigArgumentCollector {
  private List<IType> checkedTypes;
  private List<IMethod> checkedMethods;
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

  public RandoopLaunchConfigArgumentCollector(ILaunchConfiguration config) {
    checkedTypes = new ArrayList<IType>();
    checkedMethods = new ArrayList<IMethod>();
    
    List<String> checkedElements = getCheckedJavaElements(config);
    for (String id : checkedElements) {
      IJavaElement element = JavaCore.create(id);
      if (element instanceof IType) {
        checkedTypes.add((IType) element);
      } else if (element instanceof IMethod) {
        checkedMethods.add((IMethod) element);
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
    IPackageFragmentRoot outputDir = RandoopLaunchConfigUtil.getPackageFragmentRoot(outputSourceFolderHandlerId);
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
    return checkedTypes;
  }

  public List<IMethod> getCheckedMethods() {
    return checkedMethods;};
  
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

  @SuppressWarnings("unchecked")
  public static List<String> getAllJavaTypes(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_ALL_JAVA_TYPES,
          IRandoopLaunchConfigConstants.EMPTY_LIST);
    } catch (CoreException e) {
      return IRandoopLaunchConfigConstants.EMPTY_LIST;
    }
  }

  @SuppressWarnings("unchecked")
  public static List<String> getCheckedJavaElements(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_CHECKED_JAVA_ELEMENTS,
          IRandoopLaunchConfigConstants.EMPTY_LIST);
    } catch (CoreException e) {
      return IRandoopLaunchConfigConstants.EMPTY_LIST;
    }
  }

  public static String getRandomSeed(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_RANDOM_SEED,
          IRandoopLaunchConfigConstants.DEFAULT_RANDOM_SEED);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_RANDOM_SEED;
    }
  }

  public static String getMaxTestSize(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TEST_SIZE,
          IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TEST_SIZE);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TEST_SIZE;
    }
  }

  public static boolean getUseThreads(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_USE_THREADS, Boolean
              .parseBoolean(IRandoopLaunchConfigConstants.ATTR_USE_THREADS));
    } catch (CoreException ce) {
      return Boolean
          .parseBoolean(IRandoopLaunchConfigConstants.DEFAULT_USE_THREADS);
    }
  }

  public static String getThreadTimeout(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_THREAD_TIMEOUT,
          IRandoopLaunchConfigConstants.DEFAULT_THREAD_TIMEOUT);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_THREAD_TIMEOUT;
    }
  }

  public static boolean getUseNull(ILaunchConfiguration config) {
    try {
      return config.getAttribute(IRandoopLaunchConfigConstants.ATTR_USE_NULL,
          Boolean.parseBoolean(IRandoopLaunchConfigConstants.ATTR_USE_NULL));
    } catch (CoreException ce) {
      return Boolean
          .parseBoolean(IRandoopLaunchConfigConstants.DEFAULT_USE_NULL);
    }
  }

  public static String getNullRatio(ILaunchConfiguration config) {
    try {
      return config.getAttribute(IRandoopLaunchConfigConstants.ATTR_NULL_RATIO,
          IRandoopLaunchConfigConstants.DEFAULT_NULL_RATIO);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_NULL_RATIO;
    }
  }

  public static String getJUnitTestInputs(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_JUNIT_TEST_INPUTS,
          IRandoopLaunchConfigConstants.DEFAULT_JUNIT_TEST_INPUTS);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_JUNIT_TEST_INPUTS;
    }
  }

  public static String getTimeLimit(ILaunchConfiguration config) {
    try {
      return config.getAttribute(IRandoopLaunchConfigConstants.ATTR_TIME_LIMIT,
          IRandoopLaunchConfigConstants.DEFAULT_TIME_LIMIT);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_TIME_LIMIT;
    }
  }

  public static String getOutputDirectoryHandlerId(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_OUTPUT_DIRECTORY,
          IRandoopLaunchConfigConstants.DEFAULT_OUTPUT_DIRECTORY);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_OUTPUT_DIRECTORY;
    }
  }

  public static String getJUnitPackageName(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_JUNIT_PACKAGE_NAME,
          IRandoopLaunchConfigConstants.DEFAULT_JUNIT_PACKAGE_NAME);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_JUNIT_PACKAGE_NAME;
    }
  }

  public static String getJUnitClassName(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_JUNIT_CLASS_NAME,
          IRandoopLaunchConfigConstants.DEFAULT_JUNIT_CLASS_NAME);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_JUNIT_CLASS_NAME;
    }
  }

  public static String getTestKinds(ILaunchConfiguration config) {
    try {
      return config.getAttribute(IRandoopLaunchConfigConstants.ATTR_TEST_KINDS,
          IRandoopLaunchConfigConstants.DEFAULT_TEST_KINDS);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_TEST_KINDS;
    }
  }

  public static String getMaxTestsWritten(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_WRITTEN,
          IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN;
    }
  }

  public static String getMaxTestsPerFile(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_PER_FILE,
          IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE;
    }
  }
}
