package randoop.plugin.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
  private final IPath fClasspath;
  private final String fFullyQualifiedTypeName;

  private final boolean fExists;

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
    fClasspath = fClasspathEntry.getPath();
    fFullyQualifiedTypeName = fType.getFullyQualifiedName();

    fExists = fJavaProject != null && fClasspathEntry != null && fType != null;
  }

  /**
   * 
   * @param mnemonic
   * @throws CoreException
   * @throws IllegalArgumentException
   *           if the mnemonic is incorrectly formatted
   */
  public TypeMnemonic(IWorkspaceRoot root, String mnemonic) {
    String[] s = mnemonic.split(DELIMITER);
    Assert.isLegal(s.length == 3);

    fJavaProjectName = s[0];
    fClasspath = new Path(s[1]);
    fFullyQualifiedTypeName = s[2];

    IProject project = root.getProject(fJavaProjectName);
    
    IJavaProject javaProject = null;
    IClasspathEntry classpathEntry = null;
    IType type = null;

    try {
      if (project.exists()) {
        project.open(null);

        javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
        if (javaProject != null) {
          // Search for the classpath entry in the project
          for (IClasspathEntry cpe : javaProject.getRawClasspath()) {
            if (cpe.getPath().equals(fClasspath)) {
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

    fClasspathEntry = classpathEntry;
    fJavaProject = javaProject;
    fType = type;
    
    fExists = fJavaProject != null && fClasspathEntry != null && fType != null;
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

  public IPath getClasspath() {
    return fClasspath;
  }

  public String getfFullyQualifiedTypeName() {
    return fFullyQualifiedTypeName;
  }

  public boolean isExists() {
    return fExists;
  }

  @Override
  public String toString() {
    StringBuilder mnemonic = new StringBuilder();

    mnemonic.append(getJavaProjectName());
    mnemonic.append(DELIMITER);
    mnemonic.append(getClasspath());
    mnemonic.append(DELIMITER);
    mnemonic.append(getfFullyQualifiedTypeName());

    return mnemonic.toString();
  }

}
