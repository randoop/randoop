package randoop.plugin.tests.core.runtime;

import java.io.Closeable;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.PlatformUI;
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.plugin.internal.core.runtime.MessageReceiver;
import randoop.plugin.tests.ui.launching.ProjectCreator;
import randoop.runtime.ClosingStream;
import randoop.runtime.CreatedJUnitFile;
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
      } else if (m instanceof RandoopFinished) {
    	  fReceivedLast = true;
      } else if (m instanceof PercentDone) {
    	  assertNotNull("RandoopStarted message must be received before PercentDone", fStartMessage);
    	  assertFalse("PercentDone message must not be received after RandoopFinished", fReceivedLast);
    	  
    	  double pDone = ((PercentDone)m).getPercentDone();
    	  assertTrue("Percent done cannot decrease", fLastPercentDone < pDone);
    	  fLastPercentDone = pDone;
      } else if (m instanceof CreatedJUnitFile) {
        CreatedJUnitFile fileCreatedMsg = (CreatedJUnitFile) m;
        
        System.out.println(fileCreatedMsg.isDriver() + "  " + fileCreatedMsg.getFile());
      } else if (m instanceof ClosingStream) {
        fail("ClosingStream messages should not be passed to IMessageListeners");
      }
    }

    @Override
    public void handleTermination() {
      fail("Terminated unexpectedly");
    }
    
    public boolean receivedLast() {
      return fReceivedLast;
    }
  }
  
  @BeforeClass
  protected void setUp() throws Exception {
    fJavaProject = ProjectCreator.createStandardDemoProject();
  }
  
//  @Test
//  public void generateTestsForTypesInProject() throws CoreException, IOException {
//    ILaunchConfigurationWorkingCopy config = ProjectCreator.createNewAllTypeConfig(fJavaProject, 10);
//    testStartRandoop(config);
//  }
  
  @Test
  public void testGenerateTestsForTypesArrayList() throws CoreException, IOException {
    ILaunchConfigurationWorkingCopy config = ProjectCreator.createTestConfigWithSingleClass(fJavaProject, 10);
    testStartRandoop(config);
  }
  
  public void testStartRandoop(final ILaunchConfigurationWorkingCopy config) throws CoreException, IOException {
    TestMessageListener tml = new TestMessageListener();
    MessageReceiver mr = new MessageReceiver(tml);
    System.out.println("Using port " + mr.getPort());
    
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
    
    assertTrue("Never received RandoopFinished message", tml.receivedLast());
  }
}
