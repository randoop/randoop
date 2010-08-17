package randoop.plugin.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.core.TypeMnemonic;

@SuppressWarnings("nls")
public class ResourcesTest {
  static IJavaProject pathplanner;
  static IJavaProject kenken;

  @BeforeClass
  public static void setUp() throws IOException, CoreException {
    WorkspaceManager.setupDemoWorkspace();
    
    assertNull(WorkspaceManager.getJavaProject("Jf9jf23v55b4338cJR83"));
    
    pathplanner = WorkspaceManager.getJavaProject(WorkspaceManager.PATH_PLANNER);
    assertNotNull(pathplanner);
    assertTrue(pathplanner.exists());

    kenken =  WorkspaceManager.getJavaProject(WorkspaceManager.KENKEN);
    assertNotNull(kenken);
    assertTrue(kenken.exists());
  }

  @Test
  public void testProjects() throws JavaModelException {
    System.out.println("Classpaths for pathplanner:");
    printClasspaths(pathplanner);
    
    System.out.println("Classpaths for kenken:");
    printClasspaths(kenken);
  }
  
  private static void printClasspaths(IJavaProject javaProject) throws JavaModelException {
    for(IClasspathEntry ce : javaProject.getRawClasspath()) {
      System.out.println(ce);
    }
    System.out.println();
    for(IClasspathEntry ce : javaProject.getRawClasspath()) {
      System.out.println(ce.getPath());
    }
  }

  private static List<String> getAllTypeMnemonics(IJavaProject javaproject) throws JavaModelException {
    List<String> mnemonics = new ArrayList<String>();
    for (IClasspathEntry classpathEntry : javaproject.getRawClasspath()) {
      for (IPackageFragmentRoot packageFragmentRoot : javaproject.findPackageFragmentRoots(classpathEntry)) {
        // Find each IMethod in each IType in the src folder and compute its
        // mnemonic. Then store each mnemonic-IMethod pair in a HashMap
        for (IJavaElement e : packageFragmentRoot.getChildren()) {
          assertTrue(e instanceof IPackageFragment);
          IPackageFragment pf = (IPackageFragment) e;
          pf.open(null);

          if (pf.getKind() == IPackageFragmentRoot.K_SOURCE) {
            for (ICompilationUnit cu : pf.getCompilationUnits()) {
              for (IType type : cu.getTypes()) {
                if (!type.isAnonymous()) {
                  int flags = type.getFlags();
                  if (!(Flags.isInterface(flags) || Flags.isAbstract(flags))) {
                    mnemonics.add(new TypeMnemonic(type).toString());
                  }
                }
              }
            }
          }
        }
      }
    }

    return mnemonics;
  }
  
  private static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

}
