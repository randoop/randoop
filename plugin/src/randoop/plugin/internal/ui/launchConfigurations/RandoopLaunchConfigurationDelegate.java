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
import java.io.IOException;
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
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.RandoopResources;
import randoop.plugin.internal.core.RandoopTestSetResources;
import randoop.plugin.internal.core.runtime.MessageReceiver;
import randoop.plugin.internal.ui.views.TestGeneratorViewPart;

public class RandoopLaunchConfigurationDelegate extends
    AbstractJavaLaunchConfigurationDelegate {
  private int fPort;
  MessageReceiver fMessageReceiver;

  public RandoopLaunchConfigurationDelegate() {
    super();
    fPort = -1;
    fMessageReceiver = null;
  }

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

    // check for cancellation
    if (monitor.isCanceled())
      return;

    RandoopArgumentCollector args = new RandoopArgumentCollector(configuration);
    RandoopTestSetResources testSetResources = new RandoopTestSetResources(
        args, monitor);

    IStatus status = testSetResources.getStatus();
    if (!status.isOK()) {
      informAndAbort(status);
    }

    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        
        if (page != null) {
          TestGeneratorViewPart viewPart = (TestGeneratorViewPart) page
              .findView(TestGeneratorViewPart.ID);
          try {
            fMessageReceiver = new MessageReceiver(viewPart);
            fPort = fMessageReceiver.getPort();
          } catch (IOException e) {
            fMessageReceiver = null;
            System.err.println("Could not find free communication port");
          }
        }
      }
    });

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
    collectProgramArguments(testSetResources, programArguments);

    // VM-specific attributes
    Map vmAttributesMap = getVMSpecificAttributesMap(configuration);

    // Classpath
    List<String> cpList = new ArrayList<String>(Arrays
        .asList(getClasspath(configuration)));

    // XXX change to getFile("/lib")
    IPath libFolder = RandoopResources.getFullPath("/"); //$NON-NLS-1$
    if (libFolder == null) {
      informAndAbort("Library folder not found", null,
          IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
    }

    // XXX change to randoop.jar and plume.jar
    cpList.add(libFolder.toOSString() + "/../bin/"); //$NON-NLS-1$
    cpList.add(libFolder.toOSString() + "/../lib/plume.jar"); //$NON-NLS-1$
    for (IPath path : testSetResources.getClasspath()) {
      cpList.add(path.makeRelative().toOSString());
    }
    String[] classpath = cpList.toArray(new String[0]);
    
    for(String str : classpath) {
      System.out.println(str);
    }

    // Create VM config
    VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName,
        classpath);

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

    for (String str : programArguments) {
      System.out.println(str);
    }
    
    // PlatformUI.getWorkbench().getDisplay().asyncExec()
    new Thread(fMessageReceiver).start();
    runner.run(runConfig, launch, monitor);

    // check for cancellation
    if (monitor.isCanceled()) {
      return;
    }
    
    monitor.done();
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

  protected void collectProgramArguments(RandoopTestSetResources testSetResources,
      List<String> programArguments) {
    RandoopArgumentCollector args = testSetResources.getArguments();
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
    // if (testSetResources.getMethodFilePath() != null)
    // programArguments
    //          .add("--methodlist=" + testSetResources.getMethodFilePath());//$NON-NLS-1$
    programArguments.add("--comm-port=" + fPort); //$NON-NLS-1$
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
