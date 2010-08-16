package randoop.plugin.internal.ui.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

/**
 * 
 * @author Peter Kalauskas
 */
public class RandoopLaunchConfigurationWizard extends Wizard {
  protected static final String DIALOG_SETTINGS_KEY = "RandoopWizard"; //$NON-NLS-1$

  ILaunchConfigurationWorkingCopy fConfig;
  OptionWizardPage fMainPage;
  OptionWizardPage fTestInputsPage;

  public RandoopLaunchConfigurationWizard(IJavaProject javaProject,
      List<String> checkedTypes, List<String> grayedTypes,
      Map<String, List<String>> selectedMethodsByDeclaringTypes,
      ILaunchConfigurationWorkingCopy config) {

    super();

    fConfig = config;

    RandoopArgumentCollector.setProjectName(config, javaProject.getElementName());

    Set<String> availableTypesSet = new HashSet<String>();
    availableTypesSet.addAll(checkedTypes);
    availableTypesSet.addAll(grayedTypes);
    
    List<String> availableTypes = new ArrayList<String>();
    availableTypes.addAll(availableTypesSet);

    RandoopArgumentCollector.saveClassTree(config, availableTypes, checkedTypes,
        grayedTypes, null, null, selectedMethodsByDeclaringTypes);
    
    fTestInputsPage = new TestInputsPage("Test Inputs", javaProject, fConfig);
    fMainPage = new ParametersPage("Main", javaProject, fConfig);
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
    
    return true;
  }
  
}
