package randoop.plugin.internal.ui.options;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.launching.RandoopLaunchConfigurationUtil;

public class ProjectOption extends Option {
  private Shell fShell;
  
  private Text fProjectText;
  private IJavaProject fProject;
  private Button fProjectBrowseButton;
  
  private Text fOutputSourceFolderText;
  private IPackageFragmentRoot fOutputSourceFolder;
  private Button fSourceFolderBrowseButton;
  
  public ProjectOption(Shell shell, Text projectText,
      Button projectBrowseButton, Text outputSourceFolderText,
      Button sourceFolderBrowseButton) {
    fShell = shell;
    
    fProjectText = projectText;
    fProjectText.setEditable(false);
    
    fProjectBrowseButton = projectBrowseButton;
    fProjectBrowseButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent evt) {
        handleProjectBrowseButtonSelected();

        String attr = IRandoopLaunchConfigurationConstants.ATTR_PROJECT;
        String handlerId = fProject.getHandleIdentifier();
        notifyListeners(new OptionChangeEvent(attr, handlerId));
      }
    });

    fOutputSourceFolderText = outputSourceFolderText;
    fOutputSourceFolderText.setEditable(false);
    
    fSourceFolderBrowseButton = sourceFolderBrowseButton;
    fSourceFolderBrowseButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IPackageFragmentRoot chosenFolder = chooseSourceFolder();

        if (chosenFolder != null) {
          fOutputSourceFolder = chosenFolder;
          fOutputSourceFolderText.setText(fOutputSourceFolder.getPath()
              .makeRelative().toString());
        }
      }
    });
    fSourceFolderBrowseButton.setEnabled(false);
  }

  @Override
  public IStatus canSave() {
    if (fProjectText == null || fProjectBrowseButton == null
        || fOutputSourceFolderText == null || fSourceFolderBrowseButton == null) {
      return StatusFactory.createErrorStatus("ProjectOption incorrectly initialized");
    }
  
    if (fProject == null) {
      return StatusFactory.createErrorStatus("Output Directory is not a valid source folder");
    }
    
    if (fOutputSourceFolder == null) {
      return StatusFactory.createErrorStatus("Output Directory is not a valid source folder");
    }
  
    String projectHandlerId = fProject.getHandleIdentifier();
    String outputSourceFolderHandlerId = fOutputSourceFolder.getHandleIdentifier();
  
    return validate(projectHandlerId, outputSourceFolderHandlerId);
  }

  public IStatus isValid(ILaunchConfiguration config) {
    String projectHandlerId = RandoopArgumentCollector.getProjectHandlerId(config);
    String outputSourceFolderHandlerId = RandoopArgumentCollector.getOutputDirectoryHandlerId(config);
  
    return validate(projectHandlerId, outputSourceFolderHandlerId);
  }

  /**
   * Returns an OK <code>IStatus</code> if the specified arguments could be
   * passed to Randoop without raising any error. If the arguments are not
   * valid, an ERROR status is returned with a message indicating what is wrong.
   * 
   * @param projectHandlerId
   * @param outputSourceFolderHandlerId
   * @return
   */
  protected IStatus validate(String projectHandlerId,
      String outputSourceFolderHandlerId) {
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
  
    return StatusFactory.createOkStatus();
  }

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
      
      String attr = IRandoopLaunchConfigurationConstants.ATTR_PROJECT;
      notifyListeners(new OptionChangeEvent(attr, handlerId));
    }

    if (fOutputSourceFolderText != null) {
      String handlerId = RandoopArgumentCollector
          .getOutputDirectoryHandlerId(config);

      fOutputSourceFolder = RandoopLaunchConfigurationUtil
          .getPackageFragmentRoot(handlerId);
      if (fOutputSourceFolder != null) {
        fOutputSourceFolderText.setText(fOutputSourceFolder.getPath()
            .makeRelative().toString());
      } else {
        fOutputSourceFolderText.setText(IConstants.EMPTY_STRING);
      }
    }
  }
  
  // expects ILaunchConfigurationWorkingCopy
  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fProjectText != null && fProject != null)
      RandoopArgumentCollector.setProjectHandlerId(config,
          fProject.getHandleIdentifier());
  
    if (fOutputSourceFolderText != null && fOutputSourceFolder != null)
      RandoopArgumentCollector.setOutputDirectoryHandlerId(config,
          fOutputSourceFolder.getHandleIdentifier());
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreProjectHandlerId(config);
    RandoopArgumentCollector.restoreOutputDirectoryHandlerId(config);
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
  private IPackageFragmentRoot chooseSourceFolder() {
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

  private Shell getShell() {
    return fShell;
  }
}
