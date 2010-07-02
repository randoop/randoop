package randoop.plugin.internal.ui.wizards;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class MainPage extends WizardPage {

  protected MainPage(String pageName) {
    super(pageName);
    
    setTitle("Randoop Launch Configuration");
  }
  
  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(comp);    
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public void performHelp() {
  }

}
