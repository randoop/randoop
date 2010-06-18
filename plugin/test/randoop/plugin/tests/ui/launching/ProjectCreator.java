package randoop.plugin.tests.ui.launching;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.internal.junit.buildpath.BuildPathSupport;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.IRandoopLaunchConfigurationConstants;
import randoop.plugin.tests.resources.FileResources;

public class ProjectCreator extends TestCase {
  private static HashMap<String, Boolean> okToDeleteByWorkspace = new HashMap<String, Boolean>();
  public static String demoProjectName = "Demo Project"; //$NON-NLS-1$
  public static String sourceFolderName = "src"; //$NON-NLS-1$
  public static String testFolderName = "test"; //$NON-NLS-1$
  
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

  public static IJavaProject create(String projectName, Collection<File> contents) {
    if (contents == null)
      contents = new ArrayList<File>();
    
    for (File f : contents) {
      assertTrue(f.isAbsolute());
    }

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(projectName);

    if (project.exists()) {
      System.out.println(projectName + " already exists!");
      return JavaCore.create(project);
    } else {
      try {
        project.create(null);
        project.open(null);
        IProjectDescription description = project.getDescription();

        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
        project.setDescription(description, null);
        IJavaProject javaProject = JavaCore.create(project);

        IFolder srcFolder = project.getFolder(sourceFolderName);
        IFolder testFolder = project.getFolder(testFolderName);
        srcFolder.create(true, true, null);
        testFolder.create(true, true, null);

        IPath srcPath = javaProject.getPath().append(sourceFolderName);
        IPath testPath = javaProject.getPath().append(testFolderName);
        IClasspathEntry srcEntry = JavaCore.newSourceEntry(srcPath);
        IClasspathEntry testEntry = JavaCore.newSourceEntry(testPath);

        IClasspathEntry[] buildPath = {
            srcEntry,
            testEntry, 
            JavaRuntime.getDefaultJREContainerEntry(),
            BuildPathSupport.getJUnit4ClasspathEntry()
        };

        javaProject.setRawClasspath(buildPath, null);

        File destination = root.getLocation().append(javaProject.getPath()).toFile();

        try {
          for (File f : contents) {
            FileResources.copy(f, destination);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

        srcFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
        testFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
        
        return javaProject;
      } catch (CoreException e) {
        e.printStackTrace();
        return null;
      }
    }
  }

  /**
   * 
   * WARNING: This will delete all projects from the working directory.
   * 
   * @throws Exception
   */
  public static IJavaProject createStandardDemoProject() {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    
    final String location = root.getLocation().toOSString();
    Boolean okToDelete = okToDeleteByWorkspace.get(location);
    
    boolean continuteWithTest;
    if (okToDelete == null) {
      final MutableBoolean response = new MutableBoolean(false);
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          IWorkbench workbench = PlatformUI.getWorkbench();
          IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
          
          response.setResponse(MessageDialog
              .openQuestion(window.getShell(),
                  "Warning", //$NON-NLS-1$
                  "This test will delete all contents of the active workspace:\n" //$NON-NLS-1$
                    + location + "\n\n" //$NON-NLS-1$
                    + "Do you want to continue? (Pressing Yes will delete workspace)")); //$NON-NLS-1$
        }
      });
      
      continuteWithTest = response.getValue();
      okToDeleteByWorkspace.put(location, continuteWithTest);
    } else {
      continuteWithTest = okToDelete.booleanValue();
    }
    
    if (continuteWithTest) {
      // Test fails if an exception is thrown
      ProjectCreator.clearWorkspace();

      Collection<File> contents = new ArrayList<File>();
      contents.add(getFileInTestBundle("/demo"));//$NON-NLS-1$

      return ProjectCreator.create(demoProjectName, contents);
    } else {
      fail();
      return null;
    }
  }

  public static ILaunchConfigurationWorkingCopy createNewAllTypeConfig(IJavaProject javaProject) throws CoreException {
    ILaunchConfigurationType randoopType = DebugPlugin.getDefault()
    .getLaunchManager().getLaunchConfigurationType(
        IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION);

    final ILaunchConfigurationWorkingCopy config = randoopType.newInstance(null, "All Type Config");
    
    IProject project = javaProject.getProject();
    IFolder folder = project.getFolder(ProjectCreator.testFolderName);
    IPackageFragmentRoot testFolder = javaProject
        .getPackageFragmentRoot(folder);

    // Search for all java elements in the project and add them to the
    // configuration
    List<String> availableTypes = new ArrayList<String>();
    for (IPackageFragmentRoot pfRoot : javaProject.getPackageFragmentRoots()) {
      if (pfRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
        for (IJavaElement element : pfRoot.getChildren()) {
          if (element instanceof IPackageFragment) {
            IPackageFragment packageFragment = (IPackageFragment) element;
            for (IJavaElement compElement : packageFragment.getChildren()) {
              if (compElement instanceof ICompilationUnit) {
                IType[] types = ((ICompilationUnit) compElement).getAllTypes();
                for (IType type : types) {
                  availableTypes.add(type.getHandleIdentifier());
                }
              }
            }
          }
        }
      }
    }
    
    RandoopArgumentCollector.setAllJavaTypes(config, availableTypes);
    RandoopArgumentCollector.setOutputDirectoryHandlerId(config, testFolder.getHandleIdentifier());
    RandoopArgumentCollector.setJUnitPackageName(config, "demo.pathplanning.allTests");
    RandoopArgumentCollector.setJUnitPackageName(config, "AllTypeTest");
    
    return config;
  }
  
  public void testCreateProject() throws Exception {
    createStandardDemoProject();
  }
}

