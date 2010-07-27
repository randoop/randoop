package randoop.plugin.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.ui.options.Mnemonics;

public class LaunchConfigurationITypeMoveParticipant extends MoveParticipant {
  private TypeMnemonic fTypeMnemonic;

  @Override
  protected boolean initialize(Object element) {
    if (element instanceof IType) {
      try {
        fTypeMnemonic = new TypeMnemonic((IType) element);
        
        return true;
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    return false;
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException {
    List<Change> changes = new ArrayList<Change>();
    ILaunchConfiguration[] configs = RandoopRefactoringUtil.getRandoopTypeLaunchConfigurations();
    Object destination = getArguments().getDestination();
    
    if (destination instanceof IPackageFragment) {
      IPackageFragment newPackageFragment = (IPackageFragment) destination;
      
      IJavaElement pfr = newPackageFragment.getParent();
      Assert.isNotNull(pfr);
      Assert.isTrue(pfr instanceof IPackageFragmentRoot);
      IClasspathEntry newClasspathEntry = ((IPackageFragmentRoot) pfr).getRawClasspathEntry();
      
      String oldFullyQualifiedName = fTypeMnemonic.getFullyQualifiedName();
      String className = Mnemonics.getClassName(oldFullyQualifiedName);
      String newFullyQualifiedName = Mnemonics.getFullyQualifiedName(newPackageFragment.getElementName(), className);
      
      TypeMnemonic newTypeMnemonic = new TypeMnemonic(fTypeMnemonic.getJavaProjectName(),
          newClasspathEntry.getEntryKind(), newClasspathEntry.getPath(), newFullyQualifiedName);
      
      for(ILaunchConfiguration config : configs) {
        // TODO: Check if change is needed first
        Change c = new LaunchConfigurationTypeChange(config, fTypeMnemonic.toString(), newTypeMnemonic.toString());
        changes.add(c);
      }
    }
    
    return RandoopRefactoringUtil.createChangeFromList(changes, "Launch configuration updates");
  }

  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) {
    // return OK status
    return new RefactoringStatus();
  }

  @Override
  public String getName() {
    return "Launch configuration participant";
  }
  
}
