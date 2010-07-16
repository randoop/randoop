package randoop.plugin.internal.ui.wizards;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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

public class TestInputsPage extends OptionWizardPage {
  
  private IOption fTestInputSelectorOption;
  
  private IJavaProject fJavaProject;
  private IJavaElement[] fElements;

  protected TestInputsPage(String pageName, IJavaProject project, IJavaElement[] elements, ILaunchConfigurationWorkingCopy config) {
    super(pageName, config);
    
    setTitle("Test Inputs");
    setPageComplete(false);

    fJavaProject = project;
    fElements = elements;
  }
  
  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(comp);

    fTestInputSelectorOption = new ClassSelectorOption(comp, getWizard().getContainer(), getSelectionListener(), fJavaProject, fElements);
    
    addOption(fTestInputSelectorOption);
    
    super.createControl(parent);
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public void performHelp() {
  }

}
