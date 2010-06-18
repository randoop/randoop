package randoop.plugin.internal.ui.refactoring;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class LaunchConfigurationTypeChange extends Change {
  private ILaunchConfiguration fLaunchConfiguration;
  private IType fOldType;
  private IType fNewType;

  /**
   * LaunchConfigurationProjectMainTypeChange constructor.
   * 
   * @param launchConfiguration
   *          the launch configuration to modify
   * @param newMainTypeName
   *          the name of the new main type, or <code>null</code> if not
   *          modified.
   * @param newProjectName
   *          the name of the project, or <code>null</code> if not modified.
   */
  public LaunchConfigurationTypeChange(
      ILaunchConfiguration launchConfiguration, IType oldType, IType newType)
      throws CoreException {
    fLaunchConfiguration = launchConfiguration;
    fOldType = oldType;
    fNewType = newType;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
   */
  @Override
  public Object getModifiedElement() {
    return fLaunchConfiguration;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#getName()
   */
  @Override
  public String getName() {
    return "Update test input types in launch configuration";
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void initializeValidationData(IProgressMonitor pm) {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {

    return new RefactoringStatus();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    List<String> allTypes;
    List<String> checkedElements;
    final ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration
        .getWorkingCopy();

    allTypes = RandoopArgumentCollector.getAllJavaTypes(wc);
    checkedElements = RandoopArgumentCollector.getCheckedJavaElements(wc);

    String oldHandlerId = fOldType.getHandleIdentifier();
    String newHandlerId = fNewType.getHandleIdentifier();
    for (int i = 0; i < checkedElements.size(); i++) {
      if (oldHandlerId.equals(checkedElements.get(i))) {
        checkedElements.set(i, newHandlerId);
      }
    }

    for (int i = 0; i < allTypes.size(); i++) {
      if (oldHandlerId.equals(allTypes.get(i))) {
        allTypes.set(i, newHandlerId);
      }
    }

    // create the undo change
    return new LaunchConfigurationTypeChange(fLaunchConfiguration, fNewType,
        fOldType);
  }
}
