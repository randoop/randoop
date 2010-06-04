package randoop.plugin.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class IJavaProjectRenameParticipant extends
    RenameParticipant {
  private IJavaProject project;

  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm,
      CheckConditionsContext context) throws OperationCanceledException {
    // return OK status
    return new RefactoringStatus();
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    return null;
  }

  @Override
  public String getName() {
    return "Launch configuration participant";
  }

  @Override
  protected boolean initialize(Object element) {
    project = (IJavaProject) element;
    return true;
  }
}
