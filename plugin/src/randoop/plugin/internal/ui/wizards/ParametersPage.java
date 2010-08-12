package randoop.plugin.internal.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.ui.ParametersSWTFactory;
import randoop.plugin.internal.ui.RandoopMessages;
import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.JUnitTestClassNameOption;
import randoop.plugin.internal.ui.options.ProjectOption;

public class ParametersPage extends OptionWizardPage {
  final int MARGIN = 5;
  final int INDENTATION = 5;
  final int VERTICAL_LABEL_SPACING = 7;
  final int VERTICAL_TEXT_SPACING = 5;
  
  private final IJavaProject fProject;
  
  private IOption fOutputFolderOption;
  private IOption fClassName;
  
  protected ParametersPage(String pageName, IJavaProject project, ILaunchConfigurationWorkingCopy config) {
    super(pageName, config);
    
    fProject = project;
    setTitle("Randoop Launch Configuration");
  }
  
  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(comp);
    
    createResourcesComposite(comp);
    createSeperator(comp);
    
    List<IOption> options = new ArrayList<IOption>();
    
    options.addAll(ParametersSWTFactory.createGenerationLimitComposite(comp, getBasicModifyListener()));
    createSeperator(comp);
    options.addAll(ParametersSWTFactory.createOutputParametersComposite(comp, getBasicModifyListener()));
    createSeperator(comp);
    options.addAll(ParametersSWTFactory.createAdvancedComposite(comp, getBasicModifyListener(), getBasicSelectionListener()));
    
    for (IOption option : options) {
      addOption(option);
    }
    
    restoreDefualts();
    
    super.createControl(parent);
  }

  private void createSeperator(Composite comp) {
    new Separator(SWT.SEPARATOR | SWT.HORIZONTAL | SWT.BORDER).doFillIntoGrid(
        comp, 1, convertHeightInCharsToPixels(1));
  }
  
  private void createResourcesComposite(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 3, 1, GridData.FILL_HORIZONTAL);
    
    SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_junit_output_dir, 1);
    Text outputSourceFolderText = SWTFactory.createSingleText(comp, 1);
    Button sourceFolderBrowseButton = SWTFactory.createPushButton(comp, "&Browse...", //$NON-NLS-1$
        null);

    fOutputFolderOption = new ProjectOption(getShell(), fProject,
        outputSourceFolderText, sourceFolderBrowseButton);
    
    SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_junit_package_name, 1);
    Text packageNameText = SWTFactory.createSingleText(comp, 2);
    
    SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_junit_classname, 1);
    Text classNameText = SWTFactory.createSingleText(comp, 2);
    
    fClassName = new JUnitTestClassNameOption(packageNameText, classNameText);
    
    addOption(fOutputFolderOption);
    addOption(fClassName);
    
    outputSourceFolderText.addModifyListener(getBasicModifyListener());
    packageNameText.addModifyListener(getBasicModifyListener());
    classNameText.addModifyListener(getBasicModifyListener());
    
  }

  @Override
  public String getName() {
    return ""; //$NON-NLS-1$
  }

  @Override
  public void performHelp() {
  }
  
}
