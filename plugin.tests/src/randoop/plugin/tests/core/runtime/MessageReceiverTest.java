package randoop.plugin.tests.core.runtime;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.plugin.internal.core.runtime.MessageReceiver;
import randoop.plugin.tests.LaunchConfigurationFactory;
import randoop.plugin.tests.WorkspaceManager;
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
public class MessageReceiverTest {
  static IJavaProject javaProject;
  
  private static class TestMessageListener implements IMessageListener {
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
        Assert.assertNotNull("RandoopStarted message must be received before PercentDone", fStartMessage);
        Assert.assertFalse("PercentDone message must not be received after RandoopFinished", fReceivedLast);
    	  
    	  double pDone = ((PercentDone)m).getPercentDone();
    	  Assert.assertTrue("Percent done cannot decrease", fLastPercentDone < pDone);
    	  fLastPercentDone = pDone;
      } else if (m instanceof CreatedJUnitFile) {
        CreatedJUnitFile fileCreatedMsg = (CreatedJUnitFile) m;
        
        System.out.println(fileCreatedMsg.isDriver() + "  " + fileCreatedMsg.getFile());
      } else if (m instanceof ClosingStream) {
        Assert.fail("ClosingStream messages should not be passed to IMessageListeners");
      }
    }

    @Override
    public void handleTermination() {
      Assert.fail("Stream terminated unexpectedly");
    }
    
    public boolean receivedLast() {
      return fReceivedLast;
    }
  }
  
  @BeforeClass
  public static void beforeClass() throws IOException, CoreException {
    javaProject = WorkspaceManager.getJavaProject(WorkspaceManager.PATH_PLANNER);
  }
  
  @Test
  public void testStartRandoop() throws CoreException, IOException {
    ILaunchConfigurationWorkingCopy config = LaunchConfigurationFactory.createConfig(javaProject, "ReceiverTest", 20, true, false);

    RandoopArgumentCollector.setTimeLimit(config, "10");

    RandoopArgumentCollector.setProjectName(config, javaProject.getElementName());
    RandoopArgumentCollector.setOutputDirectoryName(config, "test");
    RandoopArgumentCollector.setJUnitPackageName(config, "demo.pathplanning.receiver");
    RandoopArgumentCollector.setJUnitClassName(config, "ReceiverTest");

    testReceiver(config);
  }
  
  public static void testReceiver(final ILaunchConfigurationWorkingCopy config) throws CoreException, IOException {
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
          Assert.fail();
        }
      }
    });
    
    try {
      messageReceivingThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    Assert.assertTrue("Never received RandoopFinished message", tml.receivedLast());
  }
}
