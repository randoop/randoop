package randoop.plugin.internal.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.internal.core.MutableBoolean;

public class MessageUtil {
  
  public static boolean openQuestion(final String message) {
    final MutableBoolean okToProceed = new MutableBoolean(false);
    
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        okToProceed.setValue(MessageDialog.openQuestion(
            PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Randoop", message));
      }
    });

    return okToProceed.getValue();
  }
  
}
