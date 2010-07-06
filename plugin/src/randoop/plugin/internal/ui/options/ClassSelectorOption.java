package randoop.plugin.internal.ui.options;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.operation.IRunnableContext;
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
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class ClassSelectorOption extends Option implements IOptionChangeListener {
  private IRunnableContext fRunnableContext;
  private Shell fShell;
  private ClassSelector fTypeSelector;
  private Tree fTypeTree;
  private Button fClassUp;
  private Button fClassDown;
  private Button fClassAddFromProject;
  private Button fClassAddFromClasspaths;
  private Button fClassAddFromJRESystemLibrary;
  private Button fClassRemove;
  private Button fIgnoreJUnitTestCases;
  private IJavaProject fProject;
  
  public ClassSelectorOption(Composite parent, IRunnableContext runnableContext,
      final SelectionListener listener, IJavaProject project) {
    this(parent, runnableContext, listener);
    fProject = project;
  }
  
  public ClassSelectorOption(Composite parent, IRunnableContext runnableContext,  
      final SelectionListener listener) {
    fRunnableContext = runnableContext;
    Group comp = SWTFactory.createGroup(parent, "Test Inputs", 2, 1, GridData.FILL_BOTH);
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
        IType[] classes = getClassesFromProject(fIgnoreJUnitTestCases.getSelection());
        if (classes != null) {
          handleSearchButtonSelected(classes);
        }
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
    
    fClassAddFromJRESystemLibrary = SWTFactory.createPushButton(rightcomp, "JRE System Library...", null);
    fClassAddFromJRESystemLibrary.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IType[] classes = getClassesFromJRE(fIgnoreJUnitTestCases.getSelection());
        if (classes != null) {
          handleSearchButtonSelected(classes);
        }
      }
    });
    fClassAddFromJRESystemLibrary.addSelectionListener(listener);
    
    fIgnoreJUnitTestCases = SWTFactory.createCheckButton(leftcomp,
        "Ignore JUnit tests cases when searching for Java types", null, true, 2);
    gd = (GridData) fIgnoreJUnitTestCases.getLayoutData();
    gd.horizontalIndent = 5;
    fIgnoreJUnitTestCases.setLayoutData(gd);
  }

  @Override
  public IStatus canSave() {
    if (fRunnableContext == null || fShell == null || fTypeSelector == null
        || fTypeTree == null || fClassUp == null || fClassDown == null
        || fClassDown == null || fClassAddFromProject == null
        || fClassAddFromClasspaths == null
        || fClassAddFromJRESystemLibrary == null || fClassRemove == null) {
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
  protected void handleSearchButtonSelected(IType[] types) {
    DebugTypeSelectionDialog mmsd = new DebugTypeSelectionDialog(getShell(),
        types, "Select types to test");
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
   * Returns an array of available <code>IType</code>s that have been decalred
   * in the selected project. The specified dialog will be used to display this
   * operation progress. This method will return <code>null</code> if the dialog
   * is <code>null</code> or no Java project is selected. If an error occurs, an
   * empty or partially complete array may be returned.
   * 
   * @param dialog
   *          the <code>ILaunchConfigurationDialog</code> that will be used to
   *          display the progress of this operation - must not be
   *          <code>null</code>
   * @return a complete or partially complete list of <code>IType</code>s in the
   *         workspace, or <code>null</code>
   */
  private IType[] getClassesFromProject(final boolean ignoreJUnitTestCases) {
    // Create a new runnable object that will be used to search for the Java
    // types in the active workspace.
    ClassSearcher typeSearcher = new ClassSearcher(fProject) {
      @Override
      public void run(IProgressMonitor pm) throws InvocationTargetException {
        pm.beginTask("Searching for Java types...", IProgressMonitor.UNKNOWN);

        // Search each IJavaProject for ITypes and add them to a list.
        // Each added IType represents one unit of work.
        try {
          for (IPackageFragmentRoot pfr : getJavaProject().getPackageFragmentRoots()) {
            if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
              addTypes(findTypes(pfr, ignoreJUnitTestCases, pm));

              if (pm.isCanceled()) {
                cancel();
                break;
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
    return findClasses(typeSearcher);
  }
  
  private IType[] getClassesFromJRE(final boolean ignoreJUnitTestCases) {
    // Create a new runnable object that will be used to search for the Java
    // types in the active workspace.
    ClassSearcher typeSearcher = new ClassSearcher(fProject) {
      @Override
      public void run(IProgressMonitor pm) throws InvocationTargetException {
        pm.beginTask("Searching for Java types...", IProgressMonitor.UNKNOWN);

        // Search each IJavaProject for ITypes and add them to a list.
        // Each added IType represents one unit of work.
        try {
          for (IClasspathEntry cpentry : getJavaProject().getRawClasspath()) {
            IPath path = cpentry.getPath();
            if (path.segmentCount() > 0) {
              if (path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
                for (IPackageFragmentRoot pfr : getJavaProject().findPackageFragmentRoots(cpentry)) {
                  // TODO: It takes a VERY long time to search rt.jar
                  addTypes(findTypes(pfr, ignoreJUnitTestCases, pm));
                  
                  if (pm.isCanceled()) {
                    cancel();
                    break;
                  }
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
    return findClasses(typeSearcher);
  }
  
  public static List<IType> findTypes(IPackageFragmentRoot pfr, boolean ignoreJUnitTestCases, IProgressMonitor pm) throws JavaModelException {
    if (pm == null) {
      pm = new NullProgressMonitor();
    }
    
    List<IType> types = new ArrayList<IType>();
    if (pfr.getKind() == IPackageFragmentRoot.K_BINARY) {
      for (IJavaElement e : pfr.getChildren()) {
        Assert.isTrue(e instanceof IPackageFragment);
        IPackageFragment pf = (IPackageFragment) e;
        for (IClassFile cf : pf.getClassFiles()) {
          IType t = cf.getType();
          if (isValid(t, ignoreJUnitTestCases)) {
            types.add(t);
            
            pm.worked(1);
            if (pm.isCanceled()) {
              return types;
            }
          }
        }
      }
    } else if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
      for (IJavaElement e : pfr.getChildren()) {
        Assert.isTrue(e instanceof IPackageFragment);
        IPackageFragment pf = (IPackageFragment) e;
        for (ICompilationUnit cu : pf.getCompilationUnits()) {
          for (IType t : cu.getAllTypes()) {
            if (isValid(t, ignoreJUnitTestCases)) {
              types.add(t);
              
              pm.worked(1);
              if (pm.isCanceled()) {
                return types;
              }
            }
          }
        }
      }
    }
    
    return types;
  }
  
  public static boolean isValid(IType t, boolean ignoreJUnitTestCases) throws JavaModelException {
    if (t.isInterface() || Flags.isAbstract(t.getFlags())) {
      return false;
    }
    if (ignoreJUnitTestCases) {
      // TODO: make sure this is actually of type
      // junit.framework.TestCase
      String siName = t.getSuperclassName();
      if (siName != null && siName.equals("TestCase")) { //$NON-NLS-1$
        return false;
      }
    }
    
    return true;
  }
  
  private IType[] findClasses(ClassSearcher searcher) {
    try {
      fRunnableContext.run(true, true, searcher);
    } catch (InvocationTargetException e) {
      RandoopPlugin.log(e);
    } catch (InterruptedException e) {
      RandoopPlugin.log(e);
    }
    
    if (searcher.wasCancelled()) {
      return null;
    }
    return searcher.getTypes();
  }
  
  private abstract class ClassSearcher implements IRunnableWithProgress {
    private IJavaProject fJavaProject;
    private List<IType> fAvailableTypes;
    private boolean fCancelled;

    public ClassSearcher(IJavaProject project) {
      fJavaProject = project;
      fAvailableTypes = new ArrayList<IType>();
      fCancelled = false;
    }

    protected void addType(IType type) {
      fAvailableTypes.add(type);
    }
    
    protected void addTypes(Collection<? extends IType> types) {
      fAvailableTypes.addAll(types);
    }

    public IType[] getTypes() {
      return fAvailableTypes.toArray(new IType[fAvailableTypes.size()]);
    }

    protected IJavaProject getJavaProject() {
      return fJavaProject;
    }
    
    public boolean wasCancelled() {
      return fCancelled;
    }
    
    protected void cancel() {
      fCancelled = true;
    }
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

  @Override
  public void handleEvent(IOptionChangeEvent event) {
    if (IRandoopLaunchConfigurationConstants.ATTR_PROJECT.equals(event.getAttribute())) {
      if (IRandoopLaunchConfigurationConstants.DEFAULT_PROJECT.equals(event.getValue())) {
        fProject = null;
        fClassAddFromProject.setEnabled(false);
        fClassAddFromClasspaths.setEnabled(false);
        fClassAddFromJRESystemLibrary.setEnabled(false);
      } else {
        IJavaElement element = JavaCore.create(event.getValue());
        Assert.isTrue(element instanceof IJavaProject);

        fProject = (IJavaProject) element;
        fClassAddFromProject.setEnabled(true);
        fClassAddFromClasspaths.setEnabled(false); // XXX implement this
        fClassAddFromJRESystemLibrary.setEnabled(true);
      }
    }
  }
}
