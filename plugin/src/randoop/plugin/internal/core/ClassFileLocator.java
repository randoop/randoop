package randoop.plugin.internal.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ClassFileLocator {
  public static IPath findClasspath(IType type) {
    ICompilationUnit javaFile = type.getCompilationUnit();
    if (javaFile == null)
      return null;

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IPath workspacePath = root.getLocation();

    IJavaProject javaProject = javaFile.getJavaProject();
    
    try {
      return workspacePath.append(javaProject.getOutputLocation());
    } catch (JavaModelException e) {
      return null;
    }
  }

  public static IPath findRelativeClassLocation(IType type) {
    ICompilationUnit javaFile = type.getCompilationUnit();
    if (javaFile == null)
      return null;

    String packageName = type.getPackageFragment().getElementName();
    String className = type.getCompilationUnit().getElementName();
    className = className.substring(0, className.indexOf(".java")); //$NON-NLS-1$
    String fullyQualifiedName = packageName + "/" + className; //$NON-NLS-1$

    return new Path('/' + fullyQualifiedName.replace('.', '/') + ".class"); //$NON-NLS-1$
  }

  public static IPath findFullPathToClass(IType type) {
    IPath classpath = findClasspath(type);
    
    if (classpath == null)
      return null;

    IPath fClassLocation = findRelativeClassLocation(type);

    return classpath.append(fClassLocation);
  }
  
  public static IClassFile findIClassFile(IType type) {
    IPath path = findFullPathToClass(type);
    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

    return JavaCore.createClassFileFrom(file);
  }
}