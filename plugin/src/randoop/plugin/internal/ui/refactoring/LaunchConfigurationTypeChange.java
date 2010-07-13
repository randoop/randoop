package randoop.plugin.internal.ui.refactoring;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.options.Mnemonics;

public class LaunchConfigurationTypeChange extends Change {
  
  private ILaunchConfiguration fLaunchConfiguration;

  private String fPackageName;

  private String fOldName;
  private String fNewName;
  
  private String fOldFullyQualifiedName;
  private String fNewFullyQualifiedName;

  public LaunchConfigurationTypeChange(ILaunchConfiguration launchConfiguration,
      String packageName, String oldName, String newName) throws CoreException {
    
    fLaunchConfiguration = launchConfiguration;
    
    fPackageName = packageName;
    
    fOldName = oldName;
    fNewName = newName;
    
    String prefix = getPackagePrefix(fPackageName);
    fOldFullyQualifiedName = prefix + fOldName;
    fNewFullyQualifiedName = prefix + fNewName;
  }
  
  private static String getPackagePrefix(String packageName) {
    String packagePrefix = packageName;
    if (packageName.equals(IConstants.EMPTY_STRING)) {
      packagePrefix = packageName;
    } else {
      packagePrefix = packageName + '.';
    }
    return packagePrefix;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
   */
  @Override
  public Object getModifiedElement() {
    return fLaunchConfiguration;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#getName()
   */
  @Override
  public String getName() {
    return "Update test input types in launch configuration";
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void initializeValidationData(IProgressMonitor pm) {
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    if (fLaunchConfiguration.exists()) {
      return new RefactoringStatus();
    }
    return RefactoringStatus.createFatalErrorStatus(MessageFormat.format("The launch configuration \"{0}\" no longer exists.", fLaunchConfiguration.getName()));
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    return null;
//    final ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration.getWorkingCopy();
//
//    List<String> availableTypes = RandoopArgumentCollector.getAvailableTypes(wc);
//    List<String> selectedTypes = RandoopArgumentCollector.getSelectedTypes(wc);
//    List<String> selectedMethods = RandoopArgumentCollector.getSelectedMethods(wc);
//
//    for (int i = 0; i < availableTypes.size(); i++) {
//      String s = availableTypes.get(i);
//      if (s.equals(fOldFullyQualifiedName)) {
//        availableTypes.set(i, fNewFullyQualifiedName);
//      }
//    }
//
//    for (int i = 0; i < selectedTypes.size(); i++) {
//      String s = selectedTypes.get(i);
//      if (s.equals(fOldFullyQualifiedName)) {
//        selectedTypes.set(i, fNewFullyQualifiedName);
//      }
//    }
//    
//    for (int i = 0; i < selectedMethods.size(); i++) {
//      String[] methodInfo = Mnemonics.splitMethodMnemonic(selectedMethods.get(i));
//      
//      if (methodInfo[0].equals(fOldFullyQualifiedName)) {
//        methodInfo[0] = fNewFullyQualifiedName;
//        
//        // Check if this is a constructor, and change its name if it is
//        if (methodInfo[1].equals(fOldName)) {
//          methodInfo[1] = fNewName;
//        }
//
//        selectedMethods.set(i, Mnemonics.getMethodMnemonic(methodInfo));
//      }
//    }
//    
//    RandoopArgumentCollector.setAvailableTypes(wc, availableTypes);
//    RandoopArgumentCollector.setSelectedTypes(wc, selectedTypes);
//    RandoopArgumentCollector.setSelectedMethods(wc, selectedMethods);
//
//    if(wc.isDirty()) {
//      wc.doSave();
//    }
//    
//    // create the undo change
//    return new LaunchConfigurationTypeChange(fLaunchConfiguration, fPackageName, fNewFullyQualifiedName, fOldFullyQualifiedName);
  }
}
