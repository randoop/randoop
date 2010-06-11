package randoop.plugin.launching;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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

import randoop.plugin.RandoopActivator;
import randoop.plugin.internal.ui.DebugTypeSelectionDialog;
import randoop.plugin.internal.ui.SWTFactory;
import randoop.plugin.internal.ui.TypeSelector;

public class RandoopLaunchConfigStatementsTab extends AbstractLaunchConfigurationTab {
  private TypeSelector fTypeSelector;
  private Tree fTypeTree;
  private Button fClassAdd;
  private Button fClassRemove;

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
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

    fTypeTree = new Tree(leftcomp, SWT.CHECK);
    fTypeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fTypeSelector = new TypeSelector(fTypeTree);
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
        // XXX remove the selected element
      }
    });
  }

  /**
   * Show a dialog that lists all types in the workspace
   */
  protected void handleSearchButtonSelected() {
    DebugTypeSelectionDialog mmsd = new DebugTypeSelectionDialog(getShell(),
        getAllAvailableTypes(getLaunchConfigurationDialog()),
        "Select types to test");
    if (mmsd.open() == Window.CANCEL) {
      return;
    }

    // Add all of the types to the selector
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

  private static IType[] getAllAvailableTypes(ILaunchConfigurationDialog dialog) {
    final List<IType> types = new ArrayList<IType>();

    IJavaModel model = JavaCore
        .create(ResourcesPlugin.getWorkspace().getRoot());
    if (model != null) {
      try {
        final IJavaProject[] elements = model.getJavaProjects();
        IRunnableWithProgress runnable = new IRunnableWithProgress() {
          public void run(IProgressMonitor pm) throws InvocationTargetException {
            int work = 0;
            pm.beginTask("Searching for Java types...",
                IProgressMonitor.UNKNOWN);
            try {
              for (IJavaProject e : elements) {
                for (IPackageFragment f : e.getPackageFragments()) {
                  for (ICompilationUnit c : f.getCompilationUnits()) {
                    for (IType t : c.getAllTypes()) {
                      types.add(t);
                      pm.worked(work++);
                    }
                  }
                }
              }
            } catch (JavaModelException e) {
              RandoopActivator.log(e);
            }

            pm.done();
          }
        };
        try {
          if (dialog != null)
            dialog.run(true, true, runnable);
        } catch (InvocationTargetException e) {
          RandoopActivator.log(e);
        } catch (InterruptedException e) {
          RandoopActivator.log(e);
        }
      } catch (JavaModelException e) {
        RandoopActivator.log(e);
      }
    }

    IType[] typesArr = new IType[types.size()];
    for (int i = 0; i < types.size(); i++) {
      typesArr[i] = types.get(i);
    }

    return typesArr;
  }

  @Override
  public boolean canSave() {
    setErrorMessage(null);

    if (fTypeSelector == null) {
      return false;
    }

    return true;
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration config) {
    List<?> selected = RandoopLaunchConfigArgumentCollector.getCheckedJavaElements(config);
    for (Object id : selected) {
      if (JavaCore.create((String) id).exists()) {
        return true;
      }
    }

    setErrorMessage("At least one existing type or method must be selected.");
    return false;
  };

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fTypeSelector != null) {
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_ALL_JAVA_TYPES,
          fTypeSelector.getAllTypeHandlerIds());
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_CHECKED_JAVA_ELEMENTS,
          fTypeSelector.getCheckedHandlerIds());
    }
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  @SuppressWarnings("unchecked")
  public void initializeFrom(ILaunchConfiguration config) {
    if (fTypeTree != null) {
        List<String> allTypes = RandoopLaunchConfigArgumentCollector.getAllJavaTypes(config);
        List<String> checkedElements = RandoopLaunchConfigArgumentCollector.getCheckedJavaElements(config);
        
        fTypeSelector = new TypeSelector(fTypeTree, allTypes, checkedElements);
    }
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_ALL_JAVA_TYPES,
        IRandoopLaunchConfigConstants.EMPTY_LIST);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_CHECKED_JAVA_ELEMENTS,
        IRandoopLaunchConfigConstants.EMPTY_LIST);
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName() {
    return "Statements";
  }

  /**
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
   * 
   * @since 3.3
   */
  @Override
  public String getId() {
    return "randoop.plugin.launching.testInputConfig.randoop"; //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
   */
  @Override
  public Image getImage() {
    return null;
  }
}
