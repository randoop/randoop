package randoop.plugin.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

public class IPackageFragmentMoveParticipant extends MoveParticipant {
  private IPackageFragment fPackageFragment;
  private IPackageFragmentRoot fDestination;

  /**
   * @see
   * org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize
   * (java.lang.Object)
   */
  protected boolean initialize(Object element) {
    fPackageFragment = (IPackageFragment) element;
    Object destination = getArguments().getDestination();
    if (destination instanceof IPackageFragmentRoot) {
      fDestination = (IPackageFragmentRoot) destination;
      // nothing to do if the project doesn't change
      if (fDestination.getJavaProject().equals(
          fPackageFragment.getJavaProject())) {
        return false;
      }
      return true;
    }
    return false;
  }

  /**
   * @seeorg.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
   * createChange(org.eclipse.core.runtime.IProgressMonitor)
   */
  public Change createChange(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    return null; // RandoopRefactoringUtil.createChangesForPackageMove(fPackageFragment,
    // fDestination);
  }

  /**
   * @seeorg.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
   * checkConditions(org.eclipse.core.runtime.IProgressMonitor,
   * org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
   */
  public RefactoringStatus checkConditions(IProgressMonitor pm,
      CheckConditionsContext context) throws OperationCanceledException {
    // return OK status
    return new RefactoringStatus();
  }

  /**
   * @see
   * org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName
   * ()
   */
  public String getName() {
    return "Launch configuration participant";
  }
}
