package randoop.plugin.internal.core.refactoring;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import randoop.plugin.launching.IRandoopLaunchConfigConstants;

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

  /**
   * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
   */
  public Object getModifiedElement() {
    return fLaunchConfiguration;
  }

  /**
   * @see org.eclipse.ltk.core.refactoring.Change#getName()
   */
  public String getName() {
    return "Update test input types in launch configuration";
  }

  /**
   * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse
   *      .core.runtime.IProgressMonitor)
   */
  public void initializeValidationData(IProgressMonitor pm) {
  }

  /**
   * @see org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime
   *      .IProgressMonitor)
   */
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {

    return new RefactoringStatus();
  }

  /**
   * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime
   *      .IProgressMonitor)
   */
  @SuppressWarnings("unchecked")
  public Change perform(IProgressMonitor pm) throws CoreException {
    List<String> checkedElements;
    List<String> uncheckedElements;
    final ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration
        .getWorkingCopy();

    try {
      checkedElements = wc.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_CHECKED_JAVA_ELEMENTS,
          IRandoopLaunchConfigConstants.DEFAULT_CHECKED_JAVA_ELEMENTS);
      uncheckedElements = wc.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_UNCHECKED_JAVA_TYPES,
          IRandoopLaunchConfigConstants.DEFAULT_UNCHECKED_JAVA_TYPES);
    } catch (CoreException ce) {
      return null;
    }

    String oldHandlerId = fOldType.getHandleIdentifier();
    String newHandlerId = fNewType.getHandleIdentifier();
    for (int i = 0; i < checkedElements.size(); i++) {
      if (oldHandlerId.equals(checkedElements.get(i))) {
        checkedElements.set(i, newHandlerId);
      }
    }

    for (int i = 0; i < uncheckedElements.size(); i++) {
      if (oldHandlerId.equals(uncheckedElements.get(i))) {
        uncheckedElements.set(i, newHandlerId);
      }
    }

    // create the undo change
    return new LaunchConfigurationTypeChange(fLaunchConfiguration, fNewType,
        fOldType);
  }
}
