package randoop.plugin.internal.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public class MainPage extends WizardPage {

  protected MainPage(String pageName) {
    super(pageName);
    
    setTitle("Randoop Launch Configuration");
  }
  
  @Override
  public void createControl(Composite parent) {
    
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public void performHelp() {
  }

}
