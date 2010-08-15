package randoop.plugin.internal.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;

/**
 * String mnemonic provider and parser for <code>IType</code>s. This class can
 * can be used to store an <code>IType</code> in a <code>String</code> and
 * reconstruct that <code>IType</code> from a String. The class can also as
 * identify references to the same <code>IType</code> from other Java projects.
 * The mnemonic contains the the Java project name, classpath entry kind,
 * classpath entry path, and fully qualified name of an IType in an unspecified
 * format. projects.
 * <p>
 * Note that type mnemonics are constant; none of the methods will mutate the
 * object being operated on.
 * <p>
 * This class provides several advantages over using handle identifiers provided
 * by <code>IType</code> as specified by their superclass
 * <code>IJavaElement</code>. The format of <code>IJavaElement</code> handle
 * identifiers is not specified, and there are no classes to retrieve
 * information from the <code>String</code>s without the identified resource
 * existing. The method <code>JavaCore.create(java.land.String)</code> will
 * return null if the resource cannot be found on the filesystem. This means
 * that all information in the handle identifier is effectively lost.
 * Contrarily, while <code>TypeMnemonic</code> does not have an
 * externally-defined format for the <code>String</code> mnemonics it produces,
 * it provides a suite of methods to retrieve information from mnemonics for
 * <code>IType</code>s that no longer exist.
 * 
 * @see org.eclipse.jdt.core.IJavaProject
 * @see org.eclipse.jdt.core.IClasspathEntry
 * @see org.eclipse.jdt.core.IType
 * @author Peter Kalauskas
 */
public class TypeMnemonic {
  private static final int LENGTH = 4;
  
  private final IJavaProject fJavaProject;
  private final IClasspathEntry fClasspathEntry;
  private final IType fType;
  
  private final String fJavaProjectName;
  private final int fClasspathKind;
  private final IPath fClasspath;
  private final String fFullyQualifiedTypeName;

  /**
   * Constructs a <code>TypeMnemonic</code> from the given IType. The new type
   * mnemonic is guaranteed to exist.
   * 
   * @param t
   * @throws JavaModelException
   *           if the type mnemonic could not be constructed because of an
   *           exception thrown while package fragment root
   * @throws IllegalArgumentException
   *           if type is null or does not exist
   * 
   * @see TypeMnemonic#exists()
   */
  public TypeMnemonic(IType type) throws JavaModelException {
    Assert.isLegal(type != null, "Type is null");
    Assert.isLegal(type.exists(), "Type does not exist");

    fType = type;
    fJavaProject = fType.getJavaProject();
    Assert.isNotNull(fJavaProject, "Java project is null");
    Assert.isTrue(fJavaProject.exists(), "Java project does not exist");

    IJavaElement pfr = fType.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    Assert.isNotNull(pfr, "Package fragment root is null");
    Assert.isTrue(pfr.exists(), "Package fragment root does not exist");
    
    fClasspathEntry = ((IPackageFragmentRoot) pfr).getRawClasspathEntry();
    Assert.isNotNull(fClasspathEntry);

    fJavaProjectName = fJavaProject.getElementName();
    fClasspathKind = fClasspathEntry.getEntryKind();
    fClasspath = fClasspathEntry.getPath();
    fFullyQualifiedTypeName = fType.getFullyQualifiedName();
  }

  /**
   * 
   * @param javaProjectName
   * @param classpathKind
   * @param classpath
   * @param fullyQualifiedTypeName
   */
  public TypeMnemonic(String javaProjectName, int classpathKind, IPath classpath, String fullyQualifiedTypeName) {
    fJavaProjectName = javaProjectName;
    fClasspathKind = classpathKind;
    fClasspath = classpath;
    fFullyQualifiedTypeName = fullyQualifiedTypeName;

    fJavaProject = null;
    fClasspathEntry = null;
    fType = null;
  }
  
  public TypeMnemonic(String mnemonic) {
    this(mnemonic, null);
  }
  
