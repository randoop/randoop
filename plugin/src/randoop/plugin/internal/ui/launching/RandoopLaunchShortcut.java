package randoop.plugin.internal.ui.launching;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.ui.wizards.RandoopLaunchConfigurationWizard;

public class RandoopLaunchShortcut implements ILaunchShortcut {

  @Override
  public void launch(ISelection selection, String mode) {
    Assert.isTrue(selection instanceof IStructuredSelection);
    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    
    IJavaProject project = null;
    Object[] selected = structuredSelection.toArray();
    final IJavaElement[] elements;

    if (selected.length == 1 && selected[0] instanceof IJavaElement) {
      project = (IJavaProject) selected[0];
      elements = new IJavaElement[0];
    } else {
      // Ensure every selected object is an instance of IJavaElement that is
      // contained in the same project as the other selected objects
      elements = new IJavaElement[selected.length];

      for (int i = 0; i < selected.length; i++) {
        Assert.isTrue(selected[i] instanceof IJavaElement);
        IJavaElement e = (IJavaElement) selected[i];

        if (project == null) {
          project = e.getJavaProject();
        } else {
          Assert.isTrue(e.getJavaProject().equals(project),
              "All selected elements must be contained in the same Java project."); //$NON-NLS-1$
        }
        elements[i] = e;
      }
    }
    
    final IJavaProject javaProject = project;
    
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        // The shell is not null
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        assertNotNull(shell);
        
        RandoopLaunchConfigurationWizard wizard = new RandoopLaunchConfigurationWizard(javaProject, elements);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        
        dialog.create();
        dialog.open();
      }
    });
  }

  @Override
  public void launch(IEditorPart editor, String mode) {
    System.out.println(":Launching2");
  }

  /**
   * Returns a listing of <code>ILaunchConfiguration</code>s that correspond to
   * the specified test inputs.
   */
  protected List<ILaunchConfiguration> collectConfigurations(/* test inputs */) {
    ILaunchManager manager = getLaunchManager();
    ILaunchConfigurationType type = getLaunchType();
    if (type != null) {
      try {
        ILaunchConfiguration[] configs = manager.getLaunchConfigurations(type);
        ArrayList<ILaunchConfiguration> list = new ArrayList<ILaunchConfiguration>();
        
        for (ILaunchConfiguration config : configs) {
          if (config.exists()) {
            // Check if this matches

            // list.add(configs[i]);
          }
        }
        return list;
      } catch (CoreException e) {
      }
    }
    return new ArrayList<ILaunchConfiguration>();
  }

  /**
   * Prompts the user to choose a launch configuration to run from the given
   * list and returns the chosen configuration or <code>null</code> if the given
   * list is empty or Cancel was pressed
   * 
   * @param configs
   *          list of configurations to choose from
   * 
   * @return the chosen configuration or <code>null</code>
   */
  public static ILaunchConfiguration chooseConfig(
      List<ILaunchConfiguration> configs) {
    if (configs.isEmpty()) {
      return null;
    }
    ILabelProvider labelProvider = DebugUITools.newDebugModelPresentation();
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(RandoopPlugin.getDisplay().getActiveShell(), labelProvider);
    dialog.setElements(configs.toArray(new ILaunchConfiguration[configs.size()]));
    dialog.setTitle("Randoop Configuration Selection");
    dialog.setMessage("&Choose an Randoop configuration to run:");
    dialog.setMultipleSelection(false);
    
    int result = dialog.open();
    labelProvider.dispose();
    
    if (result == Window.OK) {
      return (ILaunchConfiguration) dialog.getFirstResult();
    }
    return null;
  }

  /**
   * Creates and returns a new Randoop launch configuration for the given test
   * inputs.
   * 
   * @return new launch configuration
   */
  private ILaunchConfiguration newConfiguration(/* test inputs */) {
    ILaunchConfigurationType type = getLaunchType();
    try {
      ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null,
          "Name");
      
      // Set attributes
      
      
      // Set mapped attributes
      return workingCopy.doSave();
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return null;
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
