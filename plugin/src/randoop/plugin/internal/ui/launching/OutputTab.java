package randoop.plugin.internal.ui.launching;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
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
import org.eclipse.jdt.internal.debug.ui.SWTFactory;

import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.TestKinds;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class OutputTab extends
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

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 3, 1,
        GridData.FILL_HORIZONTAL);
    setControl(comp);

    SWTFactory.createLabel(comp, "Output Directory:", 1);
    fOutputDirectory = SWTFactory.createSingleText(comp, 1);
    fOutputDirectory.setEditable(false);

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
    fTestKinds = SWTFactory.createCombo(comp, SWT.READ_ONLY, 2,
        TestKinds.getTranslatableNames());

    SWTFactory.createLabel(comp, "Maximum Tests &Written:", 1);
    fMaxTestsWritten = SWTFactory.createSingleText(comp, 2);
    fMaxTestsWritten.addModifyListener(fBasicModifyListener);

    SWTFactory.createLabel(comp, "Maximum Tests Per &File:", 1);
    fMaxTestsPerFile = SWTFactory.createSingleText(comp, 2);
    fMaxTestsPerFile.addModifyListener(fBasicModifyListener);
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
   */
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
    String testKinds = TestKinds.getTestKind(fTestKinds.getSelectionIndex()).getArgumentName();
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

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration config) {
    String outputSourceFolderHandlerId = RandoopArgumentCollector.getOutputDirectoryHandlerId(config);
    String junitPackageName = RandoopArgumentCollector.getJUnitPackageName(config);
    String junitClassname = RandoopArgumentCollector.getJUnitClassName(config);
    String testKinds = RandoopArgumentCollector.getTestKinds(config);
    String maxTestsWritten = RandoopArgumentCollector.getMaxTestsWritten(config);
    String maxTestsPerFile = RandoopArgumentCollector.getMaxTestsPerFile(config);

    IStatus status = validate(outputSourceFolderHandlerId, junitPackageName,
        junitClassname, testKinds, maxTestsWritten, maxTestsPerFile);
    if (status.getSeverity() == IStatus.ERROR) {
      setErrorMessage(status.getMessage());
      return false;
    } else {
      setMessage(status.getMessage());
      return true;
    }
  };

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
   */
  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fOutputDirectory != null && fOutputSourceFolder != null)
      RandoopArgumentCollector.setOutputDirectoryHandlerId(config,
          fOutputSourceFolder.getHandleIdentifier());
    
    if (fJUnitPackageName != null)
      RandoopArgumentCollector.setJUnitPackageName(config,
          fJUnitPackageName.getText());
    
    if (fJUnitClassName != null)
      RandoopArgumentCollector.setJUnitClassName(config,
          fJUnitClassName.getText());
    
    if (fTestKinds != null)
      RandoopArgumentCollector.setTestKinds(config,
          TestKinds.getTestKind(fTestKinds.getSelectionIndex()).getArgumentName());
    
    if (fMaxTestsWritten != null)
      RandoopArgumentCollector.setMaxTestsWritten(config,
          fMaxTestsWritten.getText());
    
    if (fMaxTestsPerFile != null)
      RandoopArgumentCollector.setMaxTestsPerFile(config,
          fMaxTestsPerFile.getText());
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    if (fOutputDirectory != null) {
      String handlerId = RandoopArgumentCollector.getOutputDirectoryHandlerId(config);
  
      fOutputSourceFolder = RandoopLaunchConfigurationUtil.getPackageFragmentRoot(handlerId);
      if (fOutputSourceFolder != null) {
        fOutputDirectory.setText(fOutputSourceFolder.getPath().makeRelative()
            .toString());
      } else {
        fOutputDirectory.setText(IConstants.EMPTY_STRING);
      }
    }
    if (fJUnitPackageName != null)
      fJUnitPackageName.setText(RandoopArgumentCollector.getJUnitPackageName(config));
    if (fJUnitClassName != null)
      fJUnitClassName.setText(RandoopArgumentCollector.getJUnitClassName(config));
    if (fTestKinds != null)
      fTestKinds.setText(RandoopArgumentCollector.getTestKinds(config));
    if (fMaxTestsWritten != null)
      fMaxTestsWritten.setText(RandoopArgumentCollector.getMaxTestsWritten(config));
    if (fMaxTestsPerFile != null)
      fMaxTestsPerFile.setText(RandoopArgumentCollector.getMaxTestsPerFile(config));
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
   */
  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreOutputDirectoryHandlerId(config);
    RandoopArgumentCollector.restoreJUnitPackageName(config);
    RandoopArgumentCollector.restoreJUnitClassName(config);
    RandoopArgumentCollector.restoreTestKinds(config);
    RandoopArgumentCollector.restoreMaxTestsWritten(config);
    RandoopArgumentCollector.restoreMaxTestsPerFile(config);
  }

  /**
   * Returns an OK <code>IStatus</code> if the specified arguments could be
   * passed to Randoop without raising any error. If the arguments are not
   * valid, an ERROR status is returned with a message indicating what is wrong.
   * 
   * @param outputSourceFolderHandlerId
   * @param junitPackageName
   * @param junitClassname
   * @param testKinds
   * @param maxTestsWritten
   * @param maxTestsPerFile
   * @return
   */
  protected IStatus validate(String outputSourceFolderHandlerId,
      String junitPackageName, String junitClassname, String testKinds,
      String maxTestsWritten, String maxTestsPerFile) {
    IStatus status;

    IPackageFragmentRoot outputDir = RandoopLaunchConfigurationUtil
        .getPackageFragmentRoot(outputSourceFolderHandlerId);
    if (outputDir == null) {
      status = StatusFactory
          .createErrorStatus("Output Directory is not a valid source folder");
      if (!status.isOK()) {
        return status;
      }
    }
  
    // First, check if the package name is the default, empty package name
    if (!junitPackageName.equals(IConstants.EMPTY_STRING)) {
      status = JavaConventions.validatePackageName(junitPackageName,
          IConstants.EMPTY_STRING, IConstants.EMPTY_STRING);
      if (!status.isOK()) {
        return status;
      }
    }
  
    status = JavaConventions.validateJavaTypeName(junitClassname,
        IConstants.EMPTY_STRING, IConstants.EMPTY_STRING);
    if (!status.isOK()) {
      return status;
    }

    boolean validKind = false;
    for (TestKinds kindCandidate : TestKinds.values()) {
      validKind |= kindCandidate.getArgumentName().equals(testKinds);
    }
    if (!validKind) {
      return StatusFactory
          .createErrorStatus("Test Kinds must be of type All, Pass, or Fail.");
    }

    status = RandoopLaunchConfigurationUtil.validatePositiveInt(maxTestsWritten,
        "Maximum Tests Written is not a positive integer",
        "Maximum Tests Written is not a valid integer");
    if (!status.isOK()) {
      return status;
    }
    
    status = RandoopLaunchConfigurationUtil.validatePositiveInt(maxTestsPerFile,
        "Maximum Tests Per File is not a positive integer",
        "Maximum Tests Per File is not a valid integer");
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
  
  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  @Override
  public String getName() {
    return "Output Files";
  }
  
  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
   */
  @Override
  public String getId() {
    return "randoop.plugin.launching.testInputConfig.outputFiles"; //$NON-NLS-1$
  }
  
  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
   */
  @Override
  public Image getImage() {
    return null;
  }
}
