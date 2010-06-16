package randoop.plugin.tests.ui.launchConfigurations;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.tests.resources.FileResources;

public class ProjectCreator extends TestCase {
  /**
   * Returns the <code>File</code> in the test bundle given a relative path
   * name, or <code>null</code> if the <code>File</code> does not exist.
   * 
   * @param localPathName
   *          the path relative to the base of the test bundle
   * @return the absolute <code>File</code> in the the test bundle, or
   *         <code>null</code>
   */
  private static File getFileInTestBundle(String localPathName) {
    IPath localPath = new Path(localPathName);

    URL url = FileLocator.find(RandoopPlugin.getDefault().getBundle(),
        localPath, null);
    try {
      url = FileLocator.toFileURL(url);
    } catch (IOException e) {
      return null;
    }
    return new Path(url.getPath()).toFile();
  }

  public static void clearWorkspace() {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    for (IProject project : root.getProjects()) {
      try {
        project.delete(true, true, null);
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }
  }

  public static void create(String projectName, Collection<File> contents) {
    for (File f : contents) {
      assertTrue(f.isAbsolute());
    }

    ProjectCreator.clearWorkspace();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(projectName);

    if (project.exists()) {
      System.out.println(projectName + " already exists!");
    } else {
      try {
        project.create(null);
        project.open(null);
        IProjectDescription description = project.getDescription();

        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
        project.setDescription(description, null);
        IJavaProject javaProject = JavaCore.create(project);

        IFolder srcFolder = project.getFolder("src");
        srcFolder.create(true, true, null);

        IPath srcPath = javaProject.getPath().append("src"); //$NON-NLS-1$
        IClasspathEntry srcEntry = JavaCore.newSourceEntry(srcPath);

        IClasspathEntry[] buildPath = { srcEntry,
            JavaRuntime.getDefaultJREContainerEntry() };

        javaProject.setRawClasspath(buildPath, null);

        File destination = root.getLocation().append(srcPath).toFile();

        try {
          for (File f : contents) {
            FileResources.copy(f, destination);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

        srcFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 
   * WARNING: This will delete all projects from the working directory.
   * @throws Exception
   */
  public void testCreateProject() throws Exception {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    
    boolean continuteWithTest = MessageDialog.openQuestion(
            window.getShell(),
            "Warning", //$NON-NLS-1$
            "This test will delete all contents of the active workspace:\n" //$NON-NLS-1$
            + root.getLocation() + "\n\n" //$NON-NLS-1$
            + "Do you want to continue? (Pressing Yes will delete workspace)"); //$NON-NLS-1$
    
    if (continuteWithTest) {
      // Test fails if an exception is thrown
      ProjectCreator.clearWorkspace();

      Collection<File> contents = new ArrayList<File>();
      contents.add(getFileInTestBundle("/demo"));//$NON-NLS-1$

      ProjectCreator.create("Demo Project", contents); //$NON-NLS-1$
    } else {
      fail();
    }
  }
}
