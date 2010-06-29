package randoop.plugin.internal.ui.launching;

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorPart;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.ui.wizards.RandoopLaunchConfigurationWizard;

public class RandoopLaunchShortcut implements ILaunchShortcut {

  @Override
  public void launch(ISelection selection, String mode) {
    // Show the wizard to allow selection of other arguments
    RandoopLaunchConfigurationWizard wizard = new RandoopLaunchConfigurationWizard();
    
    // Instantiates the wizard container with the wizard and opens it
    WizardDialog dialog = new WizardDialog(RandoopPlugin.getActiveWorkbenchShell(), wizard);
    dialog.create();
    dialog.open();
  }

  @Override
  public void launch(IEditorPart editor, String mode) {
    // TODO Auto-generated method stub

  }
  
}
