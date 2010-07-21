package randoop.plugin.internal.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;

public class RandoopLaunchConfigurationWizard extends Wizard {
  protected static final String DIALOG_SETTINGS_KEY = "RandoopWizard"; //$NON-NLS-1$

  ILaunchConfigurationWorkingCopy fConfig;
  OptionWizardPage fMainPage;
  OptionWizardPage fTestInputsPage;

  public RandoopLaunchConfigurationWizard(IJavaProject project, IJavaElement[] elements, ILaunchConfigurationWorkingCopy config) throws CoreException {
    super();
    
    fConfig = config;
    
    // Set the project in the configuration
    // RandoopArgumentCollector.setProjectName(fConfig,
    // project.getElementName());
    //
    // // Set the available and selected types in the configurations
    // List<String> availableTypes = new ArrayList<String>();
    // List<String> selectedTypes = new ArrayList<String>();
    //
    // for (IJavaElement element : elements) {
    // switch (element.getElementType()) {
    // case IJavaElement.PACKAGE_FRAGMENT_ROOT:
    // case IJavaElement.PACKAGE_FRAGMENT:
    // for (IType type : RandoopLaunchConfigurationUtil.findTypes(element,
    // false, null)) {
    // TypeMnemonic mnemonic = new TypeMnemonic(type);
    // availableTypes.add(mnemonic.toString());
    // }
    // break;
    // case IJavaElement.COMPILATION_UNIT:
    // for (IType type : RandoopLaunchConfigurationUtil.findTypes(element,
    // false, null)) {
    // TypeMnemonic mnemonic = new TypeMnemonic(type);
    // availableTypes.add(mnemonic.toString());
    // selectedTypes.add(mnemonic.toString());
    // }
    // break;
    // default:
    //        RandoopPlugin.log(StatusFactory.createErrorStatus("Unexpected Java element type: " //$NON-NLS-1$
    // + element.getElementType()));
    // return;
    // }
    // }
    //
    // RandoopArgumentCollector.setAvailableTypes(fConfig, availableTypes);
    // RandoopArgumentCollector.setSelectedMethods(fConfig, selectedTypes);
    
    fMainPage = new MainPage("Main", project, fConfig);
    fTestInputsPage = new TestInputsPage("Test Inputs", project, elements, fConfig);
    fTestInputsPage.setPreviousPage(fMainPage);
    
    addPage(fMainPage);
    addPage(fTestInputsPage);

    setNeedsProgressMonitor(true);
    setHelpAvailable(true);

    setTitleBarColor(new RGB(167, 215, 250));
    setWindowTitle("New Randoop Launch Configuration");
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
