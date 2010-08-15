package randoop.plugin.internal.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.MutableObject;
import randoop.plugin.internal.core.RandoopStatus;
import randoop.plugin.internal.core.runtime.ITestGeneratorSessionListener;
import randoop.plugin.internal.core.runtime.TestGeneratorSession;
import randoop.plugin.internal.ui.RandoopPluginImages;
import randoop.runtime.ErrorRevealed;

public class TestGeneratorViewPart extends ViewPart {
  /**
   * The ID of the view as specified by the extension.
   */
  private static final String ID = "randoop.plugin.ui.views.TestGeneratorViewPart"; //$NON-NLS-1$

  private TreeViewer fTreeViewer;
  private CounterPanel fCounterPanel;
  private RandoopProgressBar fProgressBar;

  ICompilationUnit fJUnitDriver;
  
  private Action fDebugWithJUnitAction;
  
  private Action fRunWithJUnitAction;

  private Action fTerminateAction;
  
  private Action fRelaunchAction;
  
  private TestGeneratorSession fSession;

  private SessionListener fSessionListener;

  private boolean isDisposed = true;
  
  private Composite fParent;
  
  public static TestGeneratorViewPart openInstance() {
    final MutableObject viewPart = new MutableObject(null);
    
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

      public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        
        try {
          // Open the view
          viewPart.setValue(page.showView(TestGeneratorViewPart.ID));
        } catch (PartInitException e) {
          IStatus s = RandoopStatus.PART_INIT_EXCEPTION.getStatus(e);
          RandoopPlugin.log(s);
        }
        
      }
    });
    
    if (viewPart.getValue() != null) {
      Assert.isTrue(viewPart.getValue() instanceof TestGeneratorViewPart);
      
      return (TestGeneratorViewPart) viewPart.getValue();
    }
    return null;
  }
  
  public TestGeneratorViewPart() {
    fSessionListener = new SessionListener();
  }
  
  public TestGeneratorSession getActiveSession() {
    return fSession;
  }

  @Override
  public void createPartControl(Composite parent) {
    // Store the parent so we can see if it is disposed later
    fParent = parent;
    isDisposed = false;

    // Set the parent layout
    GridLayout layout = new GridLayout();
    layout.marginWidth = 3;
    layout.marginHeight = 3;
    layout.horizontalSpacing = 3;
    layout.numColumns = 1;
    fParent.setLayout(layout);
    
    // Create a register global actions with the action bar
    createActionBar();

    // Create controls within the parent
    fCounterPanel = new CounterPanel(fParent);
    fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    
    fProgressBar = new RandoopProgressBar(fParent);
    fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    
    Label errTitle = new Label(fParent, SWT.NONE);
    errTitle.setText("Failures:");

    fTreeViewer = new TreeViewer(fParent);
    
    // TODO: Change how sessions are manages and accessed
    setActiveTestRunSession(TestGeneratorSession.getActiveSession());
    
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.FILL;
    fTreeViewer.getControl().setLayoutData(gd);
    
    FailureItemDoubleClickListener doubleClickListener = new FailureItemDoubleClickListener();
    fTreeViewer.addDoubleClickListener(doubleClickListener);
    doubleClickListener.viewPart = this;
  }

  private class DebugWithJUNitAction extends Action {
    public DebugWithJUNitAction() {
      super("Debug Tests with JUnit");
      
      setImageDescriptor(RandoopPluginImages.DESC_ELCL_DEBUG_JUNIT);
      setEnabled(false);
    }
    
    @Override
    public void run() {
      if (fJUnitDriver != null) {
        List<IJavaElement> list = new ArrayList<IJavaElement>();
        list.add(fJUnitDriver);
        IStructuredSelection selection = new StructuredSelection(list);
        
        new JUnitLaunchShortcut().launch(selection, "debug");
      }
    }
  }
  
  private class RunWithJUnitAction extends Action {
    public RunWithJUnitAction() {
      super("Run Tests with JUnit");
      
      setImageDescriptor(RandoopPluginImages.DESC_ELCL_RUN_JUNIT);
      setEnabled(false);
    }
    
    @Override
    public void run() {
      if (fJUnitDriver != null) {
        List<IJavaElement> list = new ArrayList<IJavaElement>();
        list.add(fJUnitDriver);
        IStructuredSelection selection = new StructuredSelection(list);
        
        new JUnitLaunchShortcut().launch(selection, "run");
      }
    }
  }
  
  private class TerminateAction extends Action {
    public TerminateAction() {
      super("Stop Test Generation");
      
      setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_STOP));
      setEnabled(false);
    }
    
    @Override
    public void run() {
      terminateLaunch();
    }
  }
  
  private class RelaunchAction extends Action {
    public RelaunchAction () {
      super("Regenerate tests");
      
      setImageDescriptor(RandoopPluginImages.DESC_ELCL_RUN_RANDOOP);
    }
    @Override
    public void run() {
      // Terminate the old launch
      if (terminateLaunch()) {
        fSession = new TestGeneratorSession(fSession.getLaunch(), fSession.getArguments());
        ILaunch launch = fSession.getLaunch();
        
        ILaunchConfiguration config = launch.getLaunchConfiguration();
        Assert.isNotNull(config); // TODO right?
        String mode = launch.getLaunchMode();
        Assert.isNotNull(mode); // TODO right?
        
        TestGeneratorSession.setActiveSession(fSession);
        DebugUITools.launch(config, mode);
      }
    }
  }

  private void createActionBar() {
    fDebugWithJUnitAction = new DebugWithJUNitAction();
    fRunWithJUnitAction = new RunWithJUnitAction();
    fTerminateAction = new TerminateAction();
    fRelaunchAction = new RelaunchAction();

    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(fDebugWithJUnitAction);
    mgr.add(fRunWithJUnitAction);
    mgr.add(fTerminateAction);
    mgr.add(fRelaunchAction);
  }
  
  public void startNewLaunch() {
    setDriver(null);
    fDebugWithJUnitAction.setEnabled(false);
    fRunWithJUnitAction.setEnabled(false);
    fTerminateAction.setEnabled(true);
    fRelaunchAction.setEnabled(true);
    
    getProgressBar().reset();
    getCounterPanel().reset();
  }
  
  public boolean setActiveTestRunSession(TestGeneratorSession session) {
    deregisterTestSessionListener(true);
    
    fSession = session;
    if (fSession == null) {
      fDebugWithJUnitAction.setEnabled(false);
      fRunWithJUnitAction.setEnabled(false);
      fTerminateAction.setEnabled(false);
      fRelaunchAction.setEnabled(false);

      return true;
    } else {
      fSessionListener = new SessionListener();
      fSession.addListener(fSessionListener);

      fRelaunchAction.setEnabled(true);

      RandoopContentProvider prov = new RandoopContentProvider(fSession.getRandoopErrors());
      fTreeViewer.setContentProvider(prov);
      prov.viewer = fTreeViewer;
      fTreeViewer.setLabelProvider(new RandoopLabelProvider());
      fTreeViewer.setInput(fSession.getRandoopErrors());
      fTreeViewer.refresh();
      fTreeViewer.expandAll();

      getProgressBar().initializeFrom(fSession);
      getCounterPanel().initializeFrom(fSession);

      if (fSession.isRunning()) {
        fDebugWithJUnitAction.setEnabled(false);
        fRunWithJUnitAction.setEnabled(false);
        fTerminateAction.setEnabled(true);
      } else if (fSession.isTerminated()) {
        fDebugWithJUnitAction.setEnabled(false);
        fRunWithJUnitAction.setEnabled(false);
        fTerminateAction.setEnabled(false);
      }

      setDriver(fSession.getJUnitDriver());
    }
    return true;
  }
  
  private void deregisterTestSessionListener(boolean force) {
    if (fSession != null && fSessionListener != null && (force || !fSession.isRunning())) {
      fSession.removeListener(fSessionListener);
      fSessionListener = null;
    }
  }
  
  public boolean terminateLaunch() {
    if (!fSession.isStopped())
      getProgressBar().terminate();
    
    if (!fSession.getLaunch().isTerminated()) {
      try {
        fSession.getLaunch().terminate();
      } catch (DebugException e) {
        RandoopPlugin.log(e.getStatus());
        return false;
      }
    }
    
    fTerminateAction.setEnabled(false);
    return true;
  }
  
  public CounterPanel getCounterPanel() {
    return fCounterPanel;
  }

  public RandoopProgressBar getProgressBar() {
    return fProgressBar;
  }

  public void setDriver(ICompilationUnit driver) {
    fJUnitDriver = driver;
    if (fJUnitDriver != null) {
      fDebugWithJUnitAction.setEnabled(true);
      fRunWithJUnitAction.setEnabled(true);
    }
  }
  
  @Override
  public void setFocus() {
    // Choose a control to set focus to...
  }
  
  @Override
  public void dispose() {
    isDisposed = true;
    
    setActiveTestRunSession(null);
    super.dispose();
  }
  
  public boolean isDisposed() {
    return isDisposed || fParent.isDisposed() || getCounterPanel().isDisposed() || getProgressBar().isDisposed();
  }

  private class SessionListener implements ITestGeneratorSessionListener {

    public void sessionStarted() {
      getSite().getShell().getDisplay().syncExec(new Runnable() {

        public void run() {
          if (!isDisposed()) {
            startNewLaunch();
          }
        }
      });
    }

    public void sessionEnded() {
      getSite().getShell().getDisplay().syncExec(new Runnable() {

        public void run() {
          // TODO: RandoopFinished is sent before CreatedJUnitFile
          // so the listener cannot be removed
          // deregisterTestSessionListener(false);

          if (!isDisposed()) {
            fTerminateAction.setEnabled(false);
          }
        }
      });
    }

    public void sessionTerminated() {
      getSite().getShell().getDisplay().syncExec(new Runnable() {

        public void run() {
          deregisterTestSessionListener(true);
          
          if (!isDisposed()) {
            terminateLaunch();
          }
        }
      });
    }

    public void errorRevealed(final ErrorRevealed error) {
      getSite().getShell().getDisplay().syncExec(new Runnable() {

        public void run() {
          if (!isDisposed()) {
            getProgressBar().error();
            getCounterPanel().incrementErrorCount();

            fTreeViewer.refresh();
            fTreeViewer.expandAll();
          }
        }
      });
    }

    public void madeProgress(final double percentDone) {
        getSite().getShell().getDisplay().syncExec(new Runnable() {

          public void run() {
            if (!isDisposed()) {
              getProgressBar().setPercentDone(percentDone);
            }
          }
        });
    }

    public void madeSequences(final int count) {
      getSite().getShell().getDisplay().syncExec(new Runnable() {

        public void run() {
          if (!isDisposed()) {
            getCounterPanel().setNumSequences(count);
          }
        }
      });
    }

    public void madeJUnitDriver(final ICompilationUnit driverFile) {
      getSite().getShell().getDisplay().syncExec(new Runnable() {

        public void run() {
          if (!isDisposed()) {
            setDriver(driverFile);
          }
        }
      });
    }
  }
  
}