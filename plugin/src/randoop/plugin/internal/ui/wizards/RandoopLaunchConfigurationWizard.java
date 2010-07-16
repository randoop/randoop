package randoop.plugin.internal.ui.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.launching.RandoopLaunchConfigurationUtil;

public class RandoopLaunchConfigurationWizard extends Wizard {
  protected static final String DIALOG_SETTINGS_KEY = "RandoopWizard"; //$NON-NLS-1$

  ILaunchConfigurationWorkingCopy fConfig;
  OptionWizardPage fMainPage;
  OptionWizardPage fTestInputsPage;

  public RandoopLaunchConfigurationWizard(IJavaProject project, IJavaElement[] elements, ILaunchConfigurationWorkingCopy config) throws CoreException {
    super();
    
    fConfig = config;
    
    // Set the project in the configuration
    // RandoopArgumentCollector.setProjectName(fConfig,
    // project.getElementName());
    //
    // // Set the available and selected types in the configurations
    // List<String> availableTypes = new ArrayList<String>();
    // List<String> selectedTypes = new ArrayList<String>();
    //
    // for (IJavaElement element : elements) {
    // switch (element.getElementType()) {
    // case IJavaElement.PACKAGE_FRAGMENT_ROOT:
    // case IJavaElement.PACKAGE_FRAGMENT:
    // for (IType type : RandoopLaunchConfigurationUtil.findTypes(element,
    // false, null)) {
    // TypeMnemonic mnemonic = new TypeMnemonic(type);
    // availableTypes.add(mnemonic.toString());
    // }
    // break;
    // case IJavaElement.COMPILATION_UNIT:
    // for (IType type : RandoopLaunchConfigurationUtil.findTypes(element,
    // false, null)) {
    // TypeMnemonic mnemonic = new TypeMnemonic(type);
    // availableTypes.add(mnemonic.toString());
    // selectedTypes.add(mnemonic.toString());
    // }
    // break;
    // default:
    //        RandoopPlugin.log(StatusFactory.createErrorStatus("Unexpected Java element type: " //$NON-NLS-1$
    // + element.getElementType()));
    // return;
    // }
    // }
    //
    // RandoopArgumentCollector.setAvailableTypes(fConfig, availableTypes);
    // RandoopArgumentCollector.setSelectedMethods(fConfig, selectedTypes);
    
    fMainPage = new MainPage("Main", project, fConfig);
    fTestInputsPage = new TestInputsPage("Test Inputs", project, elements, fConfig);
    fTestInputsPage.setPreviousPage(fMainPage);
    
    addPage(fMainPage);
    addPage(fTestInputsPage);

    setNeedsProgressMonitor(true);
    setHelpAvailable(true);

    setTitleBarColor(new RGB(167, 215, 250));
    setWindowTitle("New Randoop Launch Configuration");
  }

  @Override
  public boolean performFinish() {
    if(!fMainPage.isValid(fConfig)) {
      return false;
    }
    
    if(!fTestInputsPage.isValid(fConfig)) {
      return false;
    }
    
    return true;
  }
  
}
