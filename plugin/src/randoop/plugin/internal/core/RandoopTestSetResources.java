package randoop.plugin.internal.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.internal.ui.launchConfigurations.RandoopArgumentCollector;

/**
 * Manages and supplies resources used for generating a test set. Resources
 * include the temporary folder for storing class files, and arguments used for
 * generating the test files.
 */
public class RandoopTestSetResources {
  public static String tempSegment = "temp/"; //$NON-NLS-1$
  public static String methodsSegment = "methods"; //$NON-NLS-1$

  private RandoopArgumentCollector fArguments;
  private IPath fResourceFolder;
  private IPath fMethodsFile;
  private String fId;
  private IPath[] fClasspath;
  private IStatus fStatus;

  /**
   * 
   * 
   * @param args
   *          the arguments from which Java resources will be extracted
   * @param name
   *          a name for this set of resources
   */
  public RandoopTestSetResources(RandoopArgumentCollector args, IProgressMonitor monitor) {
    if (monitor == null)
      monitor = new NullProgressMonitor();

    fArguments = args;

    // Create a unique name from the name and time stamp
    fId = tempSegment + Math.abs(args.getName().hashCode()) + '.'
        + System.currentTimeMillis() + '.' + System.nanoTime();

    // Make a directory that may be used for storing temporary file if needed
    fResourceFolder = RandoopResources.getFullPath(new Path(fId));
    fResourceFolder.toFile().mkdirs();
    
    // Search the arguments for all necessary classpaths in the workspace
    fStatus = findClasspaths(monitor);
    
    writeMethods();
  }

  private void writeMethods() {
    try {
      fMethodsFile = fResourceFolder.append(methodsSegment);
      File f = fMethodsFile.toFile();
      f.createNewFile();
      
      FileWriter fw = new FileWriter(f);
      BufferedWriter bw = new BufferedWriter(fw);
      
      List<IMethod> methods = fArguments.getCheckedMethods();
      
      for (IMethod method : methods) {
        if(method.isConstructor()) {
          bw.write("cons : ");
        } else {
          bw.write("method : ");
        }
        
        bw.newLine();
      }
      
      bw.close();
    } catch (IOException e) {
      fMethodsFile = null;
    } catch (JavaModelException e) {
      fMethodsFile = null;
    }
  }

  /**
   * Finds all the classpaths used by each java project that contains a java
   * type and methods specified by the user to be used as test input. If an
   * error is encountered, the search operation will not halt; the error status
   * will be returned once finished.
   * 
   * @param monitor
   * @return the status of this search operation
   */
  // XXX make this use the monitor
  private IStatus findClasspaths(IProgressMonitor monitor) {
    HashSet<IPath> classpath = new HashSet<IPath>();
    // copying class files to the temporary folder
    
    IStatus errorStatus = null;
    try {
      HashSet<IJavaProject> usedProjects = new HashSet<IJavaProject>();
      
      // Find projects containing the types and methods to be tested
      for (IType type : fArguments.getCheckedTypes()) {
        usedProjects.add(type.getJavaProject());
      }
      for (IMethod method : fArguments.getCheckedMethods()) {
        usedProjects.add(method.getJavaProject());
      }
      
      // Find all the classpaths required by each project
      for (IJavaProject javaProject : usedProjects) {
        classpath.add(javaProject.getOutputLocation());
        
        IStatus status = findClasspaths(javaProject, classpath);
        if (status.getSeverity() == IStatus.ERROR)
          errorStatus = status;
      }

      fClasspath = classpath.toArray(new IPath[classpath.size()]);

      return errorStatus == null ? StatusFactory.createOkStatus() : errorStatus;
    } catch (JavaModelException e) {
      return StatusFactory
          .createErrorStatus("Output location does not exist for Java project");
    }
  }

  /**
   * Searches the specified Java project for the classpaths it uses and adds
   * each to <code>classpaths</code>. This method will recursively call itself
   * if <code>javaProject</code> references other Java projects. If an error is
   * encountered, the search operation will not halt; the error status will be
   * returned once finished.
   * 
   * @param javaProject
   *          Java project to search
   * @param classpaths
   *          the <code>Collection</code> to add classpaths to
   * @return the status of this search operation
   */
  private IStatus findClasspaths(IJavaProject javaProject, HashSet<IPath> classpaths) {
    IStatus errorStatus = null;
    try {
      classpaths.add(javaProject.getOutputLocation());
      
      for (IClasspathEntry entry : javaProject.getRawClasspath()) {
        IStatus status = findClasspaths(entry, classpaths);
        if (status.getSeverity() == IStatus.ERROR)
          errorStatus = status;
      }

      return errorStatus == null ? StatusFactory.createOkStatus() : errorStatus;
    } catch (JavaModelException e) {
      return StatusFactory.createErrorStatus("Output location or raw classpath does not exist for Java project: "
          + javaProject.getElementName());
    }
  }

  /**
   * Adds the classpaths found for the specified <code>IClasspathEntry</code> to
   * the <code>Collection</code>. <code>IClasspathEntry</code>'s of type
   * <code>CPE_SOURCE</code> and <code>CPE_SOURCE</code> are immediately added.
   * Entries of type <code>CPE_PROJECT</code> are added by calling:
   * <p>
   * <code>private IStatus findClasspaths(IJavaProject javaProject, HashSet<IPath> classpaths)</code>
   * <p>
   * <code>CPE_VARIABLE</code> kinds are resolved before being recursively
   * passed into this method again.
   * 
   * @param entry
   * @param classpaths
   * @return
   */
  private IStatus findClasspaths(IClasspathEntry entry, HashSet<IPath> classpaths) {
    switch (entry.getEntryKind()) {
    case IClasspathEntry.CPE_SOURCE:
      IPath outputLocation = entry.getOutputLocation();
      if (outputLocation != null) {
        classpaths.add(outputLocation);
      }
      return StatusFactory.createOkStatus();
    case IClasspathEntry.CPE_LIBRARY:
      classpaths.add(entry.getPath());
      return StatusFactory.createOkStatus();
    case IClasspathEntry.CPE_PROJECT:
      try {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject resource = root.getProject(entry.getPath().toString());
        IJavaProject referencedProject = (IJavaProject) resource
            .getNature(JavaCore.NATURE_ID);

        if (referencedProject != null)
          return findClasspaths(referencedProject, classpaths);

      } catch (CoreException e) {
        return StatusFactory.createErrorStatus("Project could not be found: "
            + entry.getPath());
      }
      return StatusFactory.createOkStatus();
    case IClasspathEntry.CPE_VARIABLE:
      IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(entry);
      if (resolved == null) {
        return StatusFactory
            .createErrorStatus("Variable classpath entry could not be resolved.");
      }
      return findClasspaths(resolved, classpaths);
    case IClasspathEntry.CPE_CONTAINER:
      // XXX implement this
      return StatusFactory.createOkStatus();
    default:
      return StatusFactory.createErrorStatus("Unknown entry kind");
    }
  }

  public RandoopArgumentCollector getArguments() {
    return fArguments;
  }

  public IPath getFolder() {
    return fResourceFolder;
  }
  
  public IPath getMethodFilePath() {
    return fMethodsFile;
  }

  public IPath[] getClasspath() {
    return fClasspath;
  }

  public String getId() {
    return fId;
  }

  public IStatus getStatus() {
    return fStatus;
  }
}
