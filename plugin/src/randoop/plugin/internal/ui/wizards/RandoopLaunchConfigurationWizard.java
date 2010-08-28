package randoop.plugin.internal.ui.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.RandoopStatus;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.preferences.RandoopPreferences;

/**
 * 
 * @author Peter Kalauskas
 */
public class RandoopLaunchConfigurationWizard extends Wizard {
  protected static final String DIALOG_SETTINGS_KEY = "RandoopWizard"; //$NON-NLS-1$

  ILaunchConfigurationWorkingCopy fConfig;
  IJavaProject fJavaProject;
  OptionWizardPage fMainPage;
  OptionWizardPage fTestInputsPage;

  public RandoopLaunchConfigurationWizard(IJavaProject javaProject,
      List<String> checkedTypes, List<String> grayedTypes,
      Map<String, List<String>> selectedMethodsByDeclaringTypes,
      ILaunchConfigurationWorkingCopy config) {

    super();

    fConfig = config;
    fJavaProject = javaProject;

    RandoopArgumentCollector.setProjectName(config, fJavaProject.getElementName());

    Set<String> availableTypesSet = new HashSet<String>();
    availableTypesSet.addAll(checkedTypes);
    availableTypesSet.addAll(grayedTypes);
    
    List<String> availableTypes = new ArrayList<String>();
    availableTypes.addAll(availableTypesSet);

    RandoopArgumentCollector.saveClassTree(config, availableTypes, checkedTypes,
        grayedTypes, null, null, selectedMethodsByDeclaringTypes);
    
    IPreferenceStore store = RandoopPlugin.getDefault().getPreferenceStore();
    if (RandoopPreferences.doRememberParameters(store)) {
      int location = RandoopPreferences.getParameterStorageLocation(store);
      switch(location) {
      case RandoopPreferences.WORKSPACE:
        
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_RANDOM_SEED);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TEST_SIZE);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_USE_NULL);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_INPUT_LIMIT);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_TIME_LIMIT);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY_NAME);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_TEST_KINDS);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_WRITTEN);
        setConfigAttribute(store, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_PER_FILE);
        
        break;
      case RandoopPreferences.PROJECT:
        IProject project = fJavaProject.getProject();
        IScopeContext projectScope = new ProjectScope(project);
        Preferences prefs = (Preferences) projectScope.getNode(RandoopPlugin.getPluginId());
        
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_RANDOM_SEED, IRandoopLaunchConfigurationConstants.DEFAULT_RANDOM_SEED);
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TEST_SIZE, IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TEST_SIZE);
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS, Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_THREADS)); 
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT, IRandoopLaunchConfigurationConstants.DEFAULT_THREAD_TIMEOUT);
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_USE_NULL, Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_NULL));
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO, IRandoopLaunchConfigurationConstants.DEFAULT_NULL_RATIO);
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_INPUT_LIMIT, IRandoopLaunchConfigurationConstants.DEFAULT_INPUT_LIMIT); 
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_TIME_LIMIT, IRandoopLaunchConfigurationConstants.DEFAULT_TIME_LIMIT);
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY_NAME, IRandoopLaunchConfigurationConstants.DEFAULT_OUTPUT_DIRECTORY_NAME); 
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME,IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_PACKAGE_NAME);
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME, IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_CLASS_NAME);
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_TEST_KINDS, IRandoopLaunchConfigurationConstants.DEFAULT_TEST_KINDS);
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_WRITTEN, IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN);
        setConfigAttribute(prefs, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_PER_FILE,IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE);
        
        break;
      }
    }
    
    fTestInputsPage = new TestInputsPage("Test Inputs", fJavaProject, fConfig);
    fMainPage = new ParametersPage("Main", fJavaProject, fConfig);
    fMainPage.setPreviousPage(fMainPage);
    
    fMainPage.setDefaults(fConfig);

    addPage(fTestInputsPage);
    addPage(fMainPage);
    
    setTitleBarColor(new RGB(167, 215, 250));
    setWindowTitle("New Randoop Launch Configuration");
  }
  
  @Override
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);

    fTestInputsPage.initializeFrom(fConfig);
    fMainPage.initializeFrom(fConfig);

    setNeedsProgressMonitor(true);
    setHelpAvailable(false);
  }

  @Override
  public boolean performFinish() {
    if(!fMainPage.isValid(fConfig)) {
      return false;
    }
    
    if(!fTestInputsPage.isValid(fConfig)) {
      return false;
    }
    
    IPreferenceStore store = RandoopPlugin.getDefault().getPreferenceStore();
    if (RandoopPreferences.doRememberParameters(store)) {
      int location = RandoopPreferences.getParameterStorageLocation(store);
      switch(location) {
      case RandoopPreferences.WORKSPACE:        
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_RANDOM_SEED, IRandoopLaunchConfigurationConstants.DEFAULT_RANDOM_SEED);
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TEST_SIZE, IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TEST_SIZE);
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS, Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_THREADS)); 
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT, IRandoopLaunchConfigurationConstants.DEFAULT_THREAD_TIMEOUT);
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_USE_NULL, Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_NULL));
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO, IRandoopLaunchConfigurationConstants.DEFAULT_NULL_RATIO);
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_INPUT_LIMIT, IRandoopLaunchConfigurationConstants.DEFAULT_INPUT_LIMIT); 
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_TIME_LIMIT, IRandoopLaunchConfigurationConstants.DEFAULT_TIME_LIMIT);
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY_NAME, IRandoopLaunchConfigurationConstants.DEFAULT_OUTPUT_DIRECTORY_NAME); 
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME,IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_PACKAGE_NAME);
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME, IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_CLASS_NAME);
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_TEST_KINDS, IRandoopLaunchConfigurationConstants.DEFAULT_TEST_KINDS);
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_WRITTEN, IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN);
        setValueFromConfig(store, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_PER_FILE,IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE);
        
        break;
      case RandoopPreferences.PROJECT:
        IProject project = fJavaProject.getProject();
        IScopeContext projectScope = new ProjectScope(project);
        Preferences prefs = (Preferences) projectScope.getNode(RandoopPlugin
            .getPluginId());
        
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_RANDOM_SEED, IRandoopLaunchConfigurationConstants.DEFAULT_RANDOM_SEED);
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TEST_SIZE, IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TEST_SIZE);
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS, Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_THREADS)); 
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT, IRandoopLaunchConfigurationConstants.DEFAULT_THREAD_TIMEOUT);
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_USE_NULL, Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_NULL));
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO, IRandoopLaunchConfigurationConstants.DEFAULT_NULL_RATIO);
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_INPUT_LIMIT, IRandoopLaunchConfigurationConstants.DEFAULT_INPUT_LIMIT); 
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_TIME_LIMIT, IRandoopLaunchConfigurationConstants.DEFAULT_TIME_LIMIT);
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY_NAME, IRandoopLaunchConfigurationConstants.DEFAULT_OUTPUT_DIRECTORY_NAME); 
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME,IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_PACKAGE_NAME);
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME, IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_CLASS_NAME);
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_TEST_KINDS, IRandoopLaunchConfigurationConstants.DEFAULT_TEST_KINDS);
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_WRITTEN, IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN);
        setValueFromConfig(prefs, IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_PER_FILE,IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE);
        
        try {
          prefs.flush();
        } catch (BackingStoreException e) {
          // TODO: What do we do here?
        }
        
        break;
      }
    }
    
    return true;
  }
  
  private void setConfigAttribute(Preferences prefs, String attributeName, String def) {
    fConfig.setAttribute(attributeName, prefs.get(attributeName, def));
  }
  
  private void setConfigAttribute(Preferences prefs, String attributeName, boolean def) {
    fConfig.setAttribute(attributeName, prefs.getBoolean(attributeName, def));
  }
  
  private void setConfigAttribute(IPreferenceStore store, String attributeName) {
    fConfig.setAttribute(attributeName, store.getString(attributeName));
  }
  
  private void setValueFromConfig(Preferences prefs, String attributeName, String def) {
    try {
      prefs.put(attributeName, fConfig.getAttribute(attributeName, def));
    } catch (CoreException e) {
      prefs.put(attributeName, def);
    }
  }
  
  private void setValueFromConfig(Preferences prefs, String attributeName, boolean def) {
    try {
      prefs.putBoolean(attributeName, fConfig.getAttribute(attributeName, def));
    } catch (CoreException e) {
      prefs.putBoolean(attributeName, def);
    }
  }
  
  private void setValueFromConfig(IPreferenceStore store, String attributeName, String def) {
    try {
      store.setValue(attributeName, fConfig.getAttribute(attributeName, def));
    } catch (CoreException e) {
      store.setValue(attributeName, def);
    }
  }
  
  private void setValueFromConfig(IPreferenceStore store, String attributeName, boolean def) {
    try {
      store.setValue(attributeName, fConfig.getAttribute(attributeName, def));
    } catch (CoreException e) {
      store.setValue(attributeName, def);
    }
  }
  
}
