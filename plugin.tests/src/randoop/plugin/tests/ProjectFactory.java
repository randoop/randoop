package randoop.plugin.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jdt.internal.junit.buildpath.BuildPathSupport;
import org.eclipse.jdt.launching.JavaRuntime;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.tests.resources.FileResources;

@SuppressWarnings("nls")
public class ProjectFactory {
  
  public static IJavaProject createPathPlannerProject() {
    IWorkspaceRoot root = getWorkspaceRoot();

    ProjectModel ppModel = new ProjectModel("Path Planner", getFileInTestBundle("/demo/pp"));

    ppModel.addSourceFolder("src");
    ppModel.addSourceFolder("test");

    return create(root, ppModel);
  }
  
  public static IJavaProject createKenKenProject() {
    IWorkspaceRoot root = getWorkspaceRoot();

    ProjectModel kenkenModel = new ProjectModel("KenKen", getFileInTestBundle("/demo/kenken"));
    kenkenModel.addSourceFolder("src");
    kenkenModel.addSourceFolder("test");

    return create(root, kenkenModel);
  }
  
  private static IJavaProject create(IWorkspaceRoot root, ProjectModel projectModel) {
    IProject project = root.getProject(projectModel.getProjectName());

    if (project.exists()) {
      System.out.println(projectModel.getProjectName() + " already exists!");
      return JavaCore.create(project);
    } else {
      try {
        project.create(null);
        project.open(null);
        IProjectDescription description = project.getDescription();

        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
        project.setDescription(description, null);
        IJavaProject javaProject = JavaCore.create(project);

        List<IFolder> sourceFolders = new ArrayList<IFolder>();
        List<IClasspathEntry> cpEntries = new ArrayList<IClasspathEntry>();
        for(String sourceFolderName : projectModel.getSourceFolders()) {
          IFolder sourceFolder = project.getFolder(sourceFolderName);
          sourceFolder.create(true, true, null);
          sourceFolders.add(sourceFolder);
          
          IPath srcPath = javaProject.getPath().append(sourceFolderName);
          cpEntries.add(JavaCore.newSourceEntry(srcPath));
        }
        
        cpEntries.add(JavaRuntime.getDefaultJREContainerEntry());
        cpEntries.add(BuildPathSupport.getJUnit4ClasspathEntry());
        
        IClasspathEntry[] buildPath = cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
        
        javaProject.setRawClasspath(buildPath, null);

        File destination = root.getLocation().append(javaProject.getPath()).toFile();
        try {
          FileResources.copy(projectModel.getProjectDirectory(), destination);
        } catch (IOException e) {
          e.printStackTrace();
        }

        for (IFolder sourceFolder : sourceFolders) {
          sourceFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
        }
        
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        
        return javaProject;
      } catch (CoreException e) {
        e.printStackTrace();
        return null;
      }
    }
  }
  
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
  
  private static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
  
}
