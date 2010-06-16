package randoop.plugin.tests.internal.core.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.plugin.internal.core.runtime.MessageReceiver;
import randoop.plugin.internal.ui.launchConfigurations.RandoopArgumentCollector;
import randoop.plugin.internal.ui.launchConfigurations.RandoopLaunchDelegate;
import randoop.runtime.Message;

public class MessageReceiverTest extends TestCase {
  private static IPath getFullPath(IPath localPath) {
    URL url = FileLocator.find(RandoopPlugin.getDefault().getBundle(), localPath, null);
    try {
      url = FileLocator.toFileURL(url);
    } catch (IOException e) {
      return null;
    }
    return new Path(url.getPath());
  }
  
  public void testStartRandoop() throws CoreException, IOException {
    RandoopLaunchDelegate launchDelegate = new RandoopLaunchDelegate();
    String mainTypeName = launchDelegate.verifyMainTypeName(null);

    // Classpath
    List<String> cpList = new ArrayList<String>();
    String demoFolder = getFullPath(new Path("/demo")).toOSString();
    cpList.add(RandoopPlugin.getRandoopJar().toOSString());
    cpList.add(RandoopPlugin.getPlumeJar().toOSString());
    cpList.add(demoFolder); //$NON-NLS-1$

    String[] classpath = cpList.toArray(new String[0]);
    
    // Create VM config
    VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);

    List<String> programArguments = new ArrayList<String>();
    programArguments.add("gentests"); //$NON-NLS-1$

    programArguments.add("--testclass=org.example.AStar"); //$NON-NLS-1$
    programArguments.add("--testclass=org.example.Direction"); //$NON-NLS-1$
    programArguments.add("--testclass=org.example.Grid"); //$NON-NLS-1$
    programArguments.add("--testclass=org.example.Location"); //$NON-NLS-1$
    programArguments.add("--testclass=org.example.PathPlanner"); //$NON-NLS-1$
    programArguments.add("--testclass=org.example.PathPlaningContext"); //$NON-NLS-1$

    programArguments.add("--timelimit=10");//$NON-NLS-1$
    programArguments.add("--junit-output-dir=" + demoFolder);//$NON-NLS-1$
    programArguments.add("--junit-package-name=org.example.tests");//$NON-NLS-1$
    programArguments.add("--junit-classname=AllTest");//$NON-NLS-1$

    runConfig.setProgramArguments((String[]) programArguments.toArray(new String[programArguments.size()]));
    
    IVMInstall vm = JavaRuntime.computeVMInstall(null);
    IVMRunner runner = vm.getVMRunner("run");
    assertNotNull(runner);
    
    new Thread(new MessageReceiver(new IMessageListener() {
      
      @Override
      public void handleMessage(Message m) {
        System.out.println(m);
      }
    })).start();
    
    runner.run(runConfig, null, null);
  }
}
