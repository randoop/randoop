package randoop.plugin.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.junit.BeforeClass;
import org.junit.Test;

import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.TypeMnemonic;

public class ResourcesTest {
  static IJavaProject pathplanner;
  static IJavaProject kenken;

  @BeforeClass
  public static void setUp() {
    WorkspaceManager.clearActiveWorkspace();

    pathplanner = ProjectFactory.createPathPlannerProject();
    assertNotNull(pathplanner);
    assertTrue(pathplanner.exists());

    kenken = ProjectFactory.createKenKenProject();
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

  private static List<String> getTypeMnemonics(IJavaProject javaproject) throws JavaModelException {
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
