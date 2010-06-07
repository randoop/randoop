package randoop.plugin.internal.ui.launchConfigurations;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.ui.DebugTypeSelectionDialog;
import randoop.plugin.internal.ui.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.ui.SWTFactory;
import randoop.plugin.internal.ui.TypeSelector;

public class StatementsTab extends AbstractLaunchConfigurationTab {
  private TypeSelector fTypeSelector;
  private Tree fTypeTree;
  private Button fClassAdd;
  private Button fClassRemove;

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 2, 1,
        GridData.FILL_HORIZONTAL);
    setControl(comp);

    final Composite leftcomp = SWTFactory.createComposite(comp, 1, 1,
        GridData.FILL_BOTH);
    GridLayout ld = (GridLayout) leftcomp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;
    GridData gd = (GridData) leftcomp.getLayoutData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;

    final Composite rightcomp = SWTFactory.createComposite(comp, 1, 1,
        GridData.FILL);
    gd = (GridData) rightcomp.getLayoutData();
    gd.horizontalAlignment = SWT.LEFT;
    gd.verticalAlignment = SWT.TOP;

    fTypeTree = new Tree(leftcomp, SWT.MULTI | SWT.CHECK);
    fTypeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    // Add type selector functionalities to this tree. This will also add a
    // Listener to fTypeTree that adds default tree behavior. That is, checking/
    // unchecking a parent item also checks/unchecks all descendant items
    fTypeSelector = new TypeSelector(fTypeTree);
    
    // Add the
    fTypeTree.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        if (event.detail == SWT.CHECK) {
          setErrorMessage(null);
          updateLaunchConfigurationDialog();
        }
      }
    });
    
    fClassAdd = SWTFactory.createPushButton(rightcomp, "Add", null);
    fClassAdd.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSearchButtonSelected();

        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });

    fClassRemove = SWTFactory.createPushButton(rightcomp, "Remove", null);
    fClassRemove.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fTypeSelector.removeSelectedTypes();
        
        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });
  }

  /**
   * Opens a <code>FilteredItemsSelectionDialog</code> that lists all types in
   * the workspace. The user can select multiple items from the list. Upon
   * pressing OK, the selected types are added to this tab's SWT
   * <code>Tree</code>.
   */
  protected void handleSearchButtonSelected() {
    DebugTypeSelectionDialog mmsd = new DebugTypeSelectionDialog(getShell(),
        getAllAvailableTypes(getLaunchConfigurationDialog()),
        "Select types to test");
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
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
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

    IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
    if (model != null) {
      try {
        final IJavaProject[] javaProjects = model.getJavaProjects();
        
        // Create a new runnable object that will be used to search for the Java
        // types in the active workspace.
        IRunnableWithProgress typeSearcher = new IRunnableWithProgress() {
          @Override
          public void run(IProgressMonitor pm) throws InvocationTargetException {
            int work = 0;
            pm.beginTask("Searching for Java types...", IProgressMonitor.UNKNOWN);
            
            // Search each IJavaProject for ITypes and add them to a list.
            // Each added IType represents one unit of work.
            try {
              for (IJavaProject jp : javaProjects) {
                for (IPackageFragment pf : jp.getPackageFragments()) {
                  for (ICompilationUnit cu : pf.getCompilationUnits()) {
                    for (IType t : cu.getAllTypes()) {
                      availableTypes.add(t);
                      pm.worked(work++);
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

  /**
   * Always returns <code>true</code>. This dialog is never in a state where it cannot save.
   * 
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
   */
  @Override
  public boolean canSave() {
    setErrorMessage(null);
    return true;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration config) {
    List<?> selected = RandoopArgumentCollector.getCheckedJavaElements(config);
    for (Object id : selected) {
      if (JavaCore.create((String) id).exists()) {
        return true;
      }
    }
    
    setErrorMessage("At least one existing type or method must be selected.");
    return false;
  };

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fTypeSelector == null) {
      config.setAttribute(IRandoopLaunchConfigurationConstants.ATTR_ALL_JAVA_TYPES,
          IConstants.EMPTY_STRING);
      config.setAttribute(IRandoopLaunchConfigurationConstants.ATTR_CHECKED_JAVA_ELEMENTS,
          IConstants.EMPTY_STRING);
    } else {
      config.setAttribute(IRandoopLaunchConfigurationConstants.ATTR_ALL_JAVA_TYPES,
          fTypeSelector.getAllTypeHandlerIds());
      config.setAttribute(IRandoopLaunchConfigurationConstants.ATTR_CHECKED_JAVA_ELEMENTS,
          fTypeSelector.getCheckedHandlerIds());
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration config) {
    if (fTypeTree != null) {
        List<String> allTypes = RandoopArgumentCollector.getAllJavaTypes(config);
        List<String> checkedElements = RandoopArgumentCollector.getCheckedJavaElements(config);
        
        fTypeSelector = new TypeSelector(fTypeTree, allTypes, checkedElements);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(IRandoopLaunchConfigurationConstants.ATTR_ALL_JAVA_TYPES,
        IConstants.EMPTY_STRING_LIST);
    config.setAttribute(IRandoopLaunchConfigurationConstants.ATTR_CHECKED_JAVA_ELEMENTS,
        IConstants.EMPTY_STRING_LIST);
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  @Override
  public String getName() {
    return "Statements";
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
   */
  @Override
  public String getId() {
    return "randoop.plugin.launching.testInputConfig.randoop"; //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
   */
  @Override
  public Image getImage() {
    return null;
  }
}
