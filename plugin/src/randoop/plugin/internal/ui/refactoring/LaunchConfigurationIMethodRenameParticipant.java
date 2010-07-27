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

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
   */
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

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#createChange(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    List<Change> changes = new ArrayList<Change>();
    ILaunchConfiguration[] configs = RandoopRefactoringUtil.getRandoopTypeLaunchConfigurations();
    String newMethodName = getArguments().getNewName();
    
    String typeMnemonic = fMethodMnemonic.getDeclaringTypeMnemonic().toString();
    boolean isConstructor = fMethodMnemonic.isConstructor();
    String methodSignature = fMethodMnemonic.getMethodSignature();
    
    MethodMnemonic newMethodMnemonic = new MethodMnemonic(typeMnemonic, newMethodName, isConstructor, methodSignature);
    
    for(ILaunchConfiguration config : configs) {
      // TODO: Check if change is needed first
      Change c = new LaunchConfigurationMethodChange(config, fMethodMnemonic.toString(), newMethodMnemonic.toString());
      changes.add(c);
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
