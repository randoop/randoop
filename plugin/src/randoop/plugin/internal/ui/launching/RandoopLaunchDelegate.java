package randoop.plugin.internal.ui.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.MutableBoolean;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.core.launching.RandoopLaunchResources;
import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.plugin.internal.core.runtime.MessageReceiver;
import randoop.plugin.internal.core.runtime.MessageSessionListener;
import randoop.plugin.internal.core.runtime.TestGeneratorSession;
import randoop.plugin.internal.ui.MessageUtil;
import randoop.plugin.internal.ui.ResourcesListQuestionDialogWithToggle;
import randoop.plugin.internal.ui.views.TestGeneratorViewPart;

public class RandoopLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {
  MessageReceiver fMessageReceiver;
  int fPort;

  public RandoopLaunchDelegate() {
    super();
    fMessageReceiver = null;
  }
  
  private IProject[] computeReferencedProjectOrder(IJavaProject javaProject) throws JavaModelException {
    List<IProject> buildOrder = new ArrayList<IProject>();
    buildOrder.add(javaProject.getProject());
    
    for (IClasspathEntry ce : javaProject.getRawClasspath()) {
      if (ce.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
        ce = JavaCore.getResolvedClasspathEntry(ce);
      }
      
      if (ce != null && ce.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
        buildOrder.add(getWorkspaceRoot().getProject(ce.getPath().toString()));
      }
    }
    
    return buildOrder.toArray(new IProject[buildOrder.size()]);
  }
  
  @Override
  protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
    
    RandoopArgumentCollector args = new RandoopArgumentCollector(configuration, getWorkspaceRoot());
    
    IJavaProject javaProject = args.getJavaProject();
    return computeReferencedProjectOrder(javaProject);
  }
    
  @Override
  protected IProject[] getProjectsForProblemSearch(ILaunchConfiguration configuration, String mode) throws CoreException {

    IWorkspaceRoot root = getWorkspaceRoot();
    RandoopArgumentCollector args = new RandoopArgumentCollector(configuration, root);
    
    IJavaProject javaProject = args.getJavaProject();
    return computeReferencedProjectOrder(javaProject);
  }
  
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
    System.out.println("Begin launch"); //$NON-NLS-1$
    
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
    
    RandoopLaunchResources launchResources = new RandoopLaunchResources(args, monitor);
    final TestGeneratorSession session = new TestGeneratorSession(launch, args);

    fPort = RandoopArgumentCollector.getPort(configuration);

    boolean useDefault = (fPort == IConstants.INVALID_PORT);
    
    fMessageReceiver = null;
    if (useDefault) {
      try {
        IMessageListener listener = new MessageSessionListener(session);
        fMessageReceiver = new MessageReceiver(listener);
        fPort = fMessageReceiver.getPort();
      } catch (IOException e) {
        fMessageReceiver = null;
        RandoopPlugin.log(e, "Could not find free communication port"); //$NON-NLS-1$
      }
    }

    String mainTypeName = verifyMainTypeName(configuration);
    IVMRunner runner = getVMRunner(configuration, mode);

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    File workingDir = root.getLocation().toFile();
    String workingDirName = workingDir.getAbsolutePath();

    // Search for similarly named files in the output directory and warn the user if any are found.
    final IResource[] resourcesInQuestion = launchResources.getThreatendedResources();

    // Check if the output directory exists
    if (resourcesInQuestion.length > 0) {
      final String message = "The following files were found in the output directory and may be overwritten by the generated tests.";
      final String yesNoQuestion = "Proceed with test generation?";
      final String toggleQuestion = "Delete these files before launch";
      
      final MutableBoolean okToProceed = new MutableBoolean(false);
      final MutableBoolean deleteFiles = new MutableBoolean(false);
      
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

        public void run() {
          MessageDialogWithToggle d = new ResourcesListQuestionDialogWithToggle(PlatformUI
              .getWorkbench().getDisplay().getActiveShell(), "Randoop", message, yesNoQuestion,
              toggleQuestion, resourcesInQuestion);
          
          okToProceed.setValue(d.open() == Dialog.OK);
          deleteFiles.setValue(d.getToggleState());
        }
      });
      
      if (!okToProceed.getValue()) {
        return;
      }
      
      if (deleteFiles.getValue()) {
        for (IResource r : resourcesInQuestion) {
          monitor.beginTask("Deleting files", 0);
          r.delete(true, new SubProgressMonitor(monitor, 0));
        }
      }
    }
    
    // Set the shared instance of the session to the session about to run
    TestGeneratorSession.setActiveSession(session);
    
    // Open the randoop view
    // TODO: Future revisions should not need to set the session this way
    // and setActiveTestRunSession should be private
    final TestGeneratorViewPart viewPart = TestGeneratorViewPart.openInstance();
    viewPart.getSite().getShell().getDisplay().syncExec(new Runnable() {

      public void run() {
        viewPart.setActiveTestRunSession(session);
      }
    });

    ArrayList<String> vmArguments = new ArrayList<String>();
    ArrayList<String> programArguments = new ArrayList<String>();
    collectExecutionArguments(configuration, vmArguments, programArguments);
    collectProgramArguments(launchResources, programArguments);

    // Classpath
    List<String> cpList = new ArrayList<String>(Arrays.asList(getClasspath(configuration)));

    for (IPath path : launchResources.getClasspathLocations()) {
      cpList.add(path.makeRelative().toOSString());
    }
    cpList.add(RandoopPlugin.getRandoopJar().toOSString());
    cpList.add(RandoopPlugin.getPlumeJar().toOSString());

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

  protected void collectProgramArguments(RandoopLaunchResources launchResources,
      List<String> programArguments) {
    RandoopArgumentCollector args = launchResources.getArguments();
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
    programArguments.add("--inputlimit=" + args.getInputLimit());//$NON-NLS-1$
    programArguments.add("--timelimit=" + args.getTimeLimit());//$NON-NLS-1$
    programArguments.add("--junit-output-dir=" + launchResources.getOutputLocation().toOSString());//$NON-NLS-1$
    programArguments.add("--junit-package-name=" + args.getJUnitPackageName());//$NON-NLS-1$
    programArguments.add("--junit-classname=" + args.getJUnitClassName());//$NON-NLS-1$
    programArguments.add("--output-tests=" + args.getTestKinds());//$NON-NLS-1$
    programArguments.add("--outputlimit=" + args.getMaxTestsWritten());//$NON-NLS-1$
    programArguments.add("--testsperfile=" + args.getMaxTestsPerFile());//$NON-NLS-1$
    programArguments.add("--methodlist=" + launchResources.getMethodFile().getAbsolutePath());//$NON-NLS-1$
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
