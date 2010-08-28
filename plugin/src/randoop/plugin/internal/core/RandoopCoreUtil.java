package randoop.plugin.internal.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
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

/**
 * Provides methods for various JDT and Java-related tasks that are often
 * performed by the Randoop plug-in
 * 
 * @author Peter Kalauskas
 */
public class RandoopCoreUtil {

  /**
   * Returns a method signature in which every type is uses its fully-qualified
   * name and is written using the identifier for unresolved types.
   * 
   * @param method
   * @param typeSignature
   * @return
   * @throws JavaModelException
   * @see {@link Signature}
   */
  public static String getUnresolvedFullyQualifiedMethodSignature(IMethod method, String typeSignature) throws JavaModelException {
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

  /**
   * Returns the package name of the given fully-qualified name. The expected
   * enclosing type separator is <code>'$'</code>.
   * 
   * @param fullyQualifiedName
   * @return the package name, an empty string for the default package
   */
  public static String getPackageName(String fullyQualifiedName) {
    int lastDelimiter = fullyQualifiedName.lastIndexOf('.');
    
    if (lastDelimiter == -1) {
      return ""; //$NON-NLS-1$
    } else {
      return fullyQualifiedName.substring(0, lastDelimiter);
    }
  }

  /**
   * Returns the class name of the given fully-qualified name. The expected
   * enclosing type separator is <code>'$'</code>. The class name will return
   * all parent types as well. For example,
   * <code>'com.example.Graph$Node'</code> will return <code>'Graph$Node'</code>
   * 
   * @param fullyQualifiedName
   * @return the class name of the fully-qualified name
   */
  public static String getClassName(String fullyQualifiedName) {
    int lastDelimiter = fullyQualifiedName.lastIndexOf('.');
    
    if (lastDelimiter == -1) {
      return fullyQualifiedName;
    } else {
      return fullyQualifiedName.substring(lastDelimiter + 1);
    }
  }

  /**
   * Creates a fully-qualified type name for the given package name and class
   * name separated by a <code>'.'</code>. If the package name is empty, only
   * the class name is returned.
   * 
   * @param packageName
   * @param className
   * @return
   */
 public static String getFullyQualifiedName(String packageName, String className) {
    if (packageName.isEmpty()) {
      return className;
    } else {
      return packageName + '.' + className;
    }
  }

  /**
   * Returns the package fragment root for the given folder path in the project.
   * The path to the folder is expected to be relative to the project, so to get
   * the package fragment root for the source folder 'src,' the argument 'src'
   * should be used, <i>not</i> 'someProejct/src'.
   * 
   * @param javaProject
   *          the java project, may be <code>null</code>
   * @param folder
   *          the path to the source folder relative to the project the path to
   *          the folder, 'src'
   * @return the package fragment root, or <code>null</code> if it is not found
   *         or javaProject is <code>null</code> or does not exist
   */
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
   * Returns the Java project by the specified name in the workspace.
   * 
   * @param projectName
   *          the name of the project
   * @return the Java project by the specific name, or <code>null</code> it was
   *         not found
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

  /**
   * Searches for and returns a list of types found in the given java element
   * that may be used for testing. This is simply a <code>switch</code>
   * statement that checks the element's type and calls the appropriate method.
   * 
   * @param element
   *          the java element to search for types
   * @param ignoreJUnitTestCases
   *          <code>true</code> if JUnit test cases should not be returned in
   *          the list
   * @param monitor
   *          the monitor, or <code>null</code>
   * @return a list of types found in the Java element
   */
  public static List<IType> findTestableTypes(IJavaElement element, boolean ignoreJUnitTestCases, IProgressMonitor monitor) {
    switch (element.getElementType()) {
    case IJavaElement.PACKAGE_FRAGMENT_ROOT:
      IPackageFragmentRoot pfr = (IPackageFragmentRoot) element;
      return findTestableTypes(pfr, ignoreJUnitTestCases, monitor);
    case IJavaElement.PACKAGE_FRAGMENT:
      IPackageFragment pf = (IPackageFragment) element;
      return findTestableTypes(pf, ignoreJUnitTestCases, monitor);
    case IJavaElement.COMPILATION_UNIT:
      ICompilationUnit cu = (ICompilationUnit) element;
      return findTestableTypes(cu, ignoreJUnitTestCases, monitor);
    case IJavaElement.CLASS_FILE:
      IClassFile cf = (IClassFile) element;
      return findTestableTypes(cf, ignoreJUnitTestCases, monitor);
    }
    return null;
  }

  /**
   * Searches for and returns a list of types found in the given package
   * fragment root.
   * 
   * @param pfr
   * @param ignoreJUnitTestCases
   *          <code>true</code> if JUnit test cases should not be returned in
   *          the list
   * @param monitor
   *          the monitor, or <code>null</code>
   * @return a list of types found in the package fragment root
   */
  public static List<IType> findTestableTypes(IPackageFragmentRoot pfr, boolean ignoreJUnitTestCases, IProgressMonitor monitor) {
    SubMonitor sm = SubMonitor.convert(monitor);

    List<IType> types = new ArrayList<IType>();
    try {
      IJavaElement[] children = pfr.getChildren();
      String taskName = MessageFormat.format("Searching for Java types in {0}", pfr.getElementName()); 
      sm.beginTask("", children.length); //$NON-NLS-1$
      sm.subTask(taskName);
      for (IJavaElement e : children) {
        Assert.isTrue(e instanceof IPackageFragment);
        IPackageFragment pf = (IPackageFragment) e;
        types.addAll(findTestableTypes(pf, ignoreJUnitTestCases, sm.newChild(1)));
      }
    } catch (JavaModelException e) {
      IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
      RandoopPlugin.log(s);
    } finally {
      sm.done();
    }
    
    return types;
  }
  
  /**
   * Searches for and returns a list of types found in the given package
   * fragment.
   * 
   * @param pf
   * @param ignoreJUnitTestCases
   *          <code>true</code> if JUnit test cases should not be returned in
   *          the list
   * @param monitor
   *          the monitor, or <code>null</code>
   * @return a list of types found in the package fragment
   */
  public static List<IType> findTestableTypes(IPackageFragment pf, boolean ignoreJUnitTestCases, IProgressMonitor monitor) {
    SubMonitor sm = SubMonitor.convert(monitor);

    List<IType> types = new ArrayList<IType>();
    try {
      String taskName = MessageFormat.format("Searching for Java types in {0}", pf.getElementName()); 
      switch (pf.getKind()) {
      case IPackageFragmentRoot.K_BINARY:
        IClassFile[] classFiles =  pf.getClassFiles();
        sm.beginTask("", classFiles.length); //$NON-NLS-1$
        sm.subTask(taskName);
        for (IClassFile cf : classFiles) {
          types.addAll(findTestableTypes(cf, ignoreJUnitTestCases, sm.newChild(1)));

          if (sm.isCanceled()) {
            return types;
          }
        }
        break;
      case IPackageFragmentRoot.K_SOURCE:
        ICompilationUnit[] compilationUnits =  pf.getCompilationUnits();
        sm.beginTask("", compilationUnits.length); //$NON-NLS-1$
        sm.subTask(taskName);
        for (ICompilationUnit cu : compilationUnits) {
          types.addAll(findTestableTypes(cu, ignoreJUnitTestCases, sm.newChild(1)));

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
  
  /**
   * Searches for and returns a list of types found in the given compilation
   * unit.
   * 
   * @param cu
   * @param ignoreJUnitTestCases
   *          <code>true</code> if JUnit test cases should not be returned in
   *          the list
   * @param monitor
   *          the monitor, or <code>null</code>
   * @return a list of types found in the compilation unit
   */
  public static List<IType> findTestableTypes(ICompilationUnit cu, boolean ignoreJUnitTestCases, IProgressMonitor pm)  {
    SubMonitor sm = SubMonitor.convert(pm);
    
    List<IType> validTypes = new ArrayList<IType>();
    if (cu != null && cu.exists()) {
      try {
        IType[] allTypes = cu.getAllTypes();
        sm.beginTask("", allTypes.length); //$NON-NLS-1$
        sm.subTask(MessageFormat.format("Searching for valid Java types in {0}", cu.getElementName()));
        for (IType t : allTypes) {
          if (isValidTestInput(t, ignoreJUnitTestCases)) {
            validTypes.add(t);

            if (sm.isCanceled()) {
              return validTypes;
            }
          }
          sm.worked(1);
        }
      } catch (JavaModelException e) {
        IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
        RandoopPlugin.log(s);
      }
    }
    
    sm.done();
    return validTypes;
  }

  /**
   * Searches for and returns a list of types found in the given class file.
   * The list can only contain one or zero elements.
   * 
   * @param cf
   * @param ignoreJUnitTestCases
   *          <code>true</code> if JUnit test cases should not be returned in
   *          the list
   * @param monitor
   *          the monitor, or <code>null</code>
   * @return a list of types found in the class file
   */
  public static List<IType> findTestableTypes(IClassFile cf, boolean ignoreJUnitTestCases, IProgressMonitor monitor) {
    SubMonitor sm = SubMonitor.convert(monitor);

    List<IType> types = new ArrayList<IType>();

    sm.beginTask("", 1); //$NON-NLS-1$
    sm.subTask(MessageFormat.format("Checking if Java type for {0} is valid", cf.getElementName()));
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

  /**
   * Checks if the given type may be tested with Randoop. Non-public classes,
   * abstract classes, and interfaces may not be tested.
   * 
   * @param type
   * @param ignoreJUnitTestCases
   *          <code>true</code> if this method should return <code>false</code>
   *          if the type is a test case
   * @return <code>true</code> if the type may be tested with Randoop
   */
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
