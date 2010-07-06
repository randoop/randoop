package randoop.plugin.internal.ui.wizards;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class RandoopLaunchConfigurationWizard extends Wizard {
  protected static final String DIALOG_SETTINGS_KEY = "RandoopWizard"; //$NON-NLS-1$

  ILaunchConfigurationWorkingCopy fConfig;
  OptionWizardPage fMainPage;
  OptionWizardPage fTestInputsPage;

  public RandoopLaunchConfigurationWizard(IJavaProject project, IJavaElement[] elements, ILaunchConfigurationWorkingCopy config) throws CoreException {
    super();
    
    fConfig = config;
    
    RandoopArgumentCollector.setProjectHandlerId(fConfig, project.getHandleIdentifier());
    
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
