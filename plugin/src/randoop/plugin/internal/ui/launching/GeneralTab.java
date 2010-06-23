package randoop.plugin.internal.ui.launching;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog;
import org.eclipse.jdt.ui.JavaElementLabelProvider;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.TestKinds;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.tests.ui.launching.MutableBoolean;

public class GeneralTab extends AbstractLaunchConfigurationTab {
  // Project selector
  private Text fProjectText;
  private IJavaProject fProject;
  private Button fProjectBrowseButton;
  
  private Text fOutputSourceFolderText;
  private IPackageFragmentRoot fOutputSourceFolder;
  private Button fSourceFolderBrowseButton;
  private Text fFullyQualifiedTestName;

  private TypeSelector fTypeSelector;
  private Tree fTypeTree;
  private Button fClassUp;
  private Button fClassDown;
  private Button fClassAddFromProject;
  private Button fClassAddFromClasspaths;
  private Button fClassAddFromSystemLibraries;
  private Button fClassRemove;
  
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
    Composite comp = SWTFactory.createComposite(parent, 1, 1,
        GridData.FILL_HORIZONTAL);
    setControl(comp);

    createProjectGroup(comp);
    createTestInputGroup(comp);
  }

  private void createProjectGroup(Composite parent) {
    Composite group = SWTFactory.createComposite(parent, 3, 1,
        GridData.FILL_HORIZONTAL);
    
    SWTFactory.createLabel(group, "Project:", 1);
    
    fProjectText = new Text(group, SWT.SINGLE | SWT.BORDER);
    fProjectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    fProjectText.setEditable(false);

    fProjectBrowseButton = SWTFactory.createPushButton(group, "Browse...", null);
    fProjectBrowseButton.setText("&Browse...");
    fProjectBrowseButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent evt) {
        handleProjectBrowseButtonSelected();
        
        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });

    SWTFactory.createLabel(group, "Output Folder:", 1);
    fOutputSourceFolderText = SWTFactory.createSingleText(group, 1);
    fOutputSourceFolderText.setEditable(false);
    
    fSourceFolderBrowseButton = SWTFactory.createPushButton(group, "Search...", null);
    fSourceFolderBrowseButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IPackageFragmentRoot chosenFolder = chooseSourceFolder();

        if (chosenFolder != null) {
          fOutputSourceFolder = chosenFolder;
          fOutputSourceFolderText.setText(fOutputSourceFolder.getPath().makeRelative()
              .toString());
        }

        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });
    fSourceFolderBrowseButton.setEnabled(false);
    
    SWTFactory.createLabel(group, "JUnit Class Name:", 1);
    fFullyQualifiedTestName = SWTFactory.createSingleText(group, 2);
    fFullyQualifiedTestName.addModifyListener(fBasicModifyListener);
  }
  
  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createTestInputGroup(Composite parent) {
    Group comp = SWTFactory.createGroup(parent, "Test Inputs", 2, 1,
        GridData.FILL_HORIZONTAL);

    final Composite leftcomp = SWTFactory.createComposite(comp, 1, 1,
        GridData.FILL_BOTH);
    GridLayout ld = (GridLayout) leftcomp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;
    GridData gd = (GridData) leftcomp.getLayoutData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;

    final Composite rightcomp = SWTFactory.createComposite(comp, 1, 1,
        GridData.FILL);
    gd = (GridData) rightcomp.getLayoutData();
    gd.horizontalAlignment = SWT.LEFT;
    gd.verticalAlignment = SWT.TOP;

    fTypeTree = new Tree(leftcomp, SWT.MULTI | SWT.CHECK | SWT.BORDER);
    fTypeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    // Add type selector functionalities to this tree. This will also add a
    // Listener to fTypeTree that adds default tree behavior. That is, checking/
    // unchecking a parent item also checks/unchecks all descendant items
    fTypeSelector = new TypeSelector(fTypeTree);
    
    // Add the
    fTypeTree.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        if (event.detail == SWT.CHECK) {
          setErrorMessage(null);
          updateLaunchConfigurationDialog();
        }
      }
    });
    
    fClassUp = SWTFactory.createPushButton(rightcomp, "Up", null);
    fClassUp.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        // TODO: Move class Up
        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });
    
    fClassDown = SWTFactory.createPushButton(rightcomp, "Down", null);
    fClassDown.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        // TODO: Move class Down
        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });
    
    fClassRemove = SWTFactory.createPushButton(rightcomp, "Remove", null);
    fClassRemove.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fTypeSelector.removeSelectedTypes();
        
        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });
    
    SWTFactory.createHorizontalSpacer(rightcomp, 0);
    SWTFactory.createLabel(rightcomp, "Add classes from:", 1);
    
    fClassAddFromProject = SWTFactory.createPushButton(rightcomp, "Selected Project...", null);
    fClassAddFromProject.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSearchButtonSelected();

        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });
    
    fClassAddFromClasspaths = SWTFactory.createPushButton(rightcomp, "Referenced Classpaths...", null);
    fClassAddFromClasspaths.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSearchButtonSelected();

        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });
    
    fClassAddFromSystemLibraries = SWTFactory.createPushButton(rightcomp, "System Libraries...", null);
    fClassAddFromSystemLibraries.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSearchButtonSelected();

        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });
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
  
    if (fProjectText == null || fProjectBrowseButton == null
        || fOutputSourceFolderText == null || fSourceFolderBrowseButton == null
        || fFullyQualifiedTestName == null || fFullyQualifiedTestName == null) {
      return false;
    }
  
    if (fProject == null) {
      setErrorMessage("Project does not exist");
      return false;
    }
    
    if (fOutputSourceFolder == null) {
      setErrorMessage("Output Directory is not a valid source folder");
      return false;
    }
  
    String projectHandlerId = fProject.getHandleIdentifier();
    String outputSourceFolderHandlerId = fOutputSourceFolder.getHandleIdentifier();
    String junitFullyQualifiedClassName = fFullyQualifiedTestName.getText();
    List<String> selectedTypes = fTypeSelector.getCheckedHandlerIds();
  
    IStatus status = validate(projectHandlerId, outputSourceFolderHandlerId,
        junitFullyQualifiedClassName, selectedTypes);
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
    String projectHandlerId = RandoopArgumentCollector.getProjectHandlerId(config);
    String outputSourceFolderHandlerId = RandoopArgumentCollector.getOutputDirectoryHandlerId(config);
    String junitFullyQualifiedName = RandoopArgumentCollector.getJUnitFullyQualifiedTypeName(config);
    List<String> selectedTypes = RandoopArgumentCollector.getCheckedJavaElements(config);

    IStatus status = validate(projectHandlerId, outputSourceFolderHandlerId,
        junitFullyQualifiedName, selectedTypes);
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
    if (fProjectText != null && fProject != null)
      RandoopArgumentCollector.setProjectHandlerId(config,
          fProject.getHandleIdentifier());
    
    if (fOutputSourceFolderText != null && fOutputSourceFolder != null)
      RandoopArgumentCollector.setOutputDirectoryHandlerId(config,
          fOutputSourceFolder.getHandleIdentifier());
    
    if (fFullyQualifiedTestName != null)
      RandoopArgumentCollector.setJUnitFullyQualifiedTypeName(config,
          fFullyQualifiedTestName.getText());
    
    if (fTypeSelector == null) {
      setDefaults(config);
    } else {
      RandoopArgumentCollector.setAllJavaTypes(config, fTypeSelector.getAllTypeHandlerIds());
      RandoopArgumentCollector.setCheckedJavaElements(config, fTypeSelector.getCheckedHandlerIds());
    }
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    if (fProjectText != null) {
      String handlerId = RandoopArgumentCollector.getProjectHandlerId(config);
  
      fProject = RandoopLaunchConfigurationUtil.getProject(handlerId);
      if (fProject != null) {
        fProjectText.setText(fProject.getElementName());
      } else {
        fOutputSourceFolderText.setText(IConstants.EMPTY_STRING);
      }
    }
    
    if (fOutputSourceFolderText != null) {
      String handlerId = RandoopArgumentCollector.getOutputDirectoryHandlerId(config);
  
      fOutputSourceFolder = RandoopLaunchConfigurationUtil.getPackageFragmentRoot(handlerId);
      if (fOutputSourceFolder != null) {
        fOutputSourceFolderText.setText(fOutputSourceFolder.getPath().makeRelative()
            .toString());
      } else {
        fOutputSourceFolderText.setText(IConstants.EMPTY_STRING);
      }
    }
    if (fFullyQualifiedTestName != null)
      fFullyQualifiedTestName.setText(RandoopArgumentCollector.getJUnitFullyQualifiedTypeName(config));
    
    if (fTypeTree != null) {
      List<String> allTypes = RandoopArgumentCollector.getAllJavaTypes(config);
      List<String> checkedElements = RandoopArgumentCollector.getCheckedJavaElements(config);

      fTypeSelector = new TypeSelector(fTypeTree, allTypes, checkedElements);
    }
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
   */
  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreProjectHandlerId(config);
    RandoopArgumentCollector.restoreOutputDirectoryHandlerId(config);
    RandoopArgumentCollector.restoreJUnitPackageName(config);
    RandoopArgumentCollector.restoreJUnitClassName(config);
    RandoopArgumentCollector.restoreTestKinds(config);
    RandoopArgumentCollector.restoreMaxTestsWritten(config);
    RandoopArgumentCollector.restoreMaxTestsPerFile(config);
    RandoopArgumentCollector.restoreCheckedJavaElements(config);
    RandoopArgumentCollector.restoreAllJavaTypes(config);
  }

  /**
   * Returns an OK <code>IStatus</code> if the specified arguments could be
   * passed to Randoop without raising any error. If the arguments are not
   * valid, an ERROR status is returned with a message indicating what is wrong.
   * 
   * @param projectHandlerId
   * @param outputSourceFolderHandlerId
   * @param junitFullyQualifiedClassName
   * @param testKinds
   * @param maxTestsWritten
   * @param maxTestsPerFile
   * @param timeLimit
   * @return
   */
  protected IStatus validate(String projectHandlerId,
      String outputSourceFolderHandlerId, String junitFullyQualifiedClassName,
      List<String> selectedTypes) {
    IStatus status;

    IJavaProject project = RandoopLaunchConfigurationUtil
        .getProject(projectHandlerId);
    if (project == null) {
      status = StatusFactory
          .createErrorStatus("Project does not exist");
      return status;
    } else if (!project.exists()) {
      status = StatusFactory.createErrorStatus("Project "
          + project.getElementName() + " does not exist");
      return status;
    }
    
    IPackageFragmentRoot outputDir = RandoopLaunchConfigurationUtil
        .getPackageFragmentRoot(outputSourceFolderHandlerId);
    if (outputDir == null) {
      status = StatusFactory
          .createErrorStatus("Output Directory is not a valid source folder");
      return status;
    } else if (!outputDir.exists()) {
      status = StatusFactory.createErrorStatus("Output Directory "
          + outputDir.getElementName() + " does not exist");
      return status;
    } else if (!outputDir.getJavaProject().equals(project)) {
      status = StatusFactory
          .createErrorStatus("Output Directory does not exist in project "
              + project.getElementName());
      return status;
    }

    // First, check if the package name is the default, empty package name
    status = JavaConventions.validateJavaTypeName(junitFullyQualifiedClassName,
        IConstants.EMPTY_STRING, IConstants.EMPTY_STRING);
    if (!status.isOK()) {
      return status;
    }

    IStatus errorStatus = StatusFactory
    .createErrorStatus("At least one existing type or method must be selected.");

    if (selectedTypes == null || selectedTypes.isEmpty()) {
      return errorStatus;
    }

    for (String handlerId : selectedTypes) {
      if (!JavaCore.create((String) handlerId).exists()) {
        return errorStatus;
      }
    }

    return StatusFactory.createOkStatus();
  }

  /*
   * Show a dialog that lets the user select a project. This in turn provides
   * context for the main type, allowing the user to key a main type name, or
   * constraining the search for main types to the specified project.
   * 
   * Copied from org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationTab
   */
  private void handleProjectBrowseButtonSelected() {
    IJavaProject project = chooseJavaProject();
    if (project == null) {
      return;
    }

    boolean okToProceed = true;
    
    // TODO: check if test inputs will change
    
    System.out.println("okToProceed=" + okToProceed);

    // It is okay to proceed if neither the output folder or selected types will
    // change
    if (!okToProceed) {
      String[] dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
          IDialogConstants.NO_LABEL };

      MessageDialog dialog = new MessageDialog(getShell(), "Change Project", null,
          "Changing the selected project will change some of the selected test inputs.\n\nOkay to proceed?",
          MessageDialog.QUESTION, dialogButtonLabels, 0);
      okToProceed = dialog.open() == 0;
    }
    
    if (okToProceed) {
      // reset source folder if necessary
      if (fOutputSourceFolder != null) {
        if (!fOutputSourceFolder.getJavaProject().equals(project)) {
          fOutputSourceFolder = null;
          if (fOutputSourceFolderText != null) {
            fOutputSourceFolderText.setText(IConstants.EMPTY_STRING);
          }
        }
      }
      
      // TODO update selected test kinds
      
      fProject = project;
      String projectName = fProject.getElementName();
      fProjectText.setText(projectName);
    }
  }

  /*
   * Realize a Java Project selection dialog and return the first selected
   * project, or null if there was none.
   * 
   * Copied from org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationTab
   */
  private IJavaProject chooseJavaProject() {
    IJavaProject[] projects;
    try {
      projects = JavaCore.create(getWorkspaceRoot()).getJavaProjects();
    } catch (JavaModelException e) {
      RandoopPlugin.log(e.getStatus());
      projects = new IJavaProject[0];
    }

    ILabelProvider labelProvider = new JavaElementLabelProvider(
        JavaElementLabelProvider.SHOW_DEFAULT);
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(
        getShell(), labelProvider);
    dialog.setTitle("Project Selection");
    dialog.setMessage("Choose a project to constrain the search for test classes:");
    dialog.setElements(projects);

    IJavaProject javaProject = getJavaProject();
    if (javaProject != null) {
      dialog.setInitialSelections(new Object[] { javaProject });
    }
    if (dialog.open() == Window.OK) {
      Object element = dialog.getFirstResult();
      if (element instanceof IJavaProject) {
        fSourceFolderBrowseButton.setEnabled(true);
        
        return (IJavaProject) element;
      }
    }
    return null;
  }

  /*
   * Return the IJavaProject corresponding to the project name in the project
   * name text field, or null if the text does not match a project name.
   * 
   * Copied from org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationTab
   */
  private IJavaProject getJavaProject() {
    String projectName = fProjectText.getText().trim();
    if (projectName.length() < 1) {
      return null;
    }
    return JavaCore.create(getWorkspaceRoot()).getJavaProject(projectName);
  }

  /*
   * Convenience method to get the workspace root.
   */
  private static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
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
    IPackageFragmentRoot pfRoots[];
    try {
      pfRoots = fProject.getPackageFragmentRoots();

      List<IPackageFragmentRoot> sourceFolders = new ArrayList<IPackageFragmentRoot>();
      for (IPackageFragmentRoot pfRoot : pfRoots) {
        try {
          if (pfRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
            sourceFolders.add(pfRoot);
          }
        } catch (JavaModelException e) {
          e.printStackTrace();
        }
      }

      ILabelProvider labelProvider = new JavaElementLabelProvider(
          JavaElementLabelProvider.SHOW_DEFAULT);
      ElementListSelectionDialog dialog = new ElementListSelectionDialog(
          getShell(), labelProvider);
      dialog.setTitle("Project Selection");
      dialog
          .setMessage("Choose a project to constrain the search for test classes:");
      dialog.setElements(sourceFolders
          .toArray(new IPackageFragmentRoot[sourceFolders.size()]));
      dialog.setHelpAvailable(false);

      IJavaProject javaProject = getJavaProject();
      if (javaProject != null) {
        dialog.setInitialSelections(new Object[] { javaProject });
      }
      if (dialog.open() == Window.OK) {
        Object element = dialog.getFirstResult();
        if (element instanceof IPackageFragmentRoot) {
          return (IPackageFragmentRoot) element;
        }
      }
    } catch (JavaModelException e) {
      RandoopPlugin.log(e.getStatus());
    }
    return null;
  }
  
  /**
   * Opens a <code>FilteredItemsSelectionDialog</code> that lists all types in
   * the workspace. The user can select multiple items from the list. Upon
   * pressing OK, the selected types are added to this tab's SWT
   * <code>Tree</code>.
   */
  protected void handleSearchButtonSelected() {
    DebugTypeSelectionDialog mmsd = new DebugTypeSelectionDialog(getShell(),
        getAllAvailableTypes(getLaunchConfigurationDialog()),
        "Select types to test");
    if (mmsd.open() == Window.CANCEL) {
      return;
    }

    // Add all of the types to the type selector
    Object[] results = mmsd.getResult();
    if (results.length > 0) {
      for (Object element : results) {
        if (element instanceof IType) {
          IType type = (IType) element;
          if (type != null) {
            fTypeSelector.addType(type, false);
          }
        }
      }
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }
  }

  /**
   * Returns an array of all available <code>IType</code>s in the active
   * workspace. The specified dialog will be used to display this operation
   * progress. This method will return <code>null</code> if the dialog is
   * <code>null</code>. If an error occurs, an empty or partially complete array
   * may be returned.
   * 
   * @param dialog
   *          the <code>ILaunchConfigurationDialog</code> that will be used to
   *          display the progress of this operation - must not be
   *          <code>null</code>
   * @return a complete or partially complete list of <code>IType</code>s in the
   *         workspace, or <code>null</code>
   */
  private static IType[] getAllAvailableTypes(ILaunchConfigurationDialog dialog) {
    if (dialog == null)
      return null;

    final List<IType> availableTypes = new ArrayList<IType>();

    IJavaModel model = JavaCore.create(getWorkspaceRoot());
    if (model != null) {
      try {
        final IJavaProject[] javaProjects = model.getJavaProjects();
        
        // Create a new runnable object that will be used to search for the Java
        // types in the active workspace.
        IRunnableWithProgress typeSearcher = new IRunnableWithProgress() {
          @Override
          public void run(IProgressMonitor pm) throws InvocationTargetException {
            pm.beginTask("Searching for Java types...", IProgressMonitor.UNKNOWN);
            
            // Search each IJavaProject for ITypes and add them to a list.
            // Each added IType represents one unit of work.
            try {
              for (IJavaProject jp : javaProjects) {
                for (IPackageFragment pf : jp.getPackageFragments()) {
                  for (ICompilationUnit cu : pf.getCompilationUnits()) {
                    for (IType t : cu.getAllTypes()) {
                      availableTypes.add(t);
                      pm.worked(1);
                    }
                  }
                }
              }
            } catch (JavaModelException e) {
              RandoopPlugin.log(e);
            }

            pm.done();
          }
        };
        
        // Search for available ITypes while displaying progress in the dialog
        try {
            dialog.run(true, true, typeSearcher);
        } catch (InvocationTargetException e) {
          RandoopPlugin.log(e);
        } catch (InterruptedException e) {
          RandoopPlugin.log(e);
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    
    return availableTypes.toArray(new IType[availableTypes.size()]);
  }
  
  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  @Override
  public String getName() {
    return "General";
  }
  
  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
   */
  @Override
  public String getId() {
    return "randoop.plugin.ui.launching.generalrTab"; //$NON-NLS-1$
  }
  
}
