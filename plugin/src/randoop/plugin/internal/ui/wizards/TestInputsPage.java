package randoop.plugin.internal.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TestInputsPage extends WizardPage {

  protected TestInputsPage(String pageName) {
    super(pageName);
    
    setTitle("Test Inputs");
    setPageComplete(false);
    // setPreviousPage(page);
  }
  
  @Override
  public void createControl(Composite parent) {
    
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public boolean isPageComplete() {
    return false;
  }

  @Override
  public void performHelp() {
  }

}
