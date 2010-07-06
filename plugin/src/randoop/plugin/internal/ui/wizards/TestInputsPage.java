package randoop.plugin.internal.ui.wizards;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import randoop.plugin.internal.ui.options.ClassSelectorOption;
import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.ProjectOption;

public class TestInputsPage extends WizardPage {
  
  private final IJavaProject fProject;
  
  private final IJavaElement[] fElements;

  private IOption fTestInputSelectorOption;

  private SelectionListener fBasicSelectionListener = new SelectionListener() {
    @Override
    public void widgetSelected(SelectionEvent e) {
      setErrorMessage(null);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      setErrorMessage(null);
    }
  };
  
  protected TestInputsPage(String pageName, IJavaProject project, IJavaElement[] elements) {
    super(pageName);
    
    setTitle("Test Inputs");
    setPageComplete(false);
    
    fProject = project;
    fElements = elements;
  }
  
  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(comp);

    fTestInputSelectorOption = new ClassSelectorOption(comp, getWizard()
        .getContainer(), fBasicSelectionListener, fProject);
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
