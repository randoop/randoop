package randoop.plugin.internal.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
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
import randoop.plugin.internal.core.runtime.TestGeneratorSession;
import randoop.plugin.internal.core.runtime.ITestGeneratorSessionListener;
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
  
//  private static TestGeneratorViewPart viewPart = null;
  
//  public static TestGeneratorViewPart getDefault() {
//    if (viewPart == null || isDisposed) {
//      return viewPart = openInstance();
//    }
//    
//    return viewPart;
//  }
  
  public static TestGeneratorViewPart openInstance() {
    final MutableObject viewPart = new MutableObject(null);
    
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        
        try {
          // Open the view
          viewPart.setValue(page.showView(TestGeneratorViewPart.ID));
        } catch (PartInitException e) {
          RandoopPlugin.log(e, "Randoop view could not be initialized"); //$NON-NLS-1$
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
    isDisposed = false;

    GridLayout layout = new GridLayout();
    layout.marginWidth = 3;
    layout.marginHeight = 3;
    layout.horizontalSpacing = 3;
    layout.numColumns = 1;
    parent.setLayout(layout);

    fCounterPanel = new CounterPanel(parent);
    fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    
    fProgressBar = new RandoopProgressBar(parent);
    fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

    fTreeViewer = new TreeViewer(parent);

    createActionToolBar();
    
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
      super("Debug tests with JUnit");
      setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG));
    }
    
    @Override
    public void run() {
      System.out.println("Running " + fJUnitDriver);
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
      super("Run tests with JUnit");
      
      ImageDescriptor desc = RandoopPlugin.getImageDescriptor("icons/run_junit.png");
      setImageDescriptor(desc);
    }
    
    @Override
    public void run() {
      System.out.println("Running " + fJUnitDriver);
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
      super("Terminate");
      
      setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_STOP));
    }
    
    @Override
    public void run() {
      stopLaunch();
    }
  }
  
  private class RelaunchAction extends Action {
    public RelaunchAction () {
      super("Regenerate tests");
      setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
    }
    @Override
    public void run() {
      // Terminate the old launch
      if (stopLaunch()) {
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

  private void createActionToolBar() {
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
  }
  
  public void setActiveTestRunSession(TestGeneratorSession session) {
    fSession = session;
    
    if (fSession == null) {
      fDebugWithJUnitAction.setEnabled(false);
      fRunWithJUnitAction.setEnabled(false);
      fTerminateAction.setEnabled(false);
      fRelaunchAction.setEnabled(false);
    } else {
      fSession.addListener(fSessionListener);
      fRelaunchAction.setEnabled(true);

      RandoopContentProvider prov = new RandoopContentProvider(fSession.getRandoopErrors());
      fTreeViewer.setContentProvider(prov);
      prov.viewer = fTreeViewer;
      fTreeViewer.setLabelProvider(new RandoopLabelProvider());
      fTreeViewer.setInput(fSession.getRandoopErrors());
      fTreeViewer.refresh();
      fTreeViewer.expandAll();

      int errorCount = fSession.getErrorCount(); 
      if (errorCount > 0) {
        getProgressBar().error();
      }
      getCounterPanel().setErrorCount(errorCount);

      getProgressBar().setPercentDone(fSession.getPercentDone());
      getCounterPanel().setNumSequences(fSession.getSequenceCount());

      if (session.isRunning()) {
        fDebugWithJUnitAction.setEnabled(false);
        fRunWithJUnitAction.setEnabled(false);
        fTerminateAction.setEnabled(true);
      }

      setDriver(fSession.getJUnitDriver());
    }
  }
  
  public boolean stopLaunch() {
    if (!fSession.getLaunch().isTerminated()) {
      try {
        fSession.getLaunch().terminate();
      } catch (DebugException e) {
        RandoopPlugin.log(e);
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
    
    super.dispose();
  }
  
  public boolean isDisposed() {
    return isDisposed || getCounterPanel().isDisposed() || getProgressBar().isDisposed();
  }

  private class SessionListener implements ITestGeneratorSessionListener {
    @Override
    public void sessionStarted() {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          if (!isDisposed()) {
            startNewLaunch();
          }
        }
      });
    }

    @Override
    public void sessionEnded() {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          if (!isDisposed()) {
            fTerminateAction.setEnabled(false);
          }
        }
      });
    }

    @Override
    public void sessionStopped() {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          if (!isDisposed()) {
            stopLaunch();
          }
        }
      });
    }

    @Override
    public void errorRevealed(final ErrorRevealed error) {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
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

    @Override
    public void madeProgress(final double percentDone) {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
          @Override
          public void run() {
            if (!isDisposed()) {
              getProgressBar().setPercentDone(percentDone);
            }
          }
        });
    }

    @Override
    public void madeSequences(final int count) {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          if (!isDisposed()) {
            getCounterPanel().setNumSequences(count);
          }
        }
      });
    }

    @Override
    public void madeJUnitDriver(final ICompilationUnit driverFile) {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          if (!isDisposed()) {
            setDriver(driverFile);
          }
        }
      });
    }

  }
}