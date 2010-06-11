package randoop.plugin.launching;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.ui.SWTFactory;

public class RandoopLaunchConfigOutputTab extends
    AbstractLaunchConfigurationTab {
  private Text fOutputDirectory;
  private IPackageFragmentRoot fOutputSourceFolder;
  private Button fSourceFolderBrowse;

  private Text fJUnitPackageName;
  private Text fJUnitClassName;
  private Combo fTestKinds;
  private Text fMaxTestsWritten;
  private Text fMaxTestsPerFile;
  
  private ModifyListener fBasicModifyListener = new RandoopTabListener();

  private class RandoopTabListener extends SelectionAdapter implements
      ModifyListener {
    public void modifyText(ModifyEvent e) {
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
   */
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 3, 1,
        GridData.FILL_HORIZONTAL);
    setControl(comp);

    SWTFactory.createLabel(comp, "Output Directory:", 1);
    fOutputDirectory = SWTFactory.createSingleText(comp, 1);
    fOutputDirectory.setEditable(false);
    fOutputDirectory.setText(IRandoopLaunchConfigConstants.DEFAULT_OUTPUT_DIRECTORY);

    fSourceFolderBrowse = SWTFactory.createPushButton(comp, "Browse...", null);
    fSourceFolderBrowse.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IPackageFragmentRoot chosenFolder = chooseSourceFolder();

        if (chosenFolder != null) {
          fOutputSourceFolder = chosenFolder;
          fOutputDirectory.setText(fOutputSourceFolder.getPath().makeRelative().toString());
        }

        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });

    SWTFactory.createLabel(comp, "JUnit Package Name:", 1);
    fJUnitPackageName = SWTFactory.createSingleText(comp, 2);
    fJUnitPackageName.addModifyListener(fBasicModifyListener);

    SWTFactory.createLabel(comp, "JUnit Class Name:", 1);
    fJUnitClassName = SWTFactory.createSingleText(comp, 2);
    fJUnitClassName.addModifyListener(fBasicModifyListener);

    SWTFactory.createLabel(comp, "Test &Kinds:", 1);
    fTestKinds = SWTFactory.createCombo(comp, SWT.READ_ONLY, 2, new String[] {
        "All", "Pass", "Fail" });

    SWTFactory.createLabel(comp, "Maximum Tests &Written:", 1);
    fMaxTestsWritten = SWTFactory.createSingleText(comp, 2);
    fMaxTestsWritten.addModifyListener(fBasicModifyListener);

    SWTFactory.createLabel(comp, "Maximum Tests Per &File:", 1);
    fMaxTestsPerFile = SWTFactory.createSingleText(comp, 2);
    fMaxTestsPerFile.addModifyListener(fBasicModifyListener);
  }

  @Override
  public boolean canSave() {
    setErrorMessage(null);
  
    if (fOutputDirectory == null || fSourceFolderBrowse == null
        || fJUnitPackageName == null || fJUnitClassName == null
        || fTestKinds == null || fMaxTestsWritten == null
        || fMaxTestsPerFile == null || fJUnitPackageName == null
        || fJUnitClassName == null || fMaxTestsWritten == null
        || fMaxTestsPerFile == null) {
      return false;
    }
  
    if (fOutputSourceFolder == null) {
      setErrorMessage("Output Directory is not a valid source folder");
      return false;
    }
  
    String outputSourceFolderHandlerId = fOutputSourceFolder.getHandleIdentifier();
    String junitPackageName = fJUnitPackageName.getText();
    String junitClassname = fJUnitClassName.getText();
    String testKinds = fTestKinds.getText();
    String maxTestsWritten = fMaxTestsWritten.getText();
    String maxTestsPerFile = fMaxTestsPerFile.getText();
  
    IStatus status = validate(outputSourceFolderHandlerId, junitPackageName,
        junitClassname, testKinds, maxTestsWritten, maxTestsPerFile);
    if (status.isOK()) {
      return true;
    } else {
      setErrorMessage(status.getMessage());
      return false;
    }
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration config) {
    String outputSourceFolderHandlerId = RandoopLaunchConfigArgumentCollector.getOutputDirectoryHandlerId(config);
    String junitPackageName = RandoopLaunchConfigArgumentCollector.getJUnitPackageName(config);
    String junitClassname = RandoopLaunchConfigArgumentCollector.getJUnitClassName(config);
    String testKinds = RandoopLaunchConfigArgumentCollector.getTestKinds(config);
    String maxTestsWritten = RandoopLaunchConfigArgumentCollector.getMaxTestsWritten(config);
    String maxTestsPerFile = RandoopLaunchConfigArgumentCollector.getMaxTestsPerFile(config);
    
    return validate(outputSourceFolderHandlerId, junitPackageName, junitClassname, testKinds, maxTestsWritten, maxTestsPerFile).isOK();
  };

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fOutputDirectory != null && fOutputSourceFolder != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_OUTPUT_DIRECTORY,
          fOutputSourceFolder.getHandleIdentifier());
    if (fJUnitPackageName != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_JUNIT_PACKAGE_NAME, fJUnitPackageName.getText());
    if (fJUnitClassName != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_JUNIT_CLASS_NAME, fJUnitClassName.getText());
    if (fTestKinds != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_TEST_KINDS, fTestKinds.getText());
    if (fMaxTestsWritten != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_WRITTEN, fMaxTestsWritten.getText());
    if (fMaxTestsPerFile != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_PER_FILE, fMaxTestsPerFile.getText());
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration config) {
    if (fOutputDirectory != null) {
      String handlerId = RandoopLaunchConfigArgumentCollector.getOutputDirectoryHandlerId(config);
  
      fOutputSourceFolder = RandoopLaunchConfigUtil.getPackageFragmentRoot(handlerId);
      if (fOutputSourceFolder != null) {
        fOutputDirectory.setText(fOutputSourceFolder.getPath().makeRelative()
            .toString());
      } else {
        fOutputDirectory.setText(IRandoopLaunchConfigConstants.EMPTY_STRING);
      }
    }
    if (fJUnitPackageName != null)
      fJUnitPackageName.setText(RandoopLaunchConfigArgumentCollector.getJUnitPackageName(config));
    if (fJUnitClassName != null)
      fJUnitClassName.setText(RandoopLaunchConfigArgumentCollector.getJUnitClassName(config));
    if (fTestKinds != null)
      fTestKinds.setText(RandoopLaunchConfigArgumentCollector.getTestKinds(config));
    if (fMaxTestsWritten != null)
      fMaxTestsWritten.setText(RandoopLaunchConfigArgumentCollector.getMaxTestsWritten(config));
    if (fMaxTestsPerFile != null)
      fMaxTestsPerFile.setText(RandoopLaunchConfigArgumentCollector.getMaxTestsPerFile(config));
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_JUNIT_PACKAGE_NAME,
        IRandoopLaunchConfigConstants.DEFAULT_JUNIT_PACKAGE_NAME);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_JUNIT_CLASS_NAME,
        IRandoopLaunchConfigConstants.DEFAULT_JUNIT_CLASS_NAME);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_TEST_KINDS,
        IRandoopLaunchConfigConstants.DEFAULT_TEST_KINDS);
    config.setAttribute(
        IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_WRITTEN,
        IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN);
    config.setAttribute(
        IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_PER_FILE,
        IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE);
  }

  protected IStatus validate(String outputSourceFolderHandlerId, String junitPackageName,
      String junitClassname, String testKinds, String maxTestsWritten, String maxTestsPerFile) {
    IStatus status;
    
    IPackageFragmentRoot outputDir = RandoopLaunchConfigUtil.getPackageFragmentRoot(outputSourceFolderHandlerId);
    if(outputDir == null) {
      status = StatusFactory.createErrorStatus("Output Directory is not a valid source folder");
      if (!status.isOK()) {
        return status;
      }
    }
  
    // First, check if the package name is the default, empty package name
    if (!junitPackageName.equals(IRandoopLaunchConfigConstants.EMPTY_STRING)) {
      status = JavaConventions.validatePackageName(junitPackageName,
          IRandoopLaunchConfigConstants.EMPTY_STRING, IRandoopLaunchConfigConstants.EMPTY_STRING);
      if (!status.isOK()) {
        return status;
      }
    }
  
    status = JavaConventions.validateJavaTypeName(junitClassname,
        IRandoopLaunchConfigConstants.EMPTY_STRING, IRandoopLaunchConfigConstants.EMPTY_STRING);
    if (!status.isOK()) {
      return status;
    }
    
    if (!testKinds.equals(IRandoopLaunchConfigConstants.STR_ALL)
        && testKinds.equals(IRandoopLaunchConfigConstants.STR_PASS)
        && testKinds.equals(IRandoopLaunchConfigConstants.STR_FAIL)) {
      return StatusFactory.createErrorStatus("Test Kinds must be of type {}, {}, or {}.");
    }
  
    status = RandoopLaunchConfigUtil.validatePositiveInt(maxTestsWritten, "Maximum Tests Written");
    if (!status.isOK()) {
      return status;
    }
    
    status = RandoopLaunchConfigUtil.validatePositiveInt(maxTestsPerFile, "Maximum Tests Per File");
    if (!status.isOK()) {
      return status;
    }
    
    return Status.OK_STATUS;
  }

  /**
   * Opens a selection dialog that allows to select a source container.
   * 
   * @return returns the selected package fragment root or <code>null</code> if
   *         the dialog has been canceled. The caller typically sets the result
   *         to the container input field.
   *         <p>
   *         Clients can override this method if they want to offer a different
   *         dialog.
   *         </p>
   * 
   * @since 3.2
   */
  protected IPackageFragmentRoot chooseSourceFolder() {
    // <<<< Source adapted from NewContainerWizardPage.java
    final Class<?>[] acceptedClasses = new Class<?>[] { IJavaModel.class,
        IPackageFragmentRoot.class, IJavaProject.class };

    ViewerFilter filter = new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parent, Object element) {
        if (element instanceof IPackageFragmentRoot) {
          try {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          } catch (JavaModelException e) {
            return false;
          }
        } else {
          for (int i = 0; i < acceptedClasses.length; i++) {
            if (acceptedClasses[i].isInstance(element)) {
              return true;
            }
          }
          return false;
        }
      }
    }; // >>>>

    ISelectionStatusValidator validator = new ISelectionStatusValidator() {
      public IStatus validate(Object[] selection) {
        if (selection.length != 1) {
          return StatusFactory.createErrorStatus();
        }
        Object element = selection[0];
        if (element instanceof IPackageFragmentRoot) {
          try {
            IPackageFragmentRoot pfroot = (IPackageFragmentRoot) element;
            if ((pfroot.getKind() == IPackageFragmentRoot.K_SOURCE))
              return StatusFactory.createOkStatus();
            else
              return StatusFactory.createErrorStatus();
          } catch (JavaModelException e) {
            return StatusFactory.createErrorStatus();
          }
        } else {
          return StatusFactory.createErrorStatus();
        }
      }
    };

    ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
    
    dialog.setTitle("Source Folder Selection");
    dialog.setMessage("&Choose a source folder");
    dialog.addFilter(filter);
    dialog.setValidator(validator);
    dialog.setInput(JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()));
    dialog.setHelpAvailable(false);

    if (dialog.open() == Window.OK) {
      Object element = dialog.getFirstResult();
      if (element instanceof IPackageFragmentRoot) {
        return (IPackageFragmentRoot) element;
      }
    }
    return null;
  }
  
  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName() {
    return "Output Files";
  }
  
  /**
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
   * 
   * @since 3.3
   */
  @Override
  public String getId() {
    return "randoop.plugin.launching.testInputConfig.outputFiles"; //$NON-NLS-1$
  }
  
  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
   */
  @Override
  public Image getImage() {
    return null;
  }
}
