package randoop.plugin.internal.ui.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

public class RandoopLaunchConfigurationWizard implements IWizard {
  IWizardContainer fWizardContainer;
  
  @Override
  public void addPages() {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean canFinish() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void createPageControls(Composite pageContainer) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void dispose() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public IWizardContainer getContainer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Image getDefaultPageImage() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IDialogSettings getDialogSettings() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IWizardPage getNextPage(IWizardPage page) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IWizardPage getPage(String pageName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getPageCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public IWizardPage[] getPages() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IWizardPage getPreviousPage(IWizardPage page) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IWizardPage getStartingPage() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RGB getTitleBarColor() {
    return new RGB(167, 215, 250);
  }

  @Override
  public String getWindowTitle() {
    return "New Randoop Launch Configuration";
  }

  @Override
  public boolean isHelpAvailable() {
    return false;
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return false;
  }

  @Override
  public boolean needsProgressMonitor() {
    return true;
  }

  @Override
  public boolean performCancel() {
    return false;
  }

  @Override
  public boolean performFinish() {
    return false;
  }

  @Override
  public void setContainer(IWizardContainer wizardContainer) {
    fWizardContainer = wizardContainer;
  }
}
