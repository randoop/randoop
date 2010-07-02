package randoop.plugin.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class ITypeRenameParticipant extends RenameParticipant {
  private IType fType;

  @Override
  protected boolean initialize(Object element) {
    fType = (IType) element;
    return true;
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    return null; // RandoopRefactoringUtil.createChangesForTypeRename(fType,
                 // getArguments().getNewName());
  }

  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm,
      CheckConditionsContext context) throws OperationCanceledException {
    // return OK status
    return new RefactoringStatus();
  }

  @Override
  public String getName() {
    return "Launch configuration participant";
  }
}
