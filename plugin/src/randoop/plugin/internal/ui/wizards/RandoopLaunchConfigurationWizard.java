package randoop.plugin.internal.ui.wizards;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;

import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.TypeMnemonic;

public class RandoopLaunchConfigurationWizard extends Wizard {
  protected static final String DIALOG_SETTINGS_KEY = "RandoopWizard"; //$NON-NLS-1$

  ILaunchConfigurationWorkingCopy fConfig;
  OptionWizardPage fMainPage;
  OptionWizardPage fTestInputsPage;

  public RandoopLaunchConfigurationWizard(IJavaProject javaProject, List<TypeMnemonic> types,
      Map<TypeMnemonic, List<String>> methodsByDeclaringTypes,
      Map<TypeMnemonic, List<String>> availableByDeclaringTypes,
      ILaunchConfigurationWorkingCopy config) throws CoreException {
    
    super();

    fConfig = config;

    fTestInputsPage = new TestInputsPage("Test Inputs", javaProject, types,
        methodsByDeclaringTypes, availableByDeclaringTypes, fConfig);
    fMainPage = new ParametersPage("Main", javaProject, fConfig);
    fMainPage.setPreviousPage(fMainPage);

    addPage(fTestInputsPage);
    addPage(fMainPage);

    setNeedsProgressMonitor(true);
    setHelpAvailable(false);

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
