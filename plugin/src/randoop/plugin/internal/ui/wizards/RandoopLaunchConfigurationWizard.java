package randoop.plugin.internal.ui.wizards;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class RandoopLaunchConfigurationWizard extends Wizard implements IWorkbenchWizard {
  protected static final String DIALOG_SETTINGS_KEY = "RandoopWizard"; //$NON-NLS-1$

  IWizardPage fMainPage;
  IWizardPage fTestInputsPage;

  private IWorkbench fWorkbench;

  private IStructuredSelection fSelection;

  public RandoopLaunchConfigurationWizard() {
    super();

    fMainPage = new MainPage("Main");
    fTestInputsPage = new TestInputsPage("Test Inputs");
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

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    fWorkbench = workbench;
    fSelection = selection;
  }
}
