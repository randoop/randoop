package randoop.plugin.internal.ui.launching;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.TypeMnemonic;
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
    
    final List<TypeMnemonic> typeMnemonics = new ArrayList<TypeMnemonic>();
    final Map<TypeMnemonic, List<MethodMnemonic>> methodsByDeclaringTypes= new HashMap<TypeMnemonic, List<MethodMnemonic>>();
    
    try {
      final MutableBoolean isCancelled = new MutableBoolean(true);
      IRunnableWithProgress op = new IRunnableWithProgress() {

        @Override
        public void run(IProgressMonitor monitor) {
          SubMonitor parentMonitor = SubMonitor.convert(monitor);
          parentMonitor.beginTask("Searching for class and method inputs in selection...", 2);
          List<IType> types = new ArrayList<IType>();

          SubMonitor listSearchMonitor = parentMonitor.newChild(1);
          listSearchMonitor.beginTask("Searching for class inputs in selection...", elements.length);
          for (IJavaElement element : elements) {
            SubMonitor elementSearchMonitor = listSearchMonitor.newChild(2);
            switch (element.getElementType()) {
            case IType.JAVA_PROJECT:
              try {
                String taskName = MessageFormat.format("Searching for class inputs in {0}",
                    element.getElementName());
                IPackageFragmentRoot[] pfrs = ((IJavaProject) element).getPackageFragmentRoots();
                elementSearchMonitor.beginTask(taskName, pfrs.length);
                for (IPackageFragmentRoot pfr : pfrs) {
                  if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    types.addAll(RandoopCoreUtil.findTypes(pfr, false, elementSearchMonitor.newChild(1)));
                  } else {
                    elementSearchMonitor.worked(1);
                  }
                }
              } catch (JavaModelException e) {
                RandoopPlugin.log(e);
              }
              break;
            case IType.PACKAGE_FRAGMENT_ROOT:
            case IType.PACKAGE_FRAGMENT:
              types.addAll(RandoopCoreUtil.findTypes(element, false, elementSearchMonitor.newChild(1)));
              break;
            case IType.COMPILATION_UNIT:
              types.addAll(RandoopCoreUtil.findTypes(element, false, elementSearchMonitor.newChild(1)));
              break;
            case IType.TYPE:
              types.add((IType) element);
              elementSearchMonitor.worked(1);
              break;
            default:
              RandoopPlugin.log(StatusFactory.createErrorStatus("Unexpected Java element type: " //$NON-NLS-1$
                  + element.getElementType()));
              return;
            }
          }
          listSearchMonitor.done();

          SubMonitor conversionMonitor = parentMonitor.newChild(1);
          conversionMonitor.beginTask("Converting class inputs...", types.size());
          try {
            for (int i = 0; i < types.size() && !listSearchMonitor.isCanceled(); i++) {
              IType type = types.get(i);

              TypeMnemonic typeMnemonic = new TypeMnemonic(type);
              typeMnemonics.add(typeMnemonic);

              List<MethodMnemonic> methodMnemonics = methodsByDeclaringTypes.get(typeMnemonics);
              if (methodMnemonics == null) {
                methodMnemonics = new ArrayList<MethodMnemonic>();
                methodsByDeclaringTypes.put(typeMnemonic, methodMnemonics);
              }
              
              IMethod[] methods = type.getMethods();
              SubMonitor methodMonitor = conversionMonitor.newChild(1);
              methodMonitor.beginTask(MessageFormat.format("Searching for methods in {0}", type.getElementName()), methods.length);
              for (IMethod m : methods) {
                methodMnemonics.add(new MethodMnemonic(m));
                methodMonitor.worked(1);
              }
              methodMonitor.done();
            }
          } catch (JavaModelException e) {
            RandoopPlugin.log(e);
          } finally {
            conversionMonitor.done();
          }
          parentMonitor.done();

          isCancelled.setValue(parentMonitor.isCanceled());
        }
      };
      
      IWorkbench wb = PlatformUI.getWorkbench();
      IProgressService ps = wb.getProgressService();
      ps.busyCursorWhile(op);
      
      if (!isCancelled.getValue()) {
        ILaunchConfigurationType randoopLaunchType = getLaunchType();
        ILaunchManager launchManager = getLaunchManager();

        ILaunchConfigurationWorkingCopy config = randoopLaunchType.newInstance(null,
            launchManager.generateUniqueLaunchConfigurationNameFrom("RandoopTest")); //$NON-NLS-1$

        RandoopWizardRunner runner = new RandoopWizardRunner(javaProject,
            typeMnemonics, methodsByDeclaringTypes, config);
        PlatformUI.getWorkbench().getDisplay().syncExec(runner);

        if (runner.getReturnCode() == WizardDialog.OK) {
          RandoopArgumentCollector args = new RandoopArgumentCollector(config,
              RandoopPlugin.getWorkspaceRoot());
          config.rename(launchManager.generateUniqueLaunchConfigurationNameFrom(args.getJUnitClassName()));
          config.doSave();

          DebugUITools.launch(config, "run"); //$NON-NLS-1$
        }
      }
    } catch (InvocationTargetException e) {
      RandoopPlugin.log(e);
    } catch (InterruptedException e) {
      RandoopPlugin.log(e);
    } catch (CoreException e) {
      RandoopPlugin.log(e);
    }
  }
  
  private class RandoopWizardRunner implements Runnable {
    IJavaProject fJavaProject;
    List<TypeMnemonic> fTypes;
    Map<TypeMnemonic, List<MethodMnemonic>> fMethodsByDeclaringTypes;
    ILaunchConfigurationWorkingCopy fConfig;
    int fReturnCode;

    public RandoopWizardRunner(IJavaProject javaProject, List<TypeMnemonic> typeMnemonics,
        Map<TypeMnemonic, List<MethodMnemonic>> methodsByDeclaringTypes,
        ILaunchConfigurationWorkingCopy config) {
      fJavaProject = javaProject;
      fTypes = typeMnemonics;
      fMethodsByDeclaringTypes = methodsByDeclaringTypes;
      fConfig = config;
      fReturnCode = -1;
    }

    @Override
    public void run() {
      try {
        // The shell is not null
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        Assert.isNotNull(shell);

        RandoopLaunchConfigurationWizard wizard = new RandoopLaunchConfigurationWizard(fJavaProject, fTypes, fMethodsByDeclaringTypes, fConfig);
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
