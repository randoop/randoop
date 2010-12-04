package randoop.plugin.internal.core.launching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import randoop.StatementKinds;
import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.RandoopCoreUtil;
import randoop.plugin.internal.ui.MessageUtil;

/**
 * Creates and supplies resources on the filesystem that are needed for
 * generating tests with the given arguments. A temporary folder the the Randoop
 * plug-in's state location stores the method-list used for passing Randoop a
 * list of specific methods to test.
 * 
 * @author Peter Kalauskas
 */
public class RandoopLaunchResources {
  public static final String LAUNCH_SEGMENT = "/launch"; //$NON-NLS-1$
  private static final IPath LAUNCH_PATH = RandoopPlugin.getDefault().getStateLocation().append(LAUNCH_SEGMENT);
  private static final String METHODS_FILE = "methods"; //$NON-NLS-1$
  
  private static final String FAILURE_PATH = "randoopFailures"; //$NON-NLS-1$
  private static final String FAILURE_SUFFIX = "_failure_"; //$NON-NLS-1$
  
  private IFolder fJUnitOutputFolder;
  private IFolder fFailureOutputFolder;
  private RandoopArgumentCollector fArguments;
  private File fResourceFolder;
  private File fMethodsFile;
  private String fId;
  private IPath fOutputLocation;

  /**
   * Creates the output directory required by the Randoop launch arguments.
   * 
   * @param args
   *          the arguments from which Java resources will be extracted
   * @throws CoreException
   */
  public RandoopLaunchResources(RandoopArgumentCollector args, IProgressMonitor monitor) throws CoreException {

    Assert.isLegal(args != null);
    
    if (monitor == null)
      monitor = new NullProgressMonitor();
    
    fArguments = args;

    Assert.isLegal(fArguments.getJavaProject() != null);

    // Create a unique name from the name and time stamp
    StringBuilder prehashString = new StringBuilder();
    prehashString.append(args.getName());
    prehashString.append(System.currentTimeMillis());
    prehashString.append(System.nanoTime());
    fId = Integer.toString(Math.abs(prehashString.toString().hashCode()));

    // Make a directory that may be used for storing temporary file if needed
    fResourceFolder = LAUNCH_PATH.append(fId).toFile();
    fResourceFolder.mkdirs();
    
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
        // XXX On some systems files and directories cannot be named the same
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

      IResource[] resources = folder.members();
      IPath[] excludedItems = new IPath[resources.length];
      // Exclude any pre-existing files from the source folder we are about to create
      for (int i = 0; i < resources.length; i++) {
        excludedItems[i] = resources[i].getFullPath().makeRelativeTo(folder.getFullPath())
            .addTrailingSeparator();
      }
      
      IClasspathEntry newEntry = JavaCore.newSourceEntry(folder.getFullPath(), excludedItems);
      entries.add(newEntry);
      
      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
      javaProject.getProject().refreshLocal(IResource.DEPTH_ONE, monitor);
    }

