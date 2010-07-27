package randoop.plugin.internal.ui.refactoring;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class LaunchConfigurationPackageFragmentChange extends Change {
  
  private ILaunchConfiguration fLaunchConfiguration;

  private String fOldPackageName;
  private String fNewPackageName;
  
  public LaunchConfigurationPackageFragmentChange(ILaunchConfiguration launchConfiguration,
      String oldPackageName, String newPackageName) throws CoreException {
    
    fLaunchConfiguration = launchConfiguration;
    
    fOldPackageName = oldPackageName;
    fNewPackageName = newPackageName;
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
//      String[] splitName = Mnemonics.splitFullyQualifiedName(s);
//      
//      if (splitName[0].equals(fOldPackageName)) {
//        splitName[0] = fNewPackageName;
//        availableTypes.set(i, Mnemonics.getFullyQualifiedName(splitName));
//      }
//    }
//
//    for (int i = 0; i < selectedTypes.size(); i++) {
//      String s = selectedTypes.get(i);
//      String[] splitName = Mnemonics.splitFullyQualifiedName(s);
//      
//      if (splitName[0].equals(fOldPackageName)) {
//        splitName[0] = fNewPackageName;
//        selectedTypes.set(i, Mnemonics.getFullyQualifiedName(splitName));
//      }
//    }
//    
//    for (int i = 0; i < selectedMethods.size(); i++) {
//      String[] methodInfo = Mnemonics.splitMethodMnemonic(selectedMethods.get(i));
//      
//      String[] splitName = Mnemonics.splitFullyQualifiedName(methodInfo[0]);
//      
//      if (splitName[0].equals(fOldPackageName)) {
//        splitName[0] = fNewPackageName;
//        methodInfo[0] = Mnemonics.getFullyQualifiedName(splitName);
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
//    return new LaunchConfigurationPackageFragmentChange(fLaunchConfiguration, fNewPackageName, fOldPackageName);
  }
}
