package randoop.plugin.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.StatusFactory;

public class RandoopCoreUtil {
  
  public static String getFullyQualifiedUnresolvedSignature(IMethod method, String typeSignature) throws JavaModelException {
    IType type = method.getDeclaringType();
    
    int arrayCount = Signature.getArrayCount(typeSignature);
    String typeSignatureWithoutArray = typeSignature.substring(arrayCount);
    
    String typeName = Signature.toString(typeSignatureWithoutArray);
    if (method.getTypeParameter(typeName).exists() || type.getTypeParameter(typeName).exists()) {
      String typeSig = Signature.C_TYPE_VARIABLE + typeName + Signature.C_SEMICOLON;
      return Signature.createArraySignature(typeSig, arrayCount);
    }
    String[][] types = type.resolveType(typeName);
    
    StringBuilder fqname = new StringBuilder();
    if (types != null) {
      // Write the first type that was resolved
      fqname.append(types[0][0]); // the package name
      fqname.append('.');
      fqname.append(types[0][1]); // the class name
      
      String typeSig = Signature.createTypeSignature(fqname.toString(), false);
      return Signature.createArraySignature(typeSig, arrayCount);
    } else {
      // Otherwise this is a primitive type, return the signature as it is
      return typeSignature;
    }
    
  }

  public static String getFullyQualifiedName(IType type, String typeSignature) throws JavaModelException {
    String typeName = Signature.toString(typeSignature);
    String[][] types = type.resolveType(typeName);
    
    StringBuilder fqname = new StringBuilder();
    if (types != null) {
      // Write the first type that was resolved
      fqname.append(types[0][0]); // the package name
      fqname.append('.');
      fqname.append(types[0][1]); // the class name
    } else {
      // Otherwise this is a primitive type, write it as it is
      fqname.append(typeName);
    }
    
    return fqname.toString();
  }
  
  public static String getPackageName(IType type, String typeSignature) throws JavaModelException {
    String fqname = RandoopCoreUtil.getFullyQualifiedName(type, typeSignature);
    return RandoopCoreUtil.getPackageName(fqname);
  }
  
  // expects use of $
  public static String getPackageName(String fullyQualifiedName) {
    int lastDelimiter = fullyQualifiedName.lastIndexOf('.');
    
    if (lastDelimiter == -1) {
      return ""; //$NON-NLS-1$
    } else {
      return fullyQualifiedName.substring(0, lastDelimiter);
    }
  }
  
  public static String getClassName(IType type, String typeSignature) throws JavaModelException {
    String fqname = RandoopCoreUtil.getFullyQualifiedName(type, typeSignature);
    return RandoopCoreUtil.getClassName(fqname);
  }
  
  public static String getClassName(String fullyQualifiedName) {
    int lastDelimiter = fullyQualifiedName.lastIndexOf('.');
    
    if (lastDelimiter == -1) {
      return fullyQualifiedName;
    } else {
      return fullyQualifiedName.substring(lastDelimiter + 1);
    }
  }

  public static String getFullyQualifiedName(String packageName, String className) {
    if (packageName.isEmpty()) {
      return className;
    } else {
      return packageName + '.' + className;
    }
  }