  /**
   * 
   * @param mnemonic
   * @throws CoreException
   * @throws IllegalArgumentException
   *           if the mnemonic is incorrectly formatted
   */
  public TypeMnemonic(String mnemonic, IWorkspaceRoot root) {
    Assert.isLegal(mnemonic != null);
    
    String[] s = mnemonic.split(IConstants.MNEMONIC_DELIMITER);
    Assert.isLegal(s.length == LENGTH);

    fJavaProjectName = s[0];
    fClasspathKind = Integer.parseInt(s[1]);
    fClasspath = new Path(s[2]);
    fFullyQualifiedTypeName = s[3];
    
    IJavaProject javaProject = null;
    IClasspathEntry classpathEntry = null;
    IType type = null;

    if (root != null) {
      try {
        IProject project = root.getProject(fJavaProjectName);
        if (project.exists()) {
          project.open(null);

          javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
          if (javaProject != null) {
            // Search for the classpath entry in the project
            for (IClasspathEntry cpe : javaProject.getRawClasspath()) {
              if (cpe.getEntryKind() == fClasspathKind && cpe.getPath().equals(fClasspath)) {
                classpathEntry = cpe;
                break;
              }
            }

            if (classpathEntry != null) {
              type = findType(javaProject, classpathEntry, fFullyQualifiedTypeName);
            }
          }
        }
      } catch (CoreException e) {
      }
    }

    fClasspathEntry = classpathEntry;
    fJavaProject = javaProject;
    fType = type;
  }

  public TypeMnemonic resolve(IWorkspaceRoot root) {
    return new TypeMnemonic(toString(), root);
  }

