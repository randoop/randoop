package randoop.plugin.internal.ui.launching;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.wizards.RandoopLaunchConfigurationWizard;

public class RandoopLaunchShortcut implements ILaunchShortcut {

  @Override
  public void launch(ISelection selection, String mode) {
    Assert.isTrue(selection instanceof IStructuredSelection);
    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    
    IJavaProject javaProject = null;
    Object[] selected = structuredSelection.toArray();
    final IJavaElement[] elements;

    if (selected.length == 1 && selected[0] instanceof IJavaProject) {
      javaProject = (IJavaProject) selected[0];
      elements = new IJavaElement[0];
    } else {
      // Ensure every selected object is an instance of IJavaElement that is
      // contained in the same project as the other selected objects
      elements = new IJavaElement[selected.length];

      for (int i = 0; i < selected.length; i++) {
        Assert.isTrue(selected[i] instanceof IJavaElement);
        IJavaElement e = (IJavaElement) selected[i];

        if (javaProject == null) {
          javaProject = e.getJavaProject();
        } else {
          Assert.isTrue(e.getJavaProject().equals(javaProject),
              "All selected elements must be contained in the same Java project."); //$NON-NLS-1$
        }
        elements[i] = e;
      }
    }

    try {
      ILaunchConfigurationType randoopLaunchType = getLaunchType();
      ILaunchManager launchManager = getLaunchManager();

      ILaunchConfigurationWorkingCopy config = randoopLaunchType.newInstance(null,
          launchManager.generateLaunchConfigurationName("RandoopTest")); //$NON-NLS-1$

      RandoopWizardRunner runner = new RandoopWizardRunner(javaProject, elements, config);
      PlatformUI.getWorkbench().getDisplay().syncExec(runner);

      if (runner.getReturnCode() == WizardDialog.OK) {
        RandoopArgumentCollector args = new RandoopArgumentCollector(config, RandoopPlugin.getWorkspaceRoot());
        config.rename(launchManager.generateLaunchConfigurationName(args.getJUnitClassName()));
        config.doSave();

        DebugUITools.launch(config, "run"); //$NON-NLS-1$
      }
    } catch (CoreException ce) {
      RandoopPlugin.log(ce);
    }
  }
  
  private class RandoopWizardRunner implements Runnable {
    IJavaProject fJavaProject;
    IJavaElement[] fElements;
    ILaunchConfigurationWorkingCopy fConfig;
    int fReturnCode;

    public RandoopWizardRunner(IJavaProject javaProject, IJavaElement[] elements, ILaunchConfigurationWorkingCopy config) {
      fJavaProject = javaProject;
      fElements = elements;
      fConfig = config;
      fReturnCode = -1;
    }

    @Override
    public void run() {
      try {
        // The shell is not null
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        Assert.isNotNull(shell);

        RandoopLaunchConfigurationWizard wizard = new RandoopLaunchConfigurationWizard(fJavaProject, fElements, fConfig);
        WizardDialog dialog = new WizardDialog(shell, wizard);

        dialog.create();
        fReturnCode = dialog.open();
      } catch (CoreException e) {
        RandoopPlugin.log(e);
      }
    }

    public int getReturnCode() {
      return fReturnCode;
    }
  }

  @Override
  public void launch(IEditorPart editor, String mode) {
  }

  /**
   * Returns the Randoop launch configuration type.
   * 
   * @return the Randoop launch configuration type
   */
  private ILaunchConfigurationType getLaunchType() {
    ILaunchManager manager = getLaunchManager();
    ILaunchConfigurationType type = manager.getLaunchConfigurationType(IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION);
    return type;
  }
  
  /**
   * Returns the launch manager.
   * 
   * @return launch manager
   */
  private ILaunchManager getLaunchManager() {
    ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
    return manager;
  }
  
}
