package randoop.plugin.internal.ui.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

import randoop.plugin.RandoopPlugin;

public class LaunchConfigurationITypeMoveParticipant extends MoveParticipant {
  private IType fType;
  private IJavaElement fDestination;

  /**
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize
   *      (java.lang.Object)
   */
  protected boolean initialize(Object element) {
    fType = (IType) element;
    try {
      // check that the type is no a local, and is no declared in a local type
      IType declaringType = fType;
      while (declaringType != null) {
        if (fType.isLocal()) {
          return false;
        }
        declaringType = declaringType.getDeclaringType();
      }
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }
    Object destination = getArguments().getDestination();
    if (destination instanceof IPackageFragment || destination instanceof IType) {
      fDestination = (IJavaElement) destination;
      return true;
    }
    return false;
  }

  /**
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
   *      createChange(org.eclipse.core.runtime.IProgressMonitor)
   */
  public Change createChange(IProgressMonitor pm) throws CoreException {
    return null;// RandoopRefactoringUtil.createChangesForTypeMove(fType,
    // fDestination);
  }

  /**
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
   *      checkConditions(org.eclipse.core.runtime.IProgressMonitor,
   *      org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
   */
  public RefactoringStatus checkConditions(IProgressMonitor pm,
      CheckConditionsContext context) {
    // return OK status
    return new RefactoringStatus();
  }

  /**
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName
   *      ()
   */
  public String getName() {
    return "Launch configuration participant";
  }
}
