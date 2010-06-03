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
  private TypeSelector typeSelector;
  private Tree typeTree;
  private Button classAddButton;
  private Button classRemoveButton;

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

    typeTree = new Tree(leftcomp, SWT.CHECK);
    typeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    typeSelector = new TypeSelector(typeTree);
    typeTree.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        if (event.detail == SWT.CHECK) {
          setErrorMessage(null);
          updateLaunchConfigurationDialog();
        }
      }
    });

    classAddButton = SWTFactory.createPushButton(rightcomp, "Add", null);
    classAddButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleSearchButtonSelected();

        setErrorMessage(null);
        updateLaunchConfigurationDialog();
      }
    });

    classRemoveButton = SWTFactory.createPushButton(rightcomp, "Remove", null);
    classRemoveButton.addSelectionListener(new SelectionAdapter() {
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
            typeSelector.addType(type);
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

    if (typeTree == null) {
      return false;
    }

    return true;
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    RandoopLaunchConfigStatementsTab tab = new RandoopLaunchConfigStatementsTab();
    tab.initializeFrom(launchConfig);
    return tab.canSave();
  };

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (typeSelector != null) {
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_CHECKED_JAVA_ELEMENTS,
          typeSelector.getCheckedHandlerIds());
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_UNCHECKED_JAVA_TYPES,
          typeSelector.getUncheckedTypeHandlerIds());
    }
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_CHECKED_JAVA_ELEMENTS,
        IRandoopLaunchConfigConstants.DEFAULT_CHECKED_JAVA_ELEMENTS);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_UNCHECKED_JAVA_TYPES,
        IRandoopLaunchConfigConstants.DEFAULT_UNCHECKED_JAVA_TYPES);
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  @SuppressWarnings("unchecked")
  public void initializeFrom(ILaunchConfiguration config) {
    if (typeTree != null)
      try {
        List<String> checkedElements = config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_CHECKED_JAVA_ELEMENTS,
            IRandoopLaunchConfigConstants.DEFAULT_CHECKED_JAVA_ELEMENTS);
        List<String> uncheckedElements = config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_UNCHECKED_JAVA_TYPES,
            IRandoopLaunchConfigConstants.DEFAULT_UNCHECKED_JAVA_TYPES);
        typeSelector = new TypeSelector(typeTree, checkedElements,
            uncheckedElements);
      } catch (CoreException ce) {
        typeSelector = new TypeSelector(typeTree,
            IRandoopLaunchConfigConstants.DEFAULT_CHECKED_JAVA_ELEMENTS,
            IRandoopLaunchConfigConstants.DEFAULT_UNCHECKED_JAVA_TYPES);
      }
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
