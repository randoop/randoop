package randoop.plugin.internal.ui.refactoring;

import java.text.MessageFormat;

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
public class LaunchConfigurationProjectChange extends Change {

  private ILaunchConfiguration fLaunchConfiguration;
  private String fOldProjectName;
  private String fNewProjectName;

  public LaunchConfigurationProjectChange(ILaunchConfiguration launchConfiguration, String newProjectName)
      throws CoreException {
    fLaunchConfiguration = launchConfiguration;
    fNewProjectName = newProjectName;
    fOldProjectName = RandoopArgumentCollector.getProjectName(fLaunchConfiguration);
  }

  @Override
  public Object getModifiedElement() {
    return fLaunchConfiguration;
  }

  @Override
  public String getName() {
    return MessageFormat.format("Update project of launch configuration \"{0}\"", new String[] { fLaunchConfiguration.getName() });
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
      String projectName = RandoopArgumentCollector.getProjectName(fLaunchConfiguration);

      if (fOldProjectName.equals(projectName)) {
        return new RefactoringStatus();
      }
      return RefactoringStatus.createWarningStatus(MessageFormat.format("The project for launch configuration \"{0}\" is no longer \"{1}\".", new String[] { fLaunchConfiguration.getName(), fOldProjectName }));
    }
    return RefactoringStatus.createFatalErrorStatus(MessageFormat.format("The launch configuration \"{0}\" no longer exists.", new String[] { fLaunchConfiguration.getName() }));
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

    RandoopArgumentCollector.setProjectName(wc, fNewProjectName);
    
    if (wc.isDirty()) {
      fLaunchConfiguration = wc.doSave();
    }
    
    // create the undo change
    return new LaunchConfigurationProjectChange(fLaunchConfiguration, fOldProjectName);
  }
  
}
