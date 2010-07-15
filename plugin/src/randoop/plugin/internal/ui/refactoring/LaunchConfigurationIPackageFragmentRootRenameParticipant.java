package randoop.plugin.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class LaunchConfigurationIPackageFragmentRootRenameParticipant extends RenameParticipant {
  private String fOldSourceFolderName;
  IPackageFragmentRoot pfr;

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
   */
  @Override
  protected boolean initialize(Object element) {
    
    if (element instanceof IPackageFragmentRoot) {
      pfr = (IPackageFragmentRoot) element;
      
      try {
        if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
          fOldSourceFolderName = pfr.getElementName();
          return true;
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#createChange(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    List<Change> changes = new ArrayList<Change>();
    ILaunchConfiguration[] configs = RandoopRefactoringUtil.getRandoopTypeLaunchConfigurations();
    
    for(ILaunchConfiguration config : configs) {
      if(RandoopArgumentCollector.getOutputDirectoryName(config).equals(fOldSourceFolderName)) {
        Change c = new LaunchConfigurationFolderChange(config, getArguments().getNewName());
        changes.add(c);
      }
    }
    
    return RandoopRefactoringUtil.createChangeFromList(changes, "Launch configuration updates");
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
