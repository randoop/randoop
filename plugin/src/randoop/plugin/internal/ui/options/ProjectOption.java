package randoop.plugin.internal.ui.options;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.RandoopCoreUtil;
import randoop.plugin.internal.core.RandoopStatus;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

/**
 * 
 * @author Peter Kalauskas
 */
public class ProjectOption extends Option {
  private Shell fShell;

  private Text fProjectText;
  private IJavaProject fJavaProject;
  private Button fProjectBrowseButton;

  /**
   * Empty constructor to create a placeholder <code>ProjectOption</code> that
   * may be used to set defaults.
   */
  public ProjectOption() {
  }

  public ProjectOption(Shell shell, Text projectText, Button projectBrowseButton) {

    fShell = shell;

    fProjectText = projectText;
    fProjectText.addModifyListener(new ModifyListener() {

      public void modifyText(ModifyEvent e) {
        String projectName = fProjectText.getText();

        fJavaProject = JavaCore.create(RandoopCoreUtil.getProjectFromName(projectName));

        String attr = IRandoopLaunchConfigurationConstants.ATTR_PROJECT_NAME;
        notifyListeners(new OptionChangeEvent(attr, projectName));
      }
    });

    fProjectBrowseButton = projectBrowseButton;
    fProjectBrowseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent evt) {
        handleProjectBrowseButtonSelected();
      }
    });
  }

  public IStatus canSave() {
    return RandoopStatus.OK_STATUS;
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
  public IStatus isValid(ILaunchConfiguration config) {
    String projectName = RandoopArgumentCollector.getProjectName(config);

    // see org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab.isValid
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IStatus status = workspace.validateName(projectName, IResource.PROJECT);

    IJavaProject javaProject;
    if (status.isOK()) {
      IProject project = workspace.getRoot().getProject(projectName);
      if (!project.exists()) {
        return RandoopStatus.createUIStatus(IStatus.ERROR, NLS.bind(
            "Project {0} does not exist", new Object[] { projectName }));
      }
      if (!project.isOpen()) {
        return RandoopStatus.createUIStatus(IStatus.ERROR, NLS.bind(
            "Project {0} is closed", new Object[] { projectName }));
      }

      javaProject = JavaCore.create(project);
      if (javaProject == null) {
        return RandoopStatus.createUIStatus(IStatus.ERROR,
            NLS.bind("Project {0} is not a Java project", new Object[] { projectName }));
      }
    } else {
      return RandoopStatus.createUIStatus(IStatus.ERROR, NLS.bind(
          "Illegal project name: {0}", new Object[] { status.getMessage() }));
    }
    
    return RandoopStatus.OK_STATUS;
  }

  @Override
  public void initializeWithoutListenersFrom(ILaunchConfiguration config) {
    if (fProjectText != null) {
      String projectName = RandoopArgumentCollector.getProjectName(config);

      fJavaProject = JavaCore.create(RandoopCoreUtil.getProjectFromName(projectName));
      fProjectText.setText(projectName);

      String attr = IRandoopLaunchConfigurationConstants.ATTR_PROJECT_NAME;
      notifyListeners(new OptionChangeEvent(attr, projectName));
    }
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fProjectText != null) {
      RandoopArgumentCollector.setProjectName(config, fProjectText.getText());
    } else if (fJavaProject != null) {
      RandoopArgumentCollector.setProjectName(config, fJavaProject.getElementName());
    }
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.setProjectName(config,
        IRandoopLaunchConfigurationConstants.DEFAULT_PROJECT);

    RandoopArgumentCollector.setOutputDirectoryName(config,
        IRandoopLaunchConfigurationConstants.DEFAULT_OUTPUT_DIRECTORY_NAME);
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

    fJavaProject = project;
    fProjectText.setText(fJavaProject.getElementName());
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
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
        labelProvider);
    dialog.setTitle("Project Selection");
    dialog.setMessage("Choose a project to constrain the search for classes to test:");
    dialog.setElements(projects);

    IJavaProject javaProject = getJavaProject();
    if (javaProject != null) {
      dialog.setInitialSelections(new Object[] { javaProject });
    }
    if (dialog.open() == Window.OK) {
      Object element = dialog.getFirstResult();
      if (element instanceof IJavaProject) {
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
      pfRoots = fJavaProject.getPackageFragmentRoots();

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
      ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
          labelProvider);
      dialog.setTitle("Source Folder Selection");
      dialog.setMessage("Choose a source folder for the generated tests:");
      dialog.setElements(sourceFolders.toArray(new IPackageFragmentRoot[sourceFolders
          .size()]));
      dialog.setHelpAvailable(false);

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

  public void restoreDefaults() {
    if (fProjectText != null) {
      fProjectText.setText(IRandoopLaunchConfigurationConstants.DEFAULT_PROJECT);

      String attr = IRandoopLaunchConfigurationConstants.ATTR_PROJECT_NAME;
      notifyListeners(new OptionChangeEvent(attr,
          IRandoopLaunchConfigurationConstants.DEFAULT_PROJECT));
    }
  }

}
