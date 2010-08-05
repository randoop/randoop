package randoop.plugin.internal.ui.wizards;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.ui.options.ClassSelectorOption;
import randoop.plugin.internal.ui.options.IOption;

public class TestInputsPage extends OptionWizardPage {
  
  private IOption fTestInputSelectorOption;

  private IJavaProject fJavaProject;
  private List<TypeMnemonic> fTypes;
  private Map<TypeMnemonic, List<MethodMnemonic>> fMethodsByDeclaringTypes;

  protected TestInputsPage(String pageName, IJavaProject project, List<TypeMnemonic> types,
      Map<TypeMnemonic, List<MethodMnemonic>> methodsByDeclaringTypes, ILaunchConfigurationWorkingCopy config) {
    super(pageName, config);

    setTitle("Test Inputs");
    setPageComplete(false);

    fJavaProject = project;
    fTypes = types;
    fMethodsByDeclaringTypes = methodsByDeclaringTypes;
  }
  
  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(comp);

    fTestInputSelectorOption = new ClassSelectorOption(comp, getWizard().getContainer(), getBasicSelectionListener(), fJavaProject, fTypes, fMethodsByDeclaringTypes);
    
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