    fJUnitOutputFolder = getFolder(root, fullOutputDirPath.append(packagePath));
    fFailureOutputFolder = getFolder(root, fullOutputDirPath.append(FAILURE_PATH));
  }

  /**
   * Returns a folder with the given path in the workspace root.
   * 
   * @param root
   *          workspace root to make the path relative to
   * @param path
   *          path relative to the workspace
   * @return the folder at the given path, or <code>null</code> if none was
   *         found
   */
  private static IFolder getFolder(IWorkspaceRoot root, IPath path) {
    IResource outputDirResource = root.findMember(path);
    if (outputDirResource != null && outputDirResource instanceof IFolder) {
      return (IFolder) outputDirResource;
    }
    return null;
  }
  
  /**
   * Writes the method-inputs to a methods file in a format that can be
   * interpreted by {@link StatementKinds}
   */
  private void writeMethods() {
    try {
      fMethodsFile = new File(fResourceFolder, METHODS_FILE);
      fMethodsFile.createNewFile();

      FileWriter fw = new FileWriter(fMethodsFile);
      BufferedWriter bw = new BufferedWriter(fw);

      Map<IType, List<IMethod>> methods = fArguments.getSelectedMethodsByType();

      String unusedStatments = new String();
      for (IType type : methods.keySet()) {
        for (IMethod method : methods.get(type)) {
          // XXX: This blocks the output of any methods that use type variables
          boolean hasTypeVariables = false;
          if (method.getTypeParameters().length != 0)
            hasTypeVariables = true;

          List<String> potentialTypeVars = new ArrayList<String>();
          potentialTypeVars.add(method.getReturnType());
          potentialTypeVars.addAll(Arrays.asList(method.getParameterTypes()));
          for (String paramType : potentialTypeVars) {
            String sig = RandoopCoreUtil.getUnresolvedFullyQualifiedMethodSignature(method, paramType);

            int arrayCount = Signature.getArrayCount(sig);
            String sigWithoutArray = sig.substring(arrayCount);
            if (sigWithoutArray.charAt(0) == Signature.C_TYPE_VARIABLE) {
              hasTypeVariables = true;
              break;
            }
          }

          StringBuilder statement = new StringBuilder();

          boolean isConstructor = method.isConstructor();
          if (isConstructor) {
            statement.append("cons : "); //$NON-NLS-1$
          } else {
            statement.append("method : "); //$NON-NLS-1$
          }

          statement.append(method.getDeclaringType().getFullyQualifiedName());
          statement.append('.');
          if (isConstructor) {
            statement.append("<init>"); //$NON-NLS-1$
          } else {
            statement.append(method.getElementName());
          }
          statement.append('(');

          String[] parameters = method.getParameterTypes();
          for (int i = 0; i < parameters.length; i++) {
            String parameter = Signature.toString(parameters[i]);

            String[][] types = type.resolveType(parameter);

            if (types != null) {
              // Write the first type that was resolved
              statement.append(types[0][0]); // the package name
              statement.append('.');
              statement.append(types[0][1]); // the class name
            } else {
              // Otherwise this is a primitive type, write it as it is
              statement.append(parameter);
            }
            if (i < parameters.length - 1) {
              statement.append(',');
            }
          }
          statement.append(')');

          if (hasTypeVariables) {
            unusedStatments += statement.toString() + '\n';
          } else {
            bw.write(statement.toString());
            bw.newLine();
            bw.flush();
          }
        }
      }
      
      bw.close();
      
      if (unusedStatments.length() != 0) {
        unusedStatments = "The Randoop Eclipse plugin does not currently support selectively testing methods that use type variables. The following methods will not be tested:\n\n" //$NON-NLS-1$
            + unusedStatments;
        MessageUtil.openInformation(unusedStatments);
      }
    } catch (IOException e) {
      fMethodsFile = null;
    } catch (JavaModelException e) {
      fMethodsFile = null;
    }
  }

  /**
   * Returns the arguments that were used to construct this instance.
   * 
   * @return arguments used to construct this instance
   */
  public RandoopArgumentCollector getArguments() {
    return fArguments;
  }

  public File getMethodFile() {
    return fMethodsFile;
  }

  public IPath getOutputLocation() {
    return fOutputLocation;
  }
  
  /**
   * Returns a list of IResources that may be overwritten by the generated
   * tests. Similarly named files match the patterns <ClassName>[0-9]*.java or
   * <ClassName>_failure_[0-9]*.java and are found in the output source folder's
   * JUnit test package and failures package respectively.
   * 
   * @return
   * @throws CoreException
   */
  public IResource[] getThreatendedResources() throws CoreException {
    List<IResource> threatenedFiles = new ArrayList<IResource>();

    String testName = getArguments().getJUnitClassName();
    threatenedFiles.addAll(findResources(fJUnitOutputFolder,
        testName + "\\p{Digit}*.java")); //$NON-NLS-1$
    
    threatenedFiles.addAll(findResources(fFailureOutputFolder,
        testName + FAILURE_SUFFIX + "\\p{Digit}*.java")); //$NON-NLS-1$

    return threatenedFiles.toArray(new IResource[threatenedFiles.size()]);
  }
  
  private static List<IResource> findResources(IFolder folder, String pattern) throws CoreException {
    List<IResource> resources = new ArrayList<IResource>();
    
    if (folder != null && folder.exists()) {
      for (IResource resource : folder.members()) {
        if (resource instanceof IFile) {
          String resourceName = resource.getName();
          if (resourceName.matches(pattern)) {
            resources.add(resource);
          }
        }
      }
    }
    
    return resources;
  }

  /**
   * Removes the temporary folder used for writing all Randoop launch related
   * resources from the filesystem.
   */
  public static void deleteAllLaunchResources() {
    File f = LAUNCH_PATH.toFile();
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
   * @return <code>true</code> if the file exists and all files and
   *         subdirectories are successfully deleted, <code>false</code>
   *         otherwise
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
