package randoop.plugin.internal.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

/**
 * Manages and supplies resources used for generating a test set. Resources
 * include the temporary folder for storing class files, and arguments used for
 * generating the test files.
 */
public class TestGroupResources {
  public static final String TEMP_SEGMENT = "/temp"; //$NON-NLS-1$
  private static final IPath TEMP_PATH = RandoopPlugin.getDefault().getStateLocation().append(TEMP_SEGMENT);
  private static final String METHODS_FILE = "methods"; //$NON-NLS-1$
  public final IPath fOoutputPath;

  private RandoopArgumentCollector fArguments;
  private File fResourceFolder;
  private File fMethodsFile;
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
  public TestGroupResources(RandoopArgumentCollector args, IProgressMonitor monitor) {
    
    Assert.isLegal(args != null);
    
    if (monitor == null)
      monitor = new NullProgressMonitor();
    
    fArguments = args;

    Assert.isLegal(fArguments.getJavaProject() != null);

    // Create a unique name from the name and time stamp
    fId = IConstants.EMPTY_STRING + Math.abs(args.getName().hashCode()) + '.'
        + System.currentTimeMillis() + '.' + System.nanoTime();

    // Make a directory that may be used for storing temporary file if needed
    fResourceFolder = TEMP_PATH.append(fId).toFile();
    fResourceFolder.mkdirs();
    
    // Search the arguments for all necessary classpaths in the workspace
    fStatus = findClasspaths(monitor);
    
    writeMethods();
    
    String packagePath = args.getJUnitPackageName().replace('.', '/');
    fOoutputPath = args.getOutputDirectory().append(packagePath);
  }

  private void writeMethods() {
    try {
      fMethodsFile = new File(fResourceFolder, METHODS_FILE);
      fMethodsFile.createNewFile();
      
      FileWriter fw = new FileWriter(fMethodsFile);
      BufferedWriter bw = new BufferedWriter(fw);
      
      List<IMethod> methods = fArguments.getSelectedMethods();
      
      for (IMethod method : methods) {
        boolean isConstructor = method.isConstructor();
        
        if(isConstructor) {
          bw.write("cons : "); //$NON-NLS-1$
        } else {
          bw.write("method : "); //$NON-NLS-1$
        }
        
        bw.write(method.getDeclaringType().getFullyQualifiedName());
        bw.write('.');
        bw.write(method.getElementName());
        if(isConstructor) {
          bw.write("<init>"); //$NON-NLS-1$
        }
        bw.write('(');
        
        String[] parameters = method.getParameterTypes();
        for(int i=0;i<parameters.length;i++) {
          String parameter = Signature.toString(parameters[i]);
          IType type = method.getDeclaringType();
          
          String[][] types = type.resolveType(parameter);
          
          if (types != null) {
            // Write the first type that was resolved
            bw.write(types[0][0]); // the package name
            bw.write('.');
            bw.write(types[0][1]); // the class name
          } else {
            // Otherwise this is a primitive type, write it as it is
            bw.write(parameter);
          }
          if (i < parameters.length - 1) {
            bw.write(',');
          }
        }
        bw.write(')');
        
        bw.newLine();
        bw.flush();
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
      for (IType type : fArguments.getSelectedTypes()) {
        usedProjects.add(type.getJavaProject());
      }
      for (IMethod method : fArguments.getSelectedMethods()) {
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

      return errorStatus == null ? StatusFactory.OK_STATUS : errorStatus;
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

      return errorStatus == null ? StatusFactory.OK_STATUS : errorStatus;
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
      return StatusFactory.OK_STATUS;
    case IClasspathEntry.CPE_LIBRARY:
      classpaths.add(entry.getPath());
      return StatusFactory.OK_STATUS;
    case IClasspathEntry.CPE_PROJECT:
      try {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject resource = root.getProject(entry.getPath().toString());
        IJavaProject referencedProject = (IJavaProject) resource.getNature(JavaCore.NATURE_ID);

        if (referencedProject != null)
          return findClasspaths(referencedProject, classpaths);

      } catch (CoreException e) {
        return StatusFactory.createErrorStatus(MessageFormat.format("Project {0} could not be found", entry.getPath()));
      }
      return StatusFactory.OK_STATUS;
    case IClasspathEntry.CPE_VARIABLE:
      IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(entry);
      if (resolved == null) {
        return StatusFactory.createErrorStatus("Variable classpath entry could not be resolved.");
      }
      return findClasspaths(resolved, classpaths);
    case IClasspathEntry.CPE_CONTAINER:
      IStatus returnStatus = StatusFactory.OK_STATUS;
      try {
        IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), fArguments.getJavaProject());
        Assert.isNotNull(container);
        
        for (IClasspathEntry cpentry : container.getClasspathEntries()) {
          IStatus status = findClasspaths(cpentry, classpaths);
          if (status.getSeverity() == IStatus.ERROR) {
            returnStatus = status;
          }
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
      return returnStatus;
    default:
      return StatusFactory.createErrorStatus("Unknown entry kind");
    }
  }

  public RandoopArgumentCollector getArguments() {
    return fArguments;
  }

  public File getFolder() {
    return fResourceFolder;
  }
  
  public File getMethodFile() {
    return fMethodsFile;
  }

  public IPath[] getClasspath() {
    return fClasspath;
  }
  
  public IPath getOutputPath() {
    return fOoutputPath;
  }

  public String getId() {
    return fId;
  }

  public IStatus getStatus() {
    return fStatus;
  }

  public static void clearTempLocation() {
    File f = TEMP_PATH.toFile();
    if (f.exists()) {
      Assert.isTrue(delete(f));
    }
  }

  /**
   * Deletes the given <code>File</code>. If the <code>File</code> is a
   * directory, all subdirectories and contained files are deleted. Returns
   * <code>true</code> if all files and subdirectories are successfully deleted.
   * 
   * @param file
   *          <code>File</code> to delete
   * @return <code>true</code> if all files and subdirectories are successfully
   *         deleted, <code>false</code> otherwise. If the specified
   *         <code>File</code> does not exist, <code>false</code> is returned.
   */
  private static boolean delete(File file) {
    if(!file.exists())
      return false;
    
    boolean success = true;
    if (file.isDirectory()) {
      for (File subdir : file.listFiles()) {
        success &= delete(subdir);
      }
    }
    
    return success & file.delete();
  }

}
