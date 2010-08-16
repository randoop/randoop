package randoop.plugin.internal.ui.refactoring;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class RandoopRefactoringUtil {
  /*
   * See org.eclipse.jdt.internal.debug.core.refactoring.JDTDebugRefactoringUtil
   * for examples.
   */
  
  /**
   * Take a list of Changes, and return a unique Change, a CompositeChange, or null.
   */
  public static Change createChangeFromList(List<Change> changes, String changeLabel) {
    int nbChanges = changes.size();
    if (nbChanges == 0) {
      return null;
    } else if (nbChanges == 1) {
      return (Change) changes.get(0);
    } else {
      return new CompositeChange(changeLabel, (Change[]) changes.toArray(new Change[changes.size()]));
    }
  }
  
  public static ILaunchConfiguration[] getRandoopTypeLaunchConfigurations() {
    try {
      ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
      ILaunchConfigurationType randoopLaunchType = launchManager
          .getLaunchConfigurationType(IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION);

      return launchManager.getLaunchConfigurations(randoopLaunchType);
    } catch (CoreException e) {
      RandoopPlugin.log(e);
    }
    return new ILaunchConfiguration[0];
  }
  
  public static void updateTypeMnemonics(HashMap<String, String> newTypeMnemonicByOldTypeMnemonic, List<String> availableTypeMnemonics) {
    for (int i = 0; i < availableTypeMnemonics.size(); i++) {
      String mnemonic = availableTypeMnemonics.get(i);
      String newMnemonic = newTypeMnemonicByOldTypeMnemonic.get(mnemonic);
      if (newMnemonic != null) {
        availableTypeMnemonics.set(i, newMnemonic);
      }
    }
  }
  
  public static void updateMethodMnemonicKeys(ILaunchConfigurationWorkingCopy wc, HashMap<String, String> newTypeMnemonicByOldTypeMnemonic) {
    for (String oldMnemonic : newTypeMnemonicByOldTypeMnemonic.keySet()) {
      String newMnemonic = newTypeMnemonicByOldTypeMnemonic.get(oldMnemonic);
      
      List<String> availableMethods = RandoopArgumentCollector.getAvailableMethods(wc, oldMnemonic);
      List<String> checkedMethods = RandoopArgumentCollector.getCheckedMethods(wc, oldMnemonic);
      
      RandoopArgumentCollector.setAvailableMethods(wc, newMnemonic, availableMethods);
      RandoopArgumentCollector.setCheckedMethods(wc, newMnemonic, checkedMethods);
      
      RandoopArgumentCollector.deleteAvailableMethods(wc, oldMnemonic);
      RandoopArgumentCollector.deleteCheckedMethods(wc, oldMnemonic);
    }
  }

}
