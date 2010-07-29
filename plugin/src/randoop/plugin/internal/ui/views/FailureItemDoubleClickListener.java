package randoop.plugin.internal.ui.views;

import java.io.InputStream;
import java.io.StringReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.RandoopCoreUtil;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.model.resultstree.FailingMember;
import randoop.plugin.model.resultstree.UnitTest;
import randoop.plugin.util.ReaderInputStream;


public class FailureItemDoubleClickListener implements IDoubleClickListener {

  public TestGeneratorViewPart viewPart;

	  @Override
	  public void doubleClick(DoubleClickEvent e) {
	    if (viewPart != null) {
	      TreeSelection selection = (TreeSelection) e.getSelection(); // ensure?
	      for (TreePath p : selection.getPaths()) { // Should only have one?
	        if (p.getLastSegment() instanceof FailingMember) {
	          createAndOpenFile((FailingMember) p.getLastSegment());
	        }
	      }
	    }
	  }

	  
//	  // TODO error message if junit is not in classpath of project under test.
	  private void createAndOpenFile(FailingMember unitTest) {
	    IProject project = getJavaProject().getProject();
	    System.out.println(">>>" + project.getName());
	    
	    IPath junitFilePath = new Path(unitTest.witnessTest.junitFile.getPath());
	    if (project.getFullPath().isPrefixOf(junitFilePath)) {
	      junitFilePath = junitFilePath.removeFirstSegments(project.getFullPath().segmentCount());
	    } else if (project.getLocation().isPrefixOf(junitFilePath)) {
	      junitFilePath = junitFilePath.removeFirstSegments(project.getLocation().segmentCount());
	      junitFilePath = junitFilePath.setDevice(null);
	    } else {
	      // Otherwise something is very wrong, the file is not in the project at all!
	      RandoopPlugin.log(StatusFactory.createErrorStatus("Generated failure file not in selected project.")); //$NON-NLS-1$
	    }
	      
	    IFile file = project.getFile(junitFilePath);
	    
	    /// XXX TODO What is the right way to handle this exception??
	    try {
        file.refreshLocal(IResource.DEPTH_ONE, null);
      } catch (CoreException e) {
        throw new RuntimeException(e);
      }

	    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    IWorkbenchPage page = window.getActivePage();

	    //IFile file = getJavaProject().getProject().getFile(junitFile.getPath());
	    //System.out.println("@@@FILE:" + file);
	    // This code needs to run on the UI thread and result from
	    // getActiveWorkbenchWindow() and getActivePage() need to be checked against
	    // null.
	    IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
	    IEditorPart editor = null;
	    try {
	      editor = page.openEditor(new FileEditorInput(file), desc.getId());
	    } catch (PartInitException e) {
	      throw new Error(e);
	    }
	    
//	    JUnitLaunchShortcut launchShortcut = new JUnitLaunchShortcut();
//	    launchShortcut.launch(editor, "run"); // XXX editor could be null
	  }

	  private IJavaProject getJavaProject() {
	    String projectName = RandoopArgumentCollector.getProjectName(viewPart.launch.getLaunchConfiguration());
	    return RandoopCoreUtil.getProjectFromName(projectName);
	  }
//	  
//	  private IPath getOutputDir() {
//	    ILaunchConfiguration iconfig = viewPart.launch.getLaunchConfiguration();
//	    RandoopLaunchConfiguration config = new RandoopLaunchConfiguration(iconfig);
//	    RandoopArgumentCollector args = config.getArguments();
//	    IPath outputDir = args.getOutputDirectory();
//	    return outputDir;
//	  }
	
}