  public TypeMnemonic reassign(IJavaProject javaProject) throws JavaModelException {
    if (javaProject != null && exists()) {
      
      // Check if reassignment is necessary first
      if (javaProject.equals(getJavaProject())) {
        return this;
      }

      IWorkspaceRoot root = getJavaProject().getCorrespondingResource().getWorkspace()
          .getRoot();

      // Ensure both this java project and the one we are switching to are in
      // the same workspace
      Assert.isTrue(root.equals(javaProject.getCorrespondingResource().getWorkspace()
          .getRoot()));

      IClasspathEntry cpe = getClasspathEntry();
      IClasspathEntry newCpe = null;
      switch (cpe.getEntryKind()) {
      case IClasspathEntry.CPE_SOURCE:
        // this entry describes a source root (linked or otherwise) in its
        // project
        IPath cpePath = cpe.getPath().makeRelativeTo(getJavaProject().getPath());
        IFolder cpeFolder = getJavaProject().getProject().getFolder(cpePath);

        if (cpeFolder.isLinked()) {
          IPath actualPath = cpeFolder.getLocation().makeRelativeTo(root.getLocation());
          IFolder linkedFolder = root.getFolder(actualPath);
          Assert.isTrue(linkedFolder.exists());

          // Check if javaProject also links to the same source folder
          newCpe = findLinkedFolder(javaProject, actualPath);
          if (newCpe != null)
            break;

          // Check if javaProject contains the source folder
          if (linkedFolder.getProject().equals(javaProject.getProject())) {
            IPath path = linkedFolder.getFullPath();
            newCpe = findClasspathEntry(javaProject, IClasspathEntry.CPE_SOURCE, path);

            if (newCpe != null)
              break;
          }
        } else {
          // Check if javaProject links to the same source folder
          newCpe = findLinkedFolder(javaProject, cpe.getPath());
          if (newCpe != null)
            break;
        }

        // check javaProject has this.project in it's classpath
        IPath projectPath = getJavaProject().getPath();
        newCpe = findClasspathEntry(javaProject, IClasspathEntry.CPE_PROJECT, projectPath);
        if (newCpe != null)
          break;

        // Check if javaProject references the classpaths output location
        IPath outputLocation = cpe.getOutputLocation();
        if (cpe.getOutputLocation() == null)
          outputLocation = getJavaProject().getOutputLocation();

        newCpe = findClasspathEntry(javaProject, IClasspathEntry.CPE_LIBRARY,
            outputLocation);

        break;
      case IClasspathEntry.CPE_LIBRARY:
        // this entry describes a folder or JAR containing binaries

        if (cpe.isExported()) {
          projectPath = getJavaProject().getPath();
          newCpe = findClasspathEntry(javaProject, IClasspathEntry.CPE_PROJECT,
              projectPath);
          if (newCpe != null)
            break;
        }

        newCpe = findClasspathEntry(javaProject, IClasspathEntry.CPE_LIBRARY,
            cpe.getPath());
        if (newCpe != null)
          break;

        // If this is a folder
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        if (workspaceLocation.append(cpe.getPath()).toFile().isDirectory()) {

          // check if this is an output folder of something in this project
          for (IClasspathEntry classpathEntry : javaProject.getRawClasspath()) {
            if (cpe.getPath().equals(classpathEntry.getOutputLocation())) {
              TypeMnemonic tmp = new TypeMnemonic(javaProject.getElementName(),
                  classpathEntry.getEntryKind(), classpathEntry.getPath(),
                  getFullyQualifiedName());

              if (tmp.getType() != null) {
                newCpe = classpathEntry;
                break;
              }
            }
          }

          if (javaProject.getOutputLocation().equals(cpe.getPath())) {
            String fqname = getFullyQualifiedName().replace('$', '.');
            IType type = javaProject.findType(fqname, (IProgressMonitor) null);
            IJavaElement pfr = type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);

            newCpe = ((IPackageFragmentRoot) pfr).getRawClasspathEntry();
            break;
          }
        }
        break;
      case IClasspathEntry.CPE_PROJECT:
        // this entry describes another project

        // check if this type's project is the same as javaProject
        if (getClasspath().equals(javaProject.getPath())) {
          // search for the type in the java project - it should exist
          IType type = javaProject.findType(getFullyQualifiedName());
          if (type != null)
            return new TypeMnemonic(type);
        } else {
          // check for the exact same project in javaProject's classpath
          newCpe = findClasspathEntry(javaProject, IClasspathEntry.CPE_PROJECT,
              getClasspath());
        }
        break;
      case IClasspathEntry.CPE_VARIABLE:
        // this entry describes a project or library indirectly via a
        // classpath variable in the first segment of the path *
      case IClasspathEntry.CPE_CONTAINER:
        // this entry describes set of entries referenced indirectly via a
        // classpath. Check for the exact same variable/container in
        // javaProject's classpath
        newCpe = findClasspathEntry(javaProject, cpe.getEntryKind(), cpe.getPath());
        break;
      }

      if (newCpe != null) {
        TypeMnemonic tmp = new TypeMnemonic(javaProject.getElementName(),
            newCpe.getEntryKind(), newCpe.getPath(), getFullyQualifiedName());
        return new TypeMnemonic(tmp.toString(), root);
      }
    }
    return null;
  }

  private IClasspathEntry findLinkedFolder(IJavaProject javaProject, IPath actualPath) throws JavaModelException {
    
    IWorkspaceRoot root = getJavaProject().getCorrespondingResource().getWorkspace().getRoot();
    actualPath = actualPath.makeAbsolute();

    for (IClasspathEntry cpe : javaProject.getRawClasspath()) {
      if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
        IPath path = cpe.getPath().makeRelativeTo(javaProject.getProject().getFullPath());
        IFolder cpeFolder = javaProject.getProject().getFolder(path);
        if (cpeFolder.isLinked()) {
          path = cpeFolder.getLocation().makeRelativeTo(root.getLocation()).makeAbsolute();

          if (actualPath.equals(path)) {
            return cpe;
          }
        }
      }
    }

    return null;
  }
  
  private IClasspathEntry findClasspathEntry(IJavaProject javaProject, int entryKind, IPath path) throws JavaModelException {
    for (IClasspathEntry cpe : javaProject.getRawClasspath()) {
      if (cpe.getEntryKind() == entryKind && cpe.getPath().equals(path)) {
        return cpe;
      }
    }
    return null;
  }

  /**
   * Resolves this type in the within the project defined by
   * <code>TypeMnemonic.getJavaProject()</code>. If the project is
   * <code>null</code>, <code>null</code> is returned. Otherwise, the results of
   * <code>IJavaProject.findType(String)</code> are used to construct and return
   * a new TypeMnemonic.
   * 
   * @return a new TypeMnemonic using the first <code>IType</code> found in the
   *         project to match the fully-qualified name, or <code>null</code> if
   *         the project or type could not be found
   * @throws JavaModelException 
   */
  public TypeMnemonic resolve() throws JavaModelException {
    if (getJavaProject() != null) {
      return resolve(getJavaProject());
    }
    
    return null;
  }

  public TypeMnemonic resolve(IJavaProject javaProject) throws JavaModelException {
    String fqname = getFullyQualifiedName().replace('$', '.');
    
    IType type = javaProject.findType(fqname, (IProgressMonitor) null);
    
    if (type == null) {
      return null;
    }
    
    return new TypeMnemonic(type);
  }

  private static IType findType(IJavaProject javaProject, IClasspathEntry classpathEntry, String fqname) throws JavaModelException {
    IPackageFragmentRoot[] packageFragmentRoots = RandoopCoreUtil.findPackageFragmentRoots(javaProject, classpathEntry);

    String packageName = RandoopCoreUtil.getPackageName(fqname);
    String classFileName = RandoopCoreUtil.getClassName(fqname);

    String typeName;
    if (classFileName.contains("$")) { //$NON-NLS-1$
      typeName = classFileName.substring(classFileName.lastIndexOf('$') + 1);
    } else {
      typeName = classFileName;
    }
    classFileName += ".class"; //$NON-NLS-1$

    for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
      IPackageFragment packageFragment = packageFragmentRoot.getPackageFragment(packageName);
      if (packageFragment.exists()) {
        if (IPackageFragmentRoot.K_BINARY == packageFragment.getKind()) {
          for (IClassFile classFile : packageFragment.getClassFiles()) {
            if (classFile.getElementName().equals(classFileName)) {
              return classFile.getType();
            }
          }
        } else if (IPackageFragmentRoot.K_SOURCE == packageFragment.getKind()) {
          for (ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
            for (IType type : compilationUnit.getAllTypes()) {
              if (type.getElementName().equals(typeName)) {
                return type;
              }
            }
          }
        }
      }
    }

    return null;
  }

  public IJavaProject getJavaProject() {
    return fJavaProject;
  }

  public IClasspathEntry getClasspathEntry() {
    return fClasspathEntry;
  }

  public IType getType() {
    return fType;
  }

  public String getJavaProjectName() {
    return fJavaProjectName;
  }
  
  public int getClasspathKind() {
    return fClasspathKind;
  }
  
  public IPath getClasspath() {
    return fClasspath;
  }

  public String getFullyQualifiedName() {
    return fFullyQualifiedTypeName;
  }

  public boolean exists() {
    return fJavaProject != null && fClasspathEntry != null && fType != null;
  }

  @Override
  public String toString() {
    StringBuilder mnemonic = new StringBuilder();

    mnemonic.append(getJavaProjectName());
    mnemonic.append(IConstants.MNEMONIC_DELIMITER);
    mnemonic.append(getClasspathKind());
    mnemonic.append(IConstants.MNEMONIC_DELIMITER);
    mnemonic.append(getClasspath());
    mnemonic.append(IConstants.MNEMONIC_DELIMITER);
    mnemonic.append(getFullyQualifiedName());

    return mnemonic.toString();
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TypeMnemonic) {
      return toString().equals(((TypeMnemonic) obj).toString());
    }
    return false;
  }

}
