package randoop.plugin.internal.ui.launching;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.MutableBoolean;
import randoop.plugin.internal.core.RandoopCoreUtil;
import randoop.plugin.internal.core.RandoopStatus;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.AdaptablePropertyTester;
import randoop.plugin.internal.ui.wizards.RandoopLaunchConfigurationWizard;

public class RandoopLaunchShortcut implements ILaunchShortcut {

  public void launch(ISelection selection, String mode) {
    Assert.isTrue(selection instanceof IStructuredSelection);
    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;

    IJavaProject javaProject = null;
    Object[] selected = structuredSelection.toArray();
    final IJavaElement[] elements;

    if (selected.length == 1 && selected[0] instanceof IJavaProject) {
      javaProject = (IJavaProject) selected[0];
      elements = new IJavaElement[1];
      elements[0] = javaProject;
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

    final List<String> checkedTypeMnemonics = new ArrayList<String>();
    final List<String> grayedTypeMnemonics = new ArrayList<String>();
    final Map<String, List<String>> selectedMethodsByDeclaringTypes = new HashMap<String, List<String>>();

    final MutableBoolean isCancelled = new MutableBoolean(true);

    IRunnableWithProgress op = new IRunnableWithProgress() {

      public void run(IProgressMonitor monitor) {
        SubMonitor parentMonitor = SubMonitor.convert(monitor);
        parentMonitor.beginTask("Searching for class and method inputs in selection...",
            2);
        List<IType> types = new ArrayList<IType>();
        List<IType> selectedTypes = new ArrayList<IType>();

        SubMonitor listSearchMonitor = parentMonitor.newChild(1);
        listSearchMonitor.beginTask("Searching for class inputs in selection...",
            elements.length);
        for (IJavaElement element : elements) {
          SubMonitor elementSearchMonitor = listSearchMonitor.newChild(2);
          switch (element.getElementType()) {
          case IJavaElement.JAVA_PROJECT:
            try {
              String taskName = MessageFormat.format("Searching for class inputs in {0}",
                  element.getElementName());
              IPackageFragmentRoot[] pfrs = ((IJavaProject) element)
                  .getPackageFragmentRoots();
              elementSearchMonitor.beginTask(taskName, pfrs.length);
              for (IPackageFragmentRoot pfr : pfrs) {
                if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
                  types.addAll(RandoopCoreUtil.findTypes(pfr, false,
                      elementSearchMonitor.newChild(1)));
                } else {
                  elementSearchMonitor.worked(1);
                }
              }
            } catch (JavaModelException e) {
              IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
              RandoopPlugin.log(s);
            }
            break;
          case IJavaElement.PACKAGE_FRAGMENT_ROOT:
          case IJavaElement.PACKAGE_FRAGMENT:
            List<IType> foundTypes = RandoopCoreUtil.findTypes(element, false,
                elementSearchMonitor.newChild(1));
            types.addAll(RandoopCoreUtil.findTypes(element, false,
                elementSearchMonitor.newChild(1)));
            selectedTypes.addAll(foundTypes);
            break;
          case IJavaElement.COMPILATION_UNIT:
            foundTypes = RandoopCoreUtil.findTypes(element, false,
                elementSearchMonitor.newChild(1));
            types.addAll(foundTypes);
            selectedTypes.addAll(foundTypes);
            break;
          case IJavaElement.TYPE:
            types.add((IType) element);
            selectedTypes.add((IType) element);

            selectedMethodsByDeclaringTypes.remove((IType) element);
            elementSearchMonitor.worked(1);
            break;
          case IJavaElement.METHOD:
            IMethod m = (IMethod) element;
            IType type = m.getDeclaringType();

            if (!selectedTypes.contains(type)) {
              try {
                if (AdaptablePropertyTester.isTestable(m)) {
                  List<String> methodMnemonics = selectedMethodsByDeclaringTypes
                      .get(type);

                  String typeMnemonicString = new TypeMnemonic(type).toString();

                  if (methodMnemonics == null) {
                    methodMnemonics = new ArrayList<String>();
                    selectedMethodsByDeclaringTypes.put(typeMnemonicString,
                        methodMnemonics);
                  }
                  methodMnemonics.add(new MethodMnemonic(m).toString());
                  if (!types.contains(type)) {
                    types.add(type);
                  }
                }
              } catch (JavaModelException e) {
                IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
                RandoopPlugin.log(s);
              }
            }
            break;
          }
        }
        listSearchMonitor.done();

        SubMonitor conversionMonitor = parentMonitor.newChild(1);
        conversionMonitor.beginTask("Converting class inputs...", types.size());
        try {
          for (int i = 0; i < types.size() && !listSearchMonitor.isCanceled(); i++) {
            IType type = types.get(i);

            TypeMnemonic typeMnemonic = new TypeMnemonic(type);
            checkedTypeMnemonics.add(typeMnemonic.toString());

            List<String> methods = selectedMethodsByDeclaringTypes.get(type);
            if (methods != null && !methods.isEmpty()) {
              grayedTypeMnemonics.add(typeMnemonic.toString());
            }
          }
        } catch (JavaModelException e) {
          IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
          RandoopPlugin.log(s);
        } finally {
          conversionMonitor.done();
        }
        parentMonitor.done();

        isCancelled.setValue(parentMonitor.isCanceled());
      }
    };

    IWorkbench wb = PlatformUI.getWorkbench();
    IProgressService ps = wb.getProgressService();
    try {
      ps.busyCursorWhile(op);

      if (!isCancelled.getValue()) {
        ILaunchConfigurationType randoopLaunchType = getLaunchType();
        ILaunchManager launchManager = getLaunchManager();

        ILaunchConfigurationWorkingCopy config = randoopLaunchType.newInstance(null,
            launchManager.generateUniqueLaunchConfigurationNameFrom("RandoopTest")); //$NON-NLS-1$

        RandoopWizardRunner runner = new RandoopWizardRunner(javaProject,
            checkedTypeMnemonics, grayedTypeMnemonics, selectedMethodsByDeclaringTypes,
            config);
        PlatformUI.getWorkbench().getDisplay().syncExec(runner);

        if (runner.getReturnCode() == WizardDialog.OK) {
          RandoopArgumentCollector args = new RandoopArgumentCollector(config,
              getWorkspaceRoot());
          
          // Use the depreciated generateUniqueLaunchConfigurationNameFrom since
          // it is still supported in Galileo
          config.rename(launchManager.generateUniqueLaunchConfigurationNameFrom(args
              .getJUnitClassName()));
          config.doSave();

          DebugUITools.launch(config, "run"); //$NON-NLS-1$
        }
      }
    } catch (InvocationTargetException e) {
    } catch (InterruptedException e) {
    } catch (CoreException e) {
    }
      
  }
  
