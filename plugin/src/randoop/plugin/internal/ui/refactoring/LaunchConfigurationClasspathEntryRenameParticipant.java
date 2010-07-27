package randoop.plugin.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class LaunchConfigurationClasspathEntryRenameParticipant extends RenameParticipant {
  IPath fOldPath;
  
  @Override
  protected boolean initialize(Object element) {
    if(element instanceof IFile || element instanceof IFolder) {
      fOldPath = ((IResource) element).getFullPath();
      return true;
    }
    
    return false;
  }
  
  @Override
  public String getName() {
    return "Launch configuration participant";
  }

  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
    // return OK status
    return new RefactoringStatus();
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    List<Change> changes = new ArrayList<Change>();
    ILaunchConfiguration[] configs = RandoopRefactoringUtil.getRandoopTypeLaunchConfigurations();

    for (ILaunchConfiguration config : configs) {
      String newFileName = getArguments().getNewName();
      IPath newPath = fOldPath.removeLastSegments(1).append(newFileName);
      Change c = new LaunchConfigurationClasspathEntryChange(config, fOldPath, newPath);
      changes.add(c);
    }
    
    return RandoopRefactoringUtil.createChangeFromList(changes, "Launch configuration updates");
  }

}
