package randoop.plugin.internal.core.launching;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.TestKinds;

public class IRandoopLaunchConfigurationConstants {
  
  public static final String ID_RANDOOP_TEST_GENERATION = "randoop.plugin.core.launching.gentestsType"; //$NON-NLS-1$
  
  public static final String ATTR_PORT = RandoopPlugin.getPluginId() + ".PORT"; //$NON-NLS-1$
  
  public static final String ATTR_RANDOM_SEED = RandoopPlugin.getPluginId() + ".RANDOM_SEED"; //$NON-NLS-1$
  
  public static final String ATTR_MAXIMUM_TEST_SIZE = RandoopPlugin.getPluginId() + ".MAXIMUM_TEST_SIZE"; //$NON-NLS-1$
  
  public static final String ATTR_USE_THREADS = RandoopPlugin.getPluginId() + ".USE_THREADS"; //$NON-NLS-1$
  
  public static final String ATTR_THREAD_TIMEOUT = RandoopPlugin.getPluginId() + ".THREAD_TIMEOUT"; //$NON-NLS-1$
  
  public static final String ATTR_USE_NULL = RandoopPlugin.getPluginId() + ".USE_NULL"; //$NON-NLS-1$
  
  public static final String ATTR_NULL_RATIO = RandoopPlugin.getPluginId() + ".NULL_RATIO"; //$NON-NLS-1$
  
  public static final String ATTR_JUNIT_TEST_INPUTS = RandoopPlugin.getPluginId() + ".JUNIT_TEST_INPUTS"; //$NON-NLS-1$
  
  public static final String ATTR_TIME_LIMIT = RandoopPlugin.getPluginId() + ".TIME_LIMIT"; //$NON-NLS-1$
  
  public static final String ATTR_JUNIT_PACKAGE_NAME = RandoopPlugin.getPluginId() + ".JUNIT_PACKAGE_NAME"; //$NON-NLS-1$
  
  public static final String ATTR_JUNIT_CLASS_NAME = RandoopPlugin.getPluginId() + ".JUNIT_CLASS_NAME"; //$NON-NLS-1$
  
  public static final String ATTR_TEST_KINDS = RandoopPlugin.getPluginId() + ".TEST_KINDS"; //$NON-NLS-1$
  
  public static final String ATTR_MAXIMUM_TESTS_WRITTEN = RandoopPlugin.getPluginId() + ".MAXIMUM_TESTS_WRITTEN"; //$NON-NLS-1$
  
  public static final String ATTR_MAXIMUM_TESTS_PER_FILE = RandoopPlugin.getPluginId() + ".MAXIMUM_TESTS_PER_FILE"; //$NON-NLS-1$
  
  public static final String ATTR_PROJECT_NAME = RandoopPlugin.getPluginId() + ".PROJECT_NAME"; //$NON-NLS-1$
  
  public static final String ATTR_OUTPUT_DIRECTORY_NAME = RandoopPlugin.getPluginId() + ".OUTPUT_DIRECTORY"; //$NON-NLS-1$
  
  public static final String ATTR_USE_LOCAL_TEMP_FOLDER = RandoopPlugin.getPluginId() +  ".USE_LOCAL_TEMP_FOLDER"; //$NON-NLS-1$
  
  public static final String ATTR_AVAILABLE_TYPES = RandoopPlugin.getPluginId() + ".AVAILABLE_TYPES"; //$NON-NLS-1$
  
  public static final String ATTR_SELECTED_TYPES = RandoopPlugin.getPluginId() + ".SELECTED_TYPES"; //$NON-NLS-1$
  
  public static final String ATTR_AVAILABLE_METHODS = RandoopPlugin.getPluginId() + ".AVAILABLE_METHODS"; //$NON-NLS-1$
  
  public static final String ATTR_SELECTED_METHODS = RandoopPlugin.getPluginId() + ".SELECTED_METHODS"; //$NON-NLS-1$
  
  public static final String DEFAULT_RANDOM_SEED = "0"; //$NON-NLS-1$
  
  public static final String DEFAULT_MAXIMUM_TEST_SIZE = "100"; //$NON-NLS-1$
  
  public static final String DEFAULT_USE_THREADS = "true"; //$NON-NLS-1$
  
  public static final String DEFAULT_THREAD_TIMEOUT = "5000"; //$NON-NLS-1$
  
  public static final String DEFAULT_USE_NULL = "false"; //$NON-NLS-1$
  
  public static final String DEFAULT_NULL_RATIO = ""; //$NON-NLS-1$
  
  public static final String DEFAULT_JUNIT_TEST_INPUTS = "100000000"; //$NON-NLS-1$
  
  public static final String DEFAULT_TIME_LIMIT = "100"; //$NON-NLS-1$
  
  public static final String DEFAULT_JUNIT_PACKAGE_NAME = ""; //$NON-NLS-1$
  
  public static final String DEFAULT_JUNIT_CLASS_NAME = "randoop.RandoopTest"; //$NON-NLS-1$
  
  public static final String DEFAULT_MAXIMUM_TESTS_WRITTEN = "100000000"; //$NON-NLS-1$
  
  public static final String DEFAULT_MAXIMUM_TESTS_PER_FILE = "500"; //$NON-NLS-1$
  
  public static final String DEFAULT_TEST_KINDS = TestKinds.ALL.getArgumentName();

  public static final String DEFAULT_PROJECT = ""; //$NON-NLS-1$
  
  public static final String DEFAULT_OUTPUT_DIRECTORY_NAME = "test"; //$NON-NLS-1$

  public static final String DEFUALT_USE_LOCAL_TEMP_FOLDER = "false"; //$NON-NLS-1$

}
