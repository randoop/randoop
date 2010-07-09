package randoop.plugin.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class LaunchConfigurationIPackageFragmentRenameParticipant extends RenameParticipant {
  private IPackageFragment fPackageFragment;

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#createChange(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    String oldPackageName = fPackageFragment.getElementName();
    
    List<Change> changes = new ArrayList<Change>();
    ILaunchConfiguration[] configs = RandoopRefactoringUtil.getRandoopTypeLaunchConfigurations();
    
    final String anything = "\\p{ASCII}*"; //$NON-NLS-1$
    for(ILaunchConfiguration config : configs) {
      String newName = getArguments().getNewName();
      Change c = new LaunchConfigurationPackageFragmentChange(config, oldPackageName, newName);
      changes.add(c);
    }
    
    return RandoopRefactoringUtil.createChangeFromList(changes, "Launch configuration updates");
  }
  
  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
   */
  @Override
  protected boolean initialize(Object element) {
    if (element instanceof IPackageFragment) {
      fPackageFragment = (IPackageFragment) element;
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#checkConditions(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
   */
  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm,
      CheckConditionsContext context) throws OperationCanceledException {
    // return OK status
    return new RefactoringStatus();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName()
   */
  @Override
  public String getName() {
    return "Launch configuration participant";
  }
}
