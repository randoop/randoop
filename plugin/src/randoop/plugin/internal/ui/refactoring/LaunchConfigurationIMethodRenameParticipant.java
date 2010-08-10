package randoop.plugin.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.MethodMnemonic;

public class LaunchConfigurationIMethodRenameParticipant extends RenameParticipant {
  private MethodMnemonic fMethodMnemonic;

  @Override
  protected boolean initialize(Object element) {
    Assert.isLegal(element instanceof IMethod);
    try {
      fMethodMnemonic = new MethodMnemonic((IMethod) element);

      return true;
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }
    return false;
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    List<Change> changes = new ArrayList<Change>();
    ILaunchConfiguration[] configs = RandoopRefactoringUtil.getRandoopTypeLaunchConfigurations();
    String newMethodName = getArguments().getNewName();
    
    boolean isConstructor = fMethodMnemonic.isConstructor();
    String methodSignature = fMethodMnemonic.getMethodSignature();
    
    MethodMnemonic newMethodMnemonic = new MethodMnemonic(newMethodName, isConstructor, methodSignature);
    
    for(ILaunchConfiguration config : configs) {
      // TODO: Check if change is needed first
      Change c = new LaunchConfigurationMethodChange(config, fMethodMnemonic.toString(), newMethodMnemonic.toString());
      changes.add(c);
    }
    
    return RandoopRefactoringUtil.createChangeFromList(changes, "Launch configuration updates");
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
