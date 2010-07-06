package randoop.plugin.internal.ui.wizards;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class RandoopLaunchConfigurationWizard extends Wizard {
  protected static final String DIALOG_SETTINGS_KEY = "RandoopWizard"; //$NON-NLS-1$

  IWizardPage fMainPage;
  IWizardPage fTestInputsPage;

  private IWorkbench fWorkbench;

  private IStructuredSelection fSelection;

  public RandoopLaunchConfigurationWizard(IJavaProject project, IJavaElement[] elements) {
    super();

    fMainPage = new MainPage("Main", project);
    fTestInputsPage = new TestInputsPage("Test Inputs", project, elements);
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
    // TODO Auto-generated method stub
    return false;
  }

}
