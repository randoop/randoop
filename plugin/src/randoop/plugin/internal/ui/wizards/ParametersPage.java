package randoop.plugin.internal.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.ui.RandoopMessages;
import randoop.plugin.internal.ui.RandoopPluginImages;
import randoop.plugin.internal.ui.SWTFactory;
import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.JUnitTestClassNameOption;
import randoop.plugin.internal.ui.options.OptionFactory;
import randoop.plugin.internal.ui.options.OutputDirectoryOption;

public class ParametersPage extends OptionWizardPage {
  final int MARGIN = 5;
  final int INDENTATION = 5;
  final int VERTICAL_LABEL_SPACING = 7;
  final int VERTICAL_TEXT_SPACING = 5;
  
  private final IJavaProject fProject;
  
  private IOption fOutputFolderOption;
  private IOption fClassName;
  
  protected ParametersPage(String pageName, IJavaProject project, ILaunchConfigurationWorkingCopy config) {
    super(pageName, "Launch Configuration Parameters", RandoopPluginImages.DESC_WIZBAN_NEW_RNDP, config);
    
    fProject = project;
  }
  
  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(comp);
    
    createResourcesComposite(comp);
    SWTFactory.createSeperator(comp, 1);
    
    List<IOption> options = new ArrayList<IOption>();
    
    options.addAll(OptionFactory.createStoppingCriterionOptionGroup(comp, getBasicoptionChangeListener()));
    SWTFactory.createSeperator(comp, 1);
    options.addAll(OptionFactory.createOutputParametersOptionGroup(comp, getBasicoptionChangeListener()));
    SWTFactory.createSeperator(comp, 1);
    options.addAll(OptionFactory.createAdvancedOptionGroup(comp, getBasicoptionChangeListener()));
    
    for (IOption option : options) {
      addOption(option);
    }
  }
  
  private void createResourcesComposite(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 3, 1, GridData.FILL_HORIZONTAL);
    
    SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_junit_output_dir, 1);
    Text outputSourceFolderText = SWTFactory.createSingleText(comp, 1);
    Button sourceFolderBrowseButton = SWTFactory.createPushButton(comp, "&Browse...", //$NON-NLS-1$
        null);

    fOutputFolderOption = new OutputDirectoryOption(getShell(), fProject,
        outputSourceFolderText, sourceFolderBrowseButton);
    
    SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_junit_package_name, 1);
    Text packageNameText = SWTFactory.createSingleText(comp, 2);
    
    SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_junit_classname, 1);
    Text classNameText = SWTFactory.createSingleText(comp, 2);
    
    fClassName = new JUnitTestClassNameOption(packageNameText, classNameText);
    
    addOption(fOutputFolderOption);
    addOption(fClassName);
    
    fOutputFolderOption.addChangeListener(getBasicoptionChangeListener());
    fClassName.addChangeListener(getBasicoptionChangeListener());

  }

  @Override
  public String getName() {
    return ""; //$NON-NLS-1$
  }

  @Override
  public void performHelp() {
  }
  
}
