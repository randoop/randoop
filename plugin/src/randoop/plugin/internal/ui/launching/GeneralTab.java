package randoop.plugin.internal.ui.launching;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.ui.SWTFactory;
import randoop.plugin.internal.ui.options.ClassSelectorOption;
import randoop.plugin.internal.ui.options.JUnitTestClassNameOption;
import randoop.plugin.internal.ui.options.OutputDirectoryOption;
import randoop.plugin.internal.ui.options.ProjectOption;

public class GeneralTab extends OptionLaunchConfigurationTab {
  private ProjectOption fProjectOption;
  private OutputDirectoryOption fOutputDirectory;
  private JUnitTestClassNameOption fJUnitTestClassNameOption;
  private ClassSelectorOption fTestInputSelectorOption;
  
  public GeneralTab() {
    addOption(new ProjectOption());
    addOption(new OutputDirectoryOption());
    addOption(new JUnitTestClassNameOption());
    addOption(new ClassSelectorOption());
  }
  
  public void createControl(Composite parent) {
    removeAllOptions();
    
    Composite tabcomp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(tabcomp);

    // Project group:
    Composite comp = SWTFactory.createComposite(tabcomp, 3, 1, GridData.FILL_HORIZONTAL);

    SWTFactory.createLabel(comp, "Pro&ject:", 1);

    Text projectText = new Text(comp, SWT.SINGLE | SWT.BORDER);
    projectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Button projectBrowseButton = SWTFactory.createPushButton(comp, "Browse...", null);
    projectBrowseButton.setText("&Browse...");

    SWTFactory.createLabel(comp, "&Output Folder:", 1);
    Text outputSourceFolderText = SWTFactory.createSingleText(comp, 1);

    Button sourceFolderBrowseButton = SWTFactory.createPushButton(comp, "&Search...", null);

    fProjectOption = new ProjectOption(getShell(), projectText, projectBrowseButton);

    fOutputDirectory = new OutputDirectoryOption(getShell(), null,
        outputSourceFolderText, sourceFolderBrowseButton);

    // Class name option:
    comp = SWTFactory.createComposite(tabcomp, 3, 1, GridData.FILL_HORIZONTAL);

    SWTFactory.createLabel(comp, "JUnit Class Na&me:", 1);
    Text fullyQualifiedTestName = SWTFactory.createSingleText(comp, 2);

    fJUnitTestClassNameOption = new JUnitTestClassNameOption(fullyQualifiedTestName);

    // Test inputs option:
    fTestInputSelectorOption = new ClassSelectorOption(tabcomp, getLaunchConfigurationDialog());

    fProjectOption.addChangeListener(fTestInputSelectorOption);

    addOption(fProjectOption);
    addOption(fOutputDirectory);
    addOption(fJUnitTestClassNameOption);
    addOption(fTestInputSelectorOption);

    fProjectOption.addChangeListener(getBasicOptionChangeListener());
    
    // Add the outputdir option as a change listener to the project
    // option so it knows when the project changes
    fProjectOption.addChangeListener(fOutputDirectory);
    fOutputDirectory.addChangeListener(getBasicOptionChangeListener());
    fJUnitTestClassNameOption.addChangeListener(getBasicOptionChangeListener());
    fTestInputSelectorOption.addChangeListener(getBasicOptionChangeListener());
    
  }

  public String getName() {
    return "&General";
  }
  
  @Override
  public Image getImage() {
    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
  }

}