  public static IPackageFragmentRoot getPackageFragmentRoot(IJavaProject javaProject, String folder) {
    if (javaProject != null && javaProject.exists() && javaProject.isOpen()) {
      try {
        IPath path = javaProject.getPath().append(folder).makeAbsolute();
        IPackageFragmentRoot pfr = javaProject.findPackageFragmentRoot(path);
        if (pfr != null && pfr.exists()) {
          return pfr;
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    return null;
  }

  /**
   * 
   * @param projectName
   * @return the Java project by the specific name in the workspace, or
   *         <code>null</code> if no Java project by the specified name was
   *         found
   */
  public static IJavaProject getProjectFromName(String projectName) {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IStatus status = workspace.validateName(projectName, IResource.PROJECT);

    if (status.isOK()) {
      IProject project = workspace.getRoot().getProject(projectName);
      
      if (!project.exists())
        return null;
      
      try {
        return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
      } catch (CoreException e) {
        RandoopPlugin.log(e);
      }
    }
    return null;
  }
  
  public static List<IType> findTypes(IJavaElement element, boolean ignoreJUnitTestCases, IProgressMonitor pm) {
    switch (element.getElementType()) {
    case IJavaElement.PACKAGE_FRAGMENT_ROOT:
      IPackageFragmentRoot pfr = (IPackageFragmentRoot) element;
      return findTypes(pfr, ignoreJUnitTestCases, pm);
    case IJavaElement.PACKAGE_FRAGMENT:
      IPackageFragment pf = (IPackageFragment) element;
      return findTypes(pf, ignoreJUnitTestCases, pm);
    case IJavaElement.COMPILATION_UNIT:
      ICompilationUnit cu = (ICompilationUnit) element;
      return findTypes(cu, ignoreJUnitTestCases, pm);
    case IJavaElement.CLASS_FILE:
      IClassFile cf = (IClassFile) element;
      return findTypes(cf, ignoreJUnitTestCases, pm);
    default:
      RandoopPlugin.log(StatusFactory.createErrorStatus("Unexpected Java element type: " //$NON-NLS-1$
          + element.getElementType()));
    }
    return null;
  }

  public static List<IType> findTypes(IPackageFragmentRoot pfr, boolean ignoreJUnitTestCases, IProgressMonitor pm) {
    if (pm == null) {
      pm = new NullProgressMonitor();
    }

    List<IType> types = new ArrayList<IType>();
    try {
      for (IJavaElement e : pfr.getChildren()) {
        Assert.isTrue(e instanceof IPackageFragment);
        IPackageFragment pf = (IPackageFragment) e;
        types.addAll(findTypes(pf, ignoreJUnitTestCases, pm));
      }
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }
    
    return types;
  }
  
  public static List<IType> findTypes(IPackageFragment pf, boolean ignoreJUnitTestCases, IProgressMonitor pm) {
    if (pm == null) {
      pm = new NullProgressMonitor();
    }

    List<IType> types = new ArrayList<IType>();
    try {
      switch (pf.getKind()) {
      case IPackageFragmentRoot.K_BINARY:
        for (IClassFile cf : pf.getClassFiles()) {
          types.addAll(findTypes(cf, ignoreJUnitTestCases, pm));

          if (pm.isCanceled()) {
            return types;
          }
        }
        break;
      case IPackageFragmentRoot.K_SOURCE:
        for (ICompilationUnit cu : pf.getCompilationUnits()) {
          types.addAll(findTypes(cu, ignoreJUnitTestCases, pm));

          if (pm.isCanceled()) {
            return types;
          }
        }
        break;
      }
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }

    return types;
  }
  
  public static List<IType> findTypes(ICompilationUnit cu, boolean ignoreJUnitTestCases, IProgressMonitor pm)  {
    if (pm == null) {
      pm = new NullProgressMonitor();
    }
    
    List<IType> types = new ArrayList<IType>();
    if (cu != null && cu.exists()) {
      try {
        for (IType t : cu.getAllTypes()) {
          if (isValidTestInput(t, ignoreJUnitTestCases)) {
            types.add(t);

            pm.worked(1);
            if (pm.isCanceled()) {
              return types;
            }
          }
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    return types;
  }
  
  public static List<IType> findTypes(IClassFile cf, boolean ignoreJUnitTestCases, IProgressMonitor pm) {
    if (pm == null) {
      pm = new NullProgressMonitor();
    }

    List<IType> types = new ArrayList<IType>();

    if (cf != null && cf.exists()) {
      IType t = cf.getType();
      if (isValidTestInput(t, ignoreJUnitTestCases)) {
        types.add(t);

        pm.worked(1);
        if (pm.isCanceled()) {
          return types;
        }
      }
    }

    return types;
  }

  public static boolean isValidTestInput(IType type, boolean ignoreJUnitTestCases) {
    if (type == null || !type.exists()) {
      return false;
    }
    
    try {
      if (type.isInterface() || Flags.isAbstract(type.getFlags()) || !Flags.isPublic(type.getFlags())) {
        return false;
      }
      if (ignoreJUnitTestCases) {
        // TODO: make sure this is actually of type junit.framework.TestCase
        String siName = type.getSuperclassName();
        if (siName != null && siName.equals("TestCase")) { //$NON-NLS-1$
          return false;
        }
      }
    } catch (JavaModelException e) {
      RandoopPlugin.log(e);
    }

    return true;
  }

  /**
   * TODO: Find out why IJavaProject.findPackageFragmentRoots returns an empty
   * list for IClasspathEntrys of kind CPE_PROJECT.
   * 
   * This is a workaround that find the actual project that is referenced and
   * iterates through its raw classpath, searching for classpath entries that
   * are exported.
   * 
   * @param javaProject
   * @param classpathEntry
   * @return
   * @throws CoreException
   */
  public static IPackageFragmentRoot[] findPackageFragmentRoots(IJavaProject javaProject,
      IClasspathEntry classpathEntry) {
    
    if (classpathEntry == null) {
      return null;
    }
    
    if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
      IWorkspace workspace = javaProject.getProject().getWorkspace();
      IProject project = workspace.getRoot().getProject(classpathEntry.getPath().toString());

      if (project.exists()) {
        IProjectNature referencedProject;
        try {
          referencedProject = project.getNature(JavaCore.NATURE_ID);
          if (referencedProject != null) {
            IJavaProject referencedJavaProject = (IJavaProject) referencedProject;
            List<IPackageFragmentRoot> roots = new ArrayList<IPackageFragmentRoot>();
            for (IClasspathEntry cpe : referencedJavaProject.getRawClasspath()) {
              if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE || cpe.isExported()) {
                roots.addAll(Arrays.asList(findPackageFragmentRoots(referencedJavaProject, cpe)));
              }
            }
            return (IPackageFragmentRoot[]) roots.toArray(new IPackageFragmentRoot[roots.size()]);
          }
        } catch (CoreException e) {
          RandoopPlugin.log(e);
        }
      }
      return null;
    } else {
      return javaProject.findPackageFragmentRoots(classpathEntry);
    }
    
  }
  
}
