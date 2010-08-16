package randoop.plugin.internal.ui.wizards;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import randoop.plugin.internal.ui.RandoopPluginImages;
import randoop.plugin.internal.ui.SWTFactory;
import randoop.plugin.internal.ui.options.ClassSelectorOption;
import randoop.plugin.internal.ui.options.IOption;

/**
 * 
 * @author Peter Kalauskas
 */
public class TestInputsPage extends OptionWizardPage {
  
  private IOption fTestInputSelectorOption;

  private IJavaProject fJavaProject;
  
  protected TestInputsPage(String pageName, IJavaProject project,
      ILaunchConfigurationWorkingCopy config) {

    super(pageName, "Classes and Methods Under Test", RandoopPluginImages.DESC_WIZBAN_NEW_RNDP, config);
    setPageComplete(false);

    fJavaProject = project;
  }

  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(comp);

    fTestInputSelectorOption = new ClassSelectorOption(comp, getWizard().getContainer(), fJavaProject);

    addOption(fTestInputSelectorOption);

    fTestInputSelectorOption.addChangeListener(getBasicoptionChangeListener());
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public void performHelp() {
  }

}
