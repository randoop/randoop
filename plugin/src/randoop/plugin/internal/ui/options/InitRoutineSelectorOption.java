package randoop.plugin.internal.ui.options;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
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
 * TODO: Implement init routine selector option
 * This is a placeholder class (copied originally from OutputDirectoryOption)
 */
public class InitRoutineSelectorOption extends Option implements IOptionChangeListener {

  private Shell fShell;

  private IJavaProject fJavaProject;

  private Text fInitRoutineText;
  private IMethod fInitRoutine;
  private Button fInitRoutineBrowseButton;

  /**
   * Empty constructor to create a placeholder
   * <code>OutputDirectoryOption</code> that may be used to set defaults.
   */
  public InitRoutineSelectorOption() {
  }

  public InitRoutineSelectorOption(Shell shell, Text initRoutineText,
      Button initRoutineBrowseButton) {

    this(shell, null, initRoutineText, initRoutineBrowseButton);
  }

  public InitRoutineSelectorOption(Shell shell, IJavaProject project, Text initRoutineText,
      Button initRoutineBrowseButton) {

    fShell = shell;

    fJavaProject = project;

    fInitRoutineText = initRoutineText;
    // XXX
    // fInitRoutineText.addModifyListener(new ModifyListener() {
    //
    // public void modifyText(ModifyEvent e) {
    // notifyListeners(new OptionChangeEvent(
    // IRandoopLaunchConfigurationConstants.ATTR_OUTPUT_DIRECTORY_NAME,
    // fInitRoutineText.getText()));
    // }
    // });

    fInitRoutineBrowseButton = initRoutineBrowseButton;
    fInitRoutineBrowseButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IMethod chosenFolder = chooseInitRoutineFolder();

        if (chosenFolder != null) {
          fInitRoutine = chosenFolder;
          fInitRoutineText.setText(fInitRoutine.getElementName());
        }
      }
    });
    fInitRoutineBrowseButton.setEnabled(true);
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
  public IStatus canSave() {
    return RandoopStatus.OK_STATUS;
  }

  public IStatus isValid(ILaunchConfiguration config) {
    String projectName = RandoopArgumentCollector.getProjectName(config);

    String outputSourceFolderName = RandoopArgumentCollector.getOutputDirectoryName(config);

    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IStatus status = workspace.validateName(projectName, IResource.PROJECT);

    final char[] ILLEGAL_CHARACTERS = { '\\', ':', '*', '`', '?', '"', '<', '>', '|' };
    for (char c : ILLEGAL_CHARACTERS) {
      if (outputSourceFolderName.contains(new Character(c).toString())) {
        status = RandoopStatus.createUIStatus(IStatus.ERROR,
            "Output folder cannot contain any of the following characters: \\ : * ` ? \" < > |");
        return status;
      }
    }

    IPackageFragmentRoot outputDir = RandoopCoreUtil.getPackageFragmentRoot(fJavaProject,
        outputSourceFolderName);
    if (outputDir == null) {
      status = RandoopStatus.createUIStatus(IStatus.OK,
          "Output folder does not exist and will be created on launch");
      return status;
    }

    return RandoopStatus.OK_STATUS;
  }

  @Override
  public void initializeWithoutListenersFrom(ILaunchConfiguration config) {
    if (fInitRoutineText != null) {
      String projectName = RandoopArgumentCollector.getProjectName(config);
      fJavaProject = JavaCore.create(RandoopCoreUtil.getProjectFromName(projectName));

      String folderName = RandoopArgumentCollector.getOutputDirectoryName(config);

      // XXX - fOutputSourceFolder =
      // RandoopCoreUtil.getPackageFragmentRoot(fJavaProject, folderName);
      fInitRoutineText.setText(folderName);

      if (fInitRoutineBrowseButton != null) {
        fInitRoutineBrowseButton.setEnabled(fJavaProject != null && fJavaProject.exists());
      }
    }
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fInitRoutineText != null) {
      RandoopArgumentCollector.setOutputDirectoryName(config, fInitRoutineText.getText());
    }
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.setProjectName(config,
        IRandoopLaunchConfigurationConstants.DEFAULT_PROJECT);

    RandoopArgumentCollector.setOutputDirectoryName(config,
        IRandoopLaunchConfigurationConstants.DEFAULT_OUTPUT_DIRECTORY_NAME);
  }

  /**
   * Opens a filtered selection dialog that allows to search for an
   * init-routine.
   */
  private IMethod chooseInitRoutineFolder() {
    if (fJavaProject == null) {
      fInitRoutineBrowseButton.setEnabled(false);
      return null;
    }

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
      ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
      dialog.setTitle("Source Folder Selection");
      dialog.setMessage("Choose a source folder for the generated tests:");
      dialog.setElements(sourceFolders.toArray(new IPackageFragmentRoot[sourceFolders.size()]));
      dialog.setHelpAvailable(false);

      if (dialog.open() == Window.OK) {
        Object element = dialog.getFirstResult();
        if (element instanceof IPackageFragmentRoot) {
          return (IMethod) element;
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
    if (fInitRoutineText != null) {
      fInitRoutineText
          .setText(IRandoopLaunchConfigurationConstants.DEFAULT_OUTPUT_DIRECTORY_NAME);

      if (fInitRoutineBrowseButton != null) {
        fInitRoutineBrowseButton.setEnabled(fJavaProject != null);
      }
    }
  }

  public void attributeChanged(IOptionChangeEvent event) {
    String attr = event.getAttributeName();
    if (IRandoopLaunchConfigurationConstants.ATTR_PROJECT_NAME.equals(attr)) {
      String projectName = (String) event.getValue();

      fJavaProject = JavaCore.create(RandoopCoreUtil.getProjectFromName(projectName));

      fInitRoutineBrowseButton.setEnabled(fJavaProject != null && fJavaProject.exists());
    }
  }
}
