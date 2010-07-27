package randoop.plugin.internal.ui.refactoring;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class LaunchConfigurationMethodChange  extends Change  {

  private ILaunchConfiguration fLaunchConfiguration;
  
  private final String fOldMnemonic;
  private final String fNewMnemonic;

  public LaunchConfigurationMethodChange(ILaunchConfiguration launchConfiguration, String oldMnemonic, String newMnemonic) throws CoreException {
    Assert.isLegal(launchConfiguration != null, "Launch configurtion cannot be null"); //$NON-NLS-1$
    Assert.isLegal(oldMnemonic != null, "Mnemonic cannot be null"); //$NON-NLS-1$
    Assert.isLegal(newMnemonic != null, "Mnemonic cannot be null"); //$NON-NLS-1$

    fLaunchConfiguration = launchConfiguration;
    
    fOldMnemonic = oldMnemonic;
    fNewMnemonic = newMnemonic;
  }

  @Override
  public Object getModifiedElement() {
    return fLaunchConfiguration;
  }

  @Override
  public String getName() {
    return MessageFormat.format("Update method used in launch configuration \"{0}\"", fLaunchConfiguration.getName());
  }
  
  @Override
  public void initializeValidationData(IProgressMonitor pm) {
  }

  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    if (fLaunchConfiguration.exists()) {
      return new RefactoringStatus();
    }
    return RefactoringStatus.createFatalErrorStatus(MessageFormat.format("The launch configuration \"{0}\" no longer exists.", fLaunchConfiguration.getName()));
  }

  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    final ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration.getWorkingCopy();

    List<String> availableMethodMnemonics = RandoopArgumentCollector.getAvailableMethods(wc);
    List<String> selectedMethodMnemonics = RandoopArgumentCollector.getSelectedMethods(wc);
    
    for (int i = 0; i < availableMethodMnemonics.size(); i++) {
      String oldMnemonic = availableMethodMnemonics.get(i);

      if (oldMnemonic.equals(fOldMnemonic)) {
        availableMethodMnemonics.set(i, fNewMnemonic);
      }
    }

    for (int i = 0; i < selectedMethodMnemonics.size(); i++) {
      String oldMnemonic = selectedMethodMnemonics.get(i);

      if (oldMnemonic.equals(fOldMnemonic)) {
        selectedMethodMnemonics.set(i, fNewMnemonic);
      }
    }
    
    RandoopArgumentCollector.setAvailableMethods(wc, availableMethodMnemonics);
    RandoopArgumentCollector.setSelectedMethods(wc, selectedMethodMnemonics);
    
    if (wc.isDirty()) {
      fLaunchConfiguration = wc.doSave();
    }
    
    // create the undo change
    return new LaunchConfigurationMethodChange(fLaunchConfiguration, fNewMnemonic, fOldMnemonic);
  }

}
