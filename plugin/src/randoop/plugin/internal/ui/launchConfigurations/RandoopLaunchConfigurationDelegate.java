/*******************************************************************************
 *
 * Skeleton class for launching a Randoop session.
 *
 * Some of the code in this class is based on code from the Eclipse JUnit plugin.
 *******************************************************************************/

//
// NOTE: The current implementation is largely a copy-paste from the JUnit
//       plugin's launch configuration delegate class that launches
//       a remote JUnit session. We should adapt it for Randoop.
//
package randoop.plugin.internal.ui.launchConfigurations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.RandoopResources;
import randoop.plugin.internal.core.RandoopTestSetResources;
import randoop.plugin.internal.ui.IRandoopLaunchConfigurationConstants;

public class RandoopLaunchConfigurationDelegate extends
    AbstractJavaLaunchConfigurationDelegate {
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse
   * .debug.core.ILaunchConfiguration, java.lang.String,
   * org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void launch(ILaunchConfiguration configuration, String mode,
      ILaunch launch, IProgressMonitor monitor) throws CoreException {
    if (monitor == null)
      monitor = new NullProgressMonitor();

    // check for cancellation
    if (monitor.isCanceled())
      return;

    try {
      // check for cancellation
      if (monitor.isCanceled())
        return;

      RandoopArgumentCollector args = new RandoopArgumentCollector(configuration);
      RandoopTestSetResources testSetResources = new RandoopTestSetResources(args, monitor);

      IStatus status = testSetResources.getStatus();
      if (!status.isOK()) {
        informAndAbort(status);
      }
      
      int fPort = evaluatePort();
      launch.setAttribute(IRandoopLaunchConfigurationConstants.ATTR_PORT,
          String.valueOf(fPort));
      
      String mainTypeName = verifyMainTypeName(configuration);
      IVMRunner runner = getVMRunner(configuration, mode);
      
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      File workingDir = root.getLocation().toFile();
      String workingDirName = workingDir.getAbsolutePath();

      // Environment variables
      String[] envp = getEnvironment(configuration);

      ArrayList<String> vmArguments = new ArrayList<String>();
      ArrayList<String> programArguments = new ArrayList<String>();
      collectExecutionArguments(configuration, vmArguments, programArguments);
      collectProgramArguments(args, programArguments);

      // VM-specific attributes
      Map vmAttributesMap = getVMSpecificAttributesMap(configuration);

      // Classpath
      List<String> cpList = new ArrayList<String>(Arrays
          .asList(getClasspath(configuration)));

      // XXX change to getFile("/lib")
      File libFolder = RandoopResources.getPluginBase();
      if (libFolder == null) {
        informAndAbort("Library folder not found", null,
            IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
      }

      // XXX change to randoop.jar and plume.jar
      cpList.add(libFolder.getPath() + "../bin/"); //$NON-NLS-1$
      cpList.add(libFolder.getPath() + "../lib/plume.jar"); //$NON-NLS-1$
      for (IPath path : testSetResources.getClasspath()) {
        cpList.add(path.makeRelative().toOSString());
      }
      String[] classpath = cpList.toArray(new String[0]);

      // Create VM config
      VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
      
      runConfig.setVMArguments((String[]) vmArguments
          .toArray(new String[vmArguments.size()]));
      runConfig.setProgramArguments((String[]) programArguments
          .toArray(new String[programArguments.size()]));
      runConfig.setEnvironment(envp);
      runConfig.setWorkingDirectory(workingDirName);
      runConfig.setVMSpecificAttributesMap(vmAttributesMap);
      
      // Bootpath
      runConfig.setBootClassPath(getBootpath(configuration));

      // check for cancellation
      if (monitor.isCanceled()) {
        return;
      }

      // done the verification phase
      monitor.worked(1);

      // monitor.subTask("create_source_locator_description");
      // // set the default source locator if required
      // setDefaultSourceLocator(launch, configuration);
      monitor.worked(1);

      // Launch the configuration - 1 unit of work
      runner.run(runConfig, launch, monitor);

      // check for cancellation
      if (monitor.isCanceled()) {
        return;
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * Collects all VM and program arguments. Implementors can modify and add
   * arguments.
   * 
   * @param configuration
   *          the configuration to collect the arguments for
   * @param vmArguments
   *          a {@link List} of {@link String} representing the resulting VM
   *          arguments
   * @param programArguments
   *          a {@link List} of {@link String} representing the resulting
   *          program arguments
   * @exception CoreException
   *              if unable to collect the execution arguments
   */
  protected void collectExecutionArguments(ILaunchConfiguration configuration,
      List<String> vmArguments, List<String> programArguments)
      throws CoreException {
    RandoopArgumentCollector args = new RandoopArgumentCollector(configuration);

    // add program & VM arguments provided by getProgramArguments and
    // getVMArguments
    String pgmArgs = getProgramArguments(configuration);
    String vmArgs = getVMArguments(configuration);
    ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
    vmArguments.addAll(Arrays.asList(execArgs.getVMArgumentsArray()));
    programArguments.addAll(Arrays.asList(execArgs.getProgramArgumentsArray()));
  }

  protected void collectProgramArguments(RandoopArgumentCollector args,
      List<String> programArguments) {
    programArguments.add("gentests"); //$NON-NLS-1$

    for (IType type : args.getCheckedTypes()) {
      programArguments.add("--testclass=" + type.getFullyQualifiedName()); //$NON-NLS-1$
    }

    programArguments.add("--randomseed=" + args.getRandomSeed());//$NON-NLS-1$
    programArguments.add("--maxsize=" + args.getMaxTestSize());//$NON-NLS-1$
    programArguments.add("--usethreads=" + args.getUseThreads());//$NON-NLS-1$
    programArguments.add("--timeout=" + args.getThreadTimeout());//$NON-NLS-1$
    programArguments.add("--forbid-null=" + !args.getUseNull());//$NON-NLS-1$
    programArguments.add("--null-ratio=" + args.getNullRatio());//$NON-NLS-1$
    programArguments.add("--inputlimit=" + args.getJUnitTestInputs());//$NON-NLS-1$
    programArguments.add("--timelimit=" + args.getTimeLimit());//$NON-NLS-1$
    programArguments.add("--junit-output-dir=" + args.getOutputDirectory());//$NON-NLS-1$
    programArguments.add("--junit-package-name=" + args.getJUnitPackageName());//$NON-NLS-1$
    programArguments.add("--junit-classname=" + args.getJUnitClassName());//$NON-NLS-1$
    programArguments.add("--output-tests=" + args.getTestKinds());//$NON-NLS-1$
    programArguments.add("--outputlimit=" + args.getMaxTestsWritten());//$NON-NLS-1$
    programArguments.add("--testsperfile=" + args.getMaxTestsPerFile());//$NON-NLS-1$
  }

  /**
   * Finds and returns a free port for the system to use. A
   * <code>CoreException</code> is thrown if no free port could be found.
   * 
   * @return a free socket
   * @throws CoreException
   *           if no socket is found
   */
  private int evaluatePort() throws CoreException {
    int port = SocketUtil.findFreePort();
    if (port == -1) {
      informAndAbort("Error: no available ports found found", null,
          IJavaLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE);
    }
    return port;
  }

  private void informAndAbort(String message, Throwable exception, int code)
      throws CoreException {
    IStatus status = new Status(IStatus.INFO, RandoopPlugin.getPluginId(),
        code, message, exception);
    informAndAbort(status);
  }

  private void informAndAbort(IStatus status) throws CoreException {
    if (showStatusMessage(status)) {
      // Status message successfully shown
      // -> Abort with INFO exception
      // -> Worker.run() will not write to log
      throw new CoreException(status);
    } else {
      // Status message could not be shown
      // -> Abort with original exception
      // -> Will write WARNINGs and ERRORs to log
      abort(status.getMessage(), status.getException(), status.getCode());
    }
  }

  private boolean showStatusMessage(final IStatus status) {
    final boolean[] success = new boolean[] { false };
    getDisplay().syncExec(new Runnable() {
      public void run() {
        Shell shell = RandoopPlugin.getActiveWorkbenchShell();
        if (shell == null)
          shell = getDisplay().getActiveShell();
        if (shell != null) {
          MessageDialog.openInformation(shell, "Problems Launching Randoop",
              status.getMessage());
          success[0] = true;
        }
      }
    });
    return success[0];
  }

  /**
   * Returns the current display, or the default display if the currently
   * running thread is not a user-interface thread for any display.
   * 
   * @see org.eclipse.swt.widgets.Display#getCurrent()
   * @see org.eclipse.swt.widgets.Display#getDefault()
   */
  private Display getDisplay() {
    Display display;
    display = Display.getCurrent();
    if (display == null)
      display = Display.getDefault();
    return display;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#
   * verifyMainTypeName(org.eclipse.debug.core.ILaunchConfiguration)
   */
  @Override
  public String verifyMainTypeName(ILaunchConfiguration configuration)
      throws CoreException {
    return "randoop.main.Main"; //$NON-NLS-1$
  }
}
