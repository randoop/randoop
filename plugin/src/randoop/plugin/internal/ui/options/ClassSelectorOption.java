package randoop.plugin.internal.ui.options;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class ClassSelectorOption implements IOption {
  private ILaunchConfigurationDialog fDialog;
  private Shell fShell;
  private ClassSelector fTypeSelector;
  private Tree fTypeTree;
  private Button fClassUp;
  private Button fClassDown;
  private Button fClassAddFromProject;
  private Button fClassAddFromClasspaths;
  private Button fClassAddFromSystemLibraries;
  private Button fClassRemove;
  
  public ClassSelectorOption(Composite parent, ILaunchConfigurationDialog dialog,  
      final SelectionListener listener) {
    fDialog = dialog;
    Group comp = SWTFactory.createGroup(parent, "Test Inputs", 2, 1, GridData.FILL_HORIZONTAL);
    fShell = comp.getShell();

    final Composite leftcomp = SWTFactory.createComposite(comp, 1, 1, GridData.FILL_BOTH);
    GridLayout ld = (GridLayout) leftcomp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;
    GridData gd = (GridData) leftcomp.getLayoutData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;

    final Composite rightcomp = SWTFactory.createComposite(comp, 1, 1, GridData.FILL);
    gd = (GridData) rightcomp.getLayoutData();
    gd.horizontalAlignment = SWT.LEFT;
    gd.verticalAlignment = SWT.TOP;

    fTypeTree = new Tree(leftcomp, SWT.MULTI | SWT.CHECK | SWT.BORDER);
    fTypeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    // Add type selector functionalities to this tree. This will also add a
    // Listener to fTypeTree that adds default tree behavior. That is, checking/
    // unchecking a parent item also checks/unchecks all descendant items
    fTypeSelector = new ClassSelector(fTypeTree);
    
    fTypeTree.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent e) {
        TreeItem[] moveables = fTypeSelector.getMoveableSelection();
        boolean enabled = moveables != null;
        fClassUp.setEnabled(enabled);
        fClassDown.setEnabled(enabled);
        
        fClassRemove.setEnabled(fTypeSelector.canRemoveFromSelection());
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
    });
    fTypeTree.addSelectionListener(listener);
    
    fClassUp = SWTFactory.createPushButton(rightcomp, "Up", null);
    fClassUp.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fTypeSelector.moveSelectedTypesUp();
      }
    });
    fClassUp.addSelectionListener(listener);
    
    fClassDown = SWTFactory.createPushButton(rightcomp, "Down", null);
    fClassDown.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fTypeSelector.moveSelectedTypesDown();
      }
    });
    fClassDown.addSelectionListener(listener);
    
    fClassRemove = SWTFactory.createPushButton(rightcomp, "Remove", null);
    fClassRemove.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fTypeSelector.removeSelectedTypes();
      }
    });
    fClassRemove.addSelectionListener(listener);
    
    SWTFactory.createHorizontalSpacer(rightcomp, 0);
    SWTFactory.createLabel(rightcomp, "Add classes from:", 1);
    
    fClassAddFromProject = SWTFactory.createPushButton(rightcomp, "Selected Project...", null);
    fClassAddFromProject.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSearchButtonSelected();
      }
    });
    fClassAddFromProject.addSelectionListener(listener);
    
    fClassAddFromClasspaths = SWTFactory.createPushButton(rightcomp, "Referenced Classpaths...", null);
    fClassAddFromClasspaths.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
      }
    });
    fClassAddFromClasspaths.addSelectionListener(listener);
    
    fClassAddFromSystemLibraries = SWTFactory.createPushButton(rightcomp, "System Libraries...", null);
    fClassAddFromSystemLibraries.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
      }
    });
    fClassAddFromSystemLibraries.addSelectionListener(listener);
  }

  @Override
  public IStatus canSave() {
    if (fDialog == null || fShell == null || fTypeSelector == null
        || fTypeTree == null || fClassUp == null || fClassDown == null
        || fClassDown == null || fClassAddFromProject == null
        || fClassAddFromClasspaths == null
        || fClassAddFromSystemLibraries == null || fClassRemove == null) {
      return StatusFactory.createErrorStatus("TestInputOption incorrectly initialized");
    }
    
    List<String> selectedTypes = fTypeSelector.getCheckedClasses();
    List<String> selectedMethods = fTypeSelector.getCheckedMethods();
    
    return validate(selectedTypes, selectedMethods);
  }

  @Override
  public IStatus isValid(ILaunchConfiguration config) {
    List<String> selectedTypes = RandoopArgumentCollector.getSelectedTypes(config);
    List<String> selectedMethods = RandoopArgumentCollector.getSelectedMethods(config);

    return validate(selectedTypes, selectedMethods);
  }

  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    if (fTypeTree != null) {
      List<String> availableTypes = RandoopArgumentCollector.getAvailableTypes(config);
      List<String> selectedTypes = RandoopArgumentCollector.getSelectedTypes(config);
      List<String> selectedMethods = RandoopArgumentCollector.getSelectedMethods(config);

      fTypeSelector = new ClassSelector(fTypeTree, availableTypes, selectedTypes, selectedMethods);
    }
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fTypeSelector == null) {
      setDefaults(config);
    } else {
      RandoopArgumentCollector.setAvailableTypes(config, fTypeSelector.getAllTypes());
      RandoopArgumentCollector.setSelectedTypes(config, fTypeSelector.getCheckedClasses());
      RandoopArgumentCollector.setSelectedMethods(config, fTypeSelector.getCheckedMethods());
    }
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreSelectedTypes(config);
    RandoopArgumentCollector.restoreAvailableTypes(config);
    RandoopArgumentCollector.restoreSelectedMethods(config);
  }

  /**
   * Returns an OK <code>IStatus</code> if the specified arguments could be
   * passed to Randoop without raising any error. If the arguments are not
   * valid, an ERROR status is returned with a message indicating what is wrong.
   * 
   * @param selectedTypes
   * @param selectedMethods 
   * @return
   */
  protected IStatus validate(List<String> selectedTypes, List<String> selectedMethods) {
    boolean validTypes = selectedTypes == null || !selectedTypes.isEmpty();
    boolean validMethods = selectedMethods == null || !selectedMethods.isEmpty();

    if (!validTypes && !validMethods) {
      return StatusFactory
          .createErrorStatus("At least one existing type or method must be selected.");
    }

    for (String handlerId : selectedTypes) {
      if (!JavaCore.create((String) handlerId).exists()) {
        return StatusFactory
            .createErrorStatus("One of the selected types does not exist.");
      }
    }

    for (String handlerId : selectedMethods) {
      if (!JavaCore.create((String) handlerId).exists()) {
        return StatusFactory
            .createErrorStatus("One of the selected methods does not exist.");
      }
    }

    return StatusFactory.createOkStatus();
  }

  /**
   * Opens a <code>FilteredItemsSelectionDialog</code> that lists all types in
   * the workspace. The user can select multiple items from the list. Upon
   * pressing OK, the selected types are added to this tab's SWT
   * <code>Tree</code>.
   */
  protected void handleSearchButtonSelected() {
    DebugTypeSelectionDialog mmsd = new DebugTypeSelectionDialog(getShell(),
        getAllAvailableTypes(fDialog), "Select types to test");
    if (mmsd.open() == Window.CANCEL) {
      return;
    }

    // Add all of the types to the type selector
    Object[] results = mmsd.getResult();
    if (results.length > 0) {
      for (Object element : results) {
        if (element instanceof IType) {
          IType type = (IType) element;
          if (type != null) {
            fTypeSelector.addType(type, false);
          }
        }
      }
    }
  }

  /**
   * Returns an array of all available <code>IType</code>s in the active
   * workspace. The specified dialog will be used to display this operation
   * progress. This method will return <code>null</code> if the dialog is
   * <code>null</code>. If an error occurs, an empty or partially complete array
   * may be returned.
   * 
   * @param dialog
   *          the <code>ILaunchConfigurationDialog</code> that will be used to
   *          display the progress of this operation - must not be
   *          <code>null</code>
   * @return a complete or partially complete list of <code>IType</code>s in the
   *         workspace, or <code>null</code>
   */
  private static IType[] getAllAvailableTypes(ILaunchConfigurationDialog dialog) {
    if (dialog == null)
      return null;

    final List<IType> availableTypes = new ArrayList<IType>();

    IJavaModel model = JavaCore.create(getWorkspaceRoot());
    if (model != null) {
      try {
        final IJavaProject[] javaProjects = model.getJavaProjects();
        
        // Create a new runnable object that will be used to search for the Java
        // types in the active workspace.
        IRunnableWithProgress typeSearcher = new IRunnableWithProgress() {
          @Override
          public void run(IProgressMonitor pm) throws InvocationTargetException {
            pm.beginTask("Searching for Java types...", IProgressMonitor.UNKNOWN);
            
            // Search each IJavaProject for ITypes and add them to a list.
            // Each added IType represents one unit of work.
            try {
              for (IJavaProject jp : javaProjects) {
                for (IPackageFragment pf : jp.getPackageFragments()) {
                  for (ICompilationUnit cu : pf.getCompilationUnits()) {
                    for (IType t : cu.getAllTypes()) {
                      availableTypes.add(t);
                      pm.worked(1);
                    }
                  }
                }
              }
            } catch (JavaModelException e) {
              RandoopPlugin.log(e);
            }

            pm.done();
          }
        };
        
        // Search for available ITypes while displaying progress in the dialog
        try {
            dialog.run(true, true, typeSearcher);
        } catch (InvocationTargetException e) {
          RandoopPlugin.log(e);
        } catch (InterruptedException e) {
          RandoopPlugin.log(e);
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    
    return availableTypes.toArray(new IType[availableTypes.size()]);
  }
  
  /*
   * Convenience method to get the workspace root.
   */
  private static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
  
  public Shell getShell() {
    return fShell;
  }
}
