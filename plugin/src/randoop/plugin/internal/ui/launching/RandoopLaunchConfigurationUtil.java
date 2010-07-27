package randoop.plugin.internal.ui.launching;

import java.util.ArrayList;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.StatusFactory;

public class RandoopLaunchConfigurationUtil {

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
   * @return the Java project by the sepcific name in the workspace, or
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
  
}
