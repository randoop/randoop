package randoop.plugin.internal.ui.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
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
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.TestGroupResources;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.core.runtime.MessageReceiver;
import randoop.plugin.internal.ui.MessageUtil;
import randoop.plugin.internal.ui.views.MessageViewListener;
import randoop.plugin.internal.ui.views.TestGeneratorViewPart;

public class RandoopLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {
  MessageReceiver fMessageReceiver;
  int fPort;

  public RandoopLaunchDelegate() {
    super();
    fMessageReceiver = null;
  }

  @Override
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
    System.out.println("Begin launch"); //$NON-NLS-1$
    
    final ILaunch theLaunch = launch;
    
    if (monitor == null)
      monitor = new NullProgressMonitor();

    // check for cancellation
    if (monitor.isCanceled())
      return;

    RandoopArgumentCollector args = new RandoopArgumentCollector(configuration, getWorkspaceRoot());
    IStatus status = args.checkForConflicts();
    if (status.getSeverity() == IStatus.ERROR) {
      informAndAbort(status);
    } else if (status.getSeverity() == IStatus.WARNING) {
      if (!MessageUtil.openQuestion(status.getMessage() + "\n\n" + "Proceed with test generations?")) { //$NON-NLS-1$
        return;
      }
    }
    
    TestGroupResources testGroupResources = new TestGroupResources(args, monitor);

    fPort = RandoopArgumentCollector.getPort(configuration);
    boolean useDefault = (fPort == IConstants.INVALID_PORT);
    
