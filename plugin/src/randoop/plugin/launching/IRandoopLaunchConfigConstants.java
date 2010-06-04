package randoop.plugin.launching;

import java.util.ArrayList;
import java.util.List;

import randoop.plugin.RandoopActivator;

public class IRandoopLaunchConfigConstants {
  public static final String ATTR_PORT = "PORT";
  
  public static String EMPTY_STRING = ""; //$NON-NLS-1$

  public static String STR_ALL = "All";
  
  public static String STR_PASS = "Pass";
  
  public static String STR_FAIL = "Fail";
  
  public static final String ATTR_RANDOM_SEED = RandoopActivator.getPluginId() + ".RANDOM_SEED"; //$NON-NLS-1$
  
  public static final String ATTR_MAXIMUM_TEST_SIZE = RandoopActivator.getPluginId() + ".MAXIMUM_TEST_SIZE"; //$NON-NLS-1$
  
  public static final String ATTR_USE_THREADS = RandoopActivator.getPluginId() + ".USE_THREADS"; //$NON-NLS-1$
  
  public static final String ATTR_THREAD_TIMEOUT = RandoopActivator.getPluginId() + ".THREAD_TIMEOUT"; //$NON-NLS-1$
  
  public static final String ATTR_USE_NULL = RandoopActivator.getPluginId() + ".USE_NULL"; //$NON-NLS-1$
  
  public static final String ATTR_NULL_RATIO = RandoopActivator.getPluginId() + ".NULL_RATIO"; //$NON-NLS-1$
  
  public static final String ATTR_JUNIT_TEST_INPUTS = RandoopActivator.getPluginId() + ".JUNIT_TEST_INPUTS"; //$NON-NLS-1$
  
  public static final String ATTR_TIME_LIMIT = RandoopActivator.getPluginId() + ".TIME_LIMIT"; //$NON-NLS-1$
  
  public static final String ATTR_JUNIT_PACKAGE_NAME = RandoopActivator.getPluginId() + ".JUNIT_PACKAGE_NAME"; //$NON-NLS-1$
  
  public static final String ATTR_JUNIT_CLASS_NAME = RandoopActivator.getPluginId() + ".JUNIT_CLASS_NAME"; //$NON-NLS-1$
  
  public static final String ATTR_TEST_KINDS = RandoopActivator.getPluginId() + ".TEST_KINDS"; //$NON-NLS-1$
  
  public static final String ATTR_MAXIMUM_TESTS_WRITTEN = RandoopActivator.getPluginId() + ".MAXIMUM_TESTS_WRITTEN"; //$NON-NLS-1$
  
  public static final String ATTR_MAXIMUM_TESTS_PER_FILE = RandoopActivator.getPluginId() + ".MAXIMUM_TESTS_PER_FILE"; //$NON-NLS-1$
  
  public static final String ATTR_OUTPUT_DIRECTORY = RandoopActivator.getPluginId() + ".OUTPUT_DIRECTORY"; //$NON-NLS-1$
  
  public static final String ATTR_USE_LOCAL_TEMP_FOLDER = RandoopActivator.getPluginId() +  ".USE_LOCAL_TEMP_FOLDER"; //$NON-NLS-1$
  
  public static final String ATTR_TEMP_FOLDER = RandoopActivator.getPluginId() + ".TEMP_FOLDER"; //$NON-NLS-1$
  
  public static final String ATTR_ALL_JAVA_TYPES = RandoopActivator.getPluginId() + ".ALL_JAVA_TYPES"; //$NON-NLS-1$
  
  public static final String ATTR_CHECKED_JAVA_ELEMENTS = RandoopActivator.getPluginId() + ".CHECKED_JAVA_ELEMENTS"; //$NON-NLS-1$
  
  public static final String DEFAULT_RANDOM_SEED = "0"; //$NON-NLS-1$
  
  public static final String DEFAULT_MAXIMUM_TEST_SIZE = "100"; //$NON-NLS-1$
  
  public static final String DEFAULT_USE_THREADS = "true"; //$NON-NLS-1$
  
  public static final String DEFAULT_THREAD_TIMEOUT = "5000"; //$NON-NLS-1$
  
  public static final String DEFAULT_USE_NULL = "false"; //$NON-NLS-1$
  
  public static final String DEFAULT_NULL_RATIO = ""; //$NON-NLS-1$
  
  public static final String DEFAULT_JUNIT_TEST_INPUTS = "100000000"; //$NON-NLS-1$
  
  public static final String DEFAULT_TIME_LIMIT = "100"; //$NON-NLS-1$
  
  public static final String DEFAULT_JUNIT_PACKAGE_NAME = ""; //$NON-NLS-1$
  
  public static final String DEFAULT_JUNIT_CLASS_NAME = "RandoopTest"; //$NON-NLS-1$
  
  public static final String DEFAULT_MAXIMUM_TESTS_WRITTEN = "100000000"; //$NON-NLS-1$
  
  public static final String DEFAULT_MAXIMUM_TESTS_PER_FILE = "500"; //$NON-NLS-1$
  
  public static final String DEFAULT_TEST_KINDS = STR_ALL;

  public static final String DEFAULT_OUTPUT_DIRECTORY = ""; //$NON-NLS-1$

  public static final String DEFUALT_USE_LOCAL_TEMP_FOLDER = "false"; //$NON-NLS-1$

  public static final String DEFAULT_TEMP_FOLDER = "randoop-tmp/"; //$NON-NLS-1$
  
  public static final List<String> DEFAULT_ALL_JAVA_TYPES = new ArrayList<String>();

  public static final List<String> DEFAULT_CHECKED_JAVA_ELEMENTS = new ArrayList<String>();
}
