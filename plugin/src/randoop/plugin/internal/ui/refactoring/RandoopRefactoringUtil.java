package randoop.plugin.internal.ui.refactoring;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;

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

  public static void updateMethodMnemonics(HashMap<String, String> newTypeMnemonicByOldTypeMnemonic, List<String> availableMethodMnemonics) {
    for (int i = 0; i < availableMethodMnemonics.size(); i++) {
      String methodMnemonicString = availableMethodMnemonics.get(i);
      MethodMnemonic methodMnemonic = new MethodMnemonic(methodMnemonicString);
      TypeMnemonic typeMnemonic = methodMnemonic.getDeclaringTypeMnemonic();

      String newTypeMnemonic = newTypeMnemonicByOldTypeMnemonic.get(typeMnemonic.toString());
      if (newTypeMnemonic != null) {
        String methodName = methodMnemonic.getMethodName();
        boolean isConstructor = methodMnemonic.isConstructor();
        String methodSignature = methodMnemonic.getMethodSignature();

        MethodMnemonic newMethodMnemonic = new MethodMnemonic(newTypeMnemonic, methodName, isConstructor, methodSignature);
        availableMethodMnemonics.set(i, newMethodMnemonic.toString());
      }
    }
  }
  
}
