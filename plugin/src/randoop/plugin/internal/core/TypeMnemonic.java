package randoop.plugin.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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

public class TypeMnemonic {
  private final static String DELIMITER = "#"; //$NON-NLS-1$

  private final IJavaProject fJavaProject;
  private final IClasspathEntry fClasspathEntry;
  private final IType fType;
  
  private final String fJavaProjectName;
  private final int fClasspathKind;
  private final IPath fClasspath;
  private final String fFullyQualifiedTypeName;

  public TypeMnemonic(IType t) throws JavaModelException {
    Assert.isLegal(t != null);

    fType = t;

    fJavaProject = fType.getJavaProject();

    IJavaElement pfr = fType.getParent();
    while (pfr != null && pfr.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT) {
      pfr = pfr.getParent();
    }
    Assert.isNotNull(pfr);
    fClasspathEntry = ((IPackageFragmentRoot) pfr).getRawClasspathEntry();

    Assert.isNotNull(fJavaProject);
    Assert.isNotNull(fClasspathEntry);

    fJavaProjectName = fJavaProject.getElementName();
    fClasspathKind = fClasspathEntry.getEntryKind();
    fClasspath = fClasspathEntry.getPath();
    fFullyQualifiedTypeName = fType.getFullyQualifiedName();
  }

  public TypeMnemonic(String javaProjectName, int classpathKind,IPath classpath, String fullyQualifiedTypeName) {
    fJavaProjectName=javaProjectName;
    fClasspathKind=classpathKind;
    fClasspath=classpath;
    fFullyQualifiedTypeName=fullyQualifiedTypeName;

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
    
    String[] s = mnemonic.split(DELIMITER);
    Assert.isLegal(s.length == 4);

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
  
  public TypeMnemonic reassign(IJavaProject javaProject) {
    // CPE_SOURCE:
    //   if this is linked:
    //     check if it's in the javaProject as a classpath entry
    //       IPath p = classpathEntry.getPath();
    //       IResource r = kenken.findPackageFragmentRoot(p).getResource();
    //       p = r.getLocation().makeRelativeTo(getWorkspaceRoot().getLocation());
    //   check javaProject has this.project in it's classpath
    //
    // CPE_VARIABLE:
    // CPE_CONTAINER:
    //   check for the exact same variable/container in javaProject's classpath
    //
    // CPE_PROJECT:
    //   check if project is the same as javaProject
    //   check for the exact same project in javaProject's classpath
    //
    // CPE_LIBRARY:
    //   check for the exact same library in javaProject's classpath
    //     (this could be absolute to the file system or relative to the workspace)
    //   if folder:
    //     check if this is an output folder of something in this project
    //     somehow find the IPackageFragmentRoot outputting (maybe javaProject.findType is appropriate here)
    //
    // Find the type and return it with
    // return new TypeMnemonic(type);
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
    
    IProgressMonitor pm = new NullProgressMonitor();
    IType type = javaProject.findType(fqname, pm);
    
    if (type == null) {
      return null;
    }
    
    return new TypeMnemonic(type);
  }

  private static IType findType(IJavaProject javaProject, IClasspathEntry classpathEntry, String fqname) throws JavaModelException {
    IPackageFragmentRoot[] packageFragmentRoots = javaProject.findPackageFragmentRoots(classpathEntry);

    int lastDelimiter = fqname.lastIndexOf('.');
    String packageName = fqname.substring(0, lastDelimiter);
    String classFileName = fqname.substring(lastDelimiter + 1);

    String typeName;
    if (classFileName.contains("$")) { //$NON-NLS-1$
      typeName = classFileName.substring(classFileName.lastIndexOf('$') + 1);
    } else {
      typeName = classFileName;
    }
    classFileName += ".class"; //$NON-NLS-1$

    // TODO: In this one case, a 1 prefixed the fully qualified name, but not
    // the element name even though
    // the type was not anonymous:
    //
    // type.isAnonymous()
    // (boolean) false
    // type.getElementName()
    // (java.lang.String) NullInputStream
    // type.getFullyQualifiedName()
    // (java.lang.String)
    // com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility$1NullInputStream
    // int i = 0;
    // while (i <= typeName.length()) {
    // String prefix = typeName.substring(0, i + 1);
    //      if (prefix.matches("\\p{Digit}*")) { //$NON-NLS-1$
    // i++;
    // } else {
    // break;
    // }
    // }
    // typeName = typeName.substring(i < 0 ? 0 : i);

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
            for (IType type : compilationUnit.getTypes()) {
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
    mnemonic.append(DELIMITER);
    mnemonic.append(getClasspathKind());
    mnemonic.append(DELIMITER);
    mnemonic.append(getClasspath());
    mnemonic.append(DELIMITER);
    mnemonic.append(getFullyQualifiedName());

    return mnemonic.toString();
  }

}
