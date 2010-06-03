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

import randoop.plugin.RandoopActivator;
import randoop.plugin.internal.ui.SWTFactory;

public class RandoopLaunchConfigOutputTab extends
    AbstractLaunchConfigurationTab {
  private static String EMPTY_STRING = ""; //$NON-NLS-1$
  private Text fOutputDirectory;
  private IPackageFragmentRoot fOutputSourceFolder;
  private Button fSourceFolderBrowse;

  private Text fJUnitPackageName;
  private Text fJUnitClassName;
  private Combo fTestKinds;
  private Text fMaxTestsWritten;
  private Text fMaxTestsPerFile;

  private class RandoopTabListener extends SelectionAdapter implements
      ModifyListener {
    public void modifyText(ModifyEvent e) {
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }
  }

  private ModifyListener fBasicModifyListener = new RandoopTabListener();

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
   */
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 3, 1,
        GridData.FILL_HORIZONTAL);
    setControl(comp);

    SWTFactory.createLabel(comp, "Output Directory:", 1);
    fOutputDirectory = SWTFactory.createSingleText(comp, 1);
    fOutputDirectory.setText(IRandoopLaunchConfigConstants.DEFAULT_OUTPUT_DIRECTORY);

    fSourceFolderBrowse = SWTFactory.createPushButton(comp, "Browse...", null);
    fSourceFolderBrowse.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fOutputSourceFolder = chooseSourceFolder();

        String str;
        if (fOutputSourceFolder == null) {
          str = EMPTY_STRING;
        } else {
          str = fOutputSourceFolder.getPath().makeRelative().toString();
        }

        fOutputDirectory.setText(str);

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

    String pname = fJUnitPackageName.getText();
    if (!pname.equals("")) {
      IStatus result = JavaConventions.validatePackageName(pname, EMPTY_STRING, EMPTY_STRING);
      if (!result.isOK()) {
        setErrorMessage("JUnit Package Name not valid. " + result.getMessage());
        return false;
      }
    }
    
    String cname = fJUnitClassName.getText();
    IStatus result = JavaConventions.validateJavaTypeName(cname, EMPTY_STRING, EMPTY_STRING);
    if (!result.isOK()) {
      setErrorMessage("JUnit Class Name not valid. " + result.getMessage());
      return false;
    }

    if (!checkIfPositiveInt("Maximum Tests Written", fMaxTestsWritten.getText())) {
      return false;
    }

    if (!checkIfPositiveInt("Maximum Tests Per File", fMaxTestsPerFile.getText())) {
      return false;
    }

    return true;
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    RandoopLaunchConfigOutputTab tab = new RandoopLaunchConfigOutputTab();
    tab.initializeFrom(launchConfig);
    return tab.canSave();
  };

  private boolean checkIfPositiveInt(String name, String n) {
    try {
      if (Integer.parseInt(n) < 1) {
        setErrorMessage(name + " is not a positive integer");
        return false;
      }
      return true;
    } catch (NumberFormatException nfe) {
      setErrorMessage(name + " is not a valid integer");
      return false;
    }
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fOutputDirectory != null && fOutputSourceFolder != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_OUTPUT_DIRECTORY,
          fOutputSourceFolder.getHandleIdentifier());
    if (fJUnitPackageName != null)
      config.setAttribute(
          IRandoopLaunchConfigConstants.ATTR_JUNIT_PACKAGE_NAME,
          fJUnitPackageName.getText());
    if (fJUnitClassName != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_JUNIT_CLASS_NAME,
          fJUnitClassName.getText());
    if (fTestKinds != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_TEST_KINDS,
          fTestKinds.getText());
    if (fMaxTestsWritten != null)
      config.setAttribute(
          IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_WRITTEN,
          fMaxTestsWritten.getText());
    if (fMaxTestsPerFile != null)
      config.setAttribute(
          IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_PER_FILE,
          fMaxTestsPerFile.getText());
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

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration config) {
    if (fOutputDirectory != null)
      try {
        String handler = config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_OUTPUT_DIRECTORY,
            IRandoopLaunchConfigConstants.DEFAULT_OUTPUT_DIRECTORY);
        IJavaElement element = JavaCore.create(handler);

        if (element == null || !(element instanceof IPackageFragmentRoot)) {
          fOutputDirectory.setText(EMPTY_STRING);
        } else {
          fOutputSourceFolder = (IPackageFragmentRoot) element;
          fOutputDirectory.setText(fOutputSourceFolder.getPath().makeRelative()
              .toString());
        }
      } catch (CoreException ce) {
        fJUnitPackageName
            .setText(IRandoopLaunchConfigConstants.DEFAULT_OUTPUT_DIRECTORY);
      }
    if (fJUnitPackageName != null)
      try {
        fJUnitPackageName.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_JUNIT_PACKAGE_NAME,
            IRandoopLaunchConfigConstants.DEFAULT_JUNIT_PACKAGE_NAME));
      } catch (CoreException ce) {
        fJUnitPackageName
            .setText(IRandoopLaunchConfigConstants.DEFAULT_JUNIT_PACKAGE_NAME);
      }
    if (fJUnitClassName != null)
      try {
        fJUnitClassName.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_JUNIT_CLASS_NAME,
            IRandoopLaunchConfigConstants.DEFAULT_JUNIT_CLASS_NAME));
      } catch (CoreException ce) {
        fJUnitClassName
            .setText(IRandoopLaunchConfigConstants.DEFAULT_JUNIT_CLASS_NAME);
      }
    if (fTestKinds != null)
      try {
        fTestKinds.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_TEST_KINDS,
            IRandoopLaunchConfigConstants.DEFAULT_TEST_KINDS));
      } catch (CoreException ce) {
        fTestKinds.setText(IRandoopLaunchConfigConstants.DEFAULT_TEST_KINDS);
      }
    if (fMaxTestsWritten != null)
      try {
        fMaxTestsWritten.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_WRITTEN,
            IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN));
      } catch (CoreException ce) {
        fMaxTestsWritten
            .setText(IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN);
      }
    if (fMaxTestsPerFile != null)
      try {
        fMaxTestsPerFile.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TESTS_PER_FILE,
            IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE));
      } catch (CoreException ce) {
        fMaxTestsPerFile
            .setText(IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE);
      }
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
        IStatus error = new Status(IStatus.ERROR, RandoopActivator.getPluginId(), EMPTY_STRING);
        IStatus ok = new Status(IStatus.OK, RandoopActivator.getPluginId(), EMPTY_STRING);
        
        
        if (selection.length != 1) {
          return error;
        }
        Object element = selection[0];
        if (element instanceof IPackageFragmentRoot) {
          try {
            IPackageFragmentRoot pfroot = (IPackageFragmentRoot) element;
            if ((pfroot.getKind() == IPackageFragmentRoot.K_SOURCE))
              return ok;
            else
              return error;
          } catch (JavaModelException e) {
            return error;
          }
        } else {
          return error;
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
      return null;
    }
    return null;
  }
}
