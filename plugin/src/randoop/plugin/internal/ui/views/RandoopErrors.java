package randoop.plugin.internal.ui.views;

import java.io.InputStream;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import randoop.ErrorRevealed;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.launching.RandoopLaunchConfiguration;
import randoop.plugin.internal.ui.launching.RandoopLaunchConfigurationUtil;
import randoop.plugin.util.ReaderInputStream;

public class RandoopErrors implements ITreeContentProvider, IDoubleClickListener {

  Map<String, Set<ErrorRevealed>> map = new LinkedHashMap<String, Set<ErrorRevealed>>();
  
  ILaunchConfiguration config;
  
  TreeViewer viewer;
  
  TestGeneratorViewPart viewPart; // Can we find this by other means?
  
  public RandoopErrors(Map<String, Set<ErrorRevealed>> errors) {
    this.map = errors;
  }

  public void add(ErrorRevealed err) {
    if (err == null) {
      throw new IllegalArgumentException("err is null");
    }
    Set<ErrorRevealed> s = map.get(err.description);
    if (s == null) {
      s = new LinkedHashSet<ErrorRevealed>();
      map.put(err.description, s);
    }
    s.add(err);
    
    if (viewer != null) {
      viewer.add(err.description, err);
      viewer.refresh();
    }
  }
  
  public void reset() {
    map = new LinkedHashMap<String, Set<ErrorRevealed>>();
    if (viewer != null) {
      viewer.setInput(map);
      viewer.refresh();
    }
  }
  
  @Override
  public Object[] getChildren(Object n) {
    if (!(n instanceof String)) {
      return new Object[0];
    }
    String s = (String)n;
    Set<ErrorRevealed> err = map.get(s);
    if (err == null) {
      return new Object[0];
    }
    return err.toArray();
  }

  @Override
  public Object getParent(Object n) {
    if (!(n instanceof ErrorRevealed)) {
      return null;
    }
    return ((ErrorRevealed)n).description;
  }

  @Override
  public boolean hasChildren(Object n) {
    if (!(n instanceof String)) {
      return false;
    }
    String s = (String)n;
    Set<ErrorRevealed> err = map.get(s);
    if (err == null) {
      return false;
    }
    return true;
  }

  @Override
  public Object[] getElements(Object arg0) {
    return map.keySet().toArray();
  }

  @Override
  public void dispose() {
    map = null;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    if (viewer instanceof TreeViewer) {
      this.viewer = (TreeViewer)viewer;
    }
    if (newInput instanceof LinkedHashMap) {
      map = (LinkedHashMap<String, Set<ErrorRevealed>>)newInput;
    }
  }

  @Override
  public void doubleClick(DoubleClickEvent e) {
    if (viewPart != null) {
      TreeSelection selection = (TreeSelection) e.getSelection(); // ensure?
      for (TreePath p : selection.getPaths()) { // Should only have one?
        if (p.getLastSegment() instanceof ErrorRevealed) {
          createAndOpenFile((ErrorRevealed) p.getLastSegment());
        }
      }
    }
  }

  private void createAndOpenFile(ErrorRevealed err) {

    IProject project = getJavaProject().getProject();
    IPath dir = getOutputDir();
    dir = dir.removeFirstSegments(1); // XXX yuck yuck yuck
    IPath fileAsPath = dir.append("FailingTest.java");
    IFile file = project.getFile(fileAsPath);
    System.out.println("@@@dir:" + dir.toString());
    System.out.println("@@@fileAsPath:" + fileAsPath.toString());
    System.out.println("@@@file:" + file.toString());
    writeSubSuite(err.testCode, file, "FailingTest"); // TODO use IFIle to create file?

    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    IWorkbenchPage page = window.getActivePage();

    //IFile file = getJavaProject().getProject().getFile(junitFile.getPath());
    System.out.println("@@@FILE:" + file);
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
    
    JUnitLaunchShortcut launchShortcut = new JUnitLaunchShortcut();
    launchShortcut.launch(editor, "run"); // XXX editor could be null
  }

  private IJavaProject getJavaProject() {
    String projectName = RandoopArgumentCollector.getProjectName(viewPart.launch.getLaunchConfiguration());
    return RandoopLaunchConfigurationUtil.getProjectFromName(projectName);
  }
  
  private IPath getOutputDir() {
    ILaunchConfiguration iconfig = viewPart.launch.getLaunchConfiguration();
    RandoopLaunchConfiguration config = new RandoopLaunchConfiguration(iconfig);
    RandoopArgumentCollector args = config.getArguments();
    IPath outputDir = args.getOutputDirectory();
    return outputDir;
  }

  private void writeSubSuite(String testCode, IFile file, String className) {
    StringBuilder b = new StringBuilder();
    b.append("import junit.framework.*;\n\n");
    b.append("@SuppressWarnings(\"unused\")\n");
    b.append("public class " + className + " extends TestCase {\n\n");
    b.append("  public void test() throws Throwable {\n\n");
    for (String line : testCode.split("\n")) {
      b.append("    " + line + "\n");
    }
    b.append("  }\n\n");
    b.append("}\n");
    InputStream instream = new ReaderInputStream(new StringReader(b.toString()));
    try {
      if (!file.exists()) {
        file.create(instream, true, null);
      } else {
        file.setContents(instream, true, false, null);
      }
    } catch (CoreException e) {
      throw new Error(e);
    }
  }
  

}
