package randoop.plugin.internal.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
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
  
  public final IFolder fOutputFolder;
  private RandoopArgumentCollector fArguments;
  private File fResourceFolder;
  private File fMethodsFile;
  private String fId;
  private IPath[] fClasspath;
  private IPath fOutputLocation;

  /**
   * 
   * 
   * @param args
   *          the arguments from which Java resources will be extracted
   * @param name
   *          a name for this set of resources
   * @throws CoreException
   */
  public TestGroupResources(RandoopArgumentCollector args, IProgressMonitor monitor) throws CoreException {

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
    fClasspath = findClasspathLocations(args.getJavaProject());
    
    writeMethods();
    
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    // The full path to the output directory, /Project/src/
    IPath fullOutputDirPath = args.getOutputDirectory();
    
    // The relative location of the junit package, org/example
    String packagePath = args.getJUnitPackageName().replace('.', '/');
    IJavaProject javaProject = args.getJavaProject();
    Assert.isTrue(javaProject.getPath().isPrefixOf(fullOutputDirPath));
    fOutputLocation = root.getLocation().append(fullOutputDirPath);
    
    IPath outputDirPath = fullOutputDirPath.removeFirstSegments(1);
    
    IPackageFragmentRoot pfr = RandoopCoreUtil.getPackageFragmentRoot(javaProject, outputDirPath.toString());
    if (pfr == null) {
      // Make the directory
      IFolder folder = root.getFolder(fullOutputDirPath);
      File file = folder.getLocation().toFile();
      if (!file.exists() || !file.isDirectory()) {
        // On some systems files and directories cannot be named the same
        // XXX - Handle this situation more elegantly
        Assert.isTrue(file.mkdirs());
      }
      
      List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject.getRawClasspath()));

      // Search for any package fragment root that may exist in an ancestor of this IResource
      IContainer container = folder;
      while ((pfr == null || !pfr.exists()) && container.getParent() != null && container.getParent() instanceof IFolder) {
        container = container.getParent();
        pfr = javaProject.getPackageFragmentRoot(container);
      }
      
      if (pfr != null && pfr.exists()) {
        // Add the new output directory to the list of exclusion patterns
        IClasspathEntry originalEntry = pfr.getRawClasspathEntry();
        Assert.isTrue(originalEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE);

        IPath suffix = folder.getFullPath()
            .removeFirstSegments(container.getFullPath().segmentCount()).addTrailingSeparator();
        List<IPath> exclusionPatterns = new ArrayList<IPath>(Arrays.asList(originalEntry
            .getExclusionPatterns()));
        if (!exclusionPatterns.contains(suffix)) {
          exclusionPatterns.add(suffix);
        }

        IClasspathEntry replacementEntry = JavaCore.newSourceEntry(originalEntry.getPath(),
            originalEntry.getInclusionPatterns(),
            exclusionPatterns.toArray(new IPath[exclusionPatterns.size()]),
            originalEntry.getOutputLocation(), originalEntry.getExtraAttributes());

        for (int i = 0; i < entries.size(); i++) {
          if (entries.get(i).equals(originalEntry)) {
            entries.set(i, replacementEntry);
          }
        }
      }
      IClasspathEntry newEntry = JavaCore.newSourceEntry(folder.getFullPath());
      entries.add(newEntry);
      
      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
    }
    
    IResource outputDirResource = root.findMember(fullOutputDirPath.append(packagePath));
    if (outputDirResource != null) {
      Assert.isTrue(outputDirResource instanceof IFolder);
      fOutputFolder = (IFolder) outputDirResource;
    } else {
      fOutputFolder = null;
    }
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
   * @throws CoreException
   */
  private IPath[] findClasspathLocations(IJavaProject javaProject) throws CoreException {
    List<IPath> classpaths = new ArrayList<IPath>();
    classpaths.add(javaProject.getOutputLocation());

    for (IClasspathEntry entry : javaProject.getRawClasspath()) {
      classpaths.addAll(Arrays.asList(findClasspathLocations(entry)));
    }

    return classpaths.toArray(new IPath[classpaths.size()]);
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
   * @throws CoreException
   */
  private IPath[] findClasspathLocations(IClasspathEntry entry) throws CoreException {
    List<IPath> classpaths = new ArrayList<IPath>();

    switch (entry.getEntryKind()) {
    case IClasspathEntry.CPE_SOURCE:
      IPath outputLocation = entry.getOutputLocation();
      if (outputLocation != null) {
        classpaths.add(outputLocation);
      }
      break;
    case IClasspathEntry.CPE_LIBRARY:
      classpaths.add(entry.getPath());
      break;
    case IClasspathEntry.CPE_PROJECT:
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IProject resource = root.getProject(entry.getPath().toString());
      IJavaProject referencedProject = (IJavaProject) resource.getNature(JavaCore.NATURE_ID);

      if (referencedProject != null)
        classpaths.addAll(Arrays.asList(findClasspathLocations(referencedProject)));
      break;
    case IClasspathEntry.CPE_VARIABLE:
      IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(entry);
      if (resolved != null)
        classpaths.addAll(Arrays.asList(findClasspathLocations(resolved)));
      break;
    case IClasspathEntry.CPE_CONTAINER:
      IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(),
          fArguments.getJavaProject());
      Assert.isNotNull(container);

      for (IClasspathEntry cpentry : container.getClasspathEntries()) {
        classpaths.addAll(Arrays.asList(findClasspathLocations(cpentry)));
      }
      break;
    }

    return classpaths.toArray(new IPath[classpaths.size()]);
  }

  public RandoopArgumentCollector getArguments() {
    return fArguments;
  }

  public File getMethodFile() {
    return fMethodsFile;
  }

  public IPath[] getClasspathLocations() {
    return fClasspath;
  }
  
  public IFolder getOutputFolder() {
    return fOutputFolder;
  }
  
  public IPath getOutputLocation() {
    return fOutputLocation;
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
