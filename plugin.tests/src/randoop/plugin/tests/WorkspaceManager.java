package randoop.plugin.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import randoop.plugin.RandoopPlugin;

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
    copy(getFileInTestBundle("/demo-workspace"), destination, false);

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
  
  public static void buildWorkspace() throws CoreException {
    ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new IProgressMonitor() {

      @Override
      public void worked(int work) {
      }

      @Override
      public void subTask(String name) {
        System.out.println(name);
      }

      @Override
      public void setTaskName(String name) {
        System.out.println(name);
      }

      @Override
      public void setCanceled(boolean value) {
      }

      @Override
      public boolean isCanceled() {
        return false;
      }

      @Override
      public void internalWorked(double work) {
      }

      @Override
      public void done() {
      }

      @Override
      public void beginTask(String name, int totalWork) {
      }
    });
  }

  public static IJavaProject getJavaProject(String javaProjectName) throws CoreException {
    IProject project = getWorkspaceRoot().getProject(javaProjectName);
    if (project.exists()) {
      return JavaCore.create(project);
    } else {
      return null;
    }
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
  
  public static void copy(File source, File destination, boolean noClobber) throws IOException {
    if (source.isFile()) {
      copyFile(source, destination, noClobber);
    } else {
      copyDirectory(source, destination, noClobber);
    }
  }

  private static void copyDirectory(File source, File destination, boolean noClobber) throws IOException {
    assertTrue(source.isDirectory());
    assertTrue(!destination.exists() || destination.isDirectory());

    if (!destination.exists()) {
      destination.mkdirs();
    }

    for (File f : source.listFiles()) {
      copy(f, new File(destination, f.getName()), noClobber);
    }
  }

  /**
   * Copies a file from a source to the supplied destination. The destination
   * file and its parent directories will be created if they do not exist.
   * 
   * @param source
   *          file to be copied
   * @param destination
   *          location of new file
   * @param noClobber
   * @param monitor
   * @throws IOException
   */
  private static void copyFile(File source, File destination, boolean noClobber) throws IOException {
    if (noClobber && destination.exists()) {
      return;
    }

    if (!destination.exists()) {
      // Create the directories and file for destination
      destination.getParentFile().mkdirs();
      destination.createNewFile();
    }

    InputStream in = new FileInputStream(source);
    OutputStream out = new FileOutputStream(destination);

    byte[] buffer = new byte[1024];
    int length;

    while ((length = in.read(buffer)) > 0) {
      out.write(buffer, 0, length);
    }

    in.close();
    out.close();
  }
  
}
