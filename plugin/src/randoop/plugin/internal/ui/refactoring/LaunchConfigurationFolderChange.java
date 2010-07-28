package randoop.plugin.internal.ui.refactoring;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

/**
 * The change for the main type project change of a launch configuration
 */
public class LaunchConfigurationFolderChange extends Change {

  private ILaunchConfiguration fLaunchConfiguration;
  
  private String fNewFolderName;
  private String fOldFolderName;

  public LaunchConfigurationFolderChange(ILaunchConfiguration launchConfiguration, String newFolderName)
      throws CoreException {
    fLaunchConfiguration = launchConfiguration;
    
    Assert.isLegal(launchConfiguration != null, "Launch configurtion cannot be null"); //$NON-NLS-1$
    Assert.isLegal(newFolderName != null, "Folder name cannot be null"); //$NON-NLS-1$
    fNewFolderName = newFolderName;
    fOldFolderName = RandoopArgumentCollector.getOutputDirectoryName(fLaunchConfiguration);
  }

  @Override
  public Object getModifiedElement() {
    return fLaunchConfiguration;
  }

  @Override
  public String getName() {
    return MessageFormat.format("Update output directory of launch configuration \"{0}\"", fLaunchConfiguration.getName());
  }

  @Override
  public void initializeValidationData(IProgressMonitor pm) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime
   * .IProgressMonitor)
   */
  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    if (fLaunchConfiguration.exists()) {
      String folderName = RandoopArgumentCollector.getOutputDirectoryName(fLaunchConfiguration);

      if (fOldFolderName.equals(folderName)) {
        return new RefactoringStatus();
      }
      return RefactoringStatus.createWarningStatus(MessageFormat.format("The project for launch configuration \"{0}\" is no longer \"{1}\".", fLaunchConfiguration.getName(), fOldFolderName));
    }
    return RefactoringStatus.createFatalErrorStatus(MessageFormat.format("The launch configuration \"{0}\" no longer exists.", fLaunchConfiguration.getName()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime
   * .IProgressMonitor)
   */
  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    final ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration.getWorkingCopy();

    RandoopArgumentCollector.setOutputDirectoryName(wc, fNewFolderName);
    
    if (wc.isDirty()) {
      fLaunchConfiguration = wc.doSave();
    }
    
    // create the undo change
    return new LaunchConfigurationFolderChange(fLaunchConfiguration, fOldFolderName);
  }

}
