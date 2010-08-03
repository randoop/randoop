package randoop.plugin.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class LaunchConfigurationIJavaProjectRenameParticipant extends RenameParticipant {
  private String fOldProjectName;

  @Override
  protected boolean initialize(Object element) {
    if (element != null && element instanceof IJavaProject) {
      fOldProjectName = ((IJavaProject) element).getElementName();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    List<Change> changes = new ArrayList<Change>();
    ILaunchConfiguration[] configs = RandoopRefactoringUtil.getRandoopTypeLaunchConfigurations();
  
    boolean selectedProjectChangeNeeded = false;
    boolean typeChangeNeeded = false;
    boolean methodChangeNeeded = false;
  
    for (ILaunchConfiguration config : configs) {
      if (RandoopArgumentCollector.getProjectName(config).equals(fOldProjectName)) {
        selectedProjectChangeNeeded = true;
      }
  
      List<String> typeMnemonics = RandoopArgumentCollector.getAvailableTypes(config);
      for (String mnemonic : typeMnemonics) {
        TypeMnemonic typeMnemonic = new TypeMnemonic(mnemonic);
        String projectName = typeMnemonic.getJavaProjectName();
  
        if (projectName.equals(fOldProjectName)) {
          typeChangeNeeded = true;
          break;
        }
      }
  
      List<String> methodMnemonics = RandoopArgumentCollector.getAvailableMethods(config);
      for (String mnemonic : methodMnemonics) {
        MethodMnemonic methodMnemonic = new MethodMnemonic(mnemonic);
        String projectName = methodMnemonic.getDeclaringTypeMnemonic().getJavaProjectName();
  
        if (projectName.equals(fOldProjectName)) {
          methodChangeNeeded = true;
          break;
        }
      }
  
      if (selectedProjectChangeNeeded || typeChangeNeeded || methodChangeNeeded) {
        String newProjectName = getArguments().getNewName();
        IPath oldPath = new Path(fOldProjectName).makeAbsolute();
        IPath newPath = new Path(newProjectName).makeAbsolute();
        Change c = new LaunchConfigurationClasspathEntryChange(config, oldPath, newPath);
        changes.add(c);
        c = new LaunchConfigurationProjectChange(config, newProjectName, selectedProjectChangeNeeded, typeChangeNeeded, methodChangeNeeded);
        changes.add(c);
      }
    }
    
    return RandoopRefactoringUtil.createChangeFromList(changes, "Launch configuration updates");
  }

  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
    // return OK status
    return new RefactoringStatus();
  }

  @Override
  public String getName() {
    return "Launch configuration participant";
  }
  
}
