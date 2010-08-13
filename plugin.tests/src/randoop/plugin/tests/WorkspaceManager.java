package randoop.plugin.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.tests.resources.FileResources;

@SuppressWarnings("nls")
public class WorkspaceManager extends TestCase {
  public static final String BOUNDARY = "Boundary";
  public static final String COMPILATION_ERROR = "Compilation Error";
  public static final String KENKEN = "KenKen";
  public static final String PATH_PLANNER = "Path Planner";

  public static void setupDemoWorkspace() throws IOException, CoreException {
    IWorkspaceRoot root = getWorkspaceRoot();
    for (IProject project : root.getProjects()) {
      project.delete(true, true, null);
    }

    File destination = root.getLocation().toFile();
    FileResources.copy(getFileInTestBundle("/demo-workspace"), destination, false);

    for (String projectName : destination.list()) {
      if (!projectName.startsWith(".")) {
        IProject project = root.getProject(projectName);
        project.create(null);
        project.open(null);
      }
    }
    
    // Build all projects
    for (String projectName : destination.list()) {
      if (!projectName.startsWith(".")) {
        IProject project = root.getProject(projectName);
        project.build(IncrementalProjectBuilder.FULL_BUILD, null);
      }
    }

  }

  public static IJavaProject getJavaProject(String javaProjectName) throws CoreException {
    IProject project = getWorkspaceRoot().getProject(javaProjectName);
    if (project.exists()) {
      return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
    }
    return null;
  }

  private static File getFileInTestBundle(String localPathName) {
    IPath localPath = new Path(localPathName);

    URL url = FileLocator.find(RandoopPlugin.getDefault().getBundle(), localPath, null);
    try {
      if (url != null) {
        url = FileLocator.toFileURL(url);
        return new Path(url.getPath()).toFile();
      }
    } catch (IOException e) {
    }
    return null;
  }

  public static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
}
