package randoop.plugin.internal.ui.refactoring;

import java.text.MessageFormat;
import java.util.HashMap;
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

public class LaunchConfigurationTypeChange  extends Change  {

  private ILaunchConfiguration fLaunchConfiguration;
  
  private final HashMap<String, String> fNewTypeMnemonicByOldTypeMnemonic;

  public LaunchConfigurationTypeChange(ILaunchConfiguration launchConfiguration, String oldMnemonic, String newMnemonic) throws CoreException {
    Assert.isLegal(launchConfiguration != null, "Launch configurtion cannot be null"); //$NON-NLS-1$
    Assert.isLegal(oldMnemonic != null, "Mnemonic cannot be null"); //$NON-NLS-1$
    Assert.isLegal(newMnemonic != null, "Mnemonic cannot be null"); //$NON-NLS-1$

    fLaunchConfiguration = launchConfiguration;
    
    fNewTypeMnemonicByOldTypeMnemonic = new HashMap<String, String>();
    fNewTypeMnemonicByOldTypeMnemonic.put(oldMnemonic, newMnemonic);
  }
  
  public LaunchConfigurationTypeChange(ILaunchConfiguration launchConfiguration, HashMap<String, String> newTypeMnemonicByOldTypeMnemonic) throws CoreException {
    Assert.isLegal(launchConfiguration != null, "Launch configurtion cannot be null"); //$NON-NLS-1$
    Assert.isLegal(newTypeMnemonicByOldTypeMnemonic != null, "Mnemonic map cannot be null"); //$NON-NLS-1$

    fLaunchConfiguration = launchConfiguration;
    
    fNewTypeMnemonicByOldTypeMnemonic = newTypeMnemonicByOldTypeMnemonic;
  }

  @Override
  public Object getModifiedElement() {
    return fLaunchConfiguration;
  }

  @Override
  public String getName() {
    return MessageFormat.format("Update type used in launch configuration \"{0}\"", fLaunchConfiguration.getName());
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

    List<String> availableTypeMnemonics = RandoopArgumentCollector.getAvailableTypes(wc);
    List<String> grayedTypeMnemonics = RandoopArgumentCollector.getGrayedTypes(wc);
    List<String> checkedTypeMnemonics = RandoopArgumentCollector.getCheckedTypes(wc);
    
    RandoopRefactoringUtil.updateTypeMnemonics(fNewTypeMnemonicByOldTypeMnemonic, availableTypeMnemonics);
    RandoopRefactoringUtil.updateTypeMnemonics(fNewTypeMnemonicByOldTypeMnemonic, grayedTypeMnemonics);
    RandoopRefactoringUtil.updateTypeMnemonics(fNewTypeMnemonicByOldTypeMnemonic, checkedTypeMnemonics);
    
    RandoopRefactoringUtil.updateMethodMnemonicKeys(wc, fNewTypeMnemonicByOldTypeMnemonic);
    
    RandoopArgumentCollector.setAvailableTypes(wc, availableTypeMnemonics);
    RandoopArgumentCollector.setGrayedTypes(wc, grayedTypeMnemonics);
    RandoopArgumentCollector.setCheckedTypes(wc, checkedTypeMnemonics);
    
    if (wc.isDirty()) {
      fLaunchConfiguration = wc.doSave();
    }
    
    HashMap<String, String> reversedMap = new HashMap<String, String>();
    for (String key : fNewTypeMnemonicByOldTypeMnemonic.keySet()) {
      reversedMap.put(fNewTypeMnemonicByOldTypeMnemonic.get(key), key);
    }
    
    // create the undo change
    return new LaunchConfigurationTypeChange(fLaunchConfiguration, reversedMap);
  }

}