  private class RandoopWizardRunner implements Runnable {
    IJavaProject fJavaProject;
    List<String> fCheckedTypes;
    List<String> fGrayedTypes;
    Map<String, List<String>> fSelectedMethodsByDeclaringTypes;
    ILaunchConfigurationWorkingCopy fConfig;
    int fReturnCode;

    public RandoopWizardRunner(IJavaProject javaProject,
        List<String> checkedTypeMnemonics, List<String> grayedTypeMnemonics,
        Map<String, List<String>> selectedMethodsByDeclaringTypes,
        ILaunchConfigurationWorkingCopy config) {
      
      fJavaProject = javaProject;
      fCheckedTypes = checkedTypeMnemonics;
      fGrayedTypes = grayedTypeMnemonics;
      fSelectedMethodsByDeclaringTypes = selectedMethodsByDeclaringTypes;
      fConfig = config;
      fReturnCode = -1;
    }

    public void run() {
      // The shell is not null
      Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      Assert.isNotNull(shell);

      RandoopLaunchConfigurationWizard wizard = new RandoopLaunchConfigurationWizard(
          fJavaProject, fCheckedTypes, fGrayedTypes, fSelectedMethodsByDeclaringTypes,
          fConfig);
      WizardDialog dialog = new WizardDialog(shell, wizard);

      dialog.create();
      fReturnCode = dialog.open();
    }

    public int getReturnCode() {
      return fReturnCode;
    }
  }

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
  
  private IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
  
}
