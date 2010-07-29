package randoop.plugin.internal.ui.views;

import java.io.InputStream;
import java.io.StringReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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

import randoop.plugin.internal.core.RandoopCoreUtil;
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
	    
//	    IPath dir = getOutputDir();
//	    dir = dir.removeFirstSegments(1); // XXX yuck yuck yuck
//	    IPath fileAsPath = dir.append("FailingTest.java");
	    
	    String projectPrefix = "/" + project.getName();
	    assert unitTest.witnessTest.junitFile.getPath().startsWith(projectPrefix);
	    
	    IFile file = project.getFile(unitTest.witnessTest.junitFile.getPath().substring(projectPrefix.length()));
	    
	    /// XXX TODO What is the right way to handle this exception??
	    try {
        file.refreshLocal(1, null);
      } catch (CoreException e1) {
        throw new RuntimeException(e1);
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