    if (useDefault) {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        
        @Override
        public void run() {
          IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
          IWorkbenchPage page = window.getActivePage();

          if (page != null) {
            TestGeneratorViewPart viewPart;
            try {
              viewPart = (TestGeneratorViewPart) page.showView(TestGeneratorViewPart.ID);
              Assert.isTrue(viewPart != null); // TODO is this true?
              
              viewPart.relaunchAction.setEnabled(true);
              
              fMessageReceiver = new MessageReceiver(new MessageViewListener(viewPart));
              viewPart.setLaunch(theLaunch);
              fPort = fMessageReceiver.getPort();
            } catch (PartInitException e) {
              fMessageReceiver = null;
              RandoopPlugin.log(e, "Randoop view could not be initialized"); //$NON-NLS-1$
            } catch (IOException e) {
              fMessageReceiver = null;
              RandoopPlugin.log(e, "Could not find free communication port"); //$NON-NLS-1$
            }
          }
        }
      });
    } else {
      fMessageReceiver = null;
    }

    String mainTypeName = verifyMainTypeName(configuration);
    IVMRunner runner = getVMRunner(configuration, mode);

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    File workingDir = root.getLocation().toFile();
    String workingDirName = workingDir.getAbsolutePath();

    // Search for similarly named files in the output directory and warn the user
    // if any are found. Similarly named files match the pattern <ClassName>[0-9]*.java
    IResource[] threatenedResources = testGroupResources.getThreatendedResources();
    
    // Check if the output directory exists
    if (threatenedResources.length > 0) {
      if (!MessageUtil.openResourcesQuestion("Randoop found similarly named files (listed below) that may be overwritten by the generated tests.\n\nProceed with test generation?", threatenedResources)) {
        return;
      }
    }

    ArrayList<String> vmArguments = new ArrayList<String>();
    ArrayList<String> programArguments = new ArrayList<String>();
    collectExecutionArguments(configuration, vmArguments, programArguments);
    collectProgramArguments(testGroupResources, programArguments);

    // Classpath
    List<String> cpList = new ArrayList<String>(Arrays.asList(getClasspath(configuration)));

    cpList.add(RandoopPlugin.getRandoopJar().toOSString());
    cpList.add(RandoopPlugin.getPlumeJar().toOSString());
    
    for (IPath path : testGroupResources.getClasspathLocations()) {
      cpList.add(path.makeRelative().toOSString());
    }
    String[] classpath = cpList.toArray(new String[0]);
    
    for(String str : classpath) {
      System.out.println(str);
    }

    // Create VM config
    VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);

    runConfig.setVMArguments((String[]) vmArguments.toArray(new String[vmArguments.size()]));
    runConfig.setProgramArguments((String[]) programArguments.toArray(new String[programArguments.size()]));
    runConfig.setEnvironment(getEnvironment(configuration));
    runConfig.setWorkingDirectory(workingDirName);
    runConfig.setVMSpecificAttributesMap(getVMSpecificAttributesMap(configuration));

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
    
    if (useDefault)
      new Thread(fMessageReceiver).start();
    
    runner.run(runConfig, launch, monitor);
    
    // check for cancellation
    if (monitor.isCanceled()) {
      return;
    }

    System.out.println("Launching complete");
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
    // add program & VM arguments provided by getProgramArguments and
    // getVMArguments
    String pgmArgs = getProgramArguments(configuration);
    String vmArgs = getVMArguments(configuration);
    ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
    vmArguments.addAll(Arrays.asList(execArgs.getVMArgumentsArray()));
    vmArguments.add("-ea"); //$NON-NLS-1$
    programArguments.addAll(Arrays.asList(execArgs.getProgramArgumentsArray()));
  }

  protected void collectProgramArguments(TestGroupResources testSetResources,
      List<String> programArguments) {
    RandoopArgumentCollector args = testSetResources.getArguments();
    programArguments.add("gentests"); //$NON-NLS-1$

    for (IType type : args.getSelectedTypes()) {
      programArguments.add("--testclass=" + type.getFullyQualifiedName()); //$NON-NLS-1$
    }

    // TODO - Fix ERROR: while parsing command-line arguments: unknown option '--comm-port=
    // TODO - Fix java.lang.Error: Unexpected type long when --timeout is uncommented
    programArguments.add("--randomseed=" + args.getRandomSeed());//$NON-NLS-1$
    programArguments.add("--maxsize=" + args.getMaxTestSize());//$NON-NLS-1$
    programArguments.add("--usethreads=" + args.getUseThreads());//$NON-NLS-1$
    programArguments.add("--timeout=" + args.getThreadTimeout());//$NON-NLS-1$
    programArguments.add("--forbid-null=" + !args.getUseNull());//$NON-NLS-1$
    programArguments.add("--null-ratio=" + args.getNullRatio());//$NON-NLS-1$
    programArguments.add("--inputlimit=" + args.getJUnitTestInputs());//$NON-NLS-1$
    programArguments.add("--timelimit=" + args.getTimeLimit());//$NON-NLS-1$
    programArguments.add("--junit-output-dir=" + testSetResources.getOutputLocation().toOSString());//$NON-NLS-1$
    programArguments.add("--junit-package-name=" + args.getJUnitPackageName());//$NON-NLS-1$
    programArguments.add("--junit-classname=" + args.getJUnitClassName());//$NON-NLS-1$
    programArguments.add("--output-tests=" + args.getTestKinds());//$NON-NLS-1$
    programArguments.add("--outputlimit=" + args.getMaxTestsWritten());//$NON-NLS-1$
    programArguments.add("--testsperfile=" + args.getMaxTestsPerFile());//$NON-NLS-1$
    programArguments.add("--methodlist=" + testSetResources.getMethodFile().getAbsolutePath());//$NON-NLS-1$
    programArguments.add("--comm-port=" + fPort); //$NON-NLS-1$
    programArguments.add("--noprogressdisplay"); //$NON-NLS-1$
    programArguments.add("--log=randooplog.txt"); // XXX remove
  }

  private void informAndAbort(String message, Throwable exception, int code) throws CoreException {
    IStatus status = new Status(IStatus.INFO, RandoopPlugin.getPluginId(), code, message, exception);
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
    RandoopPlugin.getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        Shell shell = RandoopPlugin.getActiveWorkbenchShell();
        if (shell == null)
          shell = RandoopPlugin.getDisplay().getActiveShell();
        if (shell != null) {
          MessageDialog.openInformation(shell, "Problems Launching Randoop", status.getMessage());
          success[0] = true;
        }
      }
    });
    return success[0];
  }

  @Override
  public String verifyMainTypeName(ILaunchConfiguration configuration) throws CoreException {
    return "randoop.main.Main"; //$NON-NLS-1$
  }
  
  private static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
  
}
