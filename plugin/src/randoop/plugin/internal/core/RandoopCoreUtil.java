package randoop.plugin.internal.core;

import java.text.MessageFormat;
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
import org.eclipse.core.runtime.SubMonitor;
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

import randoop.plugin.RandoopPlugin;

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
        IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
        RandoopPlugin.log(s);
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
      
      return JavaCore.create(project);
    }
    return null;
  }
  
  public static List<IType> findTypes(IJavaElement element, boolean ignoreJUnitTestCases, IProgressMonitor monitor) {
    switch (element.getElementType()) {
    case IJavaElement.PACKAGE_FRAGMENT_ROOT:
      IPackageFragmentRoot pfr = (IPackageFragmentRoot) element;
      return findTypes(pfr, ignoreJUnitTestCases, monitor);
    case IJavaElement.PACKAGE_FRAGMENT:
      IPackageFragment pf = (IPackageFragment) element;
      return findTypes(pf, ignoreJUnitTestCases, monitor);
    case IJavaElement.COMPILATION_UNIT:
      ICompilationUnit cu = (ICompilationUnit) element;
      return findTypes(cu, ignoreJUnitTestCases, monitor);
    case IJavaElement.CLASS_FILE:
      IClassFile cf = (IClassFile) element;
      return findTypes(cf, ignoreJUnitTestCases, monitor);
    }
    return null;
  }

  public static List<IType> findTypes(IPackageFragmentRoot pfr, boolean ignoreJUnitTestCases, IProgressMonitor monitor) {
    SubMonitor sm = SubMonitor.convert(monitor);

    List<IType> types = new ArrayList<IType>();
    try {
      IJavaElement[] children = pfr.getChildren();
      String taskName = MessageFormat.format("Searching for Java types in {0}", pfr.getElementName()); 
      sm.beginTask(taskName, children.length);
      for (IJavaElement e : children) {
        Assert.isTrue(e instanceof IPackageFragment);
        IPackageFragment pf = (IPackageFragment) e;
        types.addAll(findTypes(pf, ignoreJUnitTestCases, sm.newChild(1)));
      }
    } catch (JavaModelException e) {
      IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
      RandoopPlugin.log(s);
    } finally {
      sm.done();
    }
    
    return types;
  }
  
  public static List<IType> findTypes(IPackageFragment pf, boolean ignoreJUnitTestCases, IProgressMonitor monitor) {
    SubMonitor sm = SubMonitor.convert(monitor);

    List<IType> types = new ArrayList<IType>();
    try {
      String taskName = MessageFormat.format("Searching for Java types in {0}", pf.getElementName()); 
      switch (pf.getKind()) {
      case IPackageFragmentRoot.K_BINARY:
        IClassFile[] classFiles =  pf.getClassFiles();
        sm.beginTask(taskName, classFiles.length);
        for (IClassFile cf : classFiles) {
          types.addAll(findTypes(cf, ignoreJUnitTestCases, sm.newChild(1)));

          if (sm.isCanceled()) {
            return types;
          }
        }
        break;
      case IPackageFragmentRoot.K_SOURCE:
        ICompilationUnit[] compilationUnits =  pf.getCompilationUnits();
        sm.beginTask(taskName, compilationUnits.length);
        for (ICompilationUnit cu : compilationUnits) {
          types.addAll(findTypes(cu, ignoreJUnitTestCases, sm.newChild(1)));

          if (sm.isCanceled()) {
            return types;
          }
        }
        break;
      }
    } catch (JavaModelException e) {
      IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
      RandoopPlugin.log(s);
    } finally {
      sm.done();
    }

    return types;
  }
  
  public static List<IType> findTypes(ICompilationUnit cu, boolean ignoreJUnitTestCases, IProgressMonitor pm)  {
    SubMonitor sm = SubMonitor.convert(pm);
    
    List<IType> validTypes = new ArrayList<IType>();
    if (cu != null && cu.exists()) {
      try {
        IType[] allTypes = cu.getAllTypes();
        pm.beginTask(MessageFormat.format("Searching for valid Java types in {0}", cu.getElementName()), allTypes.length);
        for (IType t : allTypes) {
          if (isValidTestInput(t, ignoreJUnitTestCases)) {
            validTypes.add(t);

            if (pm.isCanceled()) {
              return validTypes;
            }
          }
          pm.worked(1);
        }
      } catch (JavaModelException e) {
        IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
        RandoopPlugin.log(s);
      }
    }
    
    sm.done();
    return validTypes;
  }
  
  public static List<IType> findTypes(IClassFile cf, boolean ignoreJUnitTestCases, IProgressMonitor monitor) {
    SubMonitor sm = SubMonitor.convert(monitor);

    List<IType> types = new ArrayList<IType>();

    sm.beginTask(MessageFormat.format("Checking if Java type for {0} is valid", cf.getElementName()), 1);
    if (cf != null && cf.exists()) {
      IType t = cf.getType();
      if (isValidTestInput(t, ignoreJUnitTestCases)) {
        types.add(t);
      }
      
      sm.worked(1);
    }
    
    sm.done();
    return types;
  }

  public static boolean isValidTestInput(IType type, boolean ignoreJUnitTestCases) {
    try {
      int flags = type.getFlags();
      if (type.isInterface() || Flags.isAbstract(flags) || !Flags.isPublic(flags)) {
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
      IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
      RandoopPlugin.log(s);
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
   * @throws JavaModelException 
   * @throws CoreException
   */
  public static IPackageFragmentRoot[] findPackageFragmentRoots(IJavaProject javaProject,
      IClasspathEntry classpathEntry) throws JavaModelException {
    
    if (classpathEntry == null) {
      return null;
    }

    if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
      IWorkspace workspace = javaProject.getProject().getWorkspace();
      IProject project = workspace.getRoot().getProject(
          classpathEntry.getPath().toString());

      if (project.exists()) {
        IJavaProject referencedJavaProject = JavaCore.create(project);
        List<IPackageFragmentRoot> roots = new ArrayList<IPackageFragmentRoot>();
        for (IClasspathEntry cpe : referencedJavaProject.getRawClasspath()) {
          if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE || cpe.isExported()) {
            roots.addAll(Arrays.asList(findPackageFragmentRoots(referencedJavaProject, cpe)));
          }
        }
        return (IPackageFragmentRoot[]) roots.toArray(new IPackageFragmentRoot[roots
            .size()]);
      }
      return null;
    } else {
      return javaProject.findPackageFragmentRoots(classpathEntry);
    }

  }
  
}
