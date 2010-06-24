package randoop.plugin.tests.core.runtime;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.plugin.internal.core.runtime.MessageReceiver;
import randoop.plugin.tests.ui.launching.ProjectCreator;
import randoop.runtime.IMessage;
import randoop.runtime.PercentDone;
import randoop.runtime.RandoopFinished;
import randoop.runtime.RandoopStarted;

/**
 * Do not run this test in the UI thread
 */
@SuppressWarnings("nls")
public class MessageReceiverTest extends TestCase {
  IJavaProject fJavaProject;
  
  private class TestMessageListener implements IMessageListener {
    IMessage fStartMessage = null;
    boolean fReceivedLast = false;
    double fLastPercentDone = 0.0;
    
    @Override
    public void handleMessage(IMessage m) {
      System.out.println(m);
      if (m instanceof RandoopStarted) {
    	  fStartMessage = m;
    	  return;    	  
      } else if (m instanceof RandoopFinished) {
    	  fReceivedLast = true;
    	  return;    	  
      } else if (m instanceof PercentDone) {
    	  assertNotNull("START message must be received before WORK", fStartMessage);
    	  assertFalse("WORK message must not be received after DONE", fReceivedLast);
    	  
    	  double pDone = ((PercentDone)m).getPercentDone();
    	  assertTrue("Percent done cannot decrease", fLastPercentDone < pDone);
    	  fLastPercentDone = pDone;
    	  
      }
    }

    @Override
    public void handleTermination() {
      fail("Steam terminated unexpectedly");
    }
    
    public boolean hasReceivedLast() {
      return fReceivedLast;
    }
  }
  
  @Override
  protected void setUp() throws Exception {
    fJavaProject = ProjectCreator.createStandardDemoProject();
  }
  
  public void testStartRandoop() throws CoreException, IOException {
    final ILaunchConfigurationWorkingCopy config = ProjectCreator.createNewAllTypeConfig(fJavaProject, 10);
    
    TestMessageListener tml = new TestMessageListener();
    MessageReceiver mr = new MessageReceiver(tml);
    
    RandoopArgumentCollector.setPort(config, mr.getPort());
    
    Thread messageReceivingThread = new Thread(mr);
    messageReceivingThread.start();

    // Launch the configuration from the UI thread
    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        try {
          config.launch(ILaunchManager.RUN_MODE, null, true);
        } catch (CoreException e) {
          e.printStackTrace();
          fail();
        }
      }
    });
    
    try {
      messageReceivingThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    assertTrue("DONE message never received", tml.hasReceivedLast());
  }
}
